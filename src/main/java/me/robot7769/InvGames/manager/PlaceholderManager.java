package me.robot7769.InvGames.manager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.robot7769.InvGames.InvGamesPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderManager extends PlaceholderExpansion {

    private final InvGamesPlugin plugin;
    private final GameManager gameManager;
    private long lastSaveTime = 0;

    public PlaceholderManager(InvGamesPlugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "games";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Robot7769";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    private void triggerActiveGamesSave() {
        if (System.currentTimeMillis() - lastSaveTime < 2000) return;
        lastSaveTime = System.currentTimeMillis();
        for (me.robot7769.InvGames.api.Minigame game : gameManager.getActiveGames()) {
            if (game.getClass().getSimpleName().equals("CookieClicker")) {
                try {
                    java.lang.reflect.Method saveMethod = game.getClass().getDeclaredMethod("saveData");
                    saveMethod.setAccessible(true);
                    saveMethod.invoke(game);
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        triggerActiveGamesSave();

        // Format: %games_cookieclicker_cookies% or %games_cookieclicker_cookies_formatted%
        // or %games_cookieclicker_cookies_1% or %games_cookieclicker_cookies_1_formatted%
        // or %games_cookieclicker_cookies_1_name%
        String[] parts = params.toLowerCase().split("_");

        if (parts.length < 2) return "";

        String gameName = parts[0]; // e.g., "cookieclicker"
        String dataType = parts[1]; // e.g., "cookies", "cps", "cpc"
        boolean formatted = parts.length >= 3 && parts[2].equals("formatted");
        boolean nameOnly = parts.length >= 3 && parts[2].equals("name");

        int place = -1;
        if (parts.length >= 3) {
            try {
                place = Integer.parseInt(parts[2]);
                // Check if next part is "formatted" or "name"
                if (parts.length >= 4) {
                    if (parts[3].equals("formatted")) {
                        formatted = true;
                    } else if (parts[3].equals("name")) {
                        nameOnly = true;
                    }
                }
            } catch (NumberFormatException ignored) {}
        }

        if ("cookieclicker".equals(gameName)) {
            if (place > 0) {
                return getCookieClickerTop(dataType, place, formatted, nameOnly);
            }
            return getCookieClickerData(player, dataType, formatted);
        }

        return "";
    }

    private String getCookieClickerTop(String dataType, int place, boolean formatted, boolean nameOnly) {
        // Supported types: cookies, chips
        String dbKey = dataType.equals("chips") ? "heavenlyChips" : dataType;
        java.util.List<java.util.Map.Entry<String, Double>> top = InvGamesPlugin.getSaveManager().getTop("cookieclicker", dbKey);
        if (top.size() >= place) {
            java.util.Map.Entry<String, Double> entry = top.get(place - 1);
            String uuidStr = entry.getKey();
            double val = entry.getValue();
            org.bukkit.OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(java.util.UUID.fromString(uuidStr));
            String name = op.getName() != null ? op.getName() : "Unknown";
            
            if (nameOnly) {
                // Return only the player name
                return name;
            } else if (formatted) {
                // Return only the formatted value
                return formatOfflineNumberWithCookieClickerFormat(val);
            } else {
                // Return full leaderboard format with player name
                String formatPattern = plugin.getConfig().getString("leaderboard-format", "%player% - %value%");
                String valStr = formatOfflineNumber(val);
                return formatPattern.replace("%player%", name).replace("%value%", valStr);
            }
        }
        return "N/A";
    }

    private String formatOfflineNumber(double numValue) {
        if (numValue == Math.floor(numValue)) return String.format(java.util.Locale.US, "%.0f", numValue);
        return String.format(java.util.Locale.US, "%.1f", numValue);
    }

    private String formatOfflineNumberWithCookieClickerFormat(double numValue) {
        // Try to match CookieClicker's formatting with common suffixes
        if (numValue < 1000) {
            if (numValue == Math.floor(numValue)) return String.format(java.util.Locale.US, "%.0f", numValue);
            return String.format(java.util.Locale.US, "%.1f", numValue);
        }

        String[] suffixes = {"", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc"};
        int index = 0;
        double temp = numValue;
        while (temp >= 1000 && index < suffixes.length - 1) {
            temp /= 1000;
            index++;
        }
        return String.format(java.util.Locale.US, "%.1f%s", temp, suffixes[index]);
    }

    private String getCookieClickerData(Player player, String dataType, boolean formatted) {
        Object game = gameManager.getActiveGame(player);
        if (game == null) {
            SaveManager sm = InvGamesPlugin.getSaveManager();
            if (sm.hasData(player.getUniqueId(), "cookieclicker")) {
                if (dataType.equals("cookies")) {
                    double val = sm.getDouble(player.getUniqueId(), "cookieclicker", "cookies", 0);
                    return formatted ? formatOfflineNumber(val) : String.valueOf(val);
                } else if (dataType.equals("chips")) {
                    double val = sm.getDouble(player.getUniqueId(), "cookieclicker", "heavenlyChips", 0);
                    return formatted ? formatOfflineNumber(val) : String.valueOf(val);
                }
            }
            return "N/A";
        }

        try {
            // Access via reflection since CookieClicker is not public outside the package
            Class<?> crumbClazz = Class.forName("me.robot7769.InvGames.games.CookieClicker");
            if (!crumbClazz.isInstance(game)) return "N/A";

            switch (dataType) {
                case "cookies":
                    return formatted ? formatValueViaReflection(game, "cookies", true) :
                                       String.valueOf(getFieldValue(game, "cookies"));
                case "cps":
                    return formatted ? formatValueViaReflection(game, "autoCookiesPerSecond", true) :
                                       String.valueOf(getFieldValue(game, "autoCookiesPerSecond"));
                case "cpc":
                    return formatted ? formatValueViaReflection(game, "clickMultiplier", true) :
                                       String.valueOf(getFieldValue(game, "clickMultiplier"));
                case "clicks":
                    return formatted ? formatValueViaReflection(game, "totalClicks", true) :
                                       String.valueOf(getFieldValue(game, "totalClicks"));
                case "chips":
                    return formatted ? formatValueViaReflection(game, "heavenlyChips", true) :
                                       String.valueOf(getFieldValue(game, "heavenlyChips"));
                default:
                    return "N/A";
            }
        } catch (Exception e) {
            return "ERROR";
        }
    }

    private Object getFieldValue(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    private String formatValueViaReflection(Object game, String fieldName, boolean useFormatNumber) throws Exception {
        Object value = getFieldValue(game, fieldName);
        if (!(value instanceof Number)) return String.valueOf(value);

        double numValue = ((Number) value).doubleValue();

        // Try to use the formatNumber method from CookieClicker
        try {
            java.lang.reflect.Method formatMethod = game.getClass().getDeclaredMethod("formatNumber", double.class);
            formatMethod.setAccessible(true);
            return (String) formatMethod.invoke(game, numValue);
        } catch (Exception e) {
            // Fallback to simple formatting
            return String.format("%.2f", numValue);
        }
    }
}
