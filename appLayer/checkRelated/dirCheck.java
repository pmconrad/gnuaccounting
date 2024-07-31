package appLayer.checkRelated;

import java.io.File;
import GUILayer.Messages;
import appLayer.client;

public class dirCheck extends check {
	public dirCheck(IcheckInteractionProvider output, String testDescription) {
		super(output, testDescription);
	}

	public checkResult doTest() {
		if (!(new File(client.getInterfaceProcessedDir())).exists()) {
			// as of 0.7.9
			(new File(client.getInterfaceProcessedDir())).mkdirs();
		}
		if (!(new File(client.getDataPath())).exists()) {
			new File(client.getDataPath()).mkdirs();

			return new checkResult(true,
					Messages.getString("configWindow.created")); //$NON-NLS-1$
		} else {
			return new standardCheck();
			// testConcluded = true;
		}

	}
}
