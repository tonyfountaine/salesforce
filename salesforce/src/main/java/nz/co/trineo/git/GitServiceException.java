package nz.co.trineo.git;

import nz.co.trineo.repo.RepoServiceException;

public class GitServiceException extends RepoServiceException {

	private static final long serialVersionUID = -1300924517315350898L;

	public GitServiceException() {
		super();
	}

	public GitServiceException(final String message) {
		super(message);
	}

	public GitServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GitServiceException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GitServiceException(final Throwable cause) {
		super(cause);
	}
}
