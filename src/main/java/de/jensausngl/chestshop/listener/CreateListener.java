package de.jensausngl.chestshop.listener;

import de.jensausngl.chestshop.ChestShop;
import de.jensausngl.chestshop.model.Shop;
import de.jensausngl.chestshop.model.ShopItem;
import de.jensausngl.chestshop.model.ShopType;
import de.jensausngl.chestshop.util.NumberUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateListener implements Listener {

    private final ChestShop chestShop;

    public CreateListener(final ChestShop chestShop) {
        this.chestShop = chestShop;
    }

    @EventHandler
    public void onSignChangeEvent(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();

        if (!lines[0].equalsIgnoreCase("[shop]") && !lines[0].equalsIgnoreCase("[firmenshop]")) {
            return;
        }


        // ToDo: get company of player
        Block block = event.getBlock();

        Sign sign = (Sign) block.getState().getData();
        Block relativeBlock = block.getRelative(sign.getAttachedFace());

        // ToDo: get permissions of chest
        if (relativeBlock.getType() != Material.CHEST && relativeBlock.getType() != Material.TRAPPED_CHEST) {
            final ComponentBuilder builder = new ComponentBuilder("")
                    .append("» ").color(ChatColor.RED).bold(true)
                    .append("✖ ").color(ChatColor.DARK_RED)
                    .append("Es wurde keine Kiste gefunden, aus der Items entnommen werden können! (Du musst Rechte auf die Kiste haben)")
                    .color(ChatColor.RED).bold(false);

            player.spigot().sendMessage(builder.create());
            return;
        }

        ShopType type = ShopType.getByName(lines[1]);

        if (type == null) {
            final ComponentBuilder builder = new ComponentBuilder("")
                    .append("» ").color(ChatColor.RED).bold(true)
                    .append("✖ ").color(ChatColor.DARK_RED)
                    .append("In Zeile zwei wurde kein gültiger Shoptyp gefunden!")
                    .color(ChatColor.RED).bold(false);

            player.spigot().sendMessage(builder.create());
            return;
        }

        final double price = NumberUtil.getDouble(lines[2].replace(",", "."));

        if (price <= 0 || price >= 10_000_000 || NumberUtil.numbersAfterDecimal(price) > 2) {

            final ComponentBuilder builder = new ComponentBuilder("")
                    .append("» ").color(ChatColor.RED).bold(true)
                    .append("✖ ").color(ChatColor.DARK_RED)
                    .append("In Zeile drei wurde kein gültiger Preis gefunden!")
                    .color(ChatColor.RED).bold(false);

            player.spigot().sendMessage(builder.create());
            return;
        }

        final String itemName = lines[3];
        final ShopItem shopItem = itemName.equalsIgnoreCase("?") ? findItem(relativeBlock) : getItem(itemName);

        if (shopItem == null) {

            final ComponentBuilder builder = new ComponentBuilder("")
                    .append("» ").color(ChatColor.RED).bold(true)
                    .append("✖ ").color(ChatColor.DARK_RED)
                    .append("In Zeile drei wurde kein gültiges Item gefunden!")
                    .color(ChatColor.RED).bold(false);

            player.spigot().sendMessage(builder.create());
            return;
        }

        if (shopItem.getIdentifier().equals("iron_sword") || shopItem.getIdentifier().equals("diamond_sword")) {
            shopItem.setDisplayName("Custom Item");
        }

        final Shop shop = new Shop(block, type, shopItem, price);
        shop.updateSign(block);

        this.chestShop.addShop(shop);

        event.setCancelled(true);
    }

    private static ShopItem findItem(final Block block) {
        final Chest chest = (Chest) block.getState();

        for (final ItemStack itemStack : chest.getInventory().getContents()) {
            if (itemStack == null) {
                continue;
            }

            if (itemStack.getType() == Material.AIR) {
                continue;
            }

            return ShopItem.getByItemStack(itemStack);
        }

        return null;
    }

    private static ShopItem getItem(final String value) {
        final Pattern pattern = Pattern.compile("^(\\d+)(?::(\\d+))?$");
        final Matcher matcher = pattern.matcher(value);

        if (matcher.matches()) {
            final int id = NumberUtil.getInt(matcher.group(1));
            final int subId = matcher.group(2) == null ? 0 : NumberUtil.getInt(matcher.group(2));

            // block air and invalid ids
            if (id < 1 || subId < 0) {
                return null;
            }

            return ShopItem.getById(id, subId);
        }

        // block air
        if (value.equalsIgnoreCase("air")) {
            return null;
        }

        return ShopItem.getByName(value, 0);
    }

}
