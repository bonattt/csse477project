package iServer;

public interface IServlet {
	public String getPreferedContextName();
	public IRequestHandler getHandler(String uri);
}
