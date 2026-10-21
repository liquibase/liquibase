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
# The only ways a job may reach `release-publish`: approval handed down by the orchestrator,
# or create-release's rehearsal of main from main. A literal `release-publish` is allowed only
# on a job whose `if:` is exactly $dry_run_only.
approved_env="\${{ inputs.approved && 'release-publish' || 'release' }}"
rehearsal_env="\${{ (inputs.dry_run && inputs.branch == 'main' && github.ref == 'refs/heads/main') && 'release-publish' || 'release' }}"
dry_run_only="\${{ inputs.dry_run == true }}"
errors=0

fail() {
  echo "::error file=$1::$2"
  errors=$((errors + 1))
}

trim() {
  local t="$1"
  t="${t#"${t%%[![:space:]]*}"}"; t="${t%"${t##*[![:space:]]}"}"
  printf '%s\n' "$t"
}

# Prints the top-level `&&` terms of an `if:` one per line, or fails when the expression has
# a top-level `||`, since any OR there can make the job run without the approval clause.
conjuncts() {
  local s="$1" depth=0 quote=0 term="" i c two
  s="${s#"\${{"}"; s="${s%"}}"}"
  for ((i = 0; i < ${#s}; i++)); do
    c="${s:i:1}"; two="${s:i:2}"
    if [ "$c" = "'" ]; then quote=$((1 - quote)); fi
    if [ "$quote" -eq 0 ]; then
      case "$c" in "(") depth=$((depth + 1)) ;; ")") depth=$((depth - 1)) ;; esac
      if [ "$depth" -eq 0 ] && [ "$two" = "||" ]; then return 1; fi
      if [ "$depth" -eq 0 ] && [ "$two" = "&&" ]; then
        trim "$term"; term=""; i=$((i + 1)); continue
      fi
    fi
    term+="$c"
  done
  trim "$term"
}

# The gate job: reviewer-gated environment, skipped only on a dry run.
if [ "$(yq '.jobs.manual-approval.environment // ""' "$gate")" != "release" ]; then
  fail "$gate" "job manual-approval must declare 'environment: release'"
fi
# shellcheck disable=SC2016 # a literal GitHub expression, not a shell one
if [ "$(yq '.jobs.manual-approval.if // ""' "$gate")" != '${{ inputs.dry_run == false }}' ]; then
  fail "$gate" "job manual-approval must keep 'if: \${{ inputs.dry_run == false }}' and nothing looser"
fi

# The orchestrator's call to it must be unconditional, and only a real dry run may skip it.
if [ "$(yq '.jobs.manual-approval.uses // ""' "$orchestrator")" != "./.github/workflows/release-manual-approval.yml" ]; then
  fail "$orchestrator" "job manual-approval must call ./.github/workflows/release-manual-approval.yml"
fi
if [ "$(yq '.jobs.manual-approval | has("if")' "$orchestrator")" = "true" ]; then
  fail "$orchestrator" "job manual-approval must not have an 'if:'"
fi
# shellcheck disable=SC2016
if [ "$(yq '.jobs.manual-approval.with.dry_run // ""' "$orchestrator")" != '${{ inputs.dry_run || false }}' ]; then
  fail "$orchestrator" "job manual-approval must pass 'dry_run: \${{ inputs.dry_run || false }}', or the gate skips on a real release"
fi

# Every other called workflow in the orchestrator ships something, approved input or not.
gated=$(yq '.jobs // {} | to_entries | .[] | select(.value | has("uses")) | select(.key != "setup" and .key != "manual-approval") | .key' "$orchestrator")
for job in $gated; do
  if [ "$(J="$job" yq '[.jobs[strenv(J)].needs] | flatten | any_c(. == "manual-approval")' "$orchestrator")" != "true" ]; then
    fail "$orchestrator" "job $job does not list manual-approval in 'needs:'"
  fi
  cond=$(J="$job" yq '.jobs[strenv(J)].if // ""' "$orchestrator")
  if [[ "$cond" == *"always()"* ]]; then
    fail "$orchestrator" "job $job uses always() in its 'if:', which runs it after a rejected approval"
  elif ! terms=$(conjuncts "$cond"); then
    fail "$orchestrator" "job $job has a top-level '||' in its 'if:', which can run it without approval"
  elif ! grep -qxF "$clause" <<< "$terms"; then
    fail "$orchestrator" "job $job must AND $clause into its 'if:'"
  fi
done

for wf in "$dir"/*.yml "$dir"/*.yaml; do
  [ -e "$wf" ] || continue

  # A person dispatching a workflow must never be able to claim approval.
  if [ "$(yq '.on.workflow_dispatch.inputs | has("approved")' "$wf" 2>/dev/null)" = "true" ]; then
    fail "$wf" "'approved' must be a workflow_call input only, never a workflow_dispatch input"
  fi

  # Any non-false value counts, so an expression or a quoted string cannot slip past.
  if [ "$wf" != "$orchestrator" ]; then
    for job in $(yq '.jobs // {} | to_entries | .[] | select(.value.with | has("approved")) | select(.value.with.approved != false) | .key' "$wf"); do
      fail "$wf" "job $job passes 'approved' but only the orchestrator, behind manual-approval, may"
    done
  fi

  # The reviewer-less environment, reached any other way, is a release with no reviewer.
  while IFS=$'\t' read -r job env; do
    [ -n "$job" ] || continue
    case "$env" in
      "$approved_env" | "$rehearsal_env") ;;
      release-publish)
        if [ "$(J="$job" yq '.jobs[strenv(J)].if // ""' "$wf")" != "$dry_run_only" ]; then
          fail "$wf" "job $job hardcodes release-publish without 'if: $dry_run_only'"
        fi ;;
      *) fail "$wf" "job $job reaches release-publish without inputs.approved or a dry-run-only 'if:'" ;;
    esac
  done < <(yq '.jobs // {} | to_entries | .[] | [.key, ((.value.environment | select(tag == "!!map") | .name) // .value.environment // "")] | select(.[1] | test("release-publish")) | @tsv' "$wf")
done

if [ "$errors" -gt 0 ]; then
  echo "Release approval gate check failed with $errors error(s)."
  exit 1
fi
echo "Release approval gate intact."
