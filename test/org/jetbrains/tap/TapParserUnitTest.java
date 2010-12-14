package org.jetbrains.tap;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

public class TapParserUnitTest {
  private TapParser myParser;
  private Mockery myContext;
  private TapHandler myHandler;

  @Before
  public void setUp() {
    myParser = new TapParser();
    myContext = new Mockery();
    myHandler = myContext.mock(TapHandler.class);
    myParser.addHandler(myHandler);
  }

  @Test
  public void test_version() {
    myContext.checking(new Expectations() {{
      oneOf(myHandler).version(11);
      oneOf(myHandler).version(9);
      oneOf(myHandler).version(0);
      oneOf(myHandler).unknown("TAP version ");
    }});

    myParser.parse("TAP version 11");
    myParser.parse("TAP version    9   ");
    myParser.parse("TAP version 0");
    myParser.parse("TAP version ");

    myContext.assertIsSatisfied();
  }

  @Test
  public void test_plan() {
    myContext.checking(new Expectations() {{
      oneOf(myHandler).plan(21, null);
      oneOf(myHandler).plan(40, new Skip("the plan is skipped"));
      oneOf(myHandler).unknown("1..");
    }});

    myParser.parse("1..21");
    myParser.parse("1..40    #SKIP  the plan is skipped");
    myParser.parse("1..");

    myContext.assertIsSatisfied();
  }

  @Test
  public void test_comment() {
    myContext.checking(new Expectations() {{
      oneOf(myHandler).comment("#some text");
      oneOf(myHandler).unknown("not a comment");
    }});

    myParser.parse("#some text");
    myParser.parse("not a comment");

    myContext.assertIsSatisfied();
  }

  @Test
  public void test_test() {
    myContext.checking(new Expectations() {{
      oneOf(myHandler).test(11, false, "", null);
      oneOf(myHandler).test(21, true, "the test is ok!", null);
      oneOf(myHandler).test(2, true, "test is under construction", new ToDo(""));
      oneOf(myHandler).test(3, true, "", new Skip(""));
      oneOf(myHandler).test(4, true, "", new Skip(""));
      oneOf(myHandler).test(5, false, "", new ToDo(""));
      oneOf(myHandler).test(6, false, "some text with escaped \\# hash", null);
    }});

    myParser.parse("not ok 11");
    myParser.parse("ok 21   the test is ok!");
    myParser.parse("ok 2 test is under construction #TODO");
    myParser.parse("ok 3 #SKIP");
    myParser.parse("ok 4 # SKIP");
    myParser.parse("not ok 5 # todo");
    myParser.parse("not ok 6 some text with escaped \\# hash");
  }

  @Test
  public void test_bailout() {
    myContext.checking(new Expectations() {{
      oneOf(myHandler).bailout("have a good day!");
      oneOf(myHandler).bailout("");
    }});
    myParser.parse("Bail out! have a good day!");
    myParser.parse("Bail out!");
  }
}
