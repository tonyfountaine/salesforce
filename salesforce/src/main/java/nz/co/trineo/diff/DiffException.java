package nz.co.trineo.diff;

public class DiffException extends Exception {

	private static final long serialVersionUID = 8497393413098218935L;

	public DiffException() {
		super();
	}

	public DiffException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DiffException(String message, Throwable cause) {
		super(message, cause);
	}

	public DiffException(String message) {
		super(message);
	}

	public DiffException(Throwable cause) {
		super(cause);
	}
}
