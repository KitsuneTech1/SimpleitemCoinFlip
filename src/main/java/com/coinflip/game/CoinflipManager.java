package com.coinflip.game;

import com.coinflip.CoinflipPlugin;
import com.coinflip.currency.Currency;
import com.coinflip.currency.CurrencyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CoinflipManager {

    private final CoinflipPlugin plugin;
    private final Map<UUID, CoinflipGame> pendingGames;
    private final Map<UUID, CoinflipGame> activeGames;

    public CoinflipManager(CoinflipPlugin plugin) {
        this.plugin = plugin;
        this.pendingGames = new ConcurrentHashMap<>();
        this.activeGames = new ConcurrentHashMap<>();
    }

    /**
     * Create a new coinflip game
     */
    public CoinflipGame createGame(Player creator, Currency currency, int amount) {
        // Check if player already has a pending game
        if (hasActiveCoinflip(creator)) {
            return null;
        }

        // Check and remove currency from player
        if (!CurrencyManager.removeCurrency(creator, currency, amount)) {
            return null;
        }

        CoinflipGame game = new CoinflipGame(creator, currency, amount);
        pendingGames.put(game.getGameId(), game);

        return game;
    }

    /**
     * Join an existing coinflip game
     */
    public boolean joinGame(UUID gameId, Player opponent) {
        CoinflipGame game = pendingGames.get(gameId);
        if (game == null) {
            return false;
        }

        // Can't join own game
        if (game.getCreatorId().equals(opponent.getUniqueId())) {
            return false;
        }

        // Check and remove currency from opponent
        if (!CurrencyManager.removeCurrency(opponent, game.getCurrency(), game.getAmount())) {
            return false;
        }

        // Move to active games
        pendingGames.remove(gameId);
        game.setOpponent(opponent);
        activeGames.put(gameId, game);

        return true;
    }

    /**
     * Complete a coinflip game and distribute winnings
     */
    public void completeGame(UUID gameId, UUID winnerId) {
        CoinflipGame game = activeGames.remove(gameId);
        if (game == null)
            return;

        Player winner = Bukkit.getPlayer(winnerId);
        if (winner != null && winner.isOnline()) {
            CurrencyManager.giveCurrency(winner, game.getCurrency(), game.getTotalPot());
        }

        // Broadcast result
        String message = plugin.getMessageRaw("coinflip-won")
                .replace("%winner%", game.getWinnerName())
                .replace("%amount%", String.valueOf(game.getTotalPot()))
                .replace("%currency%", game.getCurrency().getDisplayNameStripped());

        Bukkit.broadcastMessage(plugin.colorize("&6[Coinflip] &r") + message);
    }

    /**
     * Cancel a coinflip and return items
     */
    public boolean cancelGame(UUID gameId, Player player) {
        CoinflipGame game = pendingGames.get(gameId);
        if (game == null || !game.getCreatorId().equals(player.getUniqueId())) {
            return false;
        }

        pendingGames.remove(gameId);
        CurrencyManager.giveCurrency(player, game.getCurrency(), game.getAmount());
        return true;
    }

    /**
     * Cancel all coinflips (server shutdown)
     */
    public void cancelAllCoinflips() {
        for (CoinflipGame game : pendingGames.values()) {
            Player creator = Bukkit.getPlayer(game.getCreatorId());
            if (creator != null && creator.isOnline()) {
                CurrencyManager.giveCurrency(creator, game.getCurrency(), game.getAmount());
            }
        }
        pendingGames.clear();

        // Return items for in-progress games too
        for (CoinflipGame game : activeGames.values()) {
            Player creator = Bukkit.getPlayer(game.getCreatorId());
            Player opponent = Bukkit.getPlayer(game.getOpponentId());

            if (creator != null && creator.isOnline()) {
                CurrencyManager.giveCurrency(creator, game.getCurrency(), game.getAmount());
            }
            if (opponent != null && opponent.isOnline()) {
                CurrencyManager.giveCurrency(opponent, game.getCurrency(), game.getAmount());
            }
        }
        activeGames.clear();
    }

    /**
     * Get all pending games
     */
    public Collection<CoinflipGame> getPendingGames() {
        return pendingGames.values();
    }

    /**
     * Get a pending game by ID
     */
    public CoinflipGame getPendingGame(UUID gameId) {
        return pendingGames.get(gameId);
    }

    /**
     * Get an active game by ID
     */
    public CoinflipGame getActiveGame(UUID gameId) {
        return activeGames.get(gameId);
    }

    /**
     * Check if player has an active coinflip
     */
    public boolean hasActiveCoinflip(Player player) {
        for (CoinflipGame game : pendingGames.values()) {
            if (game.getCreatorId().equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get player's pending game
     */
    public CoinflipGame getPlayerPendingGame(Player player) {
        for (CoinflipGame game : pendingGames.values()) {
            if (game.getCreatorId().equals(player.getUniqueId())) {
                return game;
            }
        }
        return null;
    }
}
