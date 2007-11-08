package org.liquibase.intellij.plugin;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.migrator.Migrator;
import liquibase.xml.DefaultXmlWriter;
import liquibase.xml.XmlWriter;
import org.liquibase.ide.common.ChangeLogWriter;
import org.liquibase.ide.common.IdeFacade;
import org.liquibase.ide.common.ProgressMonitor;
import com.intellij.psi.xml.XmlFile;

public class IntellijFacade implements IdeFacade {

    public ProgressMonitor getProgressMonitor() {
        return new IntellijProgressMonitor();
    }


    public Migrator getMigrator(Database database) {
        return LiquibaseProjectComponent.getInstance().getMigrator(database);
    }

    public XmlWriter getXmlWriter() {
        return new DefaultXmlWriter();
    }

    public DatabaseChangeLog getChangeLog() {
//        XmlFile xmlFile = LiquibaseProjectComponent.getInstance().getChangeLogFile();
//        return new DatabaseChangeLog(getMigrator())
        return new DatabaseChangeLog(getMigrator(null), "changelog.xml");
    }


    public ChangeLogWriter getChangeLogWriter() {
        return new IntellijChangeLogWriter();
    }
}
