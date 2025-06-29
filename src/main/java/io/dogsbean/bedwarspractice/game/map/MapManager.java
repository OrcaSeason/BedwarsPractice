package io.dogsbean.bedwarspractice.game.map;

import io.dogsbean.bedwarspractice.BedWarsPractice;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.block.Block;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class MapManager {
    @Setter
    private static Location lobbySpawn;
    private static final HashMap<UUID, String> playerWorlds = new HashMap<>();

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

    public static Location getSpawnPoint(String mapName, UUID playerUUID) {
        if (!mapExists(mapName)) {
            return null;
        }

        String worldName = createWorldForPlayer(mapName, playerUUID);
        if (worldName == null) return null;

        Location originalSpawn = getLocationFromConfig("maps." + mapName + ".spawn");
        if (originalSpawn == null) return null;

        World playerWorld = BedWarsPractice.getInstance().getServer().getWorld(worldName);
        return new Location(
                playerWorld,
                originalSpawn.getX(),
                originalSpawn.getY(),
                originalSpawn.getZ(),
                originalSpawn.getYaw(),
                originalSpawn.getPitch()
        );
    }

    public static void setSpawnPoint(String mapName, Location location) {
        saveLocationToConfig("maps." + mapName + ".spawn", location);
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

    private static String createWorldForPlayer(String mapName, UUID playerUUID) {
        String worldName = "game_" + playerUUID.toString().substring(0, 8) + "_" + mapName;

        if (playerWorlds.containsKey(playerUUID)) {
            return playerWorlds.get(playerUUID);
        }

        WorldCreator creator = new WorldCreator(worldName);
        creator.type(WorldType.FLAT);
        creator.generatorSettings("2;0;1;");
        World world = creator.createWorld();

        if (!loadMapStructure(world, mapName)) {
            return null;
        }

        playerWorlds.put(playerUUID, worldName);
        return worldName;
    }

    private static boolean loadMapStructure(World world, String mapName) {
        ConfigurationSection mapSection = BedWarsPractice.getInstance().getMapConfig().getConfigurationSection("maps." + mapName);
        if (mapSection == null) return false;

        ConfigurationSection blocksSection = mapSection.getConfigurationSection("blocks");
        if (blocksSection == null) return false;

        for (String key : blocksSection.getKeys(false)) {
            ConfigurationSection blockSection = blocksSection.getConfigurationSection(key);
            if (blockSection == null) continue;

            double x = blockSection.getDouble("x");
            double y = blockSection.getDouble("y");
            double z = blockSection.getDouble("z");
            String type = blockSection.getString("type");
            byte data = (byte) blockSection.getInt("data");

            Location loc = new Location(world, x, y, z);
            Block block = loc.getBlock();
            block.setType(org.bukkit.Material.valueOf(type));
            block.setData(data);
        }

        return true;
    }

    public static void deletePlayerWorld(UUID playerUUID) {
        String worldName = playerWorlds.get(playerUUID);
        if (worldName == null) return;

        World world = BedWarsPractice.getInstance().getServer().getWorld(worldName);
        if (world != null) {
            world.getPlayers().forEach(player -> player.teleport(getLobbySpawn()));
            BedWarsPractice.getInstance().getServer().unloadWorld(world, false);

            File worldFolder = world.getWorldFolder();
            if (worldFolder.exists()) {
                deleteWorldFiles(worldFolder);
            }
        }

        playerWorlds.remove(playerUUID);
    }

    private static void deleteWorldFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteWorldFiles(child);
                }
            }
        }
        file.delete();
    }

    public static boolean mapExists(String mapName) {
        ConfigurationSection mapsSection = BedWarsPractice.getInstance().getMapConfig().getConfigurationSection("maps");
        if (mapsSection == null) return false;

        return mapsSection.contains(mapName);
    }
}