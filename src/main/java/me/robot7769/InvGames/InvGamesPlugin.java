package me.robot7769.InvGames;

import me.robot7769.InvGames.commands.GameCommand;
import me.robot7769.InvGames.listeners.InventoryListener;
import me.robot7769.InvGames.manager.GameManager;
 import me.robot7769.InvGames.manager.SaveManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class InvGamesPlugin extends JavaPlugin {

    private GameManager gameManager;
    private static SaveManager saveManager;

    @Override
    public void onEnable() {
        saveManager = new SaveManager(this);
        gameManager = new GameManager();
        gameManager.startTicker(this);

        getServer().getPluginManager().registerEvents(new InventoryListener(gameManager), this);

        PluginCommand gameCommand = getCommand("game");
        if (gameCommand != null) {
            gameCommand.setExecutor(new GameCommand(gameManager));
        } else {
            getLogger().warning("Command 'game' neni definovan v plugin.yml.");
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
}
