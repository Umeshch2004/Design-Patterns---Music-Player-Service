# Design Patterns Documentation

## Overview

This music player service demonstrates the implementation of core design patterns in iOS development using Swift, Combine, and MVVM architecture. The solution provides a flexible, extensible, and maintainable framework for building music applications.

## Design Patterns Implemented

### 1. Singleton Pattern

**Purpose**: Ensure only one instance of the music player service exists throughout the application lifecycle.

**Implementation**: `MusicPlayerService.shared`

**Benefits**:
- Single point of audio session management
- Consistent state across the application
- Thread-safe implementation
- Proper resource management

**Code Example**:
```swift
final class MusicPlayerService: ObservableObject {
    static let shared = MusicPlayerService()
    
    private init() {
        // Private initializer prevents external instantiation
        setupAudioSession()
        setupRemoteControls()
    }
}
```

**When to Use**:
- Audio session management
- Global application state
- Resource-intensive objects that should be shared

### 2. Strategy Pattern

**Purpose**: Define a family of algorithms (music sources), encapsulate each one, and make them interchangeable.

**Implementation**: `MusicSourceProtocol` with concrete implementations

**Benefits**:
- Easy to add new music sources
- Runtime switching between providers
- Unified interface for different data sources
- Testable individual strategies

**Code Example**:
```swift
protocol MusicSourceProtocol {
    func searchSongs(query: String) -> AnyPublisher<[Song], Error>
    func loadSong(id: String) -> AnyPublisher<Song, Error>
}

class LocalMusicSource: MusicSourceProtocol { /* Implementation */ }
class SpotifyMusicSource: MusicSourceProtocol { /* Implementation */ }
class AudioDBMusicSource: MusicSourceProtocol { /* Implementation */ }
```

**When to Use**:
- Multiple ways to perform the same operation
- Runtime algorithm selection
- Avoiding conditional statements for algorithm selection

### 3. Observer Pattern

**Purpose**: Define a one-to-many dependency between objects so that when one object changes state, all dependents are notified.

**Implementation**: `PlayerObserver` protocol with notification service

**Benefits**:
- Decoupled communication
- Multiple observers can react to state changes
- Easy to add/remove observers
- Real-time UI updates

**Code Example**:
```swift
protocol PlayerObserver: AnyObject {
    func playerStateDidChange(_ newState: PlayerState)
    func playerProgressDidChange(_ progress: PlaybackProgress)
    func playerSongDidChange(_ song: Song?)
}

class NotificationService {
    private var observers: [WeakObserverWrapper] = []
    
    func addObserver(_ observer: PlayerObserver) {
        observers.append(WeakObserverWrapper(observer))
    }
    
    func notifyStateChange(_ newState: PlayerState) {
        observers.forEach { $0.observer?.playerStateDidChange(newState) }
    }
}
```

**When to Use**:
- UI components need to react to model changes
- Event-driven architecture
- Loose coupling between components

### 4. Command Pattern

**Purpose**: Encapsulate requests as objects, allowing you to parameterize clients with different requests, queue operations, and support undo operations.

**Implementation**: `MusicPlayerCommand` protocol with concrete command classes

**Benefits**:
- Undo/Redo functionality
- Command queuing and logging
- Macro commands (composite operations)
- Decouples invoker from receiver

**Code Example**:
```swift
protocol MusicPlayerCommand {
    func execute() -> AnyPublisher<Void, Error>
    func undo() -> AnyPublisher<Void, Error>
    var canUndo: Bool { get }
}

class PlayCommand: MusicPlayerCommand {
    func execute() -> AnyPublisher<Void, Error> {
        return service.internalPlay()
    }
}

class SeekCommand: MusicPlayerCommand {
    private let time: TimeInterval
    private var previousTime: TimeInterval?
    
    func execute() -> AnyPublisher<Void, Error> {
        previousTime = service.playbackProgress?.currentTime
        return service.internalSeek(to: time)
    }
    
    func undo() -> AnyPublisher<Void, Error> {
        guard let previousTime = previousTime else { 
            return Fail(error: MusicPlayerError.invalidSeekTime).eraseToAnyPublisher() 
        }
        return service.internalSeek(to: previousTime)
    }
}
```

**When to Use**:
- Need undo/redo functionality
- Want to queue operations
- Need to log or audit operations
- Decouple action triggers from action handlers

### 5. Factory Pattern

**Purpose**: Create objects without specifying their concrete classes, delegating the instantiation to factory methods.

**Implementation**: `MusicServiceFactory` class

**Benefits**:
- Centralized object creation
- Easy dependency injection
- Consistent object initialization
- Flexible object creation logic

**Code Example**:
```swift
protocol MusicServiceFactoryProtocol {
    func createMusicSource(for type: MusicSourceType) -> MusicSourceProtocol
    func createAudioPlayer() -> AudioPlayerProtocol
    func createQueueManager() -> QueueManagerProtocol
}

class MusicServiceFactory: MusicServiceFactoryProtocol {
    func createMusicSource(for type: MusicSourceType) -> MusicSourceProtocol {
        switch type {
        case .local: return LocalMusicSource()
        case .spotify: return SpotifyMusicSource()
        case .appleMusic: return AppleMusicSource()
        case .audiodb: return AudioDBMusicSource()
        }
    }
}
```

