package com.nbugaenco.ttg.model;

import java.util.Objects;

/**
 * Represents a logical variable with an expression and a boolean value.
 */
public record LogicalVariable(String expression, boolean value) {

  /**
   * Checks if this logical variable is equal to another object.
   *
   * @param o
   *     the object to compare with
   *
   * @return true if the specified object is equal to this logical variable, false otherwise
   */
  @Override
  public boolean equals(final Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final LogicalVariable that = (LogicalVariable) o;
    return value() == that.value() && Objects.equals(expression(), that.expression());
  }

  /**
   * Returns the hash code value for this logical variable.
   *
   * @return the hash code value
   */
  @Override
  public int hashCode() {
    return Objects.hash(expression(), value());
  }

}
