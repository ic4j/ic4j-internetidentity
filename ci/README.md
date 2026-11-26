# GitHub build and test

The workflow runs for pull requests and pushes to `master`, or on manual
dispatch. Java 21 and Gradle 8.14 build the Java-8-compatible module using
fixed Candid and Agent source commits in a new job-local Maven repository.
No package is published.

CI runs the four offline delegation tests and a separate smoke against a
fresh loopback-only DFX 0.32.0 replica. It deploys the checksum-pinned DFINITY
Internet Identity development WASM and Candid from `release-2026-11-19`. It
asserts fresh-account statistics and creation and decoding of a CAPTCHA
challenge, not registration or browser/WebAuthn login. No user credentials
or public replica are used. The old workstation fixture test requiring
pre-existing canister IDs and PEM files is intentionally not selected.

The Actions artifact includes JUnit XML, versions, exact source revisions,
hashes and the replica deployment log.
