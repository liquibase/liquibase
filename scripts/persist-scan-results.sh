#!/usr/bin/env bash
#
# persist-scan-results.sh
#
# Persists vulnerability scan results to the scan-results branch.
# Called after the vulnerability-scan matrix job completes.
# Downloads scan artifacts and commits them to a persistent branch
# so the Liquibase Security dashboard can read them via GitHub Contents API.
#
# Usage:
#   persist-scan-results.sh <artifacts-dir>
#
# Arguments:
#   artifacts-dir: Directory containing downloaded scan artifacts.
#                  Each subdirectory is named vulnerability-report-<image>-<tag>
#                  and contains trivy-surface.json, trivy-deep.json, grype-results.json.
#
# Environment Variables:
#   GITHUB_REPOSITORY: owner/repo (set by GitHub Actions)
#   GITHUB_SERVER_URL: GitHub server URL (set by GitHub Actions)
#   GITHUB_RUN_ID:     Workflow run ID (set by GitHub Actions)
#   EXPECTED_MATRIX:   JSON matrix from generate-matrix job (optional).
#                      When set, images in the matrix that have no artifact
#                      are recorded with status "failed" in metadata.json.
#
# Branch structure:
#   scan-results/
#     manifest.json
#     <org>/<image>/<tag>/
#       trivy-surface.json
#       trivy-deep.json
#       grype-results.json
#       metadata.json

set -euo pipefail

ARTIFACTS_DIR="$(cd "${1:?Usage: persist-scan-results.sh <artifacts-dir>}" && pwd)"
BRANCH="scan-results"
SCANNED_AT="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

if [ ! -d "$ARTIFACTS_DIR" ]; then
  echo "Error: artifacts directory not found: $ARTIFACTS_DIR" >&2
  exit 1
fi

# Count artifact directories
ARTIFACT_COUNT=$(find "$ARTIFACTS_DIR" -mindepth 1 -maxdepth 1 -type d -name "vulnerability-report-*" | wc -l | tr -d ' ')
if [ "$ARTIFACT_COUNT" -eq 0 ] && [ -z "${EXPECTED_MATRIX:-}" ]; then
  echo "No scan artifacts found in $ARTIFACTS_DIR" >&2
  exit 0
fi

echo "Found $ARTIFACT_COUNT scan artifact(s) to persist"

# --- Set up worktree for the scan-results branch ---

REPO_ROOT="$(pwd)"
WORKTREE_DIR=$(mktemp -d)
trap 'cd "$REPO_ROOT" && git worktree remove --force "$WORKTREE_DIR" 2>/dev/null || rm -rf "$WORKTREE_DIR"' EXIT

# Check if the branch exists on remote
if git ls-remote --exit-code origin "refs/heads/$BRANCH" >/dev/null 2>&1; then
  git fetch origin "$BRANCH"
  git worktree add "$WORKTREE_DIR" "origin/$BRANCH"
  cd "$WORKTREE_DIR"
  git checkout -B "$BRANCH" "origin/$BRANCH"
else
  # Create orphan branch
  git worktree add --detach "$WORKTREE_DIR"
  cd "$WORKTREE_DIR"
  git checkout --orphan "$BRANCH"
  git rm -rf . 2>/dev/null || true
  echo '{"lastUpdated":"","images":{},"scan_status":{}}' > manifest.json
  git add manifest.json
  git commit -m "Initialize scan-results branch"
fi

# --- Load existing manifest ---

if [ -f manifest.json ]; then
  MANIFEST=$(cat manifest.json)
else
  MANIFEST='{"lastUpdated":"","images":{},"scan_status":{}}'
fi

# --- Process each artifact ---

PERSISTED_IMAGES=()

