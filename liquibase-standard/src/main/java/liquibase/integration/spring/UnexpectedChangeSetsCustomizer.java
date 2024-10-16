package liquibase.integration.spring;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.command.core.UnexpectedChangesetsCommandStep;
import liquibase.exception.LiquibaseException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class UnexpectedChangeSetsCustomizer<T extends Liquibase> implements Customizer<T>, ApplicationListener<ContextRefreshedEvent> {

    private final Set<ChangeSetInfo> applied = new HashSet<>(), unexpected = new HashSet<>();

    private final Consumer<Set<ChangeSetInfo>> unexpectedChangeSetsConsumer;

    public UnexpectedChangeSetsCustomizer(Consumer<Set<ChangeSetInfo>> unexpectedChangeSetsConsumer) {
        this.unexpectedChangeSetsConsumer = unexpectedChangeSetsConsumer;
    }

    @Override
    public void customize(T liquibase) {
        try {
            liquibase.getDatabaseChangeLog().getChangeSets().stream().map(changeSet -> new ChangeSetInfo(
                    changeSet.getId(),
                    changeSet.getChangeLog().getFilePath(),
                    changeSet.getAuthor())).forEach(applied::add);
            UnexpectedChangesetsCommandStep.listUnexpectedChangeSets(
                liquibase.getDatabase(),
                liquibase.getDatabaseChangeLog(),
                new Contexts(),
                new LabelExpression()).stream().map(changeSet -> new ChangeSetInfo(
                    changeSet.getId(),
                    changeSet.getChangeLog(),
                    changeSet.getAuthor())).forEach(unexpected::add);
        } catch (LiquibaseException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        unexpected.removeAll(applied);
        if (!unexpected.isEmpty()) {
            unexpectedChangeSetsConsumer.accept(Collections.unmodifiableSet(unexpected));
        }
    }

    public static class ChangeSetInfo {

        private final String id, changeLog, author;

        ChangeSetInfo(String id, String changeLog, String author) {
            this.id = id;
            this.changeLog = changeLog;
            this.author = author;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ChangeSetInfo that = (ChangeSetInfo) o;
            return Objects.equals(id, that.id)
                    && Objects.equals(changeLog, that.changeLog)
                    && Objects.equals(author, that.author);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, changeLog, author);
        }
    }
}
