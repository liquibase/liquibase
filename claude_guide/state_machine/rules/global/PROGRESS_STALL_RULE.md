# Progress Stall Rule

## Rule Metadata
- **Version**: 1.0
- **Type**: global
- **Severity**: high
- **Enforcement**: automatic (timer-based)
- **Status**: active
- **Last Updated**: 2025-01-26
- **Confidence**: 75% (proven concept, threshold needs validation)


## Performance Metrics
- **Times Applied**: 0
- **Success Rate**: N/A
- **Last Applied**: Never
- **Average Time Impact**: Unknown

## Purpose
Detects when a task is too large or complex by monitoring time without completion. Forces task breakdown to maintain momentum and prevent overwhelm.

## Rule Statement
**WHEN** task in progress >45 minutes without completion
**THEN** MUST break into smaller subtasks
**BECAUSE** large tasks hide complexity and kill momentum
**ELSE** tasks expand indefinitely (Parkinson's Law)

## Scope
- **Applies To**: All development tasks
- **Exceptions**: Explicit long-running tasks (migrations, full test runs)

## Detailed Specification

### Time Thresholds
- **Green Zone** (0-30 min): Normal progress expected
- **Yellow Zone** (30-45 min): Monitor closely
- **Red Zone** (45+ min): Intervention required

### Stall Detection Triggers
- [ ] 45 minutes elapsed
- [ ] Status still "in progress"
- [ ] No subtasks completed
- [ ] No meaningful checkpoints
- [ ] Confidence decreasing

### Required Actions at 45 Minutes

#### Step 1: Stop and Assess
```markdown
## Progress Stall - Assessment
Current Task: [What you're doing]
Time Spent: 45+ minutes
Progress Made: [What's done]
Blockers: [What's blocking]
```

#### Step 2: Break Down Task
```markdown
## Task Breakdown
Original Task: [Too large]

Subtasks:
1. [ ] Smallest working piece (15 min)
2. [ ] Next logical step (15 min)
3. [ ] Integration (10 min)
4. [ ] Testing (15 min)
```

#### Step 3: Complete One Subtask
- Pick smallest/easiest subtask
- Complete in <15 minutes
- Build momentum
- Reassess remaining work

### Task Breakdown Templates

#### Feature Implementation
```markdown
Too Large: "Implement CreateDatabase feature"
Better:
1. [ ] Create change class skeleton (10 min)
2. [ ] Add basic properties (10 min)
3. [ ] Implement validate() (15 min)
4. [ ] Create statement class (10 min)
5. [ ] Write SQL generator (20 min)
6. [ ] Add unit tests (15 min)
```

#### Bug Investigation
```markdown
Too Large: "Fix integration test failures"
Better:
1. [ ] Reproduce locally (10 min)
2. [ ] Identify failing assertion (5 min)
3. [ ] Trace root cause (15 min)
4. [ ] Implement fix (10 min)
5. [ ] Verify fix works (5 min)
```

#### Research Task
```markdown
Too Large: "Figure out Snowflake syntax"
Better:
1. [ ] Find official docs (5 min)
2. [ ] Find working example (10 min)
3. [ ] Test minimal case (10 min)
4. [ ] Document pattern (5 min)
```

## Examples

### Example 1: Successful Breakdown
```markdown
Original: "Implement complex validation logic"
Time at 45 min: Still designing approach

Breakdown:
1. ✅ Validate required fields (8 min)
2. ✅ Validate field combinations (12 min)
3. ✅ Add error messages (5 min)
4. ✅ Test edge cases (10 min)

Result: Completed in 35 min after breakdown
Lesson: Breakdown forced clarity
```

### Example 2: Hidden Complexity
```markdown
Original: "Add WITH clause support"
Time at 45 min: Multiple approaches failing

Breakdown revealed:
1. ❌ Approach doesn't fit pattern
2. 💡 Need different strategy
3. ✅ Found similar pattern
4. ✅ Adapted successfully

Result: Breakdown exposed wrong approach
Lesson: Large tasks hide assumptions
```

## Common Task Size Mistakes

### Too Large
- "Implement entire feature"
- "Fix all test failures"
- "Refactor whole module"
- "Research everything about X"

### Right-Sized
- "Create change class with 5 properties"
- "Fix specific test case"
- "Extract one method"
- "Find pattern for WITH clause"

## Task Size Guidelines

### 15-Minute Tasks
- Add single property
- Write one test
- Fix simple bug
- Document one pattern

### 30-Minute Tasks
- Create basic class
- Implement validation
- Add error handling
- Research specific syntax

### 45-Minute Tasks (Maximum)
- Complete small feature
- Debug complex issue
- Integrate components
- Write test suite

## Enforcement

### Detection
- Timer starts with task
- Check at 30, 45 minutes
- Alert if no progress

### Response
- **30 min**: Gentle reminder
- **45 min**: Force breakdown
- **60 min**: Escalate to help

### Tracking
```markdown
## Task Progress Log
10:00 - Started: CreateDatabase feature
10:30 - Progress check: Change class done ✅
10:45 - STALL ALERT: Breaking down
10:50 - Subtask 1 complete ✅
11:05 - Subtask 2 complete ✅
11:15 - Full task complete ✅
```

## Benefits of Small Tasks

### Psychological
- Clear progress visible
- Dopamine hits from completion
- Reduced overwhelm
- Better estimation

### Practical
- Easier to help with
- Natural checkpoint
- Simple rollback points
- Clear communication

## Metrics
- **Current Confidence**: 75% (proven concept, threshold needs validation)
- **Success Metric**: Tasks completed <45 min
- **Value Metric**: Increased completion rate

## Effectiveness Metrics
- **Time Saved**: To be measured
- **Errors Prevented**: To be measured
- **Rework Reduced**: To be measured

## Learning Connections
- **Reinforces**: To be identified
- **Conflicts With**: None identified
- **Depends On**: To be identified
- **Leads To**: To be identified

## Feedback Protocol
- **Success**: +10% confidence (first success), +5% (subsequent)
- **Failure**: -15% confidence
- **Modification**: Reset to 50%
- **Review Triggers**: After 10 uses or monthly

## Related Documents
- Rules: TIME_ESTIMATION_RULE (estimates)
- Rules: THREE_STRIKE_META_RULE (attempts)
- Processes: Task breakdown templates

## Confidence Evolution
| Date | Event | Old Conf | New Conf | Evidence |
|------|-------|----------|----------|----------|
| 2025-01-26 | Created | 0% | 50% | New rule from LBCF |
| 2025-01-26 | Initial validation | 50% | 75% | Rule created from LBCF |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-26 | Initial version | Prevent task overwhelm |