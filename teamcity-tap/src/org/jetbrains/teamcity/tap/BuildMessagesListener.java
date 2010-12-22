package org.jetbrains.teamcity.tap;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.TestFinished;
import jetbrains.buildServer.messages.serviceMessages.TestIgnored;
import jetbrains.buildServer.messages.serviceMessages.TestStarted;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.tap.Directive;
import org.jetbrains.tap.TapHandler;
import org.jetbrains.tap.TapParser;

import java.util.Arrays;
import java.util.HashMap;

public class BuildMessagesListener extends AgentLifeCycleAdapter {
  private final TapParser myTapParser;
  private final ServiceMessageConverter myTapHandler;

  public BuildMessagesListener(EventDispatcher<AgentLifeCycleListener> dispatcher) {
    dispatcher.addListener(this);

    myTapParser = new TapParser();
    myTapHandler = new ServiceMessageConverter();
    myTapParser.addHandler(myTapHandler);
  }

  @Override
  public void messageLogged(@NotNull AgentRunningBuild build, @NotNull BuildMessage1 buildMessage) {
    super.messageLogged(build, buildMessage);
    if (buildMessage.getTypeId().equals(DefaultMessagesInfo.MSG_TEXT)) {
      myTapHandler.setLogger(build.getBuildLogger());
      String text = (String) buildMessage.getValue();
      if (text.startsWith(ServiceMessage.SERVICE_MESSAGE_START)) return; // skip reported service messages

      myTapParser.parse(text);
    }
  }

  @Override
  public void runnerFinished(@NotNull BuildRunnerContext runner, @NotNull BuildFinishedStatus status) {
    super.runnerFinished(runner, status);
    myTapHandler.buildStepFinished();
  }

  private static class ServiceMessageConverter implements TapHandler {
    private BuildProgressLogger myLogger;
    private boolean myTapMode = false;

    public void version(int version) {
      myTapMode = true;
    }

    public void plan(int numTests, Directive directive) {
      myTapMode = true;
    }

    public synchronized void test(int orderNum, boolean ok, final String description, Directive directive) {
      if (!myTapMode) return;
      final String testName = description != null ? description : "test-" + orderNum;
      if (directive != null) {
        message(new TestIgnored(testName, directive.toString()).asString());
      } else {
        message(new TestStarted(testName, false, null).asString());
        if (!ok) {
          message(ServiceMessage.asString("testFailed", new HashMap<String, String>() {{
            put("name", testName);
            put("message", description);
          }}));
        }
        message(new TestFinished(testName, 0).asString());
      }
    }

    private void message(String text) {
      if (!myTapMode) return;
      myLogger.logMessage(createMessage(text, Status.NORMAL));
    }

    private void error(String text) {
      if (!myTapMode) return;
      myLogger.logMessage(createMessage(text, Status.ERROR));
    }

    private BuildMessage1 createMessage(String text, Status status) {
      return DefaultMessagesInfo.createTextMessage(text, status).updateTags(Arrays.asList(DefaultMessagesInfo.TAG_INTERNAL));
    }

    public void comment(String comment) {
    }

    public synchronized void bailout(String text) {
      if (!myTapMode) return;
      error(text);
    }

    public void unknown(String text) {
    }

    public void setLogger(BuildProgressLogger logger) {
      myLogger = logger;
    }

    public void buildStepFinished() {
      myTapMode = false;
    }
  }
}
