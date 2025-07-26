# Time Tracking Methodology for Claude Code Development

## Purpose
Track Claude active time vs. elapsed time to identify collaboration bottlenecks and improve development velocity.

## Time Categories

### 1. Active Claude Time (Pure Development)
**Definition**: Time spent actively working on code, analysis, or documentation
**Examples**:
- Writing code
- Running tests
- Analyzing errors
- Reading documentation
- Writing documentation

### 2. User Collaboration Time (High-Value Waiting)
**Definition**: Time waiting for or receiving strategic guidance
**Subcategories**:
- **Strategic Input**: Architecture decisions, pattern guidance
- **Requirements Clarification**: Understanding what to build
- **Course Correction**: Fixing wrong direction
- **Approval/Review**: Getting go/no-go decisions

### 3. Context Switching Time (Process Overhead)
**Definition**: Time lost switching between tasks or approaches
**Examples**:
- Rebuilding after direction changes
- Re-reading context after interruption
- Switching between different implementation approaches

### 4. Rework Time (Quality Issues)
**Definition**: Time spent fixing mistakes or redoing work
**Examples**:
- Fixing bugs introduced
- Correcting misunderstood requirements
- Undoing wrong architectural choices

## Tracking Template

### Per-Session Tracking:
```markdown
## Session: [Date/Time]
**Total Elapsed**: [Start] - [End] = [Duration]

### Time Breakdown:
| Category | Duration | % of Total | Efficiency Notes |
|----------|----------|------------|------------------|
| Active Claude | Xh Ym | Z% | [What went well/poorly] |
| User Collaboration | Xh Ym | Z% | [Type: Strategic/Requirements/etc.] |
| Context Switching | Xh Ym | Z% | [Cause of switches] |
| Rework Time | Xh Ym | Z% | [What required rework] |

### Collaboration Analysis:
| Wait Type | Duration | Impact | Mobile Suitable? |
|-----------|----------|---------|------------------|
| [Strategic guidance] | Xmin | High | Yes |
| [Requirements clarification] | Xmin | Medium | Yes |
| [Technical review] | Xmin | Low | No |

### Efficiency Metrics:
- **Development Velocity**: Active Time / Total Time = X%
- **Collaboration Efficiency**: High-Value Waits / Total Waits = X%
- **Quality Rate**: (Total - Rework) / Total = X%
```

### Per-Cycle Tracking:
```markdown
## Cycle: [Cycle Name]
**Requirement**: [Brief description]
**Planned Duration**: [Estimate]
**Actual Duration**: [Reality]

### Phase Breakdown:
| Phase | Planned | Actual | Efficiency | Quality Issues |
|-------|---------|--------|------------|----------------|
| Requirements | Xmin | Ymin | Z% | [Issues encountered] |
| Implementation | Xmin | Ymin | Z% | [Rework needed] |
| Testing | Xmin | Ymin | Z% | [Test failures] |
| Retrospective | Xmin | Ymin | Z% | [Lessons missed] |

### Bottleneck Analysis:
- **Biggest Time Sink**: [Phase/Activity]
- **Root Cause**: [Why it took longer]
- **Prevention Strategy**: [How to avoid next time]
```

## Mobile Response Protocol

### High-Priority (Mobile Response Recommended):
**Characteristics**:
- Strategic direction changes
- Requirements clarification affecting > 30min of work
- Go/no-go decisions
- Pattern violations with architectural impact

**Mobile Response Template**:
```
🚨 MOBILE RESPONSE NEEDED
Context: [1 sentence]
Decision: [Specific A/B/C choice]
Impact: [Time cost if delayed]
Urgency: [High/Medium based on time impact]
```

### Medium-Priority (Can Wait for Full Session):
**Characteristics**:
- Technical implementation details
- Code review feedback
- Documentation questions
- Process clarifications

### Low-Priority (Async Documentation):
**Characteristics**:
- Status updates
- Completed work summaries
- Retrospective findings
- Process observations

## Efficiency Improvement Targets

### Current Baseline (From Sequence Cycle):
- **Development Velocity**: 50% (Active Time / Total Time)
- **Collaboration Bottleneck**: 37.5% of total time
- **Quality Rate**: 87.5% (12.5% rework)

### Target Improvements:
- **Development Velocity**: 70% (reduce collaboration wait time)
- **Collaboration Efficiency**: Mobile response for 80% of strategic decisions
- **Quality Rate**: 90% (reduce rework through better requirements)

## Implementation

### Start of Each Session:
1. Note start time
2. Set development cycle objectives
3. Identify likely collaboration points
4. Pre-flag mobile-suitable questions

### During Session:
1. Log time category switches in real-time
2. Note collaboration wait start/end times
3. Track context switches and causes
4. Flag rework when it happens

### End of Session:
1. Complete time breakdown analysis
2. Identify improvement opportunities
3. Update efficiency metrics
4. Plan next session objectives

### Weekly Review:
1. Analyze time trends
2. Identify recurring bottlenecks
3. Update mobile response protocol
4. Adjust estimation accuracy

## Success Metrics

### Session-Level:
- Development Velocity > 60%
- High-value collaboration time > 80% of total collaboration
- Context switching < 10% of total time

### Cycle-Level:
- Actual time within 150% of planned time
- Quality rate > 85%
- Complete cycle discipline (no skipped phases)

### Project-Level:
- Trend improvement in velocity over time
- Reduced mobile response need over time (better async planning)
- Improved estimation accuracy

This methodology will help us understand where time is well-spent vs. wasted, and optimize our collaboration pattern for maximum development velocity.