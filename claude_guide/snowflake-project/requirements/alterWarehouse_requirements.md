# AlterWarehouse Detailed Requirements

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/alter-warehouse
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Basic Syntax
```sql
-- Rename warehouse
ALTER WAREHOUSE [ IF EXISTS ] <name> RENAME TO <new_warehouse_name>;

-- Set/unset properties
ALTER WAREHOUSE [ IF EXISTS ] <name> SET
  [ WAREHOUSE_SIZE = { XSMALL | SMALL | MEDIUM | LARGE | XLARGE | XXLARGE | XXXLARGE | X4LARGE | X5LARGE | X6LARGE } ]
  [ MAX_CLUSTER_COUNT = <num> ]
  [ MIN_CLUSTER_COUNT = <num> ]
  [ SCALING_POLICY = { STANDARD | ECONOMY } ]
  [ AUTO_SUSPEND = <num> | NULL ]
  [ AUTO_RESUME = { TRUE | FALSE } ]
  [ RESOURCE_MONITOR = <monitor_name> ]
  [ COMMENT = '<string_literal>' ]
  [ ENABLE_QUERY_ACCELERATION = { TRUE | FALSE } ]
  [ QUERY_ACCELERATION_MAX_SCALE_FACTOR = <num> ]
  [ TAG <tag_name> = '<tag_value>' [ , <tag_name> = '<tag_value>' , ... ] ];

ALTER WAREHOUSE [ IF EXISTS ] <name> UNSET
  [ RESOURCE_MONITOR ]
  [ COMMENT ]
  [ TAG <tag_name> [ , <tag_name> , ... ] ];

-- Suspend/Resume operations
ALTER WAREHOUSE [ IF EXISTS ] <name> SUSPEND;
ALTER WAREHOUSE [ IF EXISTS ] <name> RESUME [ IF SUSPENDED ];

-- Abort all queries
ALTER WAREHOUSE <name> ABORT ALL QUERIES;
```

## 2. Attribute Analysis

| Attribute | Description | Data Type | Default | Valid Values | Required |
|-----------|-------------|-----------|---------|--------------|----------|
| warehouseName | Name of the warehouse to alter | String | - | Valid Snowflake identifier | Yes |
| newName | New name for warehouse (rename) | String | null | Valid Snowflake identifier | No |
| ifExists | Only alter if warehouse exists | Boolean | false | true/false | No |
| setWarehouseSize | Set warehouse size | String | null | XSMALL, SMALL, MEDIUM, LARGE, XLARGE, XXLARGE, XXXLARGE, X4LARGE, X5LARGE, X6LARGE | No |
| setMaxClusterCount | Set max clusters | Integer | null | 1-10 | No |
| setMinClusterCount | Set min clusters | Integer | null | 1-10 | No |
| setScalingPolicy | Set scaling policy | String | null | STANDARD, ECONOMY | No |
| setAutoSuspend | Set auto-suspend seconds | Integer | null | 0 or 60+ (NULL = never) | No |
| setAutoResume | Set auto-resume | Boolean | null | true/false | No |
| setResourceMonitor | Set resource monitor | String | null | Valid monitor name | No |
| setComment | Set comment | String | null | String up to 256 chars | No |
| setEnableQueryAcceleration | Set query acceleration | Boolean | null | true/false | No |
| setQueryAccelerationMaxScaleFactor | Set acceleration scale | Integer | null | 0-100 | No |
| unsetResourceMonitor | Remove resource monitor | Boolean | false | true/false | No |
| unsetComment | Remove comment | Boolean | false | true/false | No |
| suspend | Suspend warehouse | Boolean | false | true/false | No |
| resume | Resume warehouse | Boolean | false | true/false | No |
| resumeIfSuspended | Only resume if suspended | Boolean | false | true/false | No |
| abortAllQueries | Abort all running queries | Boolean | false | true/false | No |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Operations
1. **Different operation types cannot be combined**:
   - RENAME TO
   - SET properties
   - UNSET properties
   - SUSPEND
   - RESUME
   - ABORT ALL QUERIES

