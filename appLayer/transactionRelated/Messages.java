package appLayer.transactionRelated;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "appLayer.transactionRelated.messages"; //$NON-NLS-1$
	public static String receiptIncoming_transactionName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
