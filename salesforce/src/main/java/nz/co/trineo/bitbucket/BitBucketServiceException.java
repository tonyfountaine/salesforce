package nz.co.trineo.bitbucket;

import nz.co.trineo.repo.RepoServiceException;

public class BitBucketServiceException extends RepoServiceException {

	private static final long serialVersionUID = -1300924517315350898L;

	public BitBucketServiceException() {
		super();
	}

	public BitBucketServiceException(final String message) {
		super(message);
	}

	public BitBucketServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public BitBucketServiceException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BitBucketServiceException(final Throwable cause) {
		super(cause);
	}
}
