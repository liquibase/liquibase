package liquibase;

public class UIFactory {

    private static UIFactory instance = new UIFactory();

    private UIFacade facade = new SwingUIFacade();

    public static UIFactory getInstance() {
        return instance;
    }

    public UIFacade getFacade() {
        return facade;
    }

    public void setFacade(UIFacade facade) {
        this.facade = facade;
    }
}
