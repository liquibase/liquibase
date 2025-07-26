# Liquibase Extension Development Process Cycle

## Cycle Overview
Each development cycle follows a strict **Requirements → Implementation → Test → Retrospective → Document** pattern.

## Phase 1: Requirements Analysis (STOP/GO Gate)
### Inputs Required:
- [ ] Clear user story or technical requirement
- [ ] Acceptance criteria (specific, measurable)
- [ ] Definition of Done
- [ ] Time box estimate
- [ ] Success metrics

### Outputs Required:
- [ ] Requirements document with acceptance criteria
- [ ] Implementation plan with specific tasks
- [ ] Test strategy
- [ ] Risk assessment
- [ ] **GO/NO-GO decision point**

### Template:
```
## Requirement: [Title]
**User Story**: As a [user], I want [goal] so that [benefit]

**Acceptance Criteria**:
1. [ ] Specific criteria 1
2. [ ] Specific criteria 2
3. [ ] Specific criteria 3

**Definition of Done**:
- [ ] All acceptance criteria met
- [ ] All tests passing
- [ ] Code follows established patterns
- [ ] Documentation updated

**Time Box**: [X hours/days]
**Success Metrics**: [How we measure success]
```

## Phase 2: Implementation (Disciplined Execution)
### Rules:
- Follow documented patterns EXACTLY
- No deviation without explicit user approval
- Complete one task before starting next
- Update progress continuously

### Mandatory Checks:
- [ ] Following established patterns
- [ ] Code matches requirements exactly
- [ ] No scope creep
- [ ] Regular progress updates

## Phase 3: Test Complete (Quality Gate)
### Requirements:
- [ ] ALL tests passing
- [ ] Test coverage complete
- [ ] Manual verification done
- [ ] Performance acceptable

### Definition of Test Complete:
- Zero failing tests
- All acceptance criteria verified
- No known defects
- Performance within bounds

**NO PROCEEDING TO NEXT PHASE WITHOUT TEST COMPLETE**

## Phase 4: Retrospective (Learning Gate)
### Required Analysis:
1. **What Worked Well**:
   - Patterns that succeeded
   - Decisions that paid off
   - Process improvements

2. **What Didn't Work**:
   - Obstacles encountered
   - Time wasters
   - Process failures

3. **Lessons Learned**:
   - Technical insights
   - Process improvements
   - Pattern refinements

4. **Metrics**:
   - Time estimates vs. actual
   - Defect count
   - Efficiency measures

### Template:
```
## Retrospective: [Cycle Name]
**Duration**: [Planned] vs [Actual]
**Success Rate**: [X/Y criteria met]

### What Worked Well:
1. [Specific success]
2. [Specific success]

### What Didn't Work:
1. [Specific failure] - Root Cause: [Analysis]
2. [Specific failure] - Root Cause: [Analysis]

### Lessons Learned:
1. [Insight] → [Action for next cycle]
2. [Insight] → [Action for next cycle]

### Pattern Updates Needed:
- [ ] [Specific pattern update]
- [ ] [Specific pattern update]
```

## Phase 5: Document Update (Knowledge Capture)
### Required Updates:
- [ ] Pattern library updates
- [ ] Process improvements documented
- [ ] Lessons learned captured
- [ ] Templates updated
- [ ] Next cycle preparation

### Knowledge Base Maintenance:
- Update successful patterns
- Document anti-patterns discovered
- Refine templates based on experience
- Prepare for next cycle

## Time Tracking Methodology

### Time Categories:
1. **Active Claude Time**: Actually working on tasks
2. **User Collaboration Time**: Waiting for/receiving guidance
3. **Context Switching Time**: Between tasks/cycles
4. **Rework Time**: Fixing mistakes/changes

### Tracking Format:
```
## Time Log: [Cycle Name]
**Start**: [Timestamp]
**End**: [Timestamp]

| Phase | Planned | Actual | Efficiency | Notes |
|-------|---------|--------|------------|-------|
| Requirements | 15min | 20min | 75% | Needed clarification |
| Implementation | 60min | 45min | 133% | Pattern worked well |
| Testing | 30min | 60min | 50% | Validation issues |
| Retrospective | 15min | 10min | 150% | Clear outcomes |
| Documentation | 10min | 15min | 67% | More updates needed |

**Total Planned**: 2h 10min
**Total Actual**: 2h 30min
**Overall Efficiency**: 87%

**User Collaboration**: 45min (30% of total time)
**Rework Time**: 20min (13% of total time)
```

## Process Discipline Rules

### NEVER Skip:
- Requirements analysis
- Test complete verification
- Retrospective
- Documentation updates

### ALWAYS Require:
- Clear acceptance criteria
- Explicit user approval for changes
- Complete test validation
- Learning capture

### RED FLAGS:
- "Quick fix" mentality
- Skipping retrospective
- Moving to next cycle with failing tests
- Not updating documentation

## Mobile Response Protocol

### High-Priority Items (Mobile Response Needed):
- Strategic direction changes
- Pattern violations discovered
- Requirements clarification
- Go/no-go decisions

### Response Template:
```
MOBILE RESPONSE NEEDED
Context: [Brief context]
Decision Needed: [Specific decision]
Options: [A/B/C choices]
Impact: [What happens if delayed]
```

### Low-Priority Items (Can Wait):
- Implementation details
- Technical troubleshooting
- Documentation updates
- Routine progress updates

This process template creates accountability, learning, and continuous improvement while maintaining development velocity.