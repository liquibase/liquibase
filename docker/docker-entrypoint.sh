#!/bin/bash
set -e

if [[ "$INSTALL_MYSQL" ]]; then
  lpm add mysql --global
fi

if [[ "$1" != "history" ]] && [[ "$1" != "init" ]] && [[ "$1" != "liquibase" ]] && type "$1" > /dev/null 2>&1; then
  ## First argument is an actual OS command (except if the command is history, init, or liquibase). Run it
  exec "$@"
else
  # If first argument is 'liquibase', remove it since we'll be calling /liquibase/liquibase anyway
  if [ "$1" = "liquibase" ]; then
    shift
  fi

  # Check if changelog directory exists (common mount point) and change to it
  # This makes Docker behavior match CLI behavior for relative paths
  # Allow SHOULD_CHANGE_DIR to be set via environment variable to override automatic detection
  if [ -z "$SHOULD_CHANGE_DIR" ]; then
    # Check if we should change directory based on relative paths being used
    SHOULD_CHANGE_DIR=false

    # Only change directory if changelog directory is mounted AND we detect relative paths
    if [ -d "/liquibase/changelog" ]; then
      # Check if the changelog directory appears to be a mount point (has files or is writable)
      if [ "$(ls -A /liquibase/changelog 2>/dev/null)" ] || touch /liquibase/changelog/.test 2>/dev/null; then
        # Remove test file if created
        rm -f /liquibase/changelog/.test 2>/dev/null

        # Check if any arguments contain relative paths (not starting with / or containing :/ for URLs)
        for arg in "$@"; do
          # Convert argument to lowercase for case-insensitive matching
          lower_arg=$(echo "$arg" | tr '[:upper:]' '[:lower:]')
          # Match any --*file= argument (e.g., --changelog-file, --flow-file, --output-file, etc.)
          case "$lower_arg" in
            --*file=*)
              value="${arg#*=}"  # Use original arg to preserve case in the value
              # If the value doesn't start with / and doesn't contain :/ (for URLs), it's likely a relative path
              if [[ "$value" != /* && "$value" != *://* && "$value" != "" ]]; then
                SHOULD_CHANGE_DIR=true
                break
              fi
              ;;
          esac
        done

        # Also check environment variables ending in _FILE (e.g., LIQUIBASE_COMMAND_CHANGELOG_FILE)
        if [ "$SHOULD_CHANGE_DIR" = false ]; then
          while IFS='=' read -r name value; do
            # Match any *_FILE environment variable
            case "$name" in
              *_FILE)
                # If the value doesn't start with / and doesn't contain :/ (for URLs), it's likely a relative path
                if [[ "$value" != /* && "$value" != *://* && "$value" != "" ]]; then
                  SHOULD_CHANGE_DIR=true
                  break
                fi
                ;;
            esac
          done < <(env)
        fi
      fi
    fi
  fi

  # Change directory to the changelog directory if it's mounted
  # This ensures all relative paths and generated files end up in the mounted volume
  if [ -d "/liquibase/changelog" ] && [ "$SHOULD_CHANGE_DIR" = true ]; then
    cd /liquibase/changelog
  fi

  # Set search path based on whether we changed directories
  # BUT: Only inject our default search path if user hasn't already provided one
  # This respects Liquibase's configuration precedence: CLI args > env vars > properties files
  EXTRA_SEARCH_PATH=""

  # Check if user already provided a search path
  USER_HAS_SEARCH_PATH=false
  for arg in "$@"; do
    # Check for both --search-path and --searchPath (case variations)
    case "$arg" in
      --search-path=*|--searchPath=*)
        USER_HAS_SEARCH_PATH=true
        break
        ;;
    esac
  done

  # Also check if user provided LIQUIBASE_SEARCH_PATH environment variable
  if [ -n "$LIQUIBASE_SEARCH_PATH" ]; then
    USER_HAS_SEARCH_PATH=true
  fi

  # Only inject our default search path if user didn't provide one
  if [ "$USER_HAS_SEARCH_PATH" = false ]; then
    if [ "$SHOULD_CHANGE_DIR" = true ]; then
      # If we changed to changelog directory, search current directory
      EXTRA_SEARCH_PATH="--search-path=."
    else
      # If we stayed in /liquibase and changelog directory exists, add it to search path
      # This helps when using absolute paths like /liquibase/changelog/file.xml
      if [ -d "/liquibase/changelog" ]; then
        EXTRA_SEARCH_PATH="--search-path=/liquibase/changelog"
      fi
    fi
  fi

  if [[ "$*" == *--defaultsFile* ]] || [[ "$*" == *--defaults-file* ]] || [[ "$*" == *--version* ]]; then
    ## Just run as-is, but add search path if needed
    if [ -n "$EXTRA_SEARCH_PATH" ]; then
      exec /liquibase/liquibase "$EXTRA_SEARCH_PATH" "$@"
    else
      exec /liquibase/liquibase "$@"
    fi
  else
    ## Include standard defaultsFile and search path
    if [ -n "$EXTRA_SEARCH_PATH" ]; then
      exec /liquibase/liquibase "--defaultsFile=/liquibase/liquibase.docker.properties" "$EXTRA_SEARCH_PATH" "$@"
    else
      exec /liquibase/liquibase "--defaultsFile=/liquibase/liquibase.docker.properties" "$@"
    fi
  fi
fi
