package server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;

import loadBalancing.LoadBalancer;

public class TestLoadBalancer {

	protected static final String CONNECTION_QUEUE_NAME = "TomSWS_connectionQueue";

	private LoadBalancer balancer;
	private Socket sock_connection;

	private Channel channel;
	private Connection msg_connection;
	
	private boolean waitForMessage;
	private String message;

	private Logger logger = LogManager.getLogger(this.getClass());
	
	@Before
	public void setup() throws IOException, TimeoutException {
		logger.info("setuping up test...");
		balancer = new LoadBalancer("web", 8080);
		sock_connection = Mockito.mock(Socket.class);

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		msg_connection = factory.newConnection();
		channel = msg_connection.createChannel();
		channel.queueDeclare(CONNECTION_QUEUE_NAME, true, false, false, null);
		channel.basicQos(1);
		logger.info("setup successful.");

		waitForMessage = true;
		message = "nothing has been received";
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
						AMQP.BasicProperties properties, byte[] body) throws IOException {
				logger.info("handling delivery");
				message = new String(body, "UTF-8");
				waitForMessage = false;
				channel.basicAck(envelope.getDeliveryTag(), false);
				logger.info("delivery handled");
			}
		};
		channel.basicConsume(CONNECTION_QUEUE_NAME, false, consumer);
		logger.info("basic consume set");
		
	}
	
	@After
	public void tearDown() throws IOException, TimeoutException {
		channel.close();
		msg_connection.close();
	}
	

	@Test(timeout=1000)
	public void runExitsForStop() {
		balancer.stop();
		balancer.run();
	}
	
}
