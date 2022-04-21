package mineopoly_three.game;

import mineopoly_three.item.InventoryItem;
import mineopoly_three.item.ItemType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class Economy extends Observable {
    private static final int NUM_UPDATES_BEFORE_NOTIFY = 10;

    private final ItemType[] sellableResourceTypes;
    private Map<ItemType, Integer> resourcePrices;
    private int updatesSinceLastNotify;

    public Economy(ItemType[] resourceTypes) {
        this.sellableResourceTypes = resourceTypes;
        resourcePrices = new HashMap<>();
        for (ItemType resourceType : sellableResourceTypes) {
            resourcePrices.put(resourceType, resourceType.getStartingPrice());
        }
        this.updatesSinceLastNotify = 0;
    }

    /**
     * Gets a copy of the current prices for each ResourceType
     *
     * @return A Map from each ResourceType to its price
     */
    public Map<ItemType, Integer> getCurrentPrices() {
        // Nothing outside this class should be able to modify prices, need to make a copy
        Map<ItemType, Integer> resourcePricesCopy = new HashMap<>();
        for (ItemType resourceType : resourcePrices.keySet()) {
            resourcePricesCopy.put(resourceType, resourcePrices.get(resourceType));
        }
        return resourcePricesCopy;
    }

    /**
     * Increments the price for each ResourceType by its priceIncreasePerTurn value.
     * Notifies any observers (like the GUI) that they should update every NUM_UPDATES_BEFORE_NOTIFY calls
     */
    protected void increaseDemand() {
        for (ItemType resourceType : sellableResourceTypes) {
            Integer currentPrice = resourcePrices.getOrDefault(resourceType, 0);
            Integer nextPrice = currentPrice + resourceType.getPriceIncreasePerTurn();
            if (nextPrice >= resourceType.getMaxPrice()) {
                nextPrice = resourceType.getMaxPrice();
            }
            resourcePrices.put(resourceType, nextPrice);
        }

        // Let any observers, like the GUI, know to update after this updates some number of times
        this.updatesSinceLastNotify++;
        if (this.updatesSinceLastNotify >= NUM_UPDATES_BEFORE_NOTIFY) {
            this.notifyObservers();
        }
    }

    /**
     * Calculates the total price for all InventoryItems in the passed in Collection.
     * Decreases the price for each resource according to newPrice = oldPrice * (1 - (numSold / 8)).
     * Immediately notifies any observers (like the GUI) that they should update.
     *
     * @param itemsToSell The entire Collection of InventoryItems to be sold
     * @return The total price that all InventoryItems were sold for
     */
    protected int sellResources(Collection<InventoryItem> itemsToSell) {
        // Count the number of each resource type being sold
        Map<ItemType, Integer> resourcesToNumSold = new HashMap<>();
        for (InventoryItem itemToSell : itemsToSell) {
            ItemType currentType = itemToSell.getItemType();

            if(currentType.isResource()) {
                int numSold = resourcesToNumSold.getOrDefault(currentType, 0);
                resourcesToNumSold.put(currentType, numSold + 1);
            }
        }

        // We now know how many of each type are being sold, need to calculate how much they are all worth
        int totalSellPrice = 0;
        for (ItemType sellableResourceType : sellableResourceTypes) {
            int numSoldOfType = resourcesToNumSold.getOrDefault(sellableResourceType, 0);
            int priceForResource = resourcePrices.getOrDefault(sellableResourceType, 0);
            totalSellPrice += numSoldOfType * priceForResource;

            // Supply has gone up, decrease the sell price according to oldPrice * (1 - (numSold / 8))
            int newResourcePrice = (int) (priceForResource * (1.0 - (numSoldOfType / 8.0)));
            resourcePrices.put(sellableResourceType, newResourcePrice);
        }

        // Always notify observers on a sell
        this.notifyObservers();
        return totalSellPrice;
    }

    @Override
    public void notifyObservers() {
        this.setChanged();
        super.notifyObservers();
        this.updatesSinceLastNotify = 0;
    }
}
