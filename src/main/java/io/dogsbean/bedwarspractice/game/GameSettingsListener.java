package io.dogsbean.bedwarspractice.game;

import io.dogsbean.bedwarspractice.BedWarsPractice;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GameSettingsListener implements Listener {
    private final BedWarsPractice plugin;

    public GameSettingsListener(BedWarsPractice plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Game Settings")) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        GameSettings settings = plugin.getGameManager().getPlayerSettings(player);
        if (settings == null) return;

        if (event.getCurrentItem() == null) return;

        switch (event.getCurrentItem().getType()) {
            case REDSTONE:
                // Damage amount setting
                if (event.isLeftClick()) {
                    settings.setDamageAmount(Math.min(10.0, settings.getDamageAmount() + 0.5));
                } else if (event.isRightClick()) {
                    settings.setDamageAmount(Math.max(0.5, settings.getDamageAmount() - 0.5));
                }
                player.sendMessage(ChatColor.YELLOW + "Damage set to: " + settings.getDamageAmount() + " hearts");
                break;

            case WOOL:
                settings.setAutoBreakWool(!settings.isAutoBreakWool());
                player.sendMessage(ChatColor.YELLOW + "Auto break wool: " +
                        (settings.isAutoBreakWool() ? "Enabled" : "Disabled"));
                break;

            case WATCH:
                if (event.isLeftClick()) {
                    settings.setWoolBreakDelay(Math.min(10000, settings.getWoolBreakDelay() + 500));
                } else if (event.isRightClick()) {
                    settings.setWoolBreakDelay(Math.max(500, settings.getWoolBreakDelay() - 500));
                }
                player.sendMessage(ChatColor.YELLOW + "Wool break delay set to: " +
                        (settings.getWoolBreakDelay() / 1000.0) + " seconds");
                break;

            case HOPPER:
                if (event.isLeftClick()) {
                    settings.setWoolBreakInterval(Math.min(2000, settings.getWoolBreakInterval() + 100));
                } else if (event.isRightClick()) {
                    settings.setWoolBreakInterval(Math.max(100, settings.getWoolBreakInterval() - 100));
                }
                player.sendMessage(ChatColor.YELLOW + "Wool break interval set to: " +
                        (settings.getWoolBreakInterval() / 1000.0) + " seconds");
                break;

            case EMERALD:
                player.closeInventory();
                String mapName = plugin.getGameManager().getPlayerMap(player);
                if (mapName != null) {
                    plugin.getGameManager().startGame(player, mapName);
                }
                break;
        }

        if (event.getCurrentItem().getType() != Material.EMERALD) {
            new GameSettingsGUI(settings, plugin.getGameManager().getPlayerMap(player)).open(player);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.WOOL) return;

        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game != null) {
            game.addWoolBlock(event.getBlock().getLocation());
        }
    }
}