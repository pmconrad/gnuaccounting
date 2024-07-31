package GUILayer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;

import javax.imageio.ImageIO;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import appLayer.client;

class scanSavePicThread extends Thread {

	
	private BufferedImage scannedImage;
	private Shell parentShell;
	private IscanProgress currentProgress;// nullable

	public scanSavePicThread(BufferedImage image, Shell sh,
			IscanProgress progressMonitor) {
		scannedImage = image;
		parentShell = sh;
		currentProgress = progressMonitor;
	}

	public void run() {
		try {
			String filename = client.getNextScanFilename();
			String docNr = ""; //$NON-NLS-1$
			MessageDialog
					.open(MessageDialog.INFORMATION,
							parentShell,
							Messages.getString("documentsWindow.scanSucessfulLabel"), Messages.getString("documentsWindow.scanSuccesfulText") + filename, SWT.NONE); //$NON-NLS-1$ //$NON-NLS-2$
			File fw = new File(filename);
			ImageIO.write(scannedImage, "png", fw); //$NON-NLS-1$

			// look for a barcode
			URI uri = fw.toURI();
			docNr = BufferedImageLuminanceSource.scanForBarcode(uri);

			if (currentProgress != null) {
				currentProgress.onComplete(filename, docNr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
