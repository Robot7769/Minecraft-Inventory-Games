package me.robot7769.InvGames.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SaveManager {
    private final Plugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private SQLManager sqlManager;

    /**
     * Initializes the central SaveManager logic that decides routing between YAML maps and SQL endpoints.
     * Automatically attempts to create the required generic saving file data.yml as fallback.
     *
     * @param plugin     The main plugin instance.
     * @param sqlManager The constructed SQL processor handling external data loading.
     */
    public SaveManager(Plugin plugin, SQLManager sqlManager) {
        this.plugin = plugin;
        this.sqlManager = sqlManager;
        createDataFile();
    }

    /**
     * Secures a physical file on the system folder formatted specifically to handle standard SaveManager properties.
     */
    private void createDataFile() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml!");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * Automatically records specified custom keys bound per-user onto the storage infrastructure.
     *
     * @param uuid  The player's unique identifier.
     * @param game  The minigame context being played.
     * @param key   The internal name representing a saved value.
     * @param value The physical object string/int values pushed into writing logic.
     */
    public void set(UUID uuid, String game, String key, Object value) {
        if (sqlManager.isEnabled()) {
            sqlManager.set(uuid, game, key, value);
        } else {
            dataConfig.set(game + "." + uuid.toString() + "." + key, value);
        }
    }

    /**
     * General Object retriever to pull specific user-assigned entries on file properties or out of live databases.
     *
     * @param uuid The player's unique identifier.
     * @param game The minigame context targeted.
     * @param key  The mapping variable.
     * @return Standard untyped object loaded globally mapping correctly with no hard casting.
     */
    public Object get(UUID uuid, String game, String key) {
        if (sqlManager.isEnabled()) {
            return sqlManager.get(uuid, game, key);
        }
        return dataConfig.get(game + "." + uuid.toString() + "." + key);
    }

    /**
     * Safely executes an automated pull against the backend infrastructure requesting standard Doubles.
     *
     * @param uuid The connected user target.
     * @param game Targeted internal namespace handling parsing maps.
     * @param key  Variables pointing mapped value strings.
     * @param def  The default value utilized during null/failing formats.
     * @return Appropriately processed Double logic scaling.
     */
    public double getDouble(UUID uuid, String game, String key, double def) {
        if (sqlManager.isEnabled()) {
            String val = sqlManager.get(uuid, game, key);
            try { return val != null ? Double.parseDouble(val) : def; } catch (NumberFormatException e) { return def; }
        }
        return dataConfig.getDouble(game + "." + uuid.toString() + "." + key, def);
    }

    /**
     * Standard internal getter routing to safely parse SQL strings into clean integers or retrieve YAML integers cleanly.
     *
     * @param uuid User attempting mapping queries.
     * @param game Connected module space to scope files.
     * @param key  Search identifier variables targeting a score.
     * @param def  Fallback provided against null logic or processing errors.
     * @return Standard Integer.
     */
    public int getInt(UUID uuid, String game, String key, int def) {
        if (sqlManager.isEnabled()) {
            String val = sqlManager.get(uuid, game, key);
            try { return val != null ? Integer.parseInt(val) : def; } catch (NumberFormatException e) { return def; }
        }
        return dataConfig.getInt(game + "." + uuid.toString() + "." + key, def);
    }

    /**
     * Connects timestamp values matching standard system timing strings returning securely built longs or defaulting nicely.
     *
     * @param uuid User searching target database structures.
     * @param game Contextual minigame scope mapping string namespaces correctly.
     * @param key  Search ID logic returning value maps.
     * @param def  System baseline defaulted against null returns.
     * @return Extracted long metrics.
     */
    public long getLong(UUID uuid, String game, String key, long def) {
        if (sqlManager.isEnabled()) {
            String val = sqlManager.get(uuid, game, key);
            try { return val != null ? Long.parseLong(val) : def; } catch (NumberFormatException e) { return def; }
        }
        return dataConfig.getLong(game + "." + uuid.toString() + "." + key, def);
    }

    /**
     * Evaluates checking functions matching player data maps ensuring generic scores or paths register.
     *
     * @param uuid Checking entity validating strings correctly.
     * @param game Targeted minigames root file variables loaded perfectly.
     * @return Boolean true representing matching mapping spaces loaded directly into the system accurately.
     */
    public boolean hasData(UUID uuid, String game) {
        if (sqlManager.isEnabled()) {
            return sqlManager.get(uuid, game, "cookies") != null || sqlManager.get(uuid, game, "score") != null; // basic fallback
        }
        return dataConfig.contains(game + "." + uuid.toString());
    }

    /**
     * Hard-fires saving behaviors pushing file YAMLs or gracefully instructing SQL pipelines to close processing logic securely.
     */
    public void save() {
        if (!sqlManager.isEnabled()) {
            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save data.yml!");
                e.printStackTrace();
            }
        }
    }
}
