# ALTER TABLE Namespace Enhancement - Updated Requirements & Scope Analysis

## Executive Summary

**GOAL**: Add Snowflake-specific namespace attributes to existing Liquibase ALTER TABLE change types, allowing users to combine standard table modifications with Snowflake-specific features in a single operation.

**APPROACH**: Enhance existing change types with `snowflake:` namespace attributes rather than creating new change types.

---

## 1. Requirements Clarification

### Original Misunderstanding
I initially implemented `alterTableCluster` as a new change type because I thought Liquibase had no `alterTable` operation. This was incorrect.

### Correct Understanding  
Liquibase has many change types that generate `ALTER TABLE` statements. These should be enhanced with Snowflake namespace attributes.

### Target User Experience
```xml
<changeSet id="1" author="dev">
    <!-- Add column AND set clustering in one logical operation -->
    <addColumn tableName="sales_data" snowflake:clusterBy="region, new_col">
        <column name="new_col" type="varchar(50)"/>
    </addColumn>
    
    <!-- Modify column AND update time travel settings -->
    <modifyDataType tableName="sales_data" 
                    columnName="amount" 
                    newDataType="decimal(15,2)"
                    snowflake:setDataRetentionTimeInDays="60"/>
    
    <!-- Drop column AND update schema evolution -->
    <dropColumn tableName="sales_data" 
                columnName="old_col"
                snowflake:setEnableSchemaEvolution="true"/>
</changeSet>
```

---

## 2. Scope Analysis: How Widespread Will This Work Be?

### 2.1 Target Change Types (HIGH PRIORITY)
**ALTER TABLE changes that users commonly combine with Snowflake features:**

| Change Type | SQL Generated | Snowflake Enhancement Value | Priority |
|-------------|--------------|----------------------------|----------|
| `addColumn` | `ALTER TABLE ... ADD COLUMN` | High - often want to recluster after adding columns | **HIGH** |
| `dropColumn` | `ALTER TABLE ... DROP COLUMN` | High - often want to recluster after dropping columns | **HIGH** |  
| `modifyDataType` | `ALTER TABLE ... ALTER COLUMN` | Medium - may want to adjust retention/tracking | **MEDIUM** |
| `renameColumn` | `ALTER TABLE ... RENAME COLUMN` | Medium - may want to recluster | **MEDIUM** |
| `addPrimaryKey` | `ALTER TABLE ... ADD CONSTRAINT` | High - primary keys affect clustering | **HIGH** |
| `dropPrimaryKey` | `ALTER TABLE ... DROP CONSTRAINT` | High - may need to recluster | **HIGH** |

### 2.2 Target Change Types (MEDIUM PRIORITY)
| Change Type | SQL Generated | Enhancement Value | Priority |
|-------------|--------------|------------------|----------|
| `addUniqueConstraint` | `ALTER TABLE ... ADD CONSTRAINT` | Medium | **MEDIUM** |
| `dropUniqueConstraint` | `ALTER TABLE ... DROP CONSTRAINT` | Medium | **MEDIUM** |
| `addForeignKeyConstraint` | `ALTER TABLE ... ADD CONSTRAINT` | Low | **LOW** |
| `dropForeignKeyConstraint` | `ALTER TABLE ... DROP CONSTRAINT` | Low | **LOW** |

### 2.3 Out of Scope
| Change Type | Reason |
|-------------|--------|
| `addDefaultValue` | Minor operation, unlikely to need Snowflake features |
| `dropDefaultValue` | Minor operation, unlikely to need Snowflake features |
| `addNotNullConstraint` | Constraint-focused, less likely to need clustering |

---

## 3. Implementation Scope Breakdown

### 3.1 Core Implementation (6 Change Types)
**Estimated Effort: 3-4 hours per change type**

1. **addColumn** - Most important (users frequently add columns and want to recluster)
2. **dropColumn** - Second most important  
3. **modifyDataType** - Common operation
4. **addPrimaryKey** - Primary keys significantly impact performance
5. **dropPrimaryKey** - Often requires reclustering
6. **renameColumn** - May require clustering updates

**Total Core Effort: 18-24 hours**

