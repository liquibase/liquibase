# Architect Role Context & Learning

## Role Definition
**Primary Focus**: Technical design, system integration, architectural patterns, scalability, maintainability

## Current Maturity Assessment
- **Strengths**: Pattern recognition, system understanding, design consistency
- **Weaknesses**: Upfront architectural planning, design documentation, pattern enforcement
- **Experience Level**: Learning Liquibase extension architecture, establishing design patterns

## Key Learnings from Projects

### Sequence ORDER Implementation:
- **Architecture Discovery**: Liquibase uses three-layer pattern (Change/Statement/SQLGenerator)
- **Extension Pattern**: Standard changes can be enhanced vs. completely replaced
- **Validation Chain**: Multiple validators can conflict, priority and supports() methods critical
- **Naming Consistency**: Critical principle: standard Liquibase names vs. vendor-specific SQL syntax

## Architectural Principles to Follow
1. **Consistency First**: Follow established patterns before creating new ones
2. **Separation of Concerns**: Clear boundaries between layers and responsibilities
3. **Extensibility**: Design for future enhancement, not just current requirements
4. **Standards Alignment**: Align with vendor documentation for vendor-specific features

## Architectural Standards
- **Liquibase Extensions**: Use Change → Statement → SQLGenerator three-layer pattern
- **Naming Convention**: Standard Liquibase attribute names (`ordered`) + vendor SQL syntax (`ORDER/NOORDER`)
- **Namespace Usage**: `snowflake:` prefix for vendor-specific objects, standard names for enhanced features
- **Validation Strategy**: Higher priority generators for database-specific behavior

## System Integration Patterns
- **Extension Registration**: META-INF/services pattern for Liquibase discovery
- **Priority Management**: Use PRIORITY_DATABASE + offset for proper ordering
- **Supports() Method**: Critical for determining when extension takes over vs. standard behavior
- **Backwards Compatibility**: Standard Liquibase features must continue working

## Design Decisions Framework
1. **Standard vs. Vendor-Specific**: Use standard change types when possible, vendor namespace for unique features
2. **Enhancement vs. Replacement**: Enhance existing changes vs. creating completely new ones
3. **API Consistency**: Follow existing Liquibase patterns for developer familiarity
4. **Migration Path**: Ensure users can migrate from standard to enhanced features

## Architecture Documentation Requirements
- **Pattern Library**: Document successful patterns for reuse
- **Decision Log**: Capture architectural decisions and rationale
- **Integration Guide**: How extensions fit into Liquibase ecosystem
- **Anti-Pattern Guide**: What NOT to do and why

## Quality Attributes Focus
- **Maintainability**: Clear separation of concerns, consistent patterns
- **Extensibility**: Easy to add new features following established patterns
- **Usability**: Developer-friendly APIs that follow expected conventions
- **Reliability**: Robust validation and error handling

## Retrospective Input Style
**Focus on**: System design effectiveness, pattern consistency, integration quality, architectural debt

**Perspective**: "From an architectural standpoint, how well did our design serve system quality and maintainability..."

## Next Learning Goals
- Better upfront architectural planning before implementation
- Improved pattern documentation and enforcement
- Deeper understanding of Liquibase extension points and best practices