# Liquibase Database Extension Code Factory - Relational Database Project Guide

## Project Vision

The Liquibase Database Extension Code Factory (LBCF-RDBMS) is an AI-driven framework for rapidly developing high-quality, production-ready database extensions for Liquibase 4.33.0+. This guide enables AI assistants to create complete database extensions with minimal human intervention while maintaining exceptional code quality and comprehensive test coverage.

## Core Principles

### 1. Quality-First Development
- **First-Time Accuracy**: Every implementation should work correctly on first attempt
- **Comprehensive Validation**: Validate assumptions before implementation
- **Pattern-Based Development**: Use proven patterns, don't reinvent
- **Test-Driven Approach**: Write tests first, implementation second
- **No Shortcuts**: 100% working solutions only - no workarounds or partial implementations
- **Complete Validation**: Every feature must be fully tested and verified

### 2. Transparency and Trust
- **Visible Planning**: Create detailed project plans before starting
- **Real-Time Updates**: Update status after every meaningful action
- **Honest Communication**: Report blockers and challenges immediately
- **Clear Expectations**: Set realistic timelines and update when needed
- **Progress Tracking**: Maintain accurate status tables throughout

### 3. Short Development Cycles
- **Incremental Implementation**: Small, testable units of work
- **Rapid Feedback**: Test immediately after implementation
- **Minimal Context**: Keep working set small to avoid confusion
- **Continuous Validation**: Verify each step before proceeding

### 4. High Development Velocity
Development velocity = (Quality × Completeness) / Time
- **Quality**: Code that works correctly first time
- **Completeness**: All features implemented with tests
- **Time**: Minimized through automation and patterns

### 5. Efficient Time Management
- **Track Actual Time**: Record real time spent, not estimates
- **Three-Strike Rule**: After 3 failed attempts, stop and ask for help
- **No Guessing**: When uncertain, validate assumptions before proceeding
- **Early Help Requests**: Better to ask early than waste time on wrong approaches
- **Complete Solutions**: Time spent on shortcuts usually doubles total time

## Project Requirements

### Input Requirements
1. **Database Documentation Link**: Vendor's official DDL, DCL, and DML documentation
2. **Target Database**: Name and version of the database to support
3. **Liquibase Version**: 4.33.0 or higher
4. **Extension Scope**: Specific objects/features to implement

### Output Deliverables
1. **Complete Extension Module**: Fully functional database extension
2. **Comprehensive Test Suite**: Unit, integration, and test harness tests
3. **Documentation**: User guide and implementation notes
4. **Quality Report**: Test coverage and validation results

## Development Philosophy

### Pattern-First Development
1. **Identify Pattern**: Find existing implementation pattern
2. **Copy Template**: Use working example as base
3. **Adapt Specifics**: Modify for target database
4. **Validate Implementation**: Test against real database

### Test-Driven Implementation
1. **Write Test First**: Define expected behavior
2. **Implement Minimum**: Just enough to pass test
3. **Refactor**: Improve code while tests pass
4. **Document**: Capture patterns for reuse

### Uncertainty Management
When uncertain:
1. **Stop Implementation**: Don't guess
2. **Document Question**: Specific uncertainty
3. **Research Pattern**: Find similar implementation
4. **Request Clarification**: If pattern not found

## Implementation Strategy

### Critical First Step: Create Project Plan
**IMPORTANT**: Before any implementation, create a detailed project plan using the LBCF-PROJECT-PLANNING-TEMPLATE.md. This provides:
- Complete visibility into planned work
- Real-time status tracking
- Clear communication of progress
- Trust through transparency

### Phase 1: Analysis and Planning
1. Create project plan document with status tracker
2. Parse database documentation
3. Identify object types and attributes
4. Map to Liquibase patterns
5. Create detailed implementation plan
6. Define test scenarios
7. Update status tracker after each step

