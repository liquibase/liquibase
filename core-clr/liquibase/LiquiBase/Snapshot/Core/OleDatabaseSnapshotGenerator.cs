using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using liquibase.snapshot;
using LiquiBase.Database;
using System.Data.OleDb;
using System.Data;
using liquibase.database.structure;

namespace LiquiBase.Snapshot.Core {
    public class OleDatabaseSnapshotGenerator : DatabaseSnapshotGenerator {

        public int getPriority(liquibase.database.Database d) {
            return 5;
        }

        public bool supports(liquibase.database.Database d) {
            return true;
        }

        public DatabaseSnapshot createSnapshot(liquibase.database.Database database, string schema, java.util.Set listeners) {
            DatabaseSnapshot snapshot = new DatabaseSnapshot(database, schema);

            OleDbConnection conn = ((AdoConnection)database.getConnection()).GetUnderlyingConnection();

             string[] restrictions = new string[4];
             restrictions[3] = "Table";

             DataTable tables = conn.GetSchema("Tables", restrictions);

             foreach (DataRow row in tables.Rows) {                 
                 Table table =  new Table(row.Field<String>("TABLE_NAME"));
                 table.setSchema(row.Field<String>("TABLE_SCHEMA"));

                 snapshot.getTables().add(table);

                 DataTable tableInfo = conn.GetSchema("Columns", new string[4] { null, null, table.getName(), null });

                 foreach (DataRow colRow in tableInfo.Rows) {
                     Column column = new Column();
                     column.setName(colRow.Field<string>("COLUMN_NAME"));
                     column.setTable(table);
                     //column.setTypeName(colRow.Field<string>("DATA_TYPE"));
                     //column.setColumnSize(colRow.Field<int>("NUMERIC_SCALE"));

                     table.getColumns().add(column);
                 }
             }

            return snapshot;

        }


    }
}
