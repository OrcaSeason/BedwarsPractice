package io.dogsbean.bedwarspractice.command;

import io.dogsbean.bedwarspractice.BedWarsPractice;
import io.dogsbean.bedwarspractice.game.map.MapManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetupCommand implements CommandExecutor {
    private final BedWarsPractice plugin;

    public SetupCommand(BedWarsPractice plugin) {
        this.plugin = plugin;
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
                MapManager.saveMapBlocks(mapToSave, player.getLocation());
                player.sendMessage("§aBlocks for map '" + mapToSave + "' have been saved!");
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
        player.sendMessage("§7/setup setspawn <map> §f- Set spawn point for a map");
        player.sendMessage("§7/setup saveblocks <map> §f- Save blocks for a map");
    }
}