using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data.SqlClient;
using liquibase.database;
using System.Data.OleDb;

namespace LiquiBase.Database {

    public class AdoConnection : DatabaseConnection {

        OleDbConnection conn;
        OleDbTransaction transaction;

        public AdoConnection() {
            conn = new OleDbConnection("Provider=SQLOLEDB;Data Source=localhost\\SQL2005;Initial Catalog=liquibase;User Id=liquibase;Password=liquibase;");
            conn.Open();
            transaction = conn.BeginTransaction();
        }

        public OleDbConnection GetUnderlyingConnection() {
            return conn;

        }
        void DatabaseConnection.close() {
            conn.Close();
        }

        void DatabaseConnection.commit() {
            transaction.Commit();
        }

        bool DatabaseConnection.getAutoCommit() {
            return transaction == null;
        }

        string DatabaseConnection.getCatalog() {
            return null;
        }

        string DatabaseConnection.getConnectionUserName() {
            return "liquibase"; //todo
        }

        int DatabaseConnection.getDatabaseMajorVersion() {
            return 1; //todo
        }

        int DatabaseConnection.getDatabaseMinorVersion() {
            return 1; //todo
        }

        string DatabaseConnection.getDatabaseProductName() {
            return conn.Provider;
        }

        string DatabaseConnection.getDatabaseProductVersion() {
            return conn.ServerVersion;
        }

        string DatabaseConnection.getURL() {
            return conn.ConnectionString;
        }

        string DatabaseConnection.nativeSQL(string str) {
            return str; //todo
        }

        void DatabaseConnection.rollback() {
            transaction.Rollback();
        }

        void DatabaseConnection.setAutoCommit(bool b) {
            if (b) {
                transaction = conn.BeginTransaction();
            } else {
                transaction = null;
            }
        }

    }
}
