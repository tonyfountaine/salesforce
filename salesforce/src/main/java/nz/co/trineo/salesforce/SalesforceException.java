package nz.co.trineo.salesforce;

public class SalesforceException extends Exception {

	private static final long serialVersionUID = -6573552923567802051L;

	public SalesforceException() {
		super();
	}

	public SalesforceException(final String arg0, final Throwable arg1, final boolean arg2, final boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public SalesforceException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	public SalesforceException(final String arg0) {
		super(arg0);
	}

	public SalesforceException(final Throwable arg0) {
		super(arg0);
	}
}
