package me.robot7769.InvGames.commands;

import me.robot7769.InvGames.InvGamesPlugin;
import me.robot7769.InvGames.games.CookieClicker;
import me.robot7769.InvGames.games.SnakeGame;
import me.robot7769.InvGames.games.SlotMachineGame;
import me.robot7769.InvGames.games.TetrisGame;
import me.robot7769.InvGames.manager.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GameCommand implements CommandExecutor, TabCompleter {

    private final GameManager gameManager;

    public GameCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        FileConfiguration msgConfig = InvGamesPlugin.getConfigManager().getMessages("messages");

        if (!msgConfig.contains("system.no_permission")) {
            msgConfig.set("system.no_permission", "&cYou do not have permission to use this command.");
            msgConfig.set("system.player_only", "&cTento prikaz muze pouzit jen hrac.");
            msgConfig.set("system.already_playing", "&cUz hrajete jinou minihru. Nejdriv ji ukoncete zavrenim inventare.");
            msgConfig.set("system.usage",
                    "&c&b┍━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┑\n" +
                    "│INV GAME Použití                │\n" +
                    "┝━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┥\n" +
                    "│/game <hra>   &6- Zapne danou hru&b │\n" +
                    "│/game reload  &6- Načte config&b    │\n" +
                    "┕━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┙"
            );
            msgConfig.set("system.reloaded", "&aInvGames configuration successfully reloaded.");
            msgConfig.set("system.unknown_game", "&cNeznama hra: %game%");
            msgConfig.set("system.starting", "&aSpoustim hru %game%.");
            InvGamesPlugin.getConfigManager().saveMessages(msgConfig, "messages");
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("invgames.reload")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("system.no_permission", "&cNo permission.")));
                return true;
            }
            InvGamesPlugin plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(InvGamesPlugin.class);
            plugin.reloadConfig();
            // Optional: plugin.getConfigManager() reloading mechanisms here if caching occurs
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("system.reloaded", "&aReloaded.")));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("system.player_only", "&cPlayer only.")));
            return true;
        }

        if (gameManager.getActiveGame(player) != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("system.already_playing", "&cAlready playing.")));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("system.usage", "&cUsage: /game <game>")));
            return true;
        }

        String startMsg = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("system.starting", "&aStarting %game%."));

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "snake":
                gameManager.startGame(player, new SnakeGame(player));
                player.sendMessage(startMsg.replace("%game%", "Snake"));
                break;
            case "cookie":
                gameManager.startGame(player, new CookieClicker(player));
                player.sendMessage(startMsg.replace("%game%", "Cookie Clicker"));
                break;
            case "slot", "slots", "slotmachine":
                gameManager.startGame(player, new SlotMachineGame(player));
                player.sendMessage(startMsg.replace("%game%", "Slot Machine"));
                break;
            case "tetris":
                gameManager.startGame(player, new TetrisGame(player));
                break;
            default:
                String unknownMsg = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("system.unknown_game", "&cUnknown game: %game%"));
                player.sendMessage(unknownMsg.replace("%game%", args[0]));
                break;
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(List.of("snake", "cookie", "slot", "tetris", "reload"));
            List<String> result = new ArrayList<>();
            for (String c : completions) {
                if (c.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(c);
                }
            }
            return result;
        }
        return new ArrayList<>();
    }
}
