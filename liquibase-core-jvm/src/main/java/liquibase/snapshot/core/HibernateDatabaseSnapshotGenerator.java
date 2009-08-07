package liquibase.snapshot.core;

import liquibase.database.Database;
import liquibase.database.core.HibernateDatabase;
import liquibase.database.core.HibernateGenericDialect;
import liquibase.database.structure.*;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.util.StringUtils;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.Mapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class HibernateDatabaseSnapshotGenerator implements DatabaseSnapshotGenerator {
    private HibernateDatabase database;

    public HibernateDatabaseSnapshotGenerator() {
    }

    public DatabaseSnapshot createSnapshot(Database passed, String schema, Set<DiffStatusListener> listeners) throws DatabaseException {
        HibernateDatabase database = (HibernateDatabase) passed;
        try {
            Configuration cfg = database.createConfiguration();
            cfg.configure(database.getConfigFile());
            this.database = database;

//            Dialect dialect = (Dialect) Class.forName(cfg.getProperty("dialect")).newInstance();
//            dialect.
            Dialect dialect = new HibernateGenericDialect(cfg);
            cfg.buildMappings();
            Mapping mapping = cfg.buildMapping();

            DatabaseSnapshot snapshot = new DatabaseSnapshot(database, schema);

//            Dialect dialect = new MySQL5InnoDBDialect();

            Iterator tableMappings = cfg.getTableMappings();
            while (tableMappings.hasNext()) {
                org.hibernate.mapping.Table hibernateTable = (org.hibernate.mapping.Table) tableMappings.next();
                if (hibernateTable.isPhysicalTable()) {
                    Table table = new Table(hibernateTable.getName());
                    snapshot.getTables().add(table);
                    System.out.println("seen table " + table.getName());

                    Iterator columnIterator = hibernateTable.getColumnIterator();
                    while (columnIterator.hasNext()) {
                        org.hibernate.mapping.Column hibernateColumn = (org.hibernate.mapping.Column) columnIterator.next();
                        Column column = new Column();
                        column.setName(hibernateColumn.getName());
//todo:                        column.setAutoIncrement(hibernateColumn.);
                        column.setDataType(hibernateColumn.getSqlTypeCode(mapping));
                        if (column.isNumeric()) {
                            column.setColumnSize(hibernateColumn.getPrecision());
                        } else {
                            column.setColumnSize(hibernateColumn.getLength());
                        }
                        column.setDecimalDigits(hibernateColumn.getScale());
                        column.setDefaultValue(hibernateColumn.getDefaultValue());
                        column.setNullable(hibernateColumn.isNullable());
                        column.setPrimaryKey(isPrimaryKey(hibernateTable, hibernateColumn));
                        column.setTable(table);
                        column.setTypeName(hibernateColumn.getSqlType(dialect, mapping).replaceFirst("\\(.*\\)", ""));
                        column.setUnique(hibernateColumn.isUnique());
                        column.setCertainDataType(false);

                        table.getColumns().add(column);
                    }

                    Iterator indexIterator = hibernateTable.getIndexIterator();
                    while (indexIterator.hasNext()) {
                        org.hibernate.mapping.Index hibernateIndex = (org.hibernate.mapping.Index) indexIterator.next();
                        Index index = new Index();
                        index.setTable(table);
                        index.setName(hibernateIndex.getName());
                        columnIterator = hibernateIndex.getColumnIterator();
                        while (columnIterator.hasNext()) {
                            org.hibernate.mapping.Column hibernateColumn = (org.hibernate.mapping.Column) columnIterator.next();
                            index.getColumns().add(hibernateColumn.getName());
                        }

                        snapshot.getIndexes().add(index);
                    }

                    Iterator uniqueIterator = hibernateTable.getUniqueKeyIterator();
                    while (uniqueIterator.hasNext()) {
                        org.hibernate.mapping.UniqueKey hiberateUnique = (org.hibernate.mapping.UniqueKey) uniqueIterator.next();

                        Index index = new Index();
                        index.setTable(table);
                        index.setName(hiberateUnique.getName());
                        columnIterator = hiberateUnique.getColumnIterator();
                        while (columnIterator.hasNext()) {
                            org.hibernate.mapping.Column hibernateColumn = (org.hibernate.mapping.Column) columnIterator.next();
                            index.getColumns().add(hibernateColumn.getName());
                        }
                        snapshot.getIndexes().add(index);
                    }

                    org.hibernate.mapping.PrimaryKey hibernatePrimaryKey = hibernateTable.getPrimaryKey();
                    if (hibernatePrimaryKey != null) {
                        PrimaryKey pk = new PrimaryKey();
                        pk.setName(hibernatePrimaryKey.getName());
                        pk.setTable(table);
                        for (Object hibernateColumn : hibernatePrimaryKey.getColumns()) {
                            pk.getColumnNamesAsList().add(((org.hibernate.mapping.Column) hibernateColumn).getName());
                        }
                        snapshot.getPrimaryKeys().add(pk);
                    }

                }
            }

            tableMappings = cfg.getTableMappings();
            while (tableMappings.hasNext()) {
                org.hibernate.mapping.Table hibernateTable = (org.hibernate.mapping.Table) tableMappings.next();
                if (hibernateTable.isPhysicalTable()) {

                    Iterator fkIterator = hibernateTable.getForeignKeyIterator();
                    while (fkIterator.hasNext()) {
                        org.hibernate.mapping.ForeignKey hibernateForeignKey = (org.hibernate.mapping.ForeignKey) fkIterator.next();
                        if (hibernateForeignKey.getTable() != null
                                && hibernateForeignKey.getReferencedTable() != null
                                && hibernateForeignKey.isPhysicalConstraint()) {
                            ForeignKey fk = new ForeignKey();
                            fk.setName(hibernateForeignKey.getName());
                            fk.setForeignKeyTable(snapshot.getTable(hibernateForeignKey.getTable().getName()));
                            List<String> fkColumns = new ArrayList<String>();
                            for (Object column : hibernateForeignKey.getColumns()) {
                                fkColumns.add(((org.hibernate.mapping.Column) column).getName());
                            }
                            fk.setForeignKeyColumns(StringUtils.join(fkColumns, ", "));

                            fk.setPrimaryKeyTable(snapshot.getTable(hibernateForeignKey.getReferencedTable().getName()));

                            fkColumns = new ArrayList<String>();
                            for (Object column : hibernateForeignKey.getReferencedColumns()) {
                                fkColumns.add(((org.hibernate.mapping.Column) column).getName());
                            }
                            if (fkColumns.size() == 0) {
                                for (Object column : hibernateForeignKey.getReferencedTable().getPrimaryKey().getColumns()) {
                                    fkColumns.add(((org.hibernate.mapping.Column) column).getName());
                                }
                            }
                            fk.setPrimaryKeyColumns(StringUtils.join(fkColumns, ", "));
//todo                            fk.setDeferrable(hibernateForeignKey.);
//todo                            fk.setInitiallyDeferred();
                            snapshot.getForeignKeys().add(fk);
                        }

                    }

//                    script.add(
//                            table.sqlCreateString(
//                                    dialect,
//                                    mapping,
//                                    defaultCatalog,
//                                    defaultSchema
//                                )
//                        );
//                    Iterator comments = table.sqlCommentStrings( dialect, defaultCatalog, defaultSchema );
//                    while ( comments.hasNext() ) {
//                        script.add( comments.next() );
//                    }
                }
            }

            return snapshot;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }

    }

    public boolean supports(Database database) {
        return database instanceof HibernateDatabase;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    public boolean hasDatabaseChangeLogLockTable() {
        return true;
    }

    public Table getDatabaseChangeLogTable() {
        return null;
    }

    private boolean isPrimaryKey(org.hibernate.mapping.Table hibernateTable, org.hibernate.mapping.Column hibernateColumn) {
        org.hibernate.mapping.PrimaryKey key = hibernateTable.getPrimaryKey();
        if (key == null) {
            return false;
        }
        Iterator columnIterator = key.getColumnIterator();
        while (columnIterator.hasNext()) {
            if (columnIterator.next().equals(hibernateColumn)) {
                return true;
            }
        }
        return false;
    }

    public Database getDatabase() {
        return database;
    }
}
