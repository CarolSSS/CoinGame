package mineopoly_three.item;

public class InventoryItem {
    private ItemType itemType;

    public InventoryItem(ItemType itemType) {
        this.itemType = itemType;
    }

    public ItemType getItemType() {
        return itemType;
    }
}
