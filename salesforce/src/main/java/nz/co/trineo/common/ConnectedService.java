package nz.co.trineo.common;

public interface ConnectedService {
	String getName();

	boolean usesOAuth();

	String getClientId();

	String getClientSecret();
}
