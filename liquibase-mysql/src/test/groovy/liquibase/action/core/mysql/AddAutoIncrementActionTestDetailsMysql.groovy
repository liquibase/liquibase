package liquibase.action.core.mysql

import liquibase.action.core.AddAutoIncrementActionTest

class AddAutoIncrementActionTestDetailsMysql extends AddAutoIncrementActionTest.TestDetails {

    @Override
    boolean createPrimaryKeyBeforeAutoIncrement() {
        return true;
    }
}
