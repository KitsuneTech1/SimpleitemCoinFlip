package com.coinflip.gui;

import com.coinflip.CoinflipPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI that allows players to select an item from their inventory to bet in a
 * coinflip
 */
public class ItemSelectGUI {

    private final CoinflipPlugin plugin;

    // Track selected items per player
    private static final Map<UUID, ItemStack> selectedItem = new HashMap<>();
    private static final Map<UUID, Integer> selectedSlot = new HashMap<>(); // Original inventory slot

    public ItemSelectGUI(CoinflipPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the item selection GUI showing the player's inventory items
     */
    public void openItemSelection(Player player) {
        String title = plugin.colorize("§8§lSelect Item to Bet");
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Fill with glass panes
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, filler);
        }

        // Get player's inventory items (excluding armor and offhand)
        ItemStack[] contents = player.getInventory().getContents();
        int guiSlot = 10;

        for (int invSlot = 0; invSlot < 36; invSlot++) {
            ItemStack item = contents[invSlot];

            // Skip empty slots and certain items
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            // Skip certain GUI slots (borders)
            while (guiSlot < 44 && (guiSlot % 9 == 0 || guiSlot % 9 == 8)) {
                guiSlot++;
            }

            if (guiSlot >= 44)
                break;

            // Clone and add lore indicating it can be bet
            ItemStack displayItem = item.clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                lore.add("");
                lore.add(plugin.colorize("§aClick to bet this item!"));
                lore.add(plugin.colorize("§7Amount: §e" + item.getAmount()));
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }

            gui.setItem(guiSlot, displayItem);
            guiSlot++;
        }

        // Info item
        ItemStack infoItem = createItem(Material.BOOK, "§e§lSelect an Item",
                "§7Click on any item from your",
                "§7inventory to bet it in a coinflip!",
                "",
                "§7The winner takes all!");
        gui.setItem(4, infoItem);

        // Back button
        ItemStack backButton = createItem(Material.ARROW, "§c§lBack",
                "§7Return to create menu");
        gui.setItem(49, backButton);

        player.openInventory(gui);
    }

    /**
     * Open amount selection for the selected item
     */
    public void openAmountSelection(Player player, ItemStack selectedItemStack, int originalSlot) {
        selectedItem.put(player.getUniqueId(), selectedItemStack.clone());
        selectedSlot.put(player.getUniqueId(), originalSlot);

        int maxAmount = selectedItemStack.getAmount();
        String title = plugin.colorize("§8§lSelect Amount");
        Inventory gui = Bukkit.createInventory(null, 45, title);

        // Fill with glass panes
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) {
            gui.setItem(i, filler);
        }

        // Show the selected item
        ItemStack displayItem = selectedItemStack.clone();
        ItemMeta meta = displayItem.getItemMeta();
        if (meta != null) {
            String itemName = meta.hasDisplayName() ? meta.getDisplayName()
                    : formatMaterialName(selectedItemStack.getType());
            meta.setDisplayName(plugin.colorize("§e§lBetting: " + itemName));
            List<String> lore = new ArrayList<>();
            lore.add(plugin.colorize("§7Select amount below"));
            lore.add("");
            lore.add(plugin.colorize("§7You have: §e" + maxAmount));
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }
        gui.setItem(4, displayItem);

        // Amount options
        int[] amounts = { 1, 5, 10, 16, 32, 64 };
        int[] slots = { 10, 11, 12, 14, 15, 16 };

        for (int i = 0; i < amounts.length; i++) {
            int amount = amounts[i];
            boolean canAfford = amount <= maxAmount;

            ItemStack amountItem;
            if (canAfford) {
                amountItem = selectedItemStack.clone();
                ItemMeta amountMeta = amountItem.getItemMeta();
                if (amountMeta != null) {
                    amountMeta.setDisplayName(plugin.colorize("§a§l" + amount + "x"));
                    List<String> lore = new ArrayList<>();
                    lore.add(plugin.colorize("§7Click to bet " + amount));
                    lore.add("");
                    lore.add(plugin.colorize("§aYou can afford this!"));
                    amountMeta.setLore(lore);
                    amountItem.setItemMeta(amountMeta);
                }
                amountItem.setAmount(Math.min(amount, 64));
            } else {
                amountItem = createItem(Material.BARRIER, "§c§l" + amount + "x",
                        "§7Click to bet " + amount,
                        "",
                        "§cYou don't have enough!");
            }
            gui.setItem(slots[i], amountItem);
        }

        // Custom amount option
        ItemStack customItem = createItem(Material.NAME_TAG, "§d§lCustom Amount",
                "§7Bet a specific amount",
                "",
                "§7Type in chat after clicking!");
        gui.setItem(22, customItem);

        // Bet all option
        if (maxAmount > 0) {
            ItemStack allItem = selectedItemStack.clone();
            ItemMeta allMeta = allItem.getItemMeta();
            if (allMeta != null) {
                allMeta.setDisplayName(plugin.colorize("§6§lBet All (" + maxAmount + ")"));
                List<String> lore = new ArrayList<>();
                lore.add(plugin.colorize("§7Bet everything you have!"));
                lore.add("");
                lore.add(plugin.colorize("§6High risk, high reward!"));
                allMeta.setLore(lore);
                allItem.setItemMeta(allMeta);
            }
            allItem.setAmount(Math.min(maxAmount, 64));
            gui.setItem(31, allItem);
        }

        // Back button
        ItemStack backButton = createItem(Material.ARROW, "§c§lBack",
                "§7Return to item selection");
        gui.setItem(40, backButton);

        player.openInventory(gui);
    }

    public static ItemStack getSelectedItem(Player player) {
        return selectedItem.get(player.getUniqueId());
    }

    public static Integer getSelectedSlot(Player player) {
        return selectedSlot.get(player.getUniqueId());
    }

    public static void clearSelection(Player player) {
        selectedItem.remove(player.getUniqueId());
        selectedSlot.remove(player.getUniqueId());
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
