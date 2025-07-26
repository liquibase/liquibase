# Developer Role Context

## Content Standards (v1.0 - Created 2025-01-26)
1. **Only include what we've actually done** - No theoretical knowledge
2. **Evidence required** - Every claim needs proof from our work
3. **Confidence from attempts** - Not self-assessment
4. **Update after retrospectives** - This is a living document
5. **Keep it concise** - If it's not actionable, remove it

*Standards Review: During retrospectives, ask "Are these content standards working?"*

---

## Role Definition
**Primary Responsibility**: Implementation, coding, technical problem-solving, debugging

## Validated Learnings

### Sequence ORDER Implementation:
- **Learning**: Three-layer pattern (Change/Statement/SQLGenerator) is fundamental
- **Evidence**: Successfully implemented 15+ change types following this pattern
- **Application**: Use for every new database object implementation

- **Learning**: Validation chain priority determines which validator wins
- **Evidence**: Had to use PRIORITY_DATABASE to override standard validation
- **Application**: Always set appropriate priority for database-specific behavior

- **Learning**: Systematic debugging beats random attempts
- **Evidence**: Random debugging took 2+ hours, systematic approach found issue in 30 min
- **Application**: One hypothesis at a time, verify before moving to next

- **Learning**: Standard naming with vendor syntax works best
- **Evidence**: `ordered` attribute generates `ORDER/NOORDER` SQL - clear and consistent
- **Application**: Applied across all Snowflake attribute implementations

## Proven Patterns

### JAR Verification Pattern
- **What Works**: Always verify JAR timestamp after build
- **Why It Works**: Catches deployment issues before wasting time debugging
- **When to Use**: After every build, before testing
- **Success Rate**: Prevented 3+ hours of confusion

### Three-Layer Implementation
- **What Works**: Change handles parsing, Statement holds data, Generator creates SQL
- **Why It Works**: Clear separation, follows Liquibase core architecture
- **When to Use**: Every new change type implementation
- **Success Rate**: 15/15 successful implementations

## Anti-Patterns (What Failed)

### Starting Without Requirements
- **What We Tried**: Jump into coding to "save time"
- **Why It Failed**: 4x time overrun due to rework and confusion
- **What to Do Instead**: Get clear acceptance criteria first
- **Failure Rate**: 1/1 attempt failed spectacularly

### Random Debugging
- **What We Tried**: Try different things hoping to find solution
- **Why It Failed**: Wasted 2+ hours on validation chain issue
- **What to Do Instead**: Systematic approach - one hypothesis at a time
- **Failure Rate**: Multiple attempts before learning lesson

## Confidence Levels

### High Confidence Areas (85%+):
- Three-layer pattern implementation: 95% - 15+ successes
- Service registration: 92% - Works every time when done correctly
- Basic SQL generation: 90% - StringBuilder pattern proven

### Low Confidence Areas (<70%):
- Time estimation for new patterns: 25% - Consistently underestimate by 3-4x
- Complex validation debugging: 60% - Still learning validation chain
- Multi-statement SQL: 50% - Haven't implemented complex cases

## Retrospective Contribution
**Focus**: Technical implementation details, code patterns, debugging approaches
**Perspective**: "From a coding perspective, the validation chain issue was caused by..."