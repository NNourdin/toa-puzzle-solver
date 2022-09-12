package sh.woopie.toa.ScarabPuzzle;

import net.runelite.api.hooks.Callbacks;
import sh.woopie.toa.TombsOfAmascutPlugin;
import sh.woopie.toa.TombsOfAmascutConfig;
import sh.woopie.toa.Room;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.hooks.DrawCallbacks;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import static net.runelite.api.ChatMessageType.GAMEMESSAGE;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;

@Slf4j
public class ScarabPuzzle extends Room {
    @Inject
    private Client client;

    @Inject
    private ScarabPuzzleOverlay scarabPuzzleOverlay;

    @Inject
    protected ScarabPuzzle(TombsOfAmascutPlugin plugin, TombsOfAmascutConfig config) {
        super(plugin, config);
    }

    private int rememberPathSize = 1;
    private int puzzleHint = 0;

    public static final long[] denominatorList = {0, 0, 0, 0, 0, 0, 0, 0};
    public static final double[] tileStates = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
    public static final double[] lightPuzzleSolution = {0, 0, 0, 0, 0, 0, 0, 0};

    // Light puzzle matrix
    public static final double[][] coeffMatrix = {
            {1, 1, 0, 1, 0, 0, 0, 0}, // a11
            {1, 1, 1, 0, 0, 0, 0, 0}, // a12
            {0, 1, 1, 0, 1, 0, 0, 0}, // a13
            {1, 0, 0, 1, 0, 1, 0, 0}, // a21
            {0, 0, 1, 0, 1, 0, 0, 1}, // a23
            {0, 0, 0, 1, 0, 1, 1, 0}, // a31
            {0, 0, 0, 0, 0, 1, 1, 1}, // a32
            {0, 0, 0, 0, 1, 0, 1, 1}  // a33
    };

    // Start location of the light puzzle, Scene XY
    public static final int[][] lightPuzzleOffsets = {
            {40, 44}, // right_bottom
            {40, 56}, // left_bottom
            {57, 44}, // right_top
            {57, 56}, // left_top
    };

    public static final Integer SCARAB_PUZZLE_REGION = 14162;

    @Getter
    private final Hashtable<WorldPoint, Integer> puzzleRememberPath = new Hashtable<WorldPoint, Integer>();

    @Getter
    private final List<Integer> puzzlePath = new ArrayList<>();

    @Getter
    private final Hashtable<WorldPoint, String> puzzleMemory = new Hashtable<WorldPoint, String>();

    @Getter
    private final Map<Integer, WorldPoint> puzzleTiles = new HashMap<>();

    @Getter
    private final Map<Tile, Integer> lightPuzzleTiles = new HashMap<>();

    @Override
    public void load() {
        overlayManager.add(scarabPuzzleOverlay);
    }

