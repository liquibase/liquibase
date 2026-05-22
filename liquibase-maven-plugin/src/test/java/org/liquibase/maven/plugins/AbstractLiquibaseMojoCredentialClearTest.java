package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * CWE-316 regression tests for the credential-field nulling added to
 * {@link AbstractLiquibaseMojo} and its {@link LiquibaseDatabaseDiff} subclass.
 * <p>
 * Most tests exercise {@code clearCredentialFields()} directly to avoid pulling
 * in the full Maven-plugin test harness for a memory-hygiene assertion. The
 * runtime wiring (the call from {@code execute()}'s finally) is additionally
 * exercised by
 * {@link #execute_runs_clearCredentialFields_in_finally_when_skip_short_circuits()},
 * which lets {@code runExecute()} short-circuit on {@code skip=true} so the
 * finally fires without needing a real Maven plugin context.
 */
public class AbstractLiquibaseMojoCredentialClearTest {

    /**
     * Bare concrete subclass of {@link AbstractLiquibaseMojo} — supplies the
     * one abstract method so the class is instantiable. We do not invoke
     * {@code execute()} here; we only need a valid concrete subclass to exercise
     * {@code clearCredentialFields()}.
     */
    private static final class TestMojo extends AbstractLiquibaseMojo {
        @Override
        protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
            // not invoked in these tests
        }
    }

    @Test
    public void clearCredentialFields_nulls_password_but_leaves_other_fields_intact() {
        TestMojo mojo = new TestMojo();
        mojo.password = "supersecret-pw-12345";
        mojo.username = "regular-user";
        mojo.url = "jdbc:postgresql://host:5432/db";
        mojo.driver = "org.postgresql.Driver";

        mojo.clearCredentialFields();

        assertNull("password must be nulled", mojo.password);
        assertEquals("username must remain set", "regular-user", mojo.username);
        assertEquals("url must remain set", "jdbc:postgresql://host:5432/db", mojo.url);
        assertEquals("driver must remain set", "org.postgresql.Driver", mojo.driver);
    }

    @Test
    public void clearCredentialFields_is_idempotent_when_password_already_null() {
        TestMojo mojo = new TestMojo();
        mojo.password = null;

        mojo.clearCredentialFields();

        assertNull(mojo.password);
    }

    @Test
    public void liquibaseDatabaseDiff_override_nulls_password_and_referencePassword() {
        LiquibaseDatabaseDiff mojo = new LiquibaseDatabaseDiff();
        mojo.password = "supersecret-pw-12345";
        mojo.referencePassword = "supersecret-ref-67890";
        mojo.username = "regular-user";
        mojo.url = "jdbc:postgresql://host:5432/db";

        mojo.clearCredentialFields();

        assertNull("password must be nulled by super.clearCredentialFields()", mojo.password);
        assertNull("referencePassword must be nulled by the override", mojo.referencePassword);
        assertEquals("username must remain set", "regular-user", mojo.username);
        assertEquals("url must remain set", "jdbc:postgresql://host:5432/db", mojo.url);
    }

    /**
     * CWE-316 wiring regression: exercise the actual {@code execute()} entry
     * point (instead of calling {@code clearCredentialFields()} directly) so
     * that a future refactor accidentally removing the {@code try/finally}
     * wrapper, or breaking dynamic dispatch to the subclass's override, will
     * fail this test.
     * <p>
     * Uses {@code skip=true} so {@code runExecute()} short-circuits at the
     * skip check — no Maven plugin runtime context is needed. The
     * {@code getLog().warn(...)} call in the skip branch is safe because
     * Maven's {@code AbstractMojo.getLog()} lazy-initializes a
     * {@code SystemStreamLog} when no log has been set explicitly.
     * <p>
     * Uses {@link LiquibaseDatabaseDiff} (not the bare {@code TestMojo}) so
     * the test simultaneously verifies that {@code clearCredentialFields()}
     * dispatches dynamically to the override and clears
     * {@code referencePassword} in addition to {@code password}.
     */
    @Test
    public void execute_runs_clearCredentialFields_in_finally_when_skip_short_circuits() throws Exception {
        LiquibaseDatabaseDiff mojo = new LiquibaseDatabaseDiff();
        mojo.skip = true;
        // AbstractLiquibaseMojo overrides getLog() to lazy-construct a MavenLog
        // whose constructor calls Level.valueOf(logLevel); a null logLevel would
        // NPE on the first getLog() call in the skip branch. Provide a valid level
        // so runExecute()'s "Liquibase skipped due to Maven configuration" warning
        // can be emitted without bringing in a full Maven plugin context.
        mojo.logLevel = "INFO";
        mojo.password = "supersecret-pw-12345";
        mojo.referencePassword = "supersecret-ref-67890";

        mojo.execute();

        assertNull("password must be nulled from execute() finally", mojo.password);
        assertNull("referencePassword must be nulled via dynamic dispatch in finally", mojo.referencePassword);
    }
}
