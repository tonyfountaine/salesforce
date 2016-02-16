package nz.co.trineo.diff;

public class DiffException extends Exception {

	private static final long serialVersionUID = 8497393413098218935L;

	public DiffException() {
		super();
	}

	public DiffException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DiffException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public DiffException(final String message) {
		super(message);
	}

	public DiffException(final Throwable cause) {
		super(cause);
	}
}
