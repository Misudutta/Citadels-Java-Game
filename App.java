package citadels;

/**
 * Application entry point for Citadels game.
 */
public class App {
    /**
     * Main method: creates a Game and starts command processing.
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        Game game = new Game();
        CommandProcessor processor = new CommandProcessor(game);
        processor.run();
    }
}
