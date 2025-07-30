# Claude Guide - Liquibase Extension Development

This repository contains comprehensive guides, patterns, and documentation for developing Liquibase database extensions.

## Directory Structure

### 📚 generic-patterns/
Reusable patterns and guides that apply to ANY Liquibase extension development.

- **development/** - Implementation patterns for change types, namespace attributes
- **testing/** - Test harness patterns, integration testing guides
- **project-management/** - Project planning templates, tracking methodologies
- **documentation/** - Documentation patterns and best practices
- **roles/** - Role-specific context guides for team members
- **templates/** - Reusable project templates
- **examples/** - Working examples from other database extensions
- **meta/** - Meta documentation about the guide structure

### 🏔️ snowflake-project/
Snowflake-specific implementation documentation and project files.

- **quick-reference/** - Quick start guides and master prompts
- **project-status/** - Current project status, plans, and tracking
- **audits-reports/** - Implementation audits and verification reports
- **development-guides/** - Snowflake-specific development guidance
- **requirements/** - Detailed requirements for each Snowflake change type

## Quick Start

### For New Database Extension Projects
1. Start with `generic-patterns/templates/DATABASE_IMPLEMENTATION_PROJECT_PLAN_TEMPLATE.md`
2. Review role guides in `generic-patterns/roles/`
3. Follow patterns in `generic-patterns/development/`

### For Snowflake Development
1. Start with `snowflake-project/quick-reference/SNOWFLAKE_CHANGETYPE_IMPLEMENTATION_PROMPT.md`
2. Check current status in `snowflake-project/project-status/`
3. Find requirements in `snowflake-project/requirements/`

## Key Documents

### Generic Patterns
- [New Change Type Pattern](generic-patterns/development/NEW_CHANGETYPE_PATTERN_2.md)
- [Namespace Attribute Pattern](generic-patterns/development/EXISTING_CHANGETYPE_EXTENSION_PATTERN.md)
- [Test Harness Guide](generic-patterns/testing/TEST_HARNESS_IMPLEMENTATION_GUIDE_3.md)
- [Requirements Creation Guide](generic-patterns/development/DETAILED_REQUIREMENTS_CREATION_GUIDE.md)

### Snowflake Project
- [Implementation Prompt](snowflake-project/quick-reference/SNOWFLAKE_CHANGETYPE_IMPLEMENTATION_PROMPT.md)
- [Project Plan](snowflake-project/project-status/SNOWFLAKE_IMPLEMENTATION_PROJECT_PLAN.md)
- [Quick Reference](snowflake-project/quick-reference/SNOWFLAKE_QUICK_REFERENCE.md)

## Navigation Tips

- **Generic patterns** are numbered with versions (e.g., `_2.md`) when updated
- **Snowflake files** often have `SNOWFLAKE_` prefix for easy identification
- **Requirements** follow the pattern `<changeType>_requirements.md`
- **Role contexts** follow the pattern `<ROLE>_ROLE_CONTEXT.md`

## Contributing

When adding new documentation:
1. Determine if it's generic (any database) or specific (Snowflake)
2. Place in appropriate directory
3. Follow existing naming conventions
4. Update relevant README files