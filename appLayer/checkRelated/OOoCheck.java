package appLayer.checkRelated;

import appLayer.configs;

public class OOoCheck extends check {
	public OOoCheck(IcheckInteractionProvider output, String testDescription) {
		super(output, testDescription);
	}

	public checkResult doTest() {
		String oooPath = ""; //$NON-NLS-1$
		try {
			oooPath = configs.getOOoPath();
		} catch (Exception e) {
			e.printStackTrace();
		}

		int result = checkUtils.checkOOo(oooPath);
		if (result == -1) {
			// not yet configured
			checkResult tr = new checkResult(false,
					appLayer.Messages.getString("OOoCheck.ooNotYetConfigured"));  //$NON-NLS-1$
			tr.setFatal();
			return tr;

		}
		if (result == -2) {
			checkResult tr = new checkResult(false,
					appLayer.Messages.getString("OOoCheck.pathNotExist"));  //$NON-NLS-1$
			tr.setFatal();
			return tr;
		}
		if (result == -3) {
			checkResult tr = new checkResult(
					false,
					appLayer.Messages.getString("OOoCheck.pathNoProgram"));  //$NON-NLS-1$
			tr.setFatal();
			return tr;

		}
		if (result == -4) {
			checkResult tr = new checkResult(
					false,
					appLayer.Messages.getString("OOoCheck.pathNoClasses"));  //$NON-NLS-1$
			tr.setFatal();
			return tr;

		}
		if (result == -5) {
			checkResult tr = new checkResult(false,
					appLayer.Messages.getString("OOoCheck.noOfficeBean")); //$NON-NLS-1$
			tr.setFatal();
			return tr;

		}
		if (result < -5) {
			checkResult tr = new checkResult(false,
					appLayer.Messages.getString("OOoCheck.pathError"));  //$NON-NLS-1$
			tr.setFatal();
			return tr;
		}
		return new standardCheck();
	}

}
