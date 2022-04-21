package mineopoly_three.game;

import mineopoly_three.action.TurnAction;
import mineopoly_three.graphics.ImageManager;
import mineopoly_three.item.InventoryItem;
import mineopoly_three.item.ItemType;
import mineopoly_three.strategy.PlayerBoardView;
import mineopoly_three.tiles.EmptyTile;
import mineopoly_three.tiles.ResourceTile;
import mineopoly_three.tiles.Tile;
import mineopoly_three.tiles.TileType;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameBoard {

    /**How much longer it takes an autominer to mine a resource compared to a player*/
    public static final int AUTOMINER_MULTIPLIER = 4;

    private Tile[][] board;
    private Point redStartLocation;
    private Point blueStartLocation;

    // A map of tile locations affected by an autominer to the number of turns they have been mined
    private Map<Point, Integer> autominerTiles;

    // Variables to pre-compute things about the board and change them quickly on the fly
    private TileType[][] tileView;
    private Map<Point, List<InventoryItem>> itemsOnGround;

    public GameBoard(Tile[][] tiles) {
        board = tiles;

        autominerTiles = new HashMap<>();

        // Pre-compute the intensive variables to give to a PlayerBoardView
        itemsOnGround = new HashMap<>();
        tileView = new TileType[board.length][board.length];
        for (int i = 0; i < tileView.length; i++) {
            for (int j = 0; j < tileView[i].length; j++) {
                tileView[i][j] = board[i][j].getType();

                Point itemLocation = board[i][j].getLocation();
                Point itemLocationCopy = new Point(itemLocation.x, itemLocation.y);
                List<InventoryItem> items = new ArrayList<>(board[i][j].getItemsOnTile());
                itemsOnGround.put(itemLocationCopy, items);
            }
        }
    }

    public int getSize() {
        return board.length;
    }

    public Point getRedStartTileLocation() {
        return redStartLocation;
    }

    public Point getBlueStartTileLocation() {
        return blueStartLocation;
    }

    public void setRedStartLocation(Point redStartLocation) {
        this.redStartLocation = redStartLocation;
    }

    public void setBlueStartLocation(Point blueStartLocation) {
        this.blueStartLocation = blueStartLocation;
    }

    public void trackItemsOnPoint(Point pointWithItem, List<InventoryItem> itemsOnPoint) {
        itemsOnGround.put(pointWithItem, new ArrayList<>(itemsOnPoint));
    }

    /**
     * Gets the tile at the specified location in Cartesian (x, y) coordinates with (0, 0) as the bottom left tile
     *  and (boardSize - 1, boardSize - 1) as the top right tile
     *
     * @param location A Point representing (x, y) coordinates of the tile to get
     * @return The Tile at the specified location on the board
     */
    public Tile getTileAtLocation(Point location) {
        return getTileAtLocation(location.x, location.y);
    }

    /**
     * Gets the tile at the specified location in Cartesian (x, y) coordinates with (0, 0) as the bottom left tile
     *  and (boardSize - 1, boardSize - 1) as the top right tile
     *
     * @param x The x coordinate of the tile to get
     * @param y The y coordinate of the tile to get
     * @return The Tile at the specified location on the board
     */
    public Tile getTileAtLocation(int x, int y) {
        if (isValidLocation(x, y)) {
            return board[(board.length - 1) - y][x];
        }
        return null;
    }

    /**
     * Updates the Tile at the parameter tile's location to be the parameter tile. This function handles
     *  logic like calling Tile.onEnter() if necessary
     *
     * @param newTile The Tile that will be set at the Point specified by newTile.getLocation()
     */
    public void setTileAtTileLocation(Tile newTile) {
        // Because tiles know their location, we don't need to pass it in
        int x = newTile.getLocation().x;
        int y = newTile.getLocation().y;

        if (isValidLocation(x, y)) {
            Tile oldTile = board[(board.length - 1) - y][x];
            MinePlayer playerOnTile = oldTile.getPlayerOnTile();

            if (playerOnTile != null) {
                oldTile.onExit(playerOnTile);
                newTile.onEnter(playerOnTile);
                playerOnTile.setCurrentTile(newTile);
            }
            board[(board.length - 1) - y][x] = newTile;
            tileView[(board.length - 1) - y][x] = newTile.getType();
        }
    }

    private boolean isValidLocation(Point location) {
        return isValidLocation(location.x, location.y);
    }

    private boolean isValidLocation(int x, int y) {
        int xIndex = x;
        int yIndex = (board.length - 1) - y;

        boolean xIndexInBounds = (xIndex >= 0 && xIndex < board.length);
        boolean yIndexInBounds = (yIndex >= 0 && yIndex < board.length);
        return xIndexInBounds && yIndexInBounds;
    }

    /**
     * Called every turn for the board to update its internal state
     */
    public void update() {

        List<Point> autominers = new ArrayList<>();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                Tile currentTile = board[i][j];
                currentTile.update();

                if(currentTile.getItemsOnTile().stream().anyMatch(x -> x.getItemType() == ItemType.AUTOMINER)) {
                    autominers.add(currentTile.getLocation());
                }
            }
        }

        // All tiles that are affected by at least one autominer
        List<Point> affectedTiles = new ArrayList<>();

        for(Point miner : autominers) {
            affectedTiles.add(new Point(miner.x, miner.y));
            if(miner.x > 0) affectedTiles.add(new Point(miner.x - 1, miner.y));
            if(miner.x < board.length - 1) affectedTiles.add(new Point(miner.x + 1, miner.y));
            if(miner.y > 0) affectedTiles.add(new Point(miner.x, miner.y - 1));
            if(miner.y < board.length - 1) affectedTiles.add(new Point(miner.x, miner.y + 1));
        }

        for(Point p : affectedTiles) {
            int turnsMined = autominerTiles.getOrDefault(p, 0);

            Tile tile = getTileAtLocation(p);

            if(tile instanceof EmptyTile) {
                tile = tile.interact(null, TurnAction.MINE);
            } else if(turnsMined > 0 && tile instanceof ResourceTile) {
                if(turnsMined % AUTOMINER_MULTIPLIER == 0) {
                    tile = tile.interact(null, TurnAction.MINE);
                }
            }

            setTileAtTileLocation(tile);

            autominerTiles.put(p, turnsMined + 1);
        }

    }

    /**
     * Converts this GameBoard into a restricted information view of the board relative to what one player is
     *  allowed to know
     *
     * @param playerReceivingView The player who will receive this restricted information view
     * @param otherPlayer The other player, so the player receiving the view can know score information
     * @return A PlayerBoardView that contains all the information about this GameBoard for this turn which a
     *          player strategy is allowed to know
     */
    public PlayerBoardView convertToView(MinePlayer playerReceivingView, MinePlayer otherPlayer) {
        Point otherPlayerLocation = otherPlayer.getCurrentTile().getLocation();
        int otherPlayerScore = otherPlayer.getScore();
        return this.convertToView(playerReceivingView, otherPlayerLocation, otherPlayerScore);
    }

    /**
     * Converts this GameBoard into a restricted information view of the board relative to what one player is
     *  allowed to know
     *
     * @param playerReceivingView The player who will receive this restricted information view
     * @param otherPlayerLocation The opposing player's location
     * @param otherPlayerScore The opposing player's current score
     * @return A PlayerBoardView that contains all the information about this GameBoard for this turn which a
     *          player strategy is allowed to know
     */
    public PlayerBoardView convertToView(MinePlayer playerReceivingView, Point otherPlayerLocation,
                                         int otherPlayerScore) {
        // Because we're passing around references, we don't want one strategy to change what the other sees
        //  so we need to be careful and make copies of data being passed by reference
        Map<Point, List<InventoryItem>> itemsOnGroundCopy = new HashMap<>();
        for (Point pointWithItem : itemsOnGround.keySet()) {
            Point pointCopy = new Point(pointWithItem.x, pointWithItem.y);
            itemsOnGroundCopy.put(pointCopy, new ArrayList<>(itemsOnGround.get(pointWithItem)));
        }

        // Make copies of these locations so they may not be modified by a strategy
        Point playerLocation = playerReceivingView.getCurrentTile().getLocation();
        Point playerLocationCopy = new Point(playerLocation.x, playerLocation.y);
        Point otherLocationCopy = new Point(otherPlayerLocation.x, otherPlayerLocation.y);
        // We don't need to copy tileView because it is never given to the strategy by reference
        return new PlayerBoardView(tileView, itemsOnGroundCopy, playerLocationCopy, otherLocationCopy, otherPlayerScore);
    }

    /**
     * Specifies how to render this GameBoard on the Graphics2D object passed in
     *
     * @param brush The Graphics2D object on which to render this GameBoard
     * @param imageManager The ImageManager object that manages all images for the JPanel component rendering this GameBoard
     */
    public void paint(Graphics2D brush, ImageManager imageManager) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                Tile currentTile = board[i][j];
                currentTile.paint(brush, board.length, imageManager);
            }
        }
    }
}
