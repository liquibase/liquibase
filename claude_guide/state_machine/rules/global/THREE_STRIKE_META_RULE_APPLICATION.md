# THREE_STRIKE_META_RULE Application Guide

## Overview
This document clarifies which processes and loops the THREE_STRIKE_META_RULE should govern to prevent infinite loops and wasted time.

## Processes That SHOULD Be Limited (3 Strikes)

### 1. Problem-Solving Loops
**Why**: Prevents endless debugging without understanding
- ITERATION_WITHOUT_PROGRESS cycles ✅ (already documented)
- Debug-fix-test loops
- Trial-and-error implementation attempts
- Assumption validation loops

### 2. Research-Implement Cycles  
**Why**: Forces concrete action after reasonable research
- Research → Try → Fail → Research loops
- Documentation hunting without implementation
- Example searching without trying

### 3. Pattern Matching Attempts
**Why**: If 3 patterns don't work, need human insight
- Try pattern A → Fail
- Try pattern B → Fail
- Try pattern C → Fail → STOP, get help

### 4. Validation-Correction Loops
**Why**: Repeated validation failures indicate deeper issue
- Validate → Fix → Validate → Fix loops
- Test → Patch → Test → Patch cycles
- Check → Adjust → Check → Adjust patterns

### 5. Planning-Replanning Cycles
**Why**: Analysis paralysis prevention
- Plan → Find issue → Replan → Find issue loops
- Design → Redesign → Redesign patterns

### 6. Help Request Escalation
**Why**: If 3 people can't help, need different approach
- Ask developer → Ask architect → Ask expert → STOP
- Try forum → Try docs → Try chat → STOP

## Processes That Should NOT Be Limited

### 1. SUCCESS_AMPLIFIER_PROCESS
**Why**: Success should be captured every time
- No limit on celebrating wins
- No limit on pattern extraction from success

### 2. CELEBRATION_PROTOCOL_PROCESS
**Why**: Recognition builds culture
- Celebrate all wins, not just first 3
- No artificial limits on positive reinforcement

### 3. CONTINUOUS_IMPROVEMENT_PROCESS
**Why**: Ongoing measurement and learning
- Time tracking is continuous
- Learning extraction is unlimited

### 4. Normal Development Cycles
**Why**: Natural workflow shouldn't be interrupted
- DEVELOPMENT_CYCLE (5 phases) completes naturally
- Not a retry loop, but a workflow

### 5. Monitoring and Measurement
**Why**: Passive observation, not active attempts
- BALANCE_MONITORING_RULE observations
- MOMENTUM_ADAPTATION measurements
- Time tracking activities

## Special Cases

### 1. MISTAKE_TO_PATTERN_PROCESS
**Limit**: Only the "trying to create pattern" part
- If can't extract pattern after 3 attempts, get help
- But no limit on how many mistakes can be transformed

### 2. PATTERN_MATCHING_PROCESS  
**Limit**: Only the "applying suggested patterns" part
- Try top 3 pattern suggestions
- If none work, get human help
- But no limit on pattern searches

### 3. Emergency Debugging
**Exception**: Critical production issues
- May need >3 attempts if system is down
- But should parallelize with getting help

## Implementation Guidelines

### How to Apply the Rule

```markdown
FOR processes with retry/loop potential:
  IF attempt_count >= 3 AND no_meaningful_progress THEN
    TRIGGER: THREE_STRIKE_META_RULE
    ACTION: Stop attempts, seek help
    REASON: Prevent infinite loops
```

### What Counts as a "Strike"

1. **Full Cycle Completion**: Not individual steps
   - Wrong: Each line of code change
   - Right: Complete attempt at solution

2. **Meaningful Attempt**: Real effort, not trivial
   - Wrong: Fixing typo
   - Right: Different approach to problem

3. **Same Problem Context**: Related to same issue
   - Wrong: 3 strikes across different features
   - Right: 3 strikes on same bug/feature

## Rationale

### Why These Distinctions Matter

1. **Active vs Passive**: Limit active problem-solving, not passive observation
2. **Negative vs Positive**: Limit failure loops, not success capture
3. **Retry vs Workflow**: Limit retries, not natural progressions
4. **Learning vs Doing**: Limit doing without learning, not learning itself

### The Core Principle

The THREE_STRIKE_META_RULE exists to:
- Prevent wasting time on wrong approaches
- Force seeking help when stuck
- Maintain forward momentum
- Prevent frustration and burnout

It should NOT:
- Limit positive activities
- Interrupt natural workflows
- Prevent continuous improvement
- Stop celebration and recognition

## Summary

**Apply THREE_STRIKE to**:
- Problem-solving loops
- Debug cycles
- Research-implement loops
- Pattern application attempts
- Any "try again" scenarios

**Don't apply to**:
- Success processes
- Celebration activities
- Continuous measurements
- Natural workflows
- Positive reinforcement

The rule is a safety net for getting stuck, not a limitation on progress.