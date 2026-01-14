package com.coinflip.currency;

import org.bukkit.Material;

public enum Currency {
    DIAMOND(Material.DIAMOND, "&bDiamonds", "diamond"),
    IRON_INGOT(Material.IRON_INGOT, "&7Iron Ingots", "iron"),
    EMERALD(Material.EMERALD, "&aEmeralds", "emerald");

    private final Material material;
    private final String displayName;
    private final String configKey;

    Currency(Material material, String displayName, String configKey) {
        this.material = material;
        this.displayName = displayName;
        this.configKey = configKey;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName.replace("&", "§");
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getDisplayNameStripped() {
        return displayName.replaceAll("&[0-9a-fk-or]", "");
    }

    public static Currency fromMaterial(Material material) {
        for (Currency currency : values()) {
            if (currency.getMaterial() == material) {
                return currency;
            }
        }
        return null;
    }

    public static Currency fromConfigKey(String key) {
        for (Currency currency : values()) {
            if (currency.getConfigKey().equalsIgnoreCase(key)) {
                return currency;
            }
        }
        return null;
    }
}
