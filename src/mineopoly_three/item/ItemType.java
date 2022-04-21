package mineopoly_three.item;

import mineopoly_three.tiles.TileType;

public enum ItemType {
    DIAMOND(true, 150, 500, 5, 3, 0.8, 1.0, 0.075, TileType.RESOURCE_DIAMOND, "diamond_overlay", "diamond_item"),
    EMERALD(true, 100, 450, 4, 2, 0.5, 0.8, 0.09, TileType.RESOURCE_EMERALD, "emerald_overlay", "emerald_item"),
    RUBY(true, 50, 400, 3, 1, 0.3, 0.5, 0.1, TileType.RESOURCE_RUBY, "ruby_overlay", "ruby_item"),
    AUTOMINER("autominer_item");

    private final int startingPrice;
    private final int maxPrice;
    private final int priceIncreasePerTurn;
    private final int turnsToMine;
    private final double minSpawnDistanceRatio;
    private final double maxSpawnDistanceRatio;
    private final double spawnCountRatio;
    private final TileType resourceTileType;
    private final String tileImageName;
    private final String itemImageName;
    private final boolean resource;

    ItemType(boolean resource, int startingPrice, int maxPrice, int priceIncreasePerTurn, int turnsToMine,
             double minSpawnDistanceRatio, double maxSpawnDistanceRatio, double spawnCountRatio,
             TileType resourceTileType, String tileImageName, String itemImageName) {
        this.resource = resource;
        this.startingPrice = startingPrice;
        this.maxPrice = maxPrice;
        this.priceIncreasePerTurn = priceIncreasePerTurn;
        this.turnsToMine = turnsToMine;
        this.minSpawnDistanceRatio = minSpawnDistanceRatio;
        this.maxSpawnDistanceRatio = maxSpawnDistanceRatio;
        this.spawnCountRatio = spawnCountRatio;
        this.resourceTileType = resourceTileType;
        this.tileImageName = tileImageName;
        this.itemImageName = itemImageName;
    }

    ItemType(String itemImageName) {
        this(false, 0, 0, 0, 0, 0, 0, 0, null, null, itemImageName);
    }

    public boolean isResource() {
        return resource;
    }

    public int getStartingPrice() {
        return startingPrice;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public int getPriceIncreasePerTurn() {
        return priceIncreasePerTurn;
    }

    public int getTurnsToMine() {
        return turnsToMine;
    }

    public double getMinSpawnDistanceRatio() {
        return minSpawnDistanceRatio;
    }

    public double getMaxSpawnDistanceRatio() {
        return maxSpawnDistanceRatio;
    }

    public double getSpawnCountRatio() {
        return spawnCountRatio;
    }

    public TileType getResourceTileType() {
        return resourceTileType;
    }

    public String getTileImageName() {
        return tileImageName;
    }

    public String getItemImageName() {
        return itemImageName;
    }
}
