package com.birdhouses;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BirdHousePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BirdHousePlugin.class);
		RuneLite.main(args);
	}
}