package me.robot7769.InvGames.manager;

import me.robot7769.InvGames.api.Minigame;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private final Map<UUID, Minigame> activeGames = new HashMap<>();
    private BukkitTask tickerTask;

    public void startGame(Player player, Minigame game) {
        stopGame(player);
        activeGames.put(player.getUniqueId(), game);
        game.start();
    }

    public void stopGame(Player player) {
        Minigame game = activeGames.remove(player.getUniqueId());
        if (game != null) {
            game.stop();
        }
    }

    public Minigame getActiveGame(Player player) {
        return activeGames.get(player.getUniqueId());
    }

    public void stopAllGames() {
        for (Minigame game : new ArrayList<>(activeGames.values())) {
            stopGame(game.getPlayer());
        }
    }

    public void startTicker(Plugin plugin) {
        if (tickerTask != null) {
            tickerTask.cancel();
        }

        tickerTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Copy values to avoid concurrent modification if a game stops during tick callback.
                for (Minigame game : new ArrayList<>(activeGames.values())) {
                    game.tickCounter++;
                    if (game.tickCounter >= game.tickInterval) {
                        game.tickCounter = 0;
                        game.onTick();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}

