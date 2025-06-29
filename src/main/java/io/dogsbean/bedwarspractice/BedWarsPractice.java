package io.dogsbean.bedwarspractice;

import io.dogsbean.bedwarspractice.setup.command.SetupCommand;
import io.dogsbean.bedwarspractice.game.GameListener;
import io.dogsbean.bedwarspractice.game.GameManager;
import io.dogsbean.bedwarspractice.lobby.LobbyListener;
import io.dogsbean.bedwarspractice.setup.listener.SetupListener;
import io.dogsbean.bedwarspractice.setup.manager.SetupManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

@Getter
public class BedWarsPractice extends JavaPlugin {

    @Getter private static BedWarsPractice instance;
    private FileConfiguration mapConfig;
    private File mapConfigFile;

    private GameManager gameManager;
    private SetupManager setupManager;

    @Override
    public void onEnable() {
        instance = this;
        loadMapConfig();

        gameManager = new GameManager(this);
        setupManager = new SetupManager();

        getServer().getPluginManager().registerEvents(new LobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        getServer().getPluginManager().registerEvents(new SetupListener(setupManager), this);

        getCommand("setup").setExecutor(new SetupCommand(setupManager));
        getLogger().info("Bedwars Practice has been enabled!");
    }

    @Override
    public void onDisable() {
        saveMapConfig();
        getLogger().info("Bedwars Practice has been disabled!");
    }

    private void loadMapConfig() {
        mapConfigFile = new File(getDataFolder(), "maps.yml");
        if (!mapConfigFile.exists()) {
            mapConfigFile.getParentFile().mkdirs();
            try {
                mapConfigFile.createNewFile();
                YamlConfiguration defaultConfig = new YamlConfiguration();
                defaultConfig.createSection("maps");
                defaultConfig.save(mapConfigFile);
            } catch (Exception e) {
                getLogger().severe("Could not create maps.yml!");
            }
        }
        mapConfig = YamlConfiguration.loadConfiguration(mapConfigFile);
    }

    public void saveMapConfig() {
        try {
            mapConfig.save(mapConfigFile);
        } catch (Exception e) {
            getLogger().severe("Could not save maps.yml!");
        }
    }

    public FileConfiguration getMapConfig() {
        return mapConfig;
    }
}
