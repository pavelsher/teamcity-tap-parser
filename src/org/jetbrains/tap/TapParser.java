package org.jetbrains.tap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class TapParser {
  private final List<TapHandler> myHandlers = new ArrayList<TapHandler>();
  private TapHandler myHandlersNotifier;

  public TapParser() {
    InvocationHandler ih = new InvocationHandler() {
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        for (TapHandler th: myHandlers) {
          method.invoke(th, args);
        }
        return null;
      }
    };
    myHandlersNotifier = (TapHandler) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{TapHandler.class}, ih);
  }

  public void addHandler(TapHandler handler) {
    myHandlers.add(handler);
  }

  public static final String TAP_VERSION_PREFIX = "TAP version ";
  public static final String PLAN_PREFIX = "1..";
  public static final String TEST_OK_PREFIX = "ok ";
  public static final String TEST_NOT_OK_PREFIX = "not ok ";
  public static final String BAIL_OUT_PREFIX = "Bail out!";

  public void parse(String line) {
    try {
      if (line.startsWith(TAP_VERSION_PREFIX)) {
        notifyVersion(line);
        return;
      }

      if (line.startsWith(PLAN_PREFIX)) {
        notifyPlan(line);
        return;
      }

      if (line.startsWith("#")) {
        notifyComment(line);
        return;
      }

      if (line.startsWith(TEST_OK_PREFIX)) {
        notifyTestOk(line);
        return;
      }

      if (line.startsWith(TEST_NOT_OK_PREFIX)) {
        notifyTestNotOk(line);
        return;
      }

      if (line.startsWith(BAIL_OUT_PREFIX)) {
        notifyBailout(line);
        return;
      }


      myHandlersNotifier.unknown(line);

    } catch (Exception e) {
      myHandlersNotifier.unknown(line);
    }
  }

  private void notifyBailout(String line) {
    String rest = line.substring(BAIL_OUT_PREFIX.length());
    myHandlersNotifier.bailout(rest.trim());
  }

  private void notifyTestOk(String line) {
    String rest = line.substring(TEST_OK_PREFIX.length());
    notifyTest(true, rest);
  }

  private void notifyTest(boolean status, String rest) {
    String trimmed = rest.trim();
    int spaceIdx = trimmed.indexOf(' ');
    int orderNum = parseNum(spaceIdx == -1 ? trimmed : trimmed.substring(0, spaceIdx));

    int dirIdx = indexOfHash(trimmed);
    Directive dir = dirIdx == -1 ? null : parseDirective(trimmed.substring(dirIdx));

    String descr = "";
    if (spaceIdx != -1) {
      descr = dirIdx == -1 ? trimmed.substring(spaceIdx) : trimmed.substring(spaceIdx, dirIdx);
      descr = descr.trim();
      if (descr.startsWith("-")) {
        descr = descr.substring(1).trim();
      }
    }

    myHandlersNotifier.test(orderNum, status, descr.trim(), dir);
  }

  private void notifyTestNotOk(String line) {
    String rest = line.substring(TEST_NOT_OK_PREFIX.length());
    notifyTest(false, rest);
  }

  private void notifyComment(String line) {
    myHandlersNotifier.comment(line);
  }

  private void notifyPlan(String line) {
    String rest = line.substring(PLAN_PREFIX.length());
    int dirIdx = indexOfHash(rest);
    String numStr = rest.substring(0, dirIdx == -1 ? rest.length() : dirIdx);

    int num = parseNum(numStr);

    Directive dir = dirIdx == -1 ? null : parseDirective(rest.substring(dirIdx));

    myHandlersNotifier.plan(num, dir);
  }

  private int parseNum(String numStr) {
    return Integer.parseInt(numStr.trim());
  }

  private Directive parseDirective(String text) {
    Directive res = Skip.parse(text);
    if (res != null) return res;

    return ToDo.parse(text);
  }

  private int indexOfHash(String rest) {
    int pos = 0;
    while (pos < rest.length()) {
      char ch = rest.charAt(pos);
      if (ch == '#') return pos;
      if (ch == '\\') pos++;
      pos++;
    }
    return -1;
  }

  private void notifyVersion(String line) {
    String verNum = line.substring(TAP_VERSION_PREFIX.length());
    int ver = parseNum(verNum);
    myHandlersNotifier.version(ver);
  }
}
