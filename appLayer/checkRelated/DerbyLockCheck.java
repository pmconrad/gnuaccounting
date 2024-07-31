package appLayer.checkRelated;

import java.io.File;
import dataLayer.DB;
import GUILayer.Messages;
import appLayer.client;

public class DerbyLockCheck extends check {
	public DerbyLockCheck(IcheckInteractionProvider output,
			String testDescription) {
		super(output, testDescription);
	}

	public checkResult doTest() {
		if (DB.getConnection() == null) {

			String derbyLockFileName = client.getGlobalDataPath()
					+ "data" + DB.getDBVersionString() + ".lck"; //$NON-NLS-1$ //$NON-NLS-2$
			if ((new File(derbyLockFileName)).exists()) {
				boolean realProblem = false;
				boolean removeLock = outputProvider
						.confirm(
								Messages.getString("configWindow.removeLock"), Messages.getString("configWindow.anotherInstance") + derbyLockFileName + Messages.getString("configWindow.removeLockReally")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (!removeLock) {
					realProblem = true;
				}
				if (removeLock) {
					realProblem = !new File(derbyLockFileName).delete();
				}
				if (realProblem) {
					checkResult tr = new checkResult(
							false,
							Messages.getString("configWindow.pleaseQuit") + derbyLockFileName); //$NON-NLS-1$
					tr.setFatal();

				} else
					return new standardCheck();
			}
		}
		return new standardCheck();
	}

}
