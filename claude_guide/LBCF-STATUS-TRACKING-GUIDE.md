# LBCF Status Tracking Quick Reference

## Why Status Tracking Matters

**Trust through Transparency**: Detailed status tracking builds confidence by:
- Showing exactly what's being worked on
- Providing realistic progress assessments  
- Identifying blockers early
- Demonstrating systematic approach
- Enabling collaborative problem-solving

## When to Update Status

### ALWAYS Update After:
- ✅ Completing any task (even small ones)
- 🚀 Starting a new component
- 🚧 Encountering a blocker
- 💡 Making a design decision
- ⏰ Every 30 minutes during long tasks
- ❌ Finding something doesn't work
- 🔍 Discovering new information

### Status Table Quick Update

```markdown
| 3.2.1 | CreateWarehouseChange | 🟢 Completed | 10:45am | 11:30am | 45 min | All validations working | |
```

### Time Tracking in Updates

Always include:
- **Actual time spent** (not estimates)
- **Why it took longer/shorter** than expected
- **What you learned** about timing

## Status Symbols

- 🔴 **Not Started** - Haven't begun
- 🟡 **In Progress** - Currently working
- 🟢 **Completed** - Done and tested
- 🔵 **Blocked** - Can't proceed
- ⚫ **Skipped** - Not needed

## Update Message Templates

### Task Completed
```markdown
## Status Update - 11:30am
**Completed**: CreateWarehouseChange implementation
**Result**: 18 attributes mapped, validation for all fields, supports OR REPLACE
**Next**: Starting CreateWarehouseStatement
**Blockers**: None
**Timeline**: On track
```

### Blocker Found
```markdown
## Status Update - 2:15pm
**Completed**: Started SQL generator implementation
**Result**: Discovered WAREHOUSE_TYPE requires special handling
**Next**: Research Snowflake docs for WAREHOUSE_TYPE syntax
**Blockers**: Unclear if TYPE goes in WITH clause or separately
**Timeline**: May add 30 min for research
```

### Progress During Long Task
```markdown
## Status Update - 3:45pm
**Completed**: 5 of 12 unit tests written
**Result**: All passing, found edge case with null handling
**Next**: Continue with remaining 7 tests
**Blockers**: None
**Timeline**: On track, 45 min remaining
```

### Asking for Help
```markdown
## Status Update - 4:20pm
**Completed**: Attempted SQL generator implementation
**Result**: 3 approaches failed (35 min total)
**Next**: STOPPING per 3-strike rule - need help
**Blockers**: Cannot figure out proper WITH clause syntax
**Timeline**: Will slip 30-60 min without help
**Help Needed**: Example of complex WITH clause generation
```

## Best Practices

### DO:
- ✅ Update immediately after completing something
- ✅ Be specific about what was done
- ✅ Include actual times, not just estimates
- ✅ Mention unexpected findings
- ✅ Update timeline estimates when they change
- ✅ Note partial progress on long tasks

### DON'T:
- ❌ Wait until end of phase to update
- ❌ Use vague descriptions like "working on stuff"
- ❌ Hide blockers or problems
- ❌ Skip updates because "nothing interesting happened"
- ❌ Batch updates together

## Sample Status Progression

```markdown
| Component | 9:00am | 9:30am | 10:00am | 10:30am |
|-----------|---------|---------|----------|----------|
| Analysis | 🔴 Not Started | 🟡 In Progress | 🟡 In Progress | 🟢 Completed |
| Change Class | 🔴 Not Started | 🔴 Not Started | 🟡 In Progress | 🟢 Completed |
| Statement | 🔴 Not Started | 🔴 Not Started | 🔴 Not Started | 🟡 In Progress |
```

## Trust Indicators

Updates that build trust:
- "Found issue with approach, switching to pattern B"
- "This is taking longer than expected because..."
- "Blocked on X, researching alternative"
- "Discovered Y wasn't documented, testing to understand"
- "Completed ahead of schedule, moving to next"

## Remember

**Over-communication > Under-communication**

Every update provides:
- Confidence that work is progressing
- Early warning of issues
- Opportunity for guidance
- Evidence of systematic approach
- Basis for accurate estimates

The goal is to make the human feel like they're sitting next to you, seeing exactly what you're doing and why.