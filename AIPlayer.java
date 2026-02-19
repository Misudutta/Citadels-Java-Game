package citadels;

import java.util.Random;

/**
 * An AI-controlled player in Citadels.
 * <p>
 * The AI will:
 * <ul>
 *   <li>If its hand has fewer than 2 cards: draw two and keep one at random.</li>
 *   <li>Otherwise: take 2 gold.</li>
 *   <li>Then build the most expensive affordable district.</li>
 * </ul>
 */
public class AIPlayer extends Player {
    /** Random generator used for income card choice. */
    private final Random rand = new Random();

    /**
     * Construct an AIPlayer with the given ID.
     * @param id unique player identifier (1 = human, 2+ = AIs)
     */
    public AIPlayer(int id) {
        super(id);
    }

    /**
     * Draw n cards into the AI’s hand.
     * @param deck the district deck to draw from
     * @param n number of cards to draw
     */
    @Override
    public void draw(DistrictDeck deck, int n) {
        for (int i = 0; i < n; i++) {
            hand.add(deck.draw());
        }
    }

    /**
     * Take this AI’s turn: choose income then build the
     * most expensive affordable district, if any.
     * @param game current game state
     */
    @Override
    public void takeTurn(Game game) {
        // Income choice
        if (hand.size() < 2) {
            DistrictCard c1 = game.getDeck().draw();
            DistrictCard c2 = game.getDeck().draw();
            DistrictCard keep = rand.nextBoolean() ? c1 : c2;
            hand.add(keep);
            System.out.println("AI Player " + id +
                               " draws income cards, keeps " + keep.display());
        } else {
            addGold(2);
            System.out.println("AI Player " + id +
                               " takes 2 gold (total=" + gold + ").");
        }

        // Build most expensive affordable district
        DistrictCard toBuild = null;
        int bestCost = -1;
        for (DistrictCard d : hand) {
            if (d.getCost() <= gold && d.getCost() > bestCost) {
                bestCost = d.getCost();
                toBuild = d;
            }
        }
        if (toBuild != null) {
            int idx = hand.indexOf(toBuild) + 1;
            buildIndex(idx);
            System.out.println("AI Player " + id +
                               " builds " + toBuild.display());
            game.checkEndTrigger(this);
        }
    }

    /**
     * Build a district from hand by its 1-based index.
     * Removes the card from hand, adds to city, deducts gold.
     * @param cardIndex 1-based index into hand
     * @return true if build succeeded; false if index out of range
     */
    public boolean buildIndex(int cardIndex) {
        if (cardIndex < 1 || cardIndex > hand.size()) return false;
        DistrictCard c = hand.remove(cardIndex - 1);
        city.add(c);
        gold -= c.getCost();
        return true;
    }
}



