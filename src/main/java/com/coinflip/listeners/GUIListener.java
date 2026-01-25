package com.coinflip.listeners;

import com.coinflip.CoinflipPlugin;
import com.coinflip.currency.Currency;
import com.coinflip.currency.CurrencyManager;
import com.coinflip.game.CoinflipGame;
import com.coinflip.game.CoinflipManager;
import com.coinflip.game.ItemCoinflipGame;
import com.coinflip.gui.ActiveCoinflipsGUI;
import com.coinflip.gui.CoinflipAnimationGUI;
import com.coinflip.gui.CreateCoinflipGUI;
import com.coinflip.gui.ItemSelectGUI;
import com.coinflip.gui.MainMenuGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class GUIListener implements Listener {

    private final CoinflipPlugin plugin;
    private final MainMenuGUI mainMenuGUI;
    private final CreateCoinflipGUI createCoinflipGUI;
    private final ActiveCoinflipsGUI activeCoinflipsGUI;
    private final CoinflipAnimationGUI animationGUI;
    private final ItemSelectGUI itemSelectGUI;

    public GUIListener(CoinflipPlugin plugin) {
        this.plugin = plugin;
        this.mainMenuGUI = new MainMenuGUI(plugin);
        this.createCoinflipGUI = new CreateCoinflipGUI(plugin);
        this.activeCoinflipsGUI = new ActiveCoinflipsGUI(plugin);
        this.animationGUI = new CoinflipAnimationGUI(plugin);
        this.itemSelectGUI = new ItemSelectGUI(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        // Main Menu
        String mainMenuTitle = plugin
                .colorize(plugin.getConfig().getString("gui.main-menu-title", "&8&lCoinflip Menu"));
        if (title.equals(mainMenuTitle)) {
            event.setCancelled(true);
            handleMainMenu(player, event.getSlot(), clicked);
            return;
        }

        // Create Coinflip (currency selection)
        String createTitle = plugin.colorize(plugin.getConfig().getString("gui.create-title", "&8&lCreate Coinflip"));
        if (title.equals(createTitle)) {
            event.setCancelled(true);
            handleCurrencySelection(player, event.getSlot(), clicked);
            return;
        }

        // Amount selection
        if (title.equals(plugin.colorize("&8&lSelect Amount"))) {
            event.setCancelled(true);
            handleAmountSelection(player, event.getSlot(), clicked);
            return;
        }

        // Active Coinflips
        String activeTitle = plugin.colorize(plugin.getConfig().getString("gui.active-title", "&8&lActive Coinflips"));
        if (title.equals(activeTitle)) {
            event.setCancelled(true);
            handleActiveCoinflips(player, event.getSlot(), clicked);
            return;
        }

        // Animation GUI - prevent interaction
        String animationTitle = plugin.colorize(plugin.getConfig().getString("gui.animation-title", "§8§lCoinflip!"));
        if (title.equals(animationTitle)) {
            event.setCancelled(true);
            return;
        }

        // Item Selection GUI
        if (title.equals(plugin.colorize("§8§lSelect Item to Bet"))) {
            event.setCancelled(true);
            handleItemSelection(player, event.getSlot(), clicked);
            return;
        }

        // Item Amount Selection GUI (same title as currency amount but different
        // handler logic)
        // This is handled in handleAmountSelection which checks for ItemSelectGUI state
    }

    private void handleMainMenu(Player player, int slot, ItemStack clicked) {
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        if (slot == 11 && clicked.getType() == Material.GOLD_INGOT) {
            // Create Coinflip
            if (plugin.getCoinflipManager().hasActiveCoinflip(player)) {
                player.sendMessage(plugin.getMessage("already-have-coinflip"));
                player.closeInventory();
                return;
            }
            createCoinflipGUI.openCurrencySelection(player);
        } else if (slot == 15 && clicked.getType() == Material.BOOK) {
            // View Active Coinflips
            activeCoinflipsGUI.open(player);
        } else if (slot == 22 && clicked.getType() == Material.BARRIER) {
            // Cancel own coinflip
            CoinflipGame game = plugin.getCoinflipManager().getPlayerPendingGame(player);
            if (game != null) {
                plugin.getCoinflipManager().cancelGame(game.getGameId(), player);
                player.sendMessage(plugin.getMessage("coinflip-cancelled"));
            }
            player.closeInventory();
        }
    }

    private void handleCurrencySelection(Player player, int slot, ItemStack clicked) {
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        if (clicked.getType() == Material.ARROW) {
            mainMenuGUI.open(player);
            return;
        }

        // Handle custom item selection (CHEST)
        if (slot == 14 && clicked.getType() == Material.CHEST) {
            itemSelectGUI.openItemSelection(player);
            return;
        }

        Currency currency = null;
        if (slot == 10 && clicked.getType() == Material.DIAMOND) {
            currency = Currency.DIAMOND;
        } else if (slot == 12 && clicked.getType() == Material.IRON_INGOT) {
            currency = Currency.IRON_INGOT;
        } else if (slot == 16 && clicked.getType() == Material.EMERALD) {
            currency = Currency.EMERALD;
        }

        if (currency != null) {
            if (CurrencyManager.countCurrency(player, currency) < 1) {
                player.sendMessage(plugin.getMessage("not-enough-items")
                        .replace("%currency%", currency.getDisplayNameStripped()));
                return;
            }
            createCoinflipGUI.openAmountSelection(player, currency);
        }
    }

    private void handleAmountSelection(Player player, int slot, ItemStack clicked) {
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        // Check if this is item-based or currency-based
        ItemStack selectedItem = ItemSelectGUI.getSelectedItem(player);
        Currency currency = CreateCoinflipGUI.getSelectedCurrency(player);

        if (clicked.getType() == Material.ARROW) {
            if (selectedItem != null) {
                // Go back to item selection
                ItemSelectGUI.clearSelection(player);
                itemSelectGUI.openItemSelection(player);
            } else {
                CreateCoinflipGUI.clearSelection(player);
                createCoinflipGUI.openCurrencySelection(player);
            }
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            return; // Can't afford
        }

        // Handle item-based coinflip
        if (selectedItem != null) {
            handleItemAmountSelection(player, slot, clicked, selectedItem);
            return;
        }

        // Handle currency-based coinflip
        if (currency == null) {
            createCoinflipGUI.openCurrencySelection(player);
            return;
        }

        // Verify clicked item is the currency material
        if (clicked.getType() != currency.getMaterial()) {
            return;
        }

        // Get amount from item
        int amount = clicked.getAmount();

        // Parse amount from display name if needed
        String displayName = clicked.getItemMeta().getDisplayName();
        if (displayName != null) {
            try {
                String amountStr = displayName.replaceAll("[^0-9]", "");
                if (!amountStr.isEmpty()) {
                    amount = Integer.parseInt(amountStr);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // Verify player has enough
        if (!CurrencyManager.hasEnough(player, currency, amount)) {
            player.sendMessage(plugin.getMessage("not-enough-items")
                    .replace("%currency%", currency.getDisplayNameStripped()));
            return;
        }

        // Create the coinflip
        CoinflipGame game = plugin.getCoinflipManager().createGame(player, currency, amount);
        if (game != null) {
            player.sendMessage(plugin.getMessage("coinflip-created")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%currency%", currency.getDisplayNameStripped()));
            CreateCoinflipGUI.clearSelection(player);
            player.closeInventory();
        } else {
            player.sendMessage(plugin.colorize("&cFailed to create coinflip!"));
        }
    }

    private void handleItemAmountSelection(Player player, int slot, ItemStack clicked, ItemStack selectedItem) {
        // Parse amount from display name
        int amount = 1;
        String displayName = clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : null;

        if (displayName != null) {
            // Check for "Bet All"
            if (displayName.contains("Bet All")) {
                // Get max amount from player
                amount = countItems(player, selectedItem);
            } else {
                try {
                    String amountStr = displayName.replaceAll("[^0-9]", "");
                    if (!amountStr.isEmpty()) {
                        amount = Integer.parseInt(amountStr);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Verify player has enough
        if (!plugin.getCoinflipManager().hasEnoughItems(player, selectedItem, amount)) {
            player.sendMessage(plugin.colorize("&cYou don't have enough of this item!"));
            return;
        }

        // Create the item-based coinflip
        ItemCoinflipGame game = plugin.getCoinflipManager().createItemGame(player, selectedItem, amount);
        if (game != null) {
            String itemName = game.getItemDisplayName();
            player.sendMessage(plugin.colorize("&aCoinflip created! &7Betting &e" + amount + "x " + itemName));
            ItemSelectGUI.clearSelection(player);
            player.closeInventory();
        } else {
            player.sendMessage(plugin.colorize("&cFailed to create coinflip! You may already have one active."));
        }
    }

    private void handleItemSelection(Player player, int slot, ItemStack clicked) {
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        if (clicked.getType() == Material.ARROW) {
            createCoinflipGUI.openCurrencySelection(player);
            return;
        }

        if (clicked.getType() == Material.BOOK) {
            return; // Info item
        }

        // Find the actual item in the player's inventory that matches this
        ItemStack[] contents = player.getInventory().getContents();
        for (int invSlot = 0; invSlot < 36; invSlot++) {
            ItemStack invItem = contents[invSlot];
            if (invItem != null && invItem.getType() == clicked.getType()) {
                // Found a matching item - open amount selection
                itemSelectGUI.openAmountSelection(player, invItem, invSlot);
                return;
            }
        }

        player.sendMessage(plugin.colorize("&cCouldn't find that item in your inventory!"));
    }

    private int countItems(Player player, ItemStack item) {
        int count = 0;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && invItem.isSimilar(item)) {
                count += invItem.getAmount();
            }
        }
        return count;
    }

    private void handleActiveCoinflips(Player player, int slot, ItemStack clicked) {
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        if (clicked.getType() == Material.ARROW) {
            mainMenuGUI.open(player);
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            return; // No games message
        }

        // Handle currency-based coinflips (player heads)
        if (clicked.getType() == Material.PLAYER_HEAD) {
            UUID gameId = ActiveCoinflipsGUI.getGameAtSlot(player, slot);
            if (gameId == null) {
                player.sendMessage(plugin.colorize("&cCould not find this coinflip. Refreshing..."));
                activeCoinflipsGUI.open(player);
                return;
            }

            CoinflipGame game = plugin.getCoinflipManager().getPendingGame(gameId);
            if (game == null) {
                player.sendMessage(plugin.colorize("&cThis coinflip is no longer available!"));
                activeCoinflipsGUI.open(player);
                return;
            }

            // Can't join own game
            if (game.getCreatorId().equals(player.getUniqueId())) {
                player.sendMessage(plugin.getMessage("cannot-join-own"));
                return;
            }

            // Check if player has enough (pre-check for better UX)
            if (!CurrencyManager.hasEnough(player, game.getCurrency(), game.getAmount())) {
                player.sendMessage(plugin.getMessage("not-enough-items")
                        .replace("%currency%", game.getCurrency().getDisplayNameStripped()));
                return;
            }

            // Join the game
            CoinflipManager.JoinResult result = plugin.getCoinflipManager().joinGame(gameId, player);

            switch (result) {
                case SUCCESS:
                    Player creator = Bukkit.getPlayer(game.getCreatorId());
                    if (creator != null && creator.isOnline()) {
                        creator.sendMessage(plugin.getMessage("coinflip-joined")
                                .replace("%player%", player.getName()));

                        // Start the animation
                        ActiveCoinflipsGUI.clearMapping(player);
                        animationGUI.startAnimation(creator, player, game);
                    } else {
                        // Creator went offline - refund the player
                        player.sendMessage(
                                plugin.colorize("&cThe coinflip creator went offline! You have been refunded."));
                        CurrencyManager.giveCurrency(player, game.getCurrency(), game.getAmount());
                    }
                    break;

                case GAME_NOT_FOUND:
                    player.sendMessage(plugin.colorize("&cThis coinflip is no longer available!"));
                    activeCoinflipsGUI.open(player);
                    break;

                case CANNOT_JOIN_OWN:
                    player.sendMessage(plugin.getMessage("cannot-join-own"));
                    break;

                case NOT_ENOUGH_CURRENCY:
                    player.sendMessage(plugin.getMessage("not-enough-items")
                            .replace("%currency%", game.getCurrency().getDisplayNameStripped()));
                    break;
            }
            return;
        }

        // Handle item-based coinflips (non-head items)
        UUID itemGameId = ActiveCoinflipsGUI.getItemGameAtSlot(player, slot);
        if (itemGameId != null) {
            ItemCoinflipGame itemGame = plugin.getCoinflipManager().getPendingItemGame(itemGameId);
            if (itemGame == null) {
                player.sendMessage(plugin.colorize("&cThis coinflip is no longer available!"));
                activeCoinflipsGUI.open(player);
                return;
            }

            // Can't join own game
            if (itemGame.getCreatorId().equals(player.getUniqueId())) {
                player.sendMessage(plugin.getMessage("cannot-join-own"));
                return;
            }

            // Check if player has enough matching items
            if (!plugin.getCoinflipManager().hasEnoughItems(player, itemGame.getBetItem(), itemGame.getAmount())) {
                player.sendMessage(plugin.colorize("&cYou don't have enough " + itemGame.getItemDisplayName() + "!"));
                return;
            }

            // Join the item game
            CoinflipManager.JoinResult result = plugin.getCoinflipManager().joinItemGame(itemGameId, player);

            switch (result) {
                case SUCCESS:
                    Player creator = Bukkit.getPlayer(itemGame.getCreatorId());
                    if (creator != null && creator.isOnline()) {
                        creator.sendMessage(plugin.colorize("&a" + player.getName() + " has joined your coinflip!"));

                        // Start the animation for item coinflip
                        ActiveCoinflipsGUI.clearMapping(player);
                        animationGUI.startItemAnimation(creator, player, itemGame);
                    } else {
                        // Creator went offline - refund the player
                        player.sendMessage(
                                plugin.colorize("&cThe coinflip creator went offline! You have been refunded."));
                        // Items already returned by joinItemGame failure handling
                    }
                    break;

                case GAME_NOT_FOUND:
                    player.sendMessage(plugin.colorize("&cThis coinflip is no longer available!"));
                    activeCoinflipsGUI.open(player);
                    break;

                case CANNOT_JOIN_OWN:
                    player.sendMessage(plugin.getMessage("cannot-join-own"));
                    break;

                case NOT_ENOUGH_CURRENCY:
                    player.sendMessage(
                            plugin.colorize("&cYou don't have enough " + itemGame.getItemDisplayName() + "!"));
                    break;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player))
            return;

        String title = event.getView().getTitle();

        // Only clear selection when closing main menu or active coinflips
        // Don't clear when navigating between create coinflip GUIs
        String mainMenuTitle = plugin
                .colorize(plugin.getConfig().getString("gui.main-menu-title", "§8§lCoinflip Menu"));
        String activeTitle = plugin.colorize(plugin.getConfig().getString("gui.active-title", "§8§lActive Coinflips"));

        if (title.equals(mainMenuTitle) || title.equals(activeTitle)) {
            CreateCoinflipGUI.clearSelection(player);
        }

        // Only clear the slot-to-game mapping when actually closing the Active
        // Coinflips GUI
        // NOT when opening a new GUI (like the animation)
        if (title.equals(activeTitle)) {
            // Use a delayed task to allow the click handler to complete first
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Only clear if player is no longer in any coinflip-related GUI
                if (player.getOpenInventory() == null ||
                        player.getOpenInventory().getTitle() == null ||
                        !player.getOpenInventory().getTitle().contains("Coinflip")) {
                    ActiveCoinflipsGUI.clearMapping(player);
                }
            }, 5L); // 5 ticks delay
        }
    }
}
