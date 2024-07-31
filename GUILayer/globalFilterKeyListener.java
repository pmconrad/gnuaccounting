package GUILayer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import appLayer.IbarcodeListener;
import appLayer.client;
import appLayer.product;

public class globalFilterKeyListener implements Listener {
	static final int F1 = 16777226;
	static final int F12 = 16777237;
	private long lastKeyStrokeTime = 0;
	private StringBuffer typedString = new StringBuffer();
	static final int timeoutMS = 30;
	static HashMap<String, product> barcodes = new HashMap<String, product>();
	static int minBarcodeLength = Integer.MAX_VALUE;
	static private IbarcodeListener listeners = null;

	public globalFilterKeyListener() {
		refreshBarcodes();
	}

	public static void addListener(IbarcodeListener listener) {
		listeners = listener;
	}

	public static void refreshBarcodes() {
		minBarcodeLength = Integer.MAX_VALUE;
		barcodes.clear();
		for (product currentProduct : client.getProducts().getProducts()) {
			if (!currentProduct.getBarcode().isEmpty()) {
				barcodes.put(currentProduct.getBarcode(), currentProduct);
				if (currentProduct.getBarcode().length() < minBarcodeLength) {
					minBarcodeLength = currentProduct.getBarcode().length();
				}
			}
		}

	}

	@Override
	public void handleEvent(Event event) {
		long currentTime = System.currentTimeMillis();

		handleSingleKeyAction(event);
		if (lastKeyStrokeTime + timeoutMS > currentTime) {
			typedString = typedString.append(event.character);
		} else {
			// timeout
			typedString = new StringBuffer();
			typedString = typedString.append(event.character);
		}

		if (typedString.length() >= minBarcodeLength) {
			if (barcodes.containsKey(typedString.toString())) {
				/*
				 * this is probably an scanned barcode event, unfortunately we
				 * can't check for CR because if the focus rests on a control
				 * the CR will be consumed by the control, the only way to
				 * prevent being a traverselistener on each control
				 */
				if (listeners != null) {
					listeners.barcodeReceived(typedString.toString(),
							barcodes.get(typedString.toString()));
				}
			}
		}
		lastKeyStrokeTime = currentTime;

	}

	private void handleSingleKeyAction(Event event) {
		if (event.keyCode == F1) {
			Control c = null;
			c = (Control) event.widget;
			boolean eventConsumed = false;
			while (c != null) {

				Class[] faces = c.getClass().getInterfaces();
				for (Class currentInterface : faces) {
					if (currentInterface.getName().equals(
							"appLayer.IcontextProvider")) { //$NON-NLS-1$
/*						browserWindow helpWin = new browserWindow(
								((contextComposite) c).getCurrentHelpURL());
						helpWin.open();*/
						eventConsumed = true;
					}

				}
				c = c.getParent();
			}
			if (!eventConsumed) {
				String filename;
				try {
					filename = new File(".").getCanonicalFile().toURI() + Messages.getString("globalFilterKeyListener.manualFilename"); //$NON-NLS-1$ //$NON-NLS-2$
					viewerWindow helpWin = new viewerWindow(filename); //$NON-NLS-1$
					helpWin.setReadonly();
					helpWin.open();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				eventConsumed = true;

			}

		}
		if (event.keyCode == F12) {
			spareWindow sw = new spareWindow();
			sw.open();
		}

	}

}
