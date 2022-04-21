package mineopoly_three.tiles;

import mineopoly_three.game.MinePlayer;
import mineopoly_three.action.TurnAction;
import mineopoly_three.graphics.ImageManager;
import mineopoly_three.graphics.TileRenderLayer;
import mineopoly_three.item.InventoryItem;
import mineopoly_three.item.ItemType;

import java.awt.*;

public class ResourceTile extends StoneTile {
    private ItemType tileResource;
    private int numTimesMined;

    public ResourceTile(Point tileLocation, ItemType tileResource) {
        super(tileLocation);
        assert(tileResource.isResource());
        this.tileResource = tileResource;
        this.numTimesMined = 0;
    }

    @Override
    public TileType getType() {
        return tileResource.getResourceTileType();
    }

    public ItemType getTileResource() {
        return tileResource;
    }

    @Override
    public Tile interact(MinePlayer playerOnTile, TurnAction actionOnTile) {
        super.interact(playerOnTile, actionOnTile);

        if (actionOnTile == TurnAction.MINE) {
            numTimesMined++;

            if (numTimesMined >= tileResource.getTurnsToMine()) {
                Tile minedTile = new CrackedTile(location);
                minedTile.getItemsOnTile().addAll(getItemsOnTile());
                minedTile.getItemsOnTile().add(new InventoryItem(tileResource));
                return minedTile;
            }
        }

        return this;
    }

    @Override
    protected Image[] getImageOverlays(ImageManager imageManager) {
        Image[] imageOverlays = super.getImageOverlays(imageManager);

        // Add texture for resource
        Image resourceTileOverlay = imageManager.getScaledImage(tileResource.getTileImageName());
        int resourceTypeLayerIndex = TileRenderLayer.LAYER_RESOURCE_TYPE.ordinal();
        imageOverlays[resourceTypeLayerIndex] = resourceTileOverlay;

        // Add crack textures if this has been mined
        Image crackOverlay = imageManager.getScaledImage("crack_" + numTimesMined);
        int crackLayerIndex = TileRenderLayer.LAYER_CRACK.ordinal();
        imageOverlays[crackLayerIndex] = crackOverlay;
        return imageOverlays;
    }
}
