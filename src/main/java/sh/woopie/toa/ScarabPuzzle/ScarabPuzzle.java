package sh.woopie.toa.ScarabPuzzle;

import sh.woopie.toa.TombsOfAmascutPlugin;
import sh.woopie.toa.TombsOfAmascutConfig;
import sh.woopie.toa.Room;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
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

@Slf4j
public class ScarabPuzzle extends Room {
    @Inject
    private Client client;

    @Inject
    private ScarabPuzzleOverlay scarabPuzzleOverlay;

    @Inject
    protected ScarabPuzzle(TombsOfAmascutPlugin plugin, TombsOfAmascutConfig config)
    {
        super(plugin, config);
    }

    private int rememberPathSize = 1;
    private int puzzleLocation = 0;
    private int puzzleHint = 0;

    public static final Integer SCARAB_PUZZLE_REGION = 14162;

    public static final Integer PUZZLE_NONE = 0;
    public static final Integer PUZZLE_TOP_RIGHT = 1;
    public static final Integer PUZZLE_TOP_LEFT = 2;
    public static final Integer PUZZLE_BOTTOM_RIGHT = 3;
    public static final Integer PUZZLE_BOTTOM_LEFT = 4;

    @Getter
    private final Hashtable<WorldPoint, Integer> puzzleRememberPath = new Hashtable<WorldPoint, Integer>();

    @Getter
    private final List<Integer> puzzlePath = new ArrayList<>();

    @Getter
    private final Hashtable<WorldPoint, String> puzzleMemory = new Hashtable<WorldPoint, String>();

    @Getter
    private final Map<Integer, WorldPoint> puzzleTiles = new HashMap<>();

    @Override
    public void load()
    {
        overlayManager.add(scarabPuzzleOverlay);
    }

    @Override
    public void unload()
    {
        rememberPathSize = 1;
        puzzleLocation = PUZZLE_NONE;
        puzzlePath.clear();
        puzzleMemory.clear();
        puzzleRememberPath.clear();

        overlayManager.remove(scarabPuzzleOverlay);
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
                if(rememberPathSize == 6) {
                    rememberPathSize = 1;
                    puzzleRememberPath.clear();
                }
                puzzleRememberPath.put(gameObject.getWorldLocation(), rememberPathSize);
                rememberPathSize++;
                break;
            case ObjectID.ANCIENT_TABLET:
                if(puzzleLocation == PUZZLE_NONE) {
                    if(gameObject.getWorldLocation().equals(WorldPoint.fromScene(client,51, 44, 0))) {
                        puzzleLocation = PUZZLE_TOP_RIGHT;
                        definePuzzleTopRight();
                    } else if (gameObject.getWorldLocation().equals(WorldPoint.fromScene(client,51, 56, 0))) {
                        puzzleLocation = PUZZLE_TOP_LEFT;
                        definePuzzleTopLeft();
                    } else if (gameObject.getWorldLocation().equals(WorldPoint.fromScene(client,34, 44, 0))) {
                        puzzleLocation = PUZZLE_BOTTOM_RIGHT;
                        definePuzzleBottomRight();
                    } else if (gameObject.getWorldLocation().equals(WorldPoint.fromScene(client,34, 56, 0))) {
                        puzzleLocation = PUZZLE_BOTTOM_LEFT;
                        definePuzzleBottomLeft();
                    }
                }
                break;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
        if(inRoomRegion(SCARAB_PUZZLE_REGION))
        {
            if(chatMessage.getType() == GAMEMESSAGE && chatMessage.getMessage().contains("The number")) {
                Pattern pattern = Pattern.compile("(?<=>)(.*)(?=<)");
                Matcher matcher = pattern.matcher(chatMessage.getMessage());

                if(matcher.find()) {
                    puzzleHint = Integer.parseInt(matcher.group(1));
                }

                createPuzzlePath(puzzleHint);
            }
        }
    }


    @Subscribe
    public void onGameTick(GameTick event)
    {
        if(!inRoomRegion(SCARAB_PUZZLE_REGION)) {
            rememberPathSize = 1;
            puzzleLocation = PUZZLE_NONE;
            puzzlePath.clear();
            puzzleMemory.clear();
            puzzleRememberPath.clear();
        }
    }

