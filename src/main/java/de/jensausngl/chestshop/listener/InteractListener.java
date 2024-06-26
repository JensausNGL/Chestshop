package de.jensausngl.chestshop.listener;

import de.jensausngl.chestshop.ChestShop;
import de.jensausngl.chestshop.model.Shop;
import de.jensausngl.chestshop.model.ShopType;
import de.jensausngl.chestshop.util.InventoryUtil;
import de.jensausngl.chestshop.util.NumberUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Sign;

import java.util.*;

public class InteractListener implements Listener {

    private final ChestShop chestShop;
    private final Map<Player, Shop> currentBuyShop;
    private final Map<Player, Shop> currentSellShop;

    public InteractListener(final ChestShop chestShop) {
        this.chestShop = chestShop;
        this.currentBuyShop = new HashMap<>();
        this.currentSellShop = new HashMap<>();
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        this.currentBuyShop.remove(player);
        this.currentSellShop.remove(player);
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        if (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
            return;
        }

        final Sign sign = (Sign) block.getState().getData();
        final Block relativeBlock = block.getRelative(sign.getAttachedFace());

        if (relativeBlock.getType() != Material.CHEST && relativeBlock.getType() != Material.TRAPPED_CHEST) {
            return;
        }

        final Shop shop = this.chestShop.getShop(block);

        if (shop == null) {
            return;
        }

        if (shop.getType() == ShopType.BUY) {
            final ComponentBuilder builder = new ComponentBuilder("")
                    .append("» ").color(ChatColor.AQUA).bold(true)
                    .append("ℹ ").color(ChatColor.WHITE)
                    .append("Gib in den Chat ein, wie oft du das Item ").color(ChatColor.AQUA).bold(false)
                    .append(new TranslatableComponent(shop.getItem().getTranslation())).color(ChatColor.AQUA).bold(true)
                    .append(" kaufen möchtest.").color(ChatColor.AQUA).bold(false);

            player.spigot().sendMessage(builder.create());
            this.currentBuyShop.put(player, shop);
            return;
        }

        final Inventory inventory = Bukkit.createInventory(null, 27);

        for (int i = 0; i < 27; i++) {
            if (i == 11) {
                final ItemStack itemStack = InventoryUtil.createItemStack(shop.getItem());
                final ItemMeta meta = itemStack.getItemMeta();
                meta.setLore(Collections.singletonList(String.format("§7§l> §b%s Euro / Item",
                        NumberUtil.format(shop.getPrice()))));

                itemStack.setItemMeta(meta);
                inventory.setItem(i, itemStack);
                continue;
            }

            if (i == 15) {
                continue;
            }

            inventory.setItem(i, InventoryUtil.SPACER);
        }

        final IChatBaseComponent title = new ChatComponentText("§9§lVerkaufen: §8")
                .addSibling(new ChatMessage(shop.getItem().getTranslation()));

        InventoryUtil.openInventory(player, inventory, title);
        this.currentSellShop.put(player, shop);
    }

    @EventHandler
    public void onChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final Shop shop = this.currentBuyShop.get(player);

        if (shop == null) {
            return;
        }

        final Location location = player.getLocation();
        event.setCancelled(true);

        if (event.getMessage().equalsIgnoreCase("stop")) {
            final ComponentBuilder builder = new ComponentBuilder("")
                    .append("» ").color(ChatColor.GREEN).bold(true)
                    .append("✔ ").color(ChatColor.DARK_GREEN)
                    .append("Eingabe beendet!").color(ChatColor.GREEN).bold(false);

            player.spigot().sendMessage(builder.create());
            this.currentBuyShop.remove(player);
            return;
        }

        if (!location.getWorld().getName().equals(shop.getWorld())) {
            this.currentBuyShop.remove(player);
            return;
        }

        final Block signBlock = location.getWorld().getBlockAt(shop.getX(), shop.getY(), shop.getZ());

