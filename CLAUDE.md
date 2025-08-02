# CLAUDE.md

This file provides high-level guidance to Claude Code when working on the Liquibase Snowflake Extension.

## 🎯 Current Focus: Snowflake Extension Development

Working on: Phase 1 - Framework-Driven Parameter Discovery (Confidence-based validation)
Project Plan: Systematic changetype requirements update following new requirements gathering guide
Next: Phase 2 - Requirements document updates with confidence-validated parameters

## ⚡ Snowflake Database Connection (CRITICAL - DO NOT FORGET)

### Connection Credentials (ALWAYS USE WITH VALIDATION TESTS)
```bash
SNOWFLAKE_URL="jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LTHDB&warehouse=LTHDB_TEST_WH&schema=TESTHARNESS&role=LIQUIBASE_TEST_HARNESS_ROLE" SNOWFLAKE_USER="COMMUNITYKEVIN" SNOWFLAKE_PASSWORD="uQ1lAjwVisliu8CpUTVh0UnxoTUk3"
```

### Simple Parameter Validation (REQUIRES SNOWFLAKE CONNECTION)
```bash
SNOWFLAKE_URL="jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LTHDB&warehouse=LTHDB_TEST_WH&schema=TESTHARNESS&role=LIQUIBASE_TEST_HARNESS_ROLE" SNOWFLAKE_USER="COMMUNITYKEVIN" SNOWFLAKE_PASSWORD="uQ1lAjwVisliu8CpUTVh0UnxoTUk3" mvn test -Dtest=SnowflakeParameterValidationTest -q
```

**Effective 3-Step Process:**
1. Query INFORMATION_SCHEMA for actual parameters
2. Compare against XSD schema  
3. Manual doc review for gaps
**Result: 15 minutes vs days of frameworks**

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
   - `TEST_HARNESS_IMPLEMENTATION_GUIDE_3.md` - After unit tests pass (simplified with schema isolation)

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

