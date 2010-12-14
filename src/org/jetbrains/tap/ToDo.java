package org.jetbrains.tap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToDo extends Directive {
  private static final Pattern PATTERN = Pattern.compile("#\\s*TODO[\\s,]*(.*)", Pattern.CASE_INSENSITIVE);

  ToDo(String description) {
    super("TODO", description);
  }

  public static Directive parse(String line) {
    Matcher matcher = PATTERN.matcher(line);
    if (matcher.matches()) {
      return new ToDo(matcher.group(1).trim());
    }
    return null;
  }
}
