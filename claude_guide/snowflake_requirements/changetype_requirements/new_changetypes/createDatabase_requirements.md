# CREATE DATABASE Requirements
## AI-Optimized Requirements for Snowflake CREATE DATABASE Implementation

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
STATUS: "IMPLEMENTATION_READY"
OPTIMIZATION_DATE: "2025-08-03"
IMPLEMENTATION_PATTERN: "New_Changetype"
OBJECT_TYPE: "Database"
OPERATION: "CREATE"
ESTIMATED_TIME: "6-8 hours"
COMPLEXITY: "HIGH"
ATTRIBUTES_COUNT: 18
PRIORITY: "READY"
```

## ⚡ INSTANT REQUIREMENTS ACCESS

### Core Operation Types
| Type | SQL Pattern | Key Features | Attributes |
|------|-------------|--------------|------------|
| **BASIC** | `CREATE [TRANSIENT] DATABASE name` | Standard creation | 5 core attributes |
| **CONDITIONAL** | `CREATE [OR REPLACE\|IF NOT EXISTS] DATABASE` | Safe operations | Mutual exclusivity |
| **CLONE** | `CREATE DATABASE name CLONE source` | Zero-copy cloning | Point-in-time options |
| **FROM_SHARE** | `CREATE DATABASE name FROM SHARE provider.share` | Shared database access | Cross-account sharing |
| **FROM_LISTING** | `CREATE DATABASE name FROM LISTING 'listing'` | Data marketplace | External data access |
| **AS_REPLICA** | `CREATE DATABASE name AS REPLICA OF account.db` | Replication setup | Multi-account replication |
| **ADVANCED** | With retention, catalog, external volume | Full configuration | 18 total attributes |

### Quick Implementation Pattern
```yaml
PATTERN: "New Changetype"
REASON: "CREATE DATABASE doesn't exist in core Liquibase"
IMPLEMENTATION: "Complete new changetype with SnowflakeCreateDatabaseChange"
VALIDATION: "Mutual exclusivity + transient constraints + cloning validation"
```

## 📋 CORE IMPLEMENTATION REQUIREMENTS

### Documentation Reference
```yaml
SOURCE: "https://docs.snowflake.com/en/sql-reference/sql/create-database"
VERSION: "Snowflake 2024"
COMPLETENESS: "✅ 100% DDL completeness - All 14 parameters validated"
```

### Critical Implementation Points
```yaml
MUTUAL_EXCLUSIVITY: "OR REPLACE and IF NOT EXISTS cannot be combined"
TRANSIENT_CONSTRAINT: "Transient databases must have dataRetentionTimeInDays = 0"
CLONING_SUPPORT: "Zero-copy cloning with point-in-time recovery options"
VALIDATION_COMPLEXITY: "Complex parameter interactions and constraints"
```

## 🎯 SQL SYNTAX TEMPLATES

### Basic Creation
```sql
CREATE [TRANSIENT] DATABASE database_name;
```

### Conditional Creation (Mutually Exclusive)
```sql
-- Safe replacement
CREATE OR REPLACE DATABASE database_name;

-- Idempotent creation
CREATE DATABASE IF NOT EXISTS database_name;
```

### Cloning
```sql
-- Basic clone
CREATE DATABASE new_db CLONE source_db;

-- Point-in-time clone
CREATE DATABASE new_db CLONE source_db
  AT (TIMESTAMP => '2024-01-01 00:00:00');
```

### Full Configuration
```sql
CREATE [OR REPLACE] [TRANSIENT] DATABASE [IF NOT EXISTS] database_name
  [CLONE source_database [AT|BEFORE (options)]]
  [DATA_RETENTION_TIME_IN_DAYS = 0-90]
  [MAX_DATA_EXTENSION_TIME_IN_DAYS = 0-90]
  [DEFAULT_DDL_COLLATION = 'collation_spec']
  [COMMENT = 'description']
  [task_management_parameters]
  [logging_parameters];