    @Override
    public void unload() {
        rememberPathSize = 1;
        puzzlePath.clear();
        puzzleMemory.clear();
        puzzleRememberPath.clear();
        lightPuzzleTiles.clear();
        overlayManager.remove(scarabPuzzleOverlay);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if(inRoomRegion(SCARAB_PUZZLE_REGION)) {
            defineLightPuzzleLocation();
            setLightPuzzleTileStates();
            solveLightPuzzle();
        } else {
            rememberPathSize = 1;
            puzzlePath.clear();
            puzzleMemory.clear();
            puzzleRememberPath.clear();
            lightPuzzleTiles.clear();
            puzzleTiles.clear();
        }
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event)
    {
        final GroundObject groundObject = event.getGroundObject();

        switch(groundObject.getId())
        {
            // Reveal memory puzzle
            case 45356:
                puzzleMemory.put(groundObject.getWorldLocation(), "Line");
                break;
            case 45357:
                puzzleMemory.put(groundObject.getWorldLocation(), "Knives");
                break;
            case 45358:
                puzzleMemory.put(groundObject.getWorldLocation(), "Hook");
                break;
            case 45359:
                puzzleMemory.put(groundObject.getWorldLocation(), "Diamond");
                break;
            case 45360:
                puzzleMemory.put(groundObject.getWorldLocation(), "Hand");
                break;
            case 45361:
                puzzleMemory.put(groundObject.getWorldLocation(), "Star");
                break;
            case 45362:
                puzzleMemory.put(groundObject.getWorldLocation(), "Bird");
                break;
            case 45363:
                puzzleMemory.put(groundObject.getWorldLocation(), "W");
                break;
            case 45364:
                puzzleMemory.put(groundObject.getWorldLocation(), "Boots");
                break;
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        final GameObject gameObject = event.getGameObject();

        switch(gameObject.getId()) {
            case ObjectID.PRESSURE_PLATE_45341:
                // Clear the puzzle
                if(rememberPathSize == 6)
                {
                    rememberPathSize = 1;
                    puzzleRememberPath.clear();
                }

                puzzleRememberPath.put(gameObject.getWorldLocation(), rememberPathSize);
                rememberPathSize++;
                break;
            case ObjectID.ANCIENT_TABLET:
                if(puzzleTiles.isEmpty())
                {
                    if(gameObject.getWorldLocation().equals(WorldPoint.fromScene(client,51, 44, 0))) {
                        defineNumberPuzzleTiles(53,44); // top right
                    } else if (gameObject.getWorldLocation().equals(WorldPoint.fromScene(client,51, 56, 0))) {
                        defineNumberPuzzleTiles(53,56); // top left
                    } else if (gameObject.getWorldLocation().equals(WorldPoint.fromScene(client,34, 44, 0))) {
                        defineNumberPuzzleTiles(36,44); // bottom right
                    } else if (gameObject.getWorldLocation().equals(WorldPoint.fromScene(client,34, 56, 0))) {
                        defineNumberPuzzleTiles(36,56); // bottom left
                    }
                }
                break;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
        if(!inRoomRegion(SCARAB_PUZZLE_REGION))
        {
            return;
        }

        if(chatMessage.getType() == GAMEMESSAGE && chatMessage.getMessage().contains("The number")) {
            Pattern pattern = Pattern.compile("(?<=>)(.*)(?=<)");
            Matcher matcher = pattern.matcher(chatMessage.getMessage());

            if(matcher.find()) {
                puzzleHint = Integer.parseInt(matcher.group(1));
            }

            createPuzzlePath(puzzleHint);
        }
    }

    public void solveLightPuzzle()
    {
        RealMatrix coefficients = new Array2DRowRealMatrix(coeffMatrix, false);
        DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();

        RealVector constants = new ArrayRealVector(tileStates, false);
        RealVector solved = solver.solve((constants));

        double[] solution = solved.toArray();

        for(int i = 0; i < solution.length; i++) {
            denominatorList[i] = new Fraction(solution[i]).getDenominator();
        }

        // Get the greatest common divisor from all denominators
        long gcd = gcdArr(denominatorList);

        // Rebuild our solution and determine which tiles need to be flipped, odd ones.
        for(int i = 0; i < solution.length; i++)
        {
            lightPuzzleSolution[i] = Math.rint(solution[i] * gcd % 2);
            lightPuzzleSolution[i] = Math.abs(lightPuzzleSolution[i]);
        }
    }

    public void defineLightPuzzleLocation()
    {
        if(!lightPuzzleTiles.isEmpty())
        {
            return;
        }

        final Scene scene = client.getScene();
        Tile[][][] sceneTiles = scene.getTiles();

        for(int i = 0; i < lightPuzzleOffsets.length; i++) {
            int x = lightPuzzleOffsets[i][0];
            int y = lightPuzzleOffsets[i][1];

            if(sceneTiles[0][x][y].getGroundObject().getId() == 45344) {
                defineLightPuzzleTiles(sceneTiles, x, y);
            }
        }
    }

    public void setLightPuzzleTileStates()
    {
        for(Tile tile : lightPuzzleTiles.keySet())
        {
            boolean light_found = false;

            for(GameObject gameObject : tile.getGameObjects())
            {
                if(gameObject != null)
                {
                    if(gameObject.getId() == 45384)
                    {
                        tileStates[lightPuzzleTiles.get(tile)] = 0;
                        light_found = true;
                    } else if(gameObject.getId() == 29733) {
                        tileStates[lightPuzzleTiles.get(tile)] = 1;
                        light_found = true;
                    }
                }
            }
            // We need to do this here, since light_off GameObject only spawns on tile event
            if(!light_found)
            {
                tileStates[lightPuzzleTiles.get(tile)] = 1;
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
                puzzleTiles.put(tile_index, WorldPoint.fromScene(client, (x + i), (y - n),0));
                tile_index++;
            }
        }
    }

    public void defineLightPuzzleTiles(Tile[][][] tiles, int x, int y)
    {
        int tile_index = 0;
        boolean skipped = false;

        for(int i = 0; i < 3; i++)
        {
            for(int n = 0; n < 3; n++)
            {
                if(tile_index == 4 && !skipped)
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

    public void createPuzzlePath(int puzzleHint)
    {
        switch(puzzleHint) {
            case 10:
                Collections.addAll(puzzlePath, 5, 10);
                break;
            case 11:
                Collections.addAll(puzzlePath, 4, 9, 13);
                break;
            case 12:
                Collections.addAll(puzzlePath, 1, 6);
                break;
            case 13:
                Collections.addAll(puzzlePath, 2, 7);
                break;
            case 14:
                Collections.addAll(puzzlePath, 3, 8);
                break;
            case 15:
                Collections.addAll(puzzlePath, 4, 9, 14);
                break;
            case 16:
                Collections.addAll(puzzlePath, 4, 8, 12);
                break;
            case 17:
                Collections.addAll(puzzlePath, 3, 8, 13);
                break;
            case 18:
                Collections.addAll(puzzlePath, 2, 7, 12, 17);
                break;
            case 19:
                Collections.addAll(puzzlePath, 2, 7, 12, 17, 22);
                break;
            case 20:
                Collections.addAll(puzzlePath, 2, 8, 14);
                break;
            case 21:
                Collections.addAll(puzzlePath, 4, 9, 14, 19, 24);
                break;
            case 22:
                Collections.addAll(puzzlePath, 5, 10, 15, 20, 25);
                break;
            case 23:
                Collections.addAll(puzzlePath, 1, 7, 13, 19, 25);
                break;
            case 24:
                Collections.addAll(puzzlePath, 2, 8, 14, 20);
                break;
            case 25:
                Collections.addAll(puzzlePath, 3, 8, 13, 18);
                break;
            case 26:
                Collections.addAll(puzzlePath, 5, 10, 15, 20, 25, 24);
                break;
            case 27:
                Collections.addAll(puzzlePath, 3, 8, 13, 18, 19);
                break;
            case 28:
                Collections.addAll(puzzlePath, 2, 7, 12, 17, 22, 21);
                break;
            case 29:
                Collections.addAll(puzzlePath, 3, 8, 13, 18, 24);
                break;
            case 30:
                Collections.addAll(puzzlePath, 3, 8, 13, 18, 23);
                break;
            case 31:
                Collections.addAll(puzzlePath, 1, 2, 3, 4, 5);
                break;
            case 32:
                Collections.addAll(puzzlePath, 5, 10, 15, 20, 25, 24, 23, 22);
                break;
            case 33:
                Collections.addAll(puzzlePath, 1, 2, 3, 4, 5, 9);
                break;
            case 34:
                Collections.addAll(puzzlePath, 3, 8, 13, 18, 23, 24);
                break;
            case 35:
                Collections.addAll(puzzlePath, 1, 6, 7, 8, 9, 10);
                break;
            case 36:
                Collections.addAll(puzzlePath, 1, 6, 11, 16, 21, 22, 23);
                break;
            case 37:
                Collections.addAll(puzzlePath, 3, 8, 13, 18, 23, 24, 25);
                break;
            case 38:
                Collections.addAll(puzzlePath, 3, 8, 13, 18, 23, 17, 11);
                break;
            case 39:
                Collections.addAll(puzzlePath, 1, 6, 11, 16, 21, 22, 18);
                break;
            case 40:
                Collections.addAll(puzzlePath, 1, 2, 3, 4, 5, 10);
                break;
            case 41:
                Collections.addAll(puzzlePath, 1, 2, 3, 4, 5, 6, 11);
                break;
            case 42:
                Collections.addAll(puzzlePath, 1, 5, 7, 10, 13, 15, 19, 20, 25);
                break;
            case 43:
                Collections.addAll(puzzlePath, 1, 2, 3, 4, 5, 6, 11, 16);
                break;
            case 44:
                Collections.addAll(puzzlePath, 6, 7, 8, 9, 10, 11, 16, 21);
                break;
            case 45:
                Collections.addAll(puzzlePath, 1, 2, 3, 4, 9, 14, 19, 24);
                break;
            case 46:
                Collections.addAll(puzzlePath, 1, 2, 3, 4, 5, 9, 13, 17, 21);
                break;
            case 47:
                Collections.addAll(puzzlePath, 1, 2, 3, 4, 5, 10, 14);
                break;
            case 48:
                Collections.addAll(puzzlePath, 1, 2, 3, 4, 10, 15, 20);
                break;
            case 49:
                Collections.addAll(puzzlePath, 1, 2, 3, 4, 5, 10, 15, 20);
                break;
        }
    }
    static long gcdArr(long[] arr)
    {
        long result = arr[0];

        for (int i = 1; i < arr.length; i++)
        {
            result = gcd(arr[i], result);
        }

        return result;
    }

    static long gcd(long a, long b)
    {
        if (a == 0)
        {
            return b;
        }
        return gcd(b % a, a);
    }
}