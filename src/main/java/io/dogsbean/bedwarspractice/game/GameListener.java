package io.dogsbean.bedwarspractice.game;

import io.dogsbean.bedwarspractice.BedWarsPractice;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Arrays;
import java.util.List;

public class GameListener implements Listener {
    private final BedWarsPractice plugin;
    private static final List<Material> ALLOWED_BREAK = Arrays.asList(
            Material.WOOD,
            Material.ENDER_STONE,
            Material.WOOL,
            Material.BED_BLOCK,
            Material.BED
    );

    public GameListener(BedWarsPractice plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (plugin.getGameManager().isPlayerInGame(player)) {
            event.getDrops().clear();
            event.getEntity().setHealth(event.getEntity().getMaxHealth());
            plugin.getGameManager().endGame(player, false);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getGameManager().isPlayerInGame(player)) {
            return;
        }

        if (plugin.getGameManager().getPlayerGameState(player) != GameState.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getGameManager().isPlayerInGame(player)) {
            return;
        }

        GameState playerState = plugin.getGameManager().getPlayerGameState(player);
        if (playerState == GameState.STARTING) {
            event.setCancelled(true);
            return;
        }

        if (!ALLOWED_BREAK.contains(event.getBlock().getType())) {
            event.setCancelled(true);
            return;
        }

        if (event.getBlock().getType() == Material.BED_BLOCK) {
            plugin.getGameManager().endGame(player, true);
        }
    }
}