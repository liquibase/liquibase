package liquibase.logging.mdc.customobjects;

import liquibase.logging.mdc.CustomMdcObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExceptionDetails implements CustomMdcObject {
    private String primaryException;
    private String primaryExceptionReason;
    private String primaryExceptionSource;
    private String exception;

    public ExceptionDetails() {
    }
}
