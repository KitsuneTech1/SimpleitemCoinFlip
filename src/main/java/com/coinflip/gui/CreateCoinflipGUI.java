package com.coinflip.gui;

import com.coinflip.CoinflipPlugin;
import com.coinflip.currency.Currency;
import com.coinflip.currency.CurrencyManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateCoinflipGUI {

    private final CoinflipPlugin plugin;
    private static final Map<UUID, Currency> selectedCurrency = new HashMap<>();
    private static final Map<UUID, Integer> selectedAmount = new HashMap<>();

    public CreateCoinflipGUI(CoinflipPlugin plugin) {
        this.plugin = plugin;
    }

    public void openCurrencySelection(Player player) {
        String title = plugin.colorize(plugin.getConfig().getString("gui.create-title", "§8§lCreate Coinflip"));
        Inventory gui = Bukkit.createInventory(null, 36, title);

        // Fill with glass panes
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 36; i++) {
            gui.setItem(i, filler);
        }

        // Diamond option
        int diamondCount = CurrencyManager.countCurrency(player, Currency.DIAMOND);
        ItemStack diamondItem = createItem(Material.DIAMOND, "&b&lDiamonds",
                "&7Click to bet with diamonds",
                "",
                "&7You have: &b" + diamondCount);
        gui.setItem(10, diamondItem);

        // Iron option
        int ironCount = CurrencyManager.countCurrency(player, Currency.IRON_INGOT);
        ItemStack ironItem = createItem(Material.IRON_INGOT, "&7&lIron Ingots",
                "&7Click to bet with iron ingots",
                "",
                "&7You have: &f" + ironCount);
        gui.setItem(12, ironItem);

        // Custom Item option (NEW!)
        ItemStack customItem = createItem(Material.CHEST, "&d&lCustom Item",
                "&7Click to bet any item from",
                "&7your inventory!",
                "",
                "&dBet swords, armor, tools & more!");
        gui.setItem(14, customItem);

        // Emerald option
        int emeraldCount = CurrencyManager.countCurrency(player, Currency.EMERALD);
        ItemStack emeraldItem = createItem(Material.EMERALD, "&a&lEmeralds",
                "&7Click to bet with emeralds",
                "",
                "&7You have: &a" + emeraldCount);
        gui.setItem(16, emeraldItem);

        // Back button
        ItemStack backButton = createItem(Material.ARROW, "&c&lBack",
                "&7Return to main menu");
        gui.setItem(31, backButton);

        player.openInventory(gui);
    }

    public void openAmountSelection(Player player, Currency currency) {
        selectedCurrency.put(player.getUniqueId(), currency);

        String title = plugin.colorize("&8&lSelect Amount");
        Inventory gui = Bukkit.createInventory(null, 45, title);

        // Fill with glass panes
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) {
            gui.setItem(i, filler);
        }

        int playerHas = CurrencyManager.countCurrency(player, currency);
        int minBet = plugin.getConfig().getInt("limits.min-bet", 1);
        int maxBet = Math.min(plugin.getConfig().getInt("limits.max-bet", 64), playerHas);

        // Amount options
        int[] amounts = { 1, 5, 10, 16, 32, 64 };
        int[] slots = { 10, 11, 12, 14, 15, 16 };

        for (int i = 0; i < amounts.length; i++) {
            int amount = amounts[i];
            boolean canAfford = amount <= playerHas && amount >= minBet && amount <= maxBet;

            ItemStack amountItem;
            if (canAfford) {
                amountItem = createItem(currency.getMaterial(),
                        "&a&l" + amount + " " + currency.getDisplayNameStripped(),
                        "&7Click to bet " + amount,
                        "",
                        "&aYou can afford this!");
                amountItem.setAmount(Math.min(amount, 64));
            } else {
                amountItem = createItem(Material.BARRIER, "&c&l" + amount + " " + currency.getDisplayNameStripped(),
                        "&7Click to bet " + amount,
                        "",
                        "&cYou cannot afford this!");
            }
            gui.setItem(slots[i], amountItem);
        }

        // Current selection info
        ItemStack infoItem = createItem(currency.getMaterial(), "&e&lBetting: " + currency.getDisplayName(),
                "&7Select an amount below",
                "",
                "&7Your balance: &e" + playerHas);
        gui.setItem(4, infoItem);

        // Back button
        ItemStack backButton = createItem(Material.ARROW, "&c&lBack",
                "&7Return to currency selection");
        gui.setItem(40, backButton);

        player.openInventory(gui);
    }

    public static Currency getSelectedCurrency(Player player) {
        return selectedCurrency.get(player.getUniqueId());
    }

    public static void clearSelection(Player player) {
        selectedCurrency.remove(player.getUniqueId());
        selectedAmount.remove(player.getUniqueId());
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.colorize(name));
        if (lore.length > 0) {
            meta.setLore(Arrays.stream(lore).map(plugin::colorize).toList());
        }
        item.setItemMeta(meta);
        return item;
    }
}
