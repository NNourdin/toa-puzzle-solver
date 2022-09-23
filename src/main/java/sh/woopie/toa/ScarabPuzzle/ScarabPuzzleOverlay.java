package sh.woopie.toa.ScarabPuzzle;

import java.awt.*;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import net.runelite.api.Client;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import sh.woopie.toa.RoomOverlay;
import sh.woopie.toa.TombsOfAmascutConfig;


@Slf4j
public class ScarabPuzzleOverlay extends RoomOverlay {
    @Inject
    private ScarabPuzzle scarabPuzzle;

    @Inject
    private Client client;

    @Inject
    protected ScarabPuzzleOverlay(TombsOfAmascutConfig config)
    {
        super(config);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if(!ArrayUtils.contains(client.getMapRegions(), ScarabPuzzle.SCARAB_PUZZLE_REGION))
        {
            return null;
        }

		if(config.sequencePuzzle())
		{
			for(int i = 0; i < scarabPuzzle.getSequencePuzzleSolution().size(); i++)
			{
				WorldPoint point = scarabPuzzle.getSequencePuzzleSolution().get(i);
				drawTileWithText(graphics, point, String.valueOf(i + 1), config.scarabPuzzleTileMarkerColor(), 1, 255, 10);
			}
		}

        if(config.numberPuzzle())
        {
            for(Integer i : scarabPuzzle.getNumberPuzzleSolution()) {
                WorldPoint point = scarabPuzzle.getNumberPuzzleTiles().get(i);

                drawTile(graphics, point, config.scarabPuzzleTileMarkerColor(), 1, 255, 10);
            }
        }

        if(config.memoryPuzzle())
        {
            for(WorldPoint point : scarabPuzzle.getPuzzleMemorySolution().keySet())
            {
                LocalPoint lp = LocalPoint.fromWorld(client, point);
                String overlayTxt = scarabPuzzle.getPuzzleMemorySolution().get(point);

                if(overlayTxt != null && lp != null) {
                    Point textPoint = Perspective.getCanvasTextLocation(client, graphics, lp, overlayTxt, 0);

                    if (textPoint != null) {
                        renderTextLocation(graphics, overlayTxt, Color.WHITE, textPoint);
                    }
                }
            }
        }

        if(config.lightPuzzle())
        {
            for (Tile tile : scarabPuzzle.getLightPuzzleTiles().keySet())
            {
                int tile_index = scarabPuzzle.getLightPuzzleTiles().get(tile);

                if(ScarabPuzzle.lightPuzzleSolution[tile_index] == 1) {
                    drawTile(graphics, tile.getWorldLocation(), config.scarabPuzzleTileMarkerColor(), 1, 255, 10);
                }
            }
        }

		if(config.obeliskPuzzle())
		{
			for(int i = 0; i < scarabPuzzle.getObelisks().size(); i++)
			{
				WorldPoint point = scarabPuzzle.getObelisks().get(i);
				drawTileWithText(graphics, point, String.valueOf((i + 1)), config.scarabPuzzleTileMarkerColor(), 1, 255, 10);
			}
		}
        return null;
    }
}
