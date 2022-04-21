
package mineopoly_three.strategy;

import mineopoly_three.action.TurnAction;
import mineopoly_three.game.Economy;
import mineopoly_three.item.InventoryItem;
import mineopoly_three.item.ItemType;
import mineopoly_three.tiles.TileType;
import org.junit.Before;
import org.junit.Test;

import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class test {
    MyStrategy myStrategy;
    // The tile used to initialize game board
    TileType[][] tile;
    Map < Point, List< InventoryItem >> itemOnGround;
    ItemType[] allItem= {ItemType.EMERALD, ItemType.RUBY, ItemType.DIAMOND};
    Random random = null;
    Economy economy;

    @Before
    public void setUp(){
        myStrategy = new MyStrategy();
        economy = new Economy(allItem);
        itemOnGround = new HashMap<>();
        // Create a 4 * 4 board and initialize an empty itemOnGround map for all tests below
        tile = new TileType[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tile[i][j] = TileType.EMPTY;
                itemOnGround.put(new Point(i, j), Collections.emptyList());
            }
        }
        tile[1][0] = TileType.RESOURCE_RUBY;
        tile[0][2] = TileType.RESOURCE_DIAMOND;
        tile[3][1] = TileType.RESOURCE_EMERALD;
        tile[3][3] = TileType.RED_MARKET;
        tile[3][2] = TileType.BLUE_MARKET;
        tile[2][0] = TileType.RECHARGE;
        tile[2][1] = TileType.RECHARGE;
    }

    // Get Diamond for its highest step value
    @Test
    public void testGetHighestValueStonePositionNormal() {
        Point currentLocation = new Point(0,0);
        Point otherLocation = new Point(2, 2);
        PlayerBoardView board = new PlayerBoardView(tile, itemOnGround, currentLocation, otherLocation, 0);
        myStrategy.initialize(4, 5, 100, 100,
                board, currentLocation, true, random);
        Point expected = calculatePosition(new Point(3, 1), 4);
        assertEquals(expected, myStrategy.getHighestValueStonePosition(board, currentLocation, economy));
    }

    // Should stay in current location if no stones left
    @Test
    public void testGetHighestValueStonePositionEmpty() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tile[i][j] = TileType.EMPTY;
            }
        }
        tile[3][3] = TileType.RED_MARKET;
        tile[3][2] = TileType.BLUE_MARKET;
        tile[3][0] = TileType.RECHARGE;
        Map < Point, List< InventoryItem >> map = new HashMap<>();
        Point currentLocation = new Point(0,0);
        Point otherLocation = new Point(2, 2);
        PlayerBoardView board = new PlayerBoardView(tile, map, currentLocation, otherLocation, 0);
        myStrategy.initialize(4, 5, 100, 100,
                board, currentLocation, true, random);
        Point expected = currentLocation;
        assertEquals(expected, myStrategy.getHighestValueStonePosition(board, currentLocation, economy));
    }

    // Attempt to go to (-1, -1) will head the player back to the charger tile
    @Test
    public void testGetNextStepInvalidMove() {
        Point currentLocation = new Point(0,0);
        Point otherLocation = new Point(2, 2);
        PlayerBoardView board = new PlayerBoardView(tile, itemOnGround, currentLocation, otherLocation, 0);
        myStrategy.initialize(4, 5, 100, 100,
                board, currentLocation, true, random);
        // Invalid move down
        // Should return to market position due to my strategy
        Point targetLocation = new Point(-1, -1);
        TurnAction expected = TurnAction.MOVE_UP;
        assertEquals(expected, myStrategy.getNextStep(board, currentLocation, targetLocation,
                null, economy, true));
    }

    @Test
    public void testGetNextStepMoveLeft() {
        Point currentLocation = new Point(1,0);
        Point otherLocation = new Point(2, 2);
        PlayerBoardView board = new PlayerBoardView(tile, itemOnGround, currentLocation, otherLocation, 0);
        myStrategy.initialize(4, 5, 100, 100,
                board, currentLocation, true, random);
        Point targetLocation = new Point(0, 0);
        TurnAction expected = TurnAction.MOVE_LEFT;
        assertEquals(expected, myStrategy.getNextStep(board, currentLocation, targetLocation,
                null, economy, true));
    }

    // Attempt to stay in the same tile should return null (for collision or charge)
    @Test
    public void testGetNextStepSameTile() {
        Point currentLocation = new Point(0, 0);
        Point otherLocation = new Point(2, 2);
        PlayerBoardView board = new PlayerBoardView(tile, itemOnGround, currentLocation, otherLocation, 0);
        myStrategy.initialize(4, 5, 100, 100,
                board, currentLocation, true, random);
        Point targetLocation = new Point(0, 0);
        TurnAction expected = null;
        assertEquals(expected, myStrategy.getNextStep(board, currentLocation, targetLocation,
                null, economy, true));
    }

    // No charge left and currently on a charger spot, should do nothing and stay at the nearest charge
    @Test
    public void testGetTurnActionNoCharge() {
        // Already at charger
        Point currentLocation = calculatePosition(new Point(2,0), 4);
        Point otherLocation = new Point(2, 2);
        PlayerBoardView board = new PlayerBoardView(tile, itemOnGround, currentLocation, otherLocation, 0);
        myStrategy.initialize(4, 20, 100, 100,
                board, currentLocation, true, random);
        TurnAction expected = null;
        assertEquals(expected, myStrategy.getTurnAction(board, economy,2, true));
    }

    // The robot should head to the nearest market when inventory is full
    @Test
    public void testGetTurnActionInventoryFull() {
        Point currentLocation = new Point(0, 0);
        Point otherLocation = new Point(2, 2);
        PlayerBoardView board = new PlayerBoardView(tile, itemOnGround, currentLocation, otherLocation, 0);
        myStrategy.initialize(4, 2, 20, 100,
                board, currentLocation, true, random);
        myStrategy.onReceiveItem(new InventoryItem(ItemType.DIAMOND));
        myStrategy.onReceiveItem(new InventoryItem(ItemType.DIAMOND));
        TurnAction expected = TurnAction.MOVE_RIGHT;
        assertEquals(expected, myStrategy.getTurnAction(board, economy,20, true));

    }

    // When there is a stone in current location, pick it up
    @Test
    public void testGetTurnActionPickUp() {
        Point currentLocation = new Point(0, 0);
        Point otherLocation = new Point(2, 2);
        itemOnGround.put(currentLocation, Arrays.asList(new InventoryItem(ItemType.DIAMOND)));
        PlayerBoardView board = new PlayerBoardView(tile, itemOnGround, currentLocation, otherLocation, 0);
        myStrategy.initialize(4, 2, 20, 100,
                board, currentLocation, true, random);
        TurnAction expected = TurnAction.PICK_UP_RESOURCE;
        assertEquals(expected, myStrategy.getTurnAction(board, economy,20, true));
    }

    // When current place has a stone, mine it
    @Test
    public void testGetTurnActionMine() {
        // At a ruby position
        Point currentLocation = calculatePosition(new Point(1,0), 4);
        Point otherLocation = new Point(2, 2);
        PlayerBoardView board = new PlayerBoardView(tile, itemOnGround, currentLocation, otherLocation, 0);
        myStrategy.initialize(4, 2, 20, 100,
                board, currentLocation, true, random);
        TurnAction expected = TurnAction.MINE;
        assertEquals(expected, myStrategy.getTurnAction(board, economy,20, true));
    }

    // Test on calling OnSoldItem should clear the inventory list
    @Test
    public void testOnSoldInventory() {
        Point currentLocation = new Point(3, 0);
        Point otherLocation = new Point(2, 2);
        PlayerBoardView board = new PlayerBoardView(tile, itemOnGround, currentLocation, otherLocation, 0);
        myStrategy.initialize(4, 2, 20, 100,
                board, currentLocation, true, random);
        myStrategy.onReceiveItem(new InventoryItem(ItemType.DIAMOND));
        myStrategy.onReceiveItem(new InventoryItem(ItemType.DIAMOND));
        // Not tracking price, just for testing sold item
        myStrategy.onSoldInventory(0);
        assertEquals(0, myStrategy.getInventory().size());
    }

    // Test on calling OnReceiveItem should increase inventory list size by 1
    @Test
    public void testOnReceiveItem() {
        Point currentLocation = new Point(0, 0);
        Point otherLocation = new Point(2, 2);
        PlayerBoardView board = new PlayerBoardView(tile, itemOnGround, currentLocation, otherLocation, 0);
        myStrategy.initialize(4, 2, 20, 100,
                board, currentLocation, true, random);
        myStrategy.onReceiveItem(new InventoryItem(ItemType.DIAMOND));
        assertEquals(1, myStrategy.getInventory().size());

    }

    // Helper function to convert coordinate into Point position in board
    private Point calculatePosition(Point coordinate, int boardSize) {
        return new Point(coordinate.y, boardSize - coordinate.x - 1);
    }

}
