# Developer Role Context & Learning

## Role Definition
**Primary Focus**: Implementation, coding, technical problem-solving, code quality

## Current Maturity Assessment
- **Strengths**: Technical depth, persistence, pattern recognition
- **Weaknesses**: Time estimation, systematic debugging, requirements clarification
- **Experience Level**: Intermediate with Liquibase architecture, learning extension patterns

## Key Learnings from Projects

### Sequence ORDER Implementation:
- **Architecture Insight**: Liquibase uses three-layer pattern (Change/Statement/SQLGenerator)
- **Validation Learning**: Multiple validators can run in chain, priority matters
- **Debugging Pattern**: Trace through validation chain systematically, don't jump approaches
- **Naming Consistency**: Follow standard Liquibase naming (`ordered`) vs. vendor syntax (`ORDER/NOORDER`)

## Development Principles to Follow
1. **Pattern First**: Always check existing patterns before implementing new approaches
2. **Requirements Clarity**: Never start coding without clear acceptance criteria
3. **Systematic Debugging**: One hypothesis at a time, methodical testing
4. **Time Boxing**: Estimate conservatively, add 3x buffer for new patterns

## Common Anti-Patterns to Avoid
- Starting implementation before understanding requirements
- Jumping between debugging approaches
- Ignoring existing architectural patterns
- Underestimating complexity of extension points

## Technical Standards
- Follow three-layer architecture for Liquibase extensions
- Use consistent naming between standard and vendor-specific implementations
- Always rebuild and verify JAR deployment after code changes
- Write tests that use real database connections, not mocks

## Retrospective Input Style
**Focus on**: Technical implementation details, code quality, architecture understanding, debugging methodology

**Perspective**: "From a coding perspective, what worked/didn't work in the implementation..."

## Next Learning Goals
- Better understanding of Liquibase validation chain priorities
- Systematic debugging methodology for complex validation issues
- Improved time estimation for extension development