package appLayer.transactionRelated;

import appLayer.Messages;

public class transactionTypeNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public transactionTypeNotFoundException(String name) {
		super(
				Messages.getString("transactionTypeNotFoundException.transTypeNotFoundPart1") + name + Messages.getString("transactionTypeNotFoundException.transTypeNotFoundPart2")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