        if (signBlock.getLocation().distance(location) > 5) {
            final ComponentBuilder builder = new ComponentBuilder("")
                    .append("» ").color(ChatColor.RED).bold(true)
                    .append("✖ ").color(ChatColor.DARK_RED)
                    .append("Abgebrochen. ").color(ChatColor.RED)
                    .append("Du befindest dich zu weit vom Schild entfernt!").color(ChatColor.RED).bold(false);

            player.spigot().sendMessage(builder.create());
            this.currentBuyShop.remove(player);
            return;
        }

        final int amount = NumberUtil.getInt(event.getMessage());

        if (amount <= 0) {
            final ComponentBuilder builder = new ComponentBuilder("")
                    .append("» ").color(ChatColor.RED).bold(true)
                    .append("✖ ").color(ChatColor.DARK_RED)
                    .append("Abgebrochen. ").color(ChatColor.RED)
                    .append("Ungültige Itemanzahl!").color(ChatColor.RED).bold(false);

            player.spigot().sendMessage(builder.create());
            return;
        }

        final Sign sign = (Sign) signBlock.getState().getData();
        final Block chestBlock = signBlock.getRelative(sign.getAttachedFace());
        final Chest chest = (Chest) chestBlock.getState();
        final Inventory inventory = chest.getInventory();
        final Item item = Item.REGISTRY.get(new MinecraftKey(shop.getItem().getIdentifier()));

        if (item == null) {
            // this should never happen
            return;
        }

        final int shopAmount = InventoryUtil.getAmount(shop.getItem(), inventory);

        if (shopAmount == 0) {
            final ComponentBuilder builder = new ComponentBuilder("")
                    .append("» ").color(ChatColor.RED).bold(true)
                    .append("✖ ").color(ChatColor.DARK_RED)
                    .append("Abgebrochen. ").color(ChatColor.RED)
                    .append("Dieser Firmen-Shop ist derzeit ausverkauft.").color(ChatColor.RED).bold(false);

            player.spigot().sendMessage(builder.create());
            this.currentBuyShop.remove(player);
            return;
        }

        if (InventoryUtil.getSpace(shop.getItem(), player.getInventory()) < amount) {
            final ComponentBuilder builder = new ComponentBuilder("")
                    .append("» ").color(ChatColor.RED).bold(true)
                    .append("✖ ").color(ChatColor.DARK_RED)
                    .append("Abgebrochen. ").color(ChatColor.RED)
                    .append("In deinem Inventar ist nicht genügend Platz, um diese Menge zu kaufen.").color(ChatColor.RED).bold(false);

            player.spigot().sendMessage(builder.create());
            return;
        }

        // ToDo: check money

        if (amount > shopAmount) {
            final ComponentBuilder builder = new ComponentBuilder("")
                    .append("» ").color(ChatColor.AQUA).bold(true)
                    .append("ℹ ").color(ChatColor.WHITE)
                    .append("Dieser Firmenshop erhält nicht die angegebene Menge an Items.").color(ChatColor.AQUA).bold(false);
            player.spigot().sendMessage(builder.create());
            return;
        }

        InventoryUtil.transfer(shop.getItem(), amount, inventory, player.getInventory());

        final ComponentBuilder builder = new ComponentBuilder("")
                .append("» ").color(ChatColor.GREEN).bold(true)
                .append("✔ ").color(ChatColor.DARK_GREEN)
                .append("Du hast ").color(ChatColor.GREEN).bold(false)
                .append(amount + "x ").color(ChatColor.GREEN).bold(true)
                .append(new TranslatableComponent(shop.getItem().getTranslation())).color(ChatColor.GREEN)
                .append(" für ").color(ChatColor.GREEN).bold(false)
                .append(NumberUtil.format(amount * shop.getPrice()) + " Euro").color(ChatColor.GREEN).bold(true)
                .append(" gekauft!").color(ChatColor.GREEN).bold(false);

        player.spigot().sendMessage(builder.create());
        this.currentBuyShop.remove(player);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();

        if (!this.currentSellShop.containsKey(player)) {
            return;
        }

        final ItemStack previewItem = event.getView().getTopInventory().getItem(11);
        final ItemStack currentItem = event.getCurrentItem();

