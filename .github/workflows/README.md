# Liquibase Release Workflows

## Liquibase Build Process Refactoring

https://datical.atlassian.net/wiki/spaces/DEV/pages/3371335681/Liquibase+Build+Process+Refactoring#New-Build.yml-%E2%86%92

## Liquibase DryRun Releases

The `dryRun` process simulates as much as possible our current production liquibase release workflow. It mimics all important release activities in a nightly cadence so we can anticipate automation issues before the real release.

## What a DryRun Release does?

The following actions are the same as a regular liquibase release, no modifications:

- Get latests liquibase artifacts from the `run-tests.yml` workflow
- Build included extensions in core
- Build azure uber jars
- Re-version artifacts to `dry-run-GITHUB_RUN_ID` version. i.e `dry-run-10522556642`
- Build installers
- Attach artifacts to a dryRun draft release
- Deploy artifacts to Maven, to our internal Maven repository: `https://repo.liquibase.net/repository/dry-run-sonatype-nexus-staging`
- Delete the dryRun draft release. i.e `dry-run-10522556642`
- Delete the dryRun repository tag. i.e `vdry-run-10522556642`

## What a DryRun Release does not do?

- Generate PRO tags
- Generated packages: `deb`, `rpm`, `brew` and the rest of them
- Upload `javadocs` and `xsds` to `S3`
- Deploy artifacts to `GPM`

## How a DryRun Release works?

You can check the `dry-run-release.yml` worklow but it is basically composed of calls to the existing release workflows such as `create-release.yml` and `release-published` sending them a new input: `dry_run: true` to controll which steps are executed for regular releases and dryRun release:

```yml
[...]

  dry-run-create-release:
    needs: [ setup ]
    uses: liquibase/liquibase/.github/workflows/create-release.yml@master
    with:
      version: "dry-run-${{ github.run_id }}"
      runId: ${{ needs.setup.outputs.dry_run_id }}
      standalone_zip: false
      dry_run: true
    secrets: inherit

[...]

  dry-run-release-published:
    needs: [ setup, dry-run-get-draft-release ]
    uses: liquibase/liquibase/.github/workflows/release-published.yml@master
    with:
      tag: "vdry-run-${{ github.run_id }}"
      dry_run_release_id: ${{ needs.dry-run-get-draft-release.outputs.dry_run_release_id }}
      dry_run: true
    secrets: inherit

[...]
```

Here you can see all the stuff which is tested:

1. Create a draft release
2. Get the draft release ID
3. Simulate a release publish event. In blue you can see the internal Maven deploy to `https://repo.liquibase.net/repository/dry-run-sonatype-nexus-staging`
4. Clean up dryRun resources

![](./doc/img/dry-run.png)

The process will end up with the dryRun artifacts published in our Maven repository:

![](./doc/img/nexus.png)
