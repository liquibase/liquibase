# CreateWarehouse Detailed Requirements

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/create-warehouse
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Basic Syntax
```sql
-- Minimal syntax
CREATE WAREHOUSE warehouse_name;

-- Full syntax with all options
CREATE [ OR REPLACE ] WAREHOUSE [ IF NOT EXISTS ] <name>
  [ WITH ]
  [ WAREHOUSE_SIZE = { XSMALL | SMALL | MEDIUM | LARGE | XLARGE | XXLARGE | XXXLARGE | X4LARGE | X5LARGE | X6LARGE } ]
  [ MAX_CLUSTER_COUNT = <num> ]
  [ MIN_CLUSTER_COUNT = <num> ]
  [ SCALING_POLICY = { STANDARD | ECONOMY } ]
  [ AUTO_SUSPEND = <num> | NULL ]
  [ AUTO_RESUME = { TRUE | FALSE } ]
  [ INITIALLY_SUSPENDED = { TRUE | FALSE } ]
  [ RESOURCE_MONITOR = <monitor_name> ]
  [ COMMENT = '<string_literal>' ]
  [ ENABLE_QUERY_ACCELERATION = { TRUE | FALSE } ]
  [ QUERY_ACCELERATION_MAX_SCALE_FACTOR = <num> ]
  [ WAREHOUSE_TYPE = { STANDARD | SNOWPARK-OPTIMIZED } ]
  [ [ WITH ] TAG ( <tag_name> = '<tag_value>' [ , <tag_name> = '<tag_value>' , ... ] ) ]
```

## 2. Attribute Analysis

| Attribute | Description | Data Type | Default | Valid Values | Required |
|-----------|-------------|-----------|---------|--------------|----------|
| warehouseName | Name of the warehouse to create | String | - | Valid Snowflake identifier | Yes |
| orReplace | Replace warehouse if it exists | Boolean | false | true/false | No |
| ifNotExists | Only create if warehouse doesn't exist | Boolean | false | true/false | No |
| warehouseSize | Size of the warehouse | String | XSMALL | XSMALL, SMALL, MEDIUM, LARGE, XLARGE, XXLARGE, XXXLARGE, X4LARGE, X5LARGE, X6LARGE | No |
| maxClusterCount | Maximum clusters for multi-cluster | Integer | 1 | 1-10 | No |
| minClusterCount | Minimum clusters for multi-cluster | Integer | 1 | 1-10 | No |
| scalingPolicy | How to scale clusters | String | STANDARD | STANDARD, ECONOMY | No |
| autoSuspend | Seconds before auto-suspend | Integer | 600 | 0 or 60+ (NULL = never) | No |
| autoResume | Auto-resume when queried | Boolean | true | true/false | No |
| initiallySuspended | Start in suspended state | Boolean | false | true/false | No |
| resourceMonitor | Resource monitor to assign | String | null | Valid monitor name | No |
| comment | Description of warehouse | String | null | String up to 256 chars | No |
| enableQueryAcceleration | Enable query acceleration service | Boolean | false | true/false | No |
| queryAccelerationMaxScaleFactor | Max scale factor for acceleration | Integer | 8 | 0-100 | No |
| warehouseType | Type of warehouse | String | STANDARD | STANDARD, SNOWPARK-OPTIMIZED | No |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. **orReplace** and **ifNotExists** - Cannot use both in same statement
   - `CREATE OR REPLACE WAREHOUSE` - Valid
   - `CREATE WAREHOUSE IF NOT EXISTS` - Valid
   - `CREATE OR REPLACE WAREHOUSE IF NOT EXISTS` - Invalid

2. **Multi-cluster settings** require appropriate edition
   - maxClusterCount > 1 requires Enterprise Edition or higher
   - Economy scaling policy requires Enterprise Edition or higher

3. **Query Acceleration** settings
   - enableQueryAcceleration requires Enterprise Edition or higher
   - queryAccelerationMaxScaleFactor only applies if enableQueryAcceleration = true

