package org.jetbrains.tap;

public interface TapHandler {
  void version(int version);

  void plan(int numTests, Directive directive);

  void test(int orderNum, boolean ok, String description, Directive directive);

  void comment(String comment);

  void bailout(String text);

  void unknown(String text);
}
