package me.robot7769.InvGames.games;
import me.robot7769.InvGames.api.Minigame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class SlotMachineGame extends Minigame {
    private static final int INVENTORY_SIZE = 54;
    private static final int[] BOARD_SLOTS = {19, 20, 21, 28, 29, 30, 37, 38, 39};
    private static final int SLOT_BET_DOWN = 24;
    private static final int SLOT_SPIN = 25;
    private static final int SLOT_BET_UP = 26;
    private static final int SLOT_CREDITS = 0;
    private static final int SLOT_INFO = 8;
    private final Random random = new Random();
    private final Symbol[] board = new Symbol[9];
    private int credits = 10;
    private int bet = 1;
    private boolean spinning = false;
    private int spinTicksRemaining = 0;
    private boolean finished = false;
    public SlotMachineGame(Player player) {
        super(player);
        this.tickInterval = 4;
    }
    @Override
    public void start() {
        inventory = Bukkit.createInventory(player, INVENTORY_SIZE, "InvGames - Slot Machine");
        randomizeBoard();
        render();
        player.openInventory(inventory);
        player.sendMessage("§aSlot Machine spuštěna. Klikni na §2SPIN§a.");
    }
    @Override
    public void stop() {
        if (finished) {
            return;
        }
        finished = true;
        spinning = false;
        if (player.isOnline() && inventory != null && player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.closeInventory();
        }
        player.sendMessage("§eSlot Machine ukončena. Zbývající kredit: §f" + credits);
    }
    @Override
    public void onClick(int slot) {
        if (finished) {
            return;
        }
        switch (slot) {
            case SLOT_BET_DOWN -> changeBet(-1);
            case SLOT_BET_UP -> changeBet(1);
            case SLOT_SPIN -> startSpin();
            default -> {
                // Kliky do hrací plochy nic nedělají.
            }
        }
        render();
    }
    @Override
    public void onTick() {
        if (finished || !spinning) {
            return;
        }
        shiftBoardDown();
        spinTicksRemaining--;
        if (spinTicksRemaining > 0) {
            render();
            return;
        }
        spinning = false;
        if (isMiddleRowTriple()) {
            int payout = bet * 3;
            credits += payout;
            player.sendMessage("§6Výhra! §a+" + payout + " kreditů.");
        } else {
            player.sendMessage("§7Tentokrát nic. Zkus to znovu.");
        }
        render();
        if (credits <= 0) {
            endGame("Došly ti kredity.");
        }
    }
    private void changeBet(int delta) {
        if (spinning) {
            player.sendMessage("§cBěhem spinu nemůžeš měnit sázku.");
            return;
        }
        int nextBet = bet + delta;
        if (nextBet < 1) {
            nextBet = 1;
        }
        if (credits > 0 && nextBet > credits) {
            nextBet = credits;
        }
        if (nextBet == bet) {
            player.sendMessage("§eSázku už nejde dál upravit.");
            return;
        }
        bet = nextBet;
        player.sendMessage("§aSázka nastavena na §f" + bet + "§a kreditů.");
    }
    private void startSpin() {
        if (spinning) {
            player.sendMessage("§eReels už běží.");
            return;
        }
        if (credits < bet) {
            player.sendMessage("§cNemáš dost kreditů na sázku.");
            return;
        }
        credits -= bet;
        spinning = true;
        spinTicksRemaining = 12 + random.nextInt(8);
        player.sendMessage("§aSpouštím spin za §f" + bet + "§a kreditů.");
    }
    private void endGame(String reason) {
        if (finished) {
            return;
        }
        finished = true;
        spinning = false;
        player.sendMessage("§cSlot Machine - konec hry: §f" + reason + " §7Zbývající kredit: §f" + credits);
        if (player.isOnline() && inventory != null && player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.closeInventory();
        }
    }
    private void randomizeBoard() {
        for (int i = 0; i < board.length; i++) {
            board[i] = randomSymbol();
        }
    }
    private void shiftBoardDown() {
        for (int column = 0; column < 3; column++) {
            int top = column;
            int middle = 3 + column;
            int bottom = 6 + column;
            board[bottom] = board[middle];
            board[middle] = board[top];
            board[top] = randomSymbol();
        }
    }
    private boolean isMiddleRowTriple() {
        Symbol left = board[3];
        Symbol center = board[4];
        Symbol right = board[5];
        return left != null && left == center && center == right;
    }
    private Symbol randomSymbol() {
        Symbol[] values = Symbol.values();
        return values[random.nextInt(values.length)];
    }
    private void render() {
        ItemStack background = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setItem(i, background);
        }
        inventory.setItem(SLOT_CREDITS, createCreditItem());
        inventory.setItem(SLOT_INFO, createInfoItem());
        for (int i = 0; i < BOARD_SLOTS.length; i++) {
            inventory.setItem(BOARD_SLOTS[i], createSymbolItem(board[i]));
        }
        inventory.setItem(SLOT_BET_DOWN, createControlItem(
                Material.REDSTONE_TORCH,
                "§cSázka -1",
                List.of("§7Sníží sázku o 1 kredit.", "§7Minimální sázka je §f1")
        ));
        inventory.setItem(SLOT_SPIN, createControlItem(
                spinning ? Material.ORANGE_CONCRETE : Material.LIME_CONCRETE,
                spinning ? "§6Spin běží..." : "§aSPIN",
                spinning
                        ? List.of("§7Počkej, než se kola zastaví.")
                        : List.of("§7Kliknutím vsadíš aktuální sázku.", "§7Výhra = §f3x §7sázka při 3 stejných symbolech uprostřed.")
        ));
        inventory.setItem(SLOT_BET_UP, createControlItem(
                Material.GLOWSTONE_DUST,
                "§aSázka +1",
                List.of("§7Zvýší sázku o 1 kredit.", "§7Sázka nesmí být vyšší než tvoje kredity.")
        ));
    }
    private ItemStack createCreditItem() {
        int displayAmount = credits > 0 ? credits : 1;
        ItemStack item = new ItemStack(Material.REDSTONE, displayAmount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cKredit");
            meta.setLore(List.of(
                    "§7Aktuální kredit: §f" + credits,
                    "§7Aktuální sázka: §f" + bet,
                    spinning ? "§eProbíhá spin..." : "§aPřipraveno na další spin"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    private ItemStack createInfoItem() {
        return createControlItem(
                Material.PAPER,
                "§fJak hrát",
                List.of(
                        "§7Klikni na §aSPIN§7 pro roztočení.",
                        "§7Použij §cSázka -1§7 a §aSázka +1§7 pro změnu sázky.",
                        "§7Výhra nastane, když jsou ve střední řadě §f3 stejné symboly§7.",
                        "§7Při výhře dostaneš §f3x§7 sázku.",
                        "§7Hra končí při zavření nebo při 0 kreditů."
                )
        );
    }
    private ItemStack createSymbolItem(Symbol symbol) {
        if (symbol == null) {
            return createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        }
        return createItem(symbol.material, symbol.displayName);
    }
    private ItemStack createControlItem(Material material, String name, List<String> lore) {
        return createItem(material, name, lore);
    }
    private ItemStack createItem(Material material, String name) {
        return createItem(material, name, List.of());
    }
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(new ArrayList<>(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
    private enum Symbol {
        CHERRY(Material.RED_MUSHROOM, "§cTřešeň"),
        DIAMOND(Material.DIAMOND, "§bDiamant"),
        EMERALD(Material.EMERALD, "§aSmaragd"),
        GOLD(Material.GOLD_INGOT, "§6Zlato"),
        LAPIS(Material.LAPIS_LAZULI, "§9Lapis"),
        REDSTONE(Material.REDSTONE, "§cRedstone"),
        STAR(Material.NETHER_STAR, "§fHvězda");
        private final Material material;
        private final String displayName;
        Symbol(Material material, String displayName) {
            this.material = material;
            this.displayName = displayName;
        }
    }
}
