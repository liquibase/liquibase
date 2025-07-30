# Warehouse Requirements Specification

## Total Property Count: 28

## Properties Table
| Property Name | Data Type | Required | Default Value | Valid Values | Constraints |
|--------------|-----------|----------|---------------|--------------|-------------|
| name | String | Y | N/A | Valid identifier | Must be unique, no spaces |
| state | String | N | SUSPENDED | STARTED, SUSPENDED, RESIZING | Read-only state |
| type | String | N | STANDARD | STANDARD, SNOWPARK-OPTIMIZED | Determines warehouse type |
| size | String | N | XSMALL | XSMALL, SMALL, MEDIUM, LARGE, XLARGE, XXLARGE, XXXLARGE, X4LARGE, X5LARGE, X6LARGE | Compute capacity |
| min_cluster_count | Integer | N | 1 | 1 to max_cluster_count | Multi-cluster minimum |
| max_cluster_count | Integer | N | 1 | 1 to upper limit | Multi-cluster maximum |
| started_clusters | Integer | N | 0 | 0 to max_cluster_count | Read-only current clusters |
| running | Integer | N | 0 | 0+ | Read-only running queries |
| queued | Integer | N | 0 | 0+ | Read-only queued queries |
| is_default | Boolean | N | FALSE | TRUE, FALSE | Read-only default flag |
| is_current | Boolean | N | FALSE | TRUE, FALSE | Read-only current session flag |
| auto_suspend | Integer | N | 600 | 0+ or NULL | Seconds before suspension |
| auto_resume | Boolean | N | TRUE | TRUE, FALSE | Auto-resume on query |
| available | Float | N | 0.0 | 0.0-100.0 | Read-only % available resources |
| provisioning | Float | N | 0.0 | 0.0-100.0 | Read-only % provisioning resources |
| quiescing | Float | N | 0.0 | 0.0-100.0 | Read-only % quiescing resources |
| other | Float | N | 0.0 | 0.0-100.0 | Read-only % other state resources |
| created_on | Timestamp | N | Current time | Valid timestamp | Read-only creation date |
| resumed_on | Timestamp | N | NULL | Valid timestamp | Read-only last resume date |
| updated_on | Timestamp | N | Current time | Valid timestamp | Read-only last update date |
| owner | String | N | Current role | Valid role name | Read-only owning role |
| comment | String | N | NULL | Any string | Optional description |
| enable_query_acceleration | Boolean | N | FALSE | TRUE, FALSE | Query acceleration service |
| query_acceleration_max_scale_factor | Integer | N | 8 | 0-100 | Max acceleration scale |
| resource_monitor | String | N | NULL | Valid monitor name | Resource monitor ID |
| scaling_policy | String | N | STANDARD | STANDARD, ECONOMY | Cluster scaling behavior |
| owner_role_type | String | N | N/A | Role type | Read-only role type |
| resource_constraint | String | N | STANDARD_GEN_1 | STANDARD_GEN_1, STANDARD_GEN_2, MEMORY_1X, MEMORY_1X_x86, MEMORY_16X, MEMORY_16X_x86, MEMORY_64X, MEMORY_64X_x86 | Memory/CPU constraints |

## CREATE Syntax Documentation
```sql
CREATE [ OR REPLACE ] WAREHOUSE [ IF NOT EXISTS ] <name>
  [ WITH ]
    [ WAREHOUSE_TYPE = 'STANDARD' | 'SNOWPARK-OPTIMIZED' ]
    [ WAREHOUSE_SIZE = 'XSMALL' | 'SMALL' | 'MEDIUM' | 'LARGE' | 'XLARGE' | 'XXLARGE' | 'XXXLARGE' | 'X4LARGE' | 'X5LARGE' | 'X6LARGE' ]
    [ MAX_CLUSTER_COUNT = <num> ]
    [ MIN_CLUSTER_COUNT = <num> ]
    [ AUTO_SUSPEND = <num> | NULL ]
    [ AUTO_RESUME = TRUE | FALSE ]
    [ INITIALLY_SUSPENDED = TRUE | FALSE ]
    [ RESOURCE_MONITOR = <monitor_name> ]
    [ COMMENT = '<string_literal>' ]
    [ ENABLE_QUERY_ACCELERATION = TRUE | FALSE ]
    [ QUERY_ACCELERATION_MAX_SCALE_FACTOR = <num> ]
    [ RESOURCE_CONSTRAINT = '<constraint_name>' ]
    [ SCALING_POLICY = 'STANDARD' | 'ECONOMY' ]
```

## ALTER Syntax Documentation
```sql
-- Suspend/Resume warehouse
ALTER WAREHOUSE [ IF EXISTS ] <name> { SUSPEND | RESUME [ IF SUSPENDED ] }

-- Abort all queries
ALTER WAREHOUSE [ IF EXISTS ] <name> ABORT ALL QUERIES

-- Rename warehouse
ALTER WAREHOUSE [ IF EXISTS ] <name> RENAME TO <new_name>

-- Set properties
ALTER WAREHOUSE [ IF EXISTS ] <name> SET
  [ WAREHOUSE_SIZE = '<size>' ]
  [ MAX_CLUSTER_COUNT = <num> ]
  [ MIN_CLUSTER_COUNT = <num> ]
  [ AUTO_SUSPEND = <num> | NULL ]
  [ AUTO_RESUME = TRUE | FALSE ]
  [ RESOURCE_MONITOR = <monitor_name> ]
  [ COMMENT = '<string_literal>' ]
  [ ENABLE_QUERY_ACCELERATION = TRUE | FALSE ]
  [ QUERY_ACCELERATION_MAX_SCALE_FACTOR = <num> ]
  [ SCALING_POLICY = 'STANDARD' | 'ECONOMY' ]

-- Unset properties  
ALTER WAREHOUSE [ IF EXISTS ] <name> UNSET <property_name> [, <property_name> ... ]
```

## DROP Syntax Documentation
```sql
DROP WAREHOUSE [ IF EXISTS ] <name>
```

## SHOW/DESCRIBE Syntax Documentation
```sql
-- Show all warehouses
SHOW WAREHOUSES [ LIKE '<pattern>' ]

-- Describe specific warehouse
DESCRIBE WAREHOUSE <name>
DESC WAREHOUSE <name>
```

## Property Categories

### Required Properties
Count: 1
- name: Unique identifier for the warehouse, must be specified in CREATE statements

### Optional Configuration Properties  
Count: 12
- type: Warehouse compute type (STANDARD or SNOWPARK-OPTIMIZED)
- size: Compute capacity level (XSMALL to X6LARGE)
- min_cluster_count: Minimum number of clusters for multi-cluster warehouses
- max_cluster_count: Maximum number of clusters for multi-cluster warehouses
- auto_suspend: Seconds of inactivity before automatic suspension
- auto_resume: Whether warehouse automatically resumes on query submission
- resource_monitor: Resource monitor for tracking and controlling usage
- comment: Optional descriptive text for the warehouse
- enable_query_acceleration: Enable query acceleration service
- query_acceleration_max_scale_factor: Maximum scale factor for query acceleration
- scaling_policy: Cluster scaling behavior (STANDARD or ECONOMY)
- resource_constraint: Memory and CPU architecture constraints

### State Properties (Read-Only)
Count: 15
- state: Current operational status (STARTED, SUSPENDED, RESIZING)
- started_clusters: Number of currently active clusters
- running: Number of SQL statements currently executing
- queued: Number of SQL statements waiting in queue
- is_default: Whether this is the default warehouse for the account
- is_current: Whether this warehouse is in use for the current session
- available: Percentage of compute resources available
- provisioning: Percentage of resources being provisioned
- quiescing: Percentage of resources executing final queries before shutdown
- other: Percentage of resources in other operational states
- created_on: Timestamp when the warehouse was created
- resumed_on: Timestamp when the warehouse was last resumed
- updated_on: Timestamp when the warehouse was last modified
- owner: Role that owns the warehouse
- owner_role_type: Type of the role that owns the warehouse