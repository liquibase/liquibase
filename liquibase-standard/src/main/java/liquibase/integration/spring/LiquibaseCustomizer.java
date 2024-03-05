package liquibase.integration.spring;

import liquibase.Liquibase;

/**
 * Callback interface that can be implemented by beans wishing to further customize the
 * {@link Liquibase}.
 */
@FunctionalInterface
public interface LiquibaseCustomizer {

    /**
     * Customize the {@link Liquibase}.
     *
     * @param liquibase the Liquibase to customize
     */
    void customize(Liquibase liquibase);

}
