package io.dogsbean.bedwarspractice.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GameSettingsGUI {
    private static final String GUI_TITLE = "Game Settings";
    private final GameSettings settings;
    private final String mapName;

    public GameSettingsGUI(GameSettings settings, String mapName) {
        this.settings = settings;
        this.mapName = mapName;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        ItemStack damageItem = new ItemStack(Material.REDSTONE);
        ItemMeta damageMeta = damageItem.getItemMeta();
        damageMeta.setDisplayName(ChatColor.RED + "Damage Amount");
        damageMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Current: " + settings.getDamageAmount() + " hearts",
                "",
                ChatColor.YELLOW + "Left-click to increase",
                ChatColor.YELLOW + "Right-click to decrease"
                ,
                "",
                ChatColor.GRAY + "You can adjust the damage by clicking \n" +
                        "this button to either increase or decrease it."
        ));
        damageItem.setItemMeta(damageMeta);
        inv.setItem(10, damageItem);

        ItemStack woolItem = new ItemStack(Material.WOOL);
        ItemMeta woolMeta = woolItem.getItemMeta();
        woolMeta.setDisplayName(ChatColor.YELLOW + "Auto Break Wool");
        woolMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Current: " + (settings.isAutoBreakWool() ? "Enabled" : "Disabled"),
                "",
                ChatColor.YELLOW + "Click to toggle",
                "",
                ChatColor.GRAY + "Placed wool will automatically break \n" +
                        "after a set amount of time has passed."
        ));
        woolItem.setItemMeta(woolMeta);
        inv.setItem(12, woolItem);

        ItemStack delayItem = new ItemStack(Material.WATCH);
        ItemMeta delayMeta = delayItem.getItemMeta();
        delayMeta.setDisplayName(ChatColor.AQUA + "Wool Break Delay");
        delayMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Current: " + (settings.getWoolBreakDelay() / 1000.0) + " seconds",
                "",
                ChatColor.YELLOW + "Left-click to increase",
                ChatColor.YELLOW + "Right-click to decrease",
                "",
                ChatColor.GRAY + "You can modify the delay time \n" +
                        "before the placed wool automatically breaks."
        ));
        delayItem.setItemMeta(delayMeta);
        inv.setItem(14, delayItem);

        ItemStack intervalItem = new ItemStack(Material.HOPPER);
        ItemMeta intervalMeta = intervalItem.getItemMeta();
        intervalMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Wool Break Interval");
        intervalMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Current: " + (settings.getWoolBreakInterval() / 1000.0) + " seconds",
                "",
                ChatColor.YELLOW + "Left-click to increase",
                ChatColor.YELLOW + "Right-click to decrease",
                "",
                ChatColor.GRAY + "You can adjust the interval at \n" +
                        "which the wool breaks after the specified delay."
        ));
        intervalItem.setItemMeta(intervalMeta);
        inv.setItem(16, intervalItem);

        ItemStack startItem = new ItemStack(Material.EMERALD);
        ItemMeta startMeta = startItem.getItemMeta();
        startMeta.setDisplayName(ChatColor.GREEN + "Start Game");
        startMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Map: " + mapName,
                "",
                ChatColor.YELLOW + "Click to start!"
        ));
        startItem.setItemMeta(startMeta);
        inv.setItem(22, startItem);

        player.openInventory(inv);
    }
}