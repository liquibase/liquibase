# :zap: Liquibase Release Workflows

## :arrows_clockwise: Liquibase Build Process Refactoring

https://datical.atlassian.net/wiki/spaces/DEV/pages/3371335681/Liquibase+Build+Process+Refactoring#New-Build.yml-%E2%86%92

## :fire: Liquibase DryRun Releases

The `dryRun` process simulates our current production Liquibase release workflow as closely as possible. It mimics all key release activities on a nightly basis, allowing us to anticipate and address any automation issues before the actual release.

## :star2: What a DryRun Release does?

The following actions are identical to those in a regular Liquibase release, with no modifications:

- Get latests liquibase artifacts from the `run-tests.yml` workflow
- Re-version artifacts to `dry-run-GITHUB_RUN_ID` version. i.e `dry-run-10522556642`
- Build installers
- Attach artifacts (`zip` and `tar` files) to a dryRun draft release
- Upload `deb`, `rpm`, and `sdkman` to our test s3 repository: `s3://repo.liquibase.com.dry.run`
- Build `choco` package
- Build `ansible` package
- Executes the test for the `brew` PR creation
- Deploy artifacts to Maven, to our internal Maven repository: `https://repo.liquibase.net/repository/dry-run-sonatype-nexus-staging`
- Delete the dryRun draft release. i.e `dry-run-10522556642`
- Delete the dryRun repository tag. i.e `vdry-run-10522556642`

## :warning: What a DryRun Release does not do?

- Generate PRO tags
- Generate install packages: `deb`, `rpm`, `brew` and the rest of them.
- Upload `javadocs` and `xsds` to `S3`
- Deploy artifacts to `GPM`

## :wrench: How a DryRun Release works?

You can check the `dry-run-release.yml` workflow, which is essentially composed of calls to existing release workflows such as `create-release.yml` and `release-published-orchestrator.yml`. It sends them a new input, `dry_run: true`, to control which steps are executed for regular releases versus dry-run releases.

```yml
[...]

  dry-run-create-release:
    needs: [ setup ]
    uses: liquibase/liquibase/.github/workflows/create-release.yml@main
    with:
      version: "dry-run-${{ github.run_id }}"
      runId: ${{ needs.setup.outputs.dry_run_id }}
      standalone_zip: false
      dry_run: true
    secrets: inherit

[...]

  dry-run-release-published:
    needs: [ setup, dry-run-create-release, dry-run-get-draft-release ]
    uses: liquibase/liquibase/.github/workflows/release-published-orchestrator.yml@main
    with:
      tag: "vdry-run-${{ github.run_id }}"
      dry_run_release_id: ${{ needs.dry-run-get-draft-release.outputs.dry_run_release_id }}
      dry_run_zip_url: ${{ needs.dry-run-create-release.outputs.dry_run_zip_url }}
      dry_run_tar_gz_url: ${{ needs.dry-run-create-release.outputs.dry_run_tar_gz_url }}
      dry_run: true
    secrets: inherit

[...]
```

Here you can see all the stuff which is tested:

1. Create a draft release
2. Retrieve the draft release ID
3. Simulate a release publish event. In blue you can see the internal Maven deploy to `https://repo.liquibase.net/repository/dry-run-sonatype-nexus-staging` and other packages: `deb`, `rpm`, `sdkman`, `choco`, `brew`, `ansible`.
4. Clean up dryRun resources and send a **Slack** notification if the `dy-run` fails.

![](./doc/img/dry-run.png)

The process will conclude with the `dryRun` artifacts published in our Maven repository (`https://repo.liquibase.net/repository/dry-run-sonatype-nexus-staging`), `deb`, `rpm` and `sdkman` packages published in `s3://repo.liquibase.com.dry.run` and the `docker` image pushed to our internal `ecr` repo (`812559712860.dkr.ecr.us-east-1.amazonaws.com/liquibase-dry-run`):

![](./doc/img/nexus.png)

![](./doc/img/s3.png)

![](./doc/img/ecr.png)

---

# :rocket: Release Published Orchestrator - Implementation Guide

## Overview

This document describes the orchestrator pattern implementation for the Liquibase release process. The refactored release workflows follow the pattern established in `liquibase-pro`, providing better modularity, maintainability, and error recovery.

## Architecture

### Orchestrator Workflow
**File:** `release-published-orchestrator.yml`

