package de.jensausngl.chestshop.util;

import de.jensausngl.chestshop.model.ShopItem;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryUtil {

    public static final ItemStack SPACER = new ItemStack(Material.IRON_SWORD, 1, (short) 246);
    public static final ItemStack EMPTY = new ItemStack(Material.AIR);

    static {
        final ItemMeta meta = SPACER.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);
        meta.setDisplayName("§r");

        SPACER.setItemMeta(meta);
    }

    public static int getAmount(final ShopItem shopItem, final Inventory inventory) {
        int amount = 0;

        for (final ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) {
                continue;
            }

            if (!isItem(shopItem, itemStack)) {
                continue;
            }

            amount += itemStack.getAmount();
        }

        return amount;
    }

    public static int getSpace(final ShopItem shopItem, final Inventory inventory) {
        final Item item = Item.REGISTRY.get(new MinecraftKey(shopItem.getIdentifier()));
        int amount = 0;

        if (item == null) {
            return amount;
        }

        for (final ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                amount += item.getMaxStackSize();
            }
        }

        return amount;
    }

    public static boolean hasSpace(final ItemStack item, final Inventory inventory) {
        for (final ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return true;
            }

            if (itemStack.getMaxStackSize() - itemStack.getAmount() >= item.getAmount()) {
                return true;
            }
        }

        return false;
    }

    public static void transfer(final ShopItem shopItem, int amount, final Inventory fromInventory, final Inventory toInventory) {
        for (final ItemStack itemStack : fromInventory.getContents().clone()) {
            if (amount <= 0) {
                return;
            }

            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }

            if (!isItem(shopItem, itemStack)) {
                continue;
            }

            if (itemStack.getAmount() <= amount) {
                fromInventory.removeItem(itemStack);
                toInventory.addItem(itemStack);

                amount -= itemStack.getAmount();
                continue;
            }

            itemStack.setAmount(itemStack.getAmount() - amount);

            final ItemStack toItemStack = itemStack.clone();
            toItemStack.setAmount(amount);
            toInventory.addItem(toItemStack);
            return;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isItem(final ShopItem shopItem, final ItemStack itemStack) {
        final Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        final MinecraftKey key = Item.REGISTRY.b(item);

        if (key == null) {
            return false;
        }

        if (!key.getKey().equals(shopItem.getIdentifier())) {
            return false;
        }

        return itemStack.getDurability() == shopItem.getData();
    }

    public static ItemStack createItemStack(final ShopItem shopItem) {
        final Item item = Item.REGISTRY.get(new MinecraftKey(shopItem.getIdentifier()));

        if (item == null) {
            return EMPTY;
        }

        final ItemStack itemStack = CraftItemStack.asNewCraftStack(item);
        itemStack.setDurability((short) shopItem.getData());

        return itemStack;
    }



    public static void openInventory(final Player player, final Inventory inventory, final IChatBaseComponent title) {
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final IInventory minecraftInventory = ((CraftInventory) inventory).getInventory();

        final Container container = CraftEventFactory.callInventoryOpenEvent(entityPlayer,
                new ContainerChest(entityPlayer.inventory, minecraftInventory, entityPlayer));

        if (container == null) {
            minecraftInventory.closeContainer(entityPlayer);
            return;
        }

        if (entityPlayer.activeContainer != entityPlayer.defaultContainer) {
            entityPlayer.closeInventory();
        }

        final int containerCounter = entityPlayer.nextContainerCounter();

        entityPlayer.activeContainer = container;
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerCounter, "minecraft:container", title, inventory.getSize()));
        entityPlayer.activeContainer.windowId = containerCounter;
        entityPlayer.activeContainer.addSlotListener(entityPlayer);
    }

}
