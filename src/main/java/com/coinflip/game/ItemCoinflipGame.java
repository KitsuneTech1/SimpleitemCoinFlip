package com.coinflip.game;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Represents a coinflip game that uses any item type
 */
public class ItemCoinflipGame {

    private final UUID gameId;
    private final UUID creatorId;
    private final String creatorName;
    private final ItemStack betItem; // The exact item being bet
    private final int amount;
    private UUID opponentId;
    private String opponentName;
    private GameState state;
    private UUID winnerId;

    public ItemCoinflipGame(Player creator, ItemStack item, int amount) {
        this.gameId = UUID.randomUUID();
        this.creatorId = creator.getUniqueId();
        this.creatorName = creator.getName();
        // Clone the item and set the amount
        this.betItem = item.clone();
        this.betItem.setAmount(1); // Store as single item for reference
        this.amount = amount;
        this.state = GameState.WAITING;
    }

    public UUID getGameId() {
        return gameId;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public ItemStack getBetItem() {
        return betItem.clone();
    }

    public Material getBetMaterial() {
        return betItem.getType();
    }

    public int getAmount() {
        return amount;
    }

    public UUID getOpponentId() {
        return opponentId;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public GameState getState() {
        return state;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public void setOpponent(Player opponent) {
        this.opponentId = opponent.getUniqueId();
        this.opponentName = opponent.getName();
        this.state = GameState.IN_PROGRESS;
    }

    public UUID performFlip() {
        // 50/50 random chance
        boolean creatorWins = Math.random() < 0.5;
        this.winnerId = creatorWins ? creatorId : opponentId;
        this.state = GameState.COMPLETED;
        return winnerId;
    }

    public String getWinnerName() {
        if (winnerId == null)
            return null;
        return winnerId.equals(creatorId) ? creatorName : opponentName;
    }

    public String getLoserName() {
        if (winnerId == null)
            return null;
        return winnerId.equals(creatorId) ? opponentName : creatorName;
    }

    public int getTotalPot() {
        return amount * 2;
    }

    /**
     * Get the display name for the bet item
     */
    public String getItemDisplayName() {
        if (betItem.hasItemMeta() && betItem.getItemMeta().hasDisplayName()) {
            return betItem.getItemMeta().getDisplayName();
        }
        return formatMaterialName(betItem.getType());
    }

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    public enum GameState {
        WAITING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