        if (previewItem == null) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }

        if (previewItem.isSimilar(currentItem)) {
            if (event.getClick().isShiftClick()) {
                event.setCancelled(true);
                return;
            }

            if (event.getClick() == ClickType.DOUBLE_CLICK) {
                event.setCancelled(true);
                return;
            }
        }

        this.handleSellInput(player, event.getView().getTopInventory());

        final Inventory inventory = event.getClickedInventory();

        if (inventory == null || inventory instanceof PlayerInventory) {
            return;
        }

        if (event.getSlot() != 15) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();

        if (!this.currentSellShop.containsKey(player)) {
            return;
        }

        this.handleSellInput(player, event.getView().getTopInventory());

        final Set<Integer> slots = event.getRawSlots();

        for (int i = 0; i < 27; i++) {
            if (i == 15) {
                continue;
            }

            if (slots.contains(i)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            this.currentSellShop.remove((Player) event.getPlayer());
        }
    }

    private void handleSellInput(final Player player, final Inventory inventory) {
        Bukkit.getScheduler().runTaskLater(this.chestShop, () -> {
            final ItemStack itemStack = inventory.getItem(15);

            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return;
            }

            final Shop shop = this.currentSellShop.get(player);

            if (shop == null) {
                // player has closed the inventory within 1 tick
                inventory.setItem(15, InventoryUtil.EMPTY);
                player.getInventory().addItem(itemStack);
                return;
            }

            if (!InventoryUtil.isItem(shop.getItem(), itemStack)) {
                inventory.setItem(15, InventoryUtil.EMPTY);
                player.getInventory().addItem(itemStack);

                final ComponentBuilder builder = new ComponentBuilder("")
                        .append("» ").color(ChatColor.RED).bold(true)
                        .append("✖ ").color(ChatColor.DARK_RED)
                        .append("Abgebrochen. ").color(ChatColor.RED)
                        .append("Du kannst dieses Item hier nicht verkaufen. Lege das Item, das du verkaufen willst in den freien Slot!")
                        .color(ChatColor.RED).bold(false);

                player.spigot().sendMessage(builder.create());
                return;
            }

            final Block signBlock = player.getWorld().getBlockAt(shop.getX(), shop.getY(), shop.getZ());
            final Sign sign = (Sign) signBlock.getState().getData();
            final Block chestBlock = signBlock.getRelative(sign.getAttachedFace());
            final Chest chest = (Chest) chestBlock.getState();
            final Inventory chestInventory = chest.getInventory();

            if (!InventoryUtil.hasSpace(itemStack, chestInventory)) {
                inventory.setItem(15, InventoryUtil.EMPTY);
                player.getInventory().addItem(itemStack);

                final ComponentBuilder builder = new ComponentBuilder("")
                        .append("» ").color(ChatColor.RED).bold(true)
                        .append("✖ ").color(ChatColor.DARK_RED)
                        .append("Abgebrochen. ").color(ChatColor.RED)
                        .append("In dem Inventar des Firmen-Shops ist nicht genügend Platz!")
                        .color(ChatColor.RED).bold(false);

                player.spigot().sendMessage(builder.create());
                return;
            }

            // ToDo: money handling

            inventory.setItem(15, InventoryUtil.EMPTY);
            chestInventory.addItem(itemStack.clone());

            final ComponentBuilder builder = new ComponentBuilder("")
                    .append("» ").color(ChatColor.GREEN).bold(true)
                    .append("✔ ").color(ChatColor.DARK_GREEN)
                    .append("Du hast ").color(ChatColor.GREEN).bold(false)
                    .append(itemStack.getAmount() + "x ").color(ChatColor.GREEN).bold(true)
                    .append(new TranslatableComponent(shop.getItem().getTranslation())).color(ChatColor.GREEN)
                    .append(" für ").color(ChatColor.GREEN).bold(false)
                    .append(NumberUtil.format(itemStack.getAmount() * shop.getPrice()) + " Euro").color(ChatColor.GREEN).bold(true)
                    .append(" verkauft!").color(ChatColor.GREEN).bold(false);

            player.spigot().sendMessage(builder.create());
        }, 1);
    }

}
