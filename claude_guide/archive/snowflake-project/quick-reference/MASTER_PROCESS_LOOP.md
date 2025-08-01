# Master Process Loop for Snowflake Implementation

## The Loop (Execute for EVERY Task)

```
START
├── 1. CAPTURE TASK - **RETROSPECTIVE FIRST PATTERN**
│   ├── Add to todo list in this exact order:
│   │   1. Create retrospective document (PENDING - reminds me it's required)
│   │   2. Main implementation task (PENDING)
│   │   3. Update project plan with results (PENDING)
│   │   4. Update guides with learnings (PENDING)
│   └── Update project plan status → "IN PROGRESS"
│
├── 2. VERIFY REQUIREMENTS
│   ├── Check requirements doc exists:
│   │   └── Location: claude_guide/snowflake-project/requirements/detailed_requirements/<changeType>_requirements.md
│   ├── If not, create it using:
│   │   └── Guide: claude_guide/generic-patterns/requirements/DETAILED_REQUIREMENTS_CREATION_GUIDE.md
│   └── Update project plan:
│       └── File: claude_guide/snowflake-project/SNOWFLAKE_IMPLEMENTATION_PROJECT_PLAN.md
│
├── 3. ASSESS CURRENT STATE ⚡ CRITICAL STEP
│   ├── DISCOVERY PHASE (Do this FIRST!):
│   │   ├── Search for existing components:
│   │   │   find . -name "*<ChangeType>*" -type f
│   │   │   find . -name "*NamespaceAttributeStorage*" -type f
│   │   │   grep -r "changeType" src/main/java/liquibase/parser/
│   │   ├── Check service registrations:
│   │   │   └── File: src/main/resources/META-INF/services/
│   │   ├── Review XSD for existing attributes:
│   │   │   └── File: src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd
│   │   └── Question any documented limitations
│   ├── Read existing code to determine completeness
│   ├── Run phase tests to verify functionality
│   ├── Compare against requirements
│   └── Update project plan with findings
│   
│   💡 LEARNINGS: 
│   - Always check what exists before implementing!
│   - dropDatabase was 95% complete, saved 15+ minutes
│   - alterSequence ALL components existed, saved 1+ hour
│   - When tests fail, create debug test FIRST to see actual output
│
├── 4. IMPLEMENT
│   ├── Choose implementation guide based on decision tree:
│   │   ├── NEW change type (doesn't exist in Liquibase):
│   │   │   └── Guide: claude_guide/generic-patterns/development/NEW_CHANGETYPE_PATTERN_2.md
│   │   │   └── **⭐ OPERATION-TYPE-DRIVEN ARCHITECTURE for 2+ mutually exclusive modes**
│   │   └── EXISTING change type (add namespace attributes):
│   │       └── Guide: claude_guide/generic-patterns/development/EXISTING_CHANGETYPE_EXTENSION_PATTERN.md
│   ├── **ARCHITECTURE ASSESSMENT (CRITICAL FIRST STEP)**:
│   │   ├── Count mutually exclusive operation modes in requirements
│   │   ├── If 2+ modes → Use Operation-Type-Driven Architecture (71% quality improvement proven)
│   │   └── If single mode → Use standard property-based implementation
│   ├── Follow guide phases systematically
│   ├── Test each phase before moving to next
│   └── Update project plan after each major milestone
│
├── 5. VALIDATE
│   ├── Run all unit tests
│   ├── Create test harness XML file:
│   │   └── Guide: claude_guide/generic-patterns/testing/TEST_HARNESS_IMPLEMENTATION_GUIDE_3.md
│   ├── 🚨 TEST HARNESS EXECUTION (MANDATORY):
│   │   ├── Build & Install JAR: cd liquibase-snowflake && mvn clean install -DskipTests
│   │   │   ⚠️ CRITICAL: Use mvn install not mvn package! Test harness loads via Maven dependencies
│   │   ├── Change to harness: cd ../liquibase-test-harness
│   │   ├── Verify location: pwd (MUST show liquibase-test-harness)
│   │   ├── Run test: mvn test -Dtest=ChangeObjectTests -DchangeObjects=X -DdbName=snowflake
│   │   └── ONLY mark complete if test PASSES
│   ├── Verify requirements coverage
│   └── Update project plan → "COMPLETE" only after test passes
│
├── 6. RETROSPECTIVE (🚨 MANDATORY - OUR LEARNING LOOP)
│   ├── Create retrospective file:
│   │   └── Location: claude_guide/snowflake-project/retrospectives/<TASK>_RETRO.md
│   ├── Document THREE categories:
│   │   ├── 📈 WHAT'S WORKING (Keep doing):
│   │   │   ├── List successful patterns/approaches
│   │   │   ├── Update confidence metrics in docs
│   │   │   └── Reinforce in relevant guides
│   │   ├── 🛑 WHAT'S NOT WORKING (Stop doing):
│   │   │   ├── Identify failed approaches
│   │   │   ├── REMOVE from documentation
│   │   │   └── Add warnings if tempting but wrong
│   │   └── 🔧 WHAT NEEDS IMPROVEMENT (Fix/enhance):
│   │       ├── Identify gaps or inefficiencies
│   │       ├── UPDATE docs with improvements
│   │       └── Add specific examples/warnings
│   ├── 🔔 PRINT KEY LEARNINGS TO CHAT for user visibility
│   └── IMMEDIATELY apply learnings to guides
│
├── 7. INTEGRATE LEARNINGS (🚨 PREVENTS REPEATED MISTAKES)
│   ├── Update guides with learnings:
│   │   ├── Add to troubleshooting sections
│   │   ├── Update file references if needed
│   │   └── Add warnings for common pitfalls
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
- [ ] Task added to todo with ALL sub-tasks (including retrospective)
- [ ] Project plan updated to "IN PROGRESS"

During task:
- [ ] Project plan updated after each phase

After task completion - **HARD STOP CHECKPOINTS**:
- [ ] All unit tests passing
- [ ] Test harness XML created
- [ ] JAR built with mvn clean install
- [ ] Test harness executed against Snowflake
- [ ] Test harness tests passing

**🛑 CHECKPOINT 1: BEFORE CLAIMING COMPLETION**
- [ ] **SHOW ME THE RETROSPECTIVE FILE**: Use Read tool to display full retrospective content
- [ ] **CONFIRM LEARNINGS CAPTURED**: Explicitly state what we learned and what we'll do differently
- [ ] **VERIFY GUIDE UPDATES**: Show which guides were updated and what changes were made

**🛑 CHECKPOINT 2: BEFORE UPDATING PROJECT PLAN**  
- [ ] **ASK USER**: "I'm about to mark this complete. The retrospective is at [path]. Should I proceed?"
- [ ] **WAIT FOR CONFIRMATION**: Do not continue until user acknowledges retrospective completion

**🛑 CHECKPOINT 3: BEFORE STARTING NEXT TASK**
- [ ] **MANDATORY PAUSE**: State "Previous task retrospective complete. Ready for next task?"
- [ ] **FORCE REFLECTION**: "What did the last retrospective teach us about our process?"

Only after all checkpoints:
- [ ] Project plan shows "COMPLETE" with date  
- [ ] All todos marked complete

**❌ CRITICAL PROCESS FAILURES TO AVOID:**
- Marking retrospectives "Done" without creating them
- Updating project plan as afterthought instead of immediately
- Treating documentation as separate from the work
- Assuming problems exist without discovery phase validation

**NO TASK IS COMPLETE WITHOUT A RETROSPECTIVE AND UPDATED PROJECT PLAN**

## 🚨 SELF-ACCOUNTABILITY SYSTEM 

### Before Every "Complete" Claim:
1. **Read my own retrospective file aloud** to verify it exists and has content
2. **Ask user for permission** to mark complete: "Retrospective is at [path], may I proceed?"
3. **Show evidence** of guide updates made based on learnings

### Forbidden Phrases Until Retrospective Exists:
- ❌ "Task complete"
- ❌ "Implementation finished" 
- ❌ "Ready for next task"
- ❌ "Moving on"

### Required Phrases:
- ✅ "Technical work done, now creating retrospective"
- ✅ "Retrospective complete at [path], requesting permission to mark done"
- ✅ "What we learned: [specific learnings], what we'll change: [specific changes]"

### Evidence Required:
- **Retrospective file**: Must display full content using Read tool
- **Guide updates**: Must show specific changes made to process documents
- **Learning integration**: Must state explicitly how this will change future work

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
- 🔧 Test environment not available or misconfigured
- ⚠️ Can't validate implementation (e.g., no test harness)

**DO NOT**:
- Mark tasks "complete" without full validation
- Skip retrospectives because "it was simple"
- Continue without asking when blocked

## Emergency Recovery

If I realize I'm mid-task without following the loop:
1. STOP
2. Add missing todos
3. Update project plan to current state
4. Resume from correct loop position

## Key Files Reference

### Project Files
- **Project Plan**: claude_guide/snowflake-project/SNOWFLAKE_IMPLEMENTATION_PROJECT_PLAN.md
- **Requirements**: claude_guide/snowflake-project/requirements/detailed_requirements/<changeType>_requirements.md
- **Test Results**: claude_guide/snowflake-project/test-results/SNOWFLAKE_TEST_RESULTS_3.md

### Implementation Guides
- **New Change Types**: claude_guide/generic-patterns/development/NEW_CHANGETYPE_PATTERN_2.md
  - **⭐ Operation-Type-Driven Architecture**: For changetypes with 2+ mutually exclusive modes (71% quality improvement)
- **Extending Existing Types**: claude_guide/generic-patterns/development/EXISTING_CHANGETYPE_EXTENSION_PATTERN.md
- **Test Harness**: claude_guide/generic-patterns/testing/TEST_HARNESS_IMPLEMENTATION_GUIDE_3.md
- **Requirements Creation**: claude_guide/generic-patterns/requirements/DETAILED_REQUIREMENTS_CREATION_GUIDE.md
- **TDD Workflows**: claude_guide/implementation_guides/changetype_implementation/ai_workflow_guide.md
- **Implementation Patterns**: claude_guide/implementation_guides/changetype_implementation/changetype_patterns.md

### Source Code Locations
- **Change Classes**: src/main/java/liquibase/change/snowflake/
- **Generators**: src/main/java/liquibase/sqlgenerator/core/snowflake/
- **Parser**: src/main/java/liquibase/parser/snowflake/
- **Storage**: src/main/java/liquibase/parser/snowflake/SnowflakeNamespaceAttributeStorage.java
- **Service Registrations**: src/main/resources/META-INF/services/
- **XSD Schema**: src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd
- **Test Harness Files**: liquibase-test-harness/src/test/resources/liquibase/harness/change/changelogs/snowflake/

## Implementation Decision Tree

**Step 1: Does Liquibase already have this change type?**
- **YES** → Use EXISTING_CHANGETYPE_EXTENSION_PATTERN.md
  - Examples: createTable (add snowflake:transient), alterSequence (add snowflake:setNoOrder), renameTable (SQL generator override)
- **NO** → Use NEW_CHANGETYPE_PATTERN_2.md + Architecture Assessment (Step 2)

**Step 2: How many mutually exclusive operation modes? (For NEW changetypes)**
- **1 MODE** → Standard property-based implementation
  - Examples: createWarehouse, dropDatabase, createSchema
- **2+ MODES** → **⭐ Operation-Type-Driven Architecture** (RECOMMENDED)
  - Examples: alterWarehouse (RENAME/SET/UNSET/SUSPEND/RESUME/ABORT), GRANT/REVOKE operations
  - **PROVEN SUCCESS**: 71% quality improvement, 22/22 tests passing
  - **Key Indicators**: Different validation rules per mode, different SQL generation per mode, operations cannot be combined