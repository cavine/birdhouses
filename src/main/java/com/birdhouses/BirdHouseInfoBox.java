package com.birdhouses;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;

public class BirdHouseInfoBox extends InfoBox {

    @Setter
    @Getter
    private String timeSet;
    @Setter
    @Getter
    private String timePickup;
    @Setter
    @Getter
    private double diff;

    BirdHouseInfoBox(BufferedImage image, BirdHousePlugin plugin, String timeSet, String timePickup, double diff)
    {
        super(image, plugin);
        this.timeSet = timeSet;
        this.timePickup = timePickup;
        this.diff = diff;
        setPriority(InfoBoxPriority.LOW);
    }

    @Override
    public String getText()
    {
        Instant now = Instant.now();
        Instant pickup = Instant.parse(timePickup);

        Duration d = Duration.between(now,pickup);
        double min = d.toMinutes()+1.0;

        if (min<=0) {
            return "Ready!";
        }

        return String.valueOf((int)min);
    }

    @Override
    public Color getTextColor()
    {
        Instant now = Instant.now();
        Instant pickup = Instant.parse(timePickup);

        Duration d = Duration.between(now,pickup);
        double min = d.toMinutes();

        if (min<0) {
            return Color.green;
        }

        return Color.white;
    }

    @Override
    public String getTooltip()
    {
        Instant now = Instant.now();
        Instant pickup = Instant.parse(timePickup);

        Duration d = Duration.between(now,pickup);
        double min = d.toMinutes()+1.0;

        if (min<=0) {
            return "Birdhouses ready!";
        }

        return "Time: " +  String.valueOf((int)min) + " minutes";
    }
}
