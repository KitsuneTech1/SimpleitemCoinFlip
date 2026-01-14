package com.coinflip.commands;

import com.coinflip.CoinflipPlugin;
import com.coinflip.gui.MainMenuGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CoinflipCommand implements CommandExecutor, TabCompleter {

    private final CoinflipPlugin plugin;
    private final MainMenuGUI mainMenuGUI;

    public CoinflipCommand(CoinflipPlugin plugin) {
        this.plugin = plugin;
        this.mainMenuGUI = new MainMenuGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("coinflip.use")) {
            player.sendMessage(plugin.colorize("&cYou don't have permission to use coinflip!"));
            return true;
        }

        // Open main menu
        mainMenuGUI.open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
}
