package me.robot7769.InvGames.games;

import me.robot7769.InvGames.api.Minigame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TetrisGame extends Minigame {

    private static final int WIDTH = 9;
    private static final int HEIGHT = 5; // Herní plocha v rámci inventáře
    private static final int INVENTORY_SIZE = 54;

    // Ovládací prvky v posledním řádku
    private static final int CTRL_LEFT = 47;
    private static final int CTRL_ROTATE = 48;
    private static final int CTRL_DOWN = 49;
    private static final int CTRL_RIGHT = 51;
    private static final int CTRL_INFO = 53;

    private final Material[] board = new Material[WIDTH * HEIGHT];
    private final Random random = new Random();

    private Tetromino currentPiece;
    private int pieceX, pieceY;
    private int score = 0;
    private boolean finished = false;

    public TetrisGame(Player player) {
        super(player);
        this.tickInterval = 30; // Rychlost padání (1.5 sekundy)
    }

    @Override
    public void start() {
        inventory = Bukkit.createInventory(player, INVENTORY_SIZE, "InvGames - Tetris");

        Arrays.fill(board, null);

        spawnPiece();
        render();
        player.openInventory(inventory);
        player.sendMessage("Tetris start! Ovládej šipkami a rotuj středovým tlačítkem.");
    }

    @Override
    public void stop() {
        if (finished) return;
        finished = true;
        player.sendMessage("Tetris ukončen. Skóre: " + score);
    }

    private void spawnPiece() {
        currentPiece = Tetromino.values()[random.nextInt(Tetromino.values().length)];
        pieceX = WIDTH / 2 - 1;
        pieceY = 0;

        if (collides(pieceX, pieceY, currentPiece.shape)) {
            endGame("Plocha je plná!");
        }
    }

    @Override
    public void onClick(int slot) {
        if (finished) return;

        switch (slot) {
            case CTRL_LEFT -> move(-1, 0);
            case CTRL_RIGHT -> move(1, 0);
            case CTRL_DOWN -> move(0, 1);
            case CTRL_ROTATE -> rotate();
        }
        render();
    }

    @Override
    public void onTick() {
        if (finished) return;

        if (!move(0, 1)) {
            lockPiece();
            clearLines();
            spawnPiece();
        }
        render();
    }

    private boolean move(int dx, int dy) {
        if (!collides(pieceX + dx, pieceY + dy, currentPiece.shape)) {
            pieceX += dx;
            pieceY += dy;
            return true;
        }
        return false;
    }

    private void rotate() {
        int[][] rotated = new int[currentPiece.shape[0].length][currentPiece.shape.length];
        for (int y = 0; y < currentPiece.shape.length; y++) {
            for (int x = 0; x < currentPiece.shape[0].length; x++) {
                rotated[x][currentPiece.shape.length - 1 - y] = currentPiece.shape[y][x];
            }
        }
        if (!collides(pieceX, pieceY, rotated)) {
            currentPiece.shape = rotated;
        }
    }

    private boolean collides(int nx, int ny, int[][] shape) {
        for (int y = 0; y < shape.length; y++) {
            for (int x = 0; x < shape[y].length; x++) {
                if (shape[y][x] == 0) continue;
                int bx = nx + x;
                int by = ny + y;
                if (bx < 0 || bx >= WIDTH || by >= HEIGHT) return true;
                if (by >= 0 && board[by * WIDTH + bx] != null) return true;
            }
        }
        return false;
    }

    private void lockPiece() {
        for (int y = 0; y < currentPiece.shape.length; y++) {
            for (int x = 0; x < currentPiece.shape[y].length; x++) {
                if (currentPiece.shape[y][x] != 0) {
                    int boardIdx = (pieceY + y) * WIDTH + (pieceX + x);
                    if (boardIdx >= 0 && boardIdx < board.length) {
                        board[boardIdx] = currentPiece.material;
                    }
                }
            }
        }
    }

    private void clearLines() {
        for (int y = 0; y < HEIGHT; y++) {
            boolean full = true;
            for (int x = 0; x < WIDTH; x++) {
                if (board[y * WIDTH + x] == null) {
                    full = false;
                    break;
                }
            }
            if (full) {
                score += 100;
                for (int ty = y; ty > 0; ty--) {
                    System.arraycopy(board, (ty - 1) * WIDTH, board, ty * WIDTH, WIDTH);
                }
                for (int x = 0; x < WIDTH; x++) board[x] = null;
            }
        }
    }

    private void endGame(String reason) {
        finished = true;
        player.sendMessage("Tetris - konec hry: " + reason + " Skóre: " + score);
        player.closeInventory();
    }

    private void render() {
        ItemStack bg = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < INVENTORY_SIZE; i++) inventory.setItem(i, bg);

        // Vykreslení pevné desky
        for (int i = 0; i < board.length; i++) {
            if (board[i] != null) {
                inventory.setItem(i, createItem(board[i], "Blok"));
            }
        }

        // Vykreslení padajícího dílku
        for (int y = 0; y < currentPiece.shape.length; y++) {
            for (int x = 0; x < currentPiece.shape[y].length; x++) {
                if (currentPiece.shape[y][x] != 0) {
                    int slot = (pieceY + y) * WIDTH + (pieceX + x);
                    if (slot >= 0 && slot < board.length) {
                        inventory.setItem(slot, createItem(currentPiece.material, "Padající blok"));
                    }
                }
            }
        }

        renderControls();
    }

    private void renderControls() {
        inventory.setItem(CTRL_LEFT, createItem(Material.ARROW, "<- Doleva"));
        inventory.setItem(CTRL_ROTATE, createItem(Material.SUNFLOWER, "Otočit"));
        inventory.setItem(CTRL_DOWN, createItem(Material.ARROW, "Rychleji dolů"));
        inventory.setItem(CTRL_RIGHT, createItem(Material.ARROW, "Doprava ->"));

        List<String> lore = List.of("Skóre: " + score);
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
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private enum Tetromino {
        I(Material.CYAN_CONCRETE, new int[][]{{1, 1, 1, 1}}),
        O(Material.YELLOW_CONCRETE, new int[][]{{1, 1}, {1, 1}}),
        T(Material.PURPLE_CONCRETE, new int[][]{{0, 1, 0}, {1, 1, 1}}),
        L(Material.ORANGE_CONCRETE, new int[][]{{1, 0}, {1, 0}, {1, 1}}),
        J(Material.BLUE_CONCRETE, new int[][]{{0, 1}, {0, 1}, {1, 1}}),
        S(Material.LIME_CONCRETE, new int[][]{{0, 1, 1}, {1, 1, 0}}),
        Z(Material.RED_CONCRETE, new int[][]{{1, 1, 0}, {0, 1, 1}});

        private final Material material;
        private int[][] shape;

        Tetromino(Material material, int[][] shape) {
            this.material = material;
            this.shape = shape;
        }
    }
}