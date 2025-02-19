package io.dogsbean.bedwarspractice.game;

import io.dogsbean.bedwarspractice.BedWarsPractice;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class GameManager implements Listener {
    private final BedWarsPractice plugin;
    private final HashMap<UUID, Game> activeGames;
    private final HashMap<UUID, GameSettings> playerSettings;
    private final HashMap<UUID, String> pendingGames;

    public GameManager(BedWarsPractice plugin) {
        this.plugin = plugin;
        this.activeGames = new HashMap<>();
        this.playerSettings = new HashMap<>();
        this.pendingGames = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(new GameSettingsListener(plugin), plugin);
    }

    public void openGameSettings(Player player, String mapName) {
        GameSettings settings = new GameSettings();
        playerSettings.put(player.getUniqueId(), settings);
        pendingGames.put(player.getUniqueId(), mapName);
        new GameSettingsGUI(settings, mapName).open(player);
    }

    public void startGame(Player player, String mapName) {
        if (isPlayerInGame(player)) {
            player.sendMessage("§cYou are already in a game!");
            return;
        }

        GameSettings settings = playerSettings.get(player.getUniqueId());
        if (settings == null) {
            settings = new GameSettings();
        }

        Game game = new Game(plugin, player, mapName, settings.clone());
        activeGames.put(player.getUniqueId(), game);
        playerSettings.remove(player.getUniqueId());
        pendingGames.remove(player.getUniqueId());
        game.start();
    }

    public void endGame(Player player, boolean won) {
        Game game = activeGames.get(player.getUniqueId());
        if (game != null) {
            game.end(won);
            activeGames.remove(player.getUniqueId());
        }
    }

    public boolean isPlayerInGame(Player player) {
        return activeGames.containsKey(player.getUniqueId());
    }

    public GameState getPlayerGameState(Player player) {
        Game game = activeGames.get(player.getUniqueId());
        return game != null ? game.getState() : null;
    }

    public String getPlayerMap(Player player) {
        Game game = activeGames.get(player.getUniqueId());
        if (game != null) {
            return game.getMapName();
        }
        return pendingGames.get(player.getUniqueId());
    }

    public Game getPlayerGame(Player player) {
        return activeGames.get(player.getUniqueId());
    }

    public GameSettings getPlayerSettings(Player player) {
        return playerSettings.get(player.getUniqueId());
    }
}