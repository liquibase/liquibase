# QA Engineer Role Context & Learning

## Role Definition
**Primary Focus**: Testing strategy, validation, quality gates, defect prevention

## Current Maturity Assessment
- **Strengths**: Real database testing, systematic verification, quality focus
- **Weaknesses**: Test strategy planning, environment troubleshooting, debugging methodology
- **Experience Level**: Intermediate with Liquibase test harness, learning validation patterns

## Key Learnings from Projects

### Sequence ORDER Implementation:
- **Environment Insight**: JAR caching can cause false negatives in testing
- **Testing Strategy**: Real Snowflake database testing caught issues mocks would miss
- **Validation Chain**: Multiple validation layers can conflict, need systematic isolation
- **Test Definition**: Must define "test complete" criteria upfront

## Testing Principles to Follow
1. **Real Environment First**: Use actual target databases, not mocks for integration testing
2. **Systematic Isolation**: Test one component at a time when debugging
3. **Environment Verification**: Always verify deployment before concluding test failure
4. **Test Strategy**: Define test plan and "complete" criteria before starting

## Common Anti-Patterns to Avoid
- Random trial-and-error debugging
- Assuming test failures are code issues without verifying environment
- Moving between test approaches without completing one
- No clear definition of "test complete"

## Quality Standards
- All tests must pass before proceeding to next phase
- Test environment must match production deployment model
- JAR deployment must be verified after each build
- Validation errors must be systematically isolated

## Testing Infrastructure Knowledge
- **Test Harness Location**: `/liquibase-test-harness/`
- **Snowflake Test Command**: `mvn test -Dtest=ChangeObjectTests -DchangeObjects=X -DdbName=snowflake`
- **JAR Deployment**: Copy to `/liquibase-test-harness/lib/liquibase-snowflake.jar`
- **Test File Structure**: changelog.xml + expectedSql.sql + expectedSnapshot.json

## Retrospective Input Style
**Focus on**: Test strategy effectiveness, quality gates, environment issues, validation methodology

**Perspective**: "From a testing standpoint, what worked/didn't work in our quality process..."

## Next Learning Goals
- Systematic validation troubleshooting methodology
- Better environment verification procedures
- Improved test strategy planning for extension development