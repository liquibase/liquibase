# Team Folder

## Purpose
This folder is for team-specific documentation, collaboration guidelines, and workflow processes.

## Historical Note
The LBCF (Liquibase Team Collaboration Framework) files that were previously in this folder have been:
1. **Extracted**: All valuable rules and processes have been moved to `/claude_guide/state_machine/`
2. **Removed**: Original files deleted to maintain clarity (available in git history if needed)
3. **Documented**: See `/claude_guide/state_machine/LBCF_FINAL_EXTRACTION_SUMMARY.md` for details

## Current Structure
- Active rules and processes: `/claude_guide/state_machine/`
- Role definitions: `/claude_guide/roles/`
- Implementation patterns: `/claude_guide/patterns/`

## For Historical Reference
To view the original LBCF files:
```bash
git log --oneline -- 'claude_guide/team/LBCF-*.md'
git show <commit-hash>:claude_guide/team/LBCF-FILENAME.md
```

Last commit with LBCF files: Use `git log` to find the commit before deletion.