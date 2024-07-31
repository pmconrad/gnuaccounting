package appLayer;

/**
 * this holds all static vars for the application, e.g. application name,
 * version number and version history
 * */
public class application {
	static private final String versionString = "0.8.9"; //$NON-NLS-1$
	static private final double versionDouble = 0.89;

	/*
	 * versionString and versionDouble are the application version and should be
	 * x.x.x, e.g. 0.3.6. versionDouble will then be 0.36. if versionString
	 * contains alpha, beta or RC versions, versionDouble shall be the last
	 * versionDouble appended by a 9, e.g. 0.3.7alpha1 would be versionDouble
	 * 0.369 the subsequent test version, e.g. RC1 or alpha 2 would be 0.3699,
	 * this way 0.3.8 (0.38) is numerically always >0.3.7rc1>0.3.7alpha1>0.3.7
	 */
	public static String versionHistory = 
			Messages.getString("application.089versionStr")+ //$NON-NLS-1$
			Messages.getString("application.088versionStr")+ //$NON-NLS-1$
			Messages.getString("application.087versionStr")+ //$NON-NLS-1$
			Messages.getString("application.086versionStr")+ //$NON-NLS-1$
			Messages.getString("application.085versionStr")+ //$NON-NLS-1$
			Messages.getString("application.084versionStr")+ //$NON-NLS-1$
			Messages.getString("application.083versionStr")+ //$NON-NLS-1$
	 Messages.getString("application.082versionStr")+ //$NON-NLS-1$
			Messages
			.getString("application.081versionStr") + //$NON-NLS-1$
			Messages.getString("application.080versionStr") + //$NON-NLS-1$
			Messages.getString("application.079versionStr") + //$NON-NLS-1$
			Messages.getString("application.078versionStr") + //$NON-NLS-1$
			Messages.getString("application.077versionStr") + //$NON-NLS-1$
			Messages.getString("application.076versionStr") + //$NON-NLS-1$
			Messages.getString("application.075versionStr") + //$NON-NLS-1$
			Messages.getString("application.074versionStr") + //$NON-NLS-1$
			Messages.getString("application.073versionStr") + //$NON-NLS-1$
			Messages.getString("application.072versionStr") + //$NON-NLS-1$
			Messages.getString("application.071versionStr") + //$NON-NLS-1$
			Messages.getString("application.070versionStr") + //$NON-NLS-1$
			Messages.getString("application.068versionStr") + //$NON-NLS-1$
			Messages.getString("application.067versionStr") + //$NON-NLS-1$
			Messages.getString("application.066versionStr") + //$NON-NLS-1$
			Messages.getString("application.065versionStr") + //$NON-NLS-1$
			Messages.getString("application.064versionStr") + //$NON-NLS-1$
			Messages.getString("application.063versionStr") + //$NON-NLS-1$
			Messages.getString("application.062versionStr") + //$NON-NLS-1$
			Messages.getString("application.061versionStr") + //$NON-NLS-1$
			Messages.getString("application.060versionStr") + //$NON-NLS-1$
			Messages.getString("application.057versionStr") + //$NON-NLS-1$
			Messages.getString("application.056versionStr") + //$NON-NLS-1$
			Messages.getString("application.055versionStr") + //$NON-NLS-1$
			Messages.getString("application.054versionStr") + //$NON-NLS-1$
			Messages.getString("application.053versionStr") + //$NON-NLS-1$
			Messages.getString("application.052versionStr") + //$NON-NLS-1$
			Messages.getString("application.051versionStr") + //$NON-NLS-1$
			Messages.getString("application.050versionStr") + //$NON-NLS-1$
			Messages.getString("application.044versionStr") + //$NON-NLS-1$
			Messages.getString("application.043versionStr") + //$NON-NLS-1$
			Messages.getString("application.042versionStr") + //$NON-NLS-1$
			Messages.getString("application.041versionStr") + //$NON-NLS-1$
			Messages.getString("application.040versionStr") + //$NON-NLS-1$
			Messages.getString("application.039versionStr") + //$NON-NLS-1$
			Messages.getString("application.038versionStr") + //$NON-NLS-1$
			Messages.getString("application.037versionStr") + //$NON-NLS-1$
			Messages.getString("application.036versionStr") + //$NON-NLS-1$
			Messages.getString("application.035versionStr") + //$NON-NLS-1$
			Messages.getString("application.034versionStr") + //$NON-NLS-1$
			Messages.getString("application.033versionStr") + //$NON-NLS-1$
			Messages.getString("application.032versionStr") + //$NON-NLS-1$
			Messages.getString("application.031versionStr") + //$NON-NLS-1$
			Messages.getString("application.030versionStr") + //$NON-NLS-1$
			Messages.getString("application.021versionStr") + //$NON-NLS-1$
			Messages.getString("application.020versionStr") + //$NON-NLS-1$
			Messages.getString("application.015versionStr") + //$NON-NLS-1$
			Messages.getString("application.014versionStr") + //$NON-NLS-1$
			Messages.getString("application.013versionStr") + //$NON-NLS-1$
			Messages.getString("application.012versionStr") + //$NON-NLS-1$
			Messages.getString("application.011versionStr") + //$NON-NLS-1$
			Messages.getString("application.010versionStr") + //$NON-NLS-1$

			Messages.getString("application.009versionStr"); //$NON-NLS-1$
	private static users allUsers;

	public static String getVersionString() {
		return versionString;
	}

	public static double getVersionDouble() {
		return versionDouble;
	}

	public static String getAppName() {
		return Messages.getString("application.gnuaccountingSpace") + getVersionString(); //$NON-NLS-1$
	}

	public static users getUsers() {
		if (allUsers == null) {
			allUsers = new users();
			allUsers.getUsersFromDB();
		}
		return allUsers;

	}

}
