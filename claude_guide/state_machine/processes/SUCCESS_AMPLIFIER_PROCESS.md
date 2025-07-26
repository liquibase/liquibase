# Success Amplifier Process

## Process Metadata
- **Version**: 1.0
- **Status**: active
- **Scope**: global (all successful completions)
- **Owner**: scrum_master
- **Last Updated**: 2025-01-26
- **Validated Through**: To be validated (starting at 50% confidence)

## Purpose
Captures and amplifies success patterns to accelerate future development. Every success contains valuable patterns that can save time and increase quality in future work.

## Process Diagram
```mermaid
graph LR
    Start([Success Detected]) --> Log[Log the Win]
    Log --> Extract[Extract Pattern]
    Extract --> Boost[Boost Confidence]
    Boost --> Share[Share Learning]
    Share --> Update[Update Milestones]
    Update --> Reinforce[Reinforce Behavior]
    Reinforce --> End([Pattern Captured])
```

## Success Triggers
- [ ] Task completed UNDER time estimate
- [ ] First-try success (no rework needed)
- [ ] New reusable pattern discovered
- [ ] Confidence score ≥ 90% achieved
- [ ] Zero help requests needed
- [ ] Tests pass on first run

## Process Steps

### Step 1: Log the Win
- **Actor**: system (automatic)
- **Time**: < 1 minute
- **Action**: Record success prominently
- **Format**:
  ```markdown
  ## 🎊 SUCCESS DETECTED! - [Timestamp]
  **Task**: [What was accomplished]
  **Achievement**: Completed in [actual] min (estimated [estimate] min)
  **Success Type**: [Under Time / First Try / etc.]
  ```
- **Output**: Success record

### Step 2: Extract Success Pattern
- **Actor**: developer + technical_writer
- **Time**: 5-10 minutes
- **Action**: Identify reusable pattern
- **Document**:
  ```markdown
  ## 🧬 SUCCESS PATTERN EXTRACTED
  **Pattern Name**: [Descriptive name]
  **Reusability Score**: [0-100%]
  **Time Savings**: [Average minutes saved]
  **Success Rate**: [% when followed]
  **Template Created**: [Yes/No]
  ```
- **Output**: Pattern documentation

### Step 3: Boost Confidence Scores
- **Actor**: system
- **Time**: Immediate
- **Action**: Update related confidence metrics
- **Boost Amount**:
  - First-try success: +10-15%
  - Under time estimate: +15-20%
  - Pattern discovered: +20%
- **Output**: Updated confidence scores

### Step 4: Share Learning
- **Actor**: scrum_master
- **Time**: 5 minutes
- **Action**: Distribute pattern across team
- **Channels**:
  - Team dashboard
  - Pattern library
  - Role-specific docs
  - CLAUDE.md updates
- **Output**: Team awareness

### Step 5: Update Milestones
- **Actor**: product_owner
- **Time**: 2 minutes
- **Action**: Record progress
- **Updates**:
  - Badge progress
  - Achievement tracking
  - Velocity metrics
  - Quality indicators
- **Output**: Updated milestones

### Step 6: Reinforce Behavior
- **Actor**: all roles
- **Time**: Ongoing
- **Action**: Apply pattern in future work
- **Methods**:
  - Use pattern in similar tasks
  - Reference in planning
  - Include in estimates
  - Add to checklists
- **Output**: Behavior change

## Win Categories

### 🏆 EPIC WIN (Rare)
- Entire feature under estimate
- Zero rework needed
- New pattern saves others time
- **Action**: Big celebration + documentation

### 🎉 SOLID WIN (Common)
- Task completed first try
- Time estimate accurate
- No help needed
- **Action**: Recognition + reinforcement

### ✨ SMALL WIN (Daily)
- Progress without getting stuck
- Learned something new
- Improved efficiency
- **Action**: Quick acknowledgment

## Integration Points

### With Rules
- Updates CONFIDENCE_THRESHOLDS
- Feeds TIME_ESTIMATION_RULE improvements
- Validates process effectiveness

### With Other Processes
- Opposite of FAILURE_ANALYSIS_PROCESS
- Feeds DEVELOPMENT_CYCLE improvements
- Updates pattern libraries

## Metrics
- **Initial Confidence**: 50% (needs validation)
- **Success Metric**: Pattern reuse rate
- **Value Metric**: Time saved by patterns

## Related Documents
- Processes: FAILURE_ANALYSIS_PROCESS (opposite)
- Rules: CONFIDENCE_THRESHOLDS (updates)
- Patterns: Pattern library (output destination)

## Learning History
| Date | Learning | Impact |
|------|----------|--------|
| 2025-01-26 | Process created from LBCF | To be validated |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-26 | Initial version | Capture and amplify wins |