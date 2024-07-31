package GUILayer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;

import ag.ion.bion.officelayer.spreadsheet.ISpreadsheetDocument;
import appLayer.client;
import appLayer.entry;
import appLayer.utils;

public class reportWizard extends Wizard {
	private boolean canFinish = false;

	@Override
	public boolean canFinish() {
		return canFinish;
	}

	public void enableFinish() {
		canFinish = true;
	}

	protected ISpreadsheetDocument spreadDocument = null;

	public static enum format {
		formatODF, formatCSVDATEV, formatCSVLEXWARE, formatASCII
	}

	protected format activeFormat;

	private reportWizardSelect page1;
	private reportWizardShow page2;
	private reportWizardFile page3;

	public reportWizard(format selectedFormat) {
		super();
		activeFormat = selectedFormat;
	}

	@Override
	public void addPages() {
		page1 = new reportWizardSelect(this);
		addPage(page1);
		if (activeFormat == format.formatODF) {
			page2 = new reportWizardShow();
			addPage(page2);

		} else {
			page3 = new reportWizardFile();
			addPage(page3);
		}

	}

	public format getFormat() {
		return activeFormat;
	}

	public ISpreadsheetDocument getSpreadDocument() {
		return spreadDocument;
	}

	public boolean isOpenable() {
		if (client.getEntries().isEmpty()) {
			MessageDialog
					.openError(
							getShell(),
							Messages.getString("reportWizard.missingEntries"), Messages.getString("reportWizard.addEntriesBefore")); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		return true;
	}

	public void setSpreadDocument(ISpreadsheetDocument document) {
		this.spreadDocument = document;
	}

	@Override
	public boolean performFinish() {

		if (activeFormat == format.formatASCII) {
			Date fromDte = page1.getStart().getTime();
			Date toDte = page1.getEnd().getTime();
			client.getEntries().setPeriod(fromDte, toDte, true);
			client.getEntries().removeAutoVATs(); //Addison and DATEV support auto accounts...

			SimpleDateFormat germanFormatForASCII = new SimpleDateFormat(
					"dd.MM.yyyy"); //$NON-NLS-1$

			Vector<entry> completeJournal = new Vector<entry>(client
					.getEntries().getJournal(true));
			Collections.sort(completeJournal);
			FileWriter fstream;

			try {
				if (page3.getFilename() != null) {

					fstream = new FileWriter(page3.getFilename());
					BufferedWriter out = new BufferedWriter(fstream);
					// Format: Issue Date;Due Date;Document
					// number;Text;Amount;Account;Contra account
					out.write("BelegDatum;F\u00C3\u00A4lligkeitsdatum;Belegnummer;Buchungstext;Betrag;Konto;Gegenkonto\r\n"); //$NON-NLS-1$
					int rowIndex = 0;

					for (entry currentEntry : completeJournal) {
						try {
							out.write(germanFormatForASCII.format(currentEntry
									.getDate()) + ";"); //$NON-NLS-1$
							out.write(germanFormatForASCII.format(currentEntry
									.getDate()) + ";"); //$NON-NLS-1$
							out.write(currentEntry.getReference() + ";"); //$NON-NLS-1$
							out.write(currentEntry.getDescription() + ";"); //$NON-NLS-1$

							BigDecimal val = currentEntry.getValue();

							// NumberFormat df =
							// DecimalFormat.getNumberInstance();

							// DecimalFormatSymbols otherSymbols = new
							// DecimalFormatSymbols(Locale.getDefault());
							// otherSymbols.setDecimalSeparator(',');
							// otherSymbols.setGroupingSeparator(' ');
							// DecimalFormat df = new DecimalFormat("#,##",
							// otherSymbols);

							// DecimalFormat df = new DecimalFormat("#,##");
							String formattedValue = val.toString().replace('.',
									',');

							out.write(formattedValue + ";"); //$NON-NLS-1$
							String debitCode = currentEntry.getDebitAccount()
									.getCode();
							if (debitCode.equals("1400")) { //$NON-NLS-1$
								debitCode = "1401"; //$NON-NLS-1$
							}
							if (debitCode.equals("1600")) { //$NON-NLS-1$
								debitCode = "1601"; //$NON-NLS-1$
							}

							out.write(debitCode + ";"); //$NON-NLS-1$

							String creditCode = currentEntry.getCreditAccount()
									.getCode();
							if (creditCode.equals("1400")) { //$NON-NLS-1$
								creditCode = "1401"; //$NON-NLS-1$
							}
							if (creditCode.equals("1600")) { //$NON-NLS-1$
								creditCode = "1601"; //$NON-NLS-1$
							}

							out.write(creditCode + "\r\n"); //$NON-NLS-1$

						} catch (Exception ex) {
							ex.printStackTrace();
						}
						rowIndex++;
					}

					// Close the output stream
					out.close();
				}
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}

			MessageDialog
					.openInformation(
							getShell(),
							Messages.getString("reportWizardSelect.finishedCaption"), Messages.getString("reportWizardSelect.finishedText")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (activeFormat == format.formatCSVDATEV) {
			Date fromDte = page1.getStart().getTime();
			Date toDte = page1.getEnd().getTime();
			client.getEntries().setPeriod(fromDte, toDte, true);
			client.getEntries().removeAutoVATs(); // Addison and DATEV support auto VAT accounts

			Vector<entry> completeJournal = new Vector<entry>(client
					.getEntries().getJournal(true));

			Collections.sort(completeJournal);
			FileWriter fstream;

			try {
				if (page3.getFilename() != null) {

					fstream = new FileWriter(page3.getFilename());
					BufferedWriter out = new BufferedWriter(fstream);
					
					out.write("\"Sollkonto\";\"Habenkonto\";\"Belegdatum\";\"Belegnummer\";\"Buchungstext\";\"Buchungsbetrag\";\"MWST\";\"Waehrung\";\r\n"); //$NON-NLS-1$
					
					// out.write(Messages.getString("reportWizardShow.number")
					// + ";");
					out.write(Messages.getString("reportWizardShow.date") //$NON-NLS-1$
							+ ";"); //$NON-NLS-1$
					out.write(Messages.getString("reportWizardShow.amount") //$NON-NLS-1$
							+ ";"); //$NON-NLS-1$
					out.write("Soll/Habenkennzeichen" + ";"); //$NON-NLS-1$ //$NON-NLS-2$
					out.write(Messages
							.getString("reportWizardShow.creditaccount") //$NON-NLS-1$
							+ ";"); //$NON-NLS-1$
					out.write(Messages
							.getString("reportWizardShow.debitaccount") //$NON-NLS-1$
							+ ";"); //$NON-NLS-1$
					out.write(Messages.getString("reportWizardShow.reference") + ";"); //$NON-NLS-1$ //$NON-NLS-2$
					out.write(Messages
							.getString("reportWizardShow.description") //$NON-NLS-1$
							+ "\r\n"); //$NON-NLS-1$

					int rowIndex = 0;
					SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$


					for (entry currentEntry : completeJournal) {
						try {
							// out.write(currentEntry.getID() + ";");
							// out.write(currentEntry.getNumber() + ";");
							out.write(sdf.format(currentEntry.getDate()) + ";"); //$NON-NLS-1$
							out.write(currentEntry.getValue() + ";"); //$NON-NLS-1$
							out.write("H" + ";"); //$NON-NLS-1$ //$NON-NLS-2$
							out.write(currentEntry.getCreditAccount().getCode()
									+ ";"); //$NON-NLS-1$
							out.write(currentEntry.getDebitAccount().getCode()
									+ ";"); //$NON-NLS-1$
							out.write(currentEntry.getReference() + ";"); //$NON-NLS-1$
							out.write(currentEntry.getDescription() + "\r\n"); //$NON-NLS-1$

						} catch (Exception ex) {
							ex.printStackTrace();
						}
						rowIndex++;
					}

					// Close the output stream
					out.close();
				}
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			MessageDialog
					.openInformation(
							getShell(),
							Messages.getString("reportWizardSelect.finishedCaption"), Messages.getString("reportWizardSelect.finishedText")); //$NON-NLS-1$ //$NON-NLS-2$

		} else 	if (activeFormat == format.formatCSVLEXWARE) {
			Date fromDte = page1.getStart().getTime();
			Date toDte = page1.getEnd().getTime();
			client.getEntries().setPeriod(fromDte, toDte, true);

			Vector<entry> completeJournal = new Vector<entry>(client
					.getEntries().getJournal(true));

			Collections.sort(completeJournal);
			FileWriter fstream;

			try {
				if (page3.getFilename() != null) {

					fstream = new FileWriter(page3.getFilename());
					BufferedWriter out = new BufferedWriter(fstream);
					
					out.write("\"Sollkonto\";\"Habenkonto\";\"Belegdatum\";\"Belegnummer\";\"Buchungstext\";\"Buchungsbetrag\";\"MWST\";\"Waehrung\";\r\n"); //$NON-NLS-1$
					
					int rowIndex = 0;
					SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$


					for (entry currentEntry : completeJournal) {
						try {
							
							
							String descriptionComment=currentEntry.getDescription();
							if (currentEntry.getComment().length()>0) {
								descriptionComment=descriptionComment+" "+currentEntry.getComment(); //$NON-NLS-1$
							}
							
							
							/**Now we emulate "Personenkonten" (personal accounts): 
							 * Gnuaccounting does not need them since the contact is attribute of the entry, not of the account
							 * but lexware apparently expects them - and minimum 5 digits instead of the usual 4-digit SKR code
							 * (The lexware CSV-Export targets SKR03 and 04 users only)  
							 * */
							String debitCode=currentEntry.getDebitAccount().getCode();
							if ((currentEntry.getContact()!=null)&&((currentEntry.getDebitAccount().getSubAccountTypesCode()==2)||(currentEntry.getDebitAccount().getSubAccountTypesCode()==3))) {
								// "subtype" 2 or 3 means refers to clients, or suppliers 
								debitCode=debitCode.concat(Integer.toString(currentEntry.getContact().getID()));
							}

							String creditCode=currentEntry.getCreditAccount().getCode();
							if ((currentEntry.getContact()!=null)&&((currentEntry.getCreditAccount().getSubAccountTypesCode()==2)||(currentEntry.getCreditAccount().getSubAccountTypesCode()==3))) {
								creditCode=creditCode.concat(Integer.toString(currentEntry.getContact().getID()));
							}
							

							out.write(String.format("\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"0\";\"EUR\"\r\n", debitCode, creditCode, sdf.format(currentEntry //$NON-NLS-1$
									.getDate()), currentEntry.getReference(), descriptionComment, utils.currencyFormat(currentEntry.getValue(), ',')));

						} catch (Exception ex) {
							ex.printStackTrace();
						}
						rowIndex++;
					}

					// Close the output stream
					out.close();
				}
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			MessageDialog
					.openInformation(
							getShell(),
							Messages.getString("reportWizardSelect.finishedCaption"), Messages.getString("reportWizardSelect.finishedText")); //$NON-NLS-1$ //$NON-NLS-2$

		}

		
		
		if (spreadDocument != null) {
			spreadDocument.close();
		}
		return true;
	}

}
