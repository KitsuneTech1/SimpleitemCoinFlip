package com.coinflip.gui;

import com.coinflip.CoinflipPlugin;
import com.coinflip.game.CoinflipGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class ActiveCoinflipsGUI {

    private final CoinflipPlugin plugin;
    private static final Map<UUID, Map<Integer, UUID>> slotToGameMap = new HashMap<>();

    public ActiveCoinflipsGUI(CoinflipPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String title = plugin.colorize(plugin.getConfig().getString("gui.active-title", "&8&lActive Coinflips"));
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Fill with glass panes
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, filler);
        }

        Collection<CoinflipGame> games = plugin.getCoinflipManager().getPendingGames();
        Map<Integer, UUID> playerSlotMap = new HashMap<>();

        int slot = 10;
        for (CoinflipGame game : games) {
            if (slot > 43)
                break; // Max games displayed

            // Skip certain slots (borders)
            while (slot % 9 == 0 || slot % 9 == 8) {
                slot++;
            }

            // Create player head item
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(game.getCreatorId()));
            skullMeta.setDisplayName(plugin.colorize("&e&l" + game.getCreatorName() + "'s Coinflip"));

            List<String> lore = new ArrayList<>();
            lore.add(plugin
                    .colorize("&7Betting: &e" + game.getAmount() + " " + game.getCurrency().getDisplayNameStripped()));
            lore.add("");

            if (game.getCreatorId().equals(player.getUniqueId())) {
                lore.add(plugin.colorize("&cThis is your coinflip!"));
            } else {
                lore.add(plugin.colorize("&aClick to join this coinflip!"));
            }

            skullMeta.setLore(lore);
            head.setItemMeta(skullMeta);

            gui.setItem(slot, head);
            playerSlotMap.put(slot, game.getGameId());
            slot++;
        }

        slotToGameMap.put(player.getUniqueId(), playerSlotMap);

        // No games message
        if (games.isEmpty()) {
            ItemStack noGames = createItem(Material.BARRIER, "&c&lNo Active Coinflips",
                    "&7There are no coinflips to join right now.",
                    "&7Create your own!");
            gui.setItem(22, noGames);
        }

        // Back button
        ItemStack backButton = createItem(Material.ARROW, "&c&lBack",
                "&7Return to main menu");
        gui.setItem(49, backButton);

        player.openInventory(gui);
    }

    public static UUID getGameAtSlot(Player player, int slot) {
        Map<Integer, UUID> map = slotToGameMap.get(player.getUniqueId());
        return map != null ? map.get(slot) : null;
    }

    public static void clearMapping(Player player) {
        slotToGameMap.remove(player.getUniqueId());
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
