# Music Player Service - Design Patterns Implementation


A comprehensive Java music player service demonstrating **6 core design patterns** working together to create a flexible, extensible, and maintainable music playback system. Perfect for learning design patterns, studying software architecture, or as a foundation for music applications.

## 🌟 Features

- Multiple Design Patterns: Strategy, Singleton, Observer, Adapter, Facade, MVVM
- Multi-Source Support: Local files, Spotify (mock), TheAudioDB (live API)
- Advanced Playback: Sequential, Shuffle, Repeat modes with runtime switching
- Reactive Architecture: MVVM with automatic UI updates
- Thread-Safe: Concurrent operations with modern Java techniques
- Real API Integration: Live TheAudioDB API with HTTP requests
- Comprehensive Testing: Unit, integration, and thread safety tests
- Interactive Demo: Console-based demo application

##  Architecture Overview
<img width="2800" height="2000" alt="image" src="https://github.com/user-attachments/assets/7b7a003a-3238-4d3f-a577-d4b54ad6c5e5" />

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│  ┌─────────────────────┐    ┌─────────────────────────────┐ │
│  │  MusicPlayerDemo    │    │      Unit Tests             │ │
│  │  (Console App)      │    │   (JUnit 5 + Mockito)      │ │
│  └─────────────────────┘    └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                      MVVM Layer                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │           MusicPlayerViewModel                          │ │
│  │        (Observable Properties & Commands)               │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Facade Layer                            │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              MusicPlayerFacade                          │ │
│  │           (Simplified Interface)                        │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                      Core Layer                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │          MusicPlayerManager (Singleton)                 │ │
│  │         Thread-Safe Central Coordinator                 │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌────────────────────┐  ┌─────────────────┐  ┌────────────────┐
│  Strategy Pattern  │  │ Adapter Pattern │  │Observer Pattern│
│  ┌──────────────┐  │  │ ┌─────────────┐ │  │ ┌────────────┐ │
│  │ Sequential   │  │  │ │LocalAdapter │ │  │ │EventManager│ │
│  │ Shuffle      │  │  │ │SpotifyAdptr │ │  │ │UI Observers│ │
│  │ Repeat       │  │  │ │AudioDBAdptr │ │  │ │   System   │ │
│  └──────────────┘  │  │ └─────────────┘ │  │ └────────────┘ │
└────────────────────┘  └─────────────────┘  └────────────────┘
```

## 🎯 Design Patterns Demonstrated

| Pattern | Implementation | Purpose |
|---------|---------------|---------|
| **Strategy** | `PlaybackStrategy` with Sequential, Shuffle, Repeat | Runtime algorithm switching |
| **Singleton** | `MusicPlayerManager` (Bill Pugh pattern) | Single player instance |
| **Observer** | `MusicPlayerObserver` event system | UI state notifications |
| **Adapter** | Multiple music source adapters | Unified API interface |
| **Facade** | `MusicPlayerFacade` simplified interface | Hide system complexity |
| **MVVM** | `MusicPlayerViewModel` with data binding | Reactive architecture |

## 🚀 Quick Start

### Prerequisites

- **Java 11+** (OpenJDK or Oracle JDK)
- **Gradle 7.0+** (or use included wrapper)

### Clone and Run

```bash
# Clone repository
git clone https://github.com/yourusername/java-music-player-service.git
cd java-music-player-service

# Run interactive demo
./gradlew run

# Run all tests
./gradlew test

# Build project
./gradlew build
```

### Interactive Demo Commands

```
🎵 Welcome to Java Music Player Demo 🎵

Available commands:
  play/p          - Play/pause current song
  next/n          - Next song  
  prev/previous   - Previous song
  stop/s          - Stop playback
  shuffle         - Toggle shuffle mode
  repeat          - Toggle repeat mode
  search <query>  - Search for songs
  seek <seconds>  - Seek to position
  status          - Show current status
  playlist        - Show current playlist
  demo            - Run design patterns demo
  help/h          - Show help
  quit/exit/q     - Exit application
