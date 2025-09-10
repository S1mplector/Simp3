# SiMP3 Test Cases

This document enumerates detailed test cases mapped to features described in `README.md` and layers in `docs/architecture.md`.

Conventions:
- ID format: <Area>-<Number> (e.g., PLB-001)
- Priority: P0 (critical), P1, P2
- Type: Unit, Integration, UI, E2E, NonFunc

## 0. Template
- ID:
- Title:
- Priority:
- Type:
- Preconditions:
- Steps:
- Expected:
- Notes:

---

## 1) Playback (Core/Services/UI)

- ID: PLB-001
  Title: Play MP3 successfully
  Priority: P0
  Type: E2E
  Preconditions:
  - Have a valid MP3 in the library
  Steps:
  1. Launch app
  2. Select track and click Play
  3. Observe timeline and audio output
  Expected:
  - Playback starts within 1s, state shows Playing, timeline advances, audio audible

- ID: PLB-002
  Title: Pause/Resume
  Priority: P0
  Type: E2E
  Steps:
  1. While playing, click Pause, wait 2s, click Play
  Expected:
  - State toggles Paused→Playing, position resumes near pause point (±100ms)

- ID: PLB-003
  Title: Stop resets position
  Priority: P1
  Type: E2E
  Steps:
  1. While playing, click Stop
  Expected:
  - State: Stopped; position resets to 0; Play starts from beginning

- ID: PLB-004
  Title: Next/Previous track
  Priority: P0
  Type: E2E
  Steps:
  1. While playing in a list, press Next then Previous
  Expected:
  - Correct tracks load; no crash; state transitions correct

- ID: PLB-005
  Title: Seek forward/backward
  Priority: P0
  Type: UI
  Steps:
  1. Drag seek bar to 50%; then back to 10%
  Expected:
  - Position updates; audio jumps accordingly; no artifacts > 250ms

- ID: PLB-006
  Title: Volume/Mute behavior
  Priority: P1
  Type: UI
  Steps:
  1. Change volume 0→100%; toggle Mute
  Expected:
  - Gain changes smoothly; Mute silences without altering stored volume

- ID: PLB-007
  Title: Shuffle and Repeat modes
  Priority: P1
  Type: Integration
  Steps:
  1. Toggle shuffle; cycle repeat (off, one, all); play through 3 tracks
  Expected:
  - Order aligns with shuffle strategy; repeat-one loops same track; repeat-all cycles

- ID: PLB-008
  Title: Unsupported/corrupted file handling
  Priority: P0
  Type: Integration
  Preconditions:
  - Add corrupted FLAC and unknown extension file
  Steps:
  1. Attempt to play both
  Expected:
  - Graceful error; UI notification; logs warn; player stays stable

- ID: PLB-009
  Title: Album art display fallback
  Priority: P2
  Type: UI
  Steps:
  1. Play track with embedded art; then without
  Expected:
  - Embedded art shows; fallback image for missing

## 2) Library Management (Core/Data/Services)

- ID: LIB-001
  Title: First-run folder selection and initial scan
  Priority: P0
  Type: E2E
  Steps:
  1. Fresh profile; launch; select folder
  Expected:
  - Scan begins; progress shown; songs appear incrementally

- ID: LIB-002
  Title: Rescan updates library
  Priority: P1
  Type: Integration
  Steps:
  1. Add new files to folder; trigger rescan
  Expected:
  - New files indexed; removed files pruned

- ID: LIB-003
  Title: Metadata extraction (ID3/FLAC tags)
  Priority: P1
  Type: Integration
  Steps:
  1. Verify title/artist/album/track/genre/year populated
  Expected:
  - Fields populated via jaudiotagger; unknowns handled gracefully

- ID: LIB-004
  Title: Non-ASCII paths and long filenames
  Priority: P1
  Type: Integration
  Steps:
  1. Include paths with spaces, unicode, >255 chars
  Expected:
  - Indexed and playable; UI truncates safely

- ID: LIB-005
  Title: Search returns relevant results
  Priority: P1
  Type: Integration
  Steps:
  1. Search by title/artist/album/partial
  Expected:
  - Results ranked correctly; cleared when search cleared

- ID: LIB-006
  Title: Large library performance
  Priority: P1
  Type: NonFunc
  Steps:
  1. Index ≥20k tracks synthetic set
  Expected:
  - No OOM; scanning runs in background; UI responsive; completion time recorded

## 3) Playlists & Favorites (Core/Data/Services/UI)

- ID: PLS-001
  Title: Create, rename, delete playlist
  Priority: P0
  Type: UI
  Steps:
  1. Create playlist, rename, delete
  Expected:
  - CRUD persists; no orphan references

- ID: PLS-002
  Title: Add/remove tracks; reorder via drag
  Priority: P1
  Type: UI
  Steps:
  1. Add multiple; remove one; drag to reorder
  Expected:
  - Order saved; playback follows list order

- ID: PLS-003
  Title: Shuffle within playlist
  Priority: P2
  Type: Integration
  Steps:
  1. Enable shuffle; start from item N
  Expected:
  - No immediate repeats until all played (per implemented strategy)

- ID: FAV-001
  Title: Mark/unmark favorites and filter
  Priority: P2
  Type: UI
  Steps:
  1. Toggle heart; view Favorites filter
  Expected:
  - State persists; list updates

## 4) Settings & Persistence (Data/Services/UI)

