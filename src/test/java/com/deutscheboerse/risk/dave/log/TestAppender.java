package com.deutscheboerse.risk.dave.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TestAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private final String className;
    private final Map<Level, List<ILoggingEvent>> levelListMap = new ConcurrentHashMap<>();

    private TestAppender(String className) {
        this.className = className;
        this.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
    }

    public static TestAppender getAppender(final Class<?> clazz) {
        return new TestAppender(clazz.getName());
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (event.getLoggerName().equals(this.className)) {
            synchronized(this) {
                this.getList(event.getLevel()).add(event);
                this.notifyAll();
            }
        }
    }

    public ILoggingEvent getLastMessage(Level level) throws InterruptedException {
        synchronized(this) {
            while (this.getList(level).isEmpty()) {
                this.wait(5000);
            }
            return this.getList(level).get(this.getList(level).size() - 1);
        }
    }

    public void waitForMessageCount(Level level, int count) throws InterruptedException {
        synchronized(this) {
            while (this.getList(level).size() < count) {
                this.wait(5000);
            }
        }
    }

    public void waitForMessageContains(Level level, String message) throws InterruptedException {
        synchronized(this) {
            while (!findHelper(level, message).isPresent()) {
                this.wait(5000);
            }
        }
    }

    public Optional<ILoggingEvent> findMessage(Level level, String message) {
        synchronized(this) {
            return findHelper(level, message);
        }
    }

    private Optional<ILoggingEvent> findHelper(Level level, String message) {
        for (ILoggingEvent event : this.getList(level)) {
            if (event.getFormattedMessage().replace("\n", "").contains(message)) {
                return Optional.of(event);
            }
        }
        return Optional.empty();
    }

    private List<ILoggingEvent> getList(Level level) {
        return this.levelListMap.computeIfAbsent(level, i -> new ArrayList<>());
    }

    public void clear() {
        this.levelListMap.clear();
    }
}