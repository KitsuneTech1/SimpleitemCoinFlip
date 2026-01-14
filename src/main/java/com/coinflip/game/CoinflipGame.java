package com.coinflip.game;

import com.coinflip.currency.Currency;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CoinflipGame {

    private final UUID gameId;
    private final UUID creatorId;
    private final String creatorName;
    private final Currency currency;
    private final int amount;
    private UUID opponentId;
    private String opponentName;
    private GameState state;
    private UUID winnerId;

    public CoinflipGame(Player creator, Currency currency, int amount) {
        this.gameId = UUID.randomUUID();
        this.creatorId = creator.getUniqueId();
        this.creatorName = creator.getName();
        this.currency = currency;
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

    public Currency getCurrency() {
        return currency;
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

    public enum GameState {
        WAITING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
