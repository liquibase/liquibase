package liquibase;

import java.lang.reflect.Constructor;

public class UIFactory {
	private static final String UI_IMPL_CLASSNAME="liquibase.SwingUIFacade";
    private static UIFactory instance = new UIFactory();

    private UIFacade facade;// = new SwingUIFacade();

    public static UIFactory getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
	public UIFacade getFacade() {
    	if(facade==null) {
    		System.out.println("XXX - Loaded UIFacade");
    		ClassLoader cl = UIFacade.class.getClassLoader();
    		try {
				Class<UIFacade> swingUIClazz = (Class<UIFacade>)cl.loadClass(UI_IMPL_CLASSNAME);
				Constructor<UIFacade> con = swingUIClazz.getConstructor(new Class[0]);
				facade = con.newInstance(new Object[0]);
				
			} catch (Exception e) {
				// Should never happen as class exists
				throw new RuntimeException(e); 
			}
    	}
    	
        return facade;
    }

    public void setFacade(UIFacade facade) {
        this.facade = facade;
    }
}
