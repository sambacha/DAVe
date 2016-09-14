package com.deutscheboerse.risk.dave.utils;

import java.util.Properties;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Utils
{
    public AutoCloseableConnection getAdminConnection(String hostname, int port) throws JMSException, NamingException
    {
        return new ConnectionBuilder().hostname(hostname).port(Integer.toString(port)).build();
    }

    public Queue getQueue(String queueName) throws NamingException
    {
        Properties props = new Properties();
        props.setProperty("java.naming.factory.initial", "org.apache.qpid.jndi.PropertiesFileInitialContextFactory");
        props.setProperty("destination.queue", queueName + "; { node: { type: queue }, create: never, assert: never }");

        InitialContext ctx = new InitialContext(props);

        return (Queue)ctx.lookup("queue");
    }

    public Topic getTopic(String topicName) throws NamingException
    {
        Properties props = new Properties();
        props.setProperty("java.naming.factory.initial", "org.apache.qpid.jndi.PropertiesFileInitialContextFactory");
        props.setProperty("destination.topic", topicName + "; { node: { type: topic }, create: never, assert: never }");

        InitialContext ctx = new InitialContext(props);

        return (Topic)ctx.lookup("topic");
    }
}