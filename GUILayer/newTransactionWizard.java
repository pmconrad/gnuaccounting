package GUILayer;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.text.ITextDocument;
import appLayer.client;
import appLayer.configs;
import appLayer.document;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.uno.UnoRuntime;

import dataLayer.mailThread;

public class newTransactionWizard extends Wizard {

	private newTransactionSelectTransactionDetails page1;
	private newTransactionSelectItemDetails page2;
	private newTransactionWizardPreview page3;
	private newTransactionWizardPrintAndSave page4;

	private IFrame officeFrame = null;
	private ITextDocument textDocument = null;
	private static Shell activeShell = null;
	private boolean canFinish = false;
	private boolean showReferencedTypes = false;

	public newTransactionSelectItemDetails getWizardSelect() {
		return page2;
	}

	public void setOfficeFrame(IFrame officeFrame) {
		this.officeFrame = officeFrame;
	}

	public IFrame getOfficeFrame() {
		return this.officeFrame;
	}

	public void setDocument(ITextDocument document) {
		this.textDocument = document;

	}

	public ITextDocument getDocument() {
		return this.textDocument;
	}

	public newTransactionWizard() {
		super();
		setNeedsProgressMonitor(true);

		/**
		 * @todo shell.addDisposeListener(new DisposeListener() {
		 * 
		 *       public void widgetDisposed(DisposeEvent e) { try {
		 *       OfficeWriterBean.close(); } catch (RuntimeException exception)
		 *       { exception.printStackTrace(); } }
		 * 
		 *       });
		 **/
		page1 = new newTransactionSelectTransactionDetails(this);
		page2 = new newTransactionSelectItemDetails(this);
		page3 = new newTransactionWizardPreview(this);
		page4 = new newTransactionWizardPrintAndSave(this);

	}

	public static void setActiveShell(Shell sh) {
		activeShell = sh;
	}

	public static Shell getActiveShell() {
		return activeShell;
	}

	public void addPages() {
		setActiveShell(getShell());
		addPage(page1);

		addPage(page2);

		addPage(page3);

		addPage(page4);

	}

