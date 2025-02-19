package io.dogsbean.bedwarspractice.lobby;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyManager {
    public static void giveLobbyItems(Player player) {
        player.getInventory().clear();

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.setDisplayName("§aJoin Game");
        compass.setItemMeta(compassMeta);

        player.getInventory().setItem(4, compass);
    }
}
