package sh.woopie.toa;

import sh.woopie.toa.ScarabPuzzle.ScarabPuzzle;

import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.ui.overlay.OverlayManager;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
		name = "Tombs of Amascut"
)
public class TombsOfAmascutPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TombsOfAmascutConfig config;

	@Inject
	private ScarabPuzzle scarabPuzzle;

	private Room[] rooms = null;

	@Inject
	private OverlayManager overlayManager;

	@Override
	protected void startUp()
	{
		if (rooms == null)
		{
			rooms = new Room[] { scarabPuzzle };

			for (Room room : rooms)
			{
				room.init();
			}
		}

		for (Room room : rooms)
		{
			room.load();
		}
	}

	@Override
	protected void shutDown()
	{
		for (Room room : rooms)
		{
			room.unload();
		}
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		scarabPuzzle.onGroundObjectSpawned(event);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		scarabPuzzle.onGameObjectSpawned(event);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		scarabPuzzle.onChatMessage(event);
	}


	@Provides
	TombsOfAmascutConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TombsOfAmascutConfig.class);
	}
}
