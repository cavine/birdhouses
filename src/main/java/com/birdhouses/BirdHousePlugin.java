package com.birdhouses;

import com.google.inject.Provides;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

@Slf4j
@PluginDescriptor(
	name = "BirdHouses"
)
public class BirdHousePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	BirdhouseFileWriter writer;

	private final BufferedImage houseImg = ImageUtil.loadImageResource(getClass(), "/birdhouse.png");

	@Inject
	private BirdHouseConfig config;
	private BirdHouseInfoBox birdhouseBox;

	@Override
	protected void startUp() throws Exception
	{
		if (client.getGameState().equals(GameState.LOGGED_IN) || client.getGameState().equals(GameState.LOADING)) {
			updateWriterHash();
		}

	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			updateWriterHash();
			ArrayList<String> arr = writer.getDifference();
			birdhouseBox = new BirdHouseInfoBox(houseImg, this, arr.get(0), arr.get(1), Double.parseDouble(arr.get(2)));
			java.util.List<InfoBox> boxes = infoBoxManager.getInfoBoxes();
			var found = false;
			for (var i=0; i<boxes.size(); i++) {
				if (boxes.get(i).toString().contains("com.birdhouses")) {
					found=true;
					break;
				}
			}
			if (!found)
				infoBoxManager.addInfoBox(birdhouseBox);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		infoBoxManager.removeInfoBox(birdhouseBox);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {

		if(event.getType() != ChatMessageType.MESBOX) {
			return;
		}

		if (event.getMessage().equalsIgnoreCase("Your birdhouse trap is now full of seed and will start to catch birds.")) {
			writer.writeData();

			Instant timeSet = Instant.parse(birdhouseBox.getTimeSet());
			Instant timePickup = Instant.parse(birdhouseBox.getTimePickup());

			Duration d = Duration.between(timeSet,timePickup);
			double diff = d.toMinutes();

			//Birdhouses setup too recently. Most trips are done in one go. 5 min grace period
			if (diff > 45.0) {
				return;
			}

			ArrayList<String> arr = writer.getDifference();
			java.util.List<InfoBox> boxes = infoBoxManager.getInfoBoxes();
			for (var i=0; i<boxes.size(); i++) {
				if (boxes.get(i).toString().contains("com.birdhouses")) {
					infoBoxManager.removeInfoBox(boxes.get(i));
					break;
				}
			}
			birdhouseBox = new BirdHouseInfoBox(houseImg, this, arr.get(0), arr.get(1), Double.parseDouble(arr.get(2)));
			infoBoxManager.addInfoBox(birdhouseBox);
		}
	}

	private void updateWriterHash() {
		String hash = String.valueOf(client.getAccountHash());

		if (hash.equalsIgnoreCase(writer.getHash()))
		{
			return;
		}

		writer.setHash(hash);
	}

	@Provides
	BirdHouseConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BirdHouseConfig.class);
	}
}
