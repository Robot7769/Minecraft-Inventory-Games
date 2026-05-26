package me.robot7769.InvGames;

import me.robot7769.InvGames.commands.GameCommand;
import me.robot7769.InvGames.listeners.InventoryListener;
import me.robot7769.InvGames.manager.GameManager;
import me.robot7769.InvGames.manager.SaveManager;
import me.robot7769.InvGames.manager.SQLManager;
import me.robot7769.InvGames.manager.ConfigManager;
import me.robot7769.InvGames.manager.PlaceholderManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class InvGamesPlugin extends JavaPlugin {

    private GameManager gameManager;
    private static SaveManager saveManager;
    private static ConfigManager configManager;
    private SQLManager sqlManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        saveResource("messages/messages.yml", false);
        saveResource("games/cookieclicker.yml", false);

        configManager = new ConfigManager(this);
        sqlManager = new SQLManager(this);
        saveManager = new SaveManager(this, sqlManager);

        gameManager = new GameManager();
        gameManager.startTicker(this);

        getServer().getPluginManager().registerEvents(new InventoryListener(gameManager), this);

        PluginCommand gameCommand = getCommand("game");
        if (gameCommand != null) {
            GameCommand executor = new GameCommand(gameManager);
            gameCommand.setExecutor(executor);
            gameCommand.setTabCompleter(executor);
        } else {
            getLogger().warning("Command 'game' neni definovan v plugin.yml.");
        }

        // Register PlaceholderAPI expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                PlaceholderManager pm = new PlaceholderManager(this, gameManager);
                if (pm.register()) {
                    getLogger().info("PlaceholderAPI expansion 'games' registered successfully!");
                } else {
                    getLogger().warning("Failed to register PlaceholderAPI expansion!");
                }
            } catch (Exception e) {
                getLogger().severe("Error registering PlaceholderAPI expansion: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            getLogger().info("PlaceholderAPI not found, placeholder support disabled.");
        }
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.stopAllGames();
        }
        if (saveManager != null) {
            saveManager.save();
        }
    }

    public static SaveManager getSaveManager() {
        return saveManager;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }
}


