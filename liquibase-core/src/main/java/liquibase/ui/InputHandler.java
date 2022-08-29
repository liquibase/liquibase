package liquibase.ui;

public interface InputHandler<ReturnType> {

    /**
     * Converts the given input into the correct return type.
     * @throws IllegalArgumentException if the input is not valid
     */
    ReturnType parseInput(String input, Class<ReturnType> returnType) throws IllegalArgumentException;

    /**
     * Determine whether an empty input should be permitted.
     * @return true to allow empty inputs, false to reprompt when an empty value is inputted
     */
    default boolean shouldAllowEmptyInput(){
        return true;
    }
}
