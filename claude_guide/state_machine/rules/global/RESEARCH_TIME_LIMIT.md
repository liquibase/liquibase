# Research Time Limit Rule

## Rule Metadata
- **Version**: 1.0
- **Type**: global
- **Severity**: medium
- **Enforcement**: manual (timer-based)
- **Status**: active
- **Last Updated**: 2025-01-26
- **Validated Through**: To be validated (starting at 50% confidence)

## Purpose
Prevents endless research without implementation by forcing hands-on learning. Research without application rarely sticks - implementation reveals what you actually need to know.

## Rule Statement
**WHEN** researching without implementing
**THEN** must attempt implementation within 30 minutes
**ELSE** you're learning academically, not practically

## Scope
- **Applies To**: All research activities before implementation
- **Exceptions**: 
  - Explicit research tasks
  - Security/compliance research
  - Evaluating multiple technologies

## Detailed Specification

### Trigger Conditions
- [ ] Reading documentation
- [ ] Searching for examples
- [ ] Studying patterns
- [ ] No implementation attempted

### Research Time Allocation

| Research Type | Time Limit | Action Required |
|--------------|------------|-----------------|
| Quick lookup | 5 min | Try implementing |
| Pattern search | 15 min | Use best match |
| Deep dive | 30 min | Must try something |
| Not finding answers | 20 min | Ask for help |

### Required Actions

#### At 30 Minutes
1. **Stop** research activities
2. **Document** what you learned
3. **Attempt** implementation with current knowledge
4. **Mark** uncertainties with TODOs

#### Research-Implementation Cycle
```
Research (15-20 min)
    ↓
Implement attempt (30-45 min)
    ↓
Targeted research (10 min) ← Based on real problems
    ↓
Implement fix (20 min)
    ↓
Success OR Next cycle
```

### Examples

#### ✅ Correct Application
```
Situation: Learning Snowflake-specific SQL
0-15 min: Read Snowflake docs
15-20 min: Find CREATE WAREHOUSE syntax
20 min: START IMPLEMENTING
Result: Discovered real issues, targeted research helped
Total: 45 min to working code
```

#### ❌ Violation Example
```
Situation: Researching "best" testing approach
0-45 min: Reading all testing philosophy
45-90 min: Comparing frameworks
90 min: Still researching...
Result: No tests written, analysis paralysis
Should have: Written one test at 30 min
```

## Balanced Learning Approach

### Optimal Pattern
1. **Research basics** (15-20% time)
2. **Try implementing** (60-70% time)
3. **Research specific issues** (10-15% time)
4. **Refine implementation** (included above)

### Anti-Pattern
1. **Research everything** (60% time)
2. **Implement once** (30% time)
3. **Debug/fix** (10% time)
Result: Over-researched, under-practiced

## When Extended Research Justified

### Valid Reasons
1. **Technology Selection**: Choosing between options
2. **Architecture Decision**: Long-term impact
3. **Security Research**: Getting it wrong is costly
4. **Compliance Requirements**: Legal implications

### How to Handle
1. **Define** specific research goal
2. **Set** time box (max 2 hours)
3. **Document** findings as you go
4. **Decide** at time limit

## The Learning Stack

```
Most Effective:
1. Try implementing (learn by doing)
2. Hit specific problem
3. Research that problem
4. Apply solution
5. Understand deeply

Least Effective:
1. Research everything
2. Try to understand all
3. Implement once
4. Hope it works
```

## Metrics
- **Initial Confidence**: 50% (needs validation)
- **Success Metric**: Faster to working code
- **Quality Metric**: Same or better code quality

## Related Documents
- Rules: PLANNING_TIME_LIMIT (similar concept)
- Patterns: Learning by doing

## Learning History
| Date | Learning | Impact |
|------|----------|--------|
| 2025-01-26 | Rule created from LBCF | To be validated |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-26 | Initial version | Force practical learning |