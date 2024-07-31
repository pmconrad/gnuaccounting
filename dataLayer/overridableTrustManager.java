package dataLayer;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import GUILayer.newTransactionWizard;

interface Idenyable {
	public void approve(boolean yesNo);
}

public class overridableTrustManager implements X509TrustManager, Idenyable {
	// based on http://www.devx.com/tips/Tip/30077
	X509TrustManager X509TM = null; // default X.509 TrustManager
	TrustManagerFactory ClientTMF = null; // SunX509 factory from SunJSSE
											// provider
	KeyStore ClientKS = null; // keystore SSLCert - just an example

	TrustManager[] ClientTMs = null; // all the TrustManagers from SunX509
										// factory
	char[] ClientKeystorePassword = "njlh\"$\"@@sdlfs00GZfg9rjkfn".toCharArray();//SSLCert access password //$NON-NLS-1$
	boolean isTrusted = false;
	private boolean certificateAccepted = false;

	public overridableTrustManager() {
		// get an KeyStore object of type JKS (default type)
		try {
			ClientKS = KeyStore.getInstance("JKS"); //$NON-NLS-1$
		} catch (java.security.KeyStoreException e) {
			System.out.println("1: " + e.getMessage());} //$NON-NLS-1$

		// loading SSLCert keystore
		try {
			ClientKS.load(
					new FileInputStream("SSLKeystore"), ClientKeystorePassword); //$NON-NLS-1$
		} catch (java.io.IOException e) {
			System.out.println("2: " + e.getMessage()); //$NON-NLS-1$
		} catch (java.security.NoSuchAlgorithmException e) {
			System.out.println("3: " + e.getMessage()); //$NON-NLS-1$
		} catch (java.security.cert.CertificateException e) {
			System.out.println("4: " + e.getMessage()); //$NON-NLS-1$
		}

		// TrustManagerFactory of SunJSSE
		try {
			ClientTMF = TrustManagerFactory.getInstance("SunX509", "SunJSSE"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (java.security.NoSuchAlgorithmException e) {
			System.out.println("5: " + e.getMessage()); //$NON-NLS-1$
		} catch (java.security.NoSuchProviderException e) {
			System.out.println("6: " + e.getMessage());} //$NON-NLS-1$

		// call init method for ClientTMF
		try {
			ClientTMF.init(ClientKS);
		} catch (java.security.KeyStoreException e) {
			System.out.println("7: " + e.getMessage());} //$NON-NLS-1$

		// get all the TrustManagers
		ClientTMs = ClientTMF.getTrustManagers();

		// looking for a X509TrustManager instance
		for (int i = 0; i < ClientTMs.length; i++) {
			if (ClientTMs[i] instanceof X509TrustManager) {
				System.out.println("X509TrustManager certificate found..."); //$NON-NLS-1$
				X509TM = (X509TrustManager) ClientTMs[i];
				return;
			}
		}

	}

	public void checkClientTrusted(X509Certificate[] cert, String authType)
			throws CertificateException {
		try {
			X509TM.checkClientTrusted(cert, authType);
			approve(true);
		} catch (CertificateException e) {
			certificateAccepted = false;
			Thread question = new Thread(new Runnable() {
				public void run() { // see
									// http://wiki.eclipse.org/FAQ_Why_do_I_get_an_invalid_thread_access_exception%3F
									// for this construct...
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							approve(MessageDialog.openConfirm(
									newTransactionWizard.getActiveShell(),
									Messages.getString("overridableTrustManager.authErrorHeading"), Messages.getString("overridableTrustManager.authErrorText"))); //$NON-NLS-1$ //$NON-NLS-2$

						}
					});
				}
			});
			question.start();
			try {
				question.join(); // do not continue mailThread unless the user
									// has answered if (s)he trusts the
									// certificate
				if (!certificateAccepted) {
					throw e;
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	public void checkServerTrusted(X509Certificate[] cert, String authType)
			throws CertificateException {
		try {
			X509TM.checkServerTrusted(cert, authType);
			approve(true);
		} catch (CertificateException e) {
			certificateAccepted = false;
			Thread question = new Thread(new Runnable() {
				public void run() { // see
									// http://wiki.eclipse.org/FAQ_Why_do_I_get_an_invalid_thread_access_exception%3F
									// for this construct...
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							approve(MessageDialog.openConfirm(
									newTransactionWizard.getActiveShell(),
									Messages.getString("overridableTrustManager.authErrorHeading"), Messages.getString("overridableTrustManager.authErrorText"))); //$NON-NLS-1$ //$NON-NLS-2$

						}
					});
				}
			});
			question.start();
			try {
				question.join(); // do not continue mailThread unless the user
									// has answered if (s)he trusts the
									// certificate
				if (!certificateAccepted) {
					throw e;
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return X509TM.getAcceptedIssuers();

	}

	public boolean isTrusted() {
		return isTrusted;

	}

	@Override
	public void approve(boolean isApproved) {
		if (isApproved) {
			certificateAccepted = true;
		} else {
			certificateAccepted = false;
		}

	}

}
