# CLAUDE.md

This file provides high-level guidance to Claude Code when working on the Liquibase Snowflake Extension.

## 🎯 Current Focus: Snowflake Extension Development

Working on: COLUMN object change types (SQL generator overrides)
Project Plan: `claude_guide/snowflake-project/SNOWFLAKE_IMPLEMENTATION_PROJECT_PLAN.md`
Next: Column operations needing SQL syntax overrides

## 🔄 CRITICAL: Follow the Master Process Loop

**For EVERY task**: Follow `claude_guide/snowflake-project/quick-reference/MASTER_PROCESS_LOOP.md`
- This ensures project tracking and retrospectives happen
- Operational tasks are part of the work, not overhead

## 📚 Essential Guides (In Order of Use)

1. **Process & Workflow**
   - `MASTER_PROCESS_LOOP.md` - The complete process with all file references

2. **Implementation**
   - `NEW_CHANGETYPE_PATTERN_2.md` - For new change types
   - `EXISTING_CHANGETYPE_EXTENSION_PATTERN.md` - For extending existing types
   - `SQL_GENERATOR_OVERRIDE_STEP_BY_STEP.md` - For SQL syntax overrides (column operations)

3. **Testing**
   - `TEST_HARNESS_IMPLEMENTATION_GUIDE_2.md` - After unit tests pass

4. **Requirements & Tracking**
   - `detailed_requirements/<changeType>_requirements.md` - Per change type
   - `SNOWFLAKE_IMPLEMENTATION_PROJECT_PLAN.md` - Update continuously

## 🚨 When Things Go Wrong

**DO NOT assume where the bug is!**
- Use systematic debugging (test each layer separately)
- Question "known bugs" - they might be false
- See troubleshooting in implementation guides

## 🏗️ Project Structure

```
liquibase/
├── liquibase-snowflake/          # Extension module (YOU ARE HERE)
│   ├── src/main/java/           # Implementation
│   ├── src/test/java/           # Unit tests
│   └── src/main/resources/      # XSD, services
└── claude_guide/                 # All guides and patterns
    ├── generic-patterns/         # Reusable patterns
    └── snowflake-project/        # Project-specific
```

## 🔗 Quick Links

- Build Commands: See `SNOWFLAKE_QUICK_REFERENCE.md`
- Architecture Details: See `LIQUIBASE_ARCHITECTURE_OVERVIEW.md`
- Test Structure: `src/test/java/README_TEST_STRUCTURE.md`
- Current Status: Check project plan

---
**Remember**: This is a high-level entry point. For specifics, follow the guides.