### Required Combinations
1. If **maxClusterCount** > 1, **minClusterCount** must be <= maxClusterCount
2. If **autoSuspend** is 0 or NULL, warehouse never suspends (higher cost)
3. If **initiallySuspended** is true, warehouse starts suspended regardless of autoResume

## 4. SQL Examples for Testing

### Example 1: Basic Warehouse
```sql
CREATE WAREHOUSE basic_warehouse;
```

### Example 2: Sized Warehouse with Auto-suspend
```sql
CREATE WAREHOUSE sized_warehouse
  WAREHOUSE_SIZE = MEDIUM
  AUTO_SUSPEND = 300
  AUTO_RESUME = TRUE;
```

### Example 3: Multi-cluster Warehouse
```sql
CREATE WAREHOUSE multi_cluster_warehouse
  WAREHOUSE_SIZE = LARGE
  MIN_CLUSTER_COUNT = 1
  MAX_CLUSTER_COUNT = 3
  SCALING_POLICY = STANDARD;
```

### Example 4: Initially Suspended Warehouse
```sql
CREATE WAREHOUSE suspended_warehouse
  INITIALLY_SUSPENDED = TRUE
  COMMENT = 'Start suspended to save costs';
```

### Example 5: Warehouse with Resource Monitor
```sql
CREATE WAREHOUSE monitored_warehouse
  RESOURCE_MONITOR = monthly_budget_monitor
  AUTO_SUSPEND = 60;
```

### Example 6: Query Acceleration Warehouse
```sql
CREATE WAREHOUSE accelerated_warehouse
  WAREHOUSE_SIZE = XLARGE
  ENABLE_QUERY_ACCELERATION = TRUE
  QUERY_ACCELERATION_MAX_SCALE_FACTOR = 10;
```

## 5. Test Scenarios

Based on the features and mutual exclusivity:
1. **createWarehouse.xml** - Basic creation, sizes, auto-suspend/resume
2. **createWarehouseIfNotExists.xml** - IF NOT EXISTS variations
3. **createOrReplaceWarehouse.xml** - OR REPLACE variations
4. **createWarehouseMultiCluster.xml** - Multi-cluster configurations
5. **createWarehouseWithResourceMonitor.xml** - Resource monitor assignment
6. **createWarehouseQueryAcceleration.xml** - Query acceleration features

## 6. Validation Rules

1. **Required Attributes**:
   - warehouseName cannot be null or empty
   - Must be valid Snowflake identifier

2. **Mutual Exclusivity**:
   - If orReplace=true and ifNotExists=true, throw: "Cannot use both OR REPLACE and IF NOT EXISTS"

3. **Value Constraints**:
   - warehouseSize must be valid size constant
   - minClusterCount must be >= 1 and <= maxClusterCount
   - maxClusterCount must be >= minClusterCount and <= 10
   - autoSuspend must be >= 60 or 0/NULL
   - queryAccelerationMaxScaleFactor must be 0-100

4. **Edition Requirements**:
   - Multi-cluster requires Enterprise Edition
   - Query acceleration requires Enterprise Edition

## 7. Expected Behaviors

1. **Default behavior**:
   - XSMALL size if not specified
   - Single cluster
   - Auto-suspend after 600 seconds (10 minutes)
   - Auto-resume enabled

2. **OR REPLACE behavior**:
   - Drops existing warehouse
   - Creates new warehouse with specified settings
   - Active queries are terminated

3. **IF NOT EXISTS behavior**:
   - Succeeds silently if warehouse exists
   - Does not modify existing warehouse

4. **Initially Suspended**:
   - Warehouse created but not started
   - No charges until first resumed

## 8. Error Conditions

1. Warehouse already exists (without OR REPLACE or IF NOT EXISTS)
2. Invalid warehouse name
3. Insufficient privileges
4. Resource monitor doesn't exist
5. Edition limitations (multi-cluster, query acceleration)
6. Invalid size or configuration values

## 9. Implementation Notes

- Warehouse names are automatically converted to uppercase unless quoted
- Warehouses are compute resources separate from storage
- Costs accrue only when warehouse is running
- Consider cost implications of always-on warehouses (autoSuspend = NULL)
- Size changes require warehouse restart