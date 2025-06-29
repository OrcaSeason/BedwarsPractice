package io.dogsbean.bedwarspractice.setup.command;

import io.dogsbean.bedwarspractice.game.map.MapManager;
import io.dogsbean.bedwarspractice.setup.listener.SetupListener;
import io.dogsbean.bedwarspractice.setup.manager.SetupManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SetupCommand implements CommandExecutor {
    private final SetupManager setupManager;

    public SetupCommand(SetupManager setupManager) {
        this.setupManager = setupManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwarspractice.setup")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setlobby":
                MapManager.setLobbySpawn(player.getLocation());
                player.sendMessage("§aLobby spawn point has been set!");
                break;

            case "createmap":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /setup createmap <name>");
                    return true;
                }
                String mapName = args[1];
                if (MapManager.mapExists(mapName)) {
                    player.sendMessage("§cMap '" + mapName + "' already exists!");
                    return true;
                }
                MapManager.createMap(mapName);
                player.sendMessage("§aMap '" + mapName + "' has been created!");
                break;

            case "wand":
                ItemStack wand = new ItemStack(Material.GOLD_AXE);
                ItemMeta wandMeta = wand.getItemMeta();
                wandMeta.setDisplayName(SetupListener.WAND_NAME);
                wand.setItemMeta(wandMeta);
                player.getInventory().addItem(wand);
                player.sendMessage("§aYou have received the Selection Wand. Left-click for pos1, Right-click for pos2.");
                break;

            case "setspawn":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /setup setspawn <map>");
                    return true;
                }
                String map = args[1];
                if (!MapManager.mapExists(map)) {
                    player.sendMessage("§cMap '" + map + "' does not exist!");
                    return true;
                }
                MapManager.setSpawnPoint(map, player.getLocation());
                player.sendMessage("§aSpawn point for map '" + map + "' has been set!");
                break;

            case "saveblocks":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /setup saveblocks <map>");
                    return true;
                }
                String mapToSave = args[1];
                if (!MapManager.mapExists(mapToSave)) {
                    player.sendMessage("§cMap '" + mapToSave + "' does not exist!");
                    return true;
                }

                if (!setupManager.hasFullSelection(player)) {
                    player.sendMessage("§cYou must make a full selection with the wand first! (/setup wand)");
                    return true;
                }

                Location pos1 = setupManager.getPos1(player);
                Location pos2 = setupManager.getPos2(player);
                MapManager.saveMapBlocks(mapToSave, pos1, pos2);
                player.sendMessage("§aBlocks for map '" + mapToSave + "' have been saved from your selection!");
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§lBedwars Practice Setup Commands");
        player.sendMessage("§7/setup setlobby §f- Set the lobby spawn point");
        player.sendMessage("§7/setup createmap <name> §f- Create a new map");
        player.sendMessage("§7/setup wand §f- Get the selection wand");
        player.sendMessage("§7/setup setspawn <map> §f- Set spawn point for a map");
        player.sendMessage("§7/setup saveblocks <map> §f- Save blocks for a map from your wand selection");
    }
}