	@Override
	public boolean performFinish() {
		if (page4.getNumPrints() > 0) {
			// in the simplest case (1 copy on default printer) a
			// this.getDocument().print(); would suffice but
			// we have to take non-default printers and the number of copies
			// into account
			HashMap<String, Object> printOptions = new HashMap<String, Object>();
			printOptions.put("Wait", new Boolean(true)); // wait until print is //$NON-NLS-1$
															// finished, i.e.
															// execute
															// synchronously
			if (configs.getPrinterName() != null) {
				printOptions.put("Name", configs.getPrinterName()); //$NON-NLS-1$
			}
			if (page4.getNumPrints() > 1) {
				printOptions.put("CopyCount", page4.getNumPrints()); //$NON-NLS-1$
			}

			//
			// Querying for the interface XPrintable on the loaded document
			com.sun.star.view.XPrintable xPrintable = (com.sun.star.view.XPrintable) UnoRuntime
					.queryInterface(com.sun.star.view.XPrintable.class, this
							.getDocument().getXComponent());

			PropertyValue[] propertyValue = new com.sun.star.beans.PropertyValue[printOptions
					.size()];

			int propertyIndex = 0;
			for (String key : printOptions.keySet()) {
				propertyValue[propertyIndex] = new com.sun.star.beans.PropertyValue();
				propertyValue[propertyIndex].Name = key;
				propertyValue[propertyIndex].Value = printOptions.get(key);

				propertyIndex++;
			}

			// Setting the name of the printer
			try {
				xPrintable.setPrinter(propertyValue);
				// print!
				xPrintable.print(propertyValue);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (page4.isEmailChecked()) {
			boolean clearedToSend = true;
			if (configs.getSMTPServer().length() == 0) {
				MessageDialog
						.openError(
								getShell(),
								Messages.getString("newTransactionWizard.WarningHeadingMissingConfig"), Messages.getString("newTransactionWizard.WarningMissingSMTPServer")); //$NON-NLS-1$ //$NON-NLS-2$
				clearedToSend = true;
			}
			if (configs.getSenderEmail().length() == 0) {
				MessageDialog
						.openError(
								getShell(),
								Messages.getString("newTransactionWizard.WarningHeadingMissingConfig"), Messages.getString("newTransactionWizard.WarningMissingFrom")); //$NON-NLS-1$ //$NON-NLS-2$
				clearedToSend = false;
			}
			if ((client.getTransactions().getCurrentTransaction().getContact()
					.getEmail() == null)
					|| (client.getTransactions().getCurrentTransaction()
							.getContact().getEmail().length() == 0)) {
				MessageDialog
						.openError(
								getShell(),
								Messages.getString("newTransactionWizard.WarningHeadingMissingCustomerData"), Messages.getString("newTransactionWizard.WarningMissingEmailForCustomer")); //$NON-NLS-1$ //$NON-NLS-2$
				clearedToSend = false;
			}

			if (clearedToSend) {

				String SMTPUsername = configs.getSMTPUsername();
				String SMTPPassword = configs.getSMTPPassword();
				if (((SMTPPassword.length() == 0) || (SMTPUsername.length() == 0))
						&& (configs.shallUseSMTPAuth())) {
					passwordDialog pwd = new passwordDialog(getShell(),
							configs.getSMTPUsername());
					pwd.setBlockOnOpen(true);
					pwd.open();
					SMTPPassword = pwd.getPassword();
					SMTPUsername = pwd.getUsername();

				}

				mailThread mt = new mailThread(
						SMTPUsername,
						SMTPPassword,
						client.getTransactions().getCurrentTransaction()
								.getContact().getEmail(),
						client.getTransactions().getCurrentTransaction()
								.getTransactionName()
								+ " " + client.getTransactions().getCurrentTransaction().getNumber(), Messages.getString("newTransactionWizard.attachedInAdvance") + client.getTransactions().getCurrentTransaction().getTransactionName() + " " + client.getTransactions().getCurrentTransaction().getNumber() + Messages.getString("newTransactionWizard.dot-final-transactiontype-will-be-send") + client.getTransactions().getCurrentTransaction().getTransactionName() + Messages.getString("newTransactionWizard.willBeSendByMailOrFax"), client.getTransactions().getCurrentTransaction().getFilenamePDF(), client.getTransactions().getCurrentTransaction().getFilenamePDFGPG(), client.getTransactions().getCurrentTransaction().getFilenameOT()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				try {
					mt.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mt.run();
				try {
					new ProgressMonitorDialog(getShell()).run(true, true, mt);
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		// Send reminder mail
		if (configs.shallSendICAL()) {
			// Builder-generated settings dialog has been extended
			if (configs.getSMTPServer().length() == 0) {
				MessageDialog
						.openError(
								getShell(),
								Messages.getString("newTransactionWizard.WarningHeadingMissingConfig"), Messages.getString("newTransactionWizard.WarningMissingSMTPServer")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			else if (configs.getSenderEmail().length() == 0) {
				MessageDialog
						.openError(
								getShell(),
								Messages.getString("newTransactionWizard.WarningHeadingMissingConfig"), Messages.getString("newTransactionWizard.WarningMissingFrom")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Mail settings have been entered correctly
			else {
				String SMTPUsername = configs.getSMTPUsername();
				String SMTPPassword = configs.getSMTPPassword();

				if ((SMTPPassword.length() == 0 || (SMTPUsername.length() == 0))
						&& configs.shallUseSMTPAuth()) {
					passwordDialog pwd = new passwordDialog(getShell(),
							configs.getSMTPUsername());
					pwd.setBlockOnOpen(true);
					pwd.open();

					SMTPPassword = pwd.getPassword();
					SMTPUsername = pwd.getUsername();
				}

				// Generate attachment in memory
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd"); //$NON-NLS-1$
				dateFormat1.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$

				SimpleDateFormat dateFormat2 = new SimpleDateFormat("HHmmss"); //$NON-NLS-1$
				dateFormat2.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$

				Date dueDate = client.getTransactions().getCurrentTransaction()
						.getDueDate();

				// Use next day as end date for a correct all-day event for the
				// *first* day
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dueDate);
				calendar.add(Calendar.DATE, 1);
				Date nextDate = calendar.getTime();
				String iCalData = String
						.format("BEGIN:VCALENDAR\n" + //$NON-NLS-1$
								"PRODID:-//GnuAccounting//EN\n" + //$NON-NLS-1$
								"VERSION:2.0\n" + //$NON-NLS-1$
								"METHOD:PUBLISH\n" + //$NON-NLS-1$
								"BEGIN:VEVENT\n" + //$NON-NLS-1$
								"DTSTART;VALUE=DATE:" //$NON-NLS-1$
								+ dateFormat1.format(dueDate)
								+ "\n" + //$NON-NLS-1$ //$NON-NLS-2$
								"DTEND;VALUE=DATE:" //$NON-NLS-1$
								+ dateFormat1.format(nextDate)
								+ "\n" + //$NON-NLS-1$ //$NON-NLS-2$
								"DTSTAMP:" //$NON-NLS-1$
								+ dateFormat1.format(new Date())
								+ "T" + dateFormat2.format(new Date()) + "Z\n" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								"ORGANIZER;CN=" //$NON-NLS-1$
								+ configs.getSenderEmail()
								+ ":mailto:" + configs.getSenderEmail() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								"UID:" //$NON-NLS-1$
								+ client.getTransactions()
										.getCurrentTransaction().getID()
								+ "@gnuaccounting.org\n" + //$NON-NLS-1$ //$NON-NLS-2$
								"DESCRIPTION:" //$NON-NLS-1$
								+ Messages
										.getString("newTransactionWizard.invoiceReminderCalendarDescription") + //$NON-NLS-1$ //$NON-NLS-2$
								"LOCATION:-\n" //$NON-NLS-1$
								+ //$NON-NLS-1$
								"SUMMARY:" //$NON-NLS-1$
								+ Messages
										.getString("newTransactionWizard.invoiceReminderCalendarSummary") + //$NON-NLS-1$ //$NON-NLS-2$
								"TRANSP:TRANSPARENT\n" //$NON-NLS-1$
								+ //$NON-NLS-1$
								"BEGIN:VALARM\n" //$NON-NLS-1$
								+ //$NON-NLS-1$
								"ACTION:DISPLAY\n" //$NON-NLS-1$
								+ //$NON-NLS-1$
								"DESCRIPTION:" //$NON-NLS-1$
								+ //$NON-NLS-1$
								Messages.getString("newTransactionWizard.invoiceReminderCalendarAlarmDescription") + //$NON-NLS-1$
								"TRIGGER:-PT5M\n" + //$NON-NLS-1$
								"END:VALARM\n" + //$NON-NLS-1$
								"END:VEVENT\n" + //$NON-NLS-1$
								"END:VCALENDAR\n", client //$NON-NLS-1$
								//$NON-NLS-1$
								//$NON-NLS-1$
								.getTransactions().getCurrentTransaction()
								.getNumber(), client.getTransactions()
								.getCurrentTransaction().getTransactionName(),
								client.getTransactions()
										.getCurrentTransaction().getNumber(),
								client.getTransactions()
										.getCurrentTransaction()
										.getTransactionName(), client
										.getTransactions()
										.getCurrentTransaction().getNumber(),
								client.getTransactions()
										.getCurrentTransaction()
										.getTransactionName()); //$NON-NLS-1$

				// Send actual mail
				mailThread mt = new mailThread(
						SMTPUsername,
						SMTPPassword,
						configs.getSenderEmail(), // recipient is the client,
						// not the customer
						String.format(
								Messages.getString("newTransactionWizard.invoiceReminderCalendarMailSubject"), client.getTransactions().getCurrentTransaction().getNumber(), client.getTransactions().getCurrentTransaction().getTransactionName()), //$NON-NLS-1$
						String.format(
								Messages.getString("newTransactionWizard.invoiceReminderCalendarMailBody"), client.getTransactions().getCurrentTransaction().getNumber(), client.getTransactions().getCurrentTransaction().getTransactionName()), //$NON-NLS-1$
						null, null, null);

				mt.attachMemoryFile("reminder.ics", "text/calendar", iCalData); //$NON-NLS-1$ //$NON-NLS-2$

				try {
					mt.join(); // Really necessary?
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				mt.run();

				try {
					new ProgressMonitorDialog(getShell()).run(true, true, mt);
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		document d = client.getDocuments().getNewDocument(
				client.getTransactions().getCurrentTransaction()
						.getFilenamePDF());
		d.setNumber(client.getTransactions().getCurrentTransaction()
				.getNumber());
		d.setOriginalFilename(client.getTransactions().getCurrentTransaction()
				.getFilenameODT());
		d.setValue(client.getTransactions().getCurrentTransaction()
				.getTotalGross());

		client.getTransactions().getCurrentTransaction().setDocument(d);
		client.getTransactions().finishCurrentTransaction(
				page4.isBalancedChecked());
		d.save();

		if (textDocument != null) {
			textDocument.close();// hang
		}
		return true;
	}

	@Override
	public boolean canFinish() {
		return canFinish;
	}

	/**
	 * whether to allow selection of transaction types which may only refer to
	 * other transactions, e.g. reminder or cancellations
	 */
	public void setShowReferencedTypes(boolean showReferencedTypes) {
		this.showReferencedTypes = showReferencedTypes;
	}

	/**
	 * whether to allow selection of transaction types which may only refer to
	 * other transactions, e.g. reminder or cancellations
	 */
	protected boolean shallShowReferencedTypes() {
		return showReferencedTypes;
	}

	public void setCanFinish(boolean canFinish) {
		this.canFinish = canFinish;
	}

}
