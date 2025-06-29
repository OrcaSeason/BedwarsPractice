package io.dogsbean.bedwarspractice.game;

import io.dogsbean.bedwarspractice.BedWarsPractice;
import io.dogsbean.bedwarspractice.game.map.MapManager;
import io.dogsbean.bedwarspractice.lobby.LobbyManager;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Game {
    private final BedWarsPractice plugin;
    private final Player player;
    private final String mapName;
    @Getter private GameState state;
    @Getter private final Date startedAt;
    @Getter private Date endedAt;
    private BukkitTask damageCheckTask;
    private BukkitTask woolBreakTask;
    private final GameSettings settings;
    private final LinkedList<Location> placedWoolBlocks;

    public Game(BedWarsPractice plugin, Player player, String mapName, GameSettings settings) {
        this.plugin = plugin;
        this.player = player;
        this.mapName = mapName;
        this.state = GameState.STARTING;
        this.startedAt = new Date();
        this.settings = settings;
        this.placedWoolBlocks = new LinkedList<>();
    }

    public void start() {
        if (MapManager.isPlayerWorldExists(player.getUniqueId())) {
            player.sendMessage("§cPlease try again later.");
            plugin.getGameManager().endGame(player, false);
            return;
        }

        MapManager.createPlayerWorldAsync(mapName, player.getUniqueId(),
                spawnPoint -> {
                    player.teleport(spawnPoint);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.getInventory().clear();
                    player.setHealth(20.0);
                    player.setFoodLevel(20);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        giveStartingItems();
                        player.updateInventory();
                    }, 1L);

                    player.sendMessage("\n§6§lBreak the Bed!");
                    player.sendMessage("§7Stay surrounded!\n" +
                            "§7If there's any open space around or above you,\n" +
                            "§7you'll lose health over time.\n");

                    new BukkitRunnable() {
                        int count = 3;

                        @Override
                        public void run() {
                            if (count > 0) {
                                player.sendMessage(ChatColor.YELLOW.toString() + count + "...");
                                count--;
                            } else {
                                player.sendMessage("§a§lGame Started!");
                                state = GameState.PLAYING;
                                startDamageCheck();
                                if (settings.isAutoBreakWool()) {
                                    startWoolBreakTask();
                                }
                                cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 0L, 20L);
                },
                () -> {
                    player.sendMessage("§cMap is not properly set up!");
                    plugin.getGameManager().endGame(player, false);
                }
        );
    }

    private void startDamageCheck() {
        damageCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state != GameState.PLAYING) {
                    cancel();
                    return;
                }

                Location loc = player.getLocation();
                boolean isSafe = true;

                Block currentBlock = loc.getBlock();
                Block headBlock = loc.clone().add(0, 1, 0).getBlock();
                Block topBlock = loc.clone().add(0, 2, 0).getBlock();

                BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

                if (topBlock.getType() == Material.AIR) {
                    isSafe = false;
                }

                for (BlockFace face : faces) {
                    Block sideBlock = currentBlock.getRelative(face);
                    Block headSideBlock = headBlock.getRelative(face);

                    if (sideBlock.getType() == Material.AIR || headSideBlock.getType() == Material.AIR) {
                        isSafe = false;
                        break;
                    }
                }

                if (!isSafe) {
                    player.damage(settings.getDamageAmount());
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void startWoolBreakTask() {
        if (woolBreakTask != null) {
            woolBreakTask.cancel();
        }

        woolBreakTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state != GameState.PLAYING) {
                    cancel();
                    return;
                }

                if (!placedWoolBlocks.isEmpty()) {
                    Location woolLoc = placedWoolBlocks.removeFirst();
                    Block block = woolLoc.getBlock();
                    if (block.getType() == Material.WOOL) {
                        block.setType(Material.AIR);
                        block.getWorld().playEffect(woolLoc, Effect.STEP_SOUND, Material.WOOL);
                    }
                }
            }
        }.runTaskTimer(
                plugin,
                settings.getWoolBreakDelay() / 50,
                settings.getWoolBreakInterval() / 50
        );
    }

    public void addWoolBlock(Location location) {
        if (settings.isAutoBreakWool()) {
            placedWoolBlocks.addLast(location.clone());

            if (woolBreakTask == null) {
                startWoolBreakTask();
            }
        }
    }

    private void giveStartingItems() {
        player.getInventory().setItem(0, new ItemStack(Material.SHEARS));

        ItemStack axe = new ItemStack(Material.WOOD_AXE);
        ItemMeta axeM = axe.getItemMeta();
        axeM.addEnchant(org.bukkit.enchantments.Enchantment.DIG_SPEED, 1, true);
        axe.setItemMeta(axeM);
        player.getInventory().setItem(1, axe);

        ItemStack pickaxe = new ItemStack(Material.WOOD_PICKAXE);
        ItemMeta pickaxeM = pickaxe.getItemMeta();
        pickaxeM.addEnchant(org.bukkit.enchantments.Enchantment.DIG_SPEED, 1, true);
        pickaxe.setItemMeta(pickaxeM);
        player.getInventory().setItem(2, pickaxe);

        player.getInventory().setItem(3, new ItemStack(Material.WOOL, 64));
    }

    public void end(boolean won) {
        if (state == GameState.ENDED) return;

        state = GameState.ENDED;
        endedAt = new Date();

        if (damageCheckTask != null) {
            damageCheckTask.cancel();
        }

        if (woolBreakTask != null) {
            woolBreakTask.cancel();
        }

        String message = won ? "§a§lYou won!" : "§c§lYou lost!";
        player.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + "-----------------------------------------------");
        player.sendMessage(message);
        player.sendMessage(" §f⚫ §a§lDuration: §r" + getGameDuration());
        player.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + "-----------------------------------------------");

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            MapManager.deletePlayerWorld(player.getUniqueId());
        }, 20L);

        player.teleport(MapManager.getLobbySpawn());
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            LobbyManager.giveLobbyItems(player);
            player.updateInventory();
        }, 1L);
    }

    public String getGameDuration() {
        long endTimeMillis = (endedAt != null) ? endedAt.getTime() : System.currentTimeMillis();
        long durationMillis = endTimeMillis - startedAt.getTime();
        long durationSeconds = durationMillis / 1000;
        long minutes = durationSeconds / 60;
        long seconds = durationSeconds % 60;

        return minutes + "m " + seconds + "s";
    }

    public UUID getPlayerId() {
        return player.getUniqueId();
    }

    public String getMapName() {
        return mapName;
    }
}