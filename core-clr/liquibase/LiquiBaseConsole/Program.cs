using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using java.lang;
using liquibase.database;
using liquibase.util.plugin;
using System.Reflection;
using liquibase;
using liquibase.snapshot;
using LiquiBase.Database;
using liquibase.lockservice;

namespace console {
    class Program {
        static void Main(string[] args) {
            Console.WriteLine("hello");

            DatabaseConnection conn = new AdoConnection();
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn);

            //Console.WriteLine("Found database " + database + " of type " + database.GetType());


            LockService lockService = LockService.getInstance(database);
            lockService.waitForLock();
            
            Console.Read();
        }
    }
}
