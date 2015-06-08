package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

import java.math.BigInteger;

public class AddAutoIncrementAction extends AbstractAction {

        public ObjectName columnName;
        public String columnDataType;
        public BigInteger startWith;
        public BigInteger incrementBy;
}