The main coordinator that triggers all release steps in the proper sequence. Supports the same events as the original workflow:
- `release.published` - Automatic trigger when a GitHub release is published
- `workflow_dispatch` - Manual trigger with custom inputs
- `workflow_call` - Can be called by other workflows

### Extracted Reusable Workflows

| Workflow | Purpose | Can run independently |
|----------|---------|----------------------|
| `release-setup.yml` | Extract release metadata (version, tag, branch) | ✅ Yes |
| `release-manual-approval.yml` | Hold the release for approval on the `release` environment | ✅ Yes |
| `release-deploy-maven.yml` | Deploy artifacts to Maven Central | ✅ Yes, with one approval |
| `release-deploy-javadocs.yml` | Upload javadocs to S3 | ✅ Yes, with one approval |
| `release-publish-github-packages.yml` | Publish to GitHub Packages | ✅ Yes |
| `release-deploy-xsd.yml` | Deploy XSD files to S3 and SFTP | ✅ Yes, with one approval |
| `release-docker.yml` | Trigger Docker image builds | ✅ Yes |
| `release-publish-assets-s3.yml` | Publish release assets to S3 | ✅ Yes, with one approval |

## :closed_lock_with_key: What guards each release workflow

The table above says what each workflow does. This one says what stands in front of it. Read it before changing any job that touches `/vault/liquibase`.

| Job | Workflow | Gate | AWS role | Reads |
|---|---|---|---|---|
| `setup` | `release-setup.yml` | none | none | nothing |
| `manual-approval` | `release-manual-approval.yml` | **`release`**, 5 reviewers | none | nothing; the gate makes no AWS call |
| `deploy-javadocs` | `release-deploy-javadocs.yml` | via `needs` | release-scoped | `/vault/liquibase`, then assumes the build-logic prod role read out of it |
| `publish-github-packages` | `release-publish-github-packages.yml` | via `needs` | none | `GITHUB_TOKEN` with `packages: write` |
| `deploy-xsd` | `release-deploy-xsd.yml` | via `needs` | release-scoped | `/vault/liquibase`, then the build-logic role plus five WPEngine SFTP secrets |
| `package` | `build-logic/package.yml@main` | via `needs` | **broad** | `/vault/liquibase`; shared workflow, so it cannot take this repo's environment |
| `publish-assets-s3` | `release-publish-assets-s3.yml` | via `needs` | release-scoped | `/vault/liquibase`, then the build-logic prod role |
| `deploy-maven-production` | `release-deploy-maven.yml` | via `needs` | release-scoped | `/vault/liquibase`; holds the Maven Central credentials |
| `deploy-maven-dryrun` | `release-deploy-maven.yml` | none, by design | **broad** | `/vault/liquibase`; dry runs skip the gate deliberately |
| `release-docker` | `release-docker.yml` | via `needs` | **broad** | delegates to `docker-release.yml`, whose `update-dockerfiles` reads the vault |
| `reversion`, `build-installers` | `create-release.yml` | none | **broad** | `/vault/liquibase`; holds the GPG and DigiCert signing credentials |

"release-scoped" is `liquibase-release-vault-oidc-role`, whose trust lists explicit subjects. "broad" is `liquibase-vault-oidc-role`, trusted as `repo:liquibase/*:*`.

Three of the publishing jobs chain a second role: they read `AWS_PROD_GITHUB_OIDC_ROLE_ARN_BUILD_LOGIC` **out of the vault** and then assume it. The vault read is not the end of the blast radius.

### :twisted_rightwards_arrows: Which environment a publishing job gets

The four publishing jobs resolve their environment at run time:

```yaml
environment:
  name: ${{ inputs.approved && 'release-publish' || 'release' }}
```

`approved` is declared only under `workflow_call`, never under `workflow_dispatch`, so no person can set it.

| Path | `approved` | Environment | Cost |
|---|---|---|---|
| orchestrated release | `true`, passed by the orchestrator after `manual-approval` clears | `release-publish`, no reviewers | one approval for the whole release |
| direct `workflow_dispatch` of a callee | never set | `release`, 5 reviewers | one approval, from someone other than the dispatcher |

Both names emit an `environment:` OIDC subject, and the release-scoped role trusts both. `manual-approval` lives in the orchestrator, not in a callee's `needs` graph, which is why a direct dispatch needs its own gate rather than inheriting one.

