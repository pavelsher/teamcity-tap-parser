package org.jetbrains.tap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Skip extends Directive {
  private static final Pattern PATTERN = Pattern.compile("#\\s*(SKIPPED|SKIP)[\\s,]*(.*)", Pattern.CASE_INSENSITIVE);
  public static final String SKIP_TYPE = "SKIP";

  Skip(String description) {
    super(SKIP_TYPE, description);
  }

  public static Directive parse(String line) {
    Matcher matcher = PATTERN.matcher(line);
    if (matcher.matches()) {
      return new Skip(matcher.group(2).trim());
    }
    return null;
  }
}
