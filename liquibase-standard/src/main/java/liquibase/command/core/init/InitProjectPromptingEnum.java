package liquibase.command.core.init;

import java.util.Arrays;

import static liquibase.integration.commandline.LiquibaseCommandLineConfiguration.DEFAULTS_FILE;

public enum InitProjectPromptingEnum {
    PROJECT_DIR(null, "Enter a relative path to desired project directory", "./", (String)null, new DirectoryGetter()),
    DEFAULTS_FILENAME(null, "Enter name for defaults file to be created or (s)kip", DEFAULTS_FILE.getDefaultValue(), (String) null, new FilenameGetter()),
    JDBC_URL(null, "Enter the JDBC url without username or password to be used (What is a JDBC url? <url>)", "jdbc:h2:tcp://localhost:9090/mem:dev", (String) null, new StringGetter(false)),
    USERNAME(null, "Enter username to connect to JDBC url", "dbuser", (String) null, new StringGetter(false)),
    PASSWORD(null, "Enter password to connect to JDBC url", "letmein", (String) null, new StringGetter(false)),
    FILETYPE(null, "Enter your preferred changelog format", InitProjectCommandStep.FileTypeEnum.sql, InitProjectCommandStep.FileTypeEnum.values(), new EnumGetter<>(InitProjectCommandStep.FileTypeEnum.class, true)),
    SAMPLE_CHANGELOG_NAME(null, "Enter name for sample changelog file to be created or (s)kip", "example-changelog", (String) null, new FilenameGetter());

    /**
     * Description of the parameter to be used in the UI output.
     */
    public final String description;
    /**
     * Parameter message to be used in the UI output.
     */
    public final String uiMessage;
    /**
     * The default value of the parameter if the parameter has not been customized.
     */
    public final Object defaultValue;
    /**
     * The implementation of the value getter that will be used to prompt the user to input a value.
     */
    public final AbstractCommandLineValueGetter<?> interactiveCommandLineValueGetter;
    /*
     * Possible options for this parameter
     */
    public final String options;

    InitProjectPromptingEnum(String description, String uiMessage, Object defaultValue, String options, AbstractCommandLineValueGetter<?> interactiveCommandLineValueGetter) {
        this.description = description;
        this.uiMessage = uiMessage;
        this.defaultValue = defaultValue;
        this.options = options;
        this.interactiveCommandLineValueGetter = interactiveCommandLineValueGetter;
    }

    InitProjectPromptingEnum(String description, String uiMessage, Object defaultValue, Enum[] options, AbstractCommandLineValueGetter<?> interactiveCommandLineValueGetter) {
        this(description, uiMessage, defaultValue, enumOptionsToString(options), interactiveCommandLineValueGetter);
    }

    InitProjectPromptingEnum(String description, Object defaultValue, String options, AbstractCommandLineValueGetter<?> interactiveCommandLineValueGetter) {
        this.description = description;
        this.uiMessage = "Set '" + this + "'";
        this.defaultValue = defaultValue;
        this.options = options;
        this.interactiveCommandLineValueGetter = interactiveCommandLineValueGetter;
    }

    InitProjectPromptingEnum(String description, Object defaultValue, Enum[] options, AbstractCommandLineValueGetter<?> interactiveCommandLineValueGetter) {
        this (description, defaultValue, enumOptionsToString(options), interactiveCommandLineValueGetter);
    }

    private static String enumOptionsToString(Enum[] options) {
        return Arrays.toString(options).replace("[", "").replace("]", "").trim();
    }

}
