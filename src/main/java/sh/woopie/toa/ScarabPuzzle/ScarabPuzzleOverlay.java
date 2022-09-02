package sh.woopie.toa.ScarabPuzzle;

import sh.woopie.toa.RoomOverlay;
import sh.woopie.toa.TombsOfAmascutConfig;

import net.runelite.api.Client;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayLayer;

import java.awt.*;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;


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

        if(config.sequencePuzzle()) {
            for(WorldPoint point : scarabPuzzle.getPuzzleRememberPath().keySet())
            {
                LocalPoint lp = LocalPoint.fromWorld(client, point);
                Integer overlayTxt = scarabPuzzle.getPuzzleRememberPath().get(point);

                if(overlayTxt != null && lp != null) {
                    Point textPoint = Perspective.getCanvasTextLocation(client, graphics, lp, String.valueOf(overlayTxt), 0);

                    if (textPoint != null) {
                        drawTile(graphics, point, config.scarabPuzzleTileMarkerColor(), 1, 255, 0);
                        renderTextLocation(graphics, String.valueOf(overlayTxt), Color.WHITE, textPoint);
                    }
                }
            }
        }

        if(config.numberPuzzle())
        {
            for(Integer i : scarabPuzzle.getPuzzlePath()) {
                WorldPoint point = scarabPuzzle.getPuzzleTiles().get(i);

                drawTile(graphics, point, config.scarabPuzzleTileMarkerColor(), 1, 255, 10);
            }
        }

        if(config.memoryPuzzle())
        {
            for(WorldPoint point : scarabPuzzle.getPuzzleMemory().keySet())
            {
                LocalPoint lp = LocalPoint.fromWorld(client, point);
                String overlayTxt = scarabPuzzle.getPuzzleMemory().get(point);

                if(overlayTxt != null && lp != null) {
                    Point textPoint = Perspective.getCanvasTextLocation(client, graphics, lp, overlayTxt, 0);

                    if (textPoint != null) {
                        renderTextLocation(graphics, overlayTxt, Color.WHITE, textPoint);
                    }
                }
            }
        }
        return null;
    }
}
