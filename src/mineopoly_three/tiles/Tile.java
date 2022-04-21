package mineopoly_three.tiles;

import mineopoly_three.game.MinePlayer;
import mineopoly_three.action.TurnAction;
import mineopoly_three.graphics.ImageManager;
import mineopoly_three.graphics.TileRenderLayer;
import mineopoly_three.item.InventoryItem;
import mineopoly_three.item.ItemType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public abstract class Tile {
    private static final int NUM_RENDER_LAYERS = TileRenderLayer.values().length;
    protected MinePlayer playerOnTile;
    protected List<InventoryItem> itemsOnTile;
    protected Point location;

    protected Tile(Point tileLocation) {
        this.playerOnTile = null;
        this.itemsOnTile = new ArrayList<>();
        this.location = tileLocation;
    }

    public MinePlayer getPlayerOnTile() {
        return playerOnTile;
    }

    public Point getLocation() {
        return location;
    }

    public List<InventoryItem> getItemsOnTile() {
        return itemsOnTile;
    }

    /**
     * @return The TileType enum that represents this Tile
     */
    public abstract TileType getType();

    /**
     * Called when an action is performed on a Tile by a player to determine
     *  A. What happens for that action and B. What changes are made to the Tile as a result of that action
     *
     * @param playerOnTile The player performing the action on this tile
     * @param actionOnTile The TurnAction being performed on this tile
     * @return A Tile representing what the Tile at this location on the game board should be set to after this turn.
     *          Return this Tile if no change should happen
     *          Return a new Tile of the next type if the action does result in a change on the tile
     */
    public Tile interact(MinePlayer playerOnTile, TurnAction actionOnTile) {

        InventoryItem resource = itemsOnTile.stream().filter(x -> x.getItemType().isResource()).findFirst().orElse(null);
        InventoryItem autominer = itemsOnTile.stream().filter(x -> x.getItemType() == ItemType.AUTOMINER).findFirst().orElse(null);

        // Ensure there already is not an autominer here
        if(actionOnTile == TurnAction.PLACE_AUTOMINER && autominer == null) {
            InventoryItem playerAutominer = playerOnTile.getInventory().stream().filter(x -> x.getItemType() == ItemType.AUTOMINER).findFirst().orElse(null);

            if(playerAutominer != null) {
                playerOnTile.getInventory().remove(playerAutominer);
                itemsOnTile.add(playerAutominer);
            }
        } else {
            InventoryItem toPickUp = null;

            if (actionOnTile == TurnAction.PICK_UP_RESOURCE && resource != null) {
                toPickUp = resource;
            } else if(actionOnTile == TurnAction.PICK_UP_AUTOMINER && autominer != null) {
                toPickUp = autominer;
            }

            if(toPickUp != null && playerOnTile.addItemToInventory(toPickUp))
                itemsOnTile.remove(toPickUp);
        }


        return this;
    }

    /**
     * Called every turn for tiles to update their internal state, if they need to do so
     */
    public void update() {
        // By default tiles don't do anything
    }

    /**
     * Called when a player enters this tile on a turn
     *
     * @param playerEnteringTile The player entering the tile
     */
    public void onEnter(MinePlayer playerEnteringTile) {
        playerOnTile = playerEnteringTile;
    }

    /**
     * Called when a player exits a tile on a turn.
     *
     * @param playerExitingTile The player leaving the tile.
     */
    public void onExit(MinePlayer playerExitingTile) {
        playerOnTile = null;
    }

    /**
     * Gets the Image[] that encodes how to render this Tile
     * Images with later TileRenderLayer values will be rendered on top of Images with earlier values
     *
     * @param imageManager The ImageManager object that manages all images for the JPanel component rendering this Tile
     * @return The Image[] specifying the overlays to render in the order to render them
     */
    protected Image[] getImageOverlays(ImageManager imageManager) {
        Image[] imageOverlays = new Image[NUM_RENDER_LAYERS];

        // Draw players if they're standing on this tile
        if (playerOnTile != null) {
            Image playerImage = playerOnTile.getImage(imageManager);
            int playerOverlayIndex = TileRenderLayer.LAYER_PLAYER.ordinal();
            imageOverlays[playerOverlayIndex] = playerImage;
        }

        // Draw the items that could be on this tile
        for(InventoryItem item : itemsOnTile) {
            String itemImageName = item.getItemType().getItemImageName();
            Image itemImage = imageManager.getScaledImage(itemImageName);
            int itemOverlayIndex = item.getItemType() == ItemType.AUTOMINER ?
                    TileRenderLayer.LAYER_AUTOMINER.ordinal() : TileRenderLayer.LAYER_ITEM.ordinal();
            imageOverlays[itemOverlayIndex] = itemImage;
        }

        return imageOverlays;
    }

    /**
     * Specifies how to render this Tile on the Graphics object passed in
     *
     * @param brush The Graphics object on which to render this Tile
     * @param boardSize The size of the board for use in finding the starting point on the screen to render
     * @param imageManager The ImageManager object that manages all images for the JPanel component rendering this Tile
     */
    public void paint(Graphics2D brush, int boardSize, ImageManager imageManager) {
        int imageWidth = imageManager.getImageWidth();
        int imageHeight = imageManager.getImageHeight();
        Point screenIndex = this.getScreenIndexFromLocation(imageWidth, imageHeight, boardSize);

        // Draw overlays as specified by Tile subclasses
        Image[] imageOverlays = this.getImageOverlays(imageManager);
        for (Image layerImage : imageOverlays) {
            // Nothing is drawn if layerImage is null
            brush.drawImage(layerImage, screenIndex.x, screenIndex.y, null);
        }
    }

    /**
     * Computes the (x, y) coordinates of the pixel on the screen corresponding to the top left of this Tile
     *
     * @param imageWidth The width of the image to be rendered
     * @param imageHeight The height of the image to be rendered
     * @param boardSize The size of the board
     * @return The (x, y) coordinates on the screen of the top left image pixel
     */
    protected Point getScreenIndexFromLocation(int imageWidth, int imageHeight, int boardSize) {
        int xScreenIndex = this.location.x * imageWidth;
        int yScreenIndex = ((boardSize - 1) - location.y) * imageHeight;
        return new Point(xScreenIndex, yScreenIndex);
    }
}
