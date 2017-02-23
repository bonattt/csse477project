package server;

import java.io.OutputStream;
import java.net.Socket;

import iServer.IConnectionHandler;
import iServer.IRequestHandler;

public class MessageHandler implements IConnectionHandler {

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean methodSupported(String method) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addHandler(String method, IRequestHandler handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleRequest(OutputStream outStream) {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeSocket() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Socket getSocket() {
		// TODO Auto-generated method stub
		return null;
	}

}
