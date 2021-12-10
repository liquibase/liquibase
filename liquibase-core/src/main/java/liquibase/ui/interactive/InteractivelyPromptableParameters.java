package liquibase.ui.interactive;

import liquibase.ui.interactive.getter.DirectoryGetter;
import liquibase.ui.interactive.getter.EnumGetter;
import liquibase.ui.interactive.getter.FilenameGetter;
import liquibase.ui.interactive.getter.StringGetter;

import java.util.Arrays;

import static liquibase.integration.commandline.LiquibaseCommandLineConfiguration.DEFAULTS_FILE;

/**
 *
 */
public enum InteractivelyPromptableParameters implements IInteractivelyPromptableEnum {
    PROJECT_DIR(null, "Enter a relative path to desired project directory, or (n) to cancel:", ".", (String)null, new DirectoryGetter()),
    DEFAULTS_FILENAME(null, "Enter name for defaults file to be created or (s)kip", DEFAULTS_FILE.getDefaultValue(), (String) null, new FilenameGetter()),
    JDBC_URL(null, "Enter the JDBC url without username or password to be used (What is this? <url>)", "jdbc:h2:tcp://localhost:9090/mem:dev", (String) null, new StringGetter(false)),
    USERNAME(null, "Enter username to connect to JDBC url", "dbuser", (String) null, new StringGetter(false)),
    PASSWORD(null, "Enter password to connect to JDBC url", "letmein", (String) null, new StringGetter(false)),
    FILETYPE(null, "Enter your preferred changelog filetype", FileTypeEnum.sql, FileTypeEnum.values(), new EnumGetter<>(FileTypeEnum.class)),
    SAMPLE_CHANGELOG_NAME(null, "Enter name for sample changelog file to be created or (s)kip", "example-changelog." + FILETYPE.defaultValue, (String) null, new FilenameGetter());


    private final String description;
    private final String uiMessage;
    private final Object defaultValue;
    private final AbstractCommandLineValueGetter<?> interactiveCommandLineValueGetter;
    private final String options;

    InteractivelyPromptableParameters(String description, String uiMessage, Object defaultValue, String options, AbstractCommandLineValueGetter<?> interactiveCommandLineValueGetter) {
        this.description = description;
        this.uiMessage = uiMessage;
        this.defaultValue = defaultValue;
        this.options = options;
        this.interactiveCommandLineValueGetter = interactiveCommandLineValueGetter;
    }

    InteractivelyPromptableParameters(String description, String uiMessage, Object defaultValue, Enum[] options, AbstractCommandLineValueGetter<?> interactiveCommandLineValueGetter) {
        this(description, uiMessage, defaultValue, enumOptionsToString(options), interactiveCommandLineValueGetter);
    }

    InteractivelyPromptableParameters(String description, Object defaultValue, String options, AbstractCommandLineValueGetter<?> interactiveCommandLineValueGetter) {
        this.description = description;
        this.uiMessage = "Set '" + this + "'";
        this.defaultValue = defaultValue;
        this.options = options;
        this.interactiveCommandLineValueGetter = interactiveCommandLineValueGetter;
    }

    InteractivelyPromptableParameters(String description, Object defaultValue, Enum[] options, AbstractCommandLineValueGetter<?> interactiveCommandLineValueGetter) {
        this (description, defaultValue, enumOptionsToString(options), interactiveCommandLineValueGetter);
    }

    private static String enumOptionsToString(Enum[] options) {
        return Arrays.toString(options).replace("[", "").replace("]", "");
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getUiMessage() {
        return uiMessage;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public AbstractCommandLineValueGetter<?> getInteractiveCommandLineValueGetter() {
        return interactiveCommandLineValueGetter;
    }

    @Override
    public String getOptions() {
        return options;
    }
}
