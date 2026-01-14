package com.coinflip;

import com.coinflip.commands.CoinflipCommand;
import com.coinflip.game.CoinflipManager;
import com.coinflip.listeners.GUIListener;
import org.bukkit.plugin.java.JavaPlugin;

public class CoinflipPlugin extends JavaPlugin {

    private static CoinflipPlugin instance;
    private CoinflipManager coinflipManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        coinflipManager = new CoinflipManager(this);
        
        // Register commands
        CoinflipCommand coinflipCommand = new CoinflipCommand(this);
        getCommand("coinflip").setExecutor(coinflipCommand);
        getCommand("coinflip").setTabCompleter(coinflipCommand);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        
        getLogger().info("CoinflipPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Cancel all pending coinflips and return items
        if (coinflipManager != null) {
            coinflipManager.cancelAllCoinflips();
        }
        
        getLogger().info("CoinflipPlugin has been disabled!");
    }

    public static CoinflipPlugin getInstance() {
        return instance;
    }

    public CoinflipManager getCoinflipManager() {
        return coinflipManager;
    }
    
    public String getMessage(String path) {
        String prefix = getConfig().getString("messages.prefix", "&6[Coinflip] &r");
        String message = getConfig().getString("messages." + path, "");
        return colorize(prefix + message);
    }
    
    public String getMessageRaw(String path) {
        return colorize(getConfig().getString("messages." + path, ""));
    }
    
    public String colorize(String text) {
        return text.replace("&", "§");
    }
}
