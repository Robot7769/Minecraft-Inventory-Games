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

    public SaveManager(Plugin plugin) {
        this.plugin = plugin;
        createDataFile();
    }

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

    public void set(UUID uuid, String game, String key, Object value) {
        dataConfig.set(game + "." + uuid.toString() + "." + key, value);
    }

    public Object get(UUID uuid, String game, String key) {
        return dataConfig.get(game + "." + uuid.toString() + "." + key);
    }

    public double getDouble(UUID uuid, String game, String key, double def) {
        return dataConfig.getDouble(game + "." + uuid.toString() + "." + key, def);
    }

    public int getInt(UUID uuid, String game, String key, int def) {
        return dataConfig.getInt(game + "." + uuid.toString() + "." + key, def);
    }

    public long getLong(UUID uuid, String game, String key, long def) {
        return dataConfig.getLong(game + "." + uuid.toString() + "." + key, def);
    }

    public boolean hasData(UUID uuid, String game) {
        return dataConfig.contains(game + "." + uuid.toString());
    }

    public void save() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml!");
            e.printStackTrace();
        }
    }
}

