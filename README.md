# About

You are a master builder racing to construct the most magnificent city in the realm. But you're
not alone thieves lurk in the shadows ready to drain your treasury, assassins wait to cut
down your allies, and warlords are itching to tear your districts to rubble. Every round you
must outwit your opponents, pick your character wisely, and spend your gold before someone
else takes it. Build 8 districts before anyone else and claim victory but the points don't
lie, and a cleverly constructed city will always beat a rushed one.

Citadels is a turn-based strategy card game where hidden information and character bluffing
are just as important as resource management. You pick a secret role each round from a cast
of 8 characters each with their own agenda then take your turn collecting gold, drawing
cards, and laying down districts. The catch? You never know exactly who your opponents are
playing as until it's too late. The Assassin might silence the King before he even gets to
move. The Thief might strip the Merchant bare the moment they reveal themselves. Every round
is a calculated gamble, and the city you build is a reflection of every decision you made
along the way.

This implementation is a command-line Java version of the classic Citadels card game,
supporting 4–7 players with one human player and the rest computer-controlled. The AI
opponents make intelligent decisions hoarding gold when they need to build, switching to
card-draw characters when their hand runs dry, and always trying to build the most valuable
district they can afford. A full save/load system lets you pick up any game right where you
left off.

---

## Prerequisites

- **Java JDK 8+** — [Download here](https://adoptium.net/)
- **Gradle** (or use the included Gradle wrapper)

---

## Setup

Clone the repo and navigate into it:

```bash
git clone <your-repo-url>
cd <repo-folder>
```

---

## Running the Game

### Build the jar

```bash
./gradlew jar
```

> On Windows, use `gradlew.bat jar`

### Launch the game

```bash
java -jar build/libs/citadels.jar
```

You'll be prompted to enter the number of players (4–7) and the game begins immediately.

---

## Key Commands

| Command | Description |
|---|---|
| `t` | Process next turn / advance the game |
| `hand` | View your cards and gold |
| `gold` | Check how much gold you have |
| `build <n>` | Build the nth card from your hand |
| `citadel` / `city` / `list` | View a player's built districts |
| `all` | See all players' status at a glance |
| `end` | End your turn |
| `save <file>` | Save the current game state to a JSON file |
| `load <file>` | Load a saved game from a JSON file |
| `debug` | Toggle visibility of computer players' hands |
| `help` | Display all available commands |

---

## Running Tests

```bash
./gradlew test
```

To generate a code coverage report:

```bash
./gradlew test jacocoTestReport
```

Report will be available at `build/reports/jacoco/`.
