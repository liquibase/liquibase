# Architect Role Context

## Content Standards (v1.0 - Created 2025-01-26)
1. **Only include what we've actually done** - No theoretical knowledge
2. **Evidence required** - Every claim needs proof from our work
3. **Confidence from attempts** - Not self-assessment
4. **Update after retrospectives** - This is a living document
5. **Keep it concise** - If it's not actionable, remove it

*Standards Review: During retrospectives, ask "Are these content standards working?"*

---

## Role Definition
**Primary Responsibility**: Technical design, system integration, architectural patterns

## Validated Learnings

### Sequence ORDER Implementation:
- **Learning**: Liquibase uses three-layer pattern (Change/Statement/SQLGenerator)
- **Evidence**: Successfully implemented ORDER functionality following this pattern
- **Application**: Used same pattern for all subsequent Snowflake objects

- **Learning**: Extension pattern - enhance standard changes vs. completely replace
- **Evidence**: CreateSequenceChangeSnowflake extends standard change, adds Snowflake features
- **Application**: Applied to all enhanced change types (CreateTableSnowflakeChange, etc.)

- **Learning**: Validation chain priority critical
- **Evidence**: Had to use PRIORITY_DATABASE to override standard validation
- **Application**: All Snowflake generators use priority for proper ordering

- **Learning**: Naming consistency principle crucial
- **Evidence**: Used standard attribute name `ordered` with SQL syntax `ORDER/NOORDER`
- **Application**: Applied across all Snowflake extensions

## Proven Patterns

### Three-Layer Architecture
- **What Works**: Change → Statement → SQLGenerator separation
- **Why It Works**: Clear separation of concerns, follows Liquibase core
- **When to Use**: Every new database object implementation
- **Success Rate**: 15+ successful implementations

### Standard Enhancement Pattern
- **What Works**: Extend standard changes, add vendor-specific attributes
- **Why It Works**: Maintains compatibility while adding features
- **When to Use**: When database has additional options for standard objects
- **Success Rate**: 5/5 enhanced changes worked perfectly

## Anti-Patterns (What Failed)

### Namespace Confusion
- **What We Tried**: Using `snowflake:` prefix for enhanced standard objects
- **Why It Failed**: Should only use prefix for Snowflake-specific objects
- **What to Do Instead**: Standard names for enhanced features, prefix for unique objects
- **Failure Rate**: 2 attempts before understanding

## Confidence Levels

### High Confidence Areas (85%+):
- Three-layer pattern implementation: 95% - 15+ successful uses
- Service registration pattern: 92% - All components registered correctly
- Priority management: 88% - Validation chain works as expected

### Low Confidence Areas (<70%):
- Upfront architectural planning: 65% - Often discover patterns during implementation
- Multi-statement SQL generation: 60% - Haven't implemented complex cases yet

## Retrospective Contribution
**Focus**: System design effectiveness, pattern consistency, integration quality
**Perspective**: "From an architectural standpoint, the three-layer pattern continues to prove reliable..."