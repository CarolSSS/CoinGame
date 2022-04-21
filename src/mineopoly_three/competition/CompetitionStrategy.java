package mineopoly_three.competition;

import mineopoly_three.action.TurnAction;
import mineopoly_three.game.Economy;
import mineopoly_three.item.InventoryItem;
import mineopoly_three.item.ItemType;
import mineopoly_three.strategy.MinePlayerStrategy;
import mineopoly_three.strategy.PlayerBoardView;
import mineopoly_three.tiles.TileType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Search nearest each type of stone, going to the one with Max(price/Manhattan distance+mining cost)
 * When energy comes to less than 10%, heading back to the nearest charger until reaching 90%
 * When storage is full, heading to the nearest Market tiles
 */

public class CompetitionStrategy implements MinePlayerStrategy {
    private List<Point> chargerPosition;
    private List<Point> marketPosition;
    // All inventories have been picked up
    private List<InventoryItem> inventory;
    private int boardSize;
    private int maxInventorySize;
    private boolean redPlayer;
    // The flag to decide whether current robot needs charging
    private boolean chargeFlag;
    private int maxCharge;

    /**
     * This method is called before game to initialize all items.
     * @param boardSize The length and width of the square game board
     * @param maxInventorySize The maximum number of items that your player can carry at one time
     * @param maxCharge The amount of charge your robot starts with (number of tile moves before needing to recharge)
     * @param winningScore The first player to reach this score wins the round
     * @param startingBoard A view of the GameBoard at the start of the game. You can use this to pre-compute fixed
     *                       information, like the locations of market or recharge tiles
     * @param startTileLocation A Point representing your starting location in (x, y) coordinates
     *                              (0, 0) is the bottom left and (boardSize - 1, boardSize - 1) is the top right
     * @param isRedPlayer True if this strategy is the red player, false otherwise
     * @param random A random number generator, if your strategy needs random numbers you should use this.
     */
    @Override
    public void initialize(int boardSize, int maxInventorySize, int maxCharge, int winningScore,
                           PlayerBoardView startingBoard, Point startTileLocation, boolean isRedPlayer, Random random) {
        this.redPlayer = isRedPlayer;
        this.boardSize = boardSize;
        this.maxInventorySize = maxInventorySize;
        this.chargeFlag = false;
        this.maxCharge = maxCharge;
        // Reset all the member variables in initialization stage
        this.chargerPosition = new ArrayList<>();
        this.marketPosition = new ArrayList<>();
        this.inventory = new ArrayList<>();
        setChargerMarketPosition(startingBoard);
    }

    /**
     * This method is called in initializer to set the market and charger position
     * @param boardContent the PlayerBoardView Object refers to the starting board
     */
    private void setChargerMarketPosition(PlayerBoardView boardContent) {
        TileType marketTile;
        if (redPlayer) {
            marketTile = TileType.RED_MARKET;
        } else {
            marketTile = TileType.BLUE_MARKET;
        }
        for (int i = 0; i < this.boardSize; i++) {
            for (int j = 0; j < this.boardSize; j++) {
                if (boardContent.getTileTypeAtLocation(i,j).equals(marketTile)) {
                    this.marketPosition.add(new Point(i, j));
                } else if (boardContent.getTileTypeAtLocation(i,j).equals(TileType.RECHARGE)) {
                    this.chargerPosition.add(new Point(i, j));
                }
            }
        }
    }

