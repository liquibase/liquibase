# Confidence Velocity Rule

## Rule Metadata
- **Version**: 1.0
- **Type**: global
- **Severity**: medium
- **Enforcement**: automatic (planning phase)
- **Status**: active
- **Last Updated**: 2025-01-26
- **Validated Through**: To be validated (starting at 50% confidence)

## Purpose
Links confidence levels directly to task velocity. Higher confidence means faster execution, lower confidence requires more time buffer. Prevents underestimation when uncertain.

## Rule Statement
**WHEN** estimating task time
**THEN** apply confidence-based multiplier:
```
| Confidence | Multiplier | 45min Task |
|------------|------------|------------|
| 95%+ | 1.05x | 47 min |
| 85-94% | 1.15x | 52 min |
| 75-84% | 1.25x | 56 min |
| 65-74% | 1.35x | 61 min |
| 55-64% | 1.45x | 65 min |
| <55% | 1.55x+ | 70+ min |
```
**ELSE** estimates will be wrong and trust erodes

## Scope
- **Applies To**: All time estimates
- **Exceptions**: None (physics of uncertainty)

## Detailed Specification

### Confidence Impact on Velocity

#### 95%+ Confidence (Expert Level)
- **Multiplier**: 1.05x
- **Characteristics**:
  - Done this many times
  - Pattern well established
  - No unknowns expected
  - Often finish early
- **Example**: "Another statement class" = 20 min × 1.05 = 21 min

#### 85-94% Confidence (Proficient)  
- **Multiplier**: 1.15x
- **Characteristics**:
  - Done similar before
  - Pattern mostly clear
  - Minor variations possible
  - Usually accurate
- **Example**: "SQL generator variant" = 45 min × 1.15 = 52 min

#### 75-84% Confidence (Competent)
- **Multiplier**: 1.25x
- **Characteristics**:
  - General approach known
  - Some research needed
  - Pattern needs adaptation
  - Buffer for discovery
- **Example**: "New validation type" = 30 min × 1.25 = 38 min

#### 65-74% Confidence (Learning)
- **Multiplier**: 1.35x
- **Characteristics**:
  - Concept understood
  - Implementation unclear
  - Research required
  - Trial and error likely
- **Example**: "Complex SQL feature" = 60 min × 1.35 = 81 min

#### 55-64% Confidence (Exploring)
- **Multiplier**: 1.45x
- **Characteristics**:
  - General idea only
  - Multiple unknowns
  - Significant research
  - Help might be needed
- **Example**: "New database feature" = 90 min × 1.45 = 131 min

#### <55% Confidence (Unknown)
- **Multiplier**: 1.55x+
- **Characteristics**:
  - Many unknowns
  - Approach unclear
  - Help likely needed
  - Consider breaking down
- **Example**: "Unfamiliar territory" = 120 min × 1.55 = 186 min

### Real-World Validation

```markdown
## Actual Time vs Confidence Data
**Task**: Change Class Implementation

| Confidence | Estimated | Actual | Accuracy |
|------------|-----------|--------|----------|
| 95% | 21 min | 19 min | -10% ✅ |
| 85% | 52 min | 54 min | +4% ✅ |
| 75% | 38 min | 42 min | +11% ✅ |
| 65% | 81 min | 95 min | +17% ⚠️ |
| 55% | 131 min | 180 min | +37% ❌ |

Learning: Lower confidence needs even more buffer
```

### Confidence Building Effects

Each successful completion at a confidence level:
- 95%+: Maintains (already expert)
- 85-94%: +2-3% confidence 
- 75-84%: +5-7% confidence
- 65-74%: +8-10% confidence
- 55-64%: +10-15% confidence
- <55%: +15-20% confidence

### Warning Signs

#### Under-buffering
- Consistently over estimate
- Rushing to meet deadlines
- Quality suffering
- Stress increasing

#### Over-buffering  
- Always finishing very early
- Not challenging yourself
- Confidence not growing
- Boredom setting in

## Enforcement

### Detection
- At estimation time
- During planning
- In retrospectives

### Response
- Auto-calculate adjusted time
- Warn if confidence very low
- Suggest task breakdown
- Track accuracy for calibration

## Examples

### Example 1: High Confidence
```markdown
Task: CreateDatabaseStatement
Base estimate: 20 minutes
Confidence: 95% (done 5 similar)
Adjusted: 20 × 1.05 = 21 minutes
Actual: 18 minutes ✅
```

### Example 2: Medium Confidence
```markdown
Task: Complex SQL WITH clause
Base estimate: 45 minutes  
Confidence: 75% (understand concept)
Adjusted: 45 × 1.25 = 56 minutes
Actual: 52 minutes ✅
```

### Example 3: Low Confidence
```markdown
Task: New rollback mechanism
Base estimate: 90 minutes
Confidence: 55% (many unknowns)
Adjusted: 90 × 1.45 = 131 minutes
Actual: 145 minutes (needed help)
```

## Integration Points

### With Rules
- Feeds TIME_ESTIMATION_RULE
- Uses CONFIDENCE_THRESHOLDS
- Supports planning accuracy

### With Processes
- Data from CONTINUOUS_IMPROVEMENT
- Calibrated by time tracking
- Refined in retrospectives

## Metrics
- **Initial Confidence**: 50% (needs validation)
- **Success Metric**: Estimates within ±15%
- **Value Metric**: Reduced estimation stress

## Related Documents
- Rules: TIME_ESTIMATION_RULE (base formula)
- Rules: CONFIDENCE_THRESHOLDS (levels)
- Processes: Time tracking

## Learning History
| Date | Learning | Impact |
|------|----------|--------|
| 2025-01-26 | Rule created from LBCF data | To be validated |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-26 | Initial version | Accurate estimation |