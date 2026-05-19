package me.robot7769.InvGames.manager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.robot7769.InvGames.InvGamesPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderManager extends PlaceholderExpansion {

    private final InvGamesPlugin plugin;
    private final GameManager gameManager;

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
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        // Format: %games_cookieclicker_cookies% or %games_cookieclicker_cookies_formatted%
        String[] parts = params.toLowerCase().split("_");

        if (parts.length < 2) return "";

        String gameName = parts[0]; // e.g., "cookieclicker"
        String dataType = parts[1]; // e.g., "cookies", "cps", "cpc"
        boolean formatted = parts.length >= 3 && parts[2].equals("formatted");

        if ("cookieclicker".equals(gameName)) {
            return getCookieClickerData(player, dataType, formatted);
        }

        return "";
    }

    private String getCookieClickerData(Player player, String dataType, boolean formatted) {
        Object game = gameManager.getActiveGame(player);
        if (game == null) return "N/A";

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

