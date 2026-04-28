package me.robot7769.InvGames.games;

import me.robot7769.InvGames.api.Minigame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.robot7769.InvGames.InvGamesPlugin;
import me.robot7769.InvGames.manager.SaveManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class CookieClicker extends Minigame {

    private int invSize;
    private int cookieSlot;
    private int upgradesButtonSlot;
    private int oneTimeButtonSlot;
    private int achievementsButtonSlot;
    private int backButtonSlot;

    private double cookies = 0;
    private double clickMultiplier = 1;
    private double autoCookiesPerSecond = 0;

    private enum MenuState { MAIN, UPGRADES, ONE_TIME, ACHIEVEMENTS }
    private MenuState menuState = MenuState.MAIN;

    private List<Upgrade> upgrades;
    private List<Upgrade> oneTimeUpgrades;
    private List<Achievement> achievementsList;

    public CookieClicker(Player player) {
        super(player);
        this.tickInterval = 20; // 1 second intervals for auto production
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = InvGamesPlugin.getConfigManager().getGameConfig("cookieclicker");
        if (!config.contains("inventory_size")) {
            config.set("inventory_size", 45);
            config.set("slots.cookie", 22);
            config.set("slots.upgrades_button", 26);
            config.set("slots.one_time_button", 27);
            config.set("slots.achievements_button", 28);
            config.set("slots.back_button", 36);

            config.set("upgrades.cursor.name", "Cursor");
            config.set("upgrades.cursor.material", "WOODEN_PICKAXE");
            config.set("upgrades.cursor.baseCost", 15.0);
            config.set("upgrades.cursor.costMultiplier", 1.15);
            config.set("upgrades.cursor.cpsPerLevel", "0.0");
            config.set("upgrades.cursor.clickPerLevel", "+1.0");
            config.set("upgrades.cursor.require.cookies", 0.0);
            config.set("upgrades.cursor.require.cps", 0.0);
            config.set("upgrades.cursor.require.click", 0.0);
            config.set("upgrades.cursor.description", "Increases cookies per click");

            config.set("upgrades.grandma.name", "Grandma");
            config.set("upgrades.grandma.material", "WHEAT");
            config.set("upgrades.grandma.baseCost", 100.0);
            config.set("upgrades.grandma.costMultiplier", 1.15);
            config.set("upgrades.grandma.cpsPerLevel", "+1.0");
            config.set("upgrades.grandma.clickPerLevel", "0.0");
            config.set("upgrades.grandma.require.cookies", 10.0);
            config.set("upgrades.grandma.require.cps", 0.0);
            config.set("upgrades.grandma.require.click", 0.0);
            config.set("upgrades.grandma.description", "Bakes cookies passively");

            config.set("upgrades.multiplier.name", "Cookie Magic");
            config.set("upgrades.multiplier.material", "NETHER_STAR");
            config.set("upgrades.multiplier.baseCost", 500.0);
            config.set("upgrades.multiplier.costMultiplier", 2.5);
            config.set("upgrades.multiplier.cpsPerLevel", "*1.1");
            config.set("upgrades.multiplier.clickPerLevel", "*1.1");
            config.set("upgrades.multiplier.require.cookies", 250.0);
            config.set("upgrades.multiplier.require.cps", 0.0);
            config.set("upgrades.multiplier.require.click", 0.0);
            config.set("upgrades.multiplier.description", "Multiplies total gain by 10%");

            InvGamesPlugin.getConfigManager().saveGameConfig(config, "cookieclicker");
        }

        this.invSize = config.getInt("inventory_size", 45);
        this.cookieSlot = config.getInt("slots.cookie", 22);
        this.upgradesButtonSlot = config.getInt("slots.upgrades_button", 26);
        this.oneTimeButtonSlot = config.getInt("slots.one_time_button", 27);
        this.achievementsButtonSlot = config.getInt("slots.achievements_button", 28);
        this.backButtonSlot = config.getInt("slots.back_button", 36);

        upgrades = loadUpgrades(config, "upgrades");
        oneTimeUpgrades = loadUpgrades(config, "one-time-upgrades");

        achievementsList = new ArrayList<>();
        if (config.getConfigurationSection("achievements") != null) {
            for (String key : config.getConfigurationSection("achievements").getKeys(false)) {
                String path = "achievements." + key + ".";
                String name = config.getString(path + "name", "Unknown Achievement");
                Material mat = Material.matchMaterial(config.getString(path + "material", "DIAMOND"));
                if (mat == null) mat = Material.DIAMOND;
                double reqCookies = config.getDouble(path + "require.cookies", 0.0);
                double reqCps = config.getDouble(path + "require.cps", 0.0);
                double reqClick = config.getDouble(path + "require.click", 0.0);
                String desc = config.getString(path + "description", "");
                String sound = config.getString(path + "sound", "ENTITY_PLAYER_LEVELUP");
                achievementsList.add(new Achievement(name, mat, reqCookies, reqCps, reqClick, desc, sound));
            }
        }

        FileConfiguration msgConfig = InvGamesPlugin.getConfigManager().getMessages("messages");
        if (!msgConfig.contains("cookieclicker.earned_away")) {
            msgConfig.set("cookieclicker.earned_away", "&a[Cookie Clicker] You earned &e%earned% &acookies while you were away!");
            msgConfig.set("cookieclicker.gui_title", "Cookie Clicker");
            msgConfig.set("cookieclicker.cookie_name", "&6&lThe Ultimate Cookie");
            msgConfig.set("cookieclicker.upgrades_shop", "&e&lUpgrades Shop");
            msgConfig.set("cookieclicker.one_time_shop", "&b&lOne-Time Upgrades");
            msgConfig.set("cookieclicker.achievements", "&d&lAchievements");
            msgConfig.set("cookieclicker.back", "&c&lBack");
            InvGamesPlugin.getConfigManager().saveMessages(msgConfig, "messages");
        }
    }

    private List<Upgrade> loadUpgrades(FileConfiguration config, String sectionName) {
        List<Upgrade> list = new ArrayList<>();
        if (config.getConfigurationSection(sectionName) != null) {
            for (String key : config.getConfigurationSection(sectionName).getKeys(false)) {
                String path = sectionName + "." + key + ".";
                String name = config.getString(path + "name", "Unknown Upgrade");
                Material mat = Material.matchMaterial(config.getString(path + "material", "DIRT"));
                if (mat == null) mat = Material.DIRT;
                double baseCost = config.getDouble(path + "baseCost", 10.0);
                double costMult = config.getDouble(path + "costMultiplier", 1.15);

                String cpsStr = config.getString(path + "cpsPerLevel", "0.0");
                double cpsAdd = 0, cpsMult = 1;
                if (cpsStr.startsWith("*")) {
                    cpsMult = Double.parseDouble(cpsStr.substring(1));
                } else if (cpsStr.startsWith("+")) {
                    cpsAdd = Double.parseDouble(cpsStr.substring(1));
                } else {
                    cpsAdd = Double.parseDouble(cpsStr);
                }

                String clickStr = config.getString(path + "clickPerLevel", "0.0");
                double clickAdd = 0, clickMult = 1;
                if (clickStr.startsWith("*")) {
                    clickMult = Double.parseDouble(clickStr.substring(1));
                } else if (clickStr.startsWith("+")) {
                    clickAdd = Double.parseDouble(clickStr.substring(1));
                } else {
                    clickAdd = Double.parseDouble(clickStr);
                }

                double reqCookies = config.getDouble(path + "require.cookies", 0.0);
                double reqCps = config.getDouble(path + "require.cps", 0.0);
                double reqClick = config.getDouble(path + "require.click", 0.0);

                boolean oneTime = config.getBoolean(path + "one_time", false);

                String desc = config.getString(path + "description", "");

                list.add(new Upgrade(name, mat, baseCost, costMult, cpsAdd, cpsMult, clickAdd, clickMult, reqCookies, reqCps, reqClick, oneTime, desc));
            }
        }
        return list;
    }

    private void calculateStats() {
        double tempAuto = 0;
        double tempClick = 1;
        double multAuto = 1;
        double multClick = 1;

        for (Upgrade u : upgrades) {
            tempAuto += u.level * u.cpsAdd;
            tempClick += u.level * u.clickAdd;
            if (u.level > 0) {
                if (u.cpsMult != 1) multAuto *= Math.pow(u.cpsMult, u.level);
                if (u.clickMult != 1) multClick *= Math.pow(u.clickMult, u.level);
            }
        }
        for (Upgrade u : oneTimeUpgrades) {
            tempAuto += u.level * u.cpsAdd;
            tempClick += u.level * u.clickAdd;
            if (u.level > 0) {
                if (u.cpsMult != 1) multAuto *= Math.pow(u.cpsMult, u.level);
                if (u.clickMult != 1) multClick *= Math.pow(u.clickMult, u.level);
            }
        }

        autoCookiesPerSecond = tempAuto * multAuto;
        clickMultiplier = tempClick * multClick;
    }

    private void checkAchievements() {
        boolean renderNeeded = false;
        for (Achievement a : achievementsList) {
            if (!a.unlocked && cookies >= a.reqCookies && autoCookiesPerSecond >= a.reqCps && clickMultiplier >= a.reqClick) {
                a.unlocked = true;
                renderNeeded = true;
                player.sendMessage("§d§lAchievement Unlocked: §e" + ChatColor.translateAlternateColorCodes('&', a.name));
                try {
                    player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(a.sound), 1.0f, 1.0f);
                } catch (Exception ignored) {}
            }
        }
        if (renderNeeded && menuState == MenuState.ACHIEVEMENTS) {
            render();
        }
    }

    public void loadData() {
        SaveManager saveManager = InvGamesPlugin.getSaveManager();
        UUID uuid = player.getUniqueId();
        String game = "cookieclicker";

        if (saveManager.hasData(uuid, game)) {
            this.cookies = saveManager.getDouble(uuid, game, "cookies", 0);
            for (int i = 0; i < upgrades.size(); i++) {
                upgrades.get(i).level = saveManager.getInt(uuid, game, "upgrades." + i, 0);
            }
            for (int i = 0; i < oneTimeUpgrades.size(); i++) {
                oneTimeUpgrades.get(i).level = saveManager.getInt(uuid, game, "onetime." + i, 0);
            }
            for (int i = 0; i < achievementsList.size(); i++) {
                achievementsList.get(i).unlocked = saveManager.getInt(uuid, game, "achiev." + i, 0) == 1;
            }
            long lastSaveTime = saveManager.getLong(uuid, game, "lastSaveTime", 0);
            calculateStats();

            long currentTime = System.currentTimeMillis();
            long diffMillis = currentTime - lastSaveTime;
            if (diffMillis > 0 && autoCookiesPerSecond > 0 && lastSaveTime > 0) {
                double secondsAway = diffMillis / 1000.0;
                double earned = secondsAway * autoCookiesPerSecond;
                this.cookies += earned;

                FileConfiguration msgConfig = InvGamesPlugin.getConfigManager().getMessages("messages");
                String msg = msgConfig.getString("cookieclicker.earned_away", "&a[Cookie Clicker] You earned &e%earned% &acookies while you were away!")
                    .replace("%earned%", String.valueOf((int) earned));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
        } else {
            calculateStats();
        }
    }

    public void saveData() {
        SaveManager saveManager = InvGamesPlugin.getSaveManager();
        UUID uuid = player.getUniqueId();
        String game = "cookieclicker";

        saveManager.set(uuid, game, "cookies", this.cookies);
        for (int i = 0; i < upgrades.size(); i++) {
            saveManager.set(uuid, game, "upgrades." + i, upgrades.get(i).level);
        }
        for (int i = 0; i < oneTimeUpgrades.size(); i++) {
            saveManager.set(uuid, game, "onetime." + i, oneTimeUpgrades.get(i).level);
        }
        for (int i = 0; i < achievementsList.size(); i++) {
            saveManager.set(uuid, game, "achiev." + i, achievementsList.get(i).unlocked ? 1 : 0);
        }
        saveManager.set(uuid, game, "lastSaveTime", System.currentTimeMillis());
        saveManager.save();
    }

    @Override
    public void start() {
        loadData();
        FileConfiguration msgConfig = InvGamesPlugin.getConfigManager().getMessages("messages");
        String title = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.gui_title", "Cookie Clicker"));
        inventory = Bukkit.createInventory(player, invSize, title);
        menuState = MenuState.MAIN;
        render();
        player.openInventory(inventory);
    }

    @Override
    public void stop() {
        saveData();
        if (player.isOnline() && player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.closeInventory();
        }
    }

    @Override
    public void onClick(int slot) {
        if (menuState == MenuState.MAIN) {
            if (slot == cookieSlot) {
                cookies += clickMultiplier;
                checkAchievements();
                render();
                return;
            } else if (slot == upgradesButtonSlot) {
                menuState = MenuState.UPGRADES;
                render();
                return;
            } else if (slot == oneTimeButtonSlot) {
                menuState = MenuState.ONE_TIME;
                render();
                return;
            } else if (slot == achievementsButtonSlot) {
                menuState = MenuState.ACHIEVEMENTS;
                render();
                return;
            }
        } else {
            if (slot == backButtonSlot) {
                menuState = MenuState.MAIN;
                render();
                return;
            }

            if (menuState == MenuState.UPGRADES || menuState == MenuState.ONE_TIME) {
                List<Upgrade> activeList = menuState == MenuState.UPGRADES ? upgrades : oneTimeUpgrades;
                int startSlot = 10;
                int col = 0;
                int row = 0;
                for (int i = 0; i < activeList.size(); i++) {
                    Upgrade u = activeList.get(i);
                    if (!u.isUnlocked(cookies, autoCookiesPerSecond, clickMultiplier)) continue;
                    if (u.oneTime && u.level >= 1) continue;

                    int upgradeSlot = startSlot + row * 9 + col;
                    if (slot == upgradeSlot) {
                        if (cookies >= u.getCurrentCost()) {
                            cookies -= u.getCurrentCost();
                            u.purchase();
                            calculateStats();
                            checkAchievements();
                            render();
                        }
                        break;
                    }
                    col++;
                    if (col >= 7) {
                        col = 0;
                        row++;
                    }
                }
            }
        }
    }

    @Override
    public void onTick() {
        if (autoCookiesPerSecond > 0) {
            cookies += autoCookiesPerSecond;
            checkAchievements();
            render();
        }
    }

    private void render() {
        if (inventory == null) return;
        inventory.clear();

        ItemStack bg = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < invSize; i++) {
            inventory.setItem(i, bg);
        }

        FileConfiguration msgConfig = InvGamesPlugin.getConfigManager().getMessages("messages");

        if (menuState == MenuState.MAIN) {
            // Render main cookie
            String cookieName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.cookie_name", "&6&lThe Ultimate Cookie"));
            ItemStack cookie = createItem(Material.COOKIE, cookieName);
            ItemMeta meta = cookie.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("§fCookies: §e" + (int) cookies);
                lore.add("§fCookies per click: §e" + clickMultiplier);
                lore.add("§fCookies per second: §e" + autoCookiesPerSecond);
                meta.setLore(lore);
                cookie.setItemMeta(meta);
            }
            inventory.setItem(cookieSlot, cookie);

            // Render buttons
            String shopName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.upgrades_shop", "&e&lUpgrades Shop"));
            inventory.setItem(upgradesButtonSlot, createItem(Material.ANVIL, shopName, List.of("§7Click to open upgrades")));

            if (oneTimeButtonSlot > 0) {
                String oneTimeName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.one_time_shop", "&b&lOne-Time Upgrades"));
                inventory.setItem(oneTimeButtonSlot, createItem(Material.DIAMOND_PICKAXE, oneTimeName, List.of("§7Special rare upgrades")));
            }
            if (achievementsButtonSlot > 0) {
                String achName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.achievements", "&d&lAchievements"));
                inventory.setItem(achievementsButtonSlot, createItem(Material.EMERALD, achName, List.of("§7View your progress")));
            }

        } else if (menuState == MenuState.ACHIEVEMENTS) {
            String backName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.back", "&c&lBack"));
            inventory.setItem(backButtonSlot, createItem(Material.ARROW, backName, List.of("§7Return to game")));
            inventory.setItem(4, createItem(Material.COOKIE, "§fCookies: §e" + (int) cookies));

            int startSlot = 10;
            int col = 0;
            int row = 0;
            for (Achievement a : achievementsList) {
                Material mat = a.unlocked ? a.material : Material.BARRIER;
                String display = a.unlocked ? "§a" + a.name : "§c" + a.name + " §7(Locked)";
                List<String> lore = new ArrayList<>();
                lore.add("§7" + a.description);
                lore.add("");
                if (!a.unlocked) {
                    if (a.reqCookies > 0) lore.add("§eRequires Cookies: " + a.reqCookies);
                    if (a.reqCps > 0) lore.add("§eRequires CPS: " + a.reqCps);
                    if (a.reqClick > 0) lore.add("§eRequires Click: " + a.reqClick);
                } else {
                    lore.add("§a§lUNLOCKED");
                }

                inventory.setItem(startSlot + row * 9 + col, createItem(mat, display, lore));
                col++;
                if (col >= 7) {
                    col = 0;
                    row++;
                }
            }
        } else {
            // Render back button
            String backName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.back", "&c&lBack"));
            inventory.setItem(backButtonSlot, createItem(Material.ARROW, backName, List.of("§7Return to game")));

            // Render cookie info display
            inventory.setItem(4, createItem(Material.COOKIE, "§fCookies: §e" + (int) cookies));

            // Render upgrades
            List<Upgrade> activeList = menuState == MenuState.UPGRADES ? upgrades : oneTimeUpgrades;
            int startSlot = 10;
            int col = 0;
            int row = 0;
            for (Upgrade u : activeList) {
                if (!u.isUnlocked(cookies, autoCookiesPerSecond, clickMultiplier)) continue;
                if (u.oneTime && u.level >= 1) continue;

                int currentCost = u.getCurrentCost();
                boolean canAfford = cookies >= currentCost;

                ItemStack item = createItem(u.material, "§a" + u.name + " §7(Level " + u.level + ")");
                ItemMeta uMeta = item.getItemMeta();
                if (uMeta != null) {
                    List<String> lore = new ArrayList<>();
                    lore.add("§7" + u.description);
                    lore.add("");
                    lore.add(canAfford ? "§eCost: " + currentCost : "§cCost: " + currentCost + " §4(Too expensive)");
                    uMeta.setLore(lore);
                    item.setItemMeta(uMeta);
                }
                inventory.setItem(startSlot + row * 9 + col, item);

                col++;
                if (col >= 7) {
                    col = 0;
                    row++;
                }
            }
        }
    }

    private ItemStack createItem(Material material, String name) {
        return createItem(material, name, new ArrayList<>());
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private static class Upgrade {
        String name;
        Material material;
        double baseCost;
        double costMultiplier;
        int level = 0;
        double cpsAdd;
        double cpsMult;
        double clickAdd;
        double clickMult;
        double reqCookies;
        double reqCps;
        double reqClick;
        boolean oneTime;
        String description;

        public Upgrade(String name, Material material, double baseCost, double costMultiplier,
                       double cpsAdd, double cpsMult, double clickAdd, double clickMult,
                       double reqCookies, double reqCps, double reqClick, boolean oneTime, String description) {
            this.name = name;
            this.material = material;
            this.baseCost = baseCost;
            this.costMultiplier = costMultiplier;
            this.cpsAdd = cpsAdd;
            this.cpsMult = cpsMult;
            this.clickAdd = clickAdd;
            this.clickMult = clickMult;
            this.reqCookies = reqCookies;
            this.reqCps = reqCps;
            this.reqClick = reqClick;
            this.oneTime = oneTime;
            this.description = description;
        }

        public int getCurrentCost() {
            return (int) Math.floor(baseCost * Math.pow(costMultiplier, level));
        }

        public void purchase() {
            level++;
        }

        public boolean isUnlocked(double currentCookies, double currentCps, double currentClick) {
            return level > 0 || (currentCookies >= reqCookies && currentCps >= reqCps && currentClick >= reqClick);
        }
    }

    private static class Achievement {
        String name;
        Material material;
        double reqCookies;
        double reqCps;
        double reqClick;
        String description;
        String sound;
        boolean unlocked = false;

        public Achievement(String name, Material material, double reqCookies, double reqCps, double reqClick, String description, String sound) {
            this.name = name;
            this.material = material;
            this.reqCookies = reqCookies;
            this.reqCps = reqCps;
            this.reqClick = reqClick;
            this.description = description;
            this.sound = sound;
        }
    }
}
