package liquibase.integration.ant.type;

import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.junit.Test;

import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * CWE-316 regression tests for the credential-clearing mechanism added to
 * {@link DatabaseType}. We exercise the {@code clearCredentials()} method and
 * the {@code registerCredentialCleanup()} listener-registration directly,
 * including the refid traversal — running through a full Ant build with a real
 * database would require the broader Ant test harness, but the listener
 * contract (Project fires {@code buildFinished} after the last task) is
 * well-defined and tested by Ant itself.
 */
public class DatabaseTypeCredentialClearTest {

    @Test
    public void clearCredentials_nulls_password_on_a_simple_databaseType() {
        Project project = new Project();
        DatabaseType type = new DatabaseType(project);
        type.setUrl("jdbc:h2:mem:test");
        type.setUser("regular-user");
        type.setPassword("supersecret-pw-12345");

        type.clearCredentials();

        assertNull("password must be nulled", type.getPassword());
        assertEquals("user must remain set", "regular-user", type.getUser());
        assertEquals("url must remain set", "jdbc:h2:mem:test", type.getUrl());
    }

    @Test
    public void clearCredentials_traverses_refid_and_clears_the_referenced_instance() {
        // Simulate the typedef+databaseRef pattern: a referenced DatabaseType
        // (id="mydb") that holds the actual credential, plus a local "shell"
        // DatabaseType whose getters delegate to the referenced one. The
        // local shell's clearCredentials must follow the refid to the actual
        // shared instance so the password Strings on the referenced field
        // also become GC-eligible.
        Project project = new Project();
        DatabaseType referenced = new DatabaseType(project);
        referenced.setUrl("jdbc:h2:mem:shared");
        referenced.setUser("regular-user");
        referenced.setPassword("supersecret-pw-12345");
        project.addReference("mydb", referenced);

        DatabaseType shell = new DatabaseType(project);
        org.apache.tools.ant.types.Reference ref = new org.apache.tools.ant.types.Reference(project, "mydb");
        shell.setRefid(ref);

        // Sanity check: the shell's getPassword delegates to the referenced.
        assertEquals("supersecret-pw-12345", shell.getPassword());

        shell.clearCredentials();

        assertNull("referenced password must be nulled via refid traversal", referenced.getPassword());
        assertNull("shell's delegating getPassword must reflect the cleared value", shell.getPassword());
        assertEquals("user must remain set on the referenced", "regular-user", referenced.getUser());
    }

    @Test
    public void registerCredentialCleanup_registers_a_BuildListener_that_fires_on_buildFinished() {
        Project project = new Project();
        DatabaseType type = new DatabaseType(project);
        type.setPassword("supersecret-pw-12345");

        int listenerCountBefore = project.getBuildListeners().size();
        type.registerCredentialCleanup();
        int listenerCountAfter = project.getBuildListeners().size();

        assertEquals("registerCredentialCleanup must add exactly one BuildListener",
                listenerCountBefore + 1, listenerCountAfter);

        // Fire buildFinished and verify the password is nulled.
        project.fireBuildFinished(null);
        assertNull("password must be nulled when the Project fires buildFinished",
                type.getPassword());
    }

    @Test
    public void registerCredentialCleanup_is_idempotent() {
        // Multiple Ant tasks consuming the same DatabaseType (whether by
        // <database> nesting or by databaseRef) will each call createDatabase,
        // which in turn calls registerCredentialCleanup. Only the first call
        // should register a listener; subsequent calls must be no-ops.
        Project project = new Project();
        DatabaseType type = new DatabaseType(project);

        int listenerCountBefore = project.getBuildListeners().size();
        type.registerCredentialCleanup();
        type.registerCredentialCleanup();
        type.registerCredentialCleanup();
        int listenerCountAfter = project.getBuildListeners().size();

        assertEquals("registerCredentialCleanup must be idempotent on the same DatabaseType",
                listenerCountBefore + 1, listenerCountAfter);
    }

