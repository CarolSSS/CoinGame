package mineopoly_three.tiles;

import mineopoly_three.graphics.ImageManager;
import mineopoly_three.graphics.TileRenderLayer;

import java.awt.*;

public class RechargeTile extends StoneTile {
    private static final float RECHARGE_PERCENT_PER_TURN = 0.1f;

    public RechargeTile(Point tileLocation) {
        super(tileLocation);
    }

    @Override
    public TileType getType() {
        return TileType.RECHARGE;
    }

    @Override
    public void update() {
        if (super.playerOnTile != null) {
            super.playerOnTile.rechargeEnergy(RECHARGE_PERCENT_PER_TURN);
        }
    }

    @Override
    protected Image[] getImageOverlays(ImageManager imageManager) {
        Image[] imageOverlays = super.getImageOverlays(imageManager);
        // Add charging station on top of tile
        Image chargingStationImage = imageManager.getScaledImage("charging_station");
        imageOverlays[TileRenderLayer.LAYER_RECHARGE.ordinal()] = chargingStationImage;
        return imageOverlays;
    }
}
