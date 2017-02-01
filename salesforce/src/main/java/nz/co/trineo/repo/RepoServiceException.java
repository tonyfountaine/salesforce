package nz.co.trineo.repo;

public class RepoServiceException extends Exception {

	private static final long serialVersionUID = -8748092894347422352L;

	public RepoServiceException() {
	}

	public RepoServiceException(String message) {
		super(message);
	}

	public RepoServiceException(Throwable cause) {
		super(cause);
	}

	public RepoServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public RepoServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
