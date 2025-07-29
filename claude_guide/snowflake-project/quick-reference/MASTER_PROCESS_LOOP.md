# Master Process Loop for Snowflake Implementation

## The Loop (Execute for EVERY Task)

```
START
├── 1. CAPTURE TASK
│   ├── Add to todo list with ALL sub-tasks:
│   │   - Main implementation task
│   │   - Update project plan (before)
│   │   - Update project plan (after)
│   │   - Create retrospective
│   │   - Share retro synopsis
│   └── Update project plan status → "IN PROGRESS"
│
├── 2. VERIFY REQUIREMENTS
│   ├── Check requirements doc exists
│   ├── If not, create it first
│   └── Update project plan
│
├── 3. ASSESS CURRENT STATE ⚡ CRITICAL STEP
│   ├── Check if implementation files exist first
│   ├── Read existing code to determine completeness
│   ├── Run phase tests to verify functionality
│   ├── Compare against requirements
│   └── Update project plan with findings
│   
│   💡 LEARNING: Always check what exists before implementing!
│   Example: dropDatabase was 95% complete, saved 15+ minutes
│
├── 4. IMPLEMENT
│   ├── Follow technical guides
│   ├── Test each phase
│   └── Update project plan after each major milestone
│
├── 5. VALIDATE
│   ├── Run all unit tests
│   ├── Create test harness XML file
│   ├── 🚨 TEST HARNESS EXECUTION (MANDATORY):
│   │   ├── Build JAR: cd liquibase-snowflake && mvn clean package -DskipTests
│   │   ├── Copy JAR: cp target/*.jar ../liquibase-test-harness/lib/
│   │   ├── Change to harness: cd ../liquibase-test-harness
│   │   ├── Verify location: pwd (MUST show liquibase-test-harness)
│   │   ├── Run test: mvn test -Dtest=ChangeObjectTests -DchangeObjects=X -DdbName=snowflake
│   │   └── ONLY mark complete if test PASSES
│   ├── Verify requirements coverage
│   └── Update project plan → "COMPLETE" only after test passes
│
├── 6. RETROSPECTIVE
│   ├── Create retrospective immediately
│   ├── Extract key learnings
│   ├── 🔔 PRINT KEY LEARNINGS TO CHAT for user visibility
│   └── Share synopsis with user
│
├── 7. INTEGRATE LEARNINGS
│   ├── Update guides with learnings
│   ├── Update project plan → "COMPLETE"
│   └── Mark all todos complete
│
└── CONTINUE → Next task (no pause unless stuck)
    └── ⚠️ ONLY STOP IF:
        - Encountering blockers/issues
        - Need clarification on requirements
        - Finding unexpected complexity
        - Tests failing after multiple attempts
```

## Quick Checklist Version

Before starting ANY task:
- [ ] Task added to todo with ALL sub-tasks
- [ ] Project plan updated to "IN PROGRESS"

During task:
- [ ] Project plan updated after each phase

After task completion:
- [ ] All unit tests passing
- [ ] Test harness XML created
- [ ] JAR built with mvn clean install
- [ ] Test harness executed against Snowflake
- [ ] Test harness tests passing
- [ ] Retrospective created
- [ ] Key learnings printed to chat
- [ ] Synopsis shared with user
- [ ] Project plan shows "COMPLETE" with date
- [ ] All todos marked complete

## Continuous Workflow Mode

**DEFAULT**: Continue to next change type after completing current one
- No need to wait for approval between tasks
- Keep momentum going through the implementation

**STOP AND ASK FOR HELP WHEN**:
- 🚫 Tests failing after debugging attempts
- 🤔 Requirements unclear or conflicting
- 🐛 Encountering unexpected bugs or blockers
- 📚 Finding architectural decisions that need discussion
- ⚡ Implementation taking significantly longer than expected

## Emergency Recovery

If I realize I'm mid-task without following the loop:
1. STOP
2. Add missing todos
3. Update project plan to current state
4. Resume from correct loop position