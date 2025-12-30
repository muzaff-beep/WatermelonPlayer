# Watermelon Player - Testing Guide

Comprehensive testing strategy and procedures for Watermelon Player.

## Testing Strategy

### Test Pyramid

```
         ▲
        ╱ ╲
       ╱   ╲  UI Tests (10%)
      ╱─────╲
     ╱       ╲
    ╱         ╲ Integration Tests (30%)
   ╱───────────╲
  ╱             ╲
 ╱               ╲ Unit Tests (60%)
╱─────────────────╲
```

### Test Categories

| Category | Coverage | Tools | Speed |
|----------|----------|-------|-------|
| **Unit Tests** | Business logic, utilities | JUnit, Mockito | Fast |
| **Integration Tests** | Database, API, services | Espresso, TestContainers | Medium |
| **UI Tests** | Compose, navigation | Compose Testing | Slow |
| **Performance Tests** | Memory, battery, speed | Android Profiler | Slow |

## Unit Testing

### Setup

Add to `app/build.gradle.kts`:

```kotlin
dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
}
```

### Example Unit Tests

#### Test EditionManager

```kotlin
// app/src/test/java/com/watermelon/player/EditionManagerTest.kt
package com.watermelon.player

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class EditionManagerTest {
    
    @Before
    fun setUp() {
        // Reset singleton state
    }
    
    @Test
    fun `test iran edition detection`() {
        // Given
        // When
        val edition = EditionManager.getEdition()
        // Then
        assertTrue(EditionManager.isIranEdition() || EditionManager.isGlobalEdition())
    }
    
    @Test
    fun `test edition is not null`() {
        assertNotNull(EditionManager.getEdition())
    }
}
```

#### Test Repository (Future)

```kotlin
// app/src/test/java/com/watermelon/player/data/repository/TrackRepositoryTest.kt
package com.watermelon.player.data.repository

import io.mockk.mockk
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class TrackRepositoryTest {
    
    private val mockLocalSource = mockk<LocalTrackSource>()
    private val mockRemoteSource = mockk<RemoteTrackSource>()
    private val repository = TrackRepositoryImpl(mockLocalSource, mockRemoteSource)
    
    @Test
    fun `test get tracks from local source`() = runTest {
        // Given
        val mockTracks = listOf(
            Track(id = 1, title = "Song 1", artist = "Artist 1"),
            Track(id = 2, title = "Song 2", artist = "Artist 2")
        )
        coEvery { mockLocalSource.getTracks() } returns mockTracks
        
        // When
        val result = repository.getTracks()
        
        // Then
        assertEquals(2, result.size)
        assertEquals("Song 1", result[0].title)
    }
}
```

### Running Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.watermelon.player.EditionManagerTest"

# Run specific test method
./gradlew test --tests "com.watermelon.player.EditionManagerTest.test_iran_edition_detection"

# Run Iran debug tests
./gradlew testIranDebugUnitTest

# Run Global debug tests
./gradlew testGlobalDebugUnitTest

# Run with verbose output
./gradlew test --info
```

## Integration Testing

### Setup

Add to `app/build.gradle.kts`:

```kotlin
dependencies {
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
    androidTestImplementation("io.mockk:mockk-android:1.13.5")
}
```

### Example Integration Tests

#### Test MainActivity

```kotlin
// app/src/androidTest/java/com/watermelon/player/MainActivityTest.kt
package com.watermelon.player

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testGreetingDisplayed() {
        // When
        composeTestRule.setContent {
            WatermelonTheme {
                WatermelonGreeting()
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Watermelon Player")
            .assertIsDisplayed()
    }
}
```

### Running Integration Tests

```bash
# Run all instrumented tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew connectedAndroidTest --tests "com.watermelon.player.MainActivityTest"

# Run Iran edition tests
./gradlew connectedIranDebugAndroidTest

# Run on specific device
adb -s device_id shell am instrument -w com.watermelon.player.debug.test/androidx.test.runner.AndroidJUnitRunner
```

## UI Testing with Compose

### Compose Testing Basics

```kotlin
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule

class ComposableTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testUserInteraction() {
        composeTestRule.setContent {
            MyComposable()
        }
        
        // Find and interact with elements
        composeTestRule.onNodeWithTag("button").performClick()
        composeTestRule.onNodeWithText("Result").assertIsDisplayed()
    }
}
```

### Common Assertions

```kotlin
// Visibility
onNode(...).assertIsDisplayed()
onNode(...).assertIsNotDisplayed()
onNode(...).assertExists()
onNode(...).assertDoesNotExist()

// Text
onNodeWithText("Hello").assertTextEquals("Hello")

// State
onNode(...).assertIsEnabled()
onNode(...).assertIsNotEnabled()
onNode(...).assertIsFocused()

