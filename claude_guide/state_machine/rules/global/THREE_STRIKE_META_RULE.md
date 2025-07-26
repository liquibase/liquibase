# Three Strike Meta Rule

## Rule Metadata
- **Version**: 3.0
- **Type**: global meta-rule
- **Severity**: critical
- **Enforcement**: manual (will be automatic)
- **Status**: active
- **Last Updated**: 2025-01-26
- **Validated Through**: Prevents endless loops in problem-solving

## Purpose
Meta-rule that prevents any iterative process from continuing indefinitely. Ensures that after 3 cycles of any problem-solving process without meaningful progress, external help must be sought. This is the "circuit breaker" for all other processes.

## Rule Statement
**WHEN** any process/rule has been attempted 3 times without success
**THEN** you MUST stop and seek help
**ELSE** you're in an infinite loop

## Scope
- **Applies To**: ALL processes and rules, especially:
  - ITERATION_WITHOUT_PROGRESS cycles
  - Research-implement cycles  
  - Debug-fix cycles
  - Any problem-solving loop
- **Exceptions**: None - this is the ultimate safety net

## How It Works

### Relationship to Other Rules
```
ITERATION_WITHOUT_PROGRESS_RULE
    ↓
Cycle 1: Validate assumptions → Research → Plan → Execute
    ↓ (if fails)
Cycle 2: New assumptions → New research → New plan → Execute  
    ↓ (if fails)
Cycle 3: Different approach → Different research → Different plan → Execute
    ↓ (if fails)
THREE STRIKE META RULE TRIGGERS → MUST GET HELP
```

### What Counts as a Strike
1. **Strike 1**: First complete cycle of a process fails
2. **Strike 2**: Second attempt with adjustments fails
3. **Strike 3**: Third attempt with new approach fails
4. **STOP**: No fourth attempt allowed without help

### Examples

#### With ITERATION_WITHOUT_PROGRESS
```
Iteration cycles on SQL escaping:
Strike 1: Assumptions wrong, research, new plan → Still fails
Strike 2: New assumptions, more research, different plan → Still fails
Strike 3: Completely different approach → Still fails
TRIGGERED: Must ask for help (discovered it was env issue)
```

#### With Research-Implement Cycles
```
Trying to understand complex API:
Strike 1: Research 30 min → Implement → Fails
Strike 2: Research different docs → Implement → Fails
Strike 3: Research examples → Implement → Fails
TRIGGERED: Must ask expert (missing auth step)
```

## Enforcement

### Detection
- Count process cycles, not individual attempts
- Track at process boundary, not within process
- Clear strike counter per problem

### Response
- Strike 1-2: Warning to user
- Strike 3: Force help-seeking
- No strike 4: Block further attempts

## The Safety Net Hierarchy

```
Level 1: Individual Rules (handle specific situations)
    ↓
Level 2: Process Rules (handle workflows)
    ↓
Level 3: THREE STRIKE META RULE (prevents infinite loops)
    ↓
Level 4: Human intervention required
```

## Integration with State Machine

The state machine should:
1. Track strike count per problem
2. Reset strikes on success
3. Force transition to help state after 3
4. Log meta-rule triggers

## Metrics
- **Confidence**: 95% (proven loop breaker)
- **Triggers per week**: Track to identify problem areas
- **Time saved**: Average 2-4 hours per trigger

## Related Documents
- Rules: ITERATION_WITHOUT_PROGRESS_RULE (process being limited)
- Processes: All iterative processes
- States: Help states in state machine

## Learning History
| Date | Learning | Impact |
|------|----------|--------|
| 2025-01-26 | Refactored as meta-rule | Clearer rule hierarchy |
| 2025-01-20 | Original creation | Prevented time waste |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-20 | Initial version | Stop wasted time |
| 2.0 | 2025-01-26 | Template format | Standardization |
| 3.0 | 2025-01-26 | Meta-rule refactor | Eliminate overlap with iteration rule |