package nz.co.trineo.salesforce;

public class SalesforceException extends Exception {

	private static final long serialVersionUID = -6573552923567802051L;

	public SalesforceException() {
		super();
	}

	public SalesforceException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public SalesforceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public SalesforceException(String arg0) {
		super(arg0);
	}

	public SalesforceException(Throwable arg0) {
		super(arg0);
	}
}
