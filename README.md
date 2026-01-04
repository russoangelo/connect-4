# 🎮 Connect 4 - Multiplayer Game

A Java implementation of the classic Connect Four game featuring a beautiful JavaFX interface and real-time multiplayer gameplay over network.

![Java](https://img.shields.io/badge/Java-21+-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-Latest-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

## ✨ Features

- 🎯 **Real-time Multiplayer** - Two players compete over a local network
- 🎨 **Animated Interface** - Smooth disc-dropping animations with JavaFX
- 🔄 **Turn Management** - Automatic turn synchronization between players
- 🏆 **Win Detection** - Automatic detection of wins, draws, and disconnections
- 🎭 **Player Colors** - Red (X) vs Yellow (O) discs
- 📡 **TCP Sockets** - Reliable client-server communication

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Gradle 8.0+
- JavaFX SDK

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/russoangelo/connect-4.git
   cd connect-4
   ```

2. **Build the project**
   ```bash
   ./gradlew build
   ```

### Running the Game

#### Start the Server
```bash
./gradlew run --main-class=com.connectfour.connect4.Server
```
The server will start on port `12345` and wait for two players.

#### Start Client 1
Open a new terminal:
```bash
./gradlew run --main-class=com.connectfour.connect4.ClientGUI
```

#### Start Client 2
Open another terminal:
```bash
./gradlew run --main-class=com.connectfour.connect4.ClientGUI
```

## 🎮 How to Play

1. **Start the server** - It will wait for two players to connect
2. **Launch two clients** - First player gets Red (X), second gets Yellow (O)
3. **Take turns** - Click on a column number to drop your disc
4. **Win condition** - Connect four discs horizontally, vertically, or diagonally
5. **Game ends** - When someone wins, the board is full, or a player disconnects

## 🏗️ Project Structure

```
connect-4/
├── src/main/java/com/connectfour/connect4/
│   ├── Server.java           # Main server (port 12345)
│   ├── ClientGUI.java        # JavaFX client interface
│   ├── ClientHandler.java    # Handles client connections
│   ├── Game.java             # Game logic and win detection
├── build.gradle.kts
└── README.md
```

## 🔧 Technical Details

### Architecture

- **Server**: Multi-threaded TCP server handling two simultaneous connections
- **Client**: JavaFX application with socket communication
- **Protocol**: Custom text-based protocol over TCP
- **Game Logic**: Synchronized game state shared between threads

### Network Protocol

**Server → Client:**
- `PLAYER_ID:X/O` - Assigns player symbol
- `BOARD_START` - Begins board transmission
- `BOARD_END` - Ends board transmission
- `YOUR_TURN` - Notifies player's turn
- `WAIT_TURN` - Wait for opponent
- `WIN/LOSE/DRAW` - Game result

**Client → Server:**
- `0-6` - Column number for move

### Key Classes

- **`Game`**: Core game logic, board state, win detection
- **`Server`**: Accepts connections and creates game instances
- **`ClientHandler`**: Manages individual client communication
- **`ClientGUI`**: JavaFX interface with animations

## 🎨 Customization

### Change Server Port
Edit `Server.java`:
```java
final int PORT = 12345; // Change to preferred port
```

### Modify Board Size
Edit `Game.java`:
```java
public static final int ROWS = 6;
public static final int COLS = 7;
```

### Animation Speed
Edit `ClientGUI.java`:
```java
transition.setDuration(Duration.millis(500));
```

## 🐛 Known Issues

- Server must be restarted between games
- No reconnection support if client disconnects
- Requires manual IP configuration for non-localhost play

## 📝 License

This project is open source and available under the MIT License.

## 👤 Author

**Angelo Russo**
- GitHub: [@russoangelo](https://github.com/russoangelo)

## ⭐ Show Your Support

Give a ⭐️ if you like this project!
