package citadels;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SampleTest {

    private static class NoExitSecurityManager extends SecurityManager {
        @Override public void checkPermission(java.security.Permission perm) {}
        @Override public void checkPermission(java.security.Permission perm, Object context) {}
        @Override public void checkExit(int status) {
            throw new SecurityException("System.exit(" + status + ")");
        }
    }

    @BeforeAll
    public void disableExit() {
        System.setSecurityManager(new NoExitSecurityManager());
    }

    @AfterAll
    public void enableExit() {
        System.setSecurityManager(null);
    }

    private Game freshGame() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        return new Game();
    }


    @Test public void testDistrictCardConstructorAndGetters() {
        DistrictCard card = new DistrictCard("Market", Color.GREEN, 2, "");
        assertEquals("Market", card.getName());
        assertEquals(Color.GREEN, card.getColor());
        assertEquals(2, card.getCost());
        assertEquals("", card.getText());
    }

    @Test public void testDistrictCardDisplay() {
        DistrictCard card = new DistrictCard("Castle", Color.YELLOW, 4, "");
        assertEquals("Castle [yellow4]", card.display());
    }

    @Test public void testColorFromStringValid() {
        assertEquals(Color.RED, Color.fromString("red"));
        assertEquals(Color.BLUE, Color.fromString("Blue"));
    }

    @Test public void testColorFromStringInvalid() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Color.fromString("invalid");
        });
        assertTrue(e.getMessage().contains("Unknown color"));
    }

    @Test public void testCharacterEnumValues() {
        assertEquals("assassin(1)", Character.ASSASSIN.toString());
        assertEquals(6, Character.MERCHANT.rank);
    }

    @Test public void testDistrictDeckInitializationAndDraw() {
        String mockTSV = "Name\tQty\tcolor\tcost\ttext\nCastle\t2\tyellow\t4\t\n";
        ByteArrayInputStream input = new ByteArrayInputStream(mockTSV.getBytes());
        DistrictDeck deck = new DistrictDeck(input);
        DistrictCard d1 = deck.draw();
        DistrictCard d2 = deck.draw();
        assertNotNull(d1);
        assertNotNull(d2);
        assertTrue(deck.isEmpty());
    }

    @Test public void testDistrictDeckEmptyOnZeroQty() {
        String mockTSV = "Name\tQty\tcolor\tcost\ttext\nEmpty\t0\tred\t1\t\n";
        ByteArrayInputStream input = new ByteArrayInputStream(mockTSV.getBytes());
        DistrictDeck deck = new DistrictDeck(input);
        assertTrue(deck.isEmpty());
    }

    @Test public void testHumanPlayerTakeGold() {
        HumanPlayer p = new HumanPlayer(1);
        int before = p.getGold();
        p.takeGold();
        assertEquals(before + 1, p.getGold());
    }

    @Test public void testHumanPlayerBuildFromHand() {
        HumanPlayer p = new HumanPlayer(1);
        p.addGold(3);
        p.getHandCards().add(new DistrictCard("Market", Color.GREEN, 3, ""));
        p.buildFromHand(1, null);
        assertEquals(2, p.getGold());
        assertEquals(1, p.getCitySize());
    }

    @Test public void testAIPlayerBuildIndex() {
        AIPlayer ai = new AIPlayer(99);
        DistrictCard card = new DistrictCard("Test", Color.BLUE, 2, "");
        ai.getHandCards().add(card);
        ai.addGold(2);
        assertFalse(ai.buildIndex(0));
        assertFalse(ai.buildIndex(2));
        assertTrue(ai.buildIndex(1));
        assertEquals(2, ai.getGold());
        assertTrue(ai.getBuiltDistricts().contains(card));
    }

    @Test public void testAIPlayerTurnLogic() {
        String mockTSV = "Name\tQty\tcolor\tcost\ttext\nCastle\t5\tyellow\t3\t\n";
        ByteArrayInputStream input = new ByteArrayInputStream(mockTSV.getBytes());
        DistrictDeck deck = new DistrictDeck(input);

        AIPlayer ai = new AIPlayer(2);
        ai.draw(deck, 4);
        ai.addGold(3);

        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game dummyGame = new Game();
        ai.takeTurn(dummyGame);

        assertTrue(ai.getCitySize() >= 0);
    }

    @Test public void testGamePhaseTransition() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        assertEquals(Game.Phase.SELECTION, g.getPhase());
    }

    @Test public void testGameHumanIncomeGold() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        for (int i = 0; i < 10; i++) g.pressT();
        if (g.getCurrentPlayer() instanceof HumanPlayer) {
            int before = g.getHuman().getGold();
            g.humanTakeGoldIncome();
            assertEquals(before + 2, g.getHuman().getGold());
        }
    }

    @Test public void testGameEndScoringLogic() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        HumanPlayer p = g.getHuman();
        p.addGold(100);
        for (int i = 0; i < 8; i++) {
            p.getHandCards().add(new DistrictCard("Test", Color.RED, 1, ""));
            p.buildFromHand(1, g.getDeck());
        }
        g.checkEndTrigger(p);
        assertTrue(p.getCitySize() >= 8);
    }

    @Test public void testPlayerGettersAndAddGold() {
        HumanPlayer p = new HumanPlayer(5);
        assertEquals(5, p.getId());
        assertEquals(2, p.getGold());
        assertEquals(0, p.getHandSize());
        assertEquals(0, p.getCitySize());
        assertNotNull(p.getHandCards());
        assertNotNull(p.getBuiltDistricts());
        p.addGold(10);
        assertEquals(12, p.getGold());
    }

    @Test public void testCommandProcessorInfoCharacter() {
        assertDoesNotThrow(() -> {
            Character c = Character.valueOf("MAGICIAN");
            assertEquals("magician(3)", c.toString());
        });
    }

    @Test public void testShowHelp() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        g.showHelp();
    }

    @Test public void testShowCardInfo() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        g.getHuman().getHandCards().add(new DistrictCard("Keep", Color.PURPLE, 3, "Cannot be destroyed"));
        g.showCardInfo(1);
        g.showCardInfo(5);
    }

    @Test public void testToggleDebug() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        g.toggleDebug();
        g.toggleDebug();
    }

    @Test public void testListAllCharacters() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        String all = g.listAllCharacters();
        assertTrue(all.contains("assassin"));
        assertTrue(all.contains("king"));
    }

    @Test public void testShowAll() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        g.showAll();
    }

    @Test public void testShowCityEmptyAndPopulated() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        g.showCity(1);
        assertTrue(out.toString().contains("(no districts built)"));

        out.reset();
        g.getHuman().getBuiltDistricts().add(new DistrictCard("TestTown", Color.BLUE, 2, ""));
        g.showCity(1);
        assertTrue(out.toString().contains("TestTown"));
    }

    @Test public void testSaveAndLoadCreatesFile() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        g.save("testsave.json");
        File f = new File("testsave.json");
        assertTrue(f.exists());
        g.load("testsave.json");
        f.delete();
    }

    @Test public void testLoadCorruptFileDoesNotThrow() throws IOException {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        File tmp = File.createTempFile("bad", ".json");
        try (FileWriter fw = new FileWriter(tmp)) {
            fw.write("not json");
        }
        assertDoesNotThrow(() -> g.load(tmp.getAbsolutePath()));
        tmp.delete();
    }

    @Test public void testDoSelectionStepAndTurnTransition() throws Exception {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        Field chooser = Game.class.getDeclaredField("chooserIndex");
        chooser.setAccessible(true);
        chooser.setInt(g, 0);

        assertEquals(Game.Phase.SELECTION, g.getPhase());
        g.pressT();                
        g.chooseCharacter("king");
        g.pressT(); g.pressT(); g.pressT(); 
        assertEquals(Game.Phase.TURN, g.getPhase());
    }

    @Test public void testScoreAndExitInvokesExit() throws Exception {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        HumanPlayer p = g.getHuman();
        p.addGold(100);
        for (int i = 0; i < 8; i++) {
            p.getHandCards().add(new DistrictCard("Z", Color.RED, 1, ""));
            p.buildFromHand(1, g.getDeck());
        }
        g.checkEndTrigger(p);

        Method scoreMethod = Game.class.getDeclaredMethod("scoreAndExit");
        scoreMethod.setAccessible(true);

        SecurityException ex = assertThrows(SecurityException.class, () -> {
            try {
                scoreMethod.invoke(g);
            } catch (InvocationTargetException ite) {
                throw (SecurityException) ite.getCause();
            }
        });
        assertTrue(ex.getMessage().contains("System.exit"));
    }

    @Test public void testTurnStepBranchesRunsWithoutError() {
        Game g = freshGame();
        g.pressT();               
        g.chooseCharacter("assassin");
        g.pressT(); g.pressT(); g.pressT();
        assertEquals(Game.Phase.TURN, g.getPhase(), "Should now be in TURN phase");
        for (int i = 0; i < Character.values().length; i++) {
            try {
                g.processT();
            } catch (SecurityException se) {
                break;
            } catch (Exception e) {
                fail("Unexpected exception in doTurnStep branch " + i + ": " + e);
            }
        }
    }

    @Test public void testHumanDrawIncomeValidAndInvalid() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        assertDoesNotThrow(g::humanDrawIncome);

        g.pressT(); g.chooseCharacter("merchant");
        g.pressT(); g.pressT(); g.pressT();
        while (!(g.getCurrentPlayer() instanceof HumanPlayer)) {
            g.pressT();
        }
        System.setIn(new ByteArrayInputStream("1\n".getBytes()));
        assertDoesNotThrow(g::humanDrawIncome);
    }

    @Test public void testHumanBuildAndFlagsBeforeTurn() {
        System.setIn(new ByteArrayInputStream("4\n".getBytes()));
        Game g = new Game();
        assertDoesNotThrow(() -> g.humanBuild(1));
        assertFalse(g.isIncomeTaken());
        assertFalse(g.hasBuiltThisTurn());
    }

    @Test
    void testGettersOnGameAndPlayer() {
        Game g = freshGame();
        assertNotNull(g.getDeck());
        assertEquals(Game.Phase.SELECTION, g.getPhase());
        assertNotNull(g.getHuman());
        assertEquals(1, g.getHuman().getId());
        assertFalse(g.isIncomeTaken());
        assertFalse(g.hasBuiltThisTurn());
    }

    @Test
    void testProcessTAdvancesSelectionAndTurns() throws Exception {
        Game g = freshGame();
        Field chooserField = Game.class.getDeclaredField("chooserIndex");
        chooserField.setAccessible(true);
        chooserField.setInt(g, 0);
        g.processT();
        g.chooseCharacter("king");
        g.processT(); g.processT(); g.processT();
        assertEquals(Game.Phase.TURN, g.getPhase());
        for (int i = 0; i < Character.values().length + 1; i++) {
            g.processT();
        }
        assertEquals(Game.Phase.SELECTION, g.getPhase());
    }

    @Test
    void testDoTurnStepNoOwnerBranch() throws Exception {
        Game g = freshGame();
        Field avail = Game.class.getDeclaredField("availableChars");
        avail.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Character> list = (List<Character>) avail.get(g);
        list.clear(); list.add(Character.ARCHITECT);
        Field chooser = Game.class.getDeclaredField("chooserIndex");
        chooser.setAccessible(true);
        chooser.setInt(g, 0);

        g.pressT();                       
        g.chooseCharacter("architect");   
        g.pressT(); g.pressT(); g.pressT();  
        assertEquals(Game.Phase.TURN, g.getPhase());

       
        for (int i = 0; i < 5; i++) {
            g.processT();
        }
    }

    @Test
    void testHumanBuildAndIncomeEdgeCases() {
        Game g = freshGame();
        assertDoesNotThrow(() -> g.humanBuild(42));
        g.pressT(); g.chooseCharacter("merchant");
        g.pressT(); g.pressT(); g.pressT();
        while (!(g.getCurrentPlayer() instanceof HumanPlayer)) {
            g.processT();
        }
        assertDoesNotThrow(g::humanDrawIncome);
        assertDoesNotThrow(g::humanTakeGoldIncome);
        assertDoesNotThrow(() -> g.humanBuild(99));
    }

    @Test
    void testShowHandShowAllShowHelp() {
        Game g = freshGame();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        g.showHelp();
        assertTrue(out.toString().contains("Available commands"));

        out.reset();
        g.showAll();
        assertTrue(out.toString().contains("Player 1"));

        out.reset();
        g.showHand();
        assertTrue(out.toString().toLowerCase().contains("gold"));
    }

    @Test
    void testSaveLoadInvalidAndValid() throws IOException {
        Game g = freshGame();
        File bad = File.createTempFile("bad", ".json");
        try (FileWriter fw = new FileWriter(bad)) { fw.write("not json"); }
        assertDoesNotThrow(() -> g.load(bad.getAbsolutePath()));
        bad.delete();

        g.save("tmp.json");
        File good = new File("tmp.json");
        assertTrue(good.exists());
        assertDoesNotThrow(() -> g.load("tmp.json"));
        good.delete();
    }

    @Test
    void testChooseCharacterInvalidAndValid() {
        Game g = freshGame();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        g.chooseCharacter("nope");
        assertTrue(out.toString().toLowerCase().contains("invalid"));

        out.reset();
        g.chooseCharacter("assassin");
        assertTrue(out.toString().toLowerCase().contains("player 1"));
    }

    @Test
    void testListAllCharactersAndPrivateListChars() throws Exception {
        Game g = freshGame();
        assertTrue(g.listAllCharacters().contains("assassin"));

        Method m = Game.class.getDeclaredMethod("listChars");
        m.setAccessible(true);
        String list = (String) m.invoke(g);
        assertTrue(list.contains("ASSASSIN"));
    }

}


