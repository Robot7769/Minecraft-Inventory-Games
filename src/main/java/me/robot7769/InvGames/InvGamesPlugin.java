package me.robot7769.InvGames;

import me.robot7769.InvGames.commands.GameCommand;
import me.robot7769.InvGames.listeners.InventoryListener;
import me.robot7769.InvGames.manager.GameManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class InvGamesPlugin extends JavaPlugin {

    private GameManager gameManager;

    @Override
    public void onEnable() {
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
    }
}

