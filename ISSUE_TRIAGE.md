# Triage Process and GitHub Labels for Liquibase

This document describes how the Liquibase OSS team triages issues on GitHub.

Issue triage should occur daily so that issues can be prioritized against other work currently planned.

If an issue or PR obviously relates to a release regression, we will assign an appropriate priority (`P0` or `P1`) and ensure that someone from the team is actively working to resolve it.

## Triage Steps

Anyone from the team can triage issues as they arrive in the repository, although generally it is the OSS product manager.

New untriaged issues can be found by filtering the issues list for those [not in a project](https://github.com/liquibase/liquibase/issues?q=is%3Aissue+is%3Aopen+no%3Aproject).

Follow these steps to triage an issue.

### Step 1: Does the issue have an attached pull request?

If the issue has a PR associated with it add the `needs_guidance` label to the issue. Next, add the issue to the project [Open Source](https://github.com/orgs/liquibase/projects/3/views/1) with `In Development PR Issues` status. You can skip step 2.

If the issue does not have a PR, go to step 2.

### Step 2: Does the issue have enough information?

Gauge whether the issue has enough information to act upon.
This typically includes the version of Liquibase being used, type of database, and steps to reproduce.

- If the issue may be legitimate but needs more information, ask the issue author and add the `awaiting_response` label.
- If the issue does not provide clear steps to reproduce the problem then ask the issue author for the steps to reproduce and add the `needs_reproduction` label.

These labels can be revisited if the author can provide further clarification.

If the issue does have enough information, move on to step 3.

### Step 3: Labeling the issue and going through Issue Triage

Begin by labeling the issue appropriately.

| Issue Template Section | Valid Labels                                                                                                                                                                                                                                          |
| ----- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Database Vendor & Version | Related labels start with **DB\***                             |
| Liquibase Integration | Related labels start with **Integration\*** or if Command API or Java API the label is **java-api**                                |
| Liquibase Extension | Related labels start with **Extension\***                     |
| OS and/or Infrastructure Type/Provider | Related labels start with **OS\*** |  


If the issue is considered a bug, apply the `TypeBug` label and a priority label (see next section).

If the issue is a feature request, RFC, or discussion, apply the `TypeEnhancement` label.

Unless they're capturing a legitimate bug, redirect requests for debugging help or advice to a more
appropriate channel such as the [forum](https://forum.liquibase.org/), [Discord](https://discord.com/invite/9yBwMtj) or, for Liquibase Pro customers, their
support contact.

Once the issue is labeled, add the issue to the project [Issue Triage](https://github.com/orgs/liquibase/projects/11) with `New` status.

### Step 3: Set a Priority

Once an issue has been triaged in the `Issue Triage` project and moved to `Open Source`, set a priority level in the [Open Source](https://github.com/orgs/liquibase/projects/3/views/1) GitHub project.

| Label | Description                                                                                                                                                                                                                                          |
| ----- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| P0    | An issue that causes a full outage, breakage, or major function unavailability for everyone, without any known workaround. The issue must be fixed immediately, taking precedence over all other work. The issue should receive updates at least once per day. |
| P1    | An issue that significantly impacts a large percentage of users; if there is a workaround it is partial or overly painful.                                 |
| P2    | The issue is important to a large percentage of users, with a workaround. Issues with workarounds that would otherwise be P0 or P1.                     |
| P3    | An issue that is relevant to core functions, but does not impede user progress. Important, but not urgent.                                                                                                                                                |
| P4    | A relatively minor issue that is not relevant to core functionality, or relates only to the attractiveness or pleasantness of use of the system. Good to have but not necessary changes/fixes.                                                           |
| P5    | The team acknowledges the request but (due to any number of reasons) does not plan to work on or accept contributions for this request. The issue remains open for discussion.                                                                       |

Issues marked with "TypeEnhancement" don't require a priority.

### Step 4: Additional labels

The following labels can also be added to issues to provide further information to help prioritize them:

| Label         | Description                                                                                                                   |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| DocNeeded      | The issue requires a separate documentation ticket to be opened.        |
| feature*       | The issue relates to a specific Liquibase command or feature set.                  |
| good first issue | The issue has been curated by the team and is a good candidate for new contributors.                          |