package liquibase.harness.util

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.change.Change
import liquibase.changelog.ChangeSet
import liquibase.database.Database
import liquibase.resource.ResourceAccessor
import liquibase.sql.Sql
import liquibase.sqlgenerator.SqlGeneratorFactory
import liquibase.test.JUnitResourceAccessor

class TestUtils {

    static Liquibase createLiquibase(String changeLogFile,  Database database) {
        ResourceAccessor fileOpener = new JUnitResourceAccessor();
        database.resetInternalState();
        return new Liquibase(changeLogFile, fileOpener, database);
    }

    static List<String> toSql(Change change, Database db) {
        Sql[] sqls = SqlGeneratorFactory.newInstance().generateSql(change, db)
        return sqls*.toSql()
    }

    static List<String> toSql(ChangeSet changeSet, Database db) {
        return toSql(changeSet.getChanges(), db)
    }

    static List<String> toSql(List<? extends Change> changes, Database db) {
        List<String> stringList = new ArrayList<>()
        changes.each { stringList.addAll(toSql(it, db)) }
        return stringList
    }

    static List<String> toSqlFromChangeSets(List<ChangeSet> changeSets, Database db) {
        List<String> stringList = new ArrayList<>()
        changeSets.each { stringList.addAll(toSql(it, db)) }
        return stringList
    }


    static ArrayList<CatalogAndSchema> getCatalogAndSchema(List<String> schemaList, Database database) {
        List<CatalogAndSchema> finalList = new ArrayList<>()
        schemaList?.each { sch ->
            String[] catSchema = sch.split("\\.")
            String catalog, schema
            if (catSchema.length == 2) {
                catalog = catSchema[0]?.trim()
                schema = catSchema[1]?.trim()
            } else if (catSchema.length == 1) {
                catalog = null
                schema = catSchema[0]?.trim()
            } else {
                return finalList
            }
            finalList.add(new CatalogAndSchema(catalog, schema).customize(database))
        }

        return finalList
    }

    static ArrayList<String> collectValuesForDb(Object value, String dbms, String splitWith = null) {
        List<String> returnList = new ArrayList<>()
        if (!value) {
            return returnList
        }
        returnList.addAll(splitAndTrimIfNeeded(value, splitWith))
        return returnList
    }

    private static List<String> splitAndTrimIfNeeded(String str, String regex = null) {
        List<String> returnList = new ArrayList<>()
        if (str) {
            if (regex == null) {
                returnList.add(str)
                return returnList
            }
            return str?.split(regex)*.trim()
        }
        return new ArrayList<String>()
    }

    private static Collection<String> splitAndTrimIfNeeded(Collection<String> strs, String regex = null) {
        if (regex == null) {
            return strs
        }
        List<String> returnList = new ArrayList<>()
        strs.each { str ->
            if (str) {
                returnList.add(str.split(regex)*.trim())
            }
        }
        return returnList
    }
}