Both environments are defined in `liquibase-infrastructure`, not here, so they cannot be changed by editing a workflow:
[`github/liquibase/repos/public/liquibase-release-environment.tf`](https://github.com/liquibase/liquibase-infrastructure/blob/main/github/liquibase/repos/public/liquibase-release-environment.tf)

## Key Benefits

### 1. **Individual Workflow Execution**
Each workflow can be triggered manually via `workflow_dispatch` if it fails during the orchestrated release. This eliminates the need to re-run the entire release process.

**Example:** If Maven deployment fails, you can:
1. Fix the issue
2. Go to Actions → `Release Deploy Maven` → Run workflow
3. Provide the version and other required inputs
4. Execute just that step

### 2. **Better Visibility**
Each workflow appears as a separate run in the Actions tab, making it easier to:
- Identify which specific step failed
- View detailed logs for each component
- Track execution time per stage
- Monitor parallel execution

### 3. **Improved Maintainability**
- Smaller, focused workflow files (~50-150 lines each)
- Clear single responsibility per workflow
- Easier to update individual components
- Reduced risk of introducing bugs when making changes

### 4. **Consistent with liquibase-pro**
The pattern matches the orchestrator approach used in `liquibase-pro`:
- Similar job structure and naming
- Comparable error handling
- Consistent summary generation
- Same dry-run support

## Usage

### Normal Release (Production)

When a GitHub release is published, the orchestrator automatically:
1. Extracts release metadata
2. Waits for approval on the `release` environment (one reviewer, and not the person who started the run)
3. Deploys to all targets in parallel where possible
4. Generates a comprehensive summary

### Dry Run Release

For testing the release process without actually deploying:

```bash
# Via GitHub UI: Actions → Release Published Orchestrator → Run workflow
# Set inputs:
#   tag: v4.28.0
#   dry_run: true
#   dry_run_branch_name: release/4.28.0
```

Dry run mode:
- Skips manual approval
- Skips actual deployments to production
- Uses USER_MANAGED publishing for Maven (requires manual confirmation)
- Logs what would be done without executing

### Manual Trigger (Re-run Failed Steps)

If a specific step fails, you can re-run just that workflow:

1. Navigate to **Actions** tab
2. Find the failed workflow (e.g., "Release Deploy Maven")
3. Click **Run workflow**
4. Fill in required inputs:
   - `version`: 4.28.0
   - `tag`: v4.28.0
   - `dry_run`: false (for production)
5. Click **Run workflow**

For the four publishing workflows, a direct dispatch runs in the `release` environment and waits for one reviewer before it starts. That is deliberate: `manual-approval` is a job in the orchestrator, so a workflow dispatched on its own never passes it.

## Deployment Pipeline

```mermaid
flowchart LR
    trigger("release: published<br/>workflow_dispatch<br/>workflow_call"):::evt --> setup
    setup("setup"):::plain --> approval
    approval("manual-approval<br/>environment: release<br/>5 reviewers"):::gate

    approval --> javadocs
    approval --> ghpkg
    approval --> xsd
    approval --> package

    javadocs("deploy-javadocs"):::scoped --> maven
    ghpkg("publish-github-packages"):::plain --> maven
    xsd("deploy-xsd"):::scoped --> maven
    package("package<br/>build-logic@main"):::broad --> s3

    maven("deploy-maven"):::scoped --> docker("release-docker"):::broad
    s3("publish-assets-s3"):::scoped

    docker --> summary
    s3 --> summary
    summary("generate-summary<br/>needs: all nine<br/>always()"):::plain

    classDef evt    fill:#e9ecf1,stroke:#5b6573,stroke-width:1px,color:#14171c
    classDef plain  fill:#ffffff,stroke:#5b6573,stroke-width:1px,color:#14171c
    classDef gate   fill:#f6ead6,stroke:#9d5c00,stroke-width:2px,color:#14171c
    classDef scoped fill:#dcefe7,stroke:#17654f,stroke-width:2px,color:#14171c
    classDef broad  fill:#f6dedb,stroke:#9d2f26,stroke-width:2px,color:#14171c
```

| | meaning |
|---|---|
| :large_orange_diamond: amber | the reviewer gate |
| :green_square: green | reads `/vault/liquibase` through the release-scoped role |
| :red_square: red | reads `/vault/liquibase` through the broad `repo:liquibase/*:*` role |
| :white_large_square: white | no vault access |

Three edges are easy to get backwards, so read them off the `needs:` keys rather than from memory:

- `release-docker` runs **after** `deploy-maven`, not beside it.
- `deploy-xsd` runs **beside** `deploy-javadocs`, not after it.
- `publish-assets-s3` waits on `package`. That edge is load-bearing: when approval is denied `package` is skipped, and accepting a skipped `package` on its own would publish production assets past a rejected release.

## Testing Strategy

### Before Production Use

1. **Test Dry Run Mode**
   ```bash
   # Create a test branch
   git checkout -b test/orchestrator-v4.28.0-test
   
   # Trigger dry run
   # Actions → Release Published Orchestrator → Run workflow
   #   tag: v4.28.0-test
   #   dry_run: true
   ```

2. **Verify Individual Workflows**
   - Test each workflow can be triggered independently
   - Confirm outputs are correctly passed between workflows
   - Verify secrets inheritance works

3. **Test Manual Approval Logic**
   - Confirm approval is skipped in dry_run mode
   - Verify 2 approvers are required in production mode

4. **Validate Summary Generation**
   - Check that all job statuses appear correctly
   - Verify success/failure icons display properly
   - Confirm links in summary are correct

### Failure Scenario Testing

Test the orchestrator's ability to handle failures:

1. **Partial Failure**: Kill one workflow mid-execution
   - Verify other workflows continue
   - Confirm failed workflow can be re-run individually

2. **Dependency Failure**: Simulate a failure in `deploy-javadocs`
   - Verify `deploy-maven` (which depends on it) doesn't run
   - Confirm manual re-run is possible

3. **Complete Failure**: Simulate failure in `setup`
   - Verify downstream workflows are skipped
   - Confirm summary shows failure status

## Maintenance

### Adding New Release Steps

To add a new step to the release process:

1. **Create** new reusable workflow file:
   ```yaml
   name: Release Deploy NewService
   on:
     workflow_call:
       inputs:
         version:
           required: true
           type: string
   ```

2. **Add** to orchestrator:
   ```yaml
   deploy-new-service:
     needs: [setup, manual-approval]
     uses: ./.github/workflows/release-deploy-new-service.yml
     with:
       version: ${{ needs.setup.outputs.version }}
     secrets: inherit
   ```

3. **Update** summary generation to include new step

### Modifying Existing Steps

1. Edit the specific workflow file
2. Test via `workflow_dispatch`
3. No changes needed to orchestrator unless inputs/outputs change

## Troubleshooting

### Common Issues

**Issue**: Workflow doesn't trigger on release published
- **Solution**: Check permissions in orchestrator file
- **Solution**: Verify GitHub App has correct permissions

**Issue**: Manual approval never arrives or is rejected
- **Solution**: Check the run's deployment gate, not the issues list: approval happens on the run page under Review deployments
- **Solution**: Check the reviewer list on the `release` environment. It is managed in [liquibase-release-environment.tf](https://github.com/liquibase/liquibase-infrastructure/blob/main/github/liquibase/repos/public/liquibase-release-environment.tf), so changing it takes a PR in liquibase-infrastructure
- **Solution**: A reviewer cannot approve a run they started themselves. Someone else on the list has to

**Issue**: Secrets not available in called workflows
- **Solution**: Ensure `secrets: inherit` is present
- **Solution**: Verify secrets exist at repository level

**Issue**: Output not passed between workflows
- **Solution**: Check outputs are defined in called workflow
- **Solution**: Verify correct reference in orchestrator

## Support

For questions or issues:
1. Check this documentation
2. Review workflow run logs in Actions tab
3. Consult the liquibase-pro orchestrator implementations as reference
4. Contact the build/release team

---

**Note:** This orchestrator pattern is consistent with modern CI/CD best practices and the approach used in `liquibase-pro`. It provides better maintainability and reliability for the Liquibase release process.

---

# :repeat: Manual Workflow Retry Guide

This guide shows how to manually trigger each workflow if it fails during an orchestrated release.

## Quick Reference

All workflows can be triggered manually via the GitHub Actions UI:
1. Go to **Actions** tab
2. Select the workflow from the left sidebar
3. Click **Run workflow** button
4. Fill in the required inputs
5. Click **Run workflow**

## Workflow-by-Workflow Instructions

### 1. Release Setup (`release-setup.yml`)

**When to use:** If the initial setup fails or you need to regenerate release metadata.

**Required inputs:**
- `tag`: Release tag (e.g., `v4.28.0`)

**Optional inputs:**
- `dry_run`: false (default) or true for dry-run mode
- `dry_run_branch_name`: Branch name for dry-run (e.g., `release/4.28.0`)

**Example:**
```
tag: v4.28.0
dry_run: false
dry_run_branch_name: (leave empty for production)
```

### 2. Release Manual Approval (`release-manual-approval.yml`)

**When to use:** Normally never on its own. Approval is per-run: a paused orchestrator run is
approved from **Review deployments** on that run's page, and dispatching this workflow standalone
only gates its own, empty run. It exists as the reusable gate job the orchestrator calls.

**How approval works:** The job carries `environment: release`, so GitHub pauses it and shows
**Review deployments** on the run page. Any reviewer on that environment can approve or reject,
except the person who started the run. There is no tracking issue and no keyword replies. The
reviewer list is Terraform-managed in [liquibase-release-environment.tf](https://github.com/liquibase/liquibase-infrastructure/blob/main/github/liquibase/repos/public/liquibase-release-environment.tf).

**Required inputs:**
- `version`: Version to approve (e.g., `4.28.0`)

**Optional inputs:**
- `dry_run`: false (default) - if true, workflow will be skipped

**Example:**
```
version: 4.28.0
dry_run: false
```

**Note:** One approval from the environment's reviewer list releases the gate, and the approver
cannot be the person who started the run (`prevent_self_review`).

### 3. Deploy to Maven Central (`release-deploy-maven.yml`)

**When to use:** If Maven deployment fails or times out.

**Required inputs:**
- `version`: Version to deploy (e.g., `4.28.0`)
- `tag`: Release tag (e.g., `v4.28.0`)

**Optional inputs:**
- `dry_run`: false (default) or true for USER_MANAGED publishing
- `dry_run_release_id`: Release ID for dry-run (only if dry_run=true)

**Example (Production):**
```
version: 4.28.0
tag: v4.28.0
dry_run: false
dry_run_release_id: (leave empty)
```

### 4. Deploy Javadocs (`release-deploy-javadocs.yml`)

**When to use:** If javadoc upload to S3 fails.

**Required inputs:**
- `version`: Version to deploy (e.g., `4.28.0`)
- `tag`: Release tag (e.g., `v4.28.0`)

**Optional inputs:**
- `dry_run`: false (default) - if true, workflow will be skipped

**Example:**
```
version: 4.28.0
tag: v4.28.0
dry_run: false
```

### 5. Publish to GitHub Packages (`release-publish-github-packages.yml`)

**When to use:** If GitHub Packages publishing fails.

**Required inputs:**
- `version`: Version to publish (e.g., `4.28.0`)
- `latestMergeSha`: Git commit SHA (e.g., `abc1234`)
- `timeStamp`: Build timestamp (e.g., `2024-01-15 14:30:00 UTC`)

**Optional inputs:**
- `dry_run`: false (default) - if true, workflow will be skipped

**Example:**
```
version: 4.28.0
latestMergeSha: abc1234
timeStamp: 2024-01-15 14:30:00 UTC
dry_run: false
```

**Note:** To get `latestMergeSha` and `timeStamp`, check the setup workflow outputs.

### 6. Deploy XSD Files (`release-deploy-xsd.yml`)

**When to use:** If XSD file deployment to S3 or SFTP fails.

**Required inputs:**
- `version`: Version to deploy (e.g., `4.28.0`)

**Optional inputs:**
- `dry_run`: false (default) - if true, workflow will be skipped

**Example:**
```
version: 4.28.0
dry_run: false
```

### 7. Release Docker Images (`release-docker.yml`)

**When to use:** If Docker image build fails.

**Required inputs:**
- `version`: Version to release (e.g., `4.28.0`)

**Optional inputs:**
- `dry_run`: false (default) or true to skip actual build

**Example:**
```
version: 4.28.0
dry_run: false
```

**Note:** This triggers a workflow in the `liquibase/docker` repository.

### 8. Publish Assets to S3 (`release-publish-assets-s3.yml`)

**When to use:** If S3 asset upload fails.

**Required inputs:**
- `version`: Version to publish (e.g., `4.28.0`)

**Optional inputs:**
- `dry_run`: false (default) - if true, workflow will be skipped

**Example:**
```
version: 4.28.0
dry_run: false
```

## Common Scenarios

### Scenario 1: Maven Deployment Failed

1. Fix the issue (e.g., Maven Central credentials, network issue)
2. Go to Actions → "Release Deploy Maven" → Run workflow
3. Fill in:
   ```
   version: 4.28.0
   tag: v4.28.0
   dry_run: false
   ```
4. Click "Run workflow"

### Scenario 2: Docker Build Timed Out

1. Go to Actions → "Release Docker" → Run workflow
2. Fill in:
   ```
   version: 4.28.0
   dry_run: false
   ```
3. Click "Run workflow"

### Scenario 3: Multiple Workflows Failed

If multiple workflows failed, you may want to:
1. Check if there's a common cause (e.g., network, credentials)
2. Re-run the orchestrator from the failed step onwards, OR
3. Manually trigger each failed workflow individually in the correct order

### Scenario 4: Need to Get Setup Outputs

If you need values like `latestMergeSha` or `timeStamp`:
1. Go to the original orchestrator run
2. Click on "setup" job
3. Expand "Setup Release Metadata" step
4. Look for the output values at the end of the logs

## Dependency Order

If manually running multiple workflows, follow this order:

```
1. release-setup (must run first)
   |
2. release-manual-approval (if needed)
   |
3. Parallel (can run these together):
   - release-deploy-javadocs
   - release-publish-github-packages
   - release-deploy-xsd
   - package (build-logic)
   |
4. release-deploy-maven      (waits for javadocs + github-packages + xsd)
   release-publish-assets-s3 (waits for package, not for maven)
   |
5. release-docker (waits for release-deploy-maven)
```

`release-docker` is last, not part of step 3. It declares `needs: [setup, manual-approval, deploy-maven]`, so dispatching it before Maven has finished publishes images for artifacts that are not on Maven Central yet.

## Tips

- **Always check the logs** of the failed workflow to understand what went wrong
- **Don't re-run workflows that succeeded** - focus only on the failures
- **For dry-run testing**, set `dry_run: true` to test without actually deploying
- **Keep the version consistent** - use the same version across all manual retries
- **Check orchestrator outputs** - the setup job outputs (SHA, timestamp) may be needed for some workflows

## Getting Help

If a workflow continues to fail:
1. Check the workflow logs for detailed error messages
2. Verify all secrets and credentials are correctly configured
3. Check AWS/Maven/Docker service status
4. Consult the team or escalate to DevOps

## :wastebasket: Removed workflows

Deleted 2026-08-27 (TECHOPS-1188). All seven were already `disabled_manually` in the
Actions API and none of them declares an `on: workflow_call` trigger, so no active
workflow could reach them: deleting the files removes the ability for anyone to
re-enable them from the Actions UI.

| Workflow | Own runs (all time) | Last run | Why it was removed |
|---|---|---|---|
| `build-branch.yml` | 3239 | 2026-08-25 | Per-PR SNAPSHOT publisher. `pull_request_target` + `packages: write` + `secrets: inherit`; branch snapshots are no longer published per PR. |
| `claude-code-review.yml` | 1109 | 2026-08-25 | Last 15 runs were all `startup_failure`. Replaced by `@claude review` on the gated `claude.yml` (active, 3675 runs). |
| `cleanup-branch-builds.yml` | 613 | 2026-08-25 | 29 of its last 30 runs failed, so it deleted nothing. |
| `fossa.yml` | 0 | never | Never executed once since creation. FOSSA runs today as the `fossa / fossa-scan` job of `run-tests.yml` via `build-logic/fossa_ai.yml`. |
| `installer-build-check.yml` | 2 | 2026-04-13 | Both runs failed. Installers are built by the release pipeline and rehearsed weekly by `dry-run-release.yml`. |
| `owasp-scanner.yml` | 1 | 2025-10-02 | One run ever. Dependency CVEs are covered by `codeql.yml`, `trivy-scan-published-images.yml` and Dependabot. |
| `weekly-integration-tests.yml` | 19 | 2025-11-16 | Scheduled run dead since 2025-11 and its Slack alert could never fire: it dispatched cross-repo to `build-logic` with `secrets.GITHUB_TOKEN`, which is scoped to this repo only. |

Not removed, and why: `build.yml` and `run-test-harness.yml` are marked
`disabled_manually` but still execute on every internal PR and every push to `main`.
`run-tests.yml` (active) calls `./.github/workflows/build.yml`, which calls
`liquibase/liquibase/.github/workflows/run-test-harness.yml@main`. Disabling a
workflow blocks its event triggers, never `workflow_call`. Both files can only be
deleted after `run-tests.yml` is retired at the PR #7944 cutover.
