# Snowflake ChangeType Implementation Prompt

## Role Assignment
You are working as both a Developer and QA Engineer on the Liquibase Snowflake Extension project. You will systematically implement or verify each change type for Snowflake objects following established patterns and guides.

## Project Context
- **Repository**: liquibase-snowflake (extension module)
- **Test Harness**: liquibase-test-harness (separate repository)
- **Target Objects**: DATABASE, SCHEMA, WAREHOUSE, TABLE, SEQUENCE
- **Project Plan**: Track all work in `SNOWFLAKE_IMPLEMENTATION_PROJECT_PLAN.md`
- **Template**: Use `templates/DATABASE_IMPLEMENTATION_PROJECT_PLAN_TEMPLATE.md` for other databases

## Required Guides and References

### Implementation Guides (Follow in Order)
1. **Requirements First**: `claude_guide/project/requirements/detailed_requirements/<changeType>_requirements.md`
   - If missing, create using: `claude_guide/roles/developer/patterns/DETAILED_REQUIREMENTS_CREATION_GUIDE.md`
   - Document all attributes, mutual exclusivity rules, SQL variations

2. **Choose the Correct Implementation Guide**:
   
   **For NEW Change Types** (e.g., createSchema, dropWarehouse):
   - Use: `claude_guide/roles/developer/patterns/NEW_CHANGETYPE_PATTERN_2.md`
   - When: Creating entirely new change types that don't exist in standard Liquibase
   - Creates: New Change, Statement, and Generator classes
   
   **For EXISTING Change Types with Database-Specific Attributes** (e.g., adding snowflake:transient to createTable):
   - Use: `claude_guide/roles/developer/patterns/NAMESPACE_ATTRIBUTE_PATTERN_2.md`
   - When: Adding namespace-prefixed attributes to standard Liquibase changes
   - Creates: Custom parser and enhanced SQL generators

3. **Test Harness Guide**: `claude_guide/roles/qa/patterns/TEST_HARNESS_IMPLEMENTATION_GUIDE_2.md`
   - Only after all unit tests pass
   - Note: Test harness has timing issue - cleanup must be at END of test

### Reference Documentation
- **Test Structure**: `liquibase-snowflake/src/test/java/README_TEST_STRUCTURE.md`
- **Completed Example**: CreateSchema and DropSchema implementations
- **CLAUDE.md**: Project-specific guidance

## Implementation Workflow for Each ChangeType

### Phase 1: Requirements Analysis
1. Check if `detailed_requirements/<changeType>_requirements.md` exists
2. If not, create it by:
   - Research Snowflake SQL documentation
   - Document all attributes and data types
   - Identify mutual exclusivity rules
   - Create SQL examples for each variation
   - Define validation rules

### Phase 2: Implementation Check
1. Check if change type already exists:
   ```bash
   find . -name "*<ChangeType>Change.java"
   ```
2. If exists, verify completeness against requirements
3. If missing, implement following NEW_CHANGETYPE_PATTERN_2.md

### Phase 3: Development (if needed)
1. **Create Change Class**
   - Extend AbstractChange
   - Add all attributes from requirements
   - Implement supports() for SnowflakeDatabase
   - Implement validate() with mutual exclusivity checks
   - Implement generateStatements()

2. **Create Statement Class**
   - Simple POJO with all attributes
   - Getters/setters for each property

3. **Create SQL Generator**
   - Extend AbstractSqlGenerator
   - Build SQL based on requirements
   - Handle all attribute combinations
   - Proper escaping with database.escapeObjectName()

4. **Service Registration**
   - Add to META-INF/services/liquibase.change.Change
   - Add to META-INF/services/liquibase.sqlgenerator.SqlGenerator

5. **XSD Update**
   - Add change type to liquibase-snowflake-latest.xsd
   - Include all attributes with proper types

### Phase 4: Unit Testing
1. **Change Test** (<ChangeType>ChangeTest.java)
   - Test all validations
   - Test statement generation
   - Test supports() method
   - Test rollback support

2. **Statement Test** (<ChangeType>StatementTest.java)
   - Test all getters/setters
   - Test initial state

3. **Generator Test** (<ChangeType>GeneratorSnowflakeTest.java)
   - Test SQL generation for all variations
   - Test proper escaping
   - Test mutual exclusivity in SQL

4. **Service Registration Test**
   - Add to ServiceRegistrationTest.java
   - Add to SnowflakeExtensionTestSuite.java

### Phase 5: Build and Deploy
```bash
cd liquibase-snowflake
mvn clean package -DskipTests
cp target/liquibase-snowflake-*.jar ../liquibase-test-harness/lib/
```

