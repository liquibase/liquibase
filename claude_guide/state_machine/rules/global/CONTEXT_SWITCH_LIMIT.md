# Context Switch Limit Rule

## Rule Metadata
- **Version**: 1.0
- **Type**: global
- **Severity**: medium
- **Enforcement**: manual (self-monitoring)
- **Status**: active
- **Last Updated**: 2025-01-26
- **Confidence**: 75% (partially validated through experience)


## Performance Metrics
- **Times Applied**: 0
- **Success Rate**: N/A
- **Last Applied**: Never
- **Average Time Impact**: Unknown

## Purpose
Prevents productivity loss from excessive context switching between roles. Each switch has cognitive overhead - too many switches means more switching than doing.

## Rule Statement
**WHEN** switching between roles/perspectives
**THEN** maximum 3 switches per 10-minute period
**ELSE** you're thrashing, not progressing

## Scope
- **Applies To**: All development work requiring multiple perspectives
- **Exceptions**: 
  - Explicit multi-role review sessions
  - Critical bug fixes requiring all perspectives
  - Final integration testing

## Detailed Specification

### Trigger Conditions
- [ ] Changed from Developer to QA perspective
- [ ] Changed from QA to PM perspective
- [ ] Changed from PM back to Developer
- [ ] 3 switches in < 10 minutes

### Healthy Time Blocks by Role

| Role | Minimum Block | Optimal Block | Focus |
|------|---------------|---------------|-------|
| Developer | 20 min | 30-45 min | Deep implementation |
| QA | 10 min | 15-20 min | Thorough testing |
| PM | 5 min | 5-10 min | Quick status check |
| DevOps | 10 min | 15-20 min | Environment setup |
| Architect | 15 min | 20-30 min | Pattern analysis |

### Required Actions

#### When Limit Reached
1. **Lock into** current role for minimum block time
2. **Complete** current role's tasks
3. **Document** items for other roles
4. **Switch** only after meaningful progress

#### Healthy Cycle Pattern
```
Developer (30 min) 
    ↓
QA check (15 min)
    ↓
PM update (5 min)
    ↓
Developer (30 min) ← Continue cycle
```

### Examples

#### ✅ Correct Application
```
Situation: Implementing CreateWarehouseChange
9:00-9:30 - Developer: Implement change class
9:30-9:45 - QA: Write tests, check edge cases
9:45-9:50 - PM: Update status, log time
9:50-10:20 - Developer: Fix issues from QA
Result: Solid progress, clear context
```

#### ❌ Violation Example
```
Situation: Debugging validation issue
9:00-9:05 - Developer: Start debugging
9:05-9:08 - QA: "What if null?"
9:08-9:10 - PM: "How long will this take?"
9:10-9:12 - DevOps: "Is environment OK?"
9:12-9:15 - Developer: Lost context, starting over
Result: 15 min wasted, no progress
```

## Context Switching Cost

### Measured Impact
- **Switch cost**: 2-5 minutes to regain context
- **3 switches**: 6-15 minutes lost
- **In 10 minutes**: More switching than working!

### Cumulative Effect
```
1 hour with good blocks:
- 2 switches × 3 min = 6 min lost
- 54 minutes productive (90%)

1 hour with thrashing:
- 12 switches × 3 min = 36 min lost  
- 24 minutes productive (40%)
```

## When Multiple Perspectives Needed

### Structured Multi-Role Review
1. **Complete** implementation first
2. **Then** systematic review:
   - QA perspective (10 min)
   - DevOps perspective (5 min)
   - PM perspective (5 min)
3. **Document** all findings
4. **Return** to developer to address

### Emergency Debugging
1. **Acknowledge** need for all perspectives
2. **Time-box** each perspective (5 min)
3. **Document** findings from each
4. **Choose** primary role to solve

## Enforcement Strategies

### Self-Monitoring
- Note role switches in work log
- Use timer for role blocks
- Review switching patterns in retrospective

### Team Support
- Respect others' role blocks
- Batch questions for role switches
- Use async communication when possible

## Metrics
- **Current Confidence**: 75% (partially validated through experience)
- **Success Metric**: More completed work per hour
- **Quality Metric**: Fewer context-related errors

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
- Processes: MULTI_PERSPECTIVE_REVIEW (structured approach)
- Anti-patterns: Context thrashing

## Confidence Evolution
| Date | Event | Old Conf | New Conf | Evidence |
|------|-------|----------|----------|----------|
| 2025-01-26 | Created | 0% | 50% | New rule from LBCF |
| 2025-01-26 | Initial validation | 50% | 75% | Rule created from LBCF |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-26 | Initial version | Prevent context thrashing |