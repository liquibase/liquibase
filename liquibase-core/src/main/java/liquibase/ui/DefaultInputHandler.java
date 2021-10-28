package liquibase.ui;

import liquibase.util.ObjectUtil;

/**
 * Default input handler simply calls {@link liquibase.util.ObjectUtil#convert(Object, Class)}
 */

public class DefaultInputHandler<ReturnType> implements InputHandler<ReturnType> {

    @Override
    public ReturnType parseInput(String input, Class<ReturnType> returnType) throws IllegalArgumentException {
        try {
            return ObjectUtil.convert(input, returnType);
        } catch (IllegalArgumentException e) {
            throw UiIllegalArgumentExceptionMessageHandler.addPrefixToExceptionMessage(e, input);
        }
    }
}
