package fr.woorib.backand.client.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a getter method as pointing to a many to many table in Backand.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BackandManyToMany {
  /**
   * Name of the column in backand.com that contains the data for this side of the
   * many-to-many relationship.
   * @return
   */
    String parameter();
}
