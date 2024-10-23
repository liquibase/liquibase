package liquibase.integration;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class UnexpectedChangeSetsValidator<T extends Liquibase> implements Consumer<T> {

    private final Contexts contexts;
    private final LabelExpression labelExpression;

    private final Set<ChangeSetInfo> applied, unexpected;

    public UnexpectedChangeSetsValidator() {
        contexts = new Contexts();
        labelExpression = new LabelExpression();
        applied = new HashSet<>();
        unexpected = new HashSet<>();
    }

    private UnexpectedChangeSetsValidator(
            Set<ChangeSetInfo> applied,
            Set<ChangeSetInfo> unexpected,
            Contexts contexts,
            LabelExpression labelExpression) {
        this.applied = applied;
        this.unexpected = unexpected;
        this.contexts = contexts;
        this.labelExpression = labelExpression;
    }

    @Override
    public void accept(T liquibase) {
        try {
            liquibase.getDatabaseChangeLog().getChangeSets().stream().map(changeSet -> new ChangeSetInfo(
                    changeSet.getId(),
                    changeSet.getFilePath(),
                    changeSet.getAuthor())).forEach(applied::add);
            liquibase.listUnexpectedChangeSets(contexts, labelExpression).stream().map(changeSet -> new ChangeSetInfo(
                    changeSet.getId(),
                    changeSet.getChangeLog(),
                    changeSet.getAuthor())).forEach(unexpected::add);
        } catch (LiquibaseException e) {
            throw new IllegalStateException(e);
        }
    }

    public UnexpectedChangeSetsValidator<T> with(Contexts contexts, LabelExpression labelExpression) {
        return new UnexpectedChangeSetsValidator<>(applied,
                unexpected,
                contexts,
                labelExpression);
    }

    public void validate(Consumer<Set<ChangeSetInfo>> unexpectedChangeSetsConsumer) {
        Set<ChangeSetInfo> changeSets = new HashSet<>(unexpected);
        changeSets.removeAll(applied);
        if (!changeSets.isEmpty()) {
            unexpectedChangeSetsConsumer.accept(changeSets);
        }
    }

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class ChangeSetInfo {

        private final String id, changeLog, author;

        @Override
        public String toString() {
            return changeLog + "::" + id + "::" + author;
        }
    }
}
