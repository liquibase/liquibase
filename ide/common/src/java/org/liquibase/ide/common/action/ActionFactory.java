package org.liquibase.ide.common.action;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ActionFactory {
    private static final ActionFactory instance = new ActionFactory();

    private List<BaseDatabaseAction> actions = new ArrayList<BaseDatabaseAction>();


    private ActionFactory() {
        try {
            File changeActionDir = new File(this.getClass().getClassLoader().getResource("org/liquibase/ide/common/change/action").toURI());
            File[] files = changeActionDir.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".class");
                }
            });
            for (File file : files) {

            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }


    public static ActionFactory getInstance() {
        return instance;
    }

}
