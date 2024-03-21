#!/bin/bash -e

set -e

echo "Before running remember to install github cli (https://cli.github.com/) and configure it (gh auth login) to automatically create the PR"

if test "a" = "a$1"; then
	echo "PR number is a required parameter."
	exit 1
fi

echo "Cloning PR $1 to your liquibase repo."

PR=$1

git fetch
git checkout master
git pull
git fetch origin pull/$PR/head

#create new local branch
#git branch -D $PR-copy
git checkout -b $PR-copy

# cherry-pick all commits from PR
for i in $(git rev-list --reverse --no-merges origin/master..FETCH_HEAD); do
	git cherry-pick  $i
done

# up up up
git push --set-upstream origin $PR-copy

TITLE=$(gh pr view --json title $PR | cut -d \" -f 4)

gh pr create -d -m "TestPRs" --title "Test - $TITLE" --body "Test PR for $PR - do not merge" 

gh pr checks


echo "Bye~"
