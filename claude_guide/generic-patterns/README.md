# Generic Patterns for Liquibase Extension Development

This directory contains reusable patterns, guides, and templates that apply to developing ANY Liquibase database extension, not just Snowflake.

## Directory Contents

### 🔨 development/
Core development patterns for implementing Liquibase extensions.

#### Key Documents:
- **NEW_CHANGETYPE_PATTERN_2.md** - Step-by-step guide for creating new change types
- **EXISTING_CHANGETYPE_EXTENSION_PATTERN.md** - Extending existing Liquibase change types (namespace attributes, SQL generator overrides)
- **DETAILED_REQUIREMENTS_CREATION_GUIDE.md** - How to research and document requirements
- **CHANGE_CLASS_CHECKLIST.md** - Quick reference for change class implementation
- **QUALITY_FIRST_DEVELOPMENT.md** - Development philosophy and best practices

### 🧪 testing/
Comprehensive testing patterns for Liquibase extensions.

#### Key Documents:
- **TEST_HARNESS_IMPLEMENTATION_GUIDE_2.md** - End-to-end database testing guide
- **INTEGRATION_TEST_CHECKLIST.md** - Checklist for integration testing
- **GOAL_PROVE_CODE_WORKS.md** - Testing philosophy for proving functionality
- **GOAL_PROVE_CODE_WONT_BREAK.md** - Testing for robustness and edge cases

### 📋 project-management/
Project planning and tracking templates.

#### Key Documents:
- **LBCF-PROJECT-PLANNING-TEMPLATE.md** - Comprehensive project planning template
- **LBCF-STATUS-TRACKING-GUIDE.md** - How to track project status effectively
- **TIME_TRACKING_METHODOLOGY.md** - Time estimation and tracking methods
- **RETROSPECTIVE_PATTERN.md** - Running effective retrospectives

### 📝 documentation/
Documentation patterns and guidelines.

#### Key Documents:
- **GOAL_BASED_DOCUMENTATION_PATTERN.md** - Writing effective goal-oriented documentation

### 👥 roles/
Role-specific context guides for team members.

#### Available Roles:
- ARCHITECT_ROLE_CONTEXT.md
- DEVELOPER_ROLE_CONTEXT.md
- QA_ENGINEER_ROLE_CONTEXT.md
- PROJECT_MANAGER_ROLE_CONTEXT.md
- PRODUCT_OWNER_ROLE_CONTEXT.md
- DEVOPS_ENGINEER_ROLE_CONTEXT.md
- SCRUM_MASTER_ROLE_CONTEXT.md
- TECHNICAL_WRITER_ROLE_CONTEXT.md

### 📄 templates/
Reusable templates for new projects.

- **DATABASE_IMPLEMENTATION_PROJECT_PLAN_TEMPLATE.md** - Template for planning new database extensions
- **ROLE_CONTEXT_TEMPLATE.md** - Template for creating new role contexts

### 💡 examples/
Working examples from other database extensions.

- **postgresql/** - Complete example of CREATE DOMAIN implementation

### 📎 meta/
Meta documentation about the guide structure and vision.

- **RDBMS_EXTENSION_VISION.md** - Overall vision for Liquibase extensions
- **META-INF-README.md** - Understanding service registration
- **README-TEST-HARNESS.md** - Test harness overview

## Usage Guide

### Starting a New Database Extension

1. **Plan Your Project**
   - Use `templates/DATABASE_IMPLEMENTATION_PROJECT_PLAN_TEMPLATE.md`
   - Review `project-management/LBCF-PROJECT-PLANNING-TEMPLATE.md`

2. **Understand Your Role**
   - Read relevant role context in `roles/`
   - Understand responsibilities and interactions

3. **Research Requirements**
   - Follow `development/DETAILED_REQUIREMENTS_CREATION_GUIDE.md`
   - Document all SQL syntax variations

4. **Choose Implementation Pattern**
   - New change type? Use `development/NEW_CHANGETYPE_PATTERN_2.md`
   - Enhancing existing? Use `development/EXISTING_CHANGETYPE_EXTENSION_PATTERN.md`

5. **Implement with Testing**
   - Write unit tests at each step
   - Follow `testing/TEST_HARNESS_IMPLEMENTATION_GUIDE_2.md`

6. **Track Progress**
   - Use `project-management/LBCF-STATUS-TRACKING-GUIDE.md`
   - Regular retrospectives with `project-management/RETROSPECTIVE_PATTERN.md`

## Best Practices

1. **Requirements First** - Always create detailed requirements before coding
2. **Test Continuously** - Write tests at each implementation step
3. **Document Patterns** - Update guides when you discover new patterns
4. **Role Clarity** - Understand which role you're fulfilling
5. **Learn from Examples** - Study the postgresql example

## Version History

- Files ending in `_2.md` are updated versions incorporating lessons learned
- Original versions preserved when patterns significantly evolve
- Check file headers for specific version notes