    public void definePuzzleTopLeft()
    {
        puzzleTiles.put(1, WorldPoint.fromScene(client, 53, 56, 0));
        puzzleTiles.put(2, WorldPoint.fromScene(client, 53, 55, 0));
        puzzleTiles.put(3, WorldPoint.fromScene(client, 53, 54, 0));
        puzzleTiles.put(4, WorldPoint.fromScene(client, 53, 53, 0));
        puzzleTiles.put(5, WorldPoint.fromScene(client, 53, 52, 0));

        puzzleTiles.put(6, WorldPoint.fromScene(client, 54, 56, 0));
        puzzleTiles.put(7, WorldPoint.fromScene(client, 54, 55, 0));
        puzzleTiles.put(8, WorldPoint.fromScene(client, 54, 54, 0));
        puzzleTiles.put(9, WorldPoint.fromScene(client, 54, 53, 0));
        puzzleTiles.put(10, WorldPoint.fromScene(client, 54, 52, 0));

        puzzleTiles.put(11, WorldPoint.fromScene(client, 55, 56, 0));
        puzzleTiles.put(12, WorldPoint.fromScene(client, 55, 55, 0));
        puzzleTiles.put(13, WorldPoint.fromScene(client, 55, 54, 0));
        puzzleTiles.put(14,WorldPoint.fromScene(client, 55, 53, 0));
        puzzleTiles.put(15, WorldPoint.fromScene(client, 55, 52, 0));

        puzzleTiles.put(16, WorldPoint.fromScene(client, 56, 56, 0));
        puzzleTiles.put(17, WorldPoint.fromScene(client, 56, 55, 0));
        puzzleTiles.put(18, WorldPoint.fromScene(client, 56, 54, 0));
        puzzleTiles.put(19, WorldPoint.fromScene(client, 56, 53, 0));
        puzzleTiles.put(20, WorldPoint.fromScene(client, 56, 52, 0));

        puzzleTiles.put(21, WorldPoint.fromScene(client, 57, 56, 0));
        puzzleTiles.put(22, WorldPoint.fromScene(client, 57, 55, 0));
        puzzleTiles.put(23, WorldPoint.fromScene(client, 57, 54, 0));
        puzzleTiles.put(24, WorldPoint.fromScene(client, 57, 53, 0));
        puzzleTiles.put(25, WorldPoint.fromScene(client, 57, 52, 0));
    }

    public void definePuzzleBottomRight()
    {
        puzzleTiles.put(1, WorldPoint.fromScene(client, 36, 44, 0));
        puzzleTiles.put(2, WorldPoint.fromScene(client, 36, 43, 0));
        puzzleTiles.put(3, WorldPoint.fromScene(client, 36, 42, 0));
        puzzleTiles.put(4, WorldPoint.fromScene(client, 36, 41, 0));
        puzzleTiles.put(5, WorldPoint.fromScene(client, 36, 40, 0));

        puzzleTiles.put(6, WorldPoint.fromScene(client, 37, 44, 0));
        puzzleTiles.put(7, WorldPoint.fromScene(client, 37, 43, 0));
        puzzleTiles.put(8, WorldPoint.fromScene(client, 37, 42, 0));
        puzzleTiles.put(9, WorldPoint.fromScene(client, 37, 41, 0));
        puzzleTiles.put(10, WorldPoint.fromScene(client, 37, 40, 0));

        puzzleTiles.put(11, WorldPoint.fromScene(client, 38, 44, 0));
        puzzleTiles.put(12, WorldPoint.fromScene(client, 38, 43, 0));
        puzzleTiles.put(13, WorldPoint.fromScene(client, 38, 42, 0));
        puzzleTiles.put(14, WorldPoint.fromScene(client, 38, 41, 0));
        puzzleTiles.put(15, WorldPoint.fromScene(client, 38, 40, 0));

        puzzleTiles.put(16, WorldPoint.fromScene(client, 39, 44, 0));
        puzzleTiles.put(17, WorldPoint.fromScene(client, 39, 43, 0));
        puzzleTiles.put(18, WorldPoint.fromScene(client, 39, 42, 0));
        puzzleTiles.put(19, WorldPoint.fromScene(client, 39, 41, 0));
        puzzleTiles.put(20, WorldPoint.fromScene(client, 39, 40, 0));

        puzzleTiles.put(21, WorldPoint.fromScene(client, 40, 44, 0));
        puzzleTiles.put(22, WorldPoint.fromScene(client, 40, 43, 0));
        puzzleTiles.put(23, WorldPoint.fromScene(client, 40, 42, 0));
        puzzleTiles.put(24, WorldPoint.fromScene(client, 40, 41, 0));
        puzzleTiles.put(25, WorldPoint.fromScene(client, 40, 40, 0));
    }