### Phase 2: Core Infrastructure
1. Create extension module structure
2. Implement base classes
3. Set up service registration
4. Configure namespace handling
5. Create XSD schema

### Phase 3: Incremental Implementation
For each database object:
1. Create change type class
2. Create statement class
3. Create SQL generator
4. Write unit tests
5. Write integration tests
6. Validate with real database

### Phase 4: Quality Assurance
1. Run full test suite
2. Verify test coverage
3. Validate against database
4. Document any limitations
5. Create user examples

## Success Metrics

### Quality Metrics
- **Test Coverage**: >90% line coverage
- **First-Run Success**: >95% tests pass first attempt
- **Pattern Compliance**: 100% follows established patterns
- **Documentation**: Complete for all public APIs

### Velocity Metrics
- **Implementation Time**: <2 hours per change type
- **Test Development**: <1 hour per change type
- **Debug/Fix Time**: <30 minutes per issue
- **Total Cycle Time**: <4 hours per feature

## Risk Management

### Common Risks
1. **Pattern Deviation**: Using untested approaches
2. **Incomplete Testing**: Missing edge cases
3. **Documentation Gaps**: Unclear requirements
4. **Integration Issues**: Version incompatibilities

### Mitigation Strategies
1. **Strict Pattern Adherence**: Always use proven patterns
2. **Comprehensive Test Suite**: Test all scenarios
3. **Early Validation**: Test assumptions quickly
4. **Version Locking**: Fixed Liquibase version

## Next Steps

This guide will be supplemented with:
1. **Implementation Templates**: Code templates for all components
2. **Test Framework Guide**: Comprehensive testing patterns
3. **Pattern Library**: Database-specific patterns
4. **Quality Checklist**: Validation criteria
5. **Troubleshooting Guide**: Common issues and solutions

## Key Success Factors

1. **Trust the Process**: Follow patterns exactly
2. **Test Everything**: No code without tests
3. **Document Discoveries**: Capture new patterns
4. **Maintain Quality**: Never compromise on quality
5. **Iterate Quickly**: Small steps, fast feedback

## Project Structure

```
liquibase-<database>-extension/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── liquibase/
│   │   │       ├── change/core/
│   │   │       ├── statement/core/
│   │   │       └── sqlgenerator/core/<database>/
│   │   └── resources/
│   │       ├── META-INF/services/
│   │       └── www.liquibase.org/xml/ns/<database>/
│   └── test/
│       ├── groovy/
│       └── resources/
├── pom.xml
└── README.md
```

## Communication Protocol

### Progress Updates
**MANDATORY**: Maintain real-time status tracking throughout the project:
1. Update project plan status table after EVERY task
2. Include timestamp and results for each update
3. Be transparent about blockers and challenges
4. Provide revised estimates when timelines change
5. Over-communicate rather than under-communicate

### Update Frequency
- After every completed subtask (not just milestones)
- When starting a new component
- When encountering any blocker
- When making any significant decision
- At regular intervals during long tasks

### Status Update Format
```markdown
## Status Update - [Timestamp]
**Completed**: [Specific task completed]
**Result**: [What was learned/created]
**Next**: [Next immediate task]
**Blockers**: [Any issues encountered]
**Timeline**: [On track/Adjusted - explanation]
```

### Quality Gates
Before proceeding to next phase:
1. All tests pass
2. Code follows patterns
3. Documentation complete
4. No unresolved questions

## Conclusion

This guide establishes the foundation for AI-assisted database extension development. By following these principles and patterns, we can achieve rapid, high-quality implementation of database extensions with minimal human intervention while maintaining the highest standards of software quality.

Remember: **Quality enables velocity**. By doing things right the first time, we minimize rework and maximize productive output.

**Success builds confidence, mistakes build wisdom**. Track both to create a continuously improving system.

**Excellence requires multiple perspectives**. Use role-based development to ensure quality from every angle - developer, QA, PM, DevOps, and domain expert views all matter.