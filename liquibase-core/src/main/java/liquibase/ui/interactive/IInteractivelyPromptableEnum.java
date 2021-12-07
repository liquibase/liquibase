package liquibase.ui.interactive;


public interface IInteractivelyPromptableEnum {
    /**
     * Description of the parameter to be used in the UI output.
     */
    String getDescription();

    /**
     * The default value of the parameter if the parameter has not been customized.
     */
    Object getDefaultValue();

    /**
     * The implementation of the value getter that will be used to prompt the user to input a value.
     */
    AbstractCommandLineValueGetter<?> getInteractiveCommandLineValueGetter();

    /**
     * Parameter message to be used in the UI output.
     */
    String getUiMessage();

    /*
     * Possible options for this parameter
     */
    String getOptions();
}
