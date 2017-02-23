package loadBalancing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import iServer.IConnectionHandler;
import iServer.IRequestHandler;
import iServer.IServer;
import protocol.HttpRequest;
import protocol.HttpResponse;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;


public class BalanceHandler implements IConnectionHandler {

	private IServer server;
	private Socket socket;
	boolean closed;
	private final int connectionNumber;
	private Logger logger = LogManager.getLogger(this.getClass());
	
	private Channel reqChannel, resChannel;
	private Connection reqConnection, resConnection;
	private String reqQueueName, resQueueName;
	

	public BalanceHandler(IServer server, Socket socket, int connectionNumber) {
		this.server = server;
		this.socket = socket;
		this.closed = false;
		this.connectionNumber = connectionNumber;
		logger.info(String.format("connection %d: Balance Handler successfully constructed.",
				connectionNumber));
	}

	private String connectionStr() {
		return String.format("connection %d: ", connectionNumber); 
	}
	
	public void setupRequestForwarding(String queueName) throws IOException, TimeoutException {
		this.reqQueueName = queueName;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		reqConnection = factory.newConnection();
		reqChannel = reqConnection.createChannel();
		reqChannel.queueDeclare(queueName, true, false, false, null);
	}

	private void forwardRequest(byte[] body) throws IOException {
		reqChannel.basicPublish("", reqQueueName,
					MessageProperties.PERSISTENT_TEXT_PLAIN,
					body);
		logger.info("Sent '" + body + "'");
	}
	
	private void sendResponse(byte[] body) {
		logger.info(connectionStr() + "sending response.");
		try {
			OutputStream outStream = socket.getOutputStream();
			outStream.write(body);
			logger.info(connectionStr() + "response has been successfully sent.");
//			System.out.println(response);
		}
		catch(Exception e){
			// We will ignore this exception
			logger.error(connectionStr() + "an unknown error occured while sending the response.");
		}
	}

//	private void sendResponse(HttpResponse response, OutputStream outStream) {
//		logger.info(connectionStr() + "sending response.");
//		try {
//			response.write(outStream);
//			logger.info(connectionStr() + "response has been successfully sent.");
////			System.out.println(response);
//		}
//		catch(Exception e){
//			// We will ignore this exception
//			logger.error(connectionStr() + "an unknown error occured while sending the response.", e);
//			e.printStackTrace();
//		}
//	}

	public void setupResponseForwarding(String queueName) throws IOException, TimeoutException {
		this.resQueueName = queueName;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		resConnection = factory.newConnection();
		resChannel = resConnection.createChannel();
		resChannel.queueDeclare(queueName, true, false, false, null);
		resChannel.basicQos(1);
		final Consumer consumer = new DefaultConsumer(resChannel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
						AMQP.BasicProperties properties, byte[] body) throws IOException {
				sendResponse(body);
				resChannel.basicAck(envelope.getDeliveryTag(), false);
			}
		};
		resChannel.basicConsume(queueName, false, consumer);
	}

	private void closeAll() {
		try {
			reqChannel.close();
		} catch (IOException e) {
			logger.error("IO exception in closeAll on requestChannel.close(): " + e.getMessage());
		} catch (TimeoutException e) {
			logger.error("Timeout exception in closeAll on requestChannel.close(): " + e.getMessage());
		}
		try {
			reqConnection.close();
		} catch (IOException e) {
			logger.error("IO exception in closeAll on requestConnection.close(): " + e.getMessage());
		}
		try {
			resChannel.close();
		} catch (IOException e) {
			logger.error("IO exception in closeAll on responseChannel.close(): " + e.getMessage());
		} catch (TimeoutException e) {
			logger.error("Timeout exception in closeAll on responseChannel.close(): " + e.getMessage());
		}
		
		try {
			resConnection.close();
		} catch (IOException e) {
			logger.error("IO exception in closeAll on responseConnection.close(): " + e.getMessage());
		}
	}
		
	@Override
	public void run() {
		InputStream inStream = null;
		try {
			inStream = socket.getInputStream();
		} catch (Exception e) {
			logger.error(connectionStr() + "unable to open socket in/out stream(S).", e);
			e.printStackTrace();
			return;
		}
		try {
			forwardRequest(toByteArray(inStream));
		} catch (IOException e) {
			logger.error("IO exception during forwardRequest: " + e.getMessage());
		}
	}
	
	private byte[] toByteArray(InputStream inStream) throws IOException {
		byte[] bytes = new byte[inStream.available()];
		inStream.read(bytes);
		return bytes;
	}

	@Override
	public boolean methodSupported(String method) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addHandler(String method, IRequestHandler handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void handleRequest(OutputStream outStream) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeSocket() {
		if (closed) {
			logger.warn("socket already closed!");
			return;
		}
		logger.info("attempting to close connection");
		try {
			socket.close();
			closed = true;
			logger.info("successfully closed the socket!");
		} catch (IOException e) {
			logger.error("IO exception trying to close socket: " + e.getMessage());
		}
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public Socket getSocket() {
		return socket;
	}

}
