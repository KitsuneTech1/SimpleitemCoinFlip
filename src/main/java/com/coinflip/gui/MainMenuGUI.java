package com.coinflip.gui;

import com.coinflip.CoinflipPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MainMenuGUI {

    private final CoinflipPlugin plugin;

    public MainMenuGUI(CoinflipPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String title = plugin.colorize(plugin.getConfig().getString("gui.main-menu-title", "&8&lCoinflip Menu"));
        Inventory gui = Bukkit.createInventory(null, 27, title);

        // Fill with glass panes
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, filler);
        }

        // Create Coinflip button
        ItemStack createButton = createItem(Material.GOLD_INGOT, "&a&lCreate Coinflip",
                "&7Click to create a new coinflip",
                "&7and wager your items!");
        gui.setItem(11, createButton);

        // View Active Coinflips button
        int activeCount = plugin.getCoinflipManager().getPendingGames().size();
        ItemStack viewButton = createItem(Material.BOOK, "&e&lActive Coinflips",
                "&7Click to view all active coinflips",
                "&7and join one!",
                "",
                "&7Active games: &e" + activeCount);
        gui.setItem(15, viewButton);

        // Cancel own coinflip button (if they have one)
        if (plugin.getCoinflipManager().hasActiveCoinflip(player)) {
            ItemStack cancelButton = createItem(Material.BARRIER, "&c&lCancel Your Coinflip",
                    "&7Click to cancel your coinflip",
                    "&7and get your items back!");
            gui.setItem(22, cancelButton);
        }

        player.openInventory(gui);
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
