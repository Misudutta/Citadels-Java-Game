The game begins by prompting the user on how many players will be in the game. Then the initial setup of the game occurs, which involves shuffling the district card deck, randomly assigning the crown token, giving each player 2 gold tokens, and dealing 4 district cards to each player. Then rounds begin, and each round is divided into two phases: Character Selection phase, and Turn phase. Rounds will continue until the game ends (if one player builds 8 districts in their city, then the game will end at the end of the round) then points are totalled, and a winner is determined. 

| Command        | Description |
|----------------|------------|
| t              | Process turns — proceed to the next sequence in the game, e.g., the next computer player makes their turn. |
| hand           | Display the cards in your hand and the amount of gold you have. |
| gold           | Display the amount of gold you have (e.g., You have 4 gold). |
| build <h>      | Builds a district from your hand. h is the position of the card in your hand. Duplicate buildings are not allowed. |
| citadel [p]    | Display the districts in a player's city. |
| list [p]       | If p is not provided, defaults to player 1. |
| city [p]       | Example: citadel 2 displays player 2’s city. |
| action         | Gives info about your character’s special action and how to perform it (e.g., swap or redraw). |
| info <h>       | Gives information about a purple building in your hand. |
| info <name>    | Gives information about a character. |
| end            | Ends your turn. Output: You ended your turn. |
| all            | Display info about all players, including cards in hand, gold, and districts built. |
| save <file>    | Saves the current game state in JSON format. |
| load <file>    | Loads the game state from a file. |
| help           | Displays the help message. |
| debug          | Toggles debug mode (shows computer players’ hands). |

