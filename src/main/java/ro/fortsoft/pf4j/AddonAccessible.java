package ro.fortsoft.pf4j;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is used in Izou to set interfaces accessible for addons.
 * @author LeanderK
 * @version 1.0
 */
@Retention(RUNTIME)
@Documented
public @interface AddonAccessible {
}
