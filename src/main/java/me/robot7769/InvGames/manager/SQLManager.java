package me.robot7769.InvGames.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLManager {

    private final Plugin plugin;
    private Connection connection;
    private final boolean enabled;

    private String host, database, username, password;
    private int port;


    /**
     * Initializes the SQLManager by parsing the plugin's config to fetch database credentials.
     * Starts the connection process immediately if SQL is enabled.
     *
     * @param plugin The main plugin instance.
     */
    public SQLManager(Plugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.enabled = config.getBoolean("sql.enabled", false);

        if (this.enabled) {
            this.host = config.getString("sql.host", "localhost");
            this.port = config.getInt("sql.port", 3306);
            this.database = config.getString("sql.database", "invgame_db");
            this.username = config.getString("sql.username", "root");
            this.password = config.getString("sql.password", "password");
            connect();
        }
    }

    /**
     * Checks if SQL usage is enabled globally in the config.yml.
     *
     * @return true if enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Re-establishes a network connection to the database. Automatically connects using the URL prefix mapped in the configuration.
     * Parses the credentials safely and triggers table creation if missing.
     */
    public void connect() {
        if (!enabled) return;
        try {
            if (connection != null && !connection.isClosed()) return;

            // Allow multiple types by standardizing the JDBC URL
            String type = plugin.getConfig().getString("sql.type", "mariadb").toLowerCase();
            String jdbcPrefix = type.equals("postgresql") ? "jdbc:postgresql://" : "jdbc:mysql://";
            String url = jdbcPrefix + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";

            connection = DriverManager.getConnection(url, username, password);
            createTable();
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL Connection failed: " + e.getMessage());
        }
    }

    /**
     * Safely closes the live SQL connection to avoid memory and port leaks.
     */
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Automatically attempts to create the generic data storage table across UUID, Game Name, and Keys.
     *
     * @throws SQLException If an executing error occurs against the database.
     */
    private void createTable() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS invgames_data (" +
                    "uuid VARCHAR(36)," +
                    "game VARCHAR(50)," +
                    "data_key VARCHAR(50)," +
                    "data_value TEXT," +
                    "PRIMARY KEY (uuid, game, data_key)" +
                    ")");
        }
    }

    /**
     * Asynchronously uploads standard player-based values mapped by key explicitly matched to their UUID and game context.
     * Handles Insert & Update automatically.
     *
     * @param uuid  The player's unique identifier.
     * @param game  The minigame context mapping (e.g. "cookieclicker").
     * @param key   The internal storage variable representing a property (e.g. "cookies").
     * @param value The value to save which will automatically be stringified.
     */
    public void set(UUID uuid, String game, String key, Object value) {
        if (!enabled) return;
        CompletableFuture.runAsync(() -> {
            try {
                connect();
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO invgames_data (uuid, game, data_key, data_value) VALUES (?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE data_value = ?"
                );
                ps.setString(1, uuid.toString());
                ps.setString(2, game);
                ps.setString(3, key);
                String valStr = value != null ? value.toString() : "";
                ps.setString(4, valStr);
                ps.setString(5, valStr);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to save SQL data: " + e.getMessage());
            }
        });
    }

    /**
     * Synchronously searches the database specifically referencing the player's ID, game name, and assigned properties.
     *
     * @param uuid The player's unique identifier.
     * @param game The minigame context being tested.
     * @param key  The mapping variable trying to be loaded.
     * @return The raw String payload directly pulled from the database or null if incomplete.
     */
    public String get(UUID uuid, String game, String key) {
        if (!enabled) return null;
        try {
            connect();
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT data_value FROM invgames_data WHERE uuid = ? AND game = ? AND data_key = ?"
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, game);
            ps.setString(3, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("data_value");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load SQL data: " + e.getMessage());
        }
        return null;
    }
}
