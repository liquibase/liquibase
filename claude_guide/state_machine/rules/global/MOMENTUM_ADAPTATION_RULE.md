# Momentum Adaptation Rule

## Rule Metadata
- **Version**: 1.0
- **Type**: global
- **Severity**: medium
- **Enforcement**: automatic (system-level)
- **Status**: active
- **Last Updated**: 2025-01-26
- **Validated Through**: To be validated (starting at 50% confidence)

## Purpose
Dynamically adapts system support level based on current development momentum. When things are going well, reduces friction. When struggling, increases support.

## Rule Statement
**WHEN** momentum score changes significantly
**THEN** system MUST adapt support levels:
- High momentum (>80%): Reduce friction, extend limits
- Steady momentum (50-80%): Maintain normal support
- Low momentum (<50%): Increase support, reduce limits
- Crisis momentum (<30%): Recovery mode with maximum support
**ELSE** system becomes either annoying (too much help) or abandoning (too little help)

## Scope
- **Applies To**: All development activities
- **Exceptions**: Emergency debugging (always max support)

## Detailed Specification

### Momentum Calculation
```
Momentum Score = (Recent Successes × Confidence Growth × Speed) / Time Window

Where:
- Recent Successes = Last 5 tasks completion rate (0-100%)
- Confidence Growth = Average confidence increase per task
- Speed = Actual time vs estimated time ratio
- Time Window = Last 2 hours of work
```

### Momentum Levels and Actions

#### 🚀 HIGH MOMENTUM (>80%)
**Indicators**: Everything clicking, flow state active
**System Actions**:
- Reduce safeguard sensitivity by 25%
- Extend time limits (+10 minutes)
- Auto-suggest advanced patterns only
- Enable "flow state" protection
- Minimize interruptions

#### ⚖️ STEADY STATE (50-80%)
**Indicators**: Normal progress, occasional hiccups
**System Actions**:
- Standard safeguard settings
- Normal time limits apply
- Regular pattern suggestions
- Maintain current support level
- Balance help vs independence

#### 🆘 STRUGGLING (<50%)
**Indicators**: Multiple failures, slow progress
**System Actions**:
- Increase safeguard sensitivity
- Reduce time limits (-5 minutes)
- Add extra validation steps
- Suggest learning/research focus
- Proactive help offerings

#### 🛑 CRISIS MODE (<30%)
**Indicators**: Repeated failures, no progress
**System Actions**:
- Pause complex tasks
- Focus on learning/research only
- Increase help request triggers
- Suggest break (fatigue likely)
- Escalate to human help

### Example Adaptations

#### High Momentum Example
```markdown
Current State:
- Last 5 tasks: 5/5 completed
- Confidence growth: +12% average
- Speed: 0.7x estimated time
- Momentum: 87%

Adaptations Applied:
- Analysis paralysis: 15→20 min threshold
- Context switches: 3→5 allowed
- Planning limit: 15→25 minutes
- Auto-suggestions: Advanced only
```

#### Crisis Mode Example
```markdown
Current State:
- Last 5 tasks: 1/5 completed  
- Confidence growth: -5% average
- Speed: 2.5x estimated time
- Momentum: 18%

Adaptations Applied:
- All safeguards: Maximum sensitivity
- Time limits: Reduced by 50%
- Required: Break after next task
- Suggested: Review fundamentals
- Help: Auto-requested from architect
```

## Enforcement

### Detection
- **Continuous**: Calculated every 5 minutes
- **Triggers**: Task completion/failure
- **Monitoring**: Background process

### Response
- **Immediate**: Settings adjust in real-time
- **Gradual**: Changes phase in over 2-3 tasks
- **Reversible**: Returns to baseline when momentum recovers

## Anti-Patterns to Avoid

### Over-Helping
- Don't interrupt flow state with suggestions
- Don't enforce breaks during high momentum
- Don't require documentation during sprints

### Under-Supporting  
- Don't abandon during struggles
- Don't hide help during crisis
- Don't maintain high bars when failing

## Metrics
- **Initial Confidence**: 50% (needs validation)
- **Success Metric**: Appropriate support timing
- **Value Metric**: Sustained productivity

## Related Documents
- Processes: MOMENTUM_TRACKING_PROCESS (calculation)
- Rules: CONFIDENCE_THRESHOLDS (thresholds)
- Safeguards: All safeguard sensitivities

## Learning History
| Date | Learning | Impact |
|------|----------|--------|
| 2025-01-26 | Rule created from LBCF | To be validated |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-26 | Initial version | Dynamic system adaptation |