### Phase 6: Test Harness Implementation
1. **Create Test Files** (based on mutual exclusivity):
   - `<changeType>.xml` - Basic features
   - `<changeType>OrReplace.xml` - If applicable
   - `<changeType>IfNotExists.xml` - If applicable

2. **Test Structure**:
   ```xml
   <!-- Test changesets FIRST -->
   <changeSet id="1" author="test-harness">
       <snowflake:changeType .../>
   </changeSet>
   
   <!-- Test-specific cleanup -->
   <changeSet id="cleanup" author="test-harness" runAlways="true">
       <sql>DROP ... IF EXISTS ... CASCADE;</sql>
   </changeSet>
   
   <!-- Global cleanup at END -->
   <include file="liquibase/harness/change/changelogs/snowflake/cleanup.xml"/>
   ```

3. **Expected SQL**: Copy from test output, remove semicolons
4. **Expected Snapshot**: Create JSON with expected objects
5. **Run Test**: May need manual DB cleanup first

### Phase 7: Documentation and Commit
1. Update test results in SNOWFLAKE_TEST_RESULTS_3.md
2. Commit working implementation
3. Document any new patterns discovered

## Master Implementation Plan

### Object Types and Required Change Types

**IMPORTANT PRINCIPLE**: Only create change types that directly map to database SQL commands. If the database uses attributes/clauses (e.g., CREATE OR REPLACE, IF NOT EXISTS), these should be boolean attributes on the base change type, not separate change types.

#### DDL Change Types (Data Definition Language)
Every new database object typically needs:
- **create<Object>**: Maps to CREATE <OBJECT> SQL command
- **drop<Object>**: Maps to DROP <OBJECT> SQL command  
- **alter<Object>**: Maps to ALTER <OBJECT> SQL command (if supported by database)
- **rename<Object>**: Maps to RENAME <OBJECT> SQL command (if exists as separate command)

#### DML Change Types (Data Manipulation Language)
Liquibase focuses primarily on DDL, but some DML changes include:
- **insert**: Maps to INSERT statements
- **update**: Maps to UPDATE statements
- **delete**: Maps to DELETE statements
- **loadData**: Bulk data loading from CSV/other formats
- **loadUpdateData**: Upsert operations

#### Attribute vs Change Type Decision
- If SQL syntax is `CREATE OR REPLACE SCHEMA ...` → Add `orReplace` boolean attribute to createSchema
- If SQL syntax is `CREATE SCHEMA IF NOT EXISTS ...` → Add `ifNotExists` boolean attribute to createSchema
- If SQL syntax is `CREATE OR REPLACE SCHEMA ...` as a completely different command → Create separate change type (rare)

### SCHEMA Object (3 actual change types) - Use NEW_CHANGETYPE_PATTERN_2
- [x] createSchema - COMPLETED (includes orReplace, ifNotExists attributes)
- [x] dropSchema - COMPLETED (verify test harness)
- [ ] alterSchema

**Note**: createSchema handles OR REPLACE and IF NOT EXISTS via attributes, requiring separate test files due to mutual exclusivity

### DATABASE Object (3 actual change types) - Use NEW_CHANGETYPE_PATTERN_2
- [ ] createDatabase (will include orReplace attribute if supported)
- [ ] dropDatabase
- [ ] alterDatabase

### WAREHOUSE Object (3 actual change types) - Use NEW_CHANGETYPE_PATTERN_2
- [ ] createWarehouse (will include ifNotExists, orReplace, resourceMonitor attributes)
- [ ] alterWarehouse
- [ ] dropWarehouse

**Note**: Different warehouse configurations (size, type, resource monitor) are attributes, not separate change types

### TABLE Object (1 enhanced change type) - Use NAMESPACE_ATTRIBUTE_PATTERN_2
- [ ] createTable (enhance with Snowflake-specific attributes like TRANSIENT, CLUSTER BY)
  - Note: May require multiple test files for different attribute combinations

### SEQUENCE Object (3 change types) - Mixed Approach
- [ ] createSequence (verify ORDER support) - Use NAMESPACE_ATTRIBUTE_PATTERN_2
- [ ] createSequenceEnhanced - Use NEW_CHANGETYPE_PATTERN_2
- [ ] alterSequence - Use NEW_CHANGETYPE_PATTERN_2

## Guide Selection Decision Tree

### When to use NEW_CHANGETYPE_PATTERN_2:
1. The change type doesn't exist in standard Liquibase (e.g., createWarehouse)
2. You're creating a Snowflake-specific object type
3. The change needs completely custom SQL generation
4. Examples: All WAREHOUSE changes, All DATABASE changes, Most SCHEMA changes

### When to use NAMESPACE_ATTRIBUTE_PATTERN_2:
1. The change type exists in standard Liquibase (e.g., createTable)
2. You're adding Snowflake-specific attributes to existing functionality
3. The base SQL is correct but needs modifications (e.g., adding TRANSIENT)
4. Examples: createTable enhancements, createSequence ORDER support

