This README provides detailed description of 2 files: `run-tests.yml` and `new-build.yml`.

### `run-tests.yml`

    Run the `Build & Test(Java)` and `integration-tests`

1. The workflow is triggered by the pull_request event type. This event type covers various actions related to pull requests, such as opening a pull request, reopening a pull request, synchronizing changes, and labeling pull requests.

   a. ```${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}```

   b. `${{ github.workflow }}` : name of the workflow that is currently being executed.

   c. `${{ github.event.pull_request.number }}` : the number of the pull request if the workflow was triggered by a pull request event.

   d. `github.ref` : refers to the Git reference (branch or tag) that triggered the workflow.

In summary, this configuration sets up a concurrency group for the workflow run based on the workflow name and either the pull request number or the Git reference. If a new workflow run is triggered within the same concurrency group, any ongoing runs in that group will be automatically canceled. This can be useful to prevent overlapping or conflicting workflow runs.


2. ```set-up``` job :

   a. ```ref: ${{ github.event.pull_request.head.sha || github.event.after}}```

   b. `ref` : This line specifies the ref parameter for the actions/checkout action. The purpose of the ref parameter is to define which commit, branch, or tag should be checked out. In this case, the expression ${{ github.event.pull_request.head.sha || github.event.after}} is used to determine the commit SHA that should be checked out.

   c. `github.event.pull_request.head.sha` : to retrieve the SHA of the latest commit in the head(current) branch of the pull request.

   d. `github.event.after` : If the workflow was triggered by a push event, it uses github.event.after to retrieve the SHA of the latest pushed commit.


3. usage of  ```GITHUB_HEAD_REF```

   a. The head ref or source branch of the pull request in a workflow run. This property is only set when the event that triggers a workflow run is either `pull_request` or `pull_request_target`. In our case it is `pull_request`. 


### `new-build.yml`