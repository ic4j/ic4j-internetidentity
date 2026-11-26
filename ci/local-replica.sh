#!/usr/bin/env bash
# Source from build-test.sh; triggered on a fresh GitHub-hosted runner only.
archive="$work/dfx-0.32.0-x86_64-linux.tar.gz"
curl --fail --silent --show-error --location --retry 3 --max-time 180 \
    'https://github.com/dfinity/sdk/releases/download/0.32.0/dfx-0.32.0-x86_64-linux.tar.gz' -o "$archive"
printf '%s  %s\n' 218dad11e0519e11c7a310b5c7cb1eadff109bb5db1ea3432f08c870432a80fc "$archive" | sha256sum -c -
bin="$work/bin"
fixture="$work/fixture"
mkdir "$bin" "$fixture"
tar -xzf "$archive" -C "$bin" dfx
export PATH="$bin:$PATH" DFX_CONFIG_ROOT="$work/dfx-config"
mkdir "$DFX_CONFIG_ROOT"
dfx --version | tee "$reports/dfx-version.txt"
[[ "$(dfx --version)" == 'dfx 0.32.0' ]]
cd "$fixture"
cat > dfx.json <<'JSON'
{"canisters":{"fixture":{"type":"custom","candid":"fixture.did","wasm":"fixture.wasm"}}}
JSON
release=https://github.com/dfinity/internet-identity/releases/download/release-2026-11-19
curl --fail --silent --show-error --location --retry 3 --max-time 180 "$release/internet_identity.did" -o fixture.did
curl --fail --silent --show-error --location --retry 3 --max-time 180 "$release/internet_identity_dev.wasm.gz" -o fixture.wasm.gz
cat > "$reports/fixture-inputs.sha256" <<'SUMS'
8fae7bfb8aae24ed21701f5bb47ce786e1397f47b86f471e05c5314170a3695f  fixture.did
04bf164ed4b32270dd137e2427db07d51aec253f500afe7e7a8c06739e71fb89  fixture.wasm.gz
SUMS
sha256sum -c "$reports/fixture-inputs.sha256"
gzip -dc fixture.wasm.gz > fixture.wasm
printf '%s\n' "$release" > "$reports/fixture-release.txt"
trap 'cd "$fixture"; dfx stop >> "$reports/dfx.log" 2>&1 || true' EXIT
dfx start --clean --host 127.0.0.1:4943 --background > "$reports/dfx.log" 2>&1
curl --fail --silent --show-error --retry 20 --retry-delay 1 --retry-connrefused --max-time 3 http://127.0.0.1:4943/api/v2/status > /dev/null
dfx deploy fixture --network local --identity anonymous --no-wallet --argument '(null)' 2>&1 | tee "$reports/fixture-deploy.log"
export IC4J_CI_FIXTURE
IC4J_CI_FIXTURE="$(dfx canister id fixture --network local)"
[[ "$IC4J_CI_FIXTURE" =~ ^[a-z0-9]+(-[a-z0-9]+)+$ ]]
printf 'Fixture: %s\n' "$IC4J_CI_FIXTURE" | tee "$reports/fixture.txt"
cd "$GITHUB_WORKSPACE"
