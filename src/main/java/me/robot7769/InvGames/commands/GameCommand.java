package me.robot7769.InvGames.commands;

import me.robot7769.InvGames.games.CookieClicker;
import me.robot7769.InvGames.games.SnakeGame;
import me.robot7769.InvGames.games.SlotMachineGame;
import me.robot7769.InvGames.games.TetrisGame;
import me.robot7769.InvGames.manager.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class GameCommand implements CommandExecutor {

    private final GameManager gameManager;

    public GameCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Tento prikaz muze pouzit jen hrac.");
            return true;
        }

        if (gameManager.getActiveGame(player) != null) {
            player.sendMessage("Uz hrajete jinou minihru. Nejdriv ji ukoncete zavrenim inventare.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("Pouziti: /game <nazev_hry>");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "snake":
                gameManager.startGame(player, new SnakeGame(player));
                player.sendMessage("Spoustim hru Snake.");
                break;
            case "cookie":
                gameManager.startGame(player, new CookieClicker(player));
                player.sendMessage("Spoustim hru Cookie Clicker.");
                break;
            case "slot", "slots", "slotmachine":
                gameManager.startGame(player, new SlotMachineGame(player));
                player.sendMessage("Spoustim hru Slot Machine.");
                break;
            case "tetris":
                gameManager.startGame(player, new TetrisGame(player));
                break;
            default:
                player.sendMessage("Neznama hra: " + args[0]);
                break;
        }

        return true;
    }
}
