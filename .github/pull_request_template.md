## Impact

<!--- What types of changes does your code introduce? Put an `x` in all the boxes that apply: -->
- [ ] Bug fix (non-breaking change which fixes expected existing functionality)
- [ ] Enhancement/New feature (adds functionality without impacting existing logic)
- [ ] Breaking change (fix or feature that would cause existing functionality to change)
 
## Description

A clear and concise description of the change being made.  
Additional guidance [here](https://liquibase.jira.com/wiki/spaces/LB/pages/1274904896/How+to+Contribute+Code+to+Liquibase+Core).

- Introduce what was/will be done in the title
  - Titles show in release notes and search results, so make them useful
  - Use verbs at the beginning of the title, such as "fix", "implement", "improve", "update", and "add" 
  - Be specific about what was fixed or changed
  - Good Example: `Fix the --should-snapshot-data CLI parameter to be preserved when the --data-output-directory property is not specified in the command.`
  - Bad Example: `Fixed --should-snapshot-data`  
- If there is an existing issue this addresses, include "Fixes #XXXX" to auto-link the issue to this PR
- If there is NOT an existing issue, consider creating one.
  - In general, issues describe wanted change from an end-user perspective and PRs describe the technical change.
  - If this change is very small and not worth splitting off an issue, include `Steps To Reproduce`, `Expected Behavior`, and `Actual Behavior` sections in this PR as you would have in the issue.
- Describe what users need and how the fix will affect them
- Describe how the code change addresses the problem
- Ensure private information is redacted.

## Things to be aware of

- Describe the technical choices you made
- Describe impacts on the codebase

## Things to worry about

- List any questions or concerns you have with the change
- List unknowns you have 

## Additional Context

Add any other context about the problem here.

## Fast Track PR Acceptance Checklist:
<!--- Completing these speeds up the acceptance of your pull request -->
<!--- Put an `x` in all the boxes that apply. -->
<!--- If you're unsure about any of these, just ask us in a comment. We're here to help! -->
- [ ] Build is successful and all new and existing tests pass
- [ ] Added [Unit Test(s)](https://liquibase.jira.com/wiki/spaces/LB/pages/1274937609/How+to+Write+Liquibase+Core+Unit+Tests)
- [ ] Added [Integration Test(s)](https://liquibase.jira.com/wiki/spaces/LB/pages/1276608569/How+to+Write+Liquibase+Core+Integration+Tests)
- [ ] Added [Test Harness Test(s)](https://github.com/liquibase/liquibase-test-harness/pulls)
- [ ] Documentation Updated

## Need Help?
Come chat with us in the [Liquibase Forum](https://forum.liquibase.org/).
