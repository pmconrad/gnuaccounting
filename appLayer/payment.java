package appLayer;

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.structures.Konto;

import dataLayer.HBCI;

public class payment {
	private HBCI hbci = null;
	private Shell shell;

	public payment(Shell sh) {
		hbci = HBCI.getInstance(sh);
		this.shell = sh;
	}

	public void perform(final IPaymentDataProvider ipdp) {
		if (ipdp.getCashFlow() == CashFlow.RECEIVING) {
			getMoneyFrom(ipdp);
		} else if (ipdp.getCashFlow() == CashFlow.SENDING) {
			// e.g. a credit note
			transferMoneyTo(ipdp.getBankCode(), ipdp.getBankAccount(),
					ipdp.getBankAccountHolder(), ipdp.getPaymentPurpose(),
					ipdp.getPaymentAmount());

		}
	}

	private void getMoneyFrom(final IPaymentDataProvider ipdp) {
		// all HBCI operations have to run in it's own thread
		new Thread() {
			public void run() {

				Konto own = new Konto(configs.getBankCode(),
						configs.getAccountCode());
				Konto other = new Konto(ipdp.getBankCode(),
						ipdp.getBankAccount());

				// e.g. an invoice
				HBCIJob job = hbci.newJob("Last"); //$NON-NLS-1$
				job.setParam("my", own); // Kontonummer für Saldenabfrage //$NON-NLS-1$
				job.setParam("other", other); //$NON-NLS-1$
				job.setParam("btg.value", ipdp.getPaymentAmount().toString()); //$NON-NLS-1$
				job.setParam("btg.curr", "EUR"); //$NON-NLS-1$ //$NON-NLS-2$
				job.setParam("name", ipdp.getBankAccountHolder()); //$NON-NLS-1$
				String usage_1 = ipdp.getPaymentPurpose();
				String usage_2 = ""; //$NON-NLS-1$
				if (usage_1.length() > 27) {
					usage_1 = ipdp.getPaymentPurpose().substring(0, 27);
					usage_2 = ipdp.getPaymentPurpose().substring(27);
				}

				job.setParam("usage", usage_1); //$NON-NLS-1$
				if (usage_2.length() > 0) {
					job.setParam("usage_2", usage_2); //$NON-NLS-1$

				}

				job.addToQueue();
				hbci.execute("" + ipdp.getPaymentAmount() + "" + ipdp.getBankAccount() + "" + ipdp.getBankCode()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				final HBCIJobResult result = job.getJobResult();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {

						if (result.isOK()) {
							MessageDialog.openInformation(
									shell,
									Messages.getString("payment.debitSuccessfulHeading"), //$NON-NLS-1$
									Messages.getString("payment.debitSuccessfulText")); //$NON-NLS-1$
						} else {
							MessageDialog.openWarning(
									shell,
									Messages.getString("payment.debitFailedHeading"), //$NON-NLS-1$
									Messages.getString("payment.debitFailedText")); //$NON-NLS-1$
						}
					}
				});
			}
		}.start();
	}

	private void transferMoneyTo(final String bankcode,
			final String bankaccount, final String accountholder,
			final String usage, final BigDecimal value) {
		// all HBCI operations have to run in it's own thread
		new Thread() {
			public void run() {

				Konto own = new Konto(configs.getBankCode(),
						configs.getAccountCode());
				Konto other = new Konto(bankcode, bankaccount);

				try {
					HBCIJob job = hbci.newJob("Ueb"); //$NON-NLS-1$
					job.setParam("src", own); // Kontonummer für Saldenabfrage //$NON-NLS-1$
					job.setParam("dst", other); //$NON-NLS-1$
					job.setParam("btg.value", value.toString()); //$NON-NLS-1$
					job.setParam("btg.curr", "EUR"); //$NON-NLS-1$ //$NON-NLS-2$
					job.setParam("name", accountholder); //$NON-NLS-1$
					String usage_1 = usage;
					String usage_2 = ""; //$NON-NLS-1$
					if (usage_1.length() > 27) {// usage (payment purpose) field
												// is limited to 27 characters,
												// but there is another one
						usage_1 = usage.substring(0, 27);
						usage_2 = usage.substring(27);
					}

					job.setParam("usage", usage_1); //$NON-NLS-1$
					if (usage_2.length() > 0) {
						job.setParam("usage_2", usage_2); //$NON-NLS-1$

					}
					job.addToQueue();
					hbci.execute("Executing bank transfer: " + value + "" + bankcode + "/" + bankaccount); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					final HBCIJobResult result = job.getJobResult();
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {

							if (result.isOK()) {
								MessageDialog.openInformation(
										shell,
										Messages.getString("payment.transferSuccessfulHeading"), //$NON-NLS-1$
										Messages.getString("payment.transferSuccessfulText")); //$NON-NLS-1$
							} else {
								MessageDialog.openWarning(
										shell,
										Messages.getString("payment.transferFailedHeading"), //$NON-NLS-1$
										Messages.getString("payment.transferFailedText")); //$NON-NLS-1$
							}
						}
					});
				} catch (Exception ex) {
					utils.logAndShowException(shell, ex);
				}
			}
		}.start();

	}

}
