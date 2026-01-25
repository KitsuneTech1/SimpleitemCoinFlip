package com.coinflip.gui;

import com.coinflip.CoinflipPlugin;
import com.coinflip.game.CoinflipGame;
import com.coinflip.game.ItemCoinflipGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CoinflipAnimationGUI {

    private final CoinflipPlugin plugin;

    public CoinflipAnimationGUI(CoinflipPlugin plugin) {
        this.plugin = plugin;
    }

    public void startAnimation(Player player1, Player player2, CoinflipGame game) {
        String title = plugin.colorize(plugin.getConfig().getString("gui.animation-title", "§8§lCoinflip!"));

        // Create inventory for both players
        Inventory gui1 = Bukkit.createInventory(null, 27, title);
        Inventory gui2 = Bukkit.createInventory(null, 27, title);

        // Fill with glass panes
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui1.setItem(i, filler);
            gui2.setItem(i, filler);
        }

        // Create player heads
        ItemStack head1 = createPlayerHead(game.getCreatorId(), game.getCreatorName());
        ItemStack head2 = createPlayerHead(game.getOpponentId(), game.getOpponentName());

        // VS text
        ItemStack vsItem = createItem(Material.BLAZE_POWDER, "&c&lVS");
        gui1.setItem(13, vsItem);
        gui2.setItem(13, vsItem);

        // Initial positions
        gui1.setItem(11, head1);
        gui1.setItem(15, head2);
        gui2.setItem(11, head1);
        gui2.setItem(15, head2);

        // Betting info
        ItemStack betInfo = createItem(game.getCurrency().getMaterial(),
                "&e&lPot: " + game.getTotalPot() + " " + game.getCurrency().getDisplayNameStripped());
        gui1.setItem(4, betInfo);
        gui2.setItem(4, betInfo);

        player1.openInventory(gui1);
        player2.openInventory(gui2);

        // Perform the flip and determine winner
        UUID winnerId = game.performFlip();
        boolean player1Wins = winnerId.equals(game.getCreatorId());

        // Start animation
        new BukkitRunnable() {
            int ticks = 0;
            int currentDelay = 2;
            int ticksSinceSwitch = 0;
            boolean showingPlayer1 = true;
            final int totalDuration = plugin.getConfig().getInt("animation.duration", 60);
            final int startSpeed = plugin.getConfig().getInt("animation.start-speed", 2);
            final int endSpeed = plugin.getConfig().getInt("animation.end-speed", 10);

            @Override
            public void run() {
                ticks++;
                ticksSinceSwitch++;

                // Calculate current speed (slows down over time)
                float progress = (float) ticks / totalDuration;
                currentDelay = (int) (startSpeed + (endSpeed - startSpeed) * progress);

                if (ticksSinceSwitch >= currentDelay) {
                    ticksSinceSwitch = 0;
                    showingPlayer1 = !showingPlayer1;

                    // Update the center slot
                    ItemStack currentHead = showingPlayer1 ? head1 : head2;
                    gui1.setItem(13, currentHead);
                    gui2.setItem(13, currentHead);

                    // Play sound
                    if (player1.isOnline()) {
                        player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1f);
                    }
                    if (player2.isOnline()) {
                        player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1f);
                    }
                }

                // Animation complete
                if (ticks >= totalDuration) {
                    this.cancel();

                    // Show winner
                    ItemStack winnerHead = player1Wins ? head1 : head2;
                    String winnerName = player1Wins ? game.getCreatorName() : game.getOpponentName();

                    // Update to winner
                    gui1.setItem(13, winnerHead);
                    gui2.setItem(13, winnerHead);

                    // Add winner indicator
                    ItemStack winnerItem = createItem(Material.GOLD_BLOCK,
                            "&6&l" + winnerName + " WINS!",
                            "&e+" + game.getTotalPot() + " " + game.getCurrency().getDisplayNameStripped());
                    gui1.setItem(22, winnerItem);
                    gui2.setItem(22, winnerItem);

                    // Play win sound
                    if (player1.isOnline()) {
                        player1.playSound(player1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    }
                    if (player2.isOnline()) {
                        player2.playSound(player2.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    }

                    // Complete the game after a short delay
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            plugin.getCoinflipManager().completeGame(game.getGameId(), winnerId);

                            if (player1.isOnline())
                                player1.closeInventory();
                            if (player2.isOnline())
                                player2.closeInventory();
                        }
                    }.runTaskLater(plugin, 40L);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Start animation for item-based coinflips
     */
    public void startItemAnimation(Player player1, Player player2, ItemCoinflipGame game) {
        String title = plugin.colorize(plugin.getConfig().getString("gui.animation-title", "§8§lCoinflip!"));

        // Create inventory for both players
        Inventory gui1 = Bukkit.createInventory(null, 27, title);
        Inventory gui2 = Bukkit.createInventory(null, 27, title);

        // Fill with glass panes
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui1.setItem(i, filler);
            gui2.setItem(i, filler);
        }

        // Create player heads for display
        ItemStack head1 = createPlayerHead(game.getCreatorId(), game.getCreatorName());
        ItemStack head2 = createPlayerHead(game.getOpponentId(), game.getOpponentName());

        // VS text
        ItemStack vsItem = createItem(Material.BLAZE_POWDER, "&c&lVS");
        gui1.setItem(13, vsItem);
        gui2.setItem(13, vsItem);

        // Initial positions
        gui1.setItem(11, head1);
        gui1.setItem(15, head2);
        gui2.setItem(11, head1);
        gui2.setItem(15, head2);

        // Betting info - show the item being bet
        ItemStack betInfo = game.getBetItem().clone();
        ItemMeta betMeta = betInfo.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(plugin.colorize("&e&lPot: " + game.getTotalPot() + "x " + game.getItemDisplayName()));
        betMeta.setLore(lore);
        betInfo.setItemMeta(betMeta);
        betInfo.setAmount(Math.min(game.getTotalPot(), 64));
        gui1.setItem(4, betInfo);
        gui2.setItem(4, betInfo);

        player1.openInventory(gui1);
        player2.openInventory(gui2);

        // Perform the flip and determine winner
        UUID winnerId = game.performFlip();
        boolean player1Wins = winnerId.equals(game.getCreatorId());

        // Start animation
        new BukkitRunnable() {
            int ticks = 0;
            int currentDelay = 2;
            int ticksSinceSwitch = 0;
            boolean showingPlayer1 = true;
            final int totalDuration = plugin.getConfig().getInt("animation.duration", 60);
            final int startSpeed = plugin.getConfig().getInt("animation.start-speed", 2);
            final int endSpeed = plugin.getConfig().getInt("animation.end-speed", 10);

            @Override
            public void run() {
                ticks++;
                ticksSinceSwitch++;

                // Calculate current speed (slows down over time)
                float progress = (float) ticks / totalDuration;
                currentDelay = (int) (startSpeed + (endSpeed - startSpeed) * progress);

                if (ticksSinceSwitch >= currentDelay) {
                    ticksSinceSwitch = 0;
                    showingPlayer1 = !showingPlayer1;

                    // Update the center slot
                    ItemStack currentHead = showingPlayer1 ? head1 : head2;
                    gui1.setItem(13, currentHead);
                    gui2.setItem(13, currentHead);

                    // Play sound
                    if (player1.isOnline()) {
                        player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1f);
                    }
                    if (player2.isOnline()) {
                        player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1f);
                    }
                }

                // Animation complete
                if (ticks >= totalDuration) {
                    this.cancel();

                    // Show winner
                    ItemStack winnerHead = player1Wins ? head1 : head2;
                    String winnerName = player1Wins ? game.getCreatorName() : game.getOpponentName();

                    // Update to winner
                    gui1.setItem(13, winnerHead);
                    gui2.setItem(13, winnerHead);

                    // Add winner indicator
                    ItemStack winnerItem = createItem(Material.GOLD_BLOCK,
                            "&6&l" + winnerName + " WINS!",
                            "&d+" + game.getTotalPot() + "x " + game.getItemDisplayName());
                    gui1.setItem(22, winnerItem);
                    gui2.setItem(22, winnerItem);

                    // Play win sound
                    if (player1.isOnline()) {
                        player1.playSound(player1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    }
                    if (player2.isOnline()) {
                        player2.playSound(player2.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    }

                    // Complete the game after a short delay
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            plugin.getCoinflipManager().completeItemGame(game.getGameId(), winnerId);

                            if (player1.isOnline())
                                player1.closeInventory();
                            if (player2.isOnline())
                                player2.closeInventory();
                        }
                    }.runTaskLater(plugin, 40L);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private ItemStack createPlayerHead(UUID playerId, String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerId));
        meta.setDisplayName(plugin.colorize("&e&l" + playerName));
        head.setItemMeta(meta);
        return head;
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
