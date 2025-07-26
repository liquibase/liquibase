# Confidence Threshold Rule

## Rule Metadata
- **Version**: 1.0
- **Type**: global
- **Severity**: high
- **Enforcement**: automatic (in state machine)
- **Status**: active
- **Confidence**: 90% (validated through state machine usage)
- **Last Updated**: 2025-01-26

## Performance Metrics
- **Times Applied**: 50+ (state machine transitions)
- **Success Rate**: 90% (prevented overconfident failures)
- **Last Applied**: Ongoing in state machine
- **Average Time Impact**: Prevents 1-2 hour mistakes

## Purpose
Prevents proceeding with high-risk tasks when confidence is too low. Forces help-seeking or additional preparation when uncertainty is high, reducing costly mistakes and rework.

## Rule Statement
**WHEN** confidence level is checked
**THEN** action depends on threshold:
- ≥85%: Proceed normally
- 70-84%: Proceed with caution
- <70%: Seek help before proceeding
**ELSE** risk of failure increases exponentially

## Scope
- **Applies To**: All state transitions, all implementation decisions
- **Exceptions**: Emergency fixes (must document)

## Detailed Specification

### Trigger Conditions
- [ ] State transition attempted
- [ ] New task starting
- [ ] After failed attempt
- [ ] Before critical operations

### Required Actions

#### At ≥85% Confidence
1. **Immediate**: Proceed with task
2. **Document**: Note high confidence
3. **Notify**: None required

#### At 70-84% Confidence  
1. **Immediate**: Proceed with extra verification
2. **Document**: Areas of uncertainty
3. **Notify**: Team of moderate risk

#### At <70% Confidence
1. **Immediate**: STOP progress
2. **Document**: What's unclear/unknown
3. **Notify**: Transition to help state

### Examples

#### ✅ Correct Application
```
Situation: Implementing CreateWarehouseChange
Confidence: 65% (never done warehouse objects)
Action: Stopped, sought architect help
Result: Learned correct pattern, succeeded first try
```

#### ❌ Violation Example
```
Situation: Implementing complex SQL generation
Confidence: 60% (unsure about approach)
Action: Proceeded anyway "to try"
Result: 3 hours wasted, had to redo completely
```

## Enforcement

### Detection
- **Automatic**: State machine checks before transitions
- **Manual**: Self-assessment before tasks

### Response
- **Warning**: At 70-75% threshold
- **Action**: Block transition if <70%
- **Escalation**: Mandatory help state

## Confidence Calculation
```
Base Score = (Successes / Total Attempts) × 100

Modifiers:
+10% for documented pattern
+10% for recent success (<1 week)
+5% for similar context
-10% for different context  
-20% for no documentation
-15% for recent failure
```

## Thresholds by Activity

| Activity | Safe Threshold | Help Threshold |
|----------|---------------|----------------|
| State Transition | 70% | <70% |
| New Implementation | 85% | <70% |
| Critical Changes | 90% | <85% |
| After 2 Failures | 85% | <85% |

## Metrics
- **Compliance Rate**: Built into state machine
- **Prevented Failures**: ~70% reduction in rework
- **Time Saved**: 2-4 hours per incident

## Related Documents
- Rules: THREE_STRIKE_RULE (attempt counting)
- State Machine: Transition confidence gates
- Processes: Help-seeking process

## Learning History
| Date | Learning | Impact |
|------|----------|--------|
| 2025-01-20 | 60% confidence → 3 hour waste | Rule created |
| 2025-01-22 | 90% confidence → success | Threshold validated |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-26 | Initial version | Extracted from state machine |