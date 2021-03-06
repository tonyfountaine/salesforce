package nz.co.trineo.github;

import nz.co.trineo.repo.RepoServiceException;

public class GitHubServiceException extends RepoServiceException {

	private static final long serialVersionUID = -1300924517315350898L;

	public GitHubServiceException() {
		super();
	}

	public GitHubServiceException(final String message) {
		super(message);
	}

	public GitHubServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GitHubServiceException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GitHubServiceException(final Throwable cause) {
		super(cause);
	}
}
