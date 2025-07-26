# Planning Time Limit Rule

## Rule Metadata
- **Version**: 1.0
- **Type**: global
- **Severity**: medium
- **Enforcement**: manual (timer-based)
- **Status**: active
- **Last Updated**: 2025-01-26
- **Confidence**: 70% (common practice, needs fine-tuning)


## Performance Metrics
- **Times Applied**: 0
- **Success Rate**: N/A
- **Last Applied**: Never
- **Average Time Impact**: Unknown

## Purpose
Prevents analysis paralysis by limiting planning/documentation time before implementation must begin. Forces learning through doing rather than endless planning.

## Rule Statement
**WHEN** planning or documenting before implementation
**THEN** must start coding within 15 minutes
**ELSE** you're avoiding implementation through over-planning

## Scope
- **Applies To**: All new feature/task starts
- **Exceptions**: 
  - First time with new database type (+10 min)
  - Explicit architectural design sessions
  - User-requested detailed planning

## Detailed Specification

### Trigger Conditions
- [ ] Starting new task/feature
- [ ] Reading documentation
- [ ] Writing plans/designs
- [ ] No code written yet

### Time Limits by Activity

| Activity | Time Limit | Then Must |
|----------|------------|-----------|
| Reading requirements | 5 min | Start planning |
| Planning approach | 10 min | Start coding |
| Reading patterns | 10 min | Try implementing |
| Writing design doc | 15 min | Write first code |

### Required Actions

#### At 15 Minutes
1. **Stop** all planning activities
2. **Start** writing actual code
3. **Document** uncertainties as TODOs
4. **Implement** even if approach unclear

#### If Need More Planning
1. **Code first attempt** (15-30 min)
2. **Learn** what's actually needed
3. **Then** plan based on real needs
4. **Iterate** with knowledge

### Examples

#### ✅ Correct Application
```
Situation: Implementing CreateSchemaChange
0-5 min: Read requirements
5-10 min: Check existing patterns
10-15 min: Sketch approach
15 min: START CODING
Result: Discovered real issues in 20 min, adjusted approach
```

#### ❌ Violation Example
```
Situation: Implementing complex validation
0-30 min: Reading all validation docs
30-45 min: Designing "perfect" approach
45 min: Still planning...
Result: No code written, no real learning
Should have: Started coding at 15 min
```

## Planning vs Doing Balance

```
Optimal Balance:
├── Planning: 15-20% of time
├── Implementation: 60-70% of time  
└── Testing/Review: 15-20% of time

Anti-pattern:
├── Planning: 40%+ (TOO MUCH)
├── Implementation: 40%
└── Testing: 20%
```

## Override Conditions

### When Extra Planning Allowed
1. **Architectural Decision**: Affects multiple components
2. **New Technology**: Never used before
3. **Critical Path**: Failure has high cost
4. **User Request**: "Please plan this thoroughly"

### How to Override
1. **Document** why extra time needed
2. **Set** new time limit (max 30 min)
3. **Commit** to start time
4. **Start** coding at limit

## Metrics
- **Current Confidence**: 70% (common practice, needs fine-tuning)
- **Success Metric**: Faster time to first working code
- **Failure Metric**: Over-planning leading to rework

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
- Rules: RESEARCH_TIME_LIMIT (similar for research)
- Anti-patterns: Analysis Paralysis

## Confidence Evolution
| Date | Event | Old Conf | New Conf | Evidence |
|------|-------|----------|----------|----------|
| 2025-01-26 | Created | 0% | 50% | New rule from LBCF |
| 2025-01-26 | Initial validation | 50% | 70% | Rule created from LBCF |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-26 | Initial version | Prevent analysis paralysis |