    public void definePuzzleBottomLeft()
    {
        puzzleTiles.put(1, WorldPoint.fromScene(client, 36, 56, 0));
        puzzleTiles.put(2, WorldPoint.fromScene(client, 36, 55, 0));
        puzzleTiles.put(3, WorldPoint.fromScene(client, 36, 54, 0));
        puzzleTiles.put(4, WorldPoint.fromScene(client, 36, 53, 0));
        puzzleTiles.put(5, WorldPoint.fromScene(client, 36, 52, 0));

        puzzleTiles.put(6, WorldPoint.fromScene(client, 37, 56, 0));
        puzzleTiles.put(7, WorldPoint.fromScene(client, 37, 55, 0));
        puzzleTiles.put(8, WorldPoint.fromScene(client, 37, 54, 0));
        puzzleTiles.put(9, WorldPoint.fromScene(client, 37, 53, 0));
        puzzleTiles.put(10, WorldPoint.fromScene(client, 37, 52, 0));

        puzzleTiles.put(11, WorldPoint.fromScene(client, 38, 56, 0));
        puzzleTiles.put(12, WorldPoint.fromScene(client, 38, 55, 0));
        puzzleTiles.put(13, WorldPoint.fromScene(client, 38, 54, 0));
        puzzleTiles.put(14, WorldPoint.fromScene(client, 38, 53, 0));
        puzzleTiles.put(15, WorldPoint.fromScene(client, 38, 52, 0));

        puzzleTiles.put(16, WorldPoint.fromScene(client, 39, 56, 0));
        puzzleTiles.put(17, WorldPoint.fromScene(client, 39, 55, 0));
        puzzleTiles.put(18, WorldPoint.fromScene(client, 39, 54, 0));
        puzzleTiles.put(19, WorldPoint.fromScene(client, 39, 53, 0));
        puzzleTiles.put(20, WorldPoint.fromScene(client, 39, 52, 0));

        puzzleTiles.put(21, WorldPoint.fromScene(client, 40, 56, 0));
        puzzleTiles.put(22, WorldPoint.fromScene(client, 40, 55, 0));
        puzzleTiles.put(23, WorldPoint.fromScene(client, 40, 54, 0));
        puzzleTiles.put(24, WorldPoint.fromScene(client, 40, 53, 0));
        puzzleTiles.put(25, WorldPoint.fromScene(client, 40, 52, 0));
    }

    public void definePuzzleTopRight()
    {
        puzzleTiles.put(1, WorldPoint.fromScene(client, 53, 44, 0));
        puzzleTiles.put(2, WorldPoint.fromScene(client, 53, 43, 0));
        puzzleTiles.put(3, WorldPoint.fromScene(client, 53, 42, 0));
        puzzleTiles.put(4, WorldPoint.fromScene(client, 53, 41, 0));
        puzzleTiles.put(5, WorldPoint.fromScene(client, 53, 40, 0));

        puzzleTiles.put(6, WorldPoint.fromScene(client, 54, 44, 0));
        puzzleTiles.put(7, WorldPoint.fromScene(client, 54, 43, 0));
        puzzleTiles.put(8, WorldPoint.fromScene(client, 54, 42, 0));
        puzzleTiles.put(9, WorldPoint.fromScene(client, 54, 41, 0));
        puzzleTiles.put(10, WorldPoint.fromScene(client, 54, 40, 0));

        puzzleTiles.put(11, WorldPoint.fromScene(client, 55, 44, 0));
        puzzleTiles.put(12, WorldPoint.fromScene(client, 55, 43, 0));
        puzzleTiles.put(13, WorldPoint.fromScene(client, 55, 42, 0));
        puzzleTiles.put(14, WorldPoint.fromScene(client, 55, 41, 0));
        puzzleTiles.put(15, WorldPoint.fromScene(client, 55, 40, 0));

        puzzleTiles.put(16, WorldPoint.fromScene(client, 56, 44, 0));
        puzzleTiles.put(17, WorldPoint.fromScene(client, 56, 43, 0));
        puzzleTiles.put(18, WorldPoint.fromScene(client, 56, 42, 0));
        puzzleTiles.put(19, WorldPoint.fromScene(client, 56, 41, 0));
        puzzleTiles.put(20, WorldPoint.fromScene(client, 56, 40, 0));

        puzzleTiles.put(21, WorldPoint.fromScene(client, 57, 44, 0));
        puzzleTiles.put(22, WorldPoint.fromScene(client, 57, 43, 0));
        puzzleTiles.put(23, WorldPoint.fromScene(client, 57, 42, 0));
        puzzleTiles.put(24, WorldPoint.fromScene(client, 57, 41, 0));
        puzzleTiles.put(25, WorldPoint.fromScene(client, 57, 40, 0));
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
}