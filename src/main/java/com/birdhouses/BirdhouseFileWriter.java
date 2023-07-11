package com.birdhouses;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
public class BirdhouseFileWriter {
    private static final File BIRDHOUSE_DIR = new File(RUNELITE_DIR, "birdhouses");

    private static final String COMMA_DELIMITER = ",";

    @Getter
    @Setter
    private String hash;

    private String convertToCSV(String[] data) {
        return Stream.of(data).collect(Collectors.joining(","));
    }

    public ArrayList<String> getDifference() {
        ArrayList<String> vals = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(BIRDHOUSE_DIR))) {
            String line;


            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);

                if (hash.equals(values[0])) {
                    Instant now = Instant.now();
                    Instant then = Instant.parse(values[2]);

                    Duration d = Duration.between(now,then);
                    double diff = d.toMinutes();

                    if (diff<0) {
                        vals.add(now.toString());
                        vals.add(values[2]);
                        vals.add("0");
                        return vals;
                    }

                    vals.add(now.toString());
                    vals.add(values[2]);
                    vals.add(Double.toString(diff));

                    return vals;
                }

            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }
        vals.add("0");
        vals.add("0");
        vals.add("0");
        return vals;
    }

    public void writeToFile(List<String[]> dataLines) {

        for (var i=0; i<dataLines.size(); i++) {

            try (PrintWriter pw = new PrintWriter(BIRDHOUSE_DIR)) {
                dataLines.stream()
                        .map(this::convertToCSV)
                        .forEach(pw::println);
            }

            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void writeData() {

        long minutes = 50;
        //Current UTC Time
        Instant now = Instant.now();

        //Time to pickup birdhouse +50 min
        Instant pickup = now.plus(minutes, ChronoUnit.MINUTES);

        //Check if Hash exists in CSV & read all lines
        boolean found = false;
        List<String[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(BIRDHOUSE_DIR))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);

                if (hash.equals(values[0])) {
                    found = true;
                    values[1] = now.toString();
                    values[2] = pickup.toString();
                }
                records.add(values);
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        if (!found) {
            records.add(new String[] {hash, now.toString(), pickup.toString()});
        }

        writeToFile(records);
    }

}
