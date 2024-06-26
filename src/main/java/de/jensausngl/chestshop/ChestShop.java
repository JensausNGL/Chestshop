package de.jensausngl.chestshop;

import de.jensausngl.chestshop.listener.CreateListener;
import de.jensausngl.chestshop.listener.DeleteListener;
import de.jensausngl.chestshop.listener.InteractListener;
import de.jensausngl.chestshop.model.Shop;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class ChestShop extends JavaPlugin {

    private final Set<Shop> shops = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new CreateListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
        getServer().getPluginManager().registerEvents(new DeleteListener(this), this);
    }

    @Override
    public void onDisable() {

    }

    public void addShop(final Shop shop) {
        this.shops.add(shop);
    }

    public void removeShop(final Block block) {
        final Shop shop = this.getShop(block);

        if (shop != null) {
            this.shops.remove(shop);
        }
    }

    public Shop getShop(final Block block) {
        for (final Shop shop : this.shops) {
            if (block.getX() != shop.getX()) {
                continue;
            }

            if (block.getY() != shop.getY()) {
                continue;
            }

            if (block.getZ() != shop.getZ()) {
                continue;
            }

            return shop;
        }

        return null;
    }

}
