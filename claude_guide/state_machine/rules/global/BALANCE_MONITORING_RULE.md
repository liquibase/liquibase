# Balance Monitoring Rule

## Rule Metadata
- **Version**: 1.0
- **Type**: global
- **Severity**: medium
- **Enforcement**: automatic (system monitoring)
- **Status**: active
- **Last Updated**: 2025-01-26
- **Validated Through**: To be validated (starting at 50% confidence)

## Purpose
Prevents over-optimization in any single area by monitoring balance across key development dimensions. Ensures sustainable, well-rounded progress.

## Rule Statement
**WHEN** any development dimension exceeds healthy balance limits
**THEN** system MUST auto-correct:
- Too much documentation → Force coding start
- Too much learning → Force implementation
- Too much planning → Reduce planning limits
- Too much support → Increase independence
**ELSE** development becomes lopsided and unsustainable

## Scope
- **Applies To**: All development activities
- **Exceptions**: Learning sprints (temporary imbalance OK)

## Detailed Specification

### Balance Dimensions and Targets

#### Documentation vs Doing
- **Target**: 20% documenting / 80% doing
- **Warning**: <15% or >30% documentation
- **Critical**: <10% or >40% documentation

#### Learning vs Executing  
- **Target**: 30% learning / 70% executing
- **Warning**: <20% or >40% learning
- **Critical**: <10% or >50% learning

#### Planning vs Implementing
- **Target**: 15% planning / 85% implementing
- **Warning**: <10% or >25% planning
- **Critical**: <5% or >35% planning

#### Quality vs Speed
- **Target**: High quality with good speed
- **Warning**: Quality drops OR speed <50% normal
- **Critical**: Multiple quality issues OR speed <30%

#### Support vs Independence
- **Target**: <25% tasks need help
- **Warning**: >35% need help
- **Critical**: >50% need help

### Auto-Balancing Actions

#### Too Much Documentation
```markdown
Detected: Documentation time >30%
Actions:
1. Alert: "Start coding - enough documentation!"
2. Block: New documentation tasks
3. Suggest: Implementation tasks only
4. Timer: 5-minute doc limit per hour
```

#### Too Much Learning
```markdown
Detected: Learning time >40%
Actions:
1. Alert: "Time to apply what you've learned!"
2. Force: Implementation attempt required
3. Block: New research tasks
4. Challenge: "Prove the learning with code"
```

#### Too Much Planning
```markdown
Detected: Planning time >25%
Actions:
1. Alert: "Analysis paralysis detected!"
2. Reduce: Planning limit to 10 minutes
3. Force: Start with simplest approach
4. Remind: "Plans change, code teaches"
```

#### Too Much Support
```markdown
Detected: >35% tasks need help
Actions:
1. Alert: "Building independence muscles"
2. Delay: Help triggers by 5 minutes
3. Suggest: Try simpler tasks first
4. Encourage: "You've got this!"
```

### Balance Dashboard Format
```markdown
## System Health Dashboard
**Documentation vs Doing**: 15% / 85% ✅ (target: 20/80)
**Learning vs Executing**: 25% / 75% ✅ (target: 30/70)  
**Planning vs Implementing**: 10% / 90% ⚠️ (target: 15/85)
**Quality vs Speed**: High quality, good speed ✅
**Support vs Independence**: 20% helped ✅ (target: <25%)

⚠️ Planning time too low - may miss edge cases
```

## Enforcement

### Detection
- **Continuous**: Rolling 2-hour window
- **Calculation**: Every 10 minutes
- **Alerting**: When approaching limits

### Response
- **Gentle**: Suggestions at warning level
- **Firm**: Blocks at critical level
- **Smart**: Considers current task context

## Examples

### Good Balance
```
Last 2 hours:
- Coding: 75 minutes (63%)
- Documentation: 20 minutes (17%)
- Learning: 15 minutes (12%)
- Planning: 10 minutes (8%)
Status: ✅ Well balanced!
```

### Imbalanced (Too Much Learning)
```
Last 2 hours:
- Learning: 65 minutes (54%) ⚠️
- Coding: 35 minutes (29%)
- Documentation: 15 minutes (13%)
- Planning: 5 minutes (4%)
Action: Blocking research, forcing implementation
```

## Metrics
- **Initial Confidence**: 50% (needs validation)
- **Success Metric**: Sustained balance >80% of time
- **Value Metric**: Consistent daily progress

## Related Documents
- Rules: TIME_ESTIMATION_RULE (planning limits)
- Rules: RESEARCH_TIME_LIMIT (learning limits)
- Processes: Development tracking

## Learning History
| Date | Learning | Impact |
|------|----------|--------|
| 2025-01-26 | Rule created from LBCF | To be validated |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-26 | Initial version | Prevent over-optimization |