**When to Use**:
- Complex object creation logic
- Need to support multiple product families
- Want to centralize instantiation logic
- Configuration-based object creation

### 6. MVVM Pattern

**Purpose**: Separate presentation logic from business logic using data binding.

**Implementation**: ViewModels with Combine publishers and SwiftUI views

**Benefits**:
- Testable presentation logic
- Reactive data binding
- Clear separation of concerns
- Platform-independent ViewModels

**Code Example**:
```swift
class MusicPlayerViewModel: ObservableObject {
    @Published var currentSong: Song?
    @Published var playerState: PlayerState = .idle
    @Published var isPlaying: Bool = false
    
    private let musicPlayerService: MusicPlayerService
    private var cancellables = Set<AnyCancellable>()
    
    init(musicPlayerService: MusicPlayerService = .shared) {
        self.musicPlayerService = musicPlayerService
        setupBindings()
    }
    
    private func setupBindings() {
        musicPlayerService.$currentSong
            .assign(to: &$currentSong)
        
        musicPlayerService.$playerState
            .map { $0 == .playing }
            .assign(to: &$isPlaying)
    }
    
    func playPause() {
        if isPlaying {
            musicPlayerService.pause()
        } else {
            musicPlayerService.play()
        }
    }
}
```

**When to Use**:
- Building reactive UIs
- Need testable presentation logic
- Want to separate UI from business logic
- Platform-independent ViewModels

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                           UI Layer (SwiftUI)                    │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ MusicPlayerView │  │    QueueView    │  │  SearchResults  │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼ (Data Binding)
┌─────────────────────────────────────────────────────────────────┐
│                    ViewModel Layer (MVVM)                      │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │MusicPlayerVM    │  │  PlaylistVM     │  │   SearchVM      │  │
│  │@Published       │  │ @Published      │  │  @Published     │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼ (Commands & Queries)
┌─────────────────────────────────────────────────────────────────┐
│                Service Layer (Business Logic)                  │
├─────────────────────────────────────────────────────────────────┤
│                   MusicPlayerService (Singleton)               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ Command Pattern │  │Observer Pattern │  │Strategy Pattern │  │
│  │ ┌─────────────┐ │  │ ┌─────────────┐ │  │ ┌─────────────┐ │  │
│  │ │PlayCommand  │ │  │ │Notification │ │  │ │MusicSource  │ │  │
│  │ │PauseCommand │ │  │ │Service      │ │  │ │Protocol     │ │  │
│  │ │SeekCommand  │ │  │ │             │ │  │ │             │ │  │
│  │ └─────────────┘ │  │ └─────────────┘ │  │ └─────────────┘ │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼ (Factory Pattern)
┌─────────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                        │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │AudioPlayer      │  │QueueManager     │  │MusicSources     │  │
│  │(AVFoundation)   │  │                 │  │Local/Spotify    │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │AudioSession     │  │RemoteControl    │  │ServiceFactory   │  │
│  │Manager          │  │Manager          │  │                 │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow

1. **User Interaction**: User taps play button in SwiftUI view
2. **ViewModel**: MusicPlayerViewModel receives action, calls service
3. **Command**: Service creates PlayCommand and executes it
4. **Strategy**: Command delegates to appropriate music source
5. **Observer**: State changes notify all observers
6. **UI Update**: SwiftUI views reactively update based on published properties

## Key Benefits of This Architecture

### Maintainability
- Clear separation of concerns
- Single responsibility principle
- Easy to modify individual components

### Extensibility
- Easy to add new music sources (Strategy)
- Easy to add new commands (Command)
- Easy to add new UI components (Observer)

### Testability
- Mock implementations for all protocols
- Isolated business logic in ViewModels
- Dependency injection through Factory

### Performance
- Reactive updates only when needed
- Efficient queue management
- Background audio support

### User Experience
- Responsive UI with reactive data binding
- Background playback support
- Lock screen controls
- Queue management

## Testing Strategy

### Unit Tests
- Test each design pattern implementation
- Mock dependencies for isolated testing
- Verify command execution and undo functionality
- Test observer notifications

### Integration Tests
- Test complete user workflows
- Verify pattern interactions
- Test music source switching
- Validate background audio functionality

### Performance Tests
- Large playlist handling
- Memory usage optimization
- Battery usage analysis
- Network efficiency

## Best Practices Demonstrated

1. **Protocol-Oriented Programming**: Heavy use of protocols for abstraction
2. **Reactive Programming**: Combine publishers for data flow
3. **Memory Management**: Weak references in observers
4. **Error Handling**: Comprehensive error types and handling
5. **Documentation**: Extensive inline documentation
6. **Testing**: Comprehensive test coverage
7. **Accessibility**: VoiceOver support in UI components
8. **Performance**: Efficient data structures and algorithms

## Conclusion

This music player service demonstrates how multiple design patterns can work together to create a robust, maintainable, and extensible iOS application. Each pattern addresses specific challenges while contributing to the overall architecture's flexibility and testability.

The implementation showcases modern iOS development practices including Combine framework usage, SwiftUI integration, and proper audio session management, making it an excellent reference for building production-quality music applications.