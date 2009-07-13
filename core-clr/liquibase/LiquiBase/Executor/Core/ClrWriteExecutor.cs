using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using LiquiBase.Database;

namespace LiquiBase.Executor.Core {
    public class ClrWriteExecutor : liquibase.executor.AbstractExecutor, liquibase.executor.WriteExecutor {

        public java.util.Map call(liquibase.statement.CallableSqlStatement css, java.util.List l1, java.util.List l2) {
            throw new NotImplementedException();
        }

        public void comment(string str) {
            throw new NotImplementedException();
        }

        public void execute(liquibase.statement.SqlStatement ss) {
            Console.WriteLine("execute " + ss);
        }

        public void execute(liquibase.statement.SqlStatement ss, java.util.List l) {
            Console.WriteLine("execute " + ss);
        }

        public bool executesStatements() {
            throw new NotImplementedException();
        }

        public void setDatabase(liquibase.database.Database d) {
            throw new NotImplementedException();
        }

        public int update(liquibase.statement.SqlStatement ss) {
            Console.WriteLine("update " + ss);
            return 1;
        }

        public int update(liquibase.statement.SqlStatement ss, java.util.List l) {
            Console.WriteLine("execute " + ss);

            return 1;
        }

    }
}
