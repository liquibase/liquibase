# DropWarehouse Detailed Requirements

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/drop-warehouse
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Basic Syntax
```sql
-- Minimal syntax
DROP WAREHOUSE warehouse_name;

-- Full syntax with all options
DROP WAREHOUSE [ IF EXISTS ] <name>
```

## 2. Attribute Analysis

| Attribute | Description | Data Type | Default | Valid Values | Required |
|-----------|-------------|-----------|---------|--------------|----------|
| warehouseName | Name of the warehouse to drop | String | - | Valid Snowflake identifier | Yes |
| ifExists | Only drop if warehouse exists | Boolean | false | true/false | No |

## 3. Mutual Exclusivity Rules

### No Mutual Exclusivity
- DROP WAREHOUSE is a simple command with only IF EXISTS option
- No CASCADE/RESTRICT options like schemas/databases

## 4. SQL Examples for Testing

### Example 1: Basic Drop
```sql
DROP WAREHOUSE my_warehouse;
```

### Example 2: Drop If Exists
```sql
DROP WAREHOUSE IF EXISTS nonexistent_warehouse;
```

## 5. Test Scenarios

Based on the simplicity of the command:
1. **dropWarehouse.xml** - Basic drop and IF EXISTS variations

## 6. Validation Rules

1. **Required Attributes**:
   - warehouseName cannot be null or empty
   - Must be valid Snowflake identifier

2. **Operational Constraints**:
   - Cannot drop warehouse that is currently in use by active sessions
   - Warehouse must be suspended or drop will suspend it first

## 7. Expected Behaviors

1. **Basic drop**:
   - Warehouse is suspended if running
   - All active queries are terminated
   - Warehouse is removed from the system
   - Cannot be undropped

2. **IF EXISTS behavior**:
   - Succeeds silently if warehouse doesn't exist
   - No error thrown

3. **Active sessions**:
   - Sessions using the warehouse are interrupted
   - Queries fail with warehouse not found error

## 8. Error Conditions

1. Warehouse doesn't exist (without IF EXISTS)
2. Insufficient privileges (requires OWNERSHIP or higher)
3. System warehouses cannot be dropped

## 9. Implementation Notes

- Warehouse names are automatically converted to uppercase unless quoted
- Dropping a warehouse is immediate and cannot be rolled back
- No UNDROP available for warehouses
- Active queries are terminated immediately
- Consider impact on running workloads
- Resource monitor associations are automatically removed