package citadels;

/**
 * The eight character roles in Citadels, each with a rank.
 */
public enum Character {
    ASSASSIN(1), THIEF(2), MAGICIAN(3),
    KING(4), BISHOP(5), MERCHANT(6),
    ARCHITECT(7), WARLORD(8);

    /** The rank order of this character. */
    public final int rank;

    Character(int r) {
        rank = r;
    }

    /**
     * Return lowercase name with rank in parentheses.
     * @return e.g. "assassin(1)"
     */
    @Override
    public String toString() {
        return name().toLowerCase() + "(" + rank + ")";
    }
}
