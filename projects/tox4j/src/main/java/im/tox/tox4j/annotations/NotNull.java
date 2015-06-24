package im.tox.tox4j.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface NotNull {
  /**
   * Not used, yet.
   */
  String value() default "";

  /**
   * Not used, yet.
   */
  Class<? extends Exception> exception() default Exception.class;
}