2. **Within SET operations**:
   - minClusterCount must be <= maxClusterCount
   - queryAccelerationMaxScaleFactor only valid if enableQueryAcceleration = true

3. **SUSPEND vs RESUME**:
   - Cannot suspend and resume in same statement
   - These are separate operations

4. **SET vs UNSET**:
   - Cannot SET and UNSET same property

### Operation Groups
1. **Rename Operation**: Uses only warehouseName, newName, ifExists
2. **Property Operations**: Uses SET/UNSET attributes
3. **State Operations**: SUSPEND, RESUME
4. **Query Operations**: ABORT ALL QUERIES

## 4. SQL Examples for Testing

### Example 1: Rename Warehouse
```sql
ALTER WAREHOUSE old_warehouse RENAME TO new_warehouse;
```

### Example 2: Resize Warehouse
```sql
ALTER WAREHOUSE my_warehouse SET
  WAREHOUSE_SIZE = LARGE
  AUTO_SUSPEND = 300;
```

### Example 3: Configure Multi-cluster
```sql
ALTER WAREHOUSE my_warehouse SET
  MIN_CLUSTER_COUNT = 2
  MAX_CLUSTER_COUNT = 5
  SCALING_POLICY = STANDARD;
```

### Example 4: Suspend Warehouse
```sql
ALTER WAREHOUSE my_warehouse SUSPEND;
```

### Example 5: Resume Warehouse
```sql
ALTER WAREHOUSE my_warehouse RESUME IF SUSPENDED;
```

### Example 6: Remove Resource Monitor
```sql
ALTER WAREHOUSE my_warehouse UNSET RESOURCE_MONITOR;
```

### Example 7: Abort Queries
```sql
ALTER WAREHOUSE my_warehouse ABORT ALL QUERIES;
```

## 5. Test Scenarios

Based on the operation types:
1. **alterWarehouseRename.xml** - Rename operations
2. **alterWarehouseProperties.xml** - SET/UNSET property operations
3. **alterWarehouseState.xml** - SUSPEND/RESUME operations
4. **alterWarehouseQueries.xml** - ABORT ALL QUERIES operations

## 6. Validation Rules

1. **Required Attributes**:
   - warehouseName cannot be null or empty
   - For rename: newName cannot be null or empty

2. **Operation Exclusivity**:
   - Only one operation type per statement
   - Cannot combine RENAME, SET, UNSET, SUSPEND, RESUME, or ABORT

3. **Value Constraints**:
   - warehouseSize must be valid size
   - minClusterCount must be >= 1 and <= maxClusterCount
   - maxClusterCount must be >= minClusterCount and <= 10
   - autoSuspend must be >= 60 or 0/NULL
   - queryAccelerationMaxScaleFactor must be 0-100

4. **State Operations**:
   - SUSPEND on already suspended warehouse succeeds
   - RESUME on running warehouse succeeds (unless IF SUSPENDED)

## 7. Expected Behaviors

1. **Rename behavior**:
   - Warehouse must be suspended first
   - All references updated
   - Grants preserved

2. **Size changes**:
   - Requires warehouse restart
   - Active queries may be interrupted
   - New queries use new size

3. **Auto-suspend changes**:
   - Takes effect immediately
   - Timer starts from change time

4. **Suspend behavior**:
   - All running queries terminated
   - No new queries accepted
   - Billing stops

5. **Resume behavior**:
   - Warehouse starts up
   - Accepts new queries
   - Billing resumes

6. **Abort queries**:
   - All running queries terminated
   - Warehouse remains running

## 8. Error Conditions

1. Warehouse doesn't exist (without IF EXISTS)
2. New name already exists (for rename)
3. Invalid property values
4. Insufficient privileges
5. Resource monitor doesn't exist
6. Edition limitations (multi-cluster, query acceleration)
7. Cannot rename running warehouse

## 9. Implementation Notes

- Warehouse names are automatically converted to uppercase unless quoted
- Size changes require warehouse restart (brief interruption)
- SUSPEND/RESUME are immediate operations
- ABORT ALL QUERIES affects all sessions using the warehouse
- Consider impact on active workloads when altering
- Multi-cluster and query acceleration require Enterprise Edition