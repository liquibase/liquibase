# LBCF Project Planning and Status Tracking Template

## How to Use This Template

### For AI Assistants:
1. **Create this document FIRST** when starting any database extension
2. **Update after EVERY task** - Even small updates build trust
3. **Be honest about blockers** - Transparency is crucial
4. **Time estimates are examples** - Add 3x buffer for new patterns
5. **Use the status table** as your primary communication tool

## Status Update - [Timestamp]

### Status Update Pattern:
**Completed**: [What was just finished]
**Result**: [Outcome/findings]
**Next**: [What I'm doing next]
**Blockers**: [Any issues]
**ETA**: [Updated timeline if changed]

### Why This Matters:
- **Visibility** creates confidence
- **Transparency** builds trust
- **Regular updates** show progress
- **Honest assessment** prevents surprises

## Purpose
This template ensures complete transparency and visibility into the database extension development process. By maintaining a detailed project plan and real-time status tracking, we build trust through clear communication of intentions, progress, and results.

## Template Sections:
1. **Project Overview** - Metadata and executive summary
2. **Implementation Status Tracker** - The core tracking table
3. **Current Focus** - What's happening right now
4. **Risk Register** - Known risks and mitigation
5. **Decision Log** - Key decisions and rationale
6. **Metrics Dashboard** - Team and project health

### Important Notes:
- **Confidence scores** must be based on actual attempts, not guesses
- **Time estimates** are examples only - your actual times will vary
- **Status updates** should happen after every task completion
- **Phases** follow the team Development Cycle (Requirements → Implementation → Test → Document → Retrospective)

---

# [Database Name] Extension Project Plan

**Project Start Date**: [Date]  
**Target Completion**: [Date]  
**Database Version**: [Version]  
**Liquibase Version**: 4.33.0+  
**Documentation URL**: [Vendor Docs Link]

## Executive Summary
[Brief description of what this extension will provide and its scope]

## Project Scope
[Brief description of what database objects this extension will implement]

## Implementation Status Tracker

| Stage | Component | Status | Started | Completed | Time Spent | Success/Fail | Notes | Pre-Confidence | Post-Confidence |
|-------|-----------|---------|---------|-----------|------------|--------------|-------|----------------|-----------------|
| **PHASE 1: ANALYSIS** |
| 1.1 | Documentation Analysis | 🔴 Not Started | - | - | - | - | - | 75% | - |
| 1.2 | Object Identification | 🔴 Not Started | - | - | - | - | - | - |
| 1.3 | Attribute Mapping | 🔴 Not Started | - | - | - | - | - | - |
| 1.4 | Pattern Selection | 🔴 Not Started | - | - | - | - | - | - |
| 1.5 | Test Planning | 🔴 Not Started | - | - | - | - | - | - |
| 1.6 | Implementation Plan | 🔴 Not Started | - | - | - | - | - | - |
| **PHASE 2: INFRASTRUCTURE** |
| 2.1 | Module Creation | 🔴 Not Started | - | - | | |
| 2.2 | Dependencies | 🔴 Not Started | - | - | | |
| 2.3 | Package Structure | 🔴 Not Started | - | - | | |
| 2.4 | Service Files | 🔴 Not Started | - | - | | |
| 2.5 | Build Verification | 🔴 Not Started | - | - | | |
| **PHASE 3: IMPLEMENTATION** |
| 3.1 | [Database]Database class | 🔴 Not Started | - | - | | |
| 3.2.1 | Create[Object1]Change | 🔴 Not Started | - | - | | |
| 3.2.2 | Alter[Object1]Change | 🔴 Not Started | - | - | | |
| 3.2.3 | Drop[Object1]Change | 🔴 Not Started | - | - | | |
| 3.3.1 | [Object1] Statements | 🔴 Not Started | - | - | | |
| 3.4.1 | [Object1] SQL Generators | 🔴 Not Started | - | - | | |
| 3.5 | XSD Schema | 🔴 Not Started | - | - | | |
| **PHASE 4: TESTING** |
| 4.1 | Unit Tests - Changes | 🔴 Not Started | - | - | | |
| 4.2 | Unit Tests - Generators | 🔴 Not Started | - | - | | |
| 4.3 | Integration Tests | 🔴 Not Started | - | - | | |
| 4.4 | Changelog Tests | 🔴 Not Started | - | - | | |
| 4.5 | Edge Case Tests | 🔴 Not Started | - | - | | |
| **PHASE 5: DOCUMENTATION** |
| 5.1 | User Guide | 🔴 Not Started | - | - | | |
| 5.2 | API Documentation | 🔴 Not Started | - | - | | |
| 5.3 | Examples | 🔴 Not Started | - | - | | |
| 5.4 | Release Notes | 🔴 Not Started | - | - | | |

### Status Legend
- 🔴 **Not Started** - Task not yet begun
- 🟡 **In Progress** - Currently working on this task
- 🟢 **Completed** - Task successfully completed
- 🔵 **Blocked** - Cannot proceed due to blocker
- ⚫ **Skipped** - Not applicable for this database

### Success/Fail Indicators
- ✅ **Success** - Worked first try or with minor adjustments
- ⚠️ **Partial** - Required significant rework but succeeded
- ❌ **Failed** - Multiple attempts failed, needed help
- 🔄 **Retry** - Failed first attempt, succeeded after learning

### Confidence Scoring (0-100%)
- **95-100%**: Proven pattern, execute without hesitation
- **85-94%**: High confidence, proceed normally  
- **70-84%**: Moderate, proceed with verification
- **50-69%**: Low, proceed with help available
- **25-49%**: Very low, seek help before proceeding
- **0-24%**: No confidence, don't attempt alone

### Pre-Task Confidence Assessment
Before starting each task, assess confidence based on:
- Previous successes with similar tasks
- Available documentation/patterns
- Understanding of requirements
- Knowledge of potential pitfalls

### Post-Task Confidence Update
After completing, update confidence based on:
- Did it work first try? (+10-20%)
- Required multiple attempts? (-5-15%)
- Needed help? (Cap at 70%)
- Found reusable pattern? (+15-25%)

## Detailed Implementation Plan

### Object: [Object Name] (e.g., DOMAIN, SEQUENCE, etc.)

#### Attributes to Implement
| Attribute | Type | Required | Default | Validation | Notes |
|-----------|------|----------|---------|------------|-------|
| name | String | Yes | - | Not empty | |
| dataType | String | Yes | - | Valid type | |
| ... | ... | ... | ... | ... | |

#### Test Scenarios
1. **Basic Creation** - Minimal required attributes
2. **Full Creation** - All attributes specified
3. **Validation Tests** - Invalid inputs
4. **Edge Cases** - Boundary conditions
5. **Error Handling** - Database errors

#### SQL Examples
```sql
-- Minimal
CREATE [OBJECT] simple_name ...

-- Full
CREATE [OBJECT] complex_name WITH ...

-- Error case
CREATE [OBJECT] "invalid/name" ...
```

## Current Focus
**Currently Working On**: [Specific task]
**Current Task Confidence**: [X%]
**Active Role**: [Developer/QA/PM/DevOps/SME]
**Role Effectiveness**: [X%]
**Next Up**: [Next task]
**Next Task Confidence**: [X%]
**Blockers**: [Any blockers]

## Team Dashboard

### Current Role Effectiveness
| Role | Current Task | Effectiveness | Confidence | Notes |
|------|--------------|---------------|------------|-------|
| Developer | CreateDatabaseChange | 88% | 91% | Following patterns well |
| QA Engineer | Unit Tests | 82% | 85% | Good coverage |
| Project Manager | Status Tracking | 75% | 78% | Improving visibility |
| DevOps | Build Setup | 68% | 70% | Need CI/CD help |
| SME | Requirements | 65% | 65% | Researching DB specifics |

## Overall Confidence Assessment

### By Phase
| Phase | Average Confidence | Risk Level | Notes |
|-------|-------------------|------------|-------|
| Analysis | 85% | Low | Good documentation available |
| Infrastructure | 92% | Very Low | Standard patterns |
| Implementation | 78% | Medium | Some complex SQL |
| Testing | 88% | Low | Established patterns |
| Documentation | 95% | Very Low | Clear templates |

### By Component Type
| Component | Confidence | Based On |
|-----------|------------|----------|
| Change Classes | 94% | 15+ successes |
| Statements | 96% | Proven pattern |
| SQL Generators | 82% | Complexity varies |
| Integration Tests | 87% | Environment dependent |
| XSD Schema | 78% | Less experience |

### High-Risk Areas (< 70% confidence)
1. **[Component]** - 45% - Need help with [specific aspect]
2. **[Component]** - 60% - Uncertain about [specific detail]
3. **[Component]** - 55% - No clear pattern found

## Risk Register
| Risk | Impact | Likelihood | Mitigation | Status |
|------|--------|------------|------------|---------|
| Documentation gaps | High | Medium | Research patterns | Watching |
| Pattern mismatch | Medium | Low | Use proven patterns | Mitigated |
| Test environment | Low | Low | Docker fallback | Resolved |

## Decision Log
| Date | Decision | Rationale | Impact |
|------|----------|-----------|---------|
| [Date] | Use mutable statements | Matches Liquibase pattern | All statements |
| [Date] | Skip [feature] | Not in scope | Reduced complexity |

## Learning Log

### Success Patterns Discovered
| Date | Pattern | Context | Reusability | Confidence Boost |
|------|---------|---------|-------------|------------------|
| [Date] | Mutable statements | All statement classes | 100% | High - never fails |
| [Date] | Validation in Change | Input validation | 100% | High - consistent |

### Mistakes and Corrections
| Date | Mistake | Impact | Root Cause | Correction | Prevention |
|------|---------|--------|------------|------------|------------|
| [Date] | Used final fields | 45 min lost | Wrong assumption | Use setters | Check patterns first |
| [Date] | No escaping | Test failed | Missed requirement | Add escaping | Always escape SQL |

## Metrics and Progress
- **Planned Tasks**: [Total]
- **Completed**: [Count] ([%])
- **In Progress**: [Count]
- **Blocked**: [Count]
- **Estimated Completion**: [Date]

## Communication Log
| Date | Update | Next Steps |
|------|--------|------------|
| [Date] | Started project planning | Begin documentation analysis |
| [Date] | Completed Phase 1 | Start infrastructure setup |

---

## Time Tracking and Efficiency

### Why Accurate Time Tracking Matters
- **Improves estimates** for future work
- **Identifies bottlenecks** in the development process
- **Reveals patterns** of where time is actually spent
- **Enables optimization** of the development workflow
- **Builds trust** through realistic expectations

### Time Tracking Guidelines
1. **Track actual time**, not estimates
2. **Include all activities**: research, coding, testing, debugging
3. **Be honest** about time spent on failed approaches
4. **Note when spinning wheels** (more than 2-3 attempts)
5. **Distinguish between** learning time vs execution time

### When to Ask for Help

#### Three-Strike Rule
If you've tried 3 different approaches without success:
1. **STOP** attempting
2. **DOCUMENT** what you've tried
3. **ASK** for help

#### Signs You Need Help
- 🔄 Making the same error repeatedly
- 🤔 Guessing about implementation details
- 📚 Documentation is unclear or missing
- 🔍 Can't find a working pattern
- ⏰ Spending >30 min on a "simple" task
- 🎯 Assumptions keep proving wrong

### Help Request Template
```markdown
## Help Needed - [Component Name]

**What I'm trying to do**: [Specific goal]

**What I've tried**:
1. Approach A - Failed because...
2. Approach B - Failed because...
3. Approach C - Failed because...

**Time spent**: [X minutes]

**My assumptions**:
- I believe X should work because...
- Documentation says Y but...

**What I think the problem might be**:
- Invalid assumption about...
- Missing information about...
- Misunderstanding of...

**Specific questions**:
1. Is my approach correct for...?
2. Where can I find information about...?
3. Should I be using pattern X or Y?
```

### Common Time Sinks to Avoid

| Problem | Time Lost | Solution |
|---------|-----------|----------|
| Guessing syntax | 15-30 min | Check real examples first |
| Wrong assumptions | 30-60 min | Validate before implementing |
| Missing patterns | 45-90 min | Ask for pattern reference |
| Environment issues | 60+ min | Get help with setup |
| Circular debugging | 30-45 min | Step back and reassess |

### Efficiency Metrics

Track these metrics for continuous improvement:

| Metric | Target | Actual | Notes |
|--------|--------|--------|-------|
| Simple Change Type | 45-60 min | [Actual] | |
| Complex Change Type | 90-120 min | [Actual] | |
| Unit Tests | 30-45 min | [Actual] | |
| Integration Test | 45-60 min | [Actual] | |
| Debug/Fix Cycle | <15 min | [Actual] | |

---

## Risk Register

| Risk | Impact | Probability | Mitigation Strategy | Status |
|------|--------|-------------|---------------------|--------|
| [Unknown validation rules] | High | Medium | Research thoroughly, test early | Active |
| [Complex SQL generation] | Medium | High | Use existing patterns, seek help | Monitoring |

## Decision Log

| Date | Decision | Rationale | Impact |
|------|----------|-----------|--------|
| [Date] | [What was decided] | [Why] | [What it affects] |
- **Transparency** builds trust  
- **Regular updates** show progress
- **Clear planning** prevents surprises
- **Honest communication** enables collaboration

Remember: **Over-communication is better than under-communication**. Update the status table after every meaningful step, not just major milestones.