package com.coinflip.game;

import com.coinflip.CoinflipPlugin;
import com.coinflip.currency.Currency;
import com.coinflip.currency.CurrencyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CoinflipManager {

    private final CoinflipPlugin plugin;
    private final Map<UUID, CoinflipGame> pendingGames;
    private final Map<UUID, CoinflipGame> activeGames;

    // Item-based coinflip games
    private final Map<UUID, ItemCoinflipGame> pendingItemGames;
    private final Map<UUID, ItemCoinflipGame> activeItemGames;

    public CoinflipManager(CoinflipPlugin plugin) {
        this.plugin = plugin;
        this.pendingGames = new ConcurrentHashMap<>();
        this.activeGames = new ConcurrentHashMap<>();
        this.pendingItemGames = new ConcurrentHashMap<>();
        this.activeItemGames = new ConcurrentHashMap<>();
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
     * Result of attempting to join a coinflip game
     */
    public enum JoinResult {
        SUCCESS,
        GAME_NOT_FOUND,
        CANNOT_JOIN_OWN,
        NOT_ENOUGH_CURRENCY
    }

    /**
     * Join an existing coinflip game
     */
    public JoinResult joinGame(UUID gameId, Player opponent) {
        CoinflipGame game = pendingGames.get(gameId);
        if (game == null) {
            return JoinResult.GAME_NOT_FOUND;
        }

        // Can't join own game
        if (game.getCreatorId().equals(opponent.getUniqueId())) {
            return JoinResult.CANNOT_JOIN_OWN;
        }

        // Check and remove currency from opponent
        if (!CurrencyManager.removeCurrency(opponent, game.getCurrency(), game.getAmount())) {
            return JoinResult.NOT_ENOUGH_CURRENCY;
        }

        // Move to active games
        pendingGames.remove(gameId);
        game.setOpponent(opponent);
        activeGames.put(gameId, game);

        return JoinResult.SUCCESS;
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

    // ============================================
    // Item-based Coinflip Methods
    // ============================================

    /**
     * Create a new item-based coinflip game
     */
    public ItemCoinflipGame createItemGame(Player creator, ItemStack item, int amount) {
        // Check if player already has a pending game (either type)
        if (hasActiveCoinflip(creator) || hasActiveItemCoinflip(creator)) {
            return null;
        }

        // Check and remove items from player
        if (!removeItems(creator, item, amount)) {
            return null;
        }

        ItemCoinflipGame game = new ItemCoinflipGame(creator, item, amount);
        pendingItemGames.put(game.getGameId(), game);

        return game;
    }

    /**
     * Join an existing item-based coinflip game
     */
    public JoinResult joinItemGame(UUID gameId, Player opponent) {
        ItemCoinflipGame game = pendingItemGames.get(gameId);
        if (game == null) {
            return JoinResult.GAME_NOT_FOUND;
        }

        // Can't join own game
        if (game.getCreatorId().equals(opponent.getUniqueId())) {
            return JoinResult.CANNOT_JOIN_OWN;
        }

        // Check and remove matching items from opponent
        if (!removeItems(opponent, game.getBetItem(), game.getAmount())) {
            return JoinResult.NOT_ENOUGH_CURRENCY;
        }

        // Move to active games
        pendingItemGames.remove(gameId);
        game.setOpponent(opponent);
        activeItemGames.put(gameId, game);

        return JoinResult.SUCCESS;
    }

    /**
     * Complete an item-based coinflip game and distribute winnings
     */
    public void completeItemGame(UUID gameId, UUID winnerId) {
        ItemCoinflipGame game = activeItemGames.remove(gameId);
        if (game == null)
            return;

        Player winner = Bukkit.getPlayer(winnerId);
        if (winner != null && winner.isOnline()) {
            giveItems(winner, game.getBetItem(), game.getTotalPot());
        }

        // Broadcast result
        String message = plugin.getMessageRaw("coinflip-won")
                .replace("%winner%", game.getWinnerName())
                .replace("%amount%", String.valueOf(game.getTotalPot()))
                .replace("%currency%", game.getItemDisplayName());

        Bukkit.broadcastMessage(plugin.colorize("§6[Coinflip] §r") + message);
    }

    /**
     * Cancel an item-based coinflip and return items
     */
    public boolean cancelItemGame(UUID gameId, Player player) {
        ItemCoinflipGame game = pendingItemGames.get(gameId);
        if (game == null || !game.getCreatorId().equals(player.getUniqueId())) {
            return false;
        }

        pendingItemGames.remove(gameId);
        giveItems(player, game.getBetItem(), game.getAmount());
        return true;
    }

    /**
     * Get all pending item-based games
     */
    public Collection<ItemCoinflipGame> getPendingItemGames() {
        return pendingItemGames.values();
    }

    /**
     * Get a pending item-based game by ID
     */
    public ItemCoinflipGame getPendingItemGame(UUID gameId) {
        return pendingItemGames.get(gameId);
    }

    /**
     * Get an active item-based game by ID
     */
    public ItemCoinflipGame getActiveItemGame(UUID gameId) {
        return activeItemGames.get(gameId);
    }

    /**
     * Check if player has an active item-based coinflip
     */
    public boolean hasActiveItemCoinflip(Player player) {
        for (ItemCoinflipGame game : pendingItemGames.values()) {
            if (game.getCreatorId().equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get player's pending item-based game
     */
    public ItemCoinflipGame getPlayerPendingItemGame(Player player) {
        for (ItemCoinflipGame game : pendingItemGames.values()) {
            if (game.getCreatorId().equals(player.getUniqueId())) {
                return game;
            }
        }
        return null;
    }

    /**
     * Cancel all item-based coinflips (server shutdown)
     */
    public void cancelAllItemCoinflips() {
        for (ItemCoinflipGame game : pendingItemGames.values()) {
            Player creator = Bukkit.getPlayer(game.getCreatorId());
            if (creator != null && creator.isOnline()) {
                giveItems(creator, game.getBetItem(), game.getAmount());
            }
        }
        pendingItemGames.clear();

        // Return items for in-progress games too
        for (ItemCoinflipGame game : activeItemGames.values()) {
            Player creator = Bukkit.getPlayer(game.getCreatorId());
            Player opponent = Bukkit.getPlayer(game.getOpponentId());

            if (creator != null && creator.isOnline()) {
                giveItems(creator, game.getBetItem(), game.getAmount());
            }
            if (opponent != null && opponent.isOnline()) {
                giveItems(opponent, game.getBetItem(), game.getAmount());
            }
        }
        activeItemGames.clear();
    }

    // ============================================
    // Item Helper Methods
    // ============================================

    /**
     * Check if player has enough of a specific item
     */
    public boolean hasEnoughItems(Player player, ItemStack item, int amount) {
        int count = 0;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && invItem.isSimilar(item)) {
                count += invItem.getAmount();
            }
        }
        return count >= amount;
    }

    /**
     * Remove items from player's inventory
     */
    private boolean removeItems(Player player, ItemStack item, int amount) {
        if (!hasEnoughItems(player, item, amount)) {
            return false;
        }

        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack invItem = contents[i];
            if (invItem != null && invItem.isSimilar(item)) {
                int take = Math.min(remaining, invItem.getAmount());
                invItem.setAmount(invItem.getAmount() - take);
                remaining -= take;

                if (invItem.getAmount() <= 0) {
                    contents[i] = null;
                }
            }
        }

        player.getInventory().setContents(contents);
        return true;
    }

    /**
     * Give items to player's inventory
     */
    private void giveItems(Player player, ItemStack item, int amount) {
        ItemStack toGive = item.clone();
        int remaining = amount;

        while (remaining > 0) {
            int give = Math.min(remaining, toGive.getMaxStackSize());
            ItemStack stack = toGive.clone();
            stack.setAmount(give);

            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(stack);
            if (!overflow.isEmpty()) {
                // Drop items on ground if inventory is full
                for (ItemStack dropped : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), dropped);
                }
            }
            remaining -= give;
        }
    }
}