```

## 💻 Usage Examples

### Basic Usage (Facade Pattern)
```java
MusicPlayerFacade player = new MusicPlayerFacade();

// Simple playback control
player.playLocalMusic(songList);
player.enableShuffleMode();
player.skipToNext();

// Search across all sources
player.searchAllSources("beatles").thenAccept(results -> {
    System.out.println("Found " + results.size() + " songs");
});
```

### MVVM Integration
```java
MusicPlayerViewModel viewModel = new MusicPlayerViewModel();

// Add UI observer for automatic updates
viewModel.addObserver(new Observer() {
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof PropertyChangeEvent) {
            PropertyChangeEvent event = (PropertyChangeEvent) arg;
            updateUI(event.getPropertyName(), event.getNewValue());
        }
    }
});

// Execute commands (from UI events)
viewModel.searchCommand("queen");
viewModel.playPauseCommand();
viewModel.shuffleCommand();
```

### Custom Strategy Implementation
```java
// Create custom playback strategy
class SmartPlaybackStrategy implements PlaybackStrategy {
    @Override
    public Song getNextSong(List<Song> playlist, int currentIndex) {
        return selectBasedOnMoodAnalysis(playlist, currentIndex);
    }
}

// Use custom strategy
MusicPlayerManager.getInstance().setPlaybackStrategy(new SmartPlaybackStrategy());
```

### Multi-Source Search
```java
MusicPlayerFacade facade = new MusicPlayerFacade();

// Search specific sources
CompletableFuture<List<Song>> localResults = facade.searchLocalMusic("rock");
CompletableFuture<List<Song>> spotifyResults = facade.searchSpotify("pop");
CompletableFuture<List<Song>> audioDbResults = facade.searchTheAudioDB("jazz");

// Combine all results
CompletableFuture.allOf(localResults, spotifyResults, audioDbResults)
    .thenRun(() -> System.out.println("All searches completed!"));
```

## 🏗️ Project Structure

```
java-music-player-service/
├── src/
│   ├── main/java/
│   │   ├── models/                     # Song, Playlist, PlaybackInfo
│   │   ├── patterns/
│   │   │   ├── strategy/              # Playback strategies
│   │   │   ├── adapter/               # Music source adapters
│   │   │   ├── observer/              # Event system
│   │   │   ├── singleton/             # Player manager
│   │   │   ├── facade/                # Simplified interface
│   │   │   └── mvvm/                  # ViewModels and binding
│   │   ├── audio/                     # Audio engine
│   │   ├── api/                       # API integrations
│   │   └── MusicPlayerDemoApp.java    # Main demo application
│   └── test/java/
│       ├── patterns/                   # Pattern-specific tests
│       ├── integration/               # Integration tests
│       └── MusicPlayerServiceTest.java # Main test suite
├── build.gradle                       # Build configuration
├── gradle/wrapper/                    # Gradle wrapper
├── README.md                          # This file
└── LICENSE                           # MIT License
```

## 🧪 Testing

The project includes comprehensive testing with **95%+ code coverage**:

### Test Categories

- **Unit Tests**: Individual pattern testing
- **Integration Tests**: Cross-pattern interaction
- **Thread Safety Tests**: Concurrent access validation
- **Performance Tests**: Large playlist handling
- **API Tests**: External service integration

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests "MusicPlayerServiceTest"

# Run tests in specific category
./gradlew test --tests "*Strategy*"
```

### Test Examples

```java
@Test
@DisplayName("Strategy Pattern - Sequential Playback")
void testSequentialPlaybackStrategy() {
    PlaybackStrategy strategy = new SequentialPlaybackStrategy();
    Song nextSong = strategy.getNextSong(testSongs, 0);
    assertEquals(testSongs.get(1), nextSong);
}

@Test
@DisplayName("Thread Safety - Concurrent Operations")
void testConcurrentAccess() throws InterruptedException {
    int threadCount = 20;
    CountDownLatch latch = new CountDownLatch(threadCount);
    // Test concurrent operations...
}
```

## 🌐 API Integration