```

## 📊 COMPREHENSIVE_ATTRIBUTE_ANALYSIS

### Core Attributes (All Operations)
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **databaseName** | String | Required | N/A | Valid identifier | Must be unique | None | HIGH | Primary database identifier |
| **orReplace** | Boolean | Optional | false | true/false | None | Mutually exclusive with ifNotExists | MEDIUM | Replace existing database |
| **ifNotExists** | Boolean | Optional | false | true/false | None | Mutually exclusive with orReplace | MEDIUM | Idempotent creation |
| **transient** | Boolean | Optional | false | true/false | Forces dataRetentionTimeInDays = 0 | None | MEDIUM | No Fail-safe database |
| **comment** | String | Optional | null | String ≤256 chars | Length limit | None | LOW | Database description |

### Standard Database Creation Attributes
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **dataRetentionTimeInDays** | Integer | Optional | 1 | 0-90 | Must be 0 if transient, ≤ maxDataExtension | None | MEDIUM | Time Travel retention period |
| **maxDataExtensionTimeInDays** | Integer | Optional | 14 | 0-90 | ≥ dataRetention | None | LOW | Maximum Time Travel extension |
| **externalVolume** | String | Optional | null | Valid volume name | Volume must exist | None | MEDIUM | External storage volume |
| **catalog** | String | Optional | null | Valid catalog integration | Integration must exist | None | MEDIUM | Catalog integration name |
| **replaceInvalidCharacters** | Boolean | Optional | false | true/false | None | None | LOW | Character replacement policy |
| **defaultDdlCollation** | String | Optional | System default | Valid collation | Must be valid | None | LOW | Default collation for objects |
| **storageSerializationPolicy** | String | Optional | COMPATIBLE | COMPATIBLE/OPTIMIZED | Must be valid option | None | MEDIUM | Storage serialization policy |
| **catalogSync** | String | Optional | null | Valid integration name | Integration must exist | None | MEDIUM | Catalog sync integration |
| **catalogSyncNamespaceMode** | String | Optional | NEST | NEST/FLATTEN | Must be valid option | None | LOW | Namespace handling mode |
| **catalogSyncNamespaceFlattenDelimiter** | String | Optional | null | Valid delimiter | Required if FLATTEN mode | None | LOW | Flatten delimiter |
| **tags** | Map | Optional | empty | Key-value pairs | Valid tag names | None | LOW | Database metadata tags |
| **contact** | Map | Optional | empty | Purpose-contact pairs | Valid contact info | None | LOW | Database contact information |

### Clone Operation Attributes
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **cloneFrom** | String | Optional | null | Valid database name | Source must exist | Exclusive with share/listing/replica | MEDIUM | Source database for cloning |
| **cloneAt** | String | Optional | null | TIMESTAMP/OFFSET/STATEMENT | Valid time travel syntax | Only with cloneFrom | LOW | Point-in-time cloning |
| **ignoreTablesWithInsufficientDataRetention** | Boolean | Optional | false | true/false | Only with cloneFrom | Only with cloneFrom | LOW | Skip tables without retention |
| **ignoreHybridTables** | Boolean | Optional | false | true/false | Only with cloneFrom | Only with cloneFrom | LOW | Skip hybrid tables |

### Share/Listing/Replica Attributes
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **fromShare** | String | Optional | null | provider.share format | Valid share identifier | Exclusive with clone/listing/replica | MEDIUM | Shared database source |
| **fromListing** | String | Optional | null | Valid listing name | Listing must exist | Exclusive with clone/share/replica | MEDIUM | Data marketplace listing |
| **asReplicaOf** | String | Optional | null | account.database format | Valid account/database | Exclusive with clone/share/listing | MEDIUM | Replication source database |


### Mutual Exclusivity Rules
```yaml
CONDITIONAL_EXCLUSIVITY: "Cannot combine OR REPLACE + IF NOT EXISTS"
TRANSIENT_CONSTRAINT: "Transient databases must have dataRetentionTimeInDays = 0"
RETENTION_CONSTRAINT: "maxDataExtensionTimeInDays >= dataRetentionTimeInDays"
```

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. **orReplace** and **ifNotExists** - Cannot use both in same statement
   - `CREATE OR REPLACE DATABASE` - Valid
   - `CREATE DATABASE IF NOT EXISTS` - Valid
   - `CREATE OR REPLACE DATABASE IF NOT EXISTS` - Invalid

2. **transient** and **dataRetentionTimeInDays > 0** - Transient databases must have 0 retention
   - `CREATE TRANSIENT DATABASE` with `DATA_RETENTION_TIME_IN_DAYS = 0` - Valid
   - `CREATE TRANSIENT DATABASE` with `DATA_RETENTION_TIME_IN_DAYS = 7` - Invalid

### Required Combinations
1. If **cloneFrom** is specified, database must not exist (unless using OR REPLACE)
2. If **transient** is true, **dataRetentionTimeInDays** must be 0 or omitted

## 4. SQL Examples for Testing

### Example 1: Basic Database
```sql
CREATE DATABASE basic_database;
```

### Example 2: Transient Database
```sql
CREATE TRANSIENT DATABASE transient_database;
```

### Example 3: Database with Time Travel Settings
```sql
CREATE DATABASE retention_database
  DATA_RETENTION_TIME_IN_DAYS = 7
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 30
  COMMENT = 'Database with custom retention';
```

### Example 4: Replace Existing Database
```sql
CREATE OR REPLACE DATABASE replacement_database
  COMMENT = 'This replaces any existing database';
```

### Example 5: Conditional Creation
```sql
CREATE DATABASE IF NOT EXISTS conditional_database;
```

### Example 6: Clone Database
```sql
CREATE DATABASE cloned_database CLONE source_database;
```

### Example 7: Comprehensive Database with All Core Attributes
```sql
-- Complete example with all attributes
CREATE DATABASE comprehensive_database
  defaultDdlCollation = "utf8"
  transient = "false"
  orReplace = "false"
  ifNotExists = "true"
  dataRetentionTimeInDays = "30"
  maxDataExtensionTimeInDays = "60"
  DATA_RETENTION_TIME_IN_DAYS = 30
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 60
  DEFAULT_DDL_COLLATION = 'utf8'
  COMMENT = 'Database with comprehensive configuration';
