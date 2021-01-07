package liquibase.ui;

public interface InputHandler<ReturnType> {

    /**
     * Converts the given input into the correct return type.
     * @throws IllegalArgumentException if the input is not valid
     */
    ReturnType parseInput(String input, Class<ReturnType> returnType) throws IllegalArgumentException;
}
