# Play Integrity API + Screen-State Proof of Concept

Android POC demonstrating cryptographic binding of app screen state to Google Play Integrity attestation.

## What it does

1. Captures current UI state as structured JSON (text fields, selections, screen name)
2. Computes SHA-256 hash of the canonical state JSON
3. Builds a nonce that cryptographically binds: state hash + timestamp + request ID
4. Calls Play Integrity API (or mock) with that nonce
5. Stores the complete proof (hash, nonce, token, verdict) in Room database
6. Displays proof details with copy-to-clipboard support

## Architecture

- **Semantic state capture** - UI state serialized to canonical JSON with sorted keys (deterministic, auditable, no screenshots needed)
- **Cryptographic binding** - Nonce = Base64URL(SHA-256(stateHash|timestamp|requestId)), ensuring Google's attestation covers the exact screen state at that moment
- **Mock + Real modes** - Toggle between simulated responses (for demo) and real Play Integrity API (requires Play Console setup)

## Proof binding flow

```
Screen State -> Canonical JSON -> SHA-256 Hash --+
                                                  |
Timestamp (ISO 8601) ----------------------------+-> Nonce = Base64URL(SHA-256(hash|ts|reqId))
                                                  |
Request ID (UUID) -------------------------------+
                                                  |
                                          Play Integrity API
                                                  |
                                          Integrity Token
                                                  |
                                    IntegrityProof (persisted to Room)
```

## Tech stack

- Kotlin, Jetpack Compose, Material3
- Google Play Integrity API 1.3.0
- Room (local proof storage)
- Navigation Compose
- Gson (canonical JSON serialization)
- MVVM with StateFlow

## Setup

### Mock mode (default)
Just build and run. No configuration needed.

### Real Play Integrity mode
1. Set up a Google Cloud project and link it to your Play Console app
2. In `app/build.gradle.kts`, update:
   - `CLOUD_PROJECT_NUMBER` to your project number
   - `USE_MOCK_INTEGRITY` to `false`
3. Build signed APK and upload to Play Console (internal testing track)
4. Run on a real device with Google Play Services

## Project structure

```
app/src/main/java/com/hautt/playintegrity/
├── MainActivity.kt              # NavHost entry point
├── model/
│   ├── AppStateSnapshot.kt      # Screen state data class
│   ├── IntegrityProof.kt        # Room entity
│   ├── ProofDao.kt              # Room DAO
│   └── ProofDatabase.kt         # Room database
├── service/
│   ├── CryptoService.kt         # SHA-256, nonce, UUID
│   ├── StateCapture.kt          # State -> canonical JSON
│   ├── IntegrityService.kt      # Interface + result type
│   ├── PlayIntegrityService.kt  # Real API implementation
│   └── MockIntegrityService.kt  # Simulated for demo
└── ui/
    ├── home/                    # Main screen: capture + verify
    ├── history/                 # Past proofs list
    ├── detail/                  # Full proof display
    └── theme/                   # Material3 dark theme
```

## Screens

- **Home** - Enter transaction data, select type, toggle mock/real, tap "Capture State & Verify" to generate proof
- **History** - List of all generated proofs, tap to view details, "Clear All" to reset
- **Detail** - Full proof display with all fields, copy-to-clipboard per field
