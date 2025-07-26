# Goal-Based Documentation Pattern

## Pattern Overview
**Validated Through**: User feedback on role reorganization
**Confidence Level**: 90%

## Organization Principles

### Structure by Goals, Not Roles
**What Works**: Organize documentation by what needs to be accomplished
**What Doesn't**: Abstract role descriptions without clear goals

### Example Structure
```
roles/[persona]/
├── [PERSONA]_ROLE_CONTEXT.md          # Role definition and learning
├── GOAL_[SPECIFIC_GOAL].md            # Validated processes for goal
└── patterns/                          # Validated implementation patterns
    └── [PATTERN_NAME]_PATTERN.md
```

## Content Requirements

### Four Criteria for Guide Content
All documentation must be:
1. **Validated through our process** - Actually tried and proven
2. **Explicitly agreed to by both parties** - User and AI consensus
3. **Concise and AI-consumable** - Clear, structured, scannable
4. **Human-verifiable** - Can be checked and confirmed

### Document Only Validated Learnings
**What to Include**:
- Actual processes that worked with evidence
- Specific issues encountered with root causes  
- Confidence levels based on real attempts
- Concrete examples from implementation

**What to Exclude**:
- Theoretical frameworks
- Assumptions without validation
- Generic best practices
- Unproven methodologies

## Document Templates

### Goal Document Structure
```markdown
# GOAL: [Specific Goal] ([Persona])

## Goal Definition
**Primary Responsibility**: [Clear one-line description]

## Validated Processes (From [Project/Feature])

### ✅ [Process Name]
**What Worked**: [Specific approach]
**Evidence**: [Actual result]
**Process**: [Step-by-step validated approach]

## Validated Issues (From [Project/Feature])

### ❌ [Issue Name]
**What Didn't Work**: [Specific failure]
**Evidence**: [What happened]
**Learning**: [What to do instead]

## Confidence Levels

### High Confidence (Keep Doing):
- [Process]: [X]%

### Low Confidence (Needs Improvement):
- [Process]: [X]% → [Improvement action]
```

## Maintenance Triggers

### When to Update
- After each retrospective
- When confidence levels change
- When new patterns are validated
- When old patterns fail

### Update Within 24 Hours
- Retrospective findings
- Confidence level changes
- New validated patterns
- Deprecated practices