- ID: SET-001
  Title: Preferences saved to disk and loaded on restart
  Priority: P0
  Type: E2E
  Steps:
  1. Change multiple settings; restart app
  Expected:
  - All changes persist (theme, volume, window state, library paths)

- ID: SET-002
  Title: Visualizer color mode persists (regression)
  Priority: P0
  Type: E2E
  Preconditions:
  - Set visualizer color to Solid Color
  Steps:
  1. Set to Solid Color; exit; relaunch
  Expected:
  - Mode remains Solid Color (guard against defaults overriding saved value)
  Notes:
  - Regression guard for previously observed issue in settings defaulting to gradient

- ID: SET-003
  Title: Volume normalization toggle behavior
  Priority: P2
  Type: Integration
  Steps:
  1. Toggle on/off during playback
  Expected:
  - Leveling applies smoothly; persists

- ID: SET-004
  Title: Keyboard shortcuts work and conflict-free
  Priority: P1
  Type: UI
  Steps:
  1. Test Play/Pause, Next/Prev, Volume, Search, Mini Player
  Expected:
  - Actions triggered; no global OS conflicts observed during test

## 5) UI/UX (JavaFX Views/Controllers)

- ID: UIX-001
  Title: Mini player toggle and controls
  Priority: P1
  Type: UI
  Steps:
  1. Toggle Mini Player; play/pause/seek/volume in mini mode
  Expected:
  - Layout compact; controls functional; state stays in sync with main UI

- ID: UIX-002
  Title: Visualizer enable/disable and performance
  Priority: P1
  Type: NonFunc
  Steps:
  1. Enable visualizer on low-spec machine; monitor CPU
  Expected:
  - CPU within acceptable range; toggling off reduces load

- ID: UIX-003
  Title: Progress, time labels, and formatting
  Priority: P2
  Type: UI
  Steps:
  1. Observe elapsed/remaining formatting for long tracks
  Expected:
  - HH:MM:SS formatting correct

- ID: UIX-004
  Title: Theme/contrast and focus visibility
  Priority: P2
  Type: UI
  Steps:
  1. Navigate with keyboard only
  Expected:
  - Focus rings visible; contrast meets baseline

## 6) Updates (Update System)

- ID: UPT-001
  Title: Check for updates (no update available)
  Priority: P0
  Type: Integration
  Steps:
  1. Trigger update check
  Expected:
  - Reaches GitHub; indicates latest; no prompt to apply

- ID: UPT-002
  Title: Check for updates (update available)
  Priority: P0
  Type: Integration
  Steps:
  1. Point config to a test release channel; trigger check
  Expected:
  - Update available prompt; release notes visible

- ID: UPT-003
  Title: Apply update safely
  Priority: P0
  Type: E2E
  Preconditions:
  - Use sandbox build or mocks
  Steps:
  1. Start update; follow prompts; relaunch
  Expected:
  - No data loss; app version increments; rollback available on failure

## 7) Error Handling & Logging

- ID: ERR-001
  Title: Log file initialization
  Priority: P1
  Type: Integration
  Steps:
  1. Launch app; perform actions
  Expected:
  - Logs created under `logs/`; levels per config; errors stacktraced

- ID: ERR-002
  Title: User-facing error dialogs are actionable
  Priority: P2
  Type: UI
  Steps:
  1. Trigger playback error (corrupt file)
  Expected:
  - Clear message; suggests next steps; app remains usable

## 8) Distribution & Launch

- ID: DST-001
  Title: Run via JAR on all platforms
  Priority: P0
  Type: E2E
  Steps:
  1. `java -jar simp3-<ver>.jar`
  Expected:
  - App launches; icons and resources load; audio works

- ID: DST-002
  Title: Windows EXE packaging launches
  Priority: P1
  Type: E2E
  Steps:
  1. Install EXE; run from Start Menu
  Expected:
  - App launches; shortcut and icon present; uninstall clean

- ID: DST-003
  Title: Open via OS file association plays target file
  Priority: P1
  Type: E2E
  Preconditions:
  - Register .mp3 association to SiMP3 in dev build or simulate via command-line arg path
  Steps:
  1. Double-click an MP3 in file explorer (or run `java -jar simp3-<ver>.jar <path-to-mp3>`)
  Expected:
  - App launches; target track loads and begins playback; app window is focused

## 9) Performance & Resource Use

- ID: PRF-001
  Title: Startup time regression guard
  Priority: P2
  Type: NonFunc
  Steps:
  1. Measure cold start to interactive
  Expected:
  - Under agreed threshold (record baseline)

- ID: PRF-002
  Title: Memory use during large scan
  Priority: P2
  Type: NonFunc
  Steps:
  1. Monitor heap during LIB-006
  Expected:
  - No leaks; GC stabilizes; peak within limits

## 10) Traceability Matrix (excerpt)
- Features → Test IDs
  - Playback: PLB-001..009
  - Library: LIB-001..006
  - Playlist/Favorites: PLS-001..003, FAV-001
  - Settings: SET-001..004
  - UI/UX: UIX-001..004
  - Updates: UPT-001..003
  - Errors/Logging: ERR-001..002
  - Distribution: DST-001..002
  - Performance: PRF-001..002

## 11) Notes
- Use JUnit 5 for unit/integration, Mockito for mocking, TestFX for UI.
- For update tests, use a staging repo or mock responses to avoid live updates during CI.
- Add JaCoCo and TestFX deps in `pom.xml` as needed before implementing automated coverage.
