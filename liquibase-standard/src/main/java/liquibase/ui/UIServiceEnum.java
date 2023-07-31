package liquibase.ui;

public enum UIServiceEnum {

        CONSOLE(ConsoleUIService.class),
        LOGGER(LoggerUIService.class);

        private final Class<? extends UIService> uiServiceClass;

        UIServiceEnum(Class<? extends UIService> uiServiceClass) {
            this.uiServiceClass = uiServiceClass;
        }

        public Class<? extends UIService> getUiServiceClass() {
            return uiServiceClass;
        }
    }