### 3.2 Extended Implementation (4 Change Types) 
**Estimated Effort: 2-3 hours per change type**

7. **addUniqueConstraint**
8. **dropUniqueConstraint** 
9. **addForeignKeyConstraint**
10. **dropForeignKeyConstraint**

**Total Extended Effort: 8-12 hours**

### 3.3 What Each Implementation Requires

**Per Change Type:**
1. **Enhance SQL Generator** (~1-2 hours)
   - Modify existing `*GeneratorSnowflake` or create new one
   - Add namespace attribute processing
   - Generate additional ALTER TABLE statements

2. **Update XSD Schema** (~30 minutes)
   - Add Snowflake namespace attributes to existing elements

3. **Unit Tests** (~1-2 hours)
   - Test namespace attribute processing
   - Test SQL generation with attributes
   - Test validation rules

4. **Integration Testing** (~30 minutes)
   - Test with actual changelog files

**Infrastructure (One-time):**
- **Enhanced Namespace Processor** (~2-3 hours)
  - Extend `SnowflakeNamespaceAttributeStorage` for ALTER TABLE operations
  - Handle multiple ALTER TABLE statements per change

---

## 4. Recommended Implementation Plan

### Phase 1: Proof of Concept (4-6 hours)
- **Target**: `addColumn` only
- **Goal**: Validate the namespace enhancement approach
- **Deliverable**: Working `addColumn` with `snowflake:clusterBy` attribute

### Phase 2: Core Implementation (12-18 hours)
- **Targets**: `dropColumn`, `modifyDataType`, `addPrimaryKey`, `dropPrimaryKey`
- **Goal**: Cover 80% of user needs
- **Deliverable**: 5 enhanced change types with full test coverage

### Phase 3: Extended Implementation (8-12 hours)
- **Targets**: `renameColumn`, constraint-related changes
- **Goal**: Complete coverage of common scenarios
- **Deliverable**: 10 enhanced change types

---

## 5. Key Technical Challenges

### 5.1 Multiple ALTER TABLE Statements
**Challenge**: A single change with Snowflake attributes may generate multiple SQL statements.

**Example**:
```xml
<addColumn tableName="sales" snowflake:clusterBy="region, new_col">
    <column name="new_col" type="varchar(50)"/>
</addColumn>
```

**Generated SQL**:
```sql
ALTER TABLE sales ADD COLUMN new_col VARCHAR(50);
ALTER TABLE sales CLUSTER BY (region, new_col);
```

### 5.2 Attribute Validation
**Challenge**: Validate that Snowflake attributes are mutually exclusive and logically consistent.

### 5.3 Backwards Compatibility
**Challenge**: Ensure existing functionality is not affected.

---

## 6. Benefits vs. Current Approach

### Current Approach (New Change Types)
```xml
<addColumn tableName="sales"><column name="new_col" type="varchar(50)"/></addColumn>
<snowflake:alterTableCluster tableName="sales" clusterBy="region, new_col"/>
```

### Proposed Approach (Namespace Attributes)
```xml
<addColumn tableName="sales" snowflake:clusterBy="region, new_col">
    <column name="new_col" type="varchar(50)"/>
</addColumn>
```

**Benefits**:
- **More intuitive**: One logical operation = one change
- **Fewer changesets**: Reduces changelog verbosity
- **Better atomicity**: Related changes are grouped together
- **Consistent with createTable**: Follows established namespace pattern

---

## 7. Recommendation

**Proceed with Phase 1**: Implement `addColumn` enhancement as proof of concept.

**Rationale**:
- **Manageable scope**: 4-6 hours for full implementation
- **High user value**: `addColumn` + clustering is a common pattern
- **Validates approach**: Will reveal any architectural issues
- **Easy rollback**: Can revert to new change type approach if needed

**Success Criteria for Phase 1**:
- [ ] `addColumn` accepts `snowflake:clusterBy` attribute
- [ ] Generates correct multiple ALTER TABLE statements
- [ ] Full unit test coverage
- [ ] XSD validation works correctly
- [ ] User-friendly error messages for validation failures

Would you like me to proceed with Phase 1 implementation of `addColumn` enhancement?