    /**
     * This method is used to decide the turn direction of current player
     * @param boardView A PlayerBoardView object representing all the information about the board and the other player
     *                   that your strategy is allowed to access
     * @param economy The GameEngine's economy object which holds current prices for resources
     * @param currentCharge The amount of charge your robot has (number of tile moves before needing to recharge)
     * @param isRedTurn For use when two players attempt to move to the same spot on the same turn
     * @return The position to go to.
     */
    @Override
    public TurnAction getTurnAction(PlayerBoardView boardView, Economy economy, int currentCharge, boolean isRedTurn) {
        Point currentPosition = boardView.getYourLocation();
        // Go to charge if the energy comes to less than 10 percent and then keeps on charging until 90% reaches
        if (currentCharge <= maxCharge * 0.1) {
            chargeFlag = true;
            Point togo = getNearestFromList(chargerPosition, currentPosition);
            return getNextStep(boardView, currentPosition, togo, null, economy, isRedTurn);
        }  else if (chargeFlag && currentCharge <= maxCharge * 0.9) {
            return getNextStep(boardView, currentPosition, currentPosition, null, economy, isRedTurn);
        } else if (chargeFlag && currentCharge > maxCharge * 0.9) {
            chargeFlag = false;
        }
        // Cases with enough energy to move and mine
        if (inventory.size() == maxInventorySize) {
            // Full inventory, go to the nearest market and sell everything
            Point togo = getNearestFromList(marketPosition, currentPosition);
            return getNextStep(boardView, currentPosition, togo, null, economy, isRedTurn);
        } else if (boardView.getItemsOnGround().get(currentPosition).size() != 0) {
            // Item on ground waiting to be picked
            return getNextStep(boardView, currentPosition, currentPosition,
                    TurnAction.PICK_UP_RESOURCE, economy, isRedTurn);
        } else {
            // Go to dig the stone with the highest value per step
            Point ToGo = getHighestValueStonePosition(boardView, currentPosition, economy);
            if (ToGo.equals(currentPosition)) {
                return getNextStep(boardView, currentPosition, currentPosition, TurnAction.MINE, economy, isRedTurn);
            } else {
                return getNextStep(boardView, currentPosition, ToGo, null, economy,  isRedTurn);
            }
        }
    }

    /**
     * This method is used to determine what is the next step to go.
     * @param boardView A PlayerBoardView object representing all the information about the board and the other player
     *                  that your strategy is allowed to access
     * @param current The target point (position)
     * @param target The target point to go
     * @param desiredAction The Turn action comes from getTurnAction function
     * @param economy The GameEngine's economy object which holds current prices for resources
     * @param isRedTurn For use when two players attempt to move to the same spot on the same turn
     * @return The desired turn action
     */
    public TurnAction getNextStep(PlayerBoardView boardView, Point current, Point target,
                                   TurnAction desiredAction, Economy economy, boolean isRedTurn) {
        if (target.x >= this.boardSize || target.x < 0 || target.y >= this.boardSize || target.y < 0) {
            // If encountered InValid Location, go back towards the charger position
            target = getNearestFromList(this.chargerPosition, current);
        }
        Point other = boardView.getOtherPlayerLocation();
        // While chasing a target position, the robot will go horizontal first then vertical
        if (current.x < target.x && (isRedTurn || !(other.x == current.x + 1 && other.y == current.y))) {
            return TurnAction.MOVE_RIGHT;
        } else if (current.x > target.x && (isRedTurn || !(other.x == current.x - 1 && other.y == current.y))) {
            return TurnAction.MOVE_LEFT;
        } else if (current.y < target.y && (isRedTurn || !(other.y == current.y + 1 && other.x == current.x))) {
            return TurnAction.MOVE_UP;
        } else if (current.y > target.y && (isRedTurn || !(other.y == current.y - 1 && other.x == current.x))) {
            return TurnAction.MOVE_DOWN;
        } else if (!current.equals(target)) {
            // When other player is on target position, doing null
            return null;
        } else if (current.equals(target) && target.equals(this.marketPosition.get(0))) {
            // When the player is in market position, goes directly to the next stone;
            Point ToGo = getHighestValueStonePosition(boardView, current, economy);
            return getNextStep(boardView, current, ToGo, desiredAction, economy, isRedTurn);
        } else {
            // Needs charging or other cases, return null
            return desiredAction;
        }
    }

    /**
     * This method is used to return the highest value stone position from the board with current position
     * @param boardView A PlayerBoardView object representing all the information about the board and the other player
     *                  that your strategy is allowed to access
     * @param currentPosition A point represent current location
     * @param economy The GameEngine's economy object which holds current prices for resources
     * @return The point with the highest stone value
     */
    public Point getHighestValueStonePosition(PlayerBoardView boardView, Point currentPosition, Economy economy) {
        double [][] stoneValue = new double[this.boardSize][this.boardSize];
        Point currentMaxPoint = new Point(0,0);
        double currentMaxValue = -1;
        for (int i = 0; i < this.boardSize; i++) {
            for (int j = 0; j < this.boardSize; j++) {
                TileType currTile = boardView.getTileTypeAtLocation(i,j);
                // Find position with highest: stone price / (Manhattan Distance + digging step cost) value
                if (currTile.equals(TileType.RESOURCE_RUBY)) {
                    stoneValue[i][j] = getManhattanDistance(currentPosition, new Point(i,j)) + 1;
                    stoneValue[i][j] = economy.getCurrentPrices().get(ItemType.RUBY) / stoneValue[i][j];
                } else if (currTile.equals(TileType.RESOURCE_EMERALD)) {
                    stoneValue[i][j] = getManhattanDistance(currentPosition, new Point(i,j)) + 2;
                    stoneValue[i][j] = economy.getCurrentPrices().get(ItemType.EMERALD) / stoneValue[i][j];
                } else if (currTile.equals(TileType.RESOURCE_DIAMOND)) {
                    stoneValue[i][j] = getManhattanDistance(currentPosition, new Point(i,j)) + 3;
                    stoneValue[i][j] = economy.getCurrentPrices().get(ItemType.DIAMOND) / stoneValue[i][j];
                } else {
                    stoneValue[i][j] = -1;
                }
                if (stoneValue[i][j] > currentMaxValue) {
                    currentMaxValue = stoneValue[i][j];
                    currentMaxPoint = new Point(i,j);
                }
            }
        }
        if (currentMaxValue == -1) {
            // If nothing left, stay at the current position
            currentMaxPoint = currentPosition;
        }
        return currentMaxPoint;
    }

    /**
     * The method return the nearest point in positions list related to input currentPosition.
     * @param positions A list of point represents all positions
     * @param currentPosition A point represent current location
     * @return A point object of the nearest point
     */
    private Point getNearestFromList(List<Point> positions, Point currentPosition) {
        // Set current minimum to the zeroth element first
        Point currentMin = positions.get(0);
        double currentMinDistance = getManhattanDistance(currentPosition, currentMin);
        for (Point position: positions) {
            double currentDistance = getManhattanDistance(currentPosition, position);
            if (currentDistance < currentMinDistance) {
                currentMinDistance = currentDistance;
                currentMin = position;
            }
        }
        return currentMin;
    }

    /**
     * This method return manhattan distance between two points
     * @param currentPosition Point 1
     * @param target Point 2
     * @return A int represents distant
     */
    private double getManhattanDistance(Point currentPosition, Point target) {
        return (Math.abs(currentPosition.x - target.x) + Math.abs(currentPosition.y - target.y));
    }

    /**
     * The method is called while receiving an item
     * @param itemReceived The item received from the player's TurnAction on their last turn
     */
    @Override
    public void onReceiveItem(InventoryItem itemReceived) {
        inventory.add(itemReceived);
    }

    /**
     * The method is called while selling all items
     * @param totalSellPrice The combined sell price for all items in your strategy's inventory
     */
    @Override
    public void onSoldInventory(int totalSellPrice) {
        inventory.clear();
    }

    @Override
    public String getName() {
        return "MyStrategy";
    }

    @Override
    public void endRound(int totalRedPoints, int totalBluePoints) {
    }

    public List<InventoryItem> getInventory() {
        return inventory;
    }
}