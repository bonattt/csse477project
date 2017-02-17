package iServer;

import java.io.OutputStream;
import java.net.Socket;

public interface IConnectionHandler extends Runnable {
	public boolean methodSupported(String method);
	public void addHandler(String method, IRequestHandler handler);
	public void handleRequest(OutputStream outStream);
	public void closeSocket();
	public boolean isClosed();
	public Socket getSocket();
}
