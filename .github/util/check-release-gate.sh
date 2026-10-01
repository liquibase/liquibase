#!/usr/bin/env bash

###################################################################
## Fails if the release approval gate can be bypassed.
##
## Publishing jobs that pass `approved: true` run on the reviewer-less
## `release-publish` environment, so the orchestrator's dependency on
## `manual-approval` is the only thing holding reviewers in front of them.
## Usage: check-release-gate.sh [workflows-dir]
###################################################################

set -euo pipefail

dir="${1:-.github/workflows}"
orchestrator="$dir/release-published-orchestrator.yml"
gate="$dir/release-manual-approval.yml"
clause="(needs.manual-approval.result == 'success' || needs.manual-approval.result == 'skipped')"
errors=0

fail() {
  echo "::error file=$1::$2"
  errors=$((errors + 1))
}

# The gate job: reviewer-gated environment, skipped only on a dry run.
if [ "$(yq '.jobs.manual-approval.environment // ""' "$gate")" != "release" ]; then
  fail "$gate" "job manual-approval must declare 'environment: release'"
fi
# shellcheck disable=SC2016 # a literal GitHub expression, not a shell one
if [ "$(yq '.jobs.manual-approval.if // ""' "$gate")" != '${{ inputs.dry_run == false }}' ]; then
  fail "$gate" "job manual-approval must keep 'if: \${{ inputs.dry_run == false }}' and nothing looser"
fi

# The orchestrator's call to it must be unconditional.
if [ "$(yq '.jobs.manual-approval.uses // ""' "$orchestrator")" != "./.github/workflows/release-manual-approval.yml" ]; then
  fail "$orchestrator" "job manual-approval must call ./.github/workflows/release-manual-approval.yml"
fi
if [ "$(yq '.jobs.manual-approval | has("if")' "$orchestrator")" = "true" ]; then
  fail "$orchestrator" "job manual-approval must not have an 'if:'"
fi

for wf in "$dir"/*.yml "$dir"/*.yaml; do
  [ -e "$wf" ] || continue

  # A person dispatching a workflow must never be able to claim approval.
  if [ "$(yq '.on.workflow_dispatch.inputs | has("approved")' "$wf" 2>/dev/null)" = "true" ]; then
    fail "$wf" "'approved' must be a workflow_call input only, never a workflow_dispatch input"
  fi

  # Any non-false value counts, so an expression or a quoted string cannot slip past.
  jobs=$(yq '.jobs // {} | to_entries | .[] | select(.value.with | has("approved")) | select(.value.with.approved != false) | .key' "$wf")
  for job in $jobs; do
    if [ "$wf" != "$orchestrator" ]; then
      fail "$wf" "job $job passes 'approved' but only the orchestrator, behind manual-approval, may"
      continue
    fi
    if [ "$(J="$job" yq '[.jobs[strenv(J)].needs] | flatten | any_c(. == "manual-approval")' "$wf")" != "true" ]; then
      fail "$wf" "job $job passes 'approved: true' but does not list manual-approval in 'needs:'"
    fi
    cond=$(J="$job" yq '.jobs[strenv(J)].if // ""' "$wf")
    if [[ "$cond" != *"$clause"* ]]; then
      fail "$wf" "job $job passes 'approved: true' but its 'if:' does not require $clause"
    fi
    if [[ "$cond" == *"always()"* ]]; then
      fail "$wf" "job $job passes 'approved: true' and its 'if:' uses always(), which runs it after a rejected approval"
    fi
  done
done

if [ "$errors" -gt 0 ]; then
  echo "Release approval gate check failed with $errors error(s)."
  exit 1
fi
echo "Release approval gate intact."
