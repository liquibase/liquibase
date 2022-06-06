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

        }
    }
}
