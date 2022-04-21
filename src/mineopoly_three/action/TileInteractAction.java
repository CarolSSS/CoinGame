package mineopoly_three.action;

import mineopoly_three.game.GameBoard;
import mineopoly_three.game.MinePlayer;
import mineopoly_three.item.InventoryItem;
import mineopoly_three.tiles.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * A TileInteractAction represents any Action that is performed on a Tile that may change
 *  properties of the Tile after being performed. Current examples of this are mining a tile
 *  or picking up an item on the tile.
 */
public class TileInteractAction extends Action {
    @Override
    public void performAction(GameBoard board, MinePlayer player, TurnAction action) {
        Tile currentPlayerTile = player.getCurrentTile();
        Tile tileAfterAction = currentPlayerTile.interact(player, action);

        if (tileAfterAction != currentPlayerTile) {
            // Tile has changed as a result of the action
            board.setTileAtTileLocation(tileAfterAction);
            player.setCurrentTile(tileAfterAction);
        }

        board.trackItemsOnPoint(tileAfterAction.getLocation(), tileAfterAction.getItemsOnTile());
    }
}
