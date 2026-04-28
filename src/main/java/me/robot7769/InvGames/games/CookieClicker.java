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

public class CookieClicker extends Minigame {
    private static final int WIDTH = 9;
    private static final int HEIGHT = 5;
    private static final int INVENTORY_SIZE = WIDTH * HEIGHT;
    private static final int COOKIE_SLOT = 22; // Center
    private static final int UPGRADES_BUTTON_SLOT = 26; // Open upgrades menu
    private static final int BACK_BUTTON_SLOT = 36; // Return to main menu from upgrades

    private double cookies = 0;
    private double clickMultiplier = 1;
    private double autoCookiesPerSecond = 0;

    private boolean inUpgradesMenu = false;
    private List<Upgrade> upgrades;

    public CookieClicker(Player player) {
        super(player);
        this.tickInterval = 20; // 1 second intervals for auto production
        initUpgrades();
    }

    private void initUpgrades() {
        upgrades = new ArrayList<>();
        upgrades.add(new Upgrade("Cursor", Material.WOODEN_PICKAXE, 15, 1.15, 0, 1, "Increases cookies per click"));
        upgrades.add(new Upgrade("Grandma", Material.WHEAT, 100, 1.15, 1, 0, "Bakes cookies passively"));
        upgrades.add(new Upgrade("Farm", Material.DIRT, 1100, 1.15, 8, 0, "Grows cookie plants"));
        upgrades.add(new Upgrade("Mine", Material.STONE_PICKAXE, 12000, 1.15, 47, 0, "Mines cookie dough"));
    }

    private void calculateStats() {
        autoCookiesPerSecond = 0;
        clickMultiplier = 1;
        for (Upgrade u : upgrades) {
            autoCookiesPerSecond += u.level * u.cpsPerLevel;
            clickMultiplier += u.level * u.clickPerLevel;
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
            long lastSaveTime = saveManager.getLong(uuid, game, "lastSaveTime", 0);
            calculateStats();

            long currentTime = System.currentTimeMillis();
            long diffMillis = currentTime - lastSaveTime;
            if (diffMillis > 0 && autoCookiesPerSecond > 0 && lastSaveTime > 0) {
                double secondsAway = diffMillis / 1000.0;
                double earned = secondsAway * autoCookiesPerSecond;
                this.cookies += earned;
                player.sendMessage("§a[Cookie Clicker] You earned §e" + (int) earned + " §acookies while you were away!");
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
        saveManager.set(uuid, game, "lastSaveTime", System.currentTimeMillis());
        saveManager.save();
    }

    @Override
    public void start() {
        loadData();
        inventory = Bukkit.createInventory(player, INVENTORY_SIZE, "Cookie Clicker");
        inUpgradesMenu = false;
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
        if (!inUpgradesMenu) {
            if (slot == COOKIE_SLOT) {
                cookies += clickMultiplier;
                render();
                return;
            } else if (slot == UPGRADES_BUTTON_SLOT) {
                inUpgradesMenu = true;
                render();
                return;
            }
        } else {
            if (slot == BACK_BUTTON_SLOT) {
                inUpgradesMenu = false;
                render();
                return;
            }

            // Check if an upgrade was clicked
            int startSlot = 10;
            int col = 0;
            int row = 0;
            for (int i = 0; i < upgrades.size(); i++) {
                int upgradeSlot = startSlot + row * 9 + col;
                if (slot == upgradeSlot) {
                    Upgrade u = upgrades.get(i);
                    if (cookies >= u.getCurrentCost()) {
                        cookies -= u.getCurrentCost();
                        u.purchase();
                        calculateStats();
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

    @Override
    public void onTick() {
        if (autoCookiesPerSecond > 0) {
            cookies += autoCookiesPerSecond;
            render();
        }
    }

    private void render() {
        if (inventory == null) return;
        inventory.clear();

        ItemStack bg = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setItem(i, bg);
        }

        if (!inUpgradesMenu) {
            // Render main cookie
            ItemStack cookie = createItem(Material.COOKIE, "§6§lThe Ultimate Cookie");
            ItemMeta meta = cookie.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("§fCookies: §e" + (int) cookies);
                lore.add("§fCookies per click: §e" + clickMultiplier);
                lore.add("§fCookies per second: §e" + autoCookiesPerSecond);
                meta.setLore(lore);
                cookie.setItemMeta(meta);
            }
            inventory.setItem(COOKIE_SLOT, cookie);

            // Render upgrades button
            inventory.setItem(UPGRADES_BUTTON_SLOT, createItem(Material.ANVIL, "§e§lUpgrades Shop", List.of("§7Click to open upgrades")));
        } else {
            // Render back button
            inventory.setItem(BACK_BUTTON_SLOT, createItem(Material.ARROW, "§c§lBack", List.of("§7Return to game")));

            // Render cookie info display
            inventory.setItem(4, createItem(Material.COOKIE, "§fCookies: §e" + (int) cookies));

            // Render upgrades
            int startSlot = 10;
            int col = 0;
            int row = 0;
            for (Upgrade u : upgrades) {
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
        double cpsPerLevel;
        double clickPerLevel;
        String description;

        public Upgrade(String name, Material material, double baseCost, double costMultiplier, double cpsPerLevel, double clickPerLevel, String description) {
            this.name = name;
            this.material = material;
            this.baseCost = baseCost;
            this.costMultiplier = costMultiplier;
            this.cpsPerLevel = cpsPerLevel;
            this.clickPerLevel = clickPerLevel;
            this.description = description;
        }

        public int getCurrentCost() {
            return (int) Math.floor(baseCost * Math.pow(costMultiplier, level));
        }

        public void purchase() {
            level++;
        }
    }
}
