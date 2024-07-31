package appLayer.checkRelated;

import java.io.File;
import java.io.IOException;

import ag.ion.bion.officelayer.OSHelper;
import ag.ion.bion.officelayer.application.ILazyApplicationInfo;
import appLayer.configs;

public class checkUtils {
	public static int checkOOo(String path) {
		if (OSHelper.IS_MAC) {
			path = path + File.separator + "Contents"; //$NON-NLS-1$
		}

		boolean pathExists = false;
		boolean subPathExists = false;
		boolean classesPathExists = false;
		boolean jarExists = false;
		int OOOMajorVersion = -1;

		if ((path == "") || (path == null)) { //$NON-NLS-1$
			// not yet configured
			return -1;
		}
		pathExists = new File(path).exists(); //$NON-NLS-1$
		if (pathExists) {
			subPathExists = new File(path + "/program").exists(); //$NON-NLS-1$
		}
		if (subPathExists) {
			try {
				OOOMajorVersion = getOOOMajorVersion(path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (OOOMajorVersion < 3) {
				// for openoffice.org 2 we need the officebean.jar, in ooo.o 3
				// it is neither needed nor there
				classesPathExists = new File(path + "/program/classes").exists(); //$NON-NLS-1$
			} else {
				classesPathExists = true; // OOO.o needs no classes path
			}
		}
		if (classesPathExists) {
			if (OOOMajorVersion < 3) {
				jarExists = new File(path + "/program/classes/officebean.jar").exists(); //$NON-NLS-1$
			} else {
				jarExists = true; // OOO.o 3 needs no officebean.jar
			}
		}
		if (!pathExists) {
			return -2;
		}
		if (!subPathExists) {
			return -3;

		}
		if (!classesPathExists) {
			return -4;
		}
		if (!jarExists) {
			return -5;
		}
		return 1;

	}

	private static int getOOOMajorVersion(String path) throws IOException {

		ILazyApplicationInfo appInfo = configs.getOfficeInfo();
		Integer appV = appInfo.getMajorVersion();
		return appV;

	}

}
