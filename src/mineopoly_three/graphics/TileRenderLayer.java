package mineopoly_three.graphics;

/**
 * This enum implicitly specifies the order in which image overlays are rendered.
 * Because LAYER_PLAYER is last, the player will be the thing rendered on top of everything else.
 */
public enum TileRenderLayer {
    LAYER_BASE,
    LAYER_TEXTURING,
    LAYER_RESOURCE_TYPE,
    LAYER_CRACK,
    LAYER_BUILDING,
    LAYER_RECHARGE,
    LAYER_AUTOMINER,
    LAYER_ITEM,
    LAYER_PLAYER
}
