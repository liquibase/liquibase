#!/usr/bin/env bash
#
# generate-dockerhub-matrix.sh
#
# Generates a JSON matrix of Docker images and tags to scan from Docker Hub.
# Fetches recent tags for liquibase/liquibase (community only).
#
# Usage:
#   generate-dockerhub-matrix.sh [max_tags]
#
# Arguments:
#   max_tags: Maximum number of tags to scan per image (default: 10)
#
# Environment Variables:
#   MAX_TAGS: Maximum tags per image (overrides argument)
#
# Outputs:
#   - JSON matrix written to stdout and $GITHUB_OUTPUT if available
#   - Format: {"include":[{"image":"...","tag":"...","published":"..."},...]}
#   - published: ISO 8601 timestamp of when the tag was last updated

set -e

# Configuration
MAX_TAGS="${MAX_TAGS:-${1:-10}}"

echo "Generating matrix for scanning with max $MAX_TAGS tags per image..." >&2

MATRIX_INCLUDE="["
FIRST=true

for IMAGE in "liquibase/liquibase"; do
  echo "Getting tags for $IMAGE..." >&2
  REPO=$(basename "$IMAGE")
  TAGS=""
  URL="https://hub.docker.com/v2/namespaces/liquibase/repositories/${REPO}/tags?page_size=100"

  while [ -n "$URL" ]; do
    RESPONSE=$(curl -s "$URL")

    # Only include semantic version tags (e.g., 5.0.1, 4.28)
    # Format: tag|last_updated (pipe-separated to preserve dates through filtering)
    # Match semver followed by pipe delimiter (the line continues with |last_updated)
    TAG_REGEX='^[0-9]+\.[0-9]+(\.[0-9]+)?(\||$)'
    NEW_TAGS=$(echo "$RESPONSE" | jq -r '.results[] | select(.tag_status == "active") | "\(.name)|\(.last_updated)"' | grep -E "$TAG_REGEX" || true)
    TAGS=$(echo -e "$TAGS\n$NEW_TAGS" | sort -t'|' -k1 -Vu)

    # Filter out minor version tags if we have the full version
    # e.g., if we have 4.28.0, skip 4.28
    # Preserves the |last_updated suffix through filtering
    # Note: Uses GNU awk match() with capture groups (Ubuntu default, not BSD awk)
    TAGS=$(echo "$TAGS" | awk -F'|' '
      {
        tag = $1
        date = $2
        tags[NR] = $0
        tag_only[NR] = tag
        if (match(tag, /^([0-9]+)\.([0-9]+)\.([0-9]+)$/, m)) {
          full = m[1] "." m[2] "." m[3]
          has_full[full] = 1
        }
      }
      END {
        for (i = 1; i <= NR; i++) {
          tag = tag_only[i]
          if (match(tag, /^([0-9]+)\.([0-9]+)$/, m)) {
            short = m[1] "." m[2] ".0"
            if (has_full[short]) continue
          }
          print tags[i]
        }
      }
    ')

    # Get next page URL
    URL=$(echo "$RESPONSE" | jq -r '.next')
    [ "$URL" = "null" ] && break
  done

  # Get most recent tags (reverse sort and take first N)
  TAGS=$(echo "$TAGS" | tac | head -n "$MAX_TAGS")

  # Build matrix JSON
  # Each line is in format: tag|last_updated
  while IFS='|' read -r tag published; do
    if [ -n "$tag" ]; then
      # Escape any special characters in the date string for JSON
      published="${published:-unknown}"
      if [ "$FIRST" = true ]; then
        MATRIX_INCLUDE="${MATRIX_INCLUDE}{\"image\":\"$IMAGE\",\"tag\":\"$tag\",\"published\":\"$published\"}"
        FIRST=false
      else
        MATRIX_INCLUDE="${MATRIX_INCLUDE},{\"image\":\"$IMAGE\",\"tag\":\"$tag\",\"published\":\"$published\"}"
      fi
    fi
  done <<< "$TAGS"
done

MATRIX_INCLUDE="${MATRIX_INCLUDE}]"
MATRIX="{\"include\":$MATRIX_INCLUDE}"

echo "Generated matrix: $MATRIX" >&2

# Output to GitHub Actions if running in CI
if [ -n "${GITHUB_OUTPUT:-}" ]; then
  echo "matrix=$MATRIX" >> "$GITHUB_OUTPUT"
fi

# Always output to stdout for testing/debugging
echo "$MATRIX"
