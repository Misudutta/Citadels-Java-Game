t Process turns – proceed to the next sequence in the game, eg the next computer 
player makes their turn 
hand Display the cards in your hand and the amount of gold you have 
gold Display the amount of gold you have, eg: You have 4 gold. 
build <H> Builds a district from your hand. The parameter h is the position of the card in your 
hand. A player cannot have duplicate buildings in their city. 
citadel [p], 
list [p], 
city [p] 
Display the current districts in the player’s city. If the parameter p is not provided, 
then display player 1’s city. Example: citadel 2 displays player 2’s city. 
action Gives info about your character’s special action. When you run this command, it 
should describe how you can perform your character’s special ability. For example, 
the Magician may do action swap <player number> to swap their hand 
with the given player. Or they may do action redraw <id1,id2,id3,…> to 
choose to discard the given number of district cards from their hand and redraw 
them from the deck, where id1, id2 etc are the positions in the player’s hand when 
doing the hand command. 
info <H> Gives information about the special ability of a purple building in your hand. 
info <name> Gives information about a character, as specified on page 2 
end Ends your turn. Display: You ended your turn. 
all Display info about all players, including number of cards in hand, gold and districts 
built in their city. See the example on the previous page 
save <file> Saves the current game state info using the JSON file format in the given file. 
load <file> Load the game state from the given file. 
help Display the help message, similar to this: 
Available commands: 
info : show information about a character or building 
t : processes turns 
 
all : shows all current game info 
citadel/list/city : shows districts built by a player 
hand : shows cards in hand 
gold [p] : shows gold of a player 
 
build <place in hand> : Builds a building into your city 
action : Gives info about your special action and how to 
perform it 
end : Ends your turn 
debug Toggles debug mode, where the hand of computer-controlled players will be visible 
on their turn in a debug message. 


My Citadels implementation embraces core object‐oriented principles to create a flexible, maintainable design. At its foundation is an abstract Player class that encapsulates shared state—player ID, gold, hand, and built city—and declares abstract methods draw(...) and takeTurn(...). I then extend this base with two concrete subclasses: HumanPlayer, whose actions are driven via console commands, and AIPlayer, which follows a simple rule set (draw or take gold, then build the most expensive affordable district). 
I model fixed domains—character roles and card colors—as Java enums. The Character enum carries a rank and a custom toString() format, while Color offers a fromString(...) factory method. Using enums enforces compile‐time safety and prevents invalid values. For composition, the Game class owns a DistrictDeck (strong composition) and aggregates a list of Player objects, whereas each Player maintains lists of DistrictCard for hand and city. By favoring composition over inheritance for “has‐a” relationships, I keep domain entities modular and easily testable.
I separate concerns by isolating all console I/O in a CommandProcessor class that reads user input and delegates to Game methods. This adheres to the Single Responsibility Principle: the game engine remains focused on rules and state transitions while the processor handles parsing and user interaction. I covered every public API and many edge cases with JUnit tests in SampleTest.java, driving over 80% code coverage. Tests validate deck initialization, income/build logic, phase transitions, save/load behavior, and end‐game scoring.
To extend persistence, I implemented JSON‐backed save and load using the JSON‐Simple library. The Game.save(filename) method serializes the entire game state—including phase, turn pointers, player gold, hand contents, and built cities—into a JSON file. Its counterpart, Game.load(filename), parses that file, restores the game phase, reconstructs each player (human or AI) with correct gold, and refills their hand and city lists by name lookup. All exceptions during I/O are caught and reported as user‐friendly console messages, ensuring robustness without altering existing game logic. This extension exemplifies the Open/Closed Principle: I added persistence capabilities without modifying core classes or disrupting the turn‐taking rules.
