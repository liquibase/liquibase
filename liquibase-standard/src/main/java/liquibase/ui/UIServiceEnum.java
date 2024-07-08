package liquibase.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum used to define the available UIServices provided by Liquibase that can be set using global parameters
 */
@Getter
@AllArgsConstructor
public enum UIServiceEnum {

    CONSOLE(ConsoleUIService.class),
    LOGGER(LoggerUIService.class);

    private final Class<? extends UIService> uiServiceClass;

}