### Quick Decision:
- Does Liquibase already have this change type? 
  - YES → Use NAMESPACE_ATTRIBUTE_PATTERN_2
  - NO → Use NEW_CHANGETYPE_PATTERN_2

## Execution Instructions

For each change type in the master plan:

1. **Start Todo**: Create todo item for the change type
2. **Requirements**: Ensure requirements document exists
3. **Check Existing**: Verify if already implemented
4. **Implement**: Follow phases 3-4 if needed
5. **Test Harness**: Follow phases 5-6
6. **Document**: Update results and commit
7. **Complete Todo**: Mark as done, move to next

## Patterns from Other Database Extensions

### Common Change Type Patterns

1. **MongoDB Extension**:
   - Maps to MongoDB commands: createCollection, dropCollection (MongoDB doesn't have "tables")
   - createIndex, dropIndex (direct command mapping)
   - insertMany (maps to MongoDB's insertMany command)

2. **Cassandra Extension**:
   - createKeyspace, dropKeyspace (Cassandra's equivalent of database/schema)
   - createTable with Cassandra-specific attributes
   - Uses attributes for options like replication strategy

3. **Percona Extension**:
   - Enhances existing change types with Percona toolkit integration
   - Doesn't create new SQL commands, wraps existing ones with pt-online-schema-change

4. **Neo4j Extension**:
   - Maps to Cypher commands: CREATE, MATCH, DELETE
   - runCypher for arbitrary Cypher queries (like sql change type)

### Key Principles for Snowflake

1. **Map to SQL Commands**: Only create change types that map to actual Snowflake SQL commands
2. **Use Attributes for Options**: OR REPLACE, IF NOT EXISTS, TRANSIENT etc. are attributes, not separate change types
3. **Follow Database Terminology**: Use Snowflake's terms (WAREHOUSE not CLUSTER, SCHEMA not NAMESPACE)
4. **DDL Focus**: Prioritize DDL operations; DML is secondary in Liquibase
5. **Maintenance Operations**: Consider operational commands (e.g., ALTER WAREHOUSE RESUME)

## Special Considerations

1. **Test Harness Timing**: Remember that updateSql runs BEFORE update
2. **Manual Cleanup**: May need to manually clean database between runs
3. **Mutual Exclusivity**: Always create separate test files for incompatible features
4. **Boolean Attributes**: Test both true and false explicitly
5. **SQL Generation**: Snowflake uses uppercase identifiers by default
6. **Test File Naming**: Each test file tests one change type, but may have multiple changesets

## Success Criteria

Each change type is complete when:
- [ ] Requirements documented
- [ ] All unit tests passing
- [ ] Service registration working
- [ ] Test harness tests passing
- [ ] Results documented
- [ ] Code committed

## Getting Started

Begin with:
```bash
# Set up environment
cd liquibase-snowflake

# Start with next item in plan
# For example: createOrReplaceSchema
```

Then systematically work through each phase for that change type.

---

## Recommended User Prompt

For best results, use this prompt format:

```
I'd like you to implement the new database objects (DATABASE, SCHEMA, WAREHOUSE) and enhance these database objects (TABLE, SEQUENCE) for the Snowflake database extension located in liquibase/liquibase-snowflake and implement the corresponding test harness tests.

Please follow the instructions in the claude_guide/SNOWFLAKE_CHANGETYPE_IMPLEMENTATION_PROMPT.md file and all referenced guides to accomplish this work.

Key requirements:
1. Continually update the SNOWFLAKE_IMPLEMENTATION_PROJECT_PLAN.md as you complete each substep
2. Use the TodoWrite tool to track your progress
3. Create detailed work logs showing what you're doing
4. Ask for clarification if anything is unclear
5. Test everything thoroughly before moving on

Start by reviewing the project plan to identify the next pending task.
```

## Working Process

### For Each Change Type:
1. **Update Project Plan**: Mark as "In Progress" with timestamp
2. **Create Todo**: Use TodoWrite tool to track subtasks
3. **Follow Guides**: Use appropriate pattern guide based on change type
4. **Update Progress**: After each major step, update project plan
5. **Test Thoroughly**: Unit tests → Integration tests → Test harness
6. **Document Issues**: Note any blockers or decisions in project plan
7. **Mark Complete**: Update status and move to next task

### Transparency Requirements:
- Show all file paths being modified
- Display test output (pass/fail)
- Explain design decisions
- Ask for help when blocked
- Update project plan after EVERY major step

### Quality Requirements:
- All tests must pass
- No hardcoded workarounds
- Follow existing code patterns
- Proper error handling
- Clear commit messages