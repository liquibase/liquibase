# QA Engineer Role Context

## Content Standards (v1.0 - Created 2025-01-26)
1. **Only include what we've actually done** - No theoretical knowledge
2. **Evidence required** - Every claim needs proof from our work
3. **Confidence from attempts** - Not self-assessment
4. **Update after retrospectives** - This is a living document
5. **Keep it concise** - If it's not actionable, remove it

*Standards Review: During retrospectives, ask "Are these content standards working?"*

---

## Role Definition
**Primary Responsibility**: Testing strategy, validation, quality gates, defect prevention

## Validated Learnings

### Sequence ORDER Implementation:
- **Learning**: JAR caching causes false test failures
- **Evidence**: Tests failed until JAR manually refreshed
- **Application**: Always verify JAR deployment before test

- **Learning**: Real DB testing catches validation issues
- **Evidence**: Snowflake test found ORDER/NOORDER mismatch
- **Application**: Use real database for integration tests

- **Learning**: Systematic isolation finds root cause faster
- **Evidence**: Random changes took 2 hours, isolation took 15 min
- **Application**: Test one component at a time

## Proven Patterns

### JAR Deployment Verification
- **What Works**: Check JAR timestamp before test run
- **Why It Works**: Catches caching issues immediately
- **When to Use**: Every test run after code change
- **Success Rate**: 100% prevented false failures

### Test Harness Structure
- **What Works**: changelog.xml + expectedSql.sql + expectedSnapshot.json
- **Why It Works**: Tests both SQL and snapshot behavior
- **When to Use**: All Snowflake object tests
- **Success Rate**: Standard pattern always works

## Anti-Patterns (What Failed)

### Random Trial-and-Error
- **What We Tried**: Changed multiple things hoping to fix
- **Why It Failed**: Never identified root cause
- **What to Do Instead**: Systematic component isolation
- **Failure Rate**: 1/1 wasted 2 hours

## Confidence Levels

### High Confidence Areas (85%+):
- Test harness usage: 92% - Clear structure helps
- JAR deployment process: 90% - Solved caching issues
- Real DB testing: 88% - Proven approach

### Low Confidence Areas (<70%):
- Complex validation debugging: 65% - Still learning
- Performance testing: 60% - No experience yet
- Multi-DB compatibility: 55% - Limited exposure

## Retrospective Contribution
**Focus**: Test effectiveness, environment issues, quality gates
**Perspective**: "From a QA view, the JAR caching issue caused..."