// -----------------------------------------------------------------
// THIS FILE SHOULD BE MANUALLY COPY/PASTED ACROSS ALL THE USED REPOSITORIES ON CHANGES SO THEY STAY IN SYNC
// -----------------------------------------------------------------

module.exports = ({github, context}) => {

    return {

        getRepositoryOwner: function () {
            if (context.payload.repository) {
                return context.payload.repository.organization;
            } else {
                console.log(context.payload)
                throw "Could not determine repository owner"
            }
        },

        getRepositoryName: function () {
            if (context.payload.repository) {
                return context.payload.repository.name;
            } else {
                console.log(context.payload)
                throw "Could not determine repository name"
            }
        },

        getCurrentBranch: function () {
            if (context.payload.pull_request) {
                return this.cleanBranchRef(context.payload.pull_request.head.ref);
            } else {
                return this.cleanBranchRef(context.payload.ref);
            }
        },

        getCurrentBranchLabel: function () {
            if (context.payload.pull_request) {
                return this.cleanBranchRef(context.payload.pull_request.head.label);
            } else {
                return this.cleanBranchRef(context.payload.ref);
            }
        },

        getCurrentSha: function () {
            if (context.payload.pull_request) {
                return this.cleanBranchRef(context.payload.pull_request.head.sha);
            } else {
                return this.cleanBranchRef(context.payload.after);
            }
        },

        cleanBranchRef: function (branch) {
            if (!branch) {
                return branch;
            }

            return branch.replace("refs/heads/", "")
                .replace("refs/heads/tags", "");

        },

        findMatchingBranch: async function (owner, repo, branchesToCheck) {
            if (!branchesToCheck) {
                if (context.payload.pull_request) {
                    branchesToCheck = [context.payload.pull_request.head.ref, context.payload.pull_request.base.ref, "main", "master"]
                } else {
                    branchesToCheck = [context.payload.ref, "main", "master"]
                }
            }

            for (let branchName of branchesToCheck) {
                if (!branchName) {
                    console.log("Skipping empty branch name: " + branchName);
                    continue
                }

                branchName = this.cleanBranchRef(branchName);
                try {
                    let branch = await github.rest.repos.getBranch({
                        "owner": owner,
                        "repo": repo,
                        "branch": branchName,
                    });

                    let returnData = {
                        "name": branchName,
                        "sha": branch.data.commit.sha,
                    };

                    //add PR info
                    let pulls = await github.rest.pulls.list({
                        "owner": owner,
                        "repo": repo,
                        "head": `${owner}:${branchName}`,
                        "per_page": 1,
                        "page": 1,
                    });

                    if (pulls.data.length === 0) {
                        console.log(`No pull request for branch ${branchName}`);

                        if (branchName === "master" || branchName === "main") {
                            console.log(`Expect no pull request for ${branchName}`);
                        } else {
                            continue;
                        }
                    } else {
                        console.log(`Found PR for branch ${branchName}`);

                        returnData.pullRequestId = pulls.data[0].number;
                        returnData.pullRequestState = pulls.data[0].state;
                    }

                    let pageNumber = 1;
                    const maxPagesToCheck = 10;
                    let matchingBuildFound = false;
                    while(!matchingBuildFound) {
                        try { //add build info
                            let workflowId = "build.yml";
                            if (repo === "liquibase-test-harness") {
                                workflowId = "main.yml";
                            } else if (repo === "liquibase-pro-tests") {
                                workflowId = "test.yml";
                            }


                            console.log("Reading workflow run results from page", pageNumber)
                            let runs = await github.rest.actions.listWorkflowRuns({
                                "owner": owner,
                                "repo": repo,
                                "workflow_id": workflowId,
                                "per_page": 100,
                                "page": pageNumber,
                            });

                            if (runs.data.workflow_runs.length !== 0) {
                                for (let run of runs.data.workflow_runs) {
                                    if (run.event === 'pull_request_target') {
                                        if (!returnData.pullRequestId) {
                                            console.log("Skipping pull_request_target from non-pull-request build " + run.html_url);
                                            continue;
                                        }
                                        if (run.head_repository && run.head_repository.fork) {
                                            console.log("Skipping pull_request_target from fork " + run.head_repository.full_name);
                                            continue;
                                        }
                                    }
                                    if (run.head_branch != branchName) {
                                        console.log("Skipping run from branch: " + run.head_branch);
                                        continue;
                                    }

                                    console.log(`Found build for branch ${branchName}`);

                                    if (!returnData.workflowId) {
                                        returnData.workflowId = run.id;
                                    }

                                    if (!returnData.runNumber) {
                                        returnData.runNumber = run.run_number;
                                        returnData.runAttempt = run.run_attempt;
                                        returnData.runStatus = run.status;
                                        returnData.runConclusion = run.conclusion;
                                        returnData.runHtmlUrl = run.html_url;
                                        returnData.runRerunUrl = run.rerun_url;
                                    }

                                    if (run.status === "completed" && run.conclusion === "success") {
                                        console.log(`Found successful build for branch ${branchName}`);
                                        returnData.lastSuccessfulRunNumber = run.run_number;
                                        returnData.lastSuccessfulRunAttempt = run.run_attempt;
                                        returnData.lastSuccessfulRunStatus = run.status;
                                        returnData.lastSuccessfulRunConclusion = run.conclusion;
                                        returnData.lastSuccessfulRunHtmlUrl = run.html_url;
                                        returnData.lastSuccessfulRunRerunUrl = run.rerun_url;
                                        returnData.lastSuccessfulWorkflowId = run.id;

                                        matchingBuildFound = true;
                                        break;
                                    } else {
                                        console.log(`Found build ${run.run_number} was status: ${run.status} conclusion:${run.conclusion}`);
                                    }
                                }
                            }

                            if (!returnData.workflowId) {
                                console.log(`No build for branch ${branchName}`);
                            }

                        } catch (error) {
                            console.log(`Error getting build info for ${branchName}`)
                            console.log(error);
                            if (error.status === 404) {
                                console.log(`Cannot get build info for ${branchName}`);
                            } else {
                                throw error;
                            }
                        }
                        if (pageNumber >= maxPagesToCheck) {
                            console.log("Hit page limit maximum of", maxPagesToCheck);
                            matchingBuildFound = true;
                        }
                        pageNumber++;
                    }

                    console.log("Matching branch information: ");
                    console.log(returnData);

                    return returnData
                } catch
                    (error) {
                    if (error.status === 404) {
                        //try next branch
                        console.log(`No branch ${branchName}`);
                    } else {
                        console.log(error)
                        throw (`Checking branch ${branchName} returned ${error.status}`);
                    }
                }
            }
        }
    }
}
