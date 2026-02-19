package citadels;

/**
 * Represents a district card in Citadels.
 */
public class DistrictCard {
    private final String name;
    private final Color color;
    private final int cost;
    private final String text;

    /**
     * Construct a district card.
     * @param name human-readable card name
     * @param color the card’s color category
     * @param cost gold cost to build
     * @param text special ability description (or empty)
     */
    public DistrictCard(String name, Color color, int cost, String text) {
        this.name  = name;
        this.color = color;
        this.cost  = cost;
        this.text  = text;
    }

    /** @return card name */
    public String getName() { return name; }

    /** @return card color */
    public Color getColor() { return color; }

    /** @return gold cost */
    public int getCost() { return cost; }

    /** @return special ability text */
    public String getText() { return text; }

    /**
     * Display string, e.g. "Castle [yellow4]".
     * @return formatted display
     */
    public String display() {
        return String.format("%s [%s%s]",
            name,
            color.name().toLowerCase(),
            cost);
    }

    /**
     * Same as display().
     * @return formatted card string
     */
    @Override
    public String toString() {
        return display();
    }
}
