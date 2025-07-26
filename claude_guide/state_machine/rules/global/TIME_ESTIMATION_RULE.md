# Time Estimation Rule

## Rule Metadata
- **Version**: 1.0
- **Type**: global
- **Severity**: medium
- **Enforcement**: manual
- **Status**: active
- **Last Updated**: 2025-01-26
- **Validated Through**: To be validated (starting at 50% confidence)

## Purpose
Provides realistic time estimates by adjusting base estimates according to confidence level. Higher confidence requires less buffer, lower confidence requires more buffer.

## Rule Statement
**WHEN** estimating time for any task
**THEN** use formula: `Adjusted Time = Base Time × (2 - Confidence/100)`
**ELSE** estimates will be consistently wrong

## Scope
- **Applies To**: All time estimates for tasks
- **Exceptions**: Emergency fixes (document actual time after)

## Detailed Specification

### The Formula
```
Adjusted Time = Base Time × (2 - Confidence/100)

Where:
- Base Time = How long it would take at 100% confidence
- Confidence = Your confidence score (0-100%)
- Result = Realistic time estimate with buffer
```

### Examples by Confidence

| Confidence | Multiplier | 60 min base | Actual Estimate |
|------------|------------|-------------|-----------------|
| 95% | ×1.05 | 60 min | 63 min |
| 90% | ×1.10 | 60 min | 66 min |
| 80% | ×1.20 | 60 min | 72 min |
| 70% | ×1.30 | 60 min | 78 min |
| 60% | ×1.40 | 60 min | 84 min |
| 50% | ×1.50 | 60 min | 90 min |
| 40% | ×1.60 | 60 min | 96 min |
| 30% | ×1.70 | 60 min | 102 min |

### When to Apply

1. **Project Planning**: Initial estimates
2. **Status Updates**: Revised estimates
3. **Sprint Planning**: Task sizing
4. **Help Requests**: Time needed

### Examples

#### ✅ Correct Application
```
Task: Implement CreateDatabaseChange
Base estimate: 45 minutes (if I knew exactly what to do)
Confidence: 75% (done similar, but not exact)
Calculation: 45 × (2 - 0.75) = 45 × 1.25 = 56 minutes
Result: Completed in 52 minutes (estimate was good!)
```

#### ❌ Wrong Application
```
Task: Complex SQL generation
Base estimate: 30 minutes
Confidence: 60% (lots of unknowns)
Used: 30 minutes (ignored formula)
Result: Took 65 minutes (2x over!)
Should have: 30 × 1.4 = 42 minutes minimum
```

## Special Cases

### First-Time Tasks
- Confidence usually 40-60%
- Use higher multiplier (1.4-1.6×)
- Add explicit research time

### Well-Known Patterns
- Confidence usually 85-95%
- Minimal buffer needed (1.05-1.15×)
- Watch for overconfidence

### After Failed Attempts
- Reduce confidence by 15-20%
- Recalculate with new confidence
- Include learning time

## Metrics
- **Initial Confidence**: 50% (needs validation)
- **Accuracy Goal**: Within 20% of actual
- **Tracking**: Compare estimated vs actual

## Related Documents
- Rules: CONFIDENCE_THRESHOLDS (confidence scoring)
- Processes: Planning, Status Updates

## Learning History
| Date | Learning | Impact |
|------|----------|--------|
| 2025-01-26 | Rule created from LBCF | To be validated |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-26 | Initial version | Extracted from LBCF |