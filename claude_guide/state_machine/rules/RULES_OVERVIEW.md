# System Rules Overview

This document provides a quick reference to all rules in our development system.

## Global Rules (Apply Everywhere)

### Meta Rules (Govern Other Rules)
1. **[THREE_STRIKE_META_RULE](global/THREE_STRIKE_META_RULE.md)** (v3.0, active, validated)
   - Limits ANY process to 3 cycles before requiring help
   - The ultimate "circuit breaker" preventing infinite loops

### Execution Rules
2. **[ITERATION_WITHOUT_PROGRESS_RULE](global/ITERATION_WITHOUT_PROGRESS_RULE.md)** (v1.0, active, 50% confidence)
   - After 3 iterations with no progress, validate assumptions
   - Research and plan based on findings
   - Limited to 3 cycles by THREE_STRIKE_META_RULE

### Time Management Rules
3. **[TIME_ESTIMATION_RULE](global/TIME_ESTIMATION_RULE.md)** (v1.0, active, 50% confidence)
   - Formula: `Adjusted Time = Base Time × (2 - Confidence/100)`
   - Higher confidence = less buffer needed

4. **[PLANNING_TIME_LIMIT](global/PLANNING_TIME_LIMIT.md)** (v1.0, active, 50% confidence)
   - Maximum 15 minutes planning before starting implementation
   - Prevents analysis paralysis

5. **[RESEARCH_TIME_LIMIT](global/RESEARCH_TIME_LIMIT.md)** (v1.0, active, 50% confidence)
   - Maximum 30 minutes research before trying implementation
   - Forces learning by doing

### Workflow Rules
6. **[CONTEXT_SWITCH_LIMIT](global/CONTEXT_SWITCH_LIMIT.md)** (v1.0, active, 50% confidence)
   - Maximum 3 role switches per 10 minutes
   - Prevents context thrashing

7. **[CONFIDENCE_THRESHOLDS](global/CONFIDENCE_THRESHOLDS.md)** (v1.0, active, validated)
   - <70% confidence requires help
   - 70-84% proceed with caution
   - ≥85% proceed normally

## State-Specific Rules

Currently, state-specific rules are embedded in `development_machine.yaml`. These include:
- Allowed/forbidden actions per state
- Consent requirements
- Transition conditions

## Accountability Rules

Located in `/accountability/`:
- **Explicit Consent Rule**: All file changes require consent
- **Audit All Actions**: Every action must be logged
- **Trust Tracking**: Violations decrease trust score

## Rule Confidence Levels

| Rule | Confidence | Status |
|------|------------|---------|
| THREE_STRIKE_META_RULE | 95% | Validated as circuit breaker |
| CONFIDENCE_THRESHOLDS | 90% | Proven in state machine |
| ITERATION_WITHOUT_PROGRESS | 50% | New, needs validation |
| TIME_ESTIMATION_RULE | 50% | New, needs validation |
| PLANNING_TIME_LIMIT | 50% | New, needs validation |
| RESEARCH_TIME_LIMIT | 50% | New, needs validation |
| CONTEXT_SWITCH_LIMIT | 50% | New, needs validation |

## How Rules Evolve

1. **Proposed**: Rule suggested at 50% confidence
2. **Tested**: Applied in real work
3. **Adjusted**: Modified based on experience
4. **Validated**: Confidence >85% through successful use
5. **Evolved**: Continuous refinement in retrospectives

See [DOCUMENT_STANDARDS_EVOLUTION](../processes/DOCUMENT_STANDARDS_EVOLUTION.md) for the meta-process of rule evolution.