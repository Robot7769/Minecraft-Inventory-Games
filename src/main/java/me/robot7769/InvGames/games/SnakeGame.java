package me.robot7769.InvGames.games;

import me.robot7769.InvGames.api.Minigame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class SnakeGame extends Minigame {

    private static final int WIDTH = 9;
    private static final int PLAY_HEIGHT = 5;
    private static final int PLAY_SIZE = WIDTH * PLAY_HEIGHT;
    private static final int INVENTORY_SIZE = 54;

    private static final int CTRL_LEFT = 48;
    private static final int CTRL_UP = 49;
    private static final int CTRL_RIGHT = 50;
    private static final int CTRL_DOWN = 51;
    private static final int CTRL_INFO = 53;

    private final Random random = new Random();
    private final Deque<Integer> snake = new ArrayDeque<>();

    private Direction direction = Direction.RIGHT;
    private int foodSlot = -1;
    private int score = 0;
    private boolean finished = false;

    public SnakeGame(Player player) {
        super(player);
        // Pomalejsi start: 12 tiku ~= 0.6 sekundy na krok.
        this.tickInterval = 12;
    }

    @Override
    public void start() {
        inventory = Bukkit.createInventory(player, INVENTORY_SIZE, "InvGames - Snake");

        int center = 22;
        snake.clear();
        snake.addFirst(center);
        snake.addLast(center - 1);
        snake.addLast(center - 2);

        spawnFood();
        render();
        player.sendMessage("Snake start! Ovladas sipkami dole: <- ^ -> v");
        player.openInventory(inventory);
    }

    @Override
    public void stop() {
        if (finished) {
            return;
        }
        finished = true;

        if (player.isOnline() && player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.closeInventory();
        }
        player.sendMessage("Snake ukoncen. Skore: " + score);
    }

    @Override
    public void onClick(int slot) {
        if (finished || slot < 0 || slot >= INVENTORY_SIZE) {
            return;
        }

        switch (slot) {
            case CTRL_LEFT -> setDirection(Direction.LEFT);
            case CTRL_UP -> setDirection(Direction.UP);
            case CTRL_RIGHT -> setDirection(Direction.RIGHT);
            case CTRL_DOWN -> setDirection(Direction.DOWN);
            default -> {
                // Fallback: klik v hernim poli stale umi otocit smer relativne k hlave.
                if (slot >= PLAY_SIZE) {
                    return;
                }

                Integer head = snake.peekFirst();
                if (head == null) {
                    return;
                }

                int headX = head % WIDTH;
                int headY = head / WIDTH;
                int targetX = slot % WIDTH;
                int targetY = slot / WIDTH;

                int dx = targetX - headX;
                int dy = targetY - headY;

                if (Math.abs(dx) >= Math.abs(dy) && dx != 0) {
                    setDirection(dx > 0 ? Direction.RIGHT : Direction.LEFT);
                } else if (dy != 0) {
                    setDirection(dy > 0 ? Direction.DOWN : Direction.UP);
                }
            }
        }
    }

    @Override
    public void onTick() {
        if (finished) {
            return;
        }

        Integer head = snake.peekFirst();
        if (head == null) {
            endGame("Chyba hry: had nema hlavu.");
            return;
        }

        int headX = head % WIDTH;
        int headY = head / WIDTH;
        int nextX = headX + direction.dx;
        int nextY = headY + direction.dy;

        if (nextX < 0 || nextX >= WIDTH || nextY < 0 || nextY >= PLAY_HEIGHT) {
            endGame("Narazil jsi do steny.");
            return;
        }

        int nextSlot = nextY * WIDTH + nextX;
        boolean isFood = nextSlot == foodSlot;

        // Allow moving into the current tail slot when not growing this tick.
        Integer currentTail = snake.peekLast();
        boolean hitsBody = snake.contains(nextSlot) && (isFood || !nextSlotEqualsTail(nextSlot, currentTail));
        if (hitsBody) {
            endGame("Narazil jsi do sebe.");
            return;
        }

        snake.addFirst(nextSlot);

        if (isFood) {
            score++;
            spawnFood();
        } else {
            snake.removeLast();
        }

        render();
    }

    private boolean nextSlotEqualsTail(int nextSlot, Integer tail) {
        return tail != null && nextSlot == tail;
    }

    private void endGame(String reason) {
        if (finished) {
            return;
        }
        finished = true;
        player.sendMessage("Snake - konec hry: " + reason + " Skore: " + score);
        if (player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.closeInventory();
        }
    }

    private void setDirection(Direction next) {
        if (direction.isOpposite(next)) {
            return;
        }
        direction = next;
    }

    private void spawnFood() {
        List<Integer> freeSlots = new ArrayList<>();
        for (int i = 0; i < PLAY_SIZE; i++) {
            if (!snake.contains(i)) {
                freeSlots.add(i);
            }
        }

        if (freeSlots.isEmpty()) {
            endGame("Vyhral jsi! Zaplnil jsi cele pole.");
            return;
        }

        foodSlot = freeSlots.get(random.nextInt(freeSlots.size()));
    }

    private void render() {
        ItemStack empty = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack body = createItem(Material.LIME_WOOL, "Snake");
        ItemStack head = createItem(Material.GREEN_WOOL, "Hlava");
        ItemStack food = createItem(Material.RED_WOOL, "Jidlo");

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setItem(i, empty);
        }

        for (Integer slot : snake) {
            inventory.setItem(slot, body);
        }

        Integer snakeHead = snake.peekFirst();
        if (snakeHead != null) {
            inventory.setItem(snakeHead, head);
        }

        if (foodSlot >= 0 && foodSlot < PLAY_SIZE) {
            inventory.setItem(foodSlot, food);
        }

        renderControls();
    }

    private void renderControls() {
        inventory.setItem(CTRL_LEFT, createItem(Material.YELLOW_WOOL, "<- Doleva"));
        inventory.setItem(CTRL_UP, createItem(Material.YELLOW_WOOL, "^ Nahoru"));
        inventory.setItem(CTRL_RIGHT, createItem(Material.YELLOW_WOOL, "-> Doprava"));
        inventory.setItem(CTRL_DOWN, createItem(Material.YELLOW_WOOL, "v Dolu"));

        List<String> lore = new ArrayList<>();
        lore.add("Skore: " + score);
        lore.add("Rychlost: " + tickInterval + " tiku/krok");
        lore.add("Klikni na sipky dole");
        inventory.setItem(CTRL_INFO, createItem(Material.PAPER, "Info", lore));
    }

    private ItemStack createItem(Material material, String name) {
        return createItem(material, name, List.of());
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

    private enum Direction {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        private boolean isOpposite(Direction other) {
            return this.dx == -other.dx && this.dy == -other.dy;
        }
    }
}

