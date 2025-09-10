# SiMP3 Test Plan

## 1. Objectives and Scope
- __Goal__: Validate SiMP3’s core playback, library, playlist, settings, UI, and update flows across Windows, macOS, and Linux with JDK 17+ and JavaFX 19.
- __In scope__:
  - Core playback (MP3/WAV/FLAC via JavaFX Media + soundlibs), library scan/metadata, playlist & favorites, search, settings persistence, UI (main + mini player + visualizer), auto-update check/apply, logging, error handling.
- __Out of scope__ (this iteration): Online services (e.g., Last.fm), advanced equalizer, full i18n, DRM media.

## 2. Test Strategy
- __Unit tests__ (JUnit 5 + Mockito):
  - `core/`: audio control APIs, playlist order/shuffle strategies, repeat modes.
  - `data/`: models, repositories (mock I/O), preferences.
  - `services/`: `PlayerService`, `LibraryService`, `PlaylistService`, `ConfigurationService` behavior with mocks.
  - `utils/`: file, audio, string, validation utilities.
- __Integration tests__:
  - `services` ↔ `core` ↔ `data` flows: library scan → metadata → searchable index; playback state propagation; preference save/load.
  - JSON (Jackson) compatibility, tag parsing (jaudiotagger), logging setup (SLF4J + Logback).
- __UI tests__ (TestFX):
  - Main window widgets, mini player toggle, keyboard shortcuts, seek/volume controls, visualizer toggle, album art.
- __End-to-End smoke__:
  - First run → choose folder → scan → play track → next/prev → create playlist → add/remove → save prefs → relaunch.
- __Non-functional__:
  - Performance: large library (≥20k tracks) scan time and UI responsiveness.
  - Compatibility: OS (Win/macOS/Linux), JDK 17/21, audio devices.
  - Accessibility: keyboard-only navigation of key flows, focus visuals, contrast on default theme.
  - Security/basics: safe file handling, no crashes on malformed media.

## 3. Environments
- __OS__: Windows 10/11, macOS 13+/14+, Ubuntu 22.04/24.04.
- __JDK__: 17 LTS primary; spot-check 21.
- __JavaFX__: per `pom.xml` `${javafx.version}=19.0.2.1`.
- __Audio__: at least 1 output device; optional Bluetooth.
- __Display__: 1080p baseline; spot-check 4K scaling.
- __Test data__: curated library with:
  - Valid MP3/WAV/FLAC with/without tags, various bitrates; edge cases (VBR, unusual FLAC encodings); corrupted files.
  - Deep folder nesting, long filenames, non-ASCII paths.

## 4. Tooling
- __Build/Test__: Maven (Surefire 3.0.0), JUnit 5 (${junit.version}=5.9.2), Mockito 5.3.1.
- __UI testing__: TestFX (to be added if not present).
- __Logging__: SLF4J + Logback.
- __Coverage__: JaCoCo (recommended; add plugin later). Target ≥80% unit where practical.
- __Reports__: `target/surefire-reports` for unit/integration tests.

## 5. Test Data Management
- Provide `test-resources/` with sample media and metadata fixtures.
- Use factory/builders for domain models. Mock I/O for unit tests.
- Large-library synthetic generator for performance scenarios.

## 6. Entry/Exit Criteria
- __Entry__: code compiles; environments ready; sample media available.
- __Exit__: 
  - All critical/high test cases pass across primary OS and JDK 17.
  - No open Critical/High defects; Mediums have workarounds; Lows triaged.
  - Coverage goals met or justified.

## 7. Prioritization
- P0: Playback stability, library scanning, settings persistence, crash-free startup, update check.
- P1: Playlists, search, mini player, visualizer toggle.
- P2: Volume normalization toggle behavior, advanced keyboard shortcuts, aesthetics.

## 8. Defect Management
- __Severity__: Critical (crash/data loss) → High (core feature broken) → Medium (workaround exists) → Low (cosmetic).
- __Lifecycle__: New → Triaged → In Progress → In Review → Verified → Closed. Link tests by ID from `TEST_CASES.md`.

## 9. Schedules & Roles
- Unit/integration: continuous (PR-gated). UI/E2E: nightly and pre-release.
- Release candidates: full regression on all platforms; smoke on installer/JAR.

## 10. Risks & Mitigations
- __Media codec variance__: maintain diverse test samples; fall back to alternate decoders when possible.
- __JavaFX platform quirks__: run UI tests headful in CI matrix; provide headless fallback only for non-visual logic.
- __Large libraries__: throttle scanning; background tasks; monitor memory.
- __Settings regressions__: add persistence tests; verify on restart in E2E.

## 11. CI/CD
- Build and test via GitHub Actions (see `.github/workflows/`).
- Matrix: OS × JDK 17 (plus optional 21), upload surefire reports; archive UI screenshots on failure.

## 12. Traceability
- Map features (README “Features”) → `TEST_CASES.md` IDs. Keep the mapping current per `CHANGELOG.md` and roadmap.
