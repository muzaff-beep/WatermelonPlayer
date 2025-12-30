# Watermelon Player - Architecture Documentation

## System Architecture Overview

Watermelon Player follows a **layered architecture** with clear separation of concerns, designed for scalability, testability, and maintainability.

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                   │
│  (Jetpack Compose UI, Activities, ViewModels)          │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│                  Domain Layer                           │
│  (Use Cases, Entities, Repositories)                   │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│                   Data Layer                            │
│  (Local DB, Remote API, Services)                      │
└─────────────────────────────────────────────────────────┘
```

## Component Breakdown

### 1. Presentation Layer

**Location**: `app/src/main/java/com/watermelon/player/ui/`

Responsible for displaying UI and handling user interactions.

- **MainActivity.kt**: Entry point activity with Compose UI
- **Screens**: Individual composable screens (to be added in Batch 2)
- **ViewModels**: State management and business logic (to be added)
- **Theme**: Material3 design system configuration

### 2. Domain Layer

**Location**: `app/src/main/java/com/watermelon/player/domain/`

Contains business logic and use cases (to be implemented in Batch 2).

```kotlin
// Example structure (to be implemented)
domain/
├── model/
│   ├── Track.kt
│   ├── Playlist.kt
│   └── Player.kt
├── repository/
│   ├── TrackRepository.kt
│   └── PlaylistRepository.kt
└── usecase/
    ├── GetTracksUseCase.kt
    └── PlayTrackUseCase.kt
```

### 3. Data Layer

**Location**: `app/src/main/java/com/watermelon/player/data/`

Manages data sources and provides data to the domain layer.

```kotlin
// Example structure (to be implemented)
data/
├── local/
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   └── dao/
│   │       └── TrackDao.kt
│   └── preferences/
│       └── UserPreferences.kt
├── remote/
│   ├── api/
│   │   └── ApiService.kt
│   └── dto/
│       └── TrackDto.kt
└── repository/
    └── TrackRepositoryImpl.kt
```

## Dependency Injection (Koin)

**Location**: `app/src/main/java/com/watermelon/player/di/`

Koin is used for dependency injection with modular setup:

```kotlin
// appModule.kt
val appModule = module {
    // Repositories
    single<TrackRepository> { TrackRepositoryImpl(get()) }
    
    // Use Cases
    single { GetTracksUseCase(get()) }
    
    // ViewModels
    viewModel { PlayerViewModel(get()) }
}
```

### Module Organization
- `appModule`: Core application dependencies
- `dataModule`: Data layer dependencies (future)
- `domainModule`: Domain layer dependencies (future)
- `uiModule`: UI layer dependencies (future)

## Edition Management

**Location**: `app/src/main/java/com/watermelon/player/EditionManager.kt`

Handles edition-specific logic for Iran and Global versions:

```kotlin
object EditionManager {
    fun isIranEdition(): Boolean
    fun isGlobalEdition(): Boolean
    fun getEdition(): Edition
}
```

### Edition-Specific Implementation

```kotlin
// In any component
if (EditionManager.isIranEdition()) {
    // Use Zarinpal payment gateway
} else {
    // Use Google Play Billing
}
```

## Media Playback Architecture

**Location**: `app/src/main/java/com/watermelon/player/player/`

### PlaybackService

Foreground service managing ExoPlayer instance:

```kotlin
class PlaybackService : Service() {
    private var exoPlayer: ExoPlayer? = null
    
    // Handles playback lifecycle
    // Manages notifications
    // Handles audio focus
}
```

### Future Components (Batch 2)

- **PlayerViewModel**: State management for playback
- **PlaybackController**: High-level playback control
- **MediaSessionManager**: Media session and notifications

## Build Variants & Flavors

### Product Flavors

```
Edition Dimension:
├── iran
│   ├── Package: com.watermelon.player.ir
│   ├── Dependencies: Zarinpal, IDPay
│   └── Resources: Persian strings
└── global
    ├── Package: com.watermelon.player.global
    ├── Dependencies: Google Play Billing, TensorFlow
    └── Resources: English strings
```

### Build Types

```
├── debug
│   ├── Debuggable: true
│   ├── Minify: false
│   └── Suffix: .debug
└── release
    ├── Debuggable: false
    ├── Minify: true
    └── Signing: Configured
```

## Data Flow

### Example: Playing a Track

```
UI (Compose)
    ↓
PlayerViewModel
    ↓
PlayTrackUseCase
    ↓
TrackRepository
    ↓
PlaybackService + ExoPlayer
    ↓
Audio Output
```

## Threading Model

- **Main Thread**: UI updates, Compose recomposition
- **IO Thread**: Database queries, file I/O
- **Default Thread**: Heavy computation
- **Service Thread**: Background playback

### Coroutine Usage

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    // Database or network operation
}
```

## State Management

### ViewModel Pattern

```kotlin
class PlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PlayerUiState>(...)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
}
```

### Compose State

```kotlin
@Composable
fun PlayerScreen(viewModel: PlayerViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    // Render based on uiState
}
```

## Error Handling

### Strategy

1. **Try-Catch**: For synchronous operations
2. **Result Wrapper**: For use cases
3. **Error Events**: For UI error display
4. **Logging**: Timber for debug logging

### Example

```kotlin
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception) : Result<T>()
    class Loading<T> : Result<T>()
}
```

## Testing Strategy

### Unit Tests
- Repository implementations
- Use cases
- ViewModels
- Utility functions

### Integration Tests
- Database operations
- API calls
- Service interactions

### UI Tests
- Compose components
- Navigation
- User interactions

## Performance Considerations

### Memory Management
- ExoPlayer resource cleanup
- Image loading optimization (Coil)
- Coroutine scope management

### Battery Optimization
- Efficient media playback
- Foreground service when needed
- Wake lock management

### Network Efficiency
- API response caching
- Batch requests
- Compression

## Security Architecture

### Data Protection
- Encrypted preferences for sensitive data
- Network security config
- ProGuard obfuscation

### Permission Management
- Runtime permissions (Android 6.0+)
- Scoped storage compliance
- Foreground service permissions

## Scalability Considerations

### Modular Structure
- Feature-based modules (future)
- Decoupled components
- Clear interfaces

### Extensibility
- Edition-specific implementations
- Plugin architecture (future)
- Custom player implementations

## Future Enhancements (Batch 2+)

- [ ] Local database with Room
- [ ] Remote API integration
- [ ] Advanced player features
- [ ] Playlist management
- [ ] Search functionality
- [ ] User authentication
- [ ] Cloud sync
- [ ] Analytics integration
- [ ] Offline playback
- [ ] Equalizer support

---

**Last Updated**: Batch 1 Foundation  
**Status**: Foundation Architecture Complete
