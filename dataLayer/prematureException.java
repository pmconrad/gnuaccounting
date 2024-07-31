package dataLayer;

public class prematureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public prematureException(String message) {
		super(
				Messages.getString("prematureException.writeThreadStillRunning") + message); //$NON-NLS-1$
	}

}
