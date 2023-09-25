package liquibase.ui;

import lombok.Getter;

/**
 * Enum used to define the available UIServices provided by Liquibase that can be set using global parameters
 */
@Getter
public enum UIServiceEnum {

    CONSOLE(ConsoleUIService.class),
    LOGGER(LoggerUIService.class);

    private final Class<? extends UIService> uiServiceClass;

    UIServiceEnum(Class<? extends UIService> uiServiceClass) {
        this.uiServiceClass = uiServiceClass;
    }

}