    @Test
    public void registerCredentialCleanup_listener_only_implements_buildFinished_meaningfully() {
        // The listener must not throw or have side effects on the non-credential
        // lifecycle events (targetStarted, taskStarted, etc.) — those fire during
        // the build and we cannot null the password yet.
        Project project = new Project();
        DatabaseType type = new DatabaseType(project);
        type.setPassword("supersecret-pw-12345");

        type.registerCredentialCleanup();

        Vector<BuildListener> listeners = project.getBuildListeners();
        BuildListener last = listeners.lastElement();
        assertNotNull(last);

        // Fire the during-build events; password must still be present.
        org.apache.tools.ant.BuildEvent event = new org.apache.tools.ant.BuildEvent(project);
        last.buildStarted(event);
        last.targetStarted(event);
        last.taskStarted(event);
        last.taskFinished(event);
        last.targetFinished(event);
        last.messageLogged(event);
        assertEquals("password must be retained during the build",
                "supersecret-pw-12345", type.getPassword());

        // Now fire buildFinished and verify clearing.
        last.buildFinished(event);
        assertNull("password must be nulled at buildFinished", type.getPassword());
    }

    @Test
    public void clearCredentials_is_safe_on_a_refid_shell_whose_referenced_was_dropped() {
        // Defensive: if the project tear-down removed the reference before our
        // BuildListener fires (unusual but possible), clearCredentials must not
        // throw — it should still null its own (empty) field cleanly.
        Project project = new Project();
        DatabaseType shell = new DatabaseType(project);
        org.apache.tools.ant.types.Reference ref =
                new org.apache.tools.ant.types.Reference(project, "no-such-ref");
        shell.setRefid(ref);

        // No exception expected — clearCredentials catches BuildException from getCheckedRef.
        shell.clearCredentials();
        assertTrue("clearCredentials must not throw on a dangling refid", true);
    }

    @Test
    public void registerCredentialCleanup_listener_deregisters_itself_after_firing() {
        // CWE-316 regression for the long-lived embedded Ant case (per @wwillard7800's
        // review on #7743). In one-shot Ant CLI builds the Project is GC'd after the
        // build and any attached listeners go with it. In embedded hosts that reuse a
        // single Project across many builds (build daemons, IDE plugins, MPS-style
        // tools), each new DatabaseType would otherwise add a permanent listener that
        // holds a strong reference back to the (now-cleared) DatabaseType — defeating
        // the residency-window goal this PR series is trying to achieve. The listener
        // must deregister itself from the Project once buildFinished fires.
        Project project = new Project();
        int listenerCountBefore = project.getBuildListeners().size();

        DatabaseType type = new DatabaseType(project);
        type.setPassword("supersecret-pw-12345");
        type.registerCredentialCleanup();

        // Sanity: the listener was registered.
        assertEquals("registerCredentialCleanup must add exactly one BuildListener",
                listenerCountBefore + 1, project.getBuildListeners().size());

        // Fire buildFinished — listener should run clearCredentials AND remove itself.
        project.fireBuildFinished(null);

        assertNull("password must be nulled when buildFinished fires", type.getPassword());
        assertEquals("listener must deregister itself from the Project after firing",
                listenerCountBefore, project.getBuildListeners().size());
    }

    @Test
    public void multiple_databaseTypes_all_deregister_their_listeners_in_embedded_scenario() {
        // CWE-316 regression for accumulation (per @wwillard7800): in a long-lived
        // embedded Ant host that reuses one Project across N builds, N DatabaseType
        // instances should result in zero residual listeners after all builds finish —
        // not N listeners stuck holding references to empty DatabaseType shells.
        Project project = new Project();
        int listenerCountBefore = project.getBuildListeners().size();

        DatabaseType firstBuild  = new DatabaseType(project);
        DatabaseType secondBuild = new DatabaseType(project);
        DatabaseType thirdBuild  = new DatabaseType(project);
        firstBuild.registerCredentialCleanup();
        secondBuild.registerCredentialCleanup();
        thirdBuild.registerCredentialCleanup();

        assertEquals("three DatabaseType instances must register three distinct listeners",
                listenerCountBefore + 3, project.getBuildListeners().size());

        // One buildFinished cycle drains all three at once.
        project.fireBuildFinished(null);

        assertEquals("all three listeners must deregister themselves — no accumulation",
                listenerCountBefore, project.getBuildListeners().size());
    }
}
