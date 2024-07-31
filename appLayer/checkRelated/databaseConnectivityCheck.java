package appLayer.checkRelated;

import GUILayer.Messages;

public class databaseConnectivityCheck extends check {
	private IdatabaseCheckConductor test = null;

	public databaseConnectivityCheck(IcheckInteractionProvider output,
			String testDescription, IdatabaseCheckConductor test) {
		super(output, testDescription);
		this.test = test;
	}

	public checkResult doTest() {
		/*
		 * this is both the test if the DB works and at the same time the
		 * initial and only database connection (except the hibernate connection
		 * below)
		 */
		String errorCode = test.conduct();
		if (errorCode != null) {
			checkResult tr = new checkResult(false,
					Messages.getString("configWindow.noDBConnectivity")); //$NON-NLS-1$
			tr.setFatal();
			return tr;
		}
		return new standardCheck();
	}

}
