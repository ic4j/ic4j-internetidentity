#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}" "${GITHUB_WORKSPACE:?}" "${CANDID_REV:?}" "${AGENT_REV:?}"
cd "$GITHUB_WORKSPACE"
work="$RUNNER_TEMP/ic4j-build-test"
repo="$work/maven"
reports="$work/reports"
mkdir "$work"
mkdir "$repo" "$reports"
[[ "$(git -C candid rev-parse HEAD)" == "$CANDID_REV" ]]
[[ "$(git -C agent rev-parse HEAD)" == "$AGENT_REV" ]]
printf 'candid %s\nagent %s\nmodule %s\n' "$CANDID_REV" "$AGENT_REV" "$(git -C module rev-parse HEAD)" | tee "$reports/sources.txt"
java -version 2>&1 | tee "$reports/java-version.txt"

# Maven Central can transiently throttle a fresh hosted runner. Retry only
# confirmed HTTP 429 responses, never compilation or assertion failures.
g() {
    local project="$1" attempt rc log
    shift
    for attempt in 1 2 3; do
        log="$reports/$project-$attempt.log"
        if gradle --no-daemon --console=plain -Dmaven.repo.local="$repo" \
            --init-script "$GITHUB_WORKSPACE/agent/ci/local-artifacts.init.gradle" \
            -p "$project" "$@" 2>&1 | tee "$log"; then
            return 0
        else
            rc=$?
        fi
        if [[ "$attempt" == 3 ]] || ! grep -Eq 'Received status code 429|HTTP (status )?429' "$log"; then
            return "$rc"
        fi
        sleep "$((attempt * 12))"
    done
}
g candid -PrunJavacc=true clean publishToMavenLocal
g agent clean publishToMavenLocal
(
    cd "$repo"
    sha256sum org/ic4j/ic4j-{agent,candid}/0.8.5/ic4j-*-0.8.5.jar
) | tee "$reports/dependencies.sha256"
source "$GITHUB_WORKSPACE/module/ci/local-replica.sh"
g module clean jar test

(cd "$repo"; sha256sum -c "$reports/dependencies.sha256")
jar="module/build/libs/ic4j-internetidentity-0.8.5.jar"
sha256sum "$jar" | tee "$reports/module.sha256"
python3 - "$jar" <<'PY' | tee "$reports/verification.txt"
import pathlib
import struct
import sys
import xml.etree.ElementTree as ET
import zipfile

with zipfile.ZipFile(sys.argv[1]) as jar:
    classes = [name for name in jar.namelist() if name.endswith(".class")]
    assert classes, "Empty module artifact"
    for name in classes:
        assert struct.unpack(">H", jar.read(name)[6:8])[0] == 52, f"{name} is not Java 8 bytecode"
suites = list(pathlib.Path("module/build/test-results/test").glob("TEST-*.xml"))
cases = [(test.get("classname"), test.get("name").removesuffix("()"))
         for suite in suites for test in ET.parse(suite).iter("testcase")
         if not any(test.find(tag) is not None for tag in ("skipped", "error", "failure"))]
assert any(name == "deployedInternetIdentityRespondsToStatsAndChallenge" for _, name in cases), "Local replica smoke did not run"
assert len(cases) >= 5, "Expected test set incomplete"

print(f"{len(classes)} module classes with Java 8 bytecode")
for klass, test in sorted(cases):
    print(f"PASS {klass}.{test}")
PY
