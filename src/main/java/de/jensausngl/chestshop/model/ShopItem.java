package de.jensausngl.chestshop.model;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;

public class ShopItem {

    private final String identifier;
    private final int data;
    private final String translation;
    private String displayName;

    private ShopItem(final String identifier, final int data, final String translation) {
        this.identifier = identifier;
        this.data = data;
        this.translation = translation;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public int getData() {
        return this.data;
    }

    public String getTranslation() {
        return this.translation;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean hasDisplayName() {
        return this.displayName != null;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public static ShopItem getByName(final String name, final int data) {
        final MinecraftKey key = new MinecraftKey(name.toLowerCase().replaceAll("\\s+", "_"));
        final Item item = Item.REGISTRY.get(key);

        return get(key, item, data);
    }

    public static ShopItem getById(final int id, final int data) {
        final Item item = Item.getById(id);

        return get(Item.REGISTRY.b(item), item, data);
    }

    public static ShopItem getByItemStack(final org.bukkit.inventory.ItemStack itemStack) {
        final Item item = CraftItemStack.asNMSCopy(itemStack).getItem();

        return get(Item.REGISTRY.b(item), item, itemStack.getDurability());
    }

    private static ShopItem get(final MinecraftKey key, final Item item, final int data) {
        if (item == null) {
            return null;
        }

        if (key == null) {
            return null;
        }

        if (!validateData(item, data)) {
            return null;
        }

        final ItemStack itemStack = new ItemStack(item, 1, data);
        final String translation = item.a(itemStack) + ".name";

        return new ShopItem(key.getKey(), data, translation);
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "deprecation"})
    private static boolean validateData(final Item item, final int data) {
        if (data < 0) {
            return false;
        }

        // Item.k() returns if it uses the durability as data
        if (!item.k()) {
            return data <= item.getMaxDurability();
        }

        // no good way to validate item data, so we have to do it manually

        if (item instanceof ItemCoal) {
            return data <= 1;
        }

        if (item instanceof ItemGoldenApple) {
            return data <= 1;
        }

        if (item instanceof ItemFish) {
            // 0 and 1, is raw so always valid
            if (data <= 1) {
                return true;
            }

            final MinecraftKey key = Item.REGISTRY.b(item);

            if (key == null) {
                return false;
            }

            // 2 and 3, only valid if raw
            return data <= 3 && key.getKey().equals("fish");
        }

        if (item instanceof ItemDye) {
            return data <= 15;
        }

        if (item instanceof ItemBed) {
            return data <= 15;
        }

        if (item instanceof ItemWorldMap) {
            // Spigot saves the data as short
            return data <= Short.MAX_VALUE;
        }

        if (item instanceof ItemSkull) {
            return data <= 5;
        }

        if (item instanceof ItemBanner) {
            return data <= 15;
        }

        if (!(item instanceof ItemBlock)) {
            // this should never happen
            return false;
        }

        final ItemBlock itemBlock = (ItemBlock) item;
        final Block block = itemBlock.getBlock();

        if (block == Blocks.MONSTER_EGG) {
            return data <= 5;
        }

        if (block == Blocks.TALLGRASS) {
            return data <= 2;
        }

        if (block == Blocks.DOUBLE_PLANT) {
            return data <= 5;
        }

        final IBlockData blockData = block.fromLegacyData(data);
        final int dropData = block.getDropData(blockData);

        return dropData == data;
    }

}
