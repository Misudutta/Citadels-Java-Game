package citadels;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A shuffled deck of DistrictCard read from a TSV definition.
 */
public class DistrictDeck {
    private final Deque<DistrictCard> deck = new ArrayDeque<>();

    /**
     * Read cards from a TSV stream, line format:
     *   Name [tab] Qty [tab] color [tab] cost [tab] text
     * Then shuffle.
     *
     * @param tsvStream InputStream of the TSV data
     * @throws RuntimeException on I/O error
     */
    public DistrictDeck(InputStream tsvStream) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(tsvStream, StandardCharsets.UTF_8))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                String name  = parts[0];
                int    qty   = Integer.parseInt(parts[1]);
                Color  color = Color.fromString(parts[2]);
                int    cost  = Integer.parseInt(parts[3]);
                String text  = parts.length > 4 ? parts[4] : "";
                for (int i = 0; i < qty; i++) {
                    deck.add(new DistrictCard(name, color, cost, text));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        shuffle();
    }

    /** Shuffle the deck into a new random order. */
    public void shuffle() {
        List<DistrictCard> list = new ArrayList<>(deck);
        Collections.shuffle(list);
        deck.clear();
        deck.addAll(list);
    }

    /**
     * Draw the top card.
     * @return card or null if empty
     */
    public DistrictCard draw() {
        return deck.poll();
    }

    /** @return true if no cards remain */
    public boolean isEmpty() {
        return deck.isEmpty();
    }
}
