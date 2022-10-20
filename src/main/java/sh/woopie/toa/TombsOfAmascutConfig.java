package sh.woopie.toa;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import java.awt.Color;

@ConfigGroup("toa")
public interface TombsOfAmascutConfig extends Config
{
	@ConfigSection(
		name = "Scarab Puzzle",
		description = "Helpers for the Scarab puzzle room",
		position = 0,
		closedByDefault = false
	)
	String SCARAB_PUZZLE_SECTION = "scarabPuzzleSection";

	@ConfigItem(
		position = 0,
		keyName = "sequencePuzzle",
		name = "Solve Sequence Puzzle",
		description = "Solve the memory flip puzzle.",
		section = SCARAB_PUZZLE_SECTION
	)
	default boolean sequencePuzzle()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "countPuzzle",
		name = "Solve Number Puzzle",
		description = "Solve the number counting puzzle.",
		section = SCARAB_PUZZLE_SECTION
	)
	default boolean numberPuzzle()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "memoryPuzzle",
		name = "Solve Memory Puzzle",
		description = "Solve the memory flip puzzle.",
		section = SCARAB_PUZZLE_SECTION
	)
	default boolean memoryPuzzle()
	{
		return true;
	}

	@ConfigItem(
		position = 3,
		keyName = "lightPuzzle",
		name = "Solve Light Puzzle",
		description = "Solve the light puzzle.",
		section = SCARAB_PUZZLE_SECTION
	)
	default boolean lightPuzzle()
	{
		return true;
	}

	@ConfigItem(
		position = 4,
		keyName = "obeliskPuzzle",
		name = "Solve Obelisk Puzzle",
		description = "Remember obelisk order.",
		section = SCARAB_PUZZLE_SECTION
	)
	default boolean obeliskPuzzle()
	{
		return true;
	}

	@ConfigItem(
		name = "Tile Marker Color",
		keyName = "scarabPuzzleTileMarkerColor",
		description = "Set the color of the highlight overlay",
		position = 6,
		section = SCARAB_PUZZLE_SECTION
	)
	default Color scarabPuzzleTileMarkerColor()
	{
		return new Color(255, 0, 115);
	}
}
