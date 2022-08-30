package liquibase.diff.compare.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.util.StringUtil;

/**
 * DatabaseObjectComparator for Catalog and Schema comparators with common stuff
 */
public abstract class CommonCatalogSchemaComparator implements DatabaseObjectComparator {
    protected boolean equalsSchemas(Database accordingTo, String schemaName1, String schemaName2) {
        if (CatalogAndSchema.CatalogAndSchemaCase.ORIGINAL_CASE.equals(accordingTo.getSchemaAndCatalogCase())){
            return StringUtil.trimToEmpty(schemaName1).equals(StringUtil.trimToEmpty(schemaName2));
        } else {
            return StringUtil.trimToEmpty(schemaName1).equalsIgnoreCase(StringUtil.trimToEmpty(schemaName2));
        }
    }

    protected String getComparisonSchemaOrCatalog(Database accordingTo, CompareControl.SchemaComparison comparison) {
        if (accordingTo.supportsSchemas()) {
            return comparison.getComparisonSchema().getSchemaName();
        } else if (accordingTo.supportsCatalogs()) {
            return comparison.getComparisonSchema().getCatalogName();
        }
        return null;
    }

    protected String getReferenceSchemaOrCatalog(Database accordingTo, CompareControl.SchemaComparison comparison) {
        if (accordingTo.supportsSchemas()) {
            return comparison.getReferenceSchema().getSchemaName();
        } else if (accordingTo.supportsCatalogs()) {
            return comparison.getReferenceSchema().getCatalogName();
        }

        return null;
    }
}
