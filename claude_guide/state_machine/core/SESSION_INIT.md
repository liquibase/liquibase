# Session Initialization Protocol

## When Starting a New Session

```bash
# 1. Load State Machine
Read: /claude_guide/state_machine/README.md
Read: /claude_guide/state_machine/PROJECT_STATE.md

# 2. Identify Current State
From PROJECT_STATE.md:
- current_state: [phase_role]
- confidence: [X%]
- context: [current feature/task]

# 3. Load Role Context
Read: /claude_guide/roles/[role]/[ROLE]_ROLE_CONTEXT.md

# 4. Load Current Process
Read: /claude_guide/roles/[role]/GOAL_[CURRENT_GOAL].md

# 5. Check Patterns If Needed
Read: /claude_guide/roles/[role]/patterns/*.md

# 6. Execute Process
Follow loaded process until exit criteria met

# 7. Evaluate Transitions
Check: confidence levels, quality gates, global rules

# 8. Update State
Update: PROJECT_STATE.md with new state
```

## Quick Reference Card

| Current State | Load These Documents | Check These Rules |
|--------------|---------------------|-------------------|
| requirements_product_owner | PRODUCT_OWNER_ROLE_CONTEXT.md | Requirements clear? >70% confidence? |
| implementation_developer | DEVELOPER_ROLE_CONTEXT.md, GOAL_PROVE_CODE_WORKS.md | Code complete? >85% confidence? <3 attempts? |
| test_qa | QA_ROLE_CONTEXT.md, GOAL_PROVE_CODE_WONT_BREAK.md | Tests pass? Environment OK? |
| help_architect | ARCHITECT_ROLE_CONTEXT.md | Pattern found? Confidence boosted? |
| document_technical_writer | TECHNICAL_WRITER_ROLE_CONTEXT.md | Docs complete? Patterns captured? |
| retrospective_scrum_master | SCRUM_MASTER_ROLE_CONTEXT.md, RETROSPECTIVE_PATTERN.md | Learnings captured? Friction noted? |

## State Machine Reminders

- **Three Strikes Rule**: After 3 failed attempts → help state
- **Confidence Threshold**: <70% → need help, >85% → can progress
- **Time Box**: >3x estimate → reassess approach
- **Always Update**: PROJECT_STATE.md on every transition
- **Learn**: Update confidence and patterns after each cycle