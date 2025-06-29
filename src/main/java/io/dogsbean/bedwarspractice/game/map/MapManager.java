package io.dogsbean.bedwarspractice.game.map;

import io.dogsbean.bedwarspractice.BedWarsPractice;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MapManager {
    @Setter
    private static Location lobbySpawn;

    private static final ConcurrentHashMap<UUID, String> playerWorlds = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, MapCache> mapCache = new ConcurrentHashMap<>();

    private static class MapCache {
        final ConfigurationSection blocks;
        final Location spawn;
        final int blockCount;

        MapCache(ConfigurationSection blocks, Location spawn) {
            this.blocks = blocks;
            this.spawn = spawn;
            this.blockCount = blocks != null ? blocks.getKeys(false).size() : 0;
        }
    }

    public static Location getLobbySpawn() {
        if (lobbySpawn == null) {
            return BedWarsPractice.getInstance().getServer().getWorlds().get(0).getSpawnLocation();
        }
        return lobbySpawn;
    }

    public static void createMap(String mapName) {
        BedWarsPractice.getInstance().getMapConfig().createSection("maps." + mapName);
        BedWarsPractice.getInstance().saveMapConfig();
    }

    public static void createPlayerWorldAsync(String mapName, UUID playerUUID, Consumer<Location> onComplete, Runnable onError) {
        if (!mapExists(mapName)) {
            onError.run();
            return;
        }

        String existingWorld = playerWorlds.get(playerUUID);
        if (existingWorld != null) {
            World world = BedWarsPractice.getInstance().getServer().getWorld(existingWorld);
            if (world != null) {
                Location spawn = getCachedSpawn(mapName);
                if (spawn != null) {
                    Location playerSpawn = new Location(world, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), spawn.getPitch());
                    ensureChunksLoaded(world, playerSpawn, () -> {
                        onComplete.accept(playerSpawn);
                    });
                    return;
                }
            }
        }

        String worldName = "game_" + playerUUID.toString().substring(0, 8) + "_" + mapName;

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    WorldCreator creator = new WorldCreator(worldName);
                    creator.type(WorldType.FLAT);
                    creator.generatorSettings("2;0;1;");
                    World world = creator.createWorld();

                    world.setTicksPerAnimalSpawns(Integer.MAX_VALUE);
                    world.setTicksPerMonsterSpawns(Integer.MAX_VALUE);
                    world.setGameRuleValue("doMobSpawning", "false");
                    world.setGameRuleValue("doDaylightCycle", "false");
                    world.setGameRuleValue("doWeatherCycle", "false");
                    world.setGameRuleValue("randomTickSpeed", "0");
                    world.setGameRuleValue("doFireTick", "false");
                    world.setGameRuleValue("mobGriefing", "false");
                    world.setTime(6000);
                    world.setStorm(false);

                    playerWorlds.put(playerUUID, worldName);

                    Location spawn = getCachedSpawn(mapName);
                    if (spawn != null) {
                        Location playerSpawn = new Location(world, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), spawn.getPitch());

                        loadChunksAndBlocksAsync(world, mapName, playerSpawn, () -> onComplete.accept(playerSpawn));

                    } else {
                        onError.run();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onError.run();
                }
            }
        }.runTask(BedWarsPractice.getInstance());
    }

    private static void loadChunksAndBlocksAsync(World world, String mapName, Location playerSpawn, Runnable onComplete) {
        ensureChunksLoaded(world, playerSpawn, null);
        loadCriticalBlocks(world, mapName);

        new BukkitRunnable() {
            @Override
            public void run() {
                loadMapBlocksGradually(world, mapName);
                onComplete.run();
            }
        }.runTaskLater(BedWarsPractice.getInstance(), 3L);
    }

    private static void ensureChunksLoaded(World world, Location center, Runnable onComplete) {
        int centerChunkX = center.getBlockX() >> 4;
        int centerChunkZ = center.getBlockZ() >> 4;
        int radius = 3;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;

                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    world.loadChunk(chunkX, chunkZ);
                }
            }
        }

        if (onComplete != null) {
            onComplete.run();
        }
    }

    private static void loadCriticalBlocks(World world, String mapName) {
        MapCache cache = getMapCache(mapName);
        if (cache == null || cache.blocks == null) return;

        Location spawn = cache.spawn;
        if (spawn == null) return;

        for (String key : cache.blocks.getKeys(false)) {
            ConfigurationSection blockSection = cache.blocks.getConfigurationSection(key);
            if (blockSection == null) continue;

            double x = blockSection.getDouble("x");
            double y = blockSection.getDouble("y");
            double z = blockSection.getDouble("z");

            if (Math.abs(x - spawn.getX()) <= 5 && Math.abs(z - spawn.getZ()) <= 5) {
                try {
                    String type = blockSection.getString("type");
                    byte data = (byte) blockSection.getInt("data");

                    Location loc = new Location(world, x, y, z);
                    Block block = loc.getBlock();
                    block.setType(org.bukkit.Material.valueOf(type));
                    block.setData(data);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void loadMapBlocksGradually(World world, String mapName) {
        MapCache cache = getMapCache(mapName);
        if (cache == null || cache.blocks == null) return;

        String[] keys = cache.blocks.getKeys(false).toArray(new String[0]);
        final int BLOCKS_PER_TICK = 100;

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                int processed = 0;

                while (index < keys.length && processed < BLOCKS_PER_TICK) {
                    String key = keys[index];
                    ConfigurationSection blockSection = cache.blocks.getConfigurationSection(key);

                    if (blockSection != null) {
                        try {
                            double x = blockSection.getDouble("x");
                            double y = blockSection.getDouble("y");
                            double z = blockSection.getDouble("z");
                            String type = blockSection.getString("type");
                            byte data = (byte) blockSection.getInt("data");

                            Location loc = new Location(world, x, y, z);
                            Block block = loc.getBlock();

                            if (block.getType() == org.bukkit.Material.AIR) {
                                block.setType(org.bukkit.Material.valueOf(type));
                                block.setData(data);
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    index++;
                    processed++;
                }

                if (index >= keys.length) {
                    this.cancel();
                }
            }
        }.runTaskTimer(BedWarsPractice.getInstance(), 1L, 1L);
    }

    public static void setSpawnPoint(String mapName, Location location) {
        saveLocationToConfig("maps." + mapName + ".spawn", location);
        mapCache.remove(mapName);
    }

    public static void saveMapBlocks(String mapName, Location pos1, Location pos2) {
        ConfigurationSection mapSection = BedWarsPractice.getInstance().getMapConfig().getConfigurationSection("maps." + mapName);
        if (mapSection == null) {
            mapSection = BedWarsPractice.getInstance().getMapConfig().createSection("maps." + mapName);
        }

        mapSection.set("blocks", null);
        ConfigurationSection blocksSection = mapSection.createSection("blocks");

        int blockCount = 0;
        World world = pos1.getWorld();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());

        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);

                    if (block.getType() != org.bukkit.Material.AIR) {
                        ConfigurationSection blockSection = blocksSection.createSection(String.valueOf(blockCount));

                        blockSection.set("x", (double) x);
                        blockSection.set("y", (double) y);
                        blockSection.set("z", (double) z);
                        blockSection.set("type", block.getType().name());
                        blockSection.set("data", block.getData());
                        blockCount++;
                    }
                }
            }
        }

        BedWarsPractice.getInstance().saveMapConfig();
        mapCache.remove(mapName);
    }

    public static String getAvailableMap() {
        ConfigurationSection mapsSection = BedWarsPractice.getInstance().getMapConfig().getConfigurationSection("maps");
        if (mapsSection == null) return null;

        for (String mapName : mapsSection.getKeys(false)) {
            if (isMapComplete(mapName)) {
                return mapName;
            }
        }
        return null;
    }

    public static boolean isPlayerWorldExists(UUID uuid) {
        String worldName = playerWorlds.get(uuid);
        return worldName != null;
    }

    public static void deletePlayerWorldAsync(UUID playerUUID, Runnable onComplete) {
        String worldName = playerWorlds.get(playerUUID);
        if (worldName == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        World world = BedWarsPractice.getInstance().getServer().getWorld(worldName);
        if (world != null) {
            world.getPlayers().forEach(player -> player.teleport(getLobbySpawn()));
            BedWarsPractice.getInstance().getServer().unloadWorld(world, false);

            new Thread(() -> {
                try {
                    File worldFolder = world.getWorldFolder();
                    if (worldFolder.exists()) {
                        deleteWorldFilesFast(worldFolder.toPath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (onComplete != null) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                onComplete.run();
                            }
                        }.runTask(BedWarsPractice.getInstance());
                    }
                }
            }).start();
        }

        playerWorlds.remove(playerUUID);
    }

    public static void deletePlayerWorld(UUID playerUUID) {
        deletePlayerWorldAsync(playerUUID, null);
    }

    private static void deleteWorldFilesFast(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static MapCache getMapCache(String mapName) {
        return mapCache.computeIfAbsent(mapName, name -> {
            ConfigurationSection mapSection = BedWarsPractice.getInstance().getMapConfig().getConfigurationSection("maps." + name);
            if (mapSection == null) return null;

            ConfigurationSection blocksSection = mapSection.getConfigurationSection("blocks");
            Location spawn = getLocationFromConfig("maps." + name + ".spawn");

            return new MapCache(blocksSection, spawn);
        });
    }

    private static Location getCachedSpawn(String mapName) {
        MapCache cache = getMapCache(mapName);
        return cache != null ? cache.spawn : null;
    }

    private static boolean isMapComplete(String mapName) {
        ConfigurationSection mapSection = BedWarsPractice.getInstance().getMapConfig().getConfigurationSection("maps." + mapName);
        return mapSection != null &&
                mapSection.contains("spawn") &&
                mapSection.contains("blocks");
    }

    private static Location getLocationFromConfig(String path) {
        return (Location) BedWarsPractice.getInstance().getMapConfig().get(path);
    }

    private static void saveLocationToConfig(String path, Location location) {
        BedWarsPractice.getInstance().getMapConfig().set(path, location);
        BedWarsPractice.getInstance().saveMapConfig();
    }

    public static boolean mapExists(String mapName) {
        ConfigurationSection mapsSection = BedWarsPractice.getInstance().getMapConfig().getConfigurationSection("maps");
        if (mapsSection == null) return false;

        return mapsSection.contains(mapName);
    }

    public static void shutdown() {
        for (UUID playerUUID : playerWorlds.keySet()) {
            deletePlayerWorld(playerUUID);
        }
        playerWorlds.clear();
        mapCache.clear();
    }
}