// Interaction
onNode(...).performClick()
onNode(...).performScrollTo()
onNode(...).performTextInput("text")
```

## Performance Testing

### Memory Profiling

```bash
# Connect device
adb devices

# Start profiler
adb shell am start-profiler com.watermelon.player.ir

# Monitor memory
adb shell dumpsys meminfo com.watermelon.player.ir
```

### Battery Profiling

1. Open Android Studio
2. **View → Tool Windows → Profiler**
3. Select device and app
4. Monitor battery usage during playback

### Frame Rate Testing

```bash
# Enable frame rate monitoring
adb shell setprop debug.atrace.tags.enableflags 1

# Monitor performance
adb shell dumpsys SurfaceFlinger --list
```

## Test Coverage

### Generate Coverage Report

```bash
# Run tests with coverage
./gradlew testDebugUnitTest --tests "*" --coverage

# Generate Jacoco report
./gradlew jacocoTestReport

# View report
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

### Coverage Targets

| Component | Target |
|-----------|--------|
| Domain layer | 90%+ |
| Repository | 85%+ |
| ViewModel | 80%+ |
| UI Composables | 70%+ |
| Overall | 75%+ |

## Continuous Integration Testing

### GitHub Actions Workflow

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run unit tests
        run: ./gradlew test
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
```

## Testing Checklist

### Before Release

- [ ] All unit tests pass: `./gradlew test`
- [ ] All integration tests pass: `./gradlew connectedAndroidTest`
- [ ] Code coverage > 75%: `./gradlew jacocoTestReport`
- [ ] No lint warnings: `./gradlew lint`
- [ ] Performance acceptable: Monitor with Profiler
- [ ] Memory leaks checked: Use LeakCanary
- [ ] Tested on multiple devices/API levels

### Device Testing Matrix

| Device | API | Edition | Status |
|--------|-----|---------|--------|
| Pixel 6 | 33 | Iran | ✓ |
| Pixel 6 | 33 | Global | ✓ |
| Pixel 7 | 34 | Iran | ✓ |
| Pixel 7 | 34 | Global | ✓ |
| Pixel 8 | 35 | Iran | ✓ |
| Pixel 8 | 35 | Global | ✓ |

## Debugging

### Logcat Filtering

```bash
# Filter by tag
adb logcat | grep "WatermelonPlayer"

# Filter by level
adb logcat *:E  # Errors only
adb logcat *:W  # Warnings and above

# Clear logs
adb logcat -c

# Save to file
adb logcat > logcat.txt
```

### Android Studio Debugger

1. Set breakpoint in code
2. Run with debugger: **Run → Debug**
3. Step through code
4. Inspect variables
5. Evaluate expressions

### Remote Debugging

```bash
# Forward port
adb forward tcp:5005 tcp:5005

# Debug over network
adb connect device_ip:5555
adb logcat -s WatermelonPlayer
```

## Test Data

### Mock Data Factory

```kotlin
// app/src/test/java/com/watermelon/player/TestDataFactory.kt
object TestDataFactory {
    
    fun createTrack(
        id: Int = 1,
        title: String = "Test Track",
        artist: String = "Test Artist"
    ) = Track(id = id, title = title, artist = artist)
    
    fun createPlaylist(
        id: Int = 1,
        name: String = "Test Playlist",
        tracks: List<Track> = listOf(createTrack())
    ) = Playlist(id = id, name = name, tracks = tracks)
}
```

## Best Practices

### Test Naming

```kotlin
// Good: Clear, descriptive names
@Test
fun `test playing track updates current track state`() { }

@Test
fun `test invalid track id throws exception`() { }

// Avoid: Vague names
@Test
fun testTrack() { }

@Test
fun test1() { }
```

### Test Organization

```kotlin
// Arrange-Act-Assert pattern
@Test
fun testPlayback() {
    // Arrange: Set up test data
    val track = createTrack()
    val player = createPlayer()
    
    // Act: Perform action
    player.play(track)
    
    // Assert: Verify result
    assertEquals(track, player.currentTrack)
    assertTrue(player.isPlaying)
}
```

### Avoid Common Mistakes

- ❌ Don't test implementation details
- ❌ Don't create tight coupling between tests
- ❌ Don't ignore test failures
- ❌ Don't write tests that are slower than necessary
- ✅ Do test behavior and outcomes
- ✅ Do keep tests independent
- ✅ Do fix failing tests immediately
- ✅ Do optimize slow tests

## Resources

- [Android Testing Guide](https://developer.android.com/training/testing)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [JUnit Documentation](https://junit.org/)
- [Mockk Documentation](https://mockk.io/)
- [Espresso Documentation](https://developer.android.com/training/testing/espresso)

---

**Last Updated**: Batch 1 Foundation  
**Status**: Testing Guide Complete
