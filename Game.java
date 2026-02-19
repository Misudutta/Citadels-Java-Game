package citadels;

import java.io.*;
import java.util.*;

// JSON-Simple imports
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

/**
 * Core game engine for Citadels.
 * <p>
 * Manages phases (SELECTION vs TURN), character assignments, player turns,
 * income/build actions, saving/loading, and end-of-game scoring.
 */
public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Game phases: character selection or playing turns. */
    public enum Phase { SELECTION, TURN }

    // ─── Fields ──────────────────────────────────────────────────────────────

    /** Current phase of the game. */
    private Phase phase = Phase.SELECTION;

    /** Deck of district cards. */
    private final DistrictDeck districtDeck;

    /** All players in play (index 0 is human). */
    private final List<Player> players = new ArrayList<>();

    /** Characters available to pick this round. */
    private final List<Character> availableChars = new ArrayList<>();

    /** Mapping of each player to their chosen character. */
    private final Map<Player, Character> assignments = new HashMap<>();

    /** Face-up discarded characters in selection. */
    private final List<Character> faceUpDiscards = new ArrayList<>();

    /** One mystery discard, hidden from all. */
    private Character mysteryDiscard;

    /** Index in players[] whose turn to pick character. */
    private int chooserIndex;

    /** Next character rank pointer (1…8) in TURN phase. */
    private int turnRankPointer = 1;

    /** Player whose turn it currently is. */
    private Player currentPlayer;

    /** Has current player taken income this turn? */
    private boolean incomeTaken;

    /** Has current player built this turn? */
    private boolean builtThisTurn;

    /** Debug prints extra info when true. */
    private boolean debugMode = false;

    /** End-game triggered when someone builds 8th district. */
    private boolean endTriggered = false;

    /** ID of first player to complete 8 districts. */
    private int firstCompleter = 0;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Initialize game: shuffle deck, add characters, prompt for player count,
     * deal 4 cards to each, choose starting player.
     */
    public Game() {
        System.out.println("Shuffling deck...");
        this.districtDeck = new DistrictDeck(
            getClass().getResourceAsStream("/citadels/cards.tsv")
        );
        System.out.println("Adding characters...");
        Collections.addAll(availableChars, Character.values());
        initPlayers();
        System.out.println("Dealing cards...");
        for (Player p : players) p.draw(districtDeck, 4);
        System.out.printf("Starting Citadels with %d players...%n", players.size());
        System.out.println("You are player 1");
        chooserIndex = new Random().nextInt(players.size());
        System.out.printf("Player %d is the crowned player and goes first.%n",
                          players.get(chooserIndex).getId());
        System.out.println("Press t to process turns");
        printPhaseHeader();
    }

    /**
     * Prompt user for number of players [4–7], create HumanPlayer(1)
     * and AIPlayer(2…n).
     */
    private void initPlayers() {
        Scanner sc = new Scanner(System.in);
        int n;
        do {
            System.out.print("Enter how many players [4-7]: ");
            try { n = Integer.parseInt(sc.nextLine().trim()); }
            catch (Exception e) { n = -1; }
        } while (n < 4 || n > 7);
        players.add(new HumanPlayer(1));
        for (int i = 2; i <= n; i++) {
            players.add(new AIPlayer(i));
        }
    }

    /** Print a header showing current phase. */
    private void printPhaseHeader() {
        System.out.println("================================");
        System.out.println(phase == Phase.SELECTION ? "SELECTION PHASE" : "TURN PHASE");
        System.out.println("================================");
    }

    // ─── Processing ──────────────────────────────────────────────────────────

    /**
     * Process one "t" command: either a selection-phase step or a turn-phase step.
     */
    public void processT() {
        if (phase == Phase.SELECTION) doSelectionStep();
        else                         doTurnStep();
    }

    /** Alias for processT(), used by CommandProcessor. */
    public void pressT() {
        processT();
    }

    /** Handle one selection-phase action (draw/discard/pick). */
    private void doSelectionStep() {
        if (mysteryDiscard != null && availableChars.isEmpty()) {
            Collections.addAll(availableChars, Character.values());
        }
        if (mysteryDiscard == null) {
            mysteryDiscard = drawAndRemove();
            System.out.println("A mystery character was removed.");
            int faceUpCount;
            switch (players.size()) {
                case 4: case 6: faceUpCount = 2; break;
                case 5: case 7: faceUpCount = 1; break;
                default: faceUpCount = 0;
            }
            for (int i = 0; i < faceUpCount; i++) {
                Character c = drawAndRemove();
                if (c == Character.KING) {
                    System.out.println("King was removed. The King cannot be visibly removed, trying again..");
                    availableChars.add(c);
                    Collections.shuffle(availableChars);
                    i--;
                } else {
                    faceUpDiscards.add(c);
                    System.out.println(c.name() + " was removed.");
                }
            }
            return;
        }
        Player chooser = players.get(chooserIndex);
        if (chooser instanceof HumanPlayer) {
            System.out.println("Available characters: " + listChars());
            System.out.println("Choose your character.");
            return;
        }
        Character pick = availableChars.remove(new Random().nextInt(availableChars.size()));
        assignments.put(chooser, pick);
        System.out.printf("Player %d chose a character.%n", chooser.getId());
        advancePicker();
    }

     /**
     * Called by human input to choose a character by name.
     *
     * @param name the name of the character to pick (e.g. "assassin")
     */
    public void chooseCharacter(String name) {
        if (phase != Phase.SELECTION) {
            System.out.println("It is not your turn. Press t to continue.");
            return;
        }
        Character c;
        try {
            c = Character.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid. Pick one of: " + listChars());
            return;
        }
        if (!availableChars.remove(c)) {
            System.out.println("That character is not available. Pick one of: " + listChars());
            return;
        }
        assignments.put(getHuman(), c);
        System.out.println("Player 1 chose a character.");
        advancePicker();
    }

    /** Advance chooserIndex and switch to TURN when all have picked. */
    private void advancePicker() {
        chooserIndex = (chooserIndex + 1) % players.size();
        if (assignments.size() == players.size()) {
            phase = Phase.TURN;
            turnRankPointer = 1;
            printPhaseHeader();
        }
    }

    /** Handle one turn-phase action: players take income/build in rank order. */
    private void doTurnStep() {
        if (turnRankPointer > Character.values().length) {
            if (endTriggered) {
                scoreAndExit();
                return;
            }
            phase = Phase.SELECTION;
            mysteryDiscard = null;
            faceUpDiscards.clear();
            assignments.clear();
            availableChars.clear();
            Collections.addAll(availableChars, Character.values());
            printPhaseHeader();
            return;
        }
        incomeTaken   = false;
        builtThisTurn = false;
        Character curr = Character.values()[turnRankPointer - 1];
        System.out.printf("%d: %s  ", turnRankPointer, curr.name());
        Player owner = assignments.entrySet().stream()
            .filter(e -> e.getValue() == curr)
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
        currentPlayer = owner;
        if (owner == null) {
            System.out.println("No one is the " + curr.name());
        } else if (owner instanceof HumanPlayer) {
            System.out.println("Your turn.");
            System.out.println("Choose income: 'income gold' or 'income cards'");
        } else {
            System.out.printf("Player %d is the %s%n", owner.getId(), curr.name());
            if (debugMode) {
                System.out.println("DEBUG: AI hand: " + owner.getHandCards());
            }
            ((AIPlayer) owner).takeTurn(this);
        }
        turnRankPointer++;
    }

    /** Remove and return a random character from availableChars. */
    private Character drawAndRemove() {
        return availableChars.remove(new Random().nextInt(availableChars.size()));
    }

    /** Build a comma-separated list of available character names. */
    private String listChars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < availableChars.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(availableChars.get(i).name());
        }
        return sb.toString();
    }

    // ─── Exposed Methods ─────────────────────────────────────────────────────

    /** @return the human player (players.get(0)) */
    public HumanPlayer getHuman()            { return (HumanPlayer) players.get(0); }

    /** @return current phase (SELECTION or TURN) */
    public Phase getPhase()                 { return phase;               }

    /** @return player whose turn it is now */
    public Player getCurrentPlayer()        { return currentPlayer;       }

    /** @return the shared district deck */
    public DistrictDeck getDeck()           { return districtDeck;        }

    /** @return true if income has been taken this turn */
    public boolean isIncomeTaken()          { return incomeTaken;         }

    /** @return true if a build was performed this turn */
    public boolean hasBuiltThisTurn()       { return builtThisTurn;       }

    /**
     * Human takes 2 gold as income. Must be human’s turn and not yet taken.
     * Prints error if invalid.
     */
    public void humanTakeGoldIncome() {
        if (!(currentPlayer instanceof HumanPlayer) || incomeTaken) {
            System.out.println("Cannot take income now.");
            return;
        }
        getHuman().addGold(2);
        System.out.println("You chose gold and gained 2 (total=" + getHuman().getGold() + ").");
        incomeTaken = true;
    }

    /**
     * Human draws two cards as income, chooses one to keep.
     * Prints error if not allowed.
     */
    public void humanDrawIncome() {
        if (!(currentPlayer instanceof HumanPlayer) || incomeTaken) {
            System.out.println("Cannot take income now.");
            return;
        }
        HumanPlayer me = getHuman();
        DistrictCard c1 = districtDeck.draw();
        DistrictCard c2 = districtDeck.draw();
        System.out.println("Drawn: 1) " + c1.display() + "   2) " + c2.display());
        Scanner sc = new Scanner(System.in);
        int pick;
        do {
            System.out.print("Pick 1 or 2: ");
            try { pick = Integer.parseInt(sc.nextLine().trim()); }
            catch (Exception e) { pick = -1; }
        } while (pick != 1 && pick != 2);
        DistrictCard keep = (pick == 1 ? c1 : c2);
        me.getHandCards().add(keep);
        System.out.println("You kept: " + keep.display());
        incomeTaken = true;
    }

    /**
     * Human builds a district at the given hand index.
     * Must be human’s turn, income taken, and not yet built this turn.
     * Prints error if invalid.
     * @param idx 1-based hand index
     */
    public void humanBuild(int idx) {
        if (!(currentPlayer instanceof HumanPlayer)) {
            System.out.println("It is not your turn.");
            return;
        }
        if (!incomeTaken) {
            System.out.println("You must take income first.");
            return;
        }
        if (builtThisTurn) {
            System.out.println("You have already built this turn.");
            return;
        }
        getHuman().buildFromHand(idx, districtDeck);
        builtThisTurn = true;
        checkEndTrigger(getHuman());
    }

    /** Print the human player’s hand and gold. */
    public void showHand() {
        HumanPlayer me = getHuman();
        System.out.printf("You have %d gold. Cards in hand:%n", me.getGold());
        int i = 1;
        for (DistrictCard d : me.getHandCards()) {
            System.out.printf("  %d. %s (%s), cost: %d%n",
                i++, d.getName(), d.getColor().name().toLowerCase(), d.getCost());
        }
    }

    /**
     * Print the built districts of the given player.
     * @param pid player number 1…n
     */
    public void showCity(int pid) {
        if (pid < 1 || pid > players.size()) {
            System.out.println("Invalid player number. Must be 1–" + players.size() + ".");
            return;
        }
        Player p = players.get(pid - 1);
        System.out.println("Player " + p.getId() + " city:");
        List<DistrictCard> city = p.getBuiltDistricts();
        if (city.isEmpty()) {
            System.out.println("  (no districts built)");
        } else {
            for (DistrictCard d : city) {
                System.out.println("  " + d.display());
            }
        }
    }

    /** Show summary (hand size, gold, city size) for all players. */
    public void showAll() {
        for (Player p : players) {
            System.out.printf(
                "Player %d: hand=%d cards, gold=%d, city=%d districts%n",
                p.getId(), p.getHandSize(), p.getGold(), p.getCitySize()
            );
        }
    }

    /**
     * Save game state (phase, turn pointer, players’ hands/cities) to JSON file.
     * @param filename output JSON path
     */
    @SuppressWarnings("unchecked")
    public void save(String filename) {
        JSONObject root = new JSONObject();
        root.put("phase", phase.name());
        root.put("turnRankPointer", turnRankPointer);
        root.put("chooserIndex", chooserIndex);

        JSONArray jsPlayers = new JSONArray();
        for (Player p : players) {
            JSONObject jp = new JSONObject();
            jp.put("id", p.getId());
            jp.put("gold", p.getGold());
            JSONArray handArr = new JSONArray();
            for (DistrictCard d : p.getHandCards()) handArr.add(d.getName());
            jp.put("hand", handArr);
            JSONArray cityArr = new JSONArray();
            for (DistrictCard d : p.getBuiltDistricts()) cityArr.add(d.getName());
            jp.put("city", cityArr);
            jsPlayers.add(jp);
        }
        root.put("players", jsPlayers);

        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(root.toJSONString());
            System.out.println("Game saved to " + filename);
        } catch (IOException e) {
            System.out.println("Save failed: " + e.getMessage());
        }
    }

    /**
     * Load game state from JSON file. Overwrites current fields (except deck).
     * @param filename input JSON path
     */
    public void load(String filename) {
        JSONParser parser = new JSONParser();
        try (FileReader fr = new FileReader(filename)) {
            JSONObject root = (JSONObject) parser.parse(fr);

            phase            = Phase.valueOf((String) root.get("phase"));
            turnRankPointer  = ((Long)   root.get("turnRankPointer")).intValue();
            chooserIndex     = ((Long)   root.get("chooserIndex")).intValue();

            JSONArray jsPlayers = (JSONArray) root.get("players");
            players.clear();
            for (Object o : jsPlayers) {
                JSONObject jp = (JSONObject) o;
                int id   = ((Long) jp.get("id")).intValue();
                int gold = ((Long) jp.get("gold")).intValue();
                Player p = (id == 1)
                    ? new HumanPlayer(1)
                    : new AIPlayer(id);
                p.gold = gold;
                players.add(p);
            }

            System.out.println("Game loaded from " + filename);
            printPhaseHeader();
        } catch (Exception e) {
            System.out.println("Load failed: " + e.getMessage());
        }
    }

    /** Toggle debug mode on/off. */
    public void toggleDebug() {
        debugMode = !debugMode;
        System.out.println("Debug mode " + (debugMode ? "ON" : "OFF"));
    }

    /** Print the list of available commands. */
    public void showHelp() {
        System.out.println("Available commands:");
        System.out.println("  t                     : process turns");
        System.out.println("  hand                  : show your hand and gold");
        System.out.println("  gold                  : show your gold");
        System.out.println("  income gold|cards     : choose income");
        System.out.println("  build <hand-index>    : build a district");
        System.out.println("  end                   : end your turn");
        System.out.println("  citadel [p]           : show player p’s city");
        System.out.println("  list [p]              : alias for citadel");
        System.out.println("  city [p]              : alias for citadel");
        System.out.println("  all                   : show all players info");
        System.out.println("  save <file>           : save game state to JSON file");
        System.out.println("  load <file>           : load game state from JSON file");
        System.out.println("  debug                 : toggle debug mode");
        System.out.println("  help                  : show this message");
    }

    /**
     * Check if player has built ≥8 districts to trigger end-game.
     * @param p player to check
     */
    public void checkEndTrigger(Player p) {
        if (!endTriggered && p.getCitySize() >= 8) {
            endTriggered    = true;
            firstCompleter  = p.getId();
            System.out.printf(">>> Player %d has completed 8 districts first!%n", p.getId());
        }
    }

    /** Score all players and exit via System.exit(). */
    private void scoreAndExit() {
        System.out.println("\n=== GAME OVER: Scoring ===");
        class Score {
            Player p;
            int base, diversity, bonusFirst, bonusOther, extra;
            int total() { return base + diversity + bonusFirst + bonusOther + extra; }
        }
        List<Score> scores = new ArrayList<>();
        for (Player p : players) {
            Score s = new Score();
            s.p = p;
            s.base = p.getBuiltDistricts().stream().mapToInt(DistrictCard::getCost).sum();
            Set<Color> cols = new HashSet<>();
            for (DistrictCard d : p.getBuiltDistricts()) {
                cols.add(d.getColor());
            }
            if (cols.containsAll(Arrays.asList(
                    Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.PURPLE))) {
                s.diversity = 3;
            }
            if (p.getCitySize() >= 8) {
                if (p.getId() == firstCompleter) s.bonusFirst = 4;
                else                             s.bonusOther = 2;
            }
            s.extra = 0;  // no unique extras implemented
            scores.add(s);
        }
        scores.sort(Comparator
            .comparingInt(Score::total).reversed()
            .thenComparingInt(s -> {
                Character c = assignments.get(s.p);
                return c == null ? 0 : -(c.ordinal() + 1);
            })
        );
        for (Score s : scores) {
            System.out.printf("Player %d:%n", s.p.getId());
            System.out.printf("  Base (sum costs)     = %d%n", s.base);
            System.out.printf("  Diversity bonus      = %d%n", s.diversity);
            if (s.bonusFirst > 0) System.out.printf("  First-complete bonus = %d%n", s.bonusFirst);
            if (s.bonusOther > 0) System.out.printf("  Other-complete bonus = %d%n", s.bonusOther);
            System.out.printf("  Total                = %d%n%n", s.total());
        }
        Score winner = scores.get(0);
        System.out.printf("Congratulations, Player %d wins with %d points!%n",
                          winner.p.getId(), winner.total());
        System.exit(0);
    }

    /**
     * List all character names in lowercase, comma-separated.
     * @return string of all character names
     */
    public String listAllCharacters() {
        StringBuilder sb = new StringBuilder();
        for (Character c : Character.values()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(c.name().toLowerCase());
        }
        return sb.toString();
    }

    /**
     * Show special ability text for a purple card at hand index.
     * @param idx 1-based hand index
     */
    public void showCardInfo(int idx) {
        HumanPlayer me = getHuman();
        if (idx < 1 || idx > me.getHandCards().size()) {
            System.out.println("Invalid card index.");
            return;
        }
        DistrictCard card = me.getHandCards().get(idx - 1);
        if (card.getColor() != Color.PURPLE || card.getText().isEmpty()) {
            System.out.println("No special ability.");
        } else {
            System.out.println("Special ability of " + card.getName() + ": " + card.getText());
        }
    }
}
