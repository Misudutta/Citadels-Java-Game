package citadels;

import java.util.Scanner;

/**
 * Reads commands from standard input and invokes Game methods.
 */
public class CommandProcessor {
    private final Game game;
    private final Scanner sc = new Scanner(System.in);

    /**
     * Create a new processor for the given game.
     * @param game the game instance to drive
     */
    public CommandProcessor(Game game) {
        this.game = game;
    }

    /**
     * Main REPL loop: read lines, parse commands, call game methods.
     * Recognized commands include: t, hand, gold, income, build, end,
     * citadel/list/city, all, save, load, debug, help, info.
     */
    public void run() {
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                game.showHelp();
                continue;
            }

            if (game.getPhase() == Game.Phase.SELECTION) {
                if (line.equalsIgnoreCase("t")) {
                    game.pressT();
                } else {
                    game.chooseCharacter(line);
                }
                continue;
            }

            Player current = game.getCurrentPlayer();
            boolean humanTurn = current instanceof HumanPlayer;
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            if (humanTurn && cmd.equals("income") && parts.length == 2) {
                if (parts[1].equalsIgnoreCase("gold"))      game.humanTakeGoldIncome();
                else if (parts[1].equalsIgnoreCase("cards")) game.humanDrawIncome();
                else System.out.println("Usage: income gold | income cards");
                continue;
            }

            switch (cmd) {
                case "t": game.pressT(); break;
                case "hand":
                    if (humanTurn) game.showHand();
                    else           System.out.println("It is not your turn.");
                    break;
                case "gold":
                    if (humanTurn)
                        System.out.println("You have " + game.getHuman().getGold() + " gold.");
                    else
                        System.out.println("It is not your turn.");
                    break;
                case "build":
                    if (humanTurn && parts.length == 2) {
                        try {
                            int idx = Integer.parseInt(parts[1]);
                            game.humanBuild(idx);
                        } catch (NumberFormatException e) {
                            System.out.println("Usage: build <hand-index>");
                        }
                    } else {
                        System.out.println("Usage: build <hand-index>");
                    }
                    break;
                case "end":
                    if (humanTurn) game.pressT();
                    else           System.out.println("It is not your turn.");
                    break;
                case "citadel": case "list": case "city":
                    int pid = 1;
                    if (parts.length == 2) {
                        try { pid = Integer.parseInt(parts[1]); }
                        catch (NumberFormatException e) {
                            System.out.println("Usage: " + cmd + " [player#]");
                            break;
                        }
                    }
                    game.showCity(pid);
                    break;
                case "all":
                    game.showAll();
                    break;
                case "save":
                    if (parts.length == 2) game.save(parts[1]);
                    else System.out.println("Usage: save <file>");
                    break;
                case "load":
                    if (parts.length == 2) game.load(parts[1]);
                    else System.out.println("Usage: load <file>");
                    break;
                case "debug":
                    game.toggleDebug();
                    break;
                case "help":
                    game.showHelp();
                    break;
                case "info":
                    if (parts.length != 2) {
                        System.out.println("Usage: info <character-name> OR info <hand-index>");
                        break;
                    }
                    String arg = parts[1];
                    try {
                        int idx = Integer.parseInt(arg);
                        if (humanTurn) {
                            game.showCardInfo(idx);
                        } else {
                            System.out.println("It is not your turn.");
                        }
                        break;
                    } catch (NumberFormatException e) {
                        // not an index
                    }

                    try {
                        Character c = Character.valueOf(arg.toUpperCase());
                        switch (c) {
                            case ASSASSIN:
                                System.out.println("Assassin (1): Select another character to kill. That character loses their turn.");
                                break;
                            case THIEF:
                                System.out.println("Thief (2): Select another character to rob. You take their gold when their turn begins. Cannot rob Assassin or the killed character.");
                                break;
                            case MAGICIAN:
                                System.out.println("Magician (3): Swap your hand with another player OR discard and redraw any number of cards.");
                                break;
                            case KING:
                                System.out.println("King (4): Gain 1 gold per yellow district. Also gains the crown.");
                                break;
                            case BISHOP:
                                System.out.println("Bishop (5): Gain 1 gold per blue district. Warlord cannot destroy your buildings unless you're assassinated.");
                                break;
                            case MERCHANT:
                                System.out.println("Merchant (6): Gain 1 gold per green district. Also gain 1 extra gold.");
                                break;
                            case ARCHITECT:
                                System.out.println("Architect (7): Draw 2 extra cards. Can build up to 3 districts.");
                                break;
                            case WARLORD:
                                System.out.println("Warlord (8): Gain 1 gold per red district. May destroy one district at a reduced cost (not in 8-district cities).");
                                break;
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid character name. Try one of: " + game.listAllCharacters());
                    }
                    break;
                default:
                    System.out.println("Unknown command.");
                    game.showHelp();
            }
        }
    }
}
