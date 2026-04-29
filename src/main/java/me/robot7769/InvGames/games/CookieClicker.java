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
    private int rebirthButtonSlot;
    private int backButtonSlot;
    private int prevPageButtonSlot;
    private int nextPageButtonSlot;

    private double cookies = 0;
    private double heavenlyChips = 0;
    private double clickMultiplier = 1;
    private double autoCookiesPerSecond = 0;

    private enum MenuState { MAIN, UPGRADES, ONE_TIME, ACHIEVEMENTS, REBIRTH }
    private MenuState menuState = MenuState.MAIN;
    private int currentAchPage = 0;

    private List<Upgrade> upgrades;
    private List<Upgrade> oneTimeUpgrades;
    private List<RebirthUpgrade> rebirthUpgrades;
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
            config.set("slots.one_time_button", 17);
            config.set("slots.achievements_button", 18);
            config.set("slots.rebirth_button", 40);
            config.set("slots.back_button", 36);
            config.set("slots.prev_page_button", 38);
            config.set("slots.next_page_button", 42);

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
        this.oneTimeButtonSlot = config.getInt("slots.one_time_button", 17);
        this.achievementsButtonSlot = config.getInt("slots.achievements_button", 18);
        this.rebirthButtonSlot = config.getInt("slots.rebirth_button", 40);
        this.backButtonSlot = config.getInt("slots.back_button", 36);
        this.prevPageButtonSlot = config.getInt("slots.prev_page_button", 38);
        this.nextPageButtonSlot = config.getInt("slots.next_page_button", 42);

        upgrades = loadUpgrades(config, "upgrades");
        oneTimeUpgrades = loadUpgrades(config, "one-time-upgrades");
        rebirthUpgrades = loadRebirthUpgrades(config, "rebirth-upgrades");

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
            msgConfig.set("cookieclicker.rebirth", "&5&lRebirth Menu");
            msgConfig.set("cookieclicker.do_rebirth", "&d&lAscend!");
            msgConfig.set("cookieclicker.back", "&c&lBack");

            msgConfig.set("cookieclicker.stats_cookies", "&fCookies: &e%cookies%");
            msgConfig.set("cookieclicker.stats_click", "&fCookies per click: &e%click%");
            msgConfig.set("cookieclicker.stats_cps", "&fCookies per second: &e%cps%");
            msgConfig.set("cookieclicker.stats_chips", "&bHeavenly Chips: &f%chips%");
            msgConfig.set("cookieclicker.lore_upgrades", "&7Click to open upgrades");
            msgConfig.set("cookieclicker.lore_one_time", "&7Special rare upgrades");
            msgConfig.set("cookieclicker.lore_achievements", "&7View your progress");
            msgConfig.set("cookieclicker.lore_rebirth_1", "&7Ascend and unlock heavenly magic");
            msgConfig.set("cookieclicker.lore_rebirth_2", "&bChips: %chips%");
            msgConfig.set("cookieclicker.lore_return", "&7Return to game");
            msgConfig.set("cookieclicker.lore_locked", "&c%name% &7(Locked)");
            msgConfig.set("cookieclicker.req_cookies", "&eRequires Cookies: %req%");
            msgConfig.set("cookieclicker.req_cps", "&eRequires CPS: %req%");
            msgConfig.set("cookieclicker.req_click", "&eRequires Click: %req%");
            msgConfig.set("cookieclicker.achievement_unlocked", "&a&lUNLOCKED");
            msgConfig.set("cookieclicker.prev_page", "&e&lPrevious Page");
            msgConfig.set("cookieclicker.next_page", "&e&lNext Page");
            msgConfig.set("cookieclicker.lore_ascend_1", "&7Forfeit your cookies to ascend.");
            msgConfig.set("cookieclicker.lore_ascend_2", "&bPending Chips: &f%chips%");
            msgConfig.set("cookieclicker.lore_ascend_3", "&eCurrent Cookies: %cookies%");
            msgConfig.set("cookieclicker.rebirth_purchased", "&a&lPURCHASED");
            msgConfig.set("cookieclicker.rebirth_cost_afford", "&eCost: %cost% Chips");
            msgConfig.set("cookieclicker.rebirth_cost_deny", "&cCost: %cost% Chips");
            msgConfig.set("cookieclicker.upgrade_level", "&a%name% &7(Level %level%)");
            msgConfig.set("cookieclicker.upgrade_cost_afford", "&eCost: %cost%");
            msgConfig.set("cookieclicker.upgrade_cost_deny", "&cCost: %cost% &4(Too expensive)");
            msgConfig.set("cookieclicker.msg_ascended", "&dYou Ascended and received &b%chips% Heavenly Chips&d!");
            msgConfig.set("cookieclicker.msg_achievement", "&d&lAchievement Unlocked: &e%name%");

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
                String reqRebirth = config.getString(path + "require.rebirth", "");

                boolean oneTime = config.getBoolean(path + "one_time", false);

                String desc = config.getString(path + "description", "");

                list.add(new Upgrade(key, name, mat, baseCost, costMult, cpsAdd, cpsMult, clickAdd, clickMult, reqCookies, reqCps, reqClick, reqRebirth, oneTime, desc));
            }
        }
        return list;
    }

    private List<RebirthUpgrade> loadRebirthUpgrades(FileConfiguration config, String sectionName) {
        List<RebirthUpgrade> list = new ArrayList<>();
        if (config.getConfigurationSection(sectionName) != null) {
            for (String key : config.getConfigurationSection(sectionName).getKeys(false)) {
                String path = sectionName + "." + key + ".";
                String name = config.getString(path + "name", "Unknown Upgrade");
                Material mat = Material.matchMaterial(config.getString(path + "material", "NETHER_STAR"));
                if (mat == null) mat = Material.NETHER_STAR;
                double cost = config.getDouble(path + "cost", 1.0);
                
                String cpsStr = config.getString(path + "cpsPerLevel", "*1.0");
                double cpsMult = 1;
                if (cpsStr.startsWith("*")) cpsMult = Double.parseDouble(cpsStr.substring(1));
                
                String clickStr = config.getString(path + "clickPerLevel", "*1.0");
                double clickMult = 1;
                if (clickStr.startsWith("*")) clickMult = Double.parseDouble(clickStr.substring(1));
                
                int slot = config.getInt(path + "slot", -1);
                String desc = config.getString(path + "description", "");
                
                list.add(new RebirthUpgrade(key, name, mat, cost, cpsMult, clickMult, slot, desc));
            }
        } else {
            // Default config for rebirth upgrades
            config.set("rebirth-upgrades.heavenly_magic.name", "Heavenly Magic");
            config.set("rebirth-upgrades.heavenly_magic.material", "NETHER_STAR");
            config.set("rebirth-upgrades.heavenly_magic.cost", 1.0);
            config.set("rebirth-upgrades.heavenly_magic.cpsPerLevel", "*1.5");
            config.set("rebirth-upgrades.heavenly_magic.clickPerLevel", "*1.5");
            config.set("rebirth-upgrades.heavenly_magic.slot", 22);
            config.set("rebirth-upgrades.heavenly_magic.description", "Boosts CPS and Click by 50% permanently");
            InvGamesPlugin.getConfigManager().saveGameConfig(config, "cookieclicker");
            return loadRebirthUpgrades(config, sectionName);
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
        for (RebirthUpgrade ru : rebirthUpgrades) {
            if (ru.unlocked) {
                multAuto *= ru.cpsMult;
                multClick *= ru.clickMult;
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
                FileConfiguration msgConfig = InvGamesPlugin.getConfigManager().getMessages("messages");
                String msgAch = msgConfig.getString("cookieclicker.msg_achievement", "&d&lAchievement Unlocked: &e%name%").replace("%name%", a.name);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msgAch));
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
            this.heavenlyChips = saveManager.getDouble(uuid, game, "heavenlyChips", 0);
            for (int i = 0; i < upgrades.size(); i++) {
                upgrades.get(i).level = saveManager.getInt(uuid, game, "upgrades." + i, 0);
            }
            for (int i = 0; i < oneTimeUpgrades.size(); i++) {
                oneTimeUpgrades.get(i).level = saveManager.getInt(uuid, game, "onetime." + i, 0);
            }
            for (int i = 0; i < achievementsList.size(); i++) {
                achievementsList.get(i).unlocked = saveManager.getInt(uuid, game, "achiev." + i, 0) == 1;
            }
            for (int i = 0; i < rebirthUpgrades.size(); i++) {
                rebirthUpgrades.get(i).unlocked = saveManager.getInt(uuid, game, "rebirth." + rebirthUpgrades.get(i).id, 0) == 1;
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
        saveManager.set(uuid, game, "heavenlyChips", this.heavenlyChips);
        for (int i = 0; i < upgrades.size(); i++) {
            saveManager.set(uuid, game, "upgrades." + i, upgrades.get(i).level);
        }
        for (int i = 0; i < oneTimeUpgrades.size(); i++) {
            saveManager.set(uuid, game, "onetime." + i, oneTimeUpgrades.get(i).level);
        }
        for (int i = 0; i < achievementsList.size(); i++) {
            saveManager.set(uuid, game, "achiev." + i, achievementsList.get(i).unlocked ? 1 : 0);
        }
        for (int i = 0; i < rebirthUpgrades.size(); i++) {
            saveManager.set(uuid, game, "rebirth." + rebirthUpgrades.get(i).id, rebirthUpgrades.get(i).unlocked ? 1 : 0);
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
                currentAchPage = 0;
                render();
                return;
            } else if (slot == rebirthButtonSlot) {
                menuState = MenuState.REBIRTH;
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
                    boolean hasRebirthReq = u.reqRebirth.isEmpty() || hasRebirthUpgrade(u.reqRebirth);
                    if (!u.isUnlocked(cookies, autoCookiesPerSecond, clickMultiplier) || !hasRebirthReq) continue;
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
            } else if (menuState == MenuState.ACHIEVEMENTS) {
                if (slot == prevPageButtonSlot && currentAchPage > 0) {
                    currentAchPage--;
                    render();
                } else if (slot == nextPageButtonSlot && achievementsList.size() > (currentAchPage + 1) * 21) {
                    currentAchPage++;
                    render();
                }
            } else if (menuState == MenuState.REBIRTH) {
                if (slot == 4) { // Rebirth button slot
                    doRebirth();
                    render();
                    return;
                }
                for (RebirthUpgrade ru : rebirthUpgrades) {
                    if (ru.slot >= 0 && slot == ru.slot) {
                        if (!ru.unlocked && heavenlyChips >= ru.cost) {
                            heavenlyChips -= ru.cost;
                            ru.unlocked = true;
                            calculateStats();
                            render();
                        }
                        break;
                    }
                }
            }
        }
    }

    private void doRebirth() {
        double chipsToGet = calculatePendingChips();
        if (chipsToGet >= 1) {
            heavenlyChips += chipsToGet;
            cookies = 0;
            for (Upgrade u : upgrades) u.level = 0;
            for (Upgrade u : oneTimeUpgrades) u.level = 0;
            calculateStats();
            FileConfiguration msgConfig = InvGamesPlugin.getConfigManager().getMessages("messages");
            String msgAsc = msgConfig.getString("cookieclicker.msg_ascended", "&dYou Ascended and received &b%chips% Heavenly Chips&d!").replace("%chips%", String.valueOf((long)chipsToGet));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msgAsc));
        }
    }

    private double calculatePendingChips() {
        double tempCookies = cookies;
        double currentK = heavenlyChips;
        double pending = 0;
        while (true) {
            double cost = 1_000_000_000_000.0 * (3 * currentK * currentK + 3 * currentK + 1);
            if (tempCookies >= cost) {
                tempCookies -= cost;
                currentK++;
                pending++;
            } else {
                break;
            }
        }
        return pending;
    }

    private boolean hasRebirthUpgrade(String id) {
        for (RebirthUpgrade ru : rebirthUpgrades) {
            if (ru.id.equals(id)) return ru.unlocked;
        }
        return false;
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

        String strCookies = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.stats_cookies", "&fCookies: &e%cookies%").replace("%cookies%", String.valueOf((long)cookies)));
        String strClick = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.stats_click", "&fCookies per click: &e%click%").replace("%click%", String.format(java.util.Locale.US, "%.1f", clickMultiplier)));
        String strCps = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.stats_cps", "&fCookies per second: &e%cps%").replace("%cps%", String.format(java.util.Locale.US, "%.1f", autoCookiesPerSecond)));
        String strReturn = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.lore_return", "&7Return to game"));
        String backName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.back", "&c&lBack"));

        if (menuState == MenuState.MAIN) {
            // Render main cookie
            String cookieName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.cookie_name", "&6&lThe Ultimate Cookie"));
            ItemStack cookie = createItem(Material.COOKIE, cookieName);
            ItemMeta meta = cookie.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add(strCookies);
                lore.add(strClick);
                lore.add(strCps);
                meta.setLore(lore);
                cookie.setItemMeta(meta);
            }
            inventory.setItem(cookieSlot, cookie);

            // Render buttons
            String shopName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.upgrades_shop", "&e&lUpgrades Shop"));
            String shopLore = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.lore_upgrades", "&7Click to open upgrades"));
            inventory.setItem(upgradesButtonSlot, createItem(Material.ANVIL, shopName, List.of(shopLore)));

            if (oneTimeButtonSlot > 0) {
                String oneTimeName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.one_time_shop", "&b&lOne-Time Upgrades"));
                String oneTimeLore = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.lore_one_time", "&7Special rare upgrades"));
                inventory.setItem(oneTimeButtonSlot, createItem(Material.DIAMOND_PICKAXE, oneTimeName, List.of(oneTimeLore)));
            }
            if (achievementsButtonSlot > 0) {
                String achName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.achievements", "&d&lAchievements"));
                String achLore = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.lore_achievements", "&7View your progress"));
                inventory.setItem(achievementsButtonSlot, createItem(Material.EMERALD, achName, List.of(achLore)));
            }
            if (rebirthButtonSlot > 0) {
                String rbName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.rebirth", "&5&lRebirth Menu"));
                String rbLore1 = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.lore_rebirth_1", "&7Ascend and unlock heavenly magic"));
                String rbLore2 = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.lore_rebirth_2", "&bChips: %chips%").replace("%chips%", String.valueOf((long)heavenlyChips)));
                inventory.setItem(rebirthButtonSlot, createItem(Material.NETHER_STAR, rbName, List.of(rbLore1, rbLore2)));
            }

        } else if (menuState == MenuState.ACHIEVEMENTS) {
            inventory.setItem(backButtonSlot, createItem(Material.ARROW, backName, List.of(strReturn)));
            inventory.setItem(4, createItem(Material.COOKIE, strCookies));

            int itemsPerPage = 21;
            int startIndex = currentAchPage * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, achievementsList.size());

            if (currentAchPage > 0) {
                String prevName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.prev_page", "&e&lPrevious Page"));
                inventory.setItem(prevPageButtonSlot, createItem(Material.ARROW, prevName));
            }
            if (achievementsList.size() > endIndex) {
                String nextName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.next_page", "&e&lNext Page"));
                inventory.setItem(nextPageButtonSlot, createItem(Material.ARROW, nextName));
            }

            int startSlot = 10;
            int col = 0;
            int row = 0;
            for (int i = startIndex; i < endIndex; i++) {
                Achievement a = achievementsList.get(i);
                Material mat = a.unlocked ? a.material : Material.BARRIER;
                String dnUnlocked = ChatColor.translateAlternateColorCodes('&', "&a" + a.name);
                String dnLockedRaw = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.lore_locked", "&c%name% &7(Locked)").replace("%name%", a.name));
                String display = a.unlocked ? dnUnlocked : dnLockedRaw;

                List<String> lore = new ArrayList<>();
                lore.add("§7" + a.description);
                lore.add("");
                if (!a.unlocked) {
                    if (a.reqCookies > 0) lore.add(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.req_cookies", "&eRequires Cookies: %req%").replace("%req%", String.valueOf(a.reqCookies))));
                    if (a.reqCps > 0) lore.add(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.req_cps", "&eRequires CPS: %req%").replace("%req%", String.valueOf(a.reqCps))));
                    if (a.reqClick > 0) lore.add(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.req_click", "&eRequires Click: %req%").replace("%req%", String.valueOf(a.reqClick))));
                } else {
                    lore.add(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.achievement_unlocked", "&a&lUNLOCKED")));
                }

                inventory.setItem(startSlot + row * 9 + col, createItem(mat, display, lore));
                col++;
                if (col >= 7) {
                    col = 0;
                    row++;
                }
            }
        } else if (menuState == MenuState.REBIRTH) {
            inventory.setItem(backButtonSlot, createItem(Material.ARROW, backName, List.of(strReturn)));

            String chipStats = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.stats_chips", "&bHeavenly Chips: &f%chips%").replace("%chips%", String.valueOf((long)heavenlyChips)));
            inventory.setItem(0, createItem(Material.NETHER_STAR, chipStats));

            double pending = calculatePendingChips();
            String doReb = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.do_rebirth", "&d&lAscend!"));
            List<String> rLore = new ArrayList<>();
            rLore.add(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.lore_ascend_1", "&7Forfeit your cookies to ascend.")));
            rLore.add(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.lore_ascend_2", "&bPending Chips: &f%chips%").replace("%chips%", String.valueOf((long)pending))));
            rLore.add(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.lore_ascend_3", "&eCurrent Cookies: %cookies%").replace("%cookies%", String.valueOf((long)cookies))));
            inventory.setItem(4, createItem(Material.ENDER_EYE, doReb, rLore));

            for (RebirthUpgrade ru : rebirthUpgrades) {
                if (ru.slot >= 0) {
                    Material mat = ru.unlocked ? ru.material : Material.BARRIER;
                    String dn = ChatColor.translateAlternateColorCodes('&', (ru.unlocked ? "&a" : "&c") + ru.name);
                    List<String> rl = new ArrayList<>();
                    rl.add("§7" + ru.description);
                    rl.add("");
                    if (ru.unlocked) {
                        rl.add(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.rebirth_purchased", "&a&lPURCHASED")));
                    } else {
                        String costStr = String.valueOf(ru.cost);
                        if (heavenlyChips >= ru.cost) {
                            rl.add(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.rebirth_cost_afford", "&eCost: %cost% Chips").replace("%cost%", costStr)));
                        } else {
                            rl.add(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.rebirth_cost_deny", "&cCost: %cost% Chips").replace("%cost%", costStr)));
                        }
                    }
                    inventory.setItem(ru.slot, createItem(mat, dn, rl));
                }
            }
        } else {
            inventory.setItem(backButtonSlot, createItem(Material.ARROW, backName, List.of(strReturn)));
            inventory.setItem(4, createItem(Material.COOKIE, strCookies));

            List<Upgrade> activeList = menuState == MenuState.UPGRADES ? upgrades : oneTimeUpgrades;
            int startSlot = 10;
            int col = 0;
            int row = 0;
            for (Upgrade u : activeList) {
                boolean hasRebirthReq = u.reqRebirth.isEmpty() || hasRebirthUpgrade(u.reqRebirth);
                if (!u.isUnlocked(cookies, autoCookiesPerSecond, clickMultiplier) || !hasRebirthReq) continue;
                if (u.oneTime && u.level >= 1) continue;

                int currentCost = u.getCurrentCost();
                boolean canAfford = cookies >= currentCost;

                String upName = ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.upgrade_level", "&a%name% &7(Level %level%)").replace("%name%", u.name).replace("%level%", String.valueOf(u.level)));
                ItemStack item = createItem(u.material, upName);
                ItemMeta uMeta = item.getItemMeta();
                if (uMeta != null) {
                    List<String> lore = new ArrayList<>();
                    lore.add("§7" + u.description);
                    lore.add("");
                    if (canAfford) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.upgrade_cost_afford", "&eCost: %cost%").replace("%cost%", String.valueOf(currentCost))));
                    } else {
                        lore.add(ChatColor.translateAlternateColorCodes('&', msgConfig.getString("cookieclicker.upgrade_cost_deny", "&cCost: %cost% &4(Too expensive)").replace("%cost%", String.valueOf(currentCost))));
                    }
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

    private static class RebirthUpgrade {
        String id;
        String name;
        Material material;
        double cost;
        double cpsMult;
        double clickMult;
        int slot;
        String description;
        boolean unlocked = false;

        public RebirthUpgrade(String id, String name, Material material, double cost, double cpsMult, double clickMult, int slot, String description) {
            this.id = id;
            this.name = name;
            this.material = material;
            this.cost = cost;
            this.cpsMult = cpsMult;
            this.clickMult = clickMult;
            this.slot = slot;
            this.description = description;
        }
    }

    private static class Upgrade {
        String id;
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
        String reqRebirth;
        boolean oneTime;
        String description;

        public Upgrade(String id, String name, Material material, double baseCost, double costMultiplier,
                       double cpsAdd, double cpsMult, double clickAdd, double clickMult,
                       double reqCookies, double reqCps, double reqClick, String reqRebirth, boolean oneTime, String description) {
            this.id = id;
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
            this.reqRebirth = reqRebirth;
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
