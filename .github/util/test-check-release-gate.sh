#!/usr/bin/env bash

###################################################################
## Proves check-release-gate.sh fails closed: each case breaks one
## copy of the workflows and the check must reject it.
## Usage: test-check-release-gate.sh [workflows-dir]
###################################################################

set -euo pipefail

src="${1:-.github/workflows}"
check="$(dirname "$0")/check-release-gate.sh"
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
failures=0

orch=release-published-orchestrator.yml
gate=release-manual-approval.yml

# name | file | yq edit that must make the check fail
cases=(
  "drop manual-approval from needs|$orch|.jobs.deploy-maven.needs -= [\"manual-approval\"]"
  "needs as a bare string without the gate|$orch|.jobs.deploy-xsd.needs = \"setup\""
  "drop the approval clause from if|$orch|.jobs.publish-assets-s3.if = \"\${{ !cancelled() && needs.setup.result == 'success' }}\""
  "if overridden by always()|$orch|.jobs.deploy-javadocs.if = \"\${{ always() && (needs.manual-approval.result == 'success' || needs.manual-approval.result == 'skipped') }}\""
  "new approved job without the gate|$orch|.jobs.rogue = {\"needs\": [\"setup\"], \"uses\": \"./.github/workflows/release-deploy-xsd.yml\", \"with\": {\"approved\": true}}"
  "approved passed as an expression|$orch|.jobs.rogue = {\"needs\": [\"setup\"], \"uses\": \"./.github/workflows/release-deploy-xsd.yml\", \"with\": {\"approved\": \"\${{ true }}\"}}"
  "condition on the gate call|$orch|.jobs.manual-approval.if = \"\${{ false }}\""
  "gate call pointed elsewhere|$orch|.jobs.manual-approval.uses = \"./.github/workflows/release-setup.yml\""
  "gate environment removed|$gate|del(.jobs.manual-approval.environment)"
  "gate skipped on real runs|$gate|.jobs.manual-approval.if = \"\${{ false }}\""
  "approved offered to dispatchers|release-deploy-javadocs.yml|.on.workflow_dispatch.inputs.approved = {\"type\": \"boolean\", \"default\": true}"
  "approved passed outside the orchestrator|dry-run-release.yml|.jobs.rogue = {\"uses\": \"./.github/workflows/release-deploy-maven.yml\", \"with\": {\"approved\": true}}"
)

fresh() {
  rm -rf "$tmp/wf"
  cp -R "$src" "$tmp/wf"
}

fresh
if "$check" "$tmp/wf" > "$tmp/out" 2>&1; then
  echo "ok   unmodified workflows pass"
else
  echo "FAIL unmodified workflows were rejected:"; cat "$tmp/out"
  failures=$((failures + 1))
fi

for c in "${cases[@]}"; do
  IFS='|' read -r name file edit <<< "$c"
  fresh
  yq -i "$edit" "$tmp/wf/$file"
  if "$check" "$tmp/wf" > "$tmp/out" 2>&1; then
    echo "FAIL $name: check passed a broken workflow"
    failures=$((failures + 1))
  else
    echo "ok   $name: $(grep -m1 '::error' "$tmp/out" | sed 's/.*:://')"
  fi
done

if [ "$failures" -gt 0 ]; then
  echo "$failures case(s) failed."
  exit 1
fi
echo "All ${#cases[@]} broken cases rejected."
