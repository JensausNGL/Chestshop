package de.jensausngl.chestshop.model;

public enum ShopType {

    BUY("Kaufen", "buy", "b", "kaufen", "k"),
    SELL("Verkaufen", "sell", "s", "verkaufen", "v");

    private final String displayName;
    private final String[] names;

    ShopType(final String displayName, final String... names) {
        this.displayName = displayName;
        this.names = names;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static ShopType getByName(final String name) {
        for (ShopType type : ShopType.values()) {
            for (String typeName : type.names) {
                if (typeName.equalsIgnoreCase(name)) {
                    return type;
                }
            }
        }

        return null;
    }

}
