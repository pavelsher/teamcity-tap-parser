package org.jetbrains.tap;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TapParserRealTest {
  @Test
  public void test() throws IOException {
    for (int i=1; i<4; i++) {
      doTest(i);
    }
  }

  private void doTest(int inpNum) throws IOException {
    File inputFile = new File("testData/input" + inpNum + ".txt");
    File expectFile = new File("testData/expected" + inpNum + ".txt");

    String[] inputLines = lines(inputFile);
    String[] expLines = lines(expectFile);

    final List<String> actual = new ArrayList<String>();
    TapParser parser = new TapParser();
    parser.addHandler(new TapHandler() {
      public void version(int version) {
        actual.add("version " + version);
      }

      public void plan(int numTests, Directive directive) {
        String line = "plan " + numTests;
        if (directive != null) {
          line += " " + directive.toString();
        }
        actual.add(line);
      }

      public void test(int orderNum, boolean ok, String description, Directive directive) {
        String line;
        if (ok) {
          line = "passed " + orderNum;
        } else {
          line = "failed " + orderNum;
        }

        if (description != null && description.length() > 0) {
          line += " " + description;
        }
        if (directive != null) {
          line += " " + directive.toString();
        }
        actual.add(line);
      }

      public void comment(String comment) {
        actual.add("comment" + (comment != null && comment.length() > 0 ? " " + comment : ""));
      }

      public void bailout(String text) {
        actual.add("bailout" + (text != null && text.length() > 0 ? " " + text : ""));
      }

      public void unknown(String text) {
        actual.add("unknown " + text);
      }
    });

    for (String line: inputLines) {
      parser.parse(line);
    }

    for (int i=0; i<expLines.length; i++) {
      String a = i<actual.size() ? actual.get(i): null;
      Assert.assertEquals("Actual text:\n" + actual.toString(), expLines[i], a);
    }
  }


  private String[] lines(File file) throws IOException {
    List<String> result = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    try {
      while (true) {
        String line = reader.readLine();
        if (line == null) break;
        result.add(line.trim());
      }
    } finally {
      reader.close();
    }

    return result.toArray(new String[result.size()]);
  }
}
