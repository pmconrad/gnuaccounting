package appLayer.checkRelated;

import GUILayer.Messages;
import appLayer.configs;

public class databaseDriverNameCheck extends check {
	public databaseDriverNameCheck(IcheckInteractionProvider output,
			String testDescription) {
		super(output, testDescription);
	}

	public checkResult doTest() {

		if (configs.getDatabaseDriverName() == null) {
			checkResult tr = new checkResult(false,
					Messages.getString("configWindow.failed")); //$NON-NLS-1$
			tr.setFatal();
		}
		return new standardCheck();
	}

}
