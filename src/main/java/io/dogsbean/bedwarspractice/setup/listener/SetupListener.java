package io.dogsbean.bedwarspractice.setup.listener;

import io.dogsbean.bedwarspractice.setup.manager.SetupManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SetupListener implements Listener {

    private final SetupManager setupManager;
    public static final String WAND_NAME = ChatColor.GOLD + "Selection Wand";

    public SetupListener(SetupManager setupManager) {
        this.setupManager = setupManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getItemInHand();

        if (itemInHand == null || itemInHand.getType() != Material.GOLD_AXE || !itemInHand.hasItemMeta() || !itemInHand.getItemMeta().getDisplayName().equals(WAND_NAME)) {
            return;
        }

        event.setCancelled(true);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Location loc = event.getClickedBlock().getLocation();
            setupManager.setPos1(player, loc);
            player.sendMessage(ChatColor.YELLOW + "Position 1 set to (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location loc = event.getClickedBlock().getLocation();
            setupManager.setPos2(player, loc);
            player.sendMessage(ChatColor.YELLOW + "Position 2 set to (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
        }
    }
}