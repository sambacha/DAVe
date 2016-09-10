package com.opnfi.risk;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by jakub on 03.09.16.
 */
@RunWith(VertxUnitRunner.class)
public class ERSDebuggerTest {
    private static final JsonObject testMessage = new JsonObject().put("test", "message");

    private static Vertx vertx;
    private static int port;

    private static Appender<ILoggingEvent> testAppender;

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        ERSDebuggerTest.vertx = Vertx.vertx();

        JsonObject config = new JsonObject().put("httpPort", port);
        vertx.deployVerticle(ERSDebuggerVerticle.class.getName(), context.asyncAssertSuccess());

        testAppender = new TestAppender<>();
        testAppender.setContext((LoggerContext)LoggerFactory.getILoggerFactory());
        Logger logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(testAppender);
        logger.setLevel(Level.TRACE);
    }

    @Test
    public void testTradingSessionStatusLogging(TestContext context) throws InterruptedException {
        testAppender.start();

        vertx.eventBus().publish("ers.TradingSessionStatus", testMessage);

        Thread.sleep(1000);

        testAppender.stop();

        ILoggingEvent logMessage = ((TestAppender)testAppender).getLastMessage();
        context.assertEquals(Level.TRACE, logMessage.getLevel());
        context.assertTrue(logMessage.getFormattedMessage().contains(Json.encode(testMessage)));
        context.assertTrue(logMessage.getFormattedMessage().contains("Received TSS message with body:"));
    }

    @Test
    public void testPositionReportLogging(TestContext context) throws InterruptedException {
        testAppender.start();

        vertx.eventBus().publish("ers.PositionReport", testMessage);

        Thread.sleep(1000);

        testAppender.stop();

        ILoggingEvent logMessage = ((TestAppender)testAppender).getLastMessage();
        context.assertEquals(Level.TRACE, logMessage.getLevel());
        context.assertTrue(logMessage.getFormattedMessage().contains(Json.encode(testMessage)));
        context.assertTrue(logMessage.getFormattedMessage().contains("Received PR message with body:"));
    }

    @Test
    public void testMarginComponentLogging(TestContext context) throws InterruptedException {
        testAppender.start();

        vertx.eventBus().publish("ers.MarginComponent", testMessage);

        Thread.sleep(1000);

        testAppender.stop();

        ILoggingEvent logMessage = ((TestAppender)testAppender).getLastMessage();
        context.assertEquals(Level.TRACE, logMessage.getLevel());
        context.assertTrue(logMessage.getFormattedMessage().contains(Json.encode(testMessage)));
        context.assertTrue(logMessage.getFormattedMessage().contains("Received MC message with body:"));
    }

    @Test
    public void testTotalMarginRequirementLogging(TestContext context) throws InterruptedException {
        testAppender.start();

        vertx.eventBus().publish("ers.TotalMarginRequirement", testMessage);

        Thread.sleep(1000);

        testAppender.stop();

        ILoggingEvent logMessage = ((TestAppender)testAppender).getLastMessage();
        context.assertEquals(Level.TRACE, logMessage.getLevel());
        context.assertTrue(logMessage.getFormattedMessage().contains(Json.encode(testMessage)));
        context.assertTrue(logMessage.getFormattedMessage().contains("Received TMR message with body:"));
    }

    @Test
    public void testMarginSortfallSurplusLogging(TestContext context) throws InterruptedException {
        testAppender.start();

        vertx.eventBus().publish("ers.MarginShortfallSurplus", testMessage);

        Thread.sleep(1000);

        testAppender.stop();

        ILoggingEvent logMessage = ((TestAppender)testAppender).getLastMessage();
        context.assertEquals(Level.TRACE, logMessage.getLevel());
        context.assertTrue(logMessage.getFormattedMessage().contains(Json.encode(testMessage)));
        context.assertTrue(logMessage.getFormattedMessage().contains("Received MSS message with body:"));
    }

    @Test
    public void testRiskLimitLogging(TestContext context) throws InterruptedException {
        testAppender.start();

        vertx.eventBus().publish("ers.RiskLimit", testMessage);

        Thread.sleep(1000);

        testAppender.stop();

        ILoggingEvent logMessage = ((TestAppender)testAppender).getLastMessage();
        context.assertEquals(Level.TRACE, logMessage.getLevel());
        context.assertTrue(logMessage.getFormattedMessage().contains(Json.encode(testMessage)));
        context.assertTrue(logMessage.getFormattedMessage().contains("Received RiskLimit message with body:"));
    }

    @After
    public void stopAppender(TestContext context)
    {
        testAppender.stop();
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    static class TestAppender<E> extends UnsynchronizedAppenderBase<E>
    {
        private String name = "TestLogger";
        private ILoggingEvent lastLogMessage;

        @Override
        protected void append(Object o) {
            if (o instanceof ILoggingEvent) {
                ILoggingEvent event = (ILoggingEvent)o;

                if (event.getLoggerName().equals(ERSDebuggerVerticle.class.getName())) {
                    lastLogMessage = event;
                }
            }
        }

        public ILoggingEvent getLastMessage()
        {
            return lastLogMessage;
        }
    }
}
