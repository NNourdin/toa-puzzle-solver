package sh.woopie.toa;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayManager;

import org.apache.commons.lang3.ArrayUtils;
import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public abstract class Room
{
	protected final TombsOfAmascutPlugin plugin;
	protected final TombsOfAmascutConfig config;

	@Inject
	protected OverlayManager overlayManager;

	@Inject
	private Client client;

	@Inject
	protected Room(TombsOfAmascutPlugin plugin, TombsOfAmascutConfig config)
	{
		this.plugin = plugin;
		this.config = config;
	}

	public void init()
	{
	}

	public void load()
	{
	}

	public void unload()
	{
	}

	public boolean inRoomRegion(Integer roomRegionId)
	{
		return ArrayUtils.contains(client.getMapRegions(), roomRegionId);
	}
}