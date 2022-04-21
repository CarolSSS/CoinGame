package mineopoly_three.tiles;

import mineopoly_three.game.MinePlayer;
import mineopoly_three.action.TurnAction;

import java.awt.*;

public class EmptyTile extends StoneTile {

    public EmptyTile(Point tileLocation) {
        super(tileLocation);
    }

    @Override
    public TileType getType() {
        return TileType.EMPTY;
    }

    @Override
    public Tile interact(MinePlayer playerOnTile, TurnAction actionOnTile) {
        super.interact(playerOnTile, actionOnTile);

        if (actionOnTile == TurnAction.MINE) {
            // There's really no reason to do this, but sure you can mine empty tiles
            CrackedTile tile = new CrackedTile(location);
            tile.itemsOnTile.addAll(itemsOnTile);
            return tile;
        }

        return this;
    }
}
