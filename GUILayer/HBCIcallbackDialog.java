package GUILayer;

import org.kapott.hbci.callback.HBCICallbackSwing;
import org.kapott.hbci.passport.HBCIPassport;

public class HBCIcallbackDialog extends HBCICallbackSwing {
	@Override
	public void callback(final HBCIPassport passport, int reason, String msg,
			int datatype, StringBuffer retData) {
		if (reason == NEED_CONNECTION || reason == CLOSE_CONNECTION) {
			//	      System.out.println("internet connection is always available"); //$NON-NLS-1$
		} else {
			super.callback(passport, reason, msg, datatype, retData);
		}
	}
}
