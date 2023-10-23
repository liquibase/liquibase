package liquibase;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signifies that a public API (public class, method or field) is subject to incompatible changes,
 * or even removal, in a future release. An API bearing this annotation is exempt from any
 * compatibility guarantees made by its containing library. Note that the presence of this
 * annotation implies nothing about the quality or performance of the API in question, only the fact
 * that it is not "API-frozen."
 *
 * <p>It is generally safe for <i>applications</i> to depend on beta APIs, at the cost of some extra
 * work during upgrades. However it is generally inadvisable for <i>libraries</i> (which get
 * included on users' CLASSPATHs, outside the library developers' control) to do so.
 *
 * @author Kevin Bourrillion
 */
@Retention(RetentionPolicy.CLASS)
@Target({
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.TYPE
})
@Documented
public @interface Beta {}