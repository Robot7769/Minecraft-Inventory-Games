package me.robot7769.InvGames.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class Minigame {

    protected Player player;
    protected Inventory inventory;

    // How often the game should tick (in server ticks).
    public int tickInterval = 20;
    // Internal counter used by GameManager ticker.
    public int tickCounter = 0;

    protected Minigame(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public abstract void start();

    public abstract void stop();

    public abstract void onClick(int slot);

    public abstract void onTick();
}