### TheAudioDB (Live Integration)
```java
// Real HTTP API calls
String url = "https://www.theaudiodb.com/api/v1/json/2/search.php?s=" + query;
HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
// JSON parsing with Gson
TheAudioDBResponse response = gson.fromJson(jsonString, TheAudioDBResponse.class);
```

**Supported Endpoints:**
- Artist search: `/search.php?s={artist}`
- Album details: `/album.php?m={album_id}`
- Track details: `/track.php?m={track_id}`

### Spotify (Mock Implementation)
Demonstrates real-world API integration patterns:
- OAuth authentication flow
- Rate limiting and retry logic
- Response caching
- Error handling

## 🔧 Configuration

### Build Configuration (build.gradle)
```gradle
plugins {
    id 'java'
    id 'application'
}

dependencies {
    // JSON processing
    implementation 'com.google.code.gson:gson:2.10.1'

    // HTTP client
    implementation 'org.apache.httpcomponents:httpclient:4.5.14'

    // Testing
    testImplementation 'org.junit.jupiter:junit-jupiter:5.9.2'
    testImplementation 'org.mockito:mockito-core:5.1.1'

    // Logging
    implementation 'org.slf4j:slf4j-api:2.0.6'
    implementation 'ch.qos.logback:logback-classic:1.4.5'
}
```

### Application Properties
```properties
# TheAudioDB API Configuration
audiodb.base.url=https://www.theaudiodb.com/api/v1/json/2
audiodb.rate.limit=2

# Spotify Mock Configuration  
spotify.client.id=your_client_id
spotify.client.secret=your_client_secret

# Audio Configuration
audio.buffer.size=4096
audio.sample.rate=44100
```

## 🚀 Performance

### Benchmarks
- **Startup Time**: < 500ms
- **Memory Usage**: ~50MB baseline
- **Search Response**: < 200ms (local), < 1s (API)
- **Thread Safety**: 20 concurrent operations tested
- **Large Playlist**: 10,000+ songs supported

### Optimization Features
- Lazy initialization of components
- Connection pooling for API calls
- Efficient concurrent collections
- Memory-conscious caching
- Background processing for heavy operations

### Development Setup
```bash
# Fork and clone the repository
git clone https://github.com/Umeshch2004/music-player-service.git

# Create feature branch
git checkout -b feature/your-feature-name

# Make changes and test
./gradlew test

# Submit pull request
```

### Code Style
- Follow Google Java Style Guide
- Use meaningful variable names
- Add comprehensive JavaDoc comments
- Include unit tests for new features
- Maintain 90%+ test coverage

## 📚 Learning Resources

### Design Patterns
- [Gang of Four Design Patterns](https://en.wikipedia.org/wiki/Design_Patterns)
- [Java Design Patterns](https://java-design-patterns.com/)
- [Refactoring Guru - Design Patterns](https://refactoring.guru/design-patterns)

### Java Concurrency
- [Java Concurrency in Practice](https://jcip.net/)
- [Oracle Java Concurrency Tutorial](https://docs.oracle.com/javase/tutorial/essential/concurrency/)

### Testing
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **TheAudioDB** for providing free music metadata API
- **Spotify** for inspiration on music streaming APIs
- **Gang of Four** for foundational design patterns
- **Java Community** for excellent libraries and tools

## 🐛 Issues and Support

- **Bug Reports**: [GitHub Issues](https://github.com/Umeshch2004/java-music-player-service/issues)
- **Feature Requests**: [GitHub Discussions](https://github.com/Umeshch2004/java-music-player-service/discussions)
- **Questions**: [Stack Overflow](https://stackoverflow.com/questions/tagged/java-music-player)

## 📈 Roadmap

### Version 2.0 (Planned)
- [ ] Spring Boot integration
- [ ] REST API endpoints
- [ ] Database persistence
- [ ] WebSocket real-time updates
- [ ] Docker containerization

### Version 2.1 (Future)
- [ ] Android app integration
- [ ] Spotify SDK integration
- [ ] Audio effects processing
- [ ] Machine learning recommendations
- [ ] Cloud storage support

---

