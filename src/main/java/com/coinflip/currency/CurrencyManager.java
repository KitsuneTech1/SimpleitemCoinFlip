package com.coinflip.currency;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CurrencyManager {

    /**
     * Check if a player has enough of a currency
     */
    public static boolean hasEnough(Player player, Currency currency, int amount) {
        return countCurrency(player, currency) >= amount;
    }

    /**
     * Count how many of a currency a player has
     */
    public static int countCurrency(Player player, Currency currency) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == currency.getMaterial()) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Remove currency items from a player's inventory
     */
    public static boolean removeCurrency(Player player, Currency currency, int amount) {
        if (!hasEnough(player, currency, amount)) {
            return false;
        }

        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == currency.getMaterial()) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
            }
        }

        player.updateInventory();
        return remaining == 0;
    }

    /**
     * Give currency items to a player
     */
    public static void giveCurrency(Player player, Currency currency, int amount) {
        int remaining = amount;

        while (remaining > 0) {
            int stackSize = Math.min(remaining, 64);
            ItemStack item = new ItemStack(currency.getMaterial(), stackSize);

            // Try to add to inventory, drop on ground if full
            java.util.HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }

            remaining -= stackSize;
        }

        player.updateInventory();
    }
}
