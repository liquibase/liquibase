# CLAUDE.md

This file provides high-level guidance to Claude Code when working on the Liquibase Snowflake Extension.

## 🎯 Current Focus: Snowflake Extension Development

Working on: Phase 1 - Framework-Driven Parameter Discovery (Confidence-based validation)
Project Plan: Systematic changetype requirements update following new requirements gathering guide
Next: Phase 2 - Requirements document updates with confidence-validated parameters

## ⚡ Database Connection & Validation

**For all database credentials and validation commands**, see the implementation guide Phase 0 (Quick Validation).

## 🔄 Implementation Workflow Overview

### When to Use Each Guide

**Changetype Implementation** (Database operations like CREATE, ALTER, DROP):
- Use: `claude_guide/implementation_guides/changetype_implementation/CHANGETYPE_IMPLEMENTATION_GUIDE.md`
- Scenarios: New changetypes, extending existing changetypes, SQL generator overrides

**Snapshot/Diff Implementation** (Database introspection and comparison):
- Use: `claude_guide/implementation_guides/snapshot_diff_implementation/SNAPSHOT_DIFF_IMPLEMENTATION_GUIDE.md`  
- Scenarios: Database object discovery, schema comparison, migration analysis

### Implementation Process
1. **Requirements Research** → Use requirements documentation in `claude_guide/snowflake_requirements/`
2. **Choose Pattern** → Follow appropriate implementation guide
3. **Implement** → Use guide phases and validation steps  
4. **Test & Validate** → Follow guide testing protocols

## 📚 Essential Guides (In Order of Use)

1. **Process & Workflow**
   - `claude_guide/snowflake_requirements/MASTER_INDEX.md` - AI-optimized navigation for all requirements

2. **Implementation**
   - `claude_guide/implementation_guides/changetype_implementation/CHANGETYPE_IMPLEMENTATION_GUIDE.md` - Complete changetype implementation workflow
   - `claude_guide/implementation_guides/snapshot_diff_implementation/SNAPSHOT_DIFF_IMPLEMENTATION_GUIDE.md` - Snapshot and diff implementation
   - `claude_guide/implementation_guides/aipl_programs/` - AIPL automation programs for debugging, validation, and file management

3. **Requirements & Documentation**
   - `claude_guide/snowflake_requirements/changetype_requirements/` - Per-changetype requirements
   - `claude_guide/snowflake_requirements/REQUIREMENTS_SUMMARY.md` - Missing parameters reference

## 🚨 XSD Schema Validation

**For complete XSD validation protocols, checklists, and troubleshooting**, see the implementation guide Phase 3 (XSD Schema Integration).

## 🚨 When Things Go Wrong

**DO NOT assume where the bug is!**
- Use systematic debugging (test each layer separately)
- Question "known bugs" - they might be false
- See troubleshooting in implementation guides
- **For automated debugging**: Use `claude_guide/implementation_guides/aipl_programs/systematic-implementation-debugging.yaml`

## 🏗️ Project Structure Overview

```
liquibase/
├── liquibase-snowflake/          # Extension module (YOU ARE HERE)  
│   ├── src/main/java/           # Implementation
│   ├── src/test/java/           # Unit tests
│   └── src/main/resources/      # XSD, services
└── claude_guide/                 # All guides and patterns
    ├── implementation_guides/    # Complete implementation workflows
    │   └── aipl_programs/        # AIPL automation programs
    └── snowflake_requirements/   # Comprehensive requirements documentation
```

**For detailed file placement rules and repository management**, see the implementation guides.

## 🔗 Quick Links

- **Requirements Navigation**: `claude_guide/snowflake_requirements/MASTER_INDEX.md`
- **Implementation Workflow**: `claude_guide/implementation_guides/changetype_implementation/CHANGETYPE_IMPLEMENTATION_GUIDE.md`
- **AIPL Automation Programs**: `claude_guide/implementation_guides/aipl_programs/README.md`
- **Test Structure**: `liquibase-snowflake/src/test/java/README_TEST_STRUCTURE.md`

---
**Remember**: This is a high-level entry point. For specifics, follow the guides.

