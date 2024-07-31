package dataLayer;

import org.eclipse.swt.widgets.Shell;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.callback.HBCICallbackSwing;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.status.HBCIExecStatus;

import GUILayer.HBCIcallbackDialog;
import appLayer.client;
import appLayer.configs;
import appLayer.utils;

public class HBCI {
	private static HBCI _instance;

	private HBCIPassport passport;
	private HBCIHandler hbciHandle;
	private Shell sh;

	// private Container abortWindow;

	public static synchronized HBCI getInstance(Shell sh) {
		if (_instance == null) {
			_instance = new HBCI(sh);
		}
		return _instance;
	}

	private HBCI(Shell sh) {
		this.sh = sh;
		try {

			HBCICallbackSwing callback = new HBCIcallbackDialog();

			HBCIUtils.init(null, callback);
			HBCIUtils.setParam("log.loglevel.default", "3"); //$NON-NLS-1$ //$NON-NLS-2$
			HBCIUtils.setParam("log.filter", "2"); //$NON-NLS-1$ //$NON-NLS-2$
			// HBCIUtils.setParam("log.loglevel.default",Config.getInstance().get("hbci.loglevel","3"));
			// HBCIUtils.setParam("log.filter",Config.getInstance().get("hbci.filterlevel","2"));
		} catch (Exception e) {
			utils.logAndShowException(sh, e);
		}
	}

	public synchronized void setParam(String key, String value) {
		HBCIUtils.setParam(key, value);
	}

	private synchronized void initPassport() {
		try {

			/*
			 * setParam("client.passport.default","PinTan");
			 * System.err.println(client.getDataPath()+File.separator+"pintan");
			 * setParam
			 * ("client.passport.PinTan.filename",client.getDataPath()+File
			 * .separator+"pintan");
			 * setParam("client.passport.PinTan.checkcert","1");
			 * setParam("client.passport.PinTan.init","1");
			 */

			setParam("client.passport.default", "DDV"); //$NON-NLS-1$ //$NON-NLS-2$
			setParam("client.passport.DDV.path", client.getDataPath());// the "passport" file will be stored in this directory -- just enter one where you have write privileges //$NON-NLS-1$
			String ddvLibName = ""; //$NON-NLS-1$
			String libName = "libhbci4java-card-linux.so"; //$NON-NLS-1$
			if (System.getProperty("os.name").startsWith("Windows")) { //$NON-NLS-1$ //$NON-NLS-2$
				libName = "hbci4java-card-win32.dll"; //$NON-NLS-1$
			}
			ddvLibName = System.getProperty("user.dir") + System.getProperty("file.separator") + "libs" + System.getProperty("file.separator") + "hbci" + System.getProperty("file.separator") + libName; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			setParam("client.passport.DDV.libname.ddv", ddvLibName); //$NON-NLS-1$
			setParam("client.passport.DDV.libname.ctapi", configs.getCtAPI()); //$NON-NLS-1$
			setParam("client.passport.DDV.port", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			setParam("client.passport.DDV.ctnumber", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			setParam("client.passport.DDV.usebio", "-1"); //$NON-NLS-1$ //$NON-NLS-2$
			String softPin = "1"; //$NON-NLS-1$
			if (configs.shallUseCardReaderPINPad()) {
				softPin = "-1"; //$NON-NLS-1$
			}
			setParam("client.passport.DDV.softpin", softPin); //I have a class3-reader. If the PIN was to be entered on the computre, not on the reader, replace -1 by 1. //$NON-NLS-1$ //$NON-NLS-2$
			setParam("client.passport.DDV.entryidx", "1"); //$NON-NLS-1$ //$NON-NLS-2$

			passport = AbstractHBCIPassport.getInstance();
		} catch (Exception ex) {
			passport = null;
			ex.printStackTrace();
			throw new RuntimeException("", ex); //$NON-NLS-1$
		}

		try {
			String pversion = "210";//passport.getHBCIVersion(); will return the last used version, and empty string if not yet used previously. 2.10=210 is a safe assumption for my account //$NON-NLS-1$
			hbciHandle = new HBCIHandler(pversion, passport);
		} catch (Exception ex) {
			try {
				passport.close();
			} catch (Exception ex1) {
			}
			passport = null;
			utils.logAndShowException(sh, ex);
		}
	}

	public synchronized HBCIPassport getPassport() {
		if (passport == null) {
			initPassport();
		}

		return passport;
	}

	public HBCIPassport getCurrentPassport() {
		return passport;
	}

	public synchronized void closePassport() {
		if (hbciHandle != null) {
			try {
				hbciHandle.close();
			} catch (Exception e) {
				try {
					passport.close();
				} catch (Exception ex) {
				}
			} finally {
				passport = null;
				hbciHandle = null;
			}
		}
	}

	public synchronized HBCIJob newJob(String jobname) {
		getPassport();

		try {
			return hbciHandle.newJob(jobname);
		} catch (Exception e) {
			utils.logAndShowException(sh, e);
			throw new RuntimeException("", e); //$NON-NLS-1$
		}
	}

	public synchronized void addJob(String customerid, HBCIJob job) {
		getPassport();

		try {
			job.addToQueue(customerid);
		} catch (Exception e) {
			utils.logAndShowException(sh, e);
			throw new RuntimeException("", e); //$NON-NLS-1$
		}
	}

	public synchronized HBCIExecStatus execute(String headline) {
		getPassport();

		try {
			showAbortWindow();
			HBCIExecStatus status = hbciHandle.execute();
			hideAbortWindow();

			return status;
		} catch (Exception e) {
			hideAbortWindow();
			utils.logAndShowException(sh, e);
			throw new RuntimeException("", e); //$NON-NLS-1$
		}
	}

	private void showAbortWindow() {
	}

	private synchronized void hideAbortWindow() {
	}
}
