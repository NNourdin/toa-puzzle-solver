package sh.woopie.toa.ScarabPuzzle.LightPuzzle;

import java.util.Map;
import net.runelite.api.GameObject;
import net.runelite.api.Tile;

public class LightPuzzleState
{
	private final double[] state = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
	private static final int TILE_LIGHT_ON = 45384;

	public double[] getState()
	{
		return state;
	}

	public void updateState(Map<Tile, Integer> tiles)
	{
		for (Tile tile : tiles.keySet())
		{
			for (GameObject gameObject : tile.getGameObjects())
			{
				if (gameObject == null)
				{
					continue;
				}

				if (gameObject.getId() == TILE_LIGHT_ON)
				{
					state[tiles.get(tile)] = 0;
				}
				else
				{
					state[tiles.get(tile)] = 1;
				}
			}
		}
	}
}
