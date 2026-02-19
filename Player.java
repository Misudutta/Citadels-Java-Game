package citadels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base for a Citadels player (human or AI).
 */
public abstract class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Unique player number (1 = human). */
    protected final int id;

    /** Gold in hand. */
    protected int gold = 2;

    /** Cards in hand. */
    protected List<DistrictCard> hand = new ArrayList<>();

    /** Built districts in city. */
    protected List<DistrictCard> city = new ArrayList<>();

    /**
     * Construct a player.
     * @param id player identifier
     */
    public Player(int id) {
        this.id = id;
    }

    /** @return this player’s id */
    public int getId() {
        return id;
    }

    /** @return this player’s gold */
    public int getGold() {
        return gold;
    }

    /**
     * Add gold to this player.
     * @param amount positive or negative
     */
    public void addGold(int amount) {
        gold += amount;
    }

    /**
     * Draw n cards into your hand.
     * @param deck the district deck
     * @param n number of cards
     */
    public abstract void draw(DistrictDeck deck, int n);

    /**
     * Take your turn’s actions.
     * @param game current game state
     */
    public abstract void takeTurn(Game game);

    /** @return number of cards in hand */
    public int getHandSize() {
        return hand.size();
    }

    /** @return number of districts built */
    public int getCitySize() {
        return city.size();
    }

    /** @return list of built districts */
    public List<DistrictCard> getBuiltDistricts() {
        return city;
    }

    /** @return list of cards in hand */
    public List<DistrictCard> getHandCards() {
        return hand;
    }
}