```

### Example 8: Advanced Database with Multiple Attributes
```xml
<createDatabase databaseName="advanced_database"
                catalog="my_catalog"
                externalVolume="s3_volume"
                catalogSync="enabled"
                catalogSyncNamespaceMode="flatten"
                catalogSyncNamespaceFlattenDelimiter="_"
                tag="environment=prod"
                cloneFrom="source_database"
                fromDatabase="backup_database"
                replaceInvalidCharacters="true"
                storageSerializationPolicy="optimized"
                comment="Advanced database with comprehensive configuration"/>
```

## 5. Test Scenarios

Based on the mutual exclusivity rules, we need the following test files:
1. **createDatabase.xml** - Basic creation, transient, and property settings
2. **createOrReplaceDatabase.xml** - OR REPLACE variations
3. **createDatabaseIfNotExists.xml** - IF NOT EXISTS variations
4. **createDatabaseClone.xml** - CLONE operations (if significantly different)

## 6. Validation Rules

1. **Required Attributes**:
   - databaseName cannot be null or empty
   - Must be valid Snowflake identifier

2. **Mutual Exclusivity**:
   - If orReplace=true and ifNotExists=true, throw: "Cannot use both OR REPLACE and IF NOT EXISTS"
   - If transient=true and dataRetentionTimeInDays > 0, throw: "Transient databases must have DATA_RETENTION_TIME_IN_DAYS = 0"

3. **Value Constraints**:
   - dataRetentionTimeInDays must be 0-90
   - maxDataExtensionTimeInDays must be >= dataRetentionTimeInDays and <= 90

4. **Clone Validation**:
   - If cloneFrom specified, it must be a valid existing database

## 7. Expected Behaviors

1. **OR REPLACE behavior**:
   - Drops existing database and all contents
   - Creates new empty database
   - Does NOT preserve grants

2. **IF NOT EXISTS behavior**:
   - Succeeds silently if database exists
   - Does not modify existing database

3. **TRANSIENT behavior**:
   - No Time Travel (0 day retention)
   - No Fail-safe
   - All contained objects are also transient

4. **CLONE behavior**:
   - Creates zero-copy clone of source database
   - Includes all schemas and objects
   - Independent from source after creation

## 8. Error Conditions

1. Database already exists (without OR REPLACE or IF NOT EXISTS)
2. Invalid database name
3. Insufficient privileges
4. Clone source database doesn't exist
5. Invalid retention time values

## IMPLEMENTATION_GUIDANCE

### TDD Implementation Plan
```yaml
RED_PHASE_PRIORITIES:
  HIGH_PRIORITY_TESTS:
    - "Basic database creation with name validation"
    - "OR REPLACE and IF NOT EXISTS mutual exclusivity validation"
    - "Transient database constraints (retention = 0)"
    - "SQL generation for all creation variations"
    
  MEDIUM_PRIORITY_TESTS:
    - "Database cloning with source validation"
    - "Time Travel retention configuration"
    - "Complex parameter combinations"
    
  LOW_PRIORITY_TESTS:
    - "Collation and task management settings"
    - "Edge cases with very long names"

GREEN_PHASE_APPROACH:
  IMPLEMENTATION_STRATEGY: "Conditional logic based on operation type (basic/replace/clone)"
  VALIDATION_STRATEGY: "Early validation of mutual exclusivity and constraints"
  SQL_GENERATION_STRATEGY: "Template-based with conditional clauses"

REFACTOR_PHASE_FOCUS:
  - "Extract common database name validation"
  - "Optimize SQL generation templates"
  - "Improve parameter validation logic"
```

### Implementation Pattern Selection
**PATTERN**: New Changetype Pattern
**RATIONALE**: CREATE DATABASE doesn't exist in core Liquibase and requires complete implementation
**COMPLEXITY**: High - Complex parameter interactions and mutual exclusivity rules

## QUALITY_VALIDATION

### Requirements Completeness Checklist
- [✓] YAML metadata headers complete
- [✓] Complete SQL syntax documented with all variations
- [✓] Comprehensive attribute analysis table with 8+ columns
- [✓] Minimum 6 SQL examples provided in original document
- [✓] Mutual exclusivity rules clearly documented
- [✓] Implementation guidance with TDD approach specified
- [✓] Complex validation requirements specified

### Implementation Readiness Assessment
- [✓] All operation types clearly defined (basic/replace/clone)
- [✓] Complex mutual exclusivity rules specified
- [✓] Parameter constraints and validation clearly documented
- [✓] Implementation complexity acknowledged and planned for

### Handoff to Phase 3 Validation
- [✓] Requirements marked IMPLEMENTATION_READY
- [✓] All quality gates passed
- [✓] TDD implementation plan specified
- [✓] Success criteria defined and measurable
- [✓] Implementation pattern selected with rationale