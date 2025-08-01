# Query Column Mappings

Query Type: SHOW
Single Object Query: SHOW WAREHOUSES LIKE 'warehouse_name'
All Objects Query: SHOW WAREHOUSES

## Column Mappings:

Based on SHOW WAREHOUSES output columns:

- Column "name" -> property "name"
- Column "state" -> property "state"
- Column "type" -> property "type"
- Column "size" -> property "size"  
- Column "min_cluster_count" -> property "minClusterCount"
- Column "max_cluster_count" -> property "maxClusterCount"
- Column "started_clusters" -> property "startedClusters"
- Column "running" -> property "running"
- Column "queued" -> property "queued"
- Column "is_default" -> property "isDefault"
- Column "is_current" -> property "isCurrent"
- Column "auto_suspend" -> property "autoSuspend"
- Column "auto_resume" -> property "autoResume"
- Column "available" -> property "available"
- Column "provisioning" -> property "provisioning"
- Column "quiescing" -> property "quiescing"
- Column "other" -> property "other"
- Column "created_on" -> property "createdOn"
- Column "resumed_on" -> property "resumedOn"  
- Column "updated_on" -> property "updatedOn"
- Column "owner" -> property "owner"
- Column "comment" -> property "comment"
- Column "enable_query_acceleration" -> property "enableQueryAcceleration"
- Column "query_acceleration_max_scale_factor" -> property "queryAccelerationMaxScaleFactor"
- Column "resource_monitor" -> property "resourceMonitor"
- Column "scaling_policy" -> property "scalingPolicy"
- Column "owner_role_type" -> property "ownerRoleType"
- Column "resource_constraint" -> property "resourceConstraint"

## Query Examples:

### Single Warehouse Query:
```sql
SHOW WAREHOUSES LIKE 'MY_WAREHOUSE'
```

### All Warehouses Query:
```sql
SHOW WAREHOUSES
```

### Column Types:
- String columns: name, state, type, size, owner, comment, scaling_policy, owner_role_type, resource_constraint
- Integer columns: min_cluster_count, max_cluster_count, started_clusters, running, queued, auto_suspend, query_acceleration_max_scale_factor
- Boolean columns: is_default, is_current, auto_resume, enable_query_acceleration
- Float columns: available, provisioning, quiescing, other
- Timestamp columns: created_on, resumed_on, updated_on