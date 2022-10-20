package sh.woopie.toa.ScarabPuzzle;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import static net.runelite.api.ChatMessageType.GAMEMESSAGE;
import sh.woopie.toa.TombsOfAmascutPlugin;
import sh.woopie.toa.TombsOfAmascutConfig;
import sh.woopie.toa.ScarabPuzzle.LightPuzzle.LightPuzzleState;
import sh.woopie.toa.ScarabPuzzle.LightPuzzle.LightPuzzleSolver;
import sh.woopie.toa.Room;


@Slf4j
public class ScarabPuzzle extends Room
{
	@Inject
	private Client client;

	@Inject
	private ScarabPuzzleOverlay scarabPuzzleOverlay;

	@Inject
	protected ScarabPuzzle(TombsOfAmascutPlugin plugin, TombsOfAmascutConfig config)
	{
		super(plugin, config);
	}

	public static final int SCARAB_PUZZLE_REGION = 14162;
	public static final int SHINING_OBELISK = 11699;

	public static double[] lightPuzzleSolution = {0, 0, 0, 0, 0, 0, 0, 0};

	// Start location of the light puzzle, Scene XY
	private static final int[][] lightPuzzleOffsets = {
		{40, 44}, // right_bottom
		{40, 56}, // left_bottom
		{57, 44}, // right_top
		{57, 56}, // left_top
	};

	@Getter
	private final List<Integer> numberPuzzleSolution = new ArrayList<>();

	@Getter
	private final Hashtable<WorldPoint, String> puzzleMemorySolution = new Hashtable<WorldPoint, String>();

	@Getter
	private final List<WorldPoint> sequencePuzzleSolution = new ArrayList<>();

	@Getter
	private final List<WorldPoint> obelisks = new ArrayList<>();

	@Getter
	private final Map<Integer, WorldPoint> numberPuzzleTiles = new HashMap<>();

	@Getter
	private final Map<Tile, Integer> lightPuzzleTiles = new HashMap<>();

	@Override
	public void load()
	{
		overlayManager.add(scarabPuzzleOverlay);
	}

	@Override
	public void unload()
	{
		puzzleMemorySolution.clear();
		numberPuzzleSolution.clear();
		sequencePuzzleSolution.clear();
		lightPuzzleTiles.clear();
		numberPuzzleTiles.clear();
		obelisks.clear();
		overlayManager.remove(scarabPuzzleOverlay);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!inRoomRegion(SCARAB_PUZZLE_REGION))
		{
			return;
		}

		defineLightPuzzleLocation();

		LightPuzzleState lightPuzzleState = new LightPuzzleState();
		LightPuzzleSolver lightPuzzleSolver = new LightPuzzleSolver();

		lightPuzzleState.updateState(lightPuzzleTiles);
		lightPuzzleSolution = lightPuzzleSolver.solve(lightPuzzleState.getState());
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		if (event.getNpc().getId() == SHINING_OBELISK)
		{
			boolean onRecord = false;

			for (WorldPoint point : obelisks)
			{
				if (point.equals(event.getNpc().getWorldLocation()))
				{
					onRecord = true;
					break;
				}
			}

			if (!onRecord)
			{
				obelisks.add(event.getNpc().getWorldLocation());
			}
		}
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		final GroundObject groundObject = event.getGroundObject();

		switch (groundObject.getId())
		{
			// Reveal memory puzzle
			case 45356:
				puzzleMemorySolution.put(groundObject.getWorldLocation(), "Line");
				break;
			case 45357:
				puzzleMemorySolution.put(groundObject.getWorldLocation(), "Knives");
				break;
			case 45358:
				puzzleMemorySolution.put(groundObject.getWorldLocation(), "Hook");
				break;
			case 45359:
				puzzleMemorySolution.put(groundObject.getWorldLocation(), "Diamond");
				break;
			case 45360:
				puzzleMemorySolution.put(groundObject.getWorldLocation(), "Hand");
				break;
			case 45361:
				puzzleMemorySolution.put(groundObject.getWorldLocation(), "Star");
				break;
			case 45362:
				puzzleMemorySolution.put(groundObject.getWorldLocation(), "Bird");
				break;
			case 45363:
				puzzleMemorySolution.put(groundObject.getWorldLocation(), "W");
				break;
			case 45364:
				puzzleMemorySolution.put(groundObject.getWorldLocation(), "Boots");
				break;
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();
		int objectId = gameObject.getId();

		if (objectId == ObjectID.PRESSURE_PLATE_45341)
		{
			if (sequencePuzzleSolution.size() == 5)
			{
				sequencePuzzleSolution.clear();
			}
			sequencePuzzleSolution.add(gameObject.getWorldLocation());
		}

		if (objectId == ObjectID.ANCIENT_TABLET)
		{
			if (gameObject.getWorldLocation().equals(WorldPoint.fromScene(client, 51, 44, 0)))
			{
				defineNumberPuzzleTiles(53, 44); // top right
			}
			else if (gameObject.getWorldLocation().equals(WorldPoint.fromScene(client, 51, 56, 0)))
			{
				defineNumberPuzzleTiles(53, 56); // top left
			}
			else if (gameObject.getWorldLocation().equals(WorldPoint.fromScene(client, 34, 44, 0)))
			{
				defineNumberPuzzleTiles(36, 44); // bottom right
			}
			else if (gameObject.getWorldLocation().equals(WorldPoint.fromScene(client, 34, 56, 0)))
			{
				defineNumberPuzzleTiles(36, 56); // bottom left
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (!inRoomRegion(SCARAB_PUZZLE_REGION) || chatMessage.getType() != GAMEMESSAGE)
		{
			return;
		}

		if (chatMessage.getMessage().contains("The number"))
		{
			Pattern pattern = Pattern.compile("(?<=>)(.*)(?=<)");
			Matcher matcher = pattern.matcher(chatMessage.getMessage());

			if (matcher.find())
			{
				int puzzleHint = Integer.parseInt(matcher.group(1));
				createNumberPuzzleSolution(puzzleHint);
			}
		}

		if (chatMessage.getMessage().contains("completed!"))
		{
			clear(true);
		}
	}

	public void defineLightPuzzleLocation()
	{
		final Scene scene = client.getScene();
		Tile[][][] sceneTiles = scene.getTiles();

		for (int[] lightPuzzleOffset : lightPuzzleOffsets)
		{
			int x = lightPuzzleOffset[0];
			int y = lightPuzzleOffset[1];

			if (sceneTiles[0][x][y].getGroundObject().getId() == ObjectID.PRESSURE_PLATE_45344)
			{
				defineLightPuzzleTiles(sceneTiles, x, y);
			}
		}
	}

	public void defineNumberPuzzleTiles(int x, int y)
	{
		int tile_index = 1;

		for (int i = 0; i < 5; i++)
		{
			for (int n = 0; n < 5; n++)
			{
				numberPuzzleTiles.put(tile_index, WorldPoint.fromScene(client, (x + i), (y - n), 0));
				tile_index++;
			}
		}
	}

	public void defineLightPuzzleTiles(Tile[][][] tiles, int x, int y)
	{
		int tile_index = 0;
		boolean skipped = false;

		for (int i = 0; i < 3; i++)
		{
			for (int n = 0; n < 3; n++)
			{
				if (tile_index == 4 && !skipped)
				{
					skipped = true;
					continue;
				}
				lightPuzzleTiles.put(tiles[0][x][y - (n * 2)], tile_index);
				tile_index++;
			}
			x = x - 2;
		}
	}

	public void clear(boolean onlySolutions)
	{
		numberPuzzleSolution.clear();
		sequencePuzzleSolution.clear();
		obelisks.clear();

		if (!onlySolutions)
		{
			lightPuzzleTiles.clear();
			numberPuzzleTiles.clear();
			puzzleMemorySolution.clear();
		}
	}

	public void createNumberPuzzleSolution(int puzzleHint)
	{
		switch (puzzleHint)
		{
			case 20:
				Collections.addAll(numberPuzzleSolution, 2, 8, 14);
				break;
			case 21:
				Collections.addAll(numberPuzzleSolution, 4, 9, 14, 19, 24);
				break;
			case 22:
				Collections.addAll(numberPuzzleSolution, 5, 10, 15, 20, 25);
				break;
			case 23:
				Collections.addAll(numberPuzzleSolution, 1, 7, 13, 19, 25);
				break;
			case 24:
				Collections.addAll(numberPuzzleSolution, 2, 8, 14, 20);
				break;
			case 25:
				Collections.addAll(numberPuzzleSolution, 3, 8, 13, 18);
				break;
			case 26:
				Collections.addAll(numberPuzzleSolution, 5, 10, 15, 20, 25, 24);
				break;
			case 27:
				Collections.addAll(numberPuzzleSolution, 3, 8, 13, 18, 19);
				break;
			case 28:
				Collections.addAll(numberPuzzleSolution, 2, 7, 12, 17, 22, 21);
				break;
			case 29:
				Collections.addAll(numberPuzzleSolution, 3, 8, 13, 18, 24);
				break;
			case 30:
				Collections.addAll(numberPuzzleSolution, 3, 8, 13, 18, 23);
				break;
			case 31:
				Collections.addAll(numberPuzzleSolution, 1, 2, 3, 4, 5);
				break;
			case 32:
				Collections.addAll(numberPuzzleSolution, 5, 10, 15, 20, 25, 24, 23, 22);
				break;
			case 33:
				Collections.addAll(numberPuzzleSolution, 1, 2, 3, 4, 5, 9);
				break;
			case 34:
				Collections.addAll(numberPuzzleSolution, 3, 8, 13, 18, 23, 24);
				break;
			case 35:
				Collections.addAll(numberPuzzleSolution, 1, 6, 7, 8, 9, 10);
				break;
			case 36:
				Collections.addAll(numberPuzzleSolution, 1, 6, 11, 16, 21, 22, 23);
				break;
			case 37:
				Collections.addAll(numberPuzzleSolution, 3, 8, 13, 18, 23, 24, 25);
				break;
			case 38:
				Collections.addAll(numberPuzzleSolution, 3, 8, 13, 18, 23, 17, 11);
				break;
			case 39:
				Collections.addAll(numberPuzzleSolution, 1, 6, 11, 16, 21, 22, 18);
				break;
			case 40:
				Collections.addAll(numberPuzzleSolution, 1, 2, 3, 4, 5, 10);
				break;
			case 41:
				Collections.addAll(numberPuzzleSolution, 1, 2, 3, 4, 5, 6, 11);
				break;
			case 42:
				Collections.addAll(numberPuzzleSolution, 1, 5, 7, 10, 13, 15, 19, 20, 25);
				break;
			case 43:
				Collections.addAll(numberPuzzleSolution, 1, 2, 3, 4, 5, 6, 11, 16);
				break;
			case 44:
				Collections.addAll(numberPuzzleSolution, 6, 7, 8, 9, 10, 11, 16, 21);
				break;
			case 45:
				Collections.addAll(numberPuzzleSolution, 1, 2, 3, 4, 9, 14, 19, 24);
				break;
			case 46:
				Collections.addAll(numberPuzzleSolution, 1, 2, 3, 4, 5, 9, 13, 17, 21);
				break;
			case 47:
				Collections.addAll(numberPuzzleSolution, 1, 2, 3, 4, 5, 10, 14);
				break;
			case 48:
				Collections.addAll(numberPuzzleSolution, 1, 2, 3, 4, 10, 15, 20);
				break;
			case 49:
				Collections.addAll(numberPuzzleSolution, 1, 2, 3, 4, 5, 10, 15, 20);
				break;
		}
	}
}