for ARTIFACT_PATH in "$ARTIFACTS_DIR"/vulnerability-report-*; do
  [ -d "$ARTIFACT_PATH" ] || continue
  ARTIFACT_NAME=$(basename "$ARTIFACT_PATH")

  # Parse image and tag from artifact name: vulnerability-report-<org>-<image>-<tag>
  # The reusable workflow sanitizes: tr '/' '-'
  # So liquibase/liquibase:5.0.1 becomes vulnerability-report-liquibase-liquibase-5.0.1
  # We need to reconstruct org/image and tag
  #
  # Strategy (data-driven, no hardcoded image list):
  #   - First segment is always the org (liquibase)
  #   - Last segment matching a version pattern (e.g. 5.0.1, 4.30.0, latest) is the tag
  #   - Everything in between is the image name
  SUFFIX="${ARTIFACT_NAME#vulnerability-report-}"

  # Split suffix into segments by hyphen, filtering out empty segments
  # (empty segments appear when image_tag has a leading dash, e.g. "-alpine"
  #  produces a double-dash: vulnerability-report-liquibase-liquibase--alpine)
  IFS='-' read -ra RAW_SEGMENTS <<< "$SUFFIX"
  SEGMENTS=()
  for seg in "${RAW_SEGMENTS[@]}"; do
    [ -n "$seg" ] && SEGMENTS+=("$seg")
  done

  if [ "${#SEGMENTS[@]}" -lt 3 ]; then
    echo "Error: cannot parse artifact name (too few segments): $ARTIFACT_NAME" >&2
    exit 1
  fi

  ORG="${SEGMENTS[0]}"

  # Find the tag: scan from the end for the last segment that looks like a version
  # A version segment starts with a digit, or is a known non-semver tag like "latest" or "alpine"
  TAG_INDEX=-1
  for (( i=${#SEGMENTS[@]}-1; i>=2; i-- )); do
    if [[ "${SEGMENTS[$i]}" =~ ^[0-9] ]] || [[ "${SEGMENTS[$i]}" == "latest" ]] || [[ "${SEGMENTS[$i]}" =~ ^(alpine|slim|jammy|focal|bullseye|noble)$ ]]; then
      TAG_INDEX=$i
      break
    fi
  done

  if [ "$TAG_INDEX" -lt 0 ]; then
    echo "Error: cannot identify version tag in artifact name: $ARTIFACT_NAME" >&2
    exit 1
  fi

  # Tag may contain hyphens (e.g. 5.0.1-beta) — rejoin from TAG_INDEX to end
  TAG=$(IFS='-'; echo "${SEGMENTS[*]:$TAG_INDEX}")

  # Image name is everything between org and tag
  IMAGE_NAME=$(IFS='-'; echo "${SEGMENTS[*]:1:$((TAG_INDEX-1))}")

  if [ -z "$IMAGE_NAME" ] || [ -z "$TAG" ]; then
    echo "Error: could not parse artifact name: $ARTIFACT_NAME (org=$ORG, image=$IMAGE_NAME, tag=$TAG)" >&2
    exit 1
  fi

  # Reconstruct image path: org/image-name
  IMAGE_PATH="$ORG/$IMAGE_NAME"

  DEST_DIR="$IMAGE_PATH/$TAG"
  mkdir -p "$DEST_DIR"

  echo "Persisting $IMAGE_PATH:$TAG"

  # Copy scan result files and track any missing ones
  MISSING_FILES=()
  REQUIRED_SCAN_FILES=(trivy-surface.json trivy-deep.json grype-results.json)
  for FILE in "${REQUIRED_SCAN_FILES[@]}"; do
    if [ -f "$ARTIFACT_PATH/$FILE" ]; then
      cp "$ARTIFACT_PATH/$FILE" "$DEST_DIR/$FILE"
    else
      MISSING_FILES+=("$FILE")
    fi
  done

  if [ "${#MISSING_FILES[@]}" -gt 0 ]; then
    echo "WARNING: $IMAGE_PATH:$TAG is missing scan files: ${MISSING_FILES[*]}"
  fi

  # Copy optional Scout JSON if present (not all images get a Scout scan).
  # When absent, remove any stale copy from a previous run (the branch is persistent).
  OPTIONAL_SCAN_FILES=(scout-results.json)
  for FILE in "${OPTIONAL_SCAN_FILES[@]}"; do
    if [ -f "$ARTIFACT_PATH/$FILE" ]; then
      cp "$ARTIFACT_PATH/$FILE" "$DEST_DIR/$FILE"
      echo "  Included optional file: $FILE"
    else
      rm -f "$DEST_DIR/$FILE"
    fi
  done

  # Also check for grype-results.json variants (some workflows output grype-results.sarif too)
  # We only need the JSON

  # Build missing_files JSON array
  MISSING_JSON="[]"
  if [ "${#MISSING_FILES[@]}" -gt 0 ]; then
    MISSING_JSON=$(printf '%s\n' "${MISSING_FILES[@]}" | jq -R . | jq -s .)
  fi

  # Create metadata.json
  cat > "$DEST_DIR/metadata.json" <<EOF
{
  "scannedAt": "$SCANNED_AT",
  "image": "$IMAGE_PATH",
  "tag": "$TAG",
  "status": "success",
  "missing_files": $MISSING_JSON,
  "workflowRunId": "${GITHUB_RUN_ID:-}"
}
EOF

  git add "$DEST_DIR/"

  # Track successfully persisted images
  PERSISTED_IMAGES+=("${IMAGE_PATH}:${TAG}")

  # Update manifest in memory — add tag to image list if not already present
  MANIFEST=$(echo "$MANIFEST" | jq --arg img "$IMAGE_PATH" --arg tag "$TAG" '
    .images[$img] = ((.images[$img] // []) | if index($tag) then . else . + [$tag] end)
  ')

  # Update scan_status for this image:tag
  MANIFEST=$(echo "$MANIFEST" | jq --arg img "$IMAGE_PATH" --arg tag "$TAG" '
    .scan_status[$img + ":" + $tag] = "success"
  ')
done

# --- Handle missing expected scans ---
# If EXPECTED_MATRIX is set (passed from workflow), check for images that
# were expected to be scanned but have no artifact (scan job failed).

if [ -n "${EXPECTED_MATRIX:-}" ]; then
  # Extract expected image:tag pairs from the matrix JSON
  EXPECTED_PAIRS=$(echo "$EXPECTED_MATRIX" | jq -r '.include[] | "\(.image):\(.tag)"')

  while IFS= read -r PAIR; do
    [ -z "$PAIR" ] && continue

    # Check if this pair was already persisted successfully
    FOUND=false
    for PERSISTED in "${PERSISTED_IMAGES[@]:-}"; do
      if [ "$PERSISTED" = "$PAIR" ]; then
        FOUND=true
        break
      fi
    done

    if [ "$FOUND" = true ]; then
      continue
    fi

    # This image:tag was expected but not found — scan failed
    FAIL_IMAGE="${PAIR%%:*}"
    FAIL_TAG="${PAIR##*:}"

    echo "WARNING: Expected scan missing for $FAIL_IMAGE:$FAIL_TAG — recording as failed"

    DEST_DIR="$FAIL_IMAGE/$FAIL_TAG"
    mkdir -p "$DEST_DIR"

    # Create metadata.json with failed status
    cat > "$DEST_DIR/metadata.json" <<EOF
{
  "scannedAt": "$SCANNED_AT",
  "image": "$FAIL_IMAGE",
  "tag": "$FAIL_TAG",
  "status": "failed",
  "workflowRunId": "${GITHUB_RUN_ID:-}"
}
EOF

    git add "$DEST_DIR/"

    # Update manifest — ensure the tag is tracked
    MANIFEST=$(echo "$MANIFEST" | jq --arg img "$FAIL_IMAGE" --arg tag "$FAIL_TAG" '
      .images[$img] = ((.images[$img] // []) | if index($tag) then . else . + [$tag] end)
    ')

    # Update scan_status as failed
    MANIFEST=$(echo "$MANIFEST" | jq --arg img "$FAIL_IMAGE" --arg tag "$FAIL_TAG" '
      .scan_status[$img + ":" + $tag] = "failed"
    ')
  done <<< "$EXPECTED_PAIRS"
fi

# --- Update manifest ---

MANIFEST=$(echo "$MANIFEST" | jq --arg ts "$SCANNED_AT" '.lastUpdated = $ts')

# Sort version tags in descending semver order for each image.
# Semver tags (e.g. 5.0.1, 4.30.0) are sorted numerically by major.minor.patch.
# Non-semver tags (e.g. "latest") are appended at the end in alphabetical order.
MANIFEST=$(echo "$MANIFEST" | jq '
  .images |= with_entries(
    .value |= (
      group_by(test("^[0-9]") | not) |
      # group_by false=semver first, true=non-semver second
      (.[0] // []) as $semver |
      (.[1] // []) as $other |
      ($semver | sort_by(
        split(".") | [
          (.[0] // "0" | tonumber),
          (.[1] // "0" | tonumber),
          (.[2] // "0" | split("-")[0] | tonumber)
        ]
      ) | reverse) + ($other | sort)
    )
  )
')

echo "$MANIFEST" | jq . > manifest.json
git add manifest.json

# --- Commit and push ---

if git diff --cached --quiet; then
  echo "No changes to commit"
  exit 0
fi

CHANGED_FILES=$(git diff --cached --name-only)
CHANGED_COUNT=0
if [ -n "$CHANGED_FILES" ]; then
  CHANGED_COUNT=$(echo "$CHANGED_FILES" | grep -c "metadata.json" || true)
fi
git commit -m "Update scan results ($CHANGED_COUNT version(s)) — $SCANNED_AT"
git push origin "$BRANCH"

echo "Persisted scan results to $BRANCH branch ($CHANGED_COUNT version(s))"
