# Three Strike Rule

## Rule Metadata
- **Version**: 2.0
- **Type**: global
- **Severity**: high
- **Enforcement**: manual (will be automatic)
- **Status**: active
- **Last Updated**: 2025-01-26
- **Validated Through**: Multiple time-wasting incidents during Snowflake extension

## Purpose
Prevents wasted time on wrong approaches by forcing help-seeking after repeated failures. Addresses the tendency to keep trying variations of a failing approach instead of stepping back.

## Rule Statement
**WHEN** you have tried 3 different approaches without success
**THEN** you MUST:
1. STOP attempting
2. DOCUMENT what you tried
3. ASK for help
**ELSE** time waste multiplies exponentially

## Scope
- **Applies To**: All states, all roles, all tasks
- **Exceptions**: None - this rule is absolute

## Detailed Specification

### Trigger Conditions
- [ ] First approach failed
- [ ] Second approach failed  
- [ ] Third approach failed
- [ ] OR: Spending >30 min on "simple" task
- [ ] OR: Making same error repeatedly
- [ ] OR: Guessing about implementation

### Required Actions
1. **Immediate**: Stop all attempts at current approach
2. **Document**: 
   - What you tried (all 3 approaches)
   - Why each failed
   - What assumptions you made
3. **Notify**: Transition to help state or ask user

### Examples

#### ✅ Correct Application
```
Situation: Trying to fix sequence ORDER validation
Attempt 1: Changed attribute name → Still failed
Attempt 2: Modified validation logic → Still failed  
Attempt 3: Checked SQL generator → Still failed
Action: STOPPED, documented all attempts, asked for help
Result: Discovered JAR caching issue in 15 minutes
```

#### ❌ Violation Example
```
Situation: Trying to fix sequence ORDER validation
Attempts 1-6: Kept modifying code randomly
Action: Continued trying without documenting or asking
Result: Wasted 2 hours, problem was environment not code
```

## Enforcement

### Detection
- **Automatic**: State machine tracks attempt count
- **Manual**: Time tracking shows >30 min on task

### Response
- **Warning**: After 2nd failed attempt
- **Action**: Force transition to help state after 3rd
- **Escalation**: Reduce confidence score for violations

## Metrics
- **Compliance Rate**: Currently ~60% (improving)
- **Violations Prevented**: ~5 hours/week saved
- **Time Saved**: 2-3 hours per incident

## Related Documents
- Processes: DEVELOPMENT_CYCLE (includes help states)
- State Machine: Help state transitions
- Patterns: When to seek architect/DevOps help

## Learning History
| Date | Learning | Impact |
|------|----------|--------|
| 2025-01-20 | Sequence ORDER took 3 hours due to no rule | Rule created |
| 2025-01-22 | JAR caching issue found quickly with rule | Validated effectiveness |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-20 | Initial version | Wasted time on sequence |
| 2.0 | 2025-01-26 | Template format | Standardization |