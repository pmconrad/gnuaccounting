package appLayer.checkRelated;

import java.io.File;
import java.io.IOException;

import appLayer.client;
import appLayer.transactionRelated.transactionType;
import dataLayer.fileUtils;

public class templateCheck extends check {
	public templateCheck(IcheckInteractionProvider output,
			String testDescription) {
		super(output, testDescription);
	}

	public checkResult doTest() {
		transactionType[] types = client.getTransactions().getAllTypes();
		for (transactionType currentTransactionType : types) {

			if (!new File(client.getDataPath()
					+ currentTransactionType.getTypePrefix() + "template1.odt").exists()) { //$NON-NLS-1$
				try {
					fileUtils
							.copyURLToFile(
									getClass().getResource(
											"/init/defaulttemplate-1.odt"), new File(client.getDataPath() + currentTransactionType.getTypePrefix() //$NON-NLS-1$
													+ "template1.odt")); //$NON-NLS-1$
					/*
					 * InputStream in =
					 * getClass().getResourceAsStream("/defaulttemplate-1.odt");
					 * 
					 * OutputStream out = new FileOutputStream(new
					 * File(client.getDataPath() +
					 * currentTransactionType.getTypePrefix() +
					 * "template1.odt"));
					 * 
					 * byte[] buf = new byte[1024]; int len; while ((len =
					 * in.read(buf)) > 0){ out.write(buf, 0, len); } in.close();
					 * out.close();
					 */

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		return new standardCheck();
	}

}
