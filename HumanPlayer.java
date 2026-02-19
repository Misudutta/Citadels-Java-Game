package citadels;

/**
 * The human-controlled player.
 */
public class HumanPlayer extends Player {
    /**
     * Construct a human player with the given ID.
     * @param id player number (1)
     */
    public HumanPlayer(int id) {
        super(id);
    }

    /**
     * Draw n cards into human’s hand.
     * @param deck the district deck
     * @param n number of cards to draw
     */
    @Override
    public void draw(DistrictDeck deck, int n) {
        for (int i = 0; i < n; i++) {
            hand.add(deck.draw());
        }
    }

    /** No-op: human takes actions via CommandProcessor. */
    @Override
    public void takeTurn(Game g) { /* no-op */ }

    /** Gain 1 gold (used by Warlord ability). */
    public void takeGold() {
        gold += 1;
        System.out.println("You gained 1 gold (total=" + gold + ").");
    }

    /**
     * Build a district from your hand by index.
     * @param cardIndex 1-based index into hand
     * @param deck unused
     */
    public void buildFromHand(int cardIndex, DistrictDeck deck) {
        if (cardIndex < 1 || cardIndex > hand.size()) {
            System.out.println("Invalid card index.");
            return;
        }
        DistrictCard card = hand.get(cardIndex - 1);
        if (card.getCost() > gold) {
            System.out.println("You cannot afford to build this building.");
            return;
        }
        hand.remove(cardIndex - 1);
        city.add(card);
        gold -= card.getCost();
        System.out.println("Built: " + card.display());
    }
}

