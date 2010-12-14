package org.jetbrains.tap;

import java.util.regex.Pattern;

public abstract class Directive {
  public static final Pattern SKIP_PATTERN = Pattern.compile("#\\s*SKIP");

  private final String myType;
  private final String myDescription;

  Directive(String type, String description) {
    myDescription = description;
    myType = type;
  }

  public String getType() {
    return myType;
  }

  public String getDescription() {
    return myDescription;
  }

  @Override
  public String toString() {
    return getType() + (getDescription().length() > 0 ? " " + getDescription() : "");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Directive directive = (Directive) o;

    if (myDescription != null ? !myDescription.equals(directive.myDescription) : directive.myDescription != null)
      return false;
    if (!myType.equals(directive.myType)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myType.hashCode();
    result = 31 * result + (myDescription != null ? myDescription.hashCode() : 0);
    return result;
  }
}
