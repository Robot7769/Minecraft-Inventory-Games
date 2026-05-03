package me.robot7769.InvGames.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final Plugin plugin;
    private final File gamesFolder;
    private final File messagesFolder;

    /**
     * Initializes the ConfigManager and ensures the configuration directories exist.
     *
     * @param plugin The main plugin instance.
     */
    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.gamesFolder = new File(plugin.getDataFolder(), "games");
        this.messagesFolder = new File(plugin.getDataFolder(), "messages");

        if (!gamesFolder.exists()) gamesFolder.mkdirs();
        if (!messagesFolder.exists()) messagesFolder.mkdirs();
    }

    /**
     * Retrieves the specific configuration file for a given game.
     *
     * @param gameName The name of the game (e.g., "cookieclicker").
     * @return The loaded FileConfiguration for the game.
     */
    public FileConfiguration getGameConfig(String gameName) {
        return loadOrCreate(gamesFolder, gameName + ".yml");
    }

    /**
     * Saves the modified configuration back to the game's YAML file.
     *
     * @param config   The modified FileConfiguration to save.
     * @param gameName The name of the game.
     */
    public void saveGameConfig(FileConfiguration config, String gameName) {
        saveConfig(config, new File(gamesFolder, gameName + ".yml"));
    }

    /**
     * Retrieves the specific messages/language configuration file.
     *
     * @param languageOrGame The language code or game name (e.g., "en" or "snake").
     * @return The loaded FileConfiguration containing the messages.
     */
    public FileConfiguration getMessages(String languageOrGame) {
        return loadOrCreate(messagesFolder, languageOrGame + ".yml");
    }

    /**
     * Saves the modified messages configuration back to its YAML file.
     *
     * @param config         The modified FileConfiguration to save.
     * @param languageOrGame The language code or game name.
     */
    public void saveMessages(FileConfiguration config, String languageOrGame) {
        saveConfig(config, new File(messagesFolder, languageOrGame + ".yml"));
    }

    /**
     * Helper method to load a YAML configuration from a file, creating it if it doesn't exist.
     *
     * @param folder   The directory where the file should be located.
     * @param fileName The name of the file to load or create.
     * @return The loaded FileConfiguration.
     */
    private FileConfiguration loadOrCreate(File folder, String fileName) {
        File file = new File(folder, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create config file: " + fileName);
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Helper method to save a YAML configuration to a file.
     *
     * @param config The FileConfiguration to save.
     * @param file   The target File to save to.
     */
    private void saveConfig(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config file: " + file.getName());
        }
    }
}
