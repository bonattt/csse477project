package server;

public class RequestAddress {
	
	private String addr, uri, context, filename;
	
	public RequestAddress(String addr) {
		this.addr = addr.replace('\\', '/');
		if (! this.addr.startsWith("/")) {
			this.addr = "/" + this.addr;
		}
		this.context = parseOutContext(this.addr);
		this.uri = parseOutUri(this.addr);
		this.filename = parseOutFilename(this.addr);
	}

	public static String parseOutUri(String addr) {
		String subStr = addr.substring(1);
		int index = subStr.indexOf('/');
		if (index == -1) {
			return "";
		}
		return addr.substring(index+1);
	}

	public static String parseOutContext(String addr) {
		String subStr = addr.substring(1);
		int index = subStr.indexOf('/');
		if (index == -1) {
			return subStr;
		}
		return subStr.substring(0, index);
	}
	
	public static String parseOutFilename(String addr) {
		int index = addr.lastIndexOf('/');
		if (index == 0) {
			return "";
		}
		return addr.substring(index+1);
	}
	
	public String getUri() {
		return uri;
	}
	
	public String getContext() {
		return context;
	}

	public String getFullAddr() {
		return addr;
	}

	public String getFilename() {
		return filename;
	}
	
}
