package nz.co.trineo.git;

public class GitServiceException extends Exception {

	private static final long serialVersionUID = -1300924517315350898L;

	public GitServiceException() {
		super();
	}

	public GitServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GitServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public GitServiceException(String message) {
		super(message);
	}

	public GitServiceException(Throwable cause) {
		super(cause);
	}
}
