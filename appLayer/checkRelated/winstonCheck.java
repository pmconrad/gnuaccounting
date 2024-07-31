package appLayer.checkRelated;

import java.io.File;
import appLayer.Messages;
import appLayer.configs;

public class winstonCheck extends check {
	public winstonCheck(IcheckInteractionProvider output, String testDescription) {
		super(output, testDescription);
	}

	public checkResult doTest() {
		if (configs.getWinstonPath().length() == 0) {
			return new checkResult(true, Messages.getString("winstonCheck.notConfigured")); //$NON-NLS-1$

		} else if (!(new File(configs.getWinstonPath())).exists()) {
			checkResult cr = new checkResult(false, String.format(
					Messages.getString("winstonCheck.pathNotFound"), configs.getWinstonPath())); //$NON-NLS-1$
			cr.setFatal();
			return cr;
		} else {
			return new checkResult(true, Messages.getString("winstonCheck.OK")); //$NON-NLS-1$

		}
	}
}
