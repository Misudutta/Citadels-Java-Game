package citadels;

/**
 * The color categories of district cards.
 */
public enum Color {
    RED, GREEN, BLUE, YELLOW, PURPLE;

    /**
     * Parse a color from a string, case-insensitive.
     * @param s color name
     * @return matching Color
     * @throws IllegalArgumentException if unknown
     */
    public static Color fromString(String s) {
        switch (s.toLowerCase()) {
            case "red":    return RED;
            case "green":  return GREEN;
            case "blue":   return BLUE;
            case "yellow": return YELLOW;
            case "purple": return PURPLE;
            default:
                throw new IllegalArgumentException("Unknown color: " + s);
        }
    }
}
