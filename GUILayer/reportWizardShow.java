package GUILayer;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JPanel;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.desktop.GlobalCommands;
import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.spreadsheet.ISpreadsheetDocument;
import ag.ion.noa.frame.ILayoutManager;
import appLayer.account;
import appLayer.asset;
import appLayer.client;
import appLayer.configs;
import appLayer.entry;
import appLayer.entryNotInThisAccountException;
import appLayer.taxRelated.taxCalculator;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sheet.XSheetCellCursor;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.table.TableBorder;
import com.sun.star.table.XCell;
import com.sun.star.text.XText;
import com.sun.star.uno.UnoRuntime;

public class reportWizardShow extends WizardPage {

	private class tAccountLine {
		String value, reference;

		public tAccountLine(String value, String reference) {
			this.value = value;
			this.reference = reference;

		}

		public String getValue() {
			return value;
		}

		public String getReference() {
			return reference;
		}

	}

	private class tAccount {
		private account accountObject;
		private Vector<String> headerLines = new Vector<String>();
		private Vector<tAccountLine> creditLines = new Vector<tAccountLine>();
		private Vector<tAccountLine> debitLines = new Vector<tAccountLine>();
		private Vector<String> footerLines = new Vector<String>();

		private static final int TOP = 0;
		private static final int BOTTOM = 1;
		private static final int LEFT = 2;
		private static final int RIGHT = 3;

		public tAccount(account a, Vector<entry> accountJournal) {
			this.accountObject = a;
			addHeaderLine(accountObject.getAsString());
			for (Object currentEntryObject : accountJournal) {
				entry currentEntry = (entry) currentEntryObject;
				try {
					tAccountLine currentLine = new tAccountLine(currentEntry
							.getValue().toString(),
							"(" + currentEntry.getID() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					if (currentEntry.isCreditSide(accountObject)) {
						addCreditLine(currentLine);
					} else {
						addDebitLine(currentLine);
					}
				} catch (entryNotInThisAccountException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			BigDecimal closingBalance = a.getBalanceTotal();
			tAccountLine closingLine = new tAccountLine(closingBalance.abs()
					.toString(), Messages.getString("reportWizardShow.balance")); //$NON-NLS-1$

			boolean balanceInDebit = true;
			if (closingBalance.compareTo(new BigDecimal(0)) < 0) {
				balanceInDebit = !balanceInDebit;
			}
			if (!accountObject.balanceIncreasesInDebit()) {
				//balanceInDebit = !balanceInDebit;
			}
			if (!balanceInDebit) {
				/*
				 * for optical reasons we want a positive debit balance shown in
				 * the credit column, so that e.g. with a single entry we get
				 * ____________ 123|123 (balance)
				 * 
				 * not ____________ 123| (balance)123|
				 */
				addDebitLine(closingLine);
			} else {
				addCreditLine(closingLine);
			}

		}

		public void addHeaderLine(String s) {
			headerLines.add(s);
		}

		public void addCreditLine(tAccountLine tal) {
			creditLines.add(tal);
		}

		public void addDebitLine(tAccountLine tal) {
			debitLines.add(tal);
		}

		private int getMaxLines() {
			if (creditLines.size() > debitLines.size()) {
				return creditLines.size();
			} else {
				return debitLines.size();
			}
		}

		/**
		 * returns the new rowIndex
		 * 
		 * @throws IllegalArgumentException
		 */
		public int draw(XSheetCellCursor cellCursor, int rowIndex)
				throws IllegalArgumentException {
			XCell cell = null;
			if (rowIndex < 0) {
				throw new IllegalArgumentException(
						Messages.getString("reportWizardShow.rowIndexGreaterEqualZero")); //$NON-NLS-1$
			}
			// draw header lines
			for (String currentLine : headerLines) {
				try {
					cell = cellCursor.getCellByPosition(0, rowIndex);
					XText cellText = (XText) UnoRuntime.queryInterface(
							XText.class, cell);
					cellText.setString(currentLine);
					// cell.setValue or cellText.setString
					rowIndex++;

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

			// draw top border of t account (the horizontal line of the T)
			try {
				cell = cellCursor.getCellByPosition(0, rowIndex);
				drawBorder(cell, TOP);
				cell = cellCursor.getCellByPosition(1, rowIndex);
				drawBorder(cell, TOP);
				cell = cellCursor.getCellByPosition(2, rowIndex);
				drawBorder(cell, TOP);
				cell = cellCursor.getCellByPosition(3, rowIndex);
				drawBorder(cell, TOP);
			} catch (IndexOutOfBoundsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// draw content
			for (int i = 0; i < getMaxLines(); i++) {

				try {
					/* drfaw left hand side */
					tAccountLine currentLine = new tAccountLine("", ""); //$NON-NLS-1$ //$NON-NLS-2$
					if (i < debitLines.size()) {
						currentLine = debitLines.get(i);
					}
					/* draw left part of left hand side */
					cell = cellCursor.getCellByPosition(0, rowIndex);
					XText cellText = (XText) UnoRuntime.queryInterface(
							XText.class, cell);
					cellText.setString(currentLine.getReference());
					/* draw right part of left hand side */
					cell = cellCursor.getCellByPosition(1, rowIndex);
					cellText = (XText) UnoRuntime.queryInterface(XText.class,
							cell);
					cellText.setString(currentLine.getValue());

					/* draw border in the middle */
					drawBorder(cell, RIGHT);

					/* draw right hand side */
					currentLine = new tAccountLine("", ""); //$NON-NLS-1$ //$NON-NLS-2$
					if (i < creditLines.size()) {
						currentLine = creditLines.get(i);
					}
					/* draw left part of right hand side */
					cell = cellCursor.getCellByPosition(2, rowIndex);
					cellText = (XText) UnoRuntime.queryInterface(XText.class,
							cell);
					cellText.setString(currentLine.getValue());
					/* draw right part of right hand side */
					cell = cellCursor.getCellByPosition(3, rowIndex);
					cellText = (XText) UnoRuntime.queryInterface(XText.class,
							cell);
					cellText.setString(currentLine.getReference());
					rowIndex++;
				} catch (IndexOutOfBoundsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			// draw footer lines
			for (String currentLine : footerLines) {
				try {
					cell = cellCursor.getCellByPosition(0, rowIndex);
					XText cellText = (XText) UnoRuntime.queryInterface(
							XText.class, cell);
					cellText.setString(currentLine);
					// cell.setValue or cellText.setString
					rowIndex++;

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
			return rowIndex;
		}

		/**
		 * draws a border round a XCell based on
		 * http://www.mail-archive.com/dev@sc.openoffice.org/msg00908.html
		 * 
		 * @throws IllegalArgumentException
		 * */
		private void drawBorder(XCell cell, int side)
				throws IllegalArgumentException {
			int color = Integer.valueOf(0x000000);
			short inner = 1;
			short outer = 0;
			short distance = 0;

			XPropertySet propertySet = (XPropertySet) UnoRuntime
					.queryInterface(XPropertySet.class, cell);

			TableBorder t = new TableBorder();

			switch (side) {
			case TOP:
				t.TopLine.Color = color;
				t.TopLine.InnerLineWidth = inner;
				t.TopLine.OuterLineWidth = outer;
				t.TopLine.LineDistance = distance;
				t.IsTopLineValid = true;
				break;

			case BOTTOM:
				t.BottomLine.Color = color;
				t.BottomLine.InnerLineWidth = inner;
				t.BottomLine.OuterLineWidth = outer;
				t.BottomLine.LineDistance = distance;
				t.IsBottomLineValid = true;

			case LEFT:
				t.LeftLine.Color = color;
				t.LeftLine.InnerLineWidth = inner;
				t.LeftLine.OuterLineWidth = outer;
				t.LeftLine.LineDistance = distance;
				t.IsLeftLineValid = true;
				break;

			case RIGHT:
				t.RightLine.Color = color;
				t.RightLine.InnerLineWidth = inner;
				t.RightLine.OuterLineWidth = outer;
				t.RightLine.LineDistance = distance;
				t.IsRightLineValid = true;
				break;

			default:
				throw new IllegalArgumentException(
						Messages.getString("reportWizardShow.callIllegalSide") + side); //$NON-NLS-1$
			}
			try {
				propertySet.setPropertyValue("TableBorder", t); //$NON-NLS-1$
			} catch (UnknownPropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (com.sun.star.lang.IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrappedTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected Vector<Object[]> items = new Vector<Object[]>();
	protected XSpreadsheet spreadsheet1;
	protected XSpreadsheet spreadsheet2;
	protected XSpreadsheet spreadsheet3;
	protected XSpreadsheet spreadsheet4;
	protected XSpreadsheet spreadsheet5;
	protected XSpreadsheet spreadsheet6;
	protected XSpreadsheet spreadsheet7;
	protected XSpreadsheet spreadsheet8;
	private Vector<entry> completeJournal=null;

	/**
	 * Create the wizard
	 */
	public reportWizardShow() {
		super(Messages.getString("reportWizardShow.wizardPage")); //$NON-NLS-1$
		setTitle(Messages.getString("reportWizardShow.reportWizard")); //$NON-NLS-1$
		setDescription(Messages.getString("reportWizardShow.SaveGenerated")); //$NON-NLS-1$
	}

	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.EMBEDDED);
		//
		setControl(container);

		final Frame frame = SWT_AWT.new_Frame(container);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setVisible(true);
		frame.add(panel, BorderLayout.CENTER);
		try {

			IOfficeApplication officeApplication = configs
					.getOfficeApplication();

			IFrame officeFrame;

			// embedd OOo Frame only if it's possible
			if (configs.isOOoEmbedded())
				officeFrame = officeApplication.getDesktopService()
						.constructNewOfficeFrame(panel);
			else
				officeFrame = null;

			IDocument document = officeApplication.getDocumentService()
					.constructNewDocument(officeFrame, IDocument.CALC,
							DocumentDescriptor.DEFAULT);

			if (officeFrame == null) {
				officeFrame = document.getFrame();
			}

			ILayoutManager layoutManager = officeFrame.getLayoutManager();

			layoutManager.hideElement(ILayoutManager.URL_MENUBAR);
			layoutManager.showElement(ILayoutManager.URL_TOOLBAR_TEXTOBJECTBAR);

			// Now it is time to disable two commands in the frame
			officeFrame.disableDispatch(GlobalCommands.CLOSE_DOCUMENT);
			officeFrame.disableDispatch(GlobalCommands.QUIT_APPLICATION);

			((reportWizard) getWizard())
					.setSpreadDocument((ISpreadsheetDocument) document);
			XSpreadsheets spreadsheets = ((reportWizard) getWizard())
					.getSpreadDocument().getSpreadsheetDocument().getSheets();

			/*
			 * XSpreadsheet spreadsheet1 =
			 * (XSpreadsheet)UnoRuntime.queryInterface(
			 * XSpreadsheet.class,spreadsheets.getByName(sheetName));
			 */

			// get the first table
			spreadsheet1 = (XSpreadsheet) UnoRuntime.queryInterface(
					XSpreadsheet.class,
					spreadsheets.getByName(spreadsheets.getElementNames()[0]));

			/* now append tables. In OpenOffice.org there might be a then-empty
			 table 2 and table 3 but in LO 4 there is by default only one table 
			 in a new calc document so looking for a second one will throw an exception
			 */ 
			short newIndex = (short) (spreadsheets.getElementNames().length + 1);

			String sheet2name = Messages
					.getString("reportWizardShow.tAccountsSheetName"); //$NON-NLS-1$
			spreadsheets.insertNewByName(sheet2name, newIndex);
			spreadsheet2 = (XSpreadsheet) UnoRuntime.queryInterface(
					XSpreadsheet.class, spreadsheets.getByName(sheet2name));

			String sheet3name = Messages
					.getString("reportWizardShow.journalSheetName"); //$NON-NLS-1$
			spreadsheets.insertNewByName(sheet3name, ++newIndex);
			spreadsheet3 = (XSpreadsheet) UnoRuntime.queryInterface(
					XSpreadsheet.class, spreadsheets.getByName(sheet3name));

			String sheet4name = Messages
					.getString("reportWizardShow.AccountStatements"); //$NON-NLS-1$

			spreadsheets.insertNewByName(sheet4name, ++newIndex);
			spreadsheet4 = (XSpreadsheet) UnoRuntime.queryInterface(
					XSpreadsheet.class, spreadsheets.getByName(sheet4name));

			String sheet5name = Messages
					.getString("reportWizardShow.netIncomeDeterminationSheetName"); //$NON-NLS-1$

			spreadsheets.insertNewByName(sheet5name, ++newIndex);
			spreadsheet5 = (XSpreadsheet) UnoRuntime.queryInterface(
					XSpreadsheet.class, spreadsheets.getByName(sheet5name));

			String sheet6name = Messages
					.getString("reportWizardShow.incomeSheetName"); //$NON-NLS-1$

			spreadsheets.insertNewByName(sheet6name, ++newIndex);
			spreadsheet6 = (XSpreadsheet) UnoRuntime.queryInterface(
					XSpreadsheet.class, spreadsheets.getByName(sheet6name));

			String sheet7name = Messages
					.getString("reportWizardShow.expensesSheetName"); //$NON-NLS-1$

			spreadsheets.insertNewByName(sheet7name, ++newIndex);
			spreadsheet7 = (XSpreadsheet) UnoRuntime.queryInterface(
					XSpreadsheet.class, spreadsheets.getByName(sheet7name));

			String sheet8name = Messages
					.getString("reportWizardShow.assetSheetName"); //$NON-NLS-1$

			spreadsheets.insertNewByName(sheet8name, ++newIndex);
			spreadsheet8 = (XSpreadsheet) UnoRuntime.queryInterface(
					XSpreadsheet.class, spreadsheets.getByName(sheet8name));

			frame.validate();

			// Now it is time to disable two commands in the frame
			officeFrame.disableDispatch(GlobalCommands.CLOSE_DOCUMENT);
			officeFrame.disableDispatch(GlobalCommands.QUIT_APPLICATION);
			officeFrame.updateDispatches();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
		// accounts on sheet 0

	}

	private void reportJournal() {
		XSheetCellCursor cellCursor;
		int rowIndex;
		// create the complete journal
		cellCursor = spreadsheet3.createCursor();
		rowIndex = 0;

		XCell cell = null;
		try {
			cell = cellCursor.getCellByPosition(0, rowIndex);
			XText cellText = (XText) UnoRuntime.queryInterface(XText.class,
					cell);
			cellText.setString(Messages
					.getString("reportWizardShow.cellHeadingID")); //$NON-NLS-1$
			// out.write(Messages.getString("reportWizardShow.cellHeadingID") +
			// ";");
			cell = cellCursor.getCellByPosition(1, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.number")); //$NON-NLS-1$
			// out.write(Messages.getString("reportWizardShow.number") + ";");
			cell = cellCursor.getCellByPosition(2, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.date")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(3, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.amount")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(4, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.debitaccount")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(5, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.creditaccount")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(6, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.reference")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(7, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.description")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(8, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.comment")); //$NON-NLS-1$

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		rowIndex++;

		SimpleDateFormat sdf = new SimpleDateFormat(
				Messages.getString("reportWizardShow.journalDateFormat")); //$NON-NLS-1$

		for (entry currentEntry : completeJournal) {
			cell = null;
			try {
				cell = cellCursor.getCellByPosition(0, rowIndex);
				cell.setValue(currentEntry.getID());
				// out.write(currentEntry.getID() + ";");
				cell = cellCursor.getCellByPosition(1, rowIndex);
				cell.setValue(currentEntry.getNumber());
				// out.write(currentEntry.getNumber() + ";");
				cell = cellCursor.getCellByPosition(2, rowIndex);
				XText cellText = (XText) UnoRuntime.queryInterface(XText.class,
						cell);
				cellText.setString(sdf.format(currentEntry.getDate()));
				cell = cellCursor.getCellByPosition(3, rowIndex);
				cell.setValue(currentEntry.getValue().doubleValue());
				cell = cellCursor.getCellByPosition(4, rowIndex);
				cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
				cellText.setString(currentEntry.getDebitAccount().getAsString());
				cell = cellCursor.getCellByPosition(5, rowIndex);
				cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
				cellText.setString(currentEntry.getCreditAccount()
						.getAsString());
				cell = cellCursor.getCellByPosition(6, rowIndex);
				cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
				cellText.setString(currentEntry.getReference());
				cell = cellCursor.getCellByPosition(7, rowIndex);
				cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
				cellText.setString(currentEntry.getDescription());
				cell = cellCursor.getCellByPosition(8, rowIndex);
				cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
				cellText.setString(currentEntry.getComment());

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			rowIndex++;
		}

		rowIndex = 0;

	}

	private void reportLists() {
		XSheetCellCursor cellCursor;
		int rowIndex;
		cellCursor = spreadsheet4.createCursor();
		rowIndex = 0;
		XCell cell = null;
		try {
			cell = cellCursor.getCellByPosition(0, rowIndex);
			XText cellText = (XText) UnoRuntime.queryInterface(XText.class,
					cell);
			cellText.setString(Messages.getString("reportWizardShow.number")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(1, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.cellHeadingID")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(2, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.date")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(3, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.debitAccount")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(4, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.creditAccount")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(5, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.reference")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(6, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.description")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(7, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.comment")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(8, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.cellheadingAmountDebit")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(9, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.cellheadingAmountCredit")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(10, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.cellheadingBalance")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(11, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.balanceIn")); //$NON-NLS-1$

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		rowIndex++;

		SimpleDateFormat sdf = new SimpleDateFormat(
				Messages.getString("reportWizardShow.listsDateFormat")); //$NON-NLS-1$
		for (account currentAccount : client.getAccounts().getCurrentChart()
				.getAccounts(false)) {
			Vector<entry> entries = currentAccount.getJournalForAccount(true);
			if (entries.size() > 0) {
				try {
					cell = cellCursor.getCellByPosition(0, rowIndex);
				} catch (IndexOutOfBoundsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				XText cellText = (XText) UnoRuntime.queryInterface(XText.class,
						cell);
				cellText.setString(currentAccount.getAsString());

				rowIndex++;
				BigDecimal balance = new BigDecimal(0);
				for (entry currentEntry : entries) {
					cell = null;
					try {
						cell = cellCursor.getCellByPosition(0, rowIndex);
						cell.setValue(currentEntry.getID());
						cell = cellCursor.getCellByPosition(1, rowIndex);
						cell.setValue(currentEntry.getNumber());
						cell = cellCursor.getCellByPosition(2, rowIndex);
						cellText = (XText) UnoRuntime.queryInterface(
								XText.class, cell);
						cellText.setString(sdf.format(currentEntry.getDate()));
						cell = cellCursor.getCellByPosition(3, rowIndex);
						cellText = (XText) UnoRuntime.queryInterface(
								XText.class, cell);
						cellText.setString(currentEntry.getDebitAccount()
								.getAsString());
						cell = cellCursor.getCellByPosition(4, rowIndex);
						cellText = (XText) UnoRuntime.queryInterface(
								XText.class, cell);
						cellText.setString(currentEntry.getCreditAccount()
								.getAsString());
						cell = cellCursor.getCellByPosition(5, rowIndex);
						cellText = (XText) UnoRuntime.queryInterface(
								XText.class, cell);
						cellText.setString(currentEntry.getReference());
						cell = cellCursor.getCellByPosition(6, rowIndex);
						cellText = (XText) UnoRuntime.queryInterface(
								XText.class, cell);
						cellText.setString(currentEntry.getDescription());
						cell = cellCursor.getCellByPosition(7, rowIndex);
						cellText = (XText) UnoRuntime.queryInterface(
								XText.class, cell);
						cellText.setString(currentEntry.getComment());
						int amountColNumber;
						if (!currentEntry.isCreditSide(currentAccount)) {
							amountColNumber = 8;

						} else {
							amountColNumber = 9;

						}
						if (currentEntry.increasesBalance(currentAccount)) {
							balance = balance.add(currentEntry.getValue());
						} else {
							balance = balance.subtract(currentEntry.getValue());
						}

						cell = cellCursor.getCellByPosition(amountColNumber,
								rowIndex);
						cell.setValue(currentEntry.getValue().doubleValue());
						cell = cellCursor.getCellByPosition(10, rowIndex);
						cell.setValue(balance.abs().doubleValue());

						boolean balanceInDebit = true;

						if (balance.compareTo(new BigDecimal("0")) == -1) { //$NON-NLS-1$
							balanceInDebit = !balanceInDebit;
						}
						if (!currentAccount.balanceIncreasesInDebit()) {
							balanceInDebit = !balanceInDebit;
						}

						cell = cellCursor.getCellByPosition(11, rowIndex);
						cellText = (XText) UnoRuntime.queryInterface(
								XText.class, cell);
						cellText.setString(balanceInDebit ? Messages
								.getString("reportWizardShow.balanceInDebit") : Messages.getString("reportWizardShow.balanceInCredit")); //$NON-NLS-1$ //$NON-NLS-2$

					} catch (Exception ex) {
						ex.printStackTrace();
					}
					rowIndex++;

				}
				rowIndex++;// empty row after each account

			}

		}
	}

	private void reportTAccounts() {
		XSheetCellCursor cellCursor;
		// insert your Data
		cellCursor = spreadsheet2.createCursor();
		int rowIndex = 0;

		for (Object currentAccountObject : client.getAccounts()
				.getCurrentChart().getAccounts(false)) {
			account currentAccount = (account) currentAccountObject;
			Vector<entry> accountJournal = currentAccount
					.getJournalForAccount(true);
			/*
			 * rm BigDecimal
			 * openingBalance=client.getEntries().getOpeningBalance();
			 */
			if (accountJournal.size() > 0) {
				tAccount ta = new tAccount(currentAccount, accountJournal);
				try {
					rowIndex = ta.draw(cellCursor, rowIndex) + 2;
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void reportAccountsList() {

		int rowIndex = 0;// =rowIndex
		XSheetCellCursor cellCursor = spreadsheet1.createCursor();

		XCell cell = null;

		// header
		try {
			cell = cellCursor.getCellByPosition(0, rowIndex);
			XText cellText = (XText) UnoRuntime.queryInterface(XText.class,
					cell);
			cellText.setString(Messages.getString("reportWizardShow.code")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(1, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.description")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(2, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.type")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(3, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.openingBalance")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(4, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.debit")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(5, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.credit")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(6, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.closingBalance")); //$NON-NLS-1$
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		BigDecimal grandTotalCredit=new BigDecimal(0);
		BigDecimal grandTotalDebit=new BigDecimal(0);

		// values

		for (Object currentObject : client.getAccounts().getCurrentChart()
				.getAccounts(false)) {
			account currentAccount = (account) currentObject;
			String code = currentAccount.getCode();
			String description = currentAccount.getDescription();
			String type = currentAccount.getTypeString();

			
			currentAccount.getJournalForAccount(true); // needed to initalize
														// account balance for
														// getAccountBalance()

			
			if (!currentAccount.getOpeningBalance().equals(BigDecimal.ZERO)||!currentAccount.getBalanceCredit().equals(BigDecimal.ZERO)||!currentAccount.getBalanceDebit().equals(BigDecimal.ZERO)) {
				rowIndex++;

				try {
					cell = cellCursor.getCellByPosition(0, rowIndex);
					XText cellText = (XText) UnoRuntime.queryInterface(XText.class,
							cell);
					cellText.setString(code);
					cell = cellCursor.getCellByPosition(1, rowIndex);
					cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
					cellText.setString(description);
					cell = cellCursor.getCellByPosition(2, rowIndex);
					cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
					cellText.setString(type);
					cell = cellCursor.getCellByPosition(3, rowIndex);
					cell.setValue(currentAccount.getOpeningBalance().doubleValue());
					cell = cellCursor.getCellByPosition(4, rowIndex);
					cell.setValue(currentAccount.getBalanceDebit().doubleValue());
					cell = cellCursor.getCellByPosition(5, rowIndex);
					cell.setValue(currentAccount.getBalanceCredit().doubleValue());
					cell = cellCursor.getCellByPosition(6, rowIndex);
					cell.setValue(currentAccount.getOpeningBalance().add(currentAccount.getBalanceTotal()).doubleValue());
					
					grandTotalDebit=grandTotalDebit.add(currentAccount.getBalanceDebit());
					grandTotalCredit=grandTotalCredit.add(currentAccount.getBalanceCredit());
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
			}

		}

		try {
			rowIndex++;
			cell = cellCursor.getCellByPosition(1, rowIndex);
			XText cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.total")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(4, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(grandTotalDebit.toString());
			cell = cellCursor.getCellByPosition(5, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(grandTotalCredit.toString());
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// return parentWizard.getAccountsList();
	}

	/**
	 * reports the net income statement (german: einnahme/überschussrechnnung)
	 * */
	private void reportNetIncome() {
		XSheetCellCursor cellCursor;
		int rowIndex;
		cellCursor = spreadsheet5.createCursor();
		rowIndex = 0;
		XCell cell = null;
		taxCalculator taxCalc = new taxCalculator();
		try {
			cell = cellCursor.getCellByPosition(0, rowIndex);
			XText cellText = (XText) UnoRuntime.queryInterface(XText.class,
					cell);
			cellText.setString(Messages
					.getString("reportWizardShow.netIncomeSheetOrganization")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(1, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString((configs.getOrganisationName().length() == 0 ? Messages
					.getString("reportWizardShow.netIncomeSheetNoOrganizationName") : configs.getOrganisationName())); //$NON-NLS-1$
			rowIndex++;

			cell = cellCursor.getCellByPosition(0, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.netIncomeSheetTaxID")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(1, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(configs.getTaxID());
			rowIndex++;

			BigDecimal income = taxCalc.getNetIncome();
			BigDecimal spendings = taxCalc.getNetSpendings();
			BigDecimal profit = income.subtract(spendings);

			cell = cellCursor.getCellByPosition(0, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.netIncomeSheetIncome")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(1, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			if (configs.isVATexempt()) {
				cell.setValue(0);
			} else {
				cell.setValue(income.doubleValue());
			}
			rowIndex++;

			cell = cellCursor.getCellByPosition(0, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.VATExemption")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(1, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			if (configs.isVATexempt()) {
				cell.setValue(income.doubleValue());
			} else {
				cell.setValue(0);
			}
			rowIndex++;

			cell = cellCursor.getCellByPosition(0, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.netIncomeSheetSpendings")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(1, rowIndex);
			cell.setValue(spendings.doubleValue());
			rowIndex++;

			cell = cellCursor.getCellByPosition(0, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.netIncomeSheetProfitLoss")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(1, rowIndex);
			cell.setValue(profit.doubleValue());
			rowIndex++;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		rowIndex = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$

		/*
		 * if account has expenses on debit (Aufwand auf soll) or income on
		 * credit (Ertrag auf Haben) the entry is supposed to enter the profit
		 * and loss sheet (GUV) income: income on credit
		 */
		// income
		cellCursor = spreadsheet6.createCursor();
		rowIndex = 0;
		incomeExpensesHeadlines(cellCursor, rowIndex);

		rowIndex++;
		try {
			for (entry currentEntry : completeJournal) {
				if (currentEntry.getCreditAccount().isIncomeAccount()) {
				
					if (currentEntry.getValue().compareTo(new BigDecimal(0)) > -1) {
						incomeExpensesLine(cellCursor, rowIndex, sdf,
								currentEntry);
						rowIndex++;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// expenses
		cellCursor = spreadsheet7.createCursor();
		rowIndex = 0;
		incomeExpensesHeadlines(cellCursor, rowIndex);
		rowIndex++;
		try {
			for (entry currentEntry : completeJournal) {
				if (currentEntry.getDebitAccount().isExpenseAccount()) {
					if (currentEntry.getValue().compareTo(new BigDecimal(0)) > -1) {
						incomeExpensesLine(cellCursor, rowIndex, sdf,
								currentEntry);
						rowIndex++;
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * adds a sheet with a list of assets
	 * */
	private void reportAssets() {
		XSheetCellCursor cellCursor;
		int rowIndex;
		// create the complete journal
		cellCursor = spreadsheet8.createCursor();
		rowIndex = 0;
		XCell cell = null;
		try {
			cell = cellCursor.getCellByPosition(0, rowIndex);
			XText cellText = (XText) UnoRuntime.queryInterface(XText.class,
					cell);
			cellText.setString(Messages
					.getString("reportWizardShow.assetNumberHeading")); //$NON-NLS-1$

			cell = cellCursor.getCellByPosition(1, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.assetNameHeading")); //$NON-NLS-1$

			cell = cellCursor.getCellByPosition(2, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.assetLocationHeading")); //$NON-NLS-1$

			cell = cellCursor.getCellByPosition(3, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.assetRemarksHeading")); //$NON-NLS-1$

			cell = cellCursor.getCellByPosition(4, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.assetProcureDateHeading")); //$NON-NLS-1$

			cell = cellCursor.getCellByPosition(5, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.assetPriceHeading")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(6, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.assetStatusHeading"));  //$NON-NLS-1$

			rowIndex++;
			SimpleDateFormat sdf = new SimpleDateFormat(
					Messages.getString("reportWizardShow.assetProcureDateFormat")); //$NON-NLS-1$

			for (asset currentAsset : client.getAssets().getAssets(false)) {
				cell = cellCursor.getCellByPosition(0, rowIndex);
				cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
				cellText.setString(currentAsset.getNumber());

				cell = cellCursor.getCellByPosition(1, rowIndex);
				cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
				cellText.setString(currentAsset.getName());

				cell = cellCursor.getCellByPosition(2, rowIndex);
				cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
				cellText.setString(currentAsset.getLocation());

				cell = cellCursor.getCellByPosition(3, rowIndex);
				cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
				cellText.setString(currentAsset.getRemark());

				cell = cellCursor.getCellByPosition(4, rowIndex);
				cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
				cellText.setString(sdf.format(currentAsset
						.getDepreciationStart()));

				cell = cellCursor.getCellByPosition(5, rowIndex);
				cell.setValue(currentAsset.getValue().doubleValue());

				cell = cellCursor.getCellByPosition(6, rowIndex);
				cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
				cellText.setString(asset.getStringForStatus(currentAsset.getStatus()));
				rowIndex++;

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void incomeExpensesLine(XSheetCellCursor cellCursor, int rowIndex,
			SimpleDateFormat sdf, entry currentEntry)
			throws IndexOutOfBoundsException {
		XCell cell;
		cell = cellCursor.getCellByPosition(0, rowIndex);
		XText cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);

		cellText.setString(sdf.format(currentEntry.getDate()));

		cell = cellCursor.getCellByPosition(1, rowIndex);
		cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
		cellText.setString(currentEntry.getDescription());

		cell = cellCursor.getCellByPosition(2, rowIndex);
		cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
		cellText.setString(currentEntry.getComment());

		cell = cellCursor.getCellByPosition(3, rowIndex);
		cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
		cellText.setString(currentEntry.getReference());

		cell = cellCursor.getCellByPosition(4, rowIndex);
		cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
		cellText.setString(currentEntry.getValue().toPlainString());
	}

	private void incomeExpensesHeadlines(XSheetCellCursor cellCursor,
			int rowIndex) {

		try {
			XCell cell;
			cell = cellCursor.getCellByPosition(0, rowIndex);
			XText cellText = (XText) UnoRuntime.queryInterface(XText.class,
					cell);
			cellText.setString(Messages.getString("reportWizardShow.date")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(1, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages
					.getString("reportWizardShow.description")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(2, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.comment")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(3, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.reference")); //$NON-NLS-1$
			cell = cellCursor.getCellByPosition(4, rowIndex);
			cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);
			cellText.setString(Messages.getString("reportWizardShow.value")); //$NON-NLS-1$
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		// reload the data to get potentially corrected report start/end dates
		if (visible) {
			((reportWizard) getWizard()).enableFinish();
			
			completeJournal = new Vector<entry>(client.getEntries()
					.getJournal(true));

			Collections.sort(completeJournal);

			
			reportAccountsList();
			reportTAccounts(); //18.2% CPU time on profile run
			reportJournal();//13.2% CPU time on profile run
			reportLists();//29.4% CPU time on profile run
			reportNetIncome();
			reportAssets();
		}
	}

	@Override
	public void dispose() {
		ISpreadsheetDocument spreadDocument = ((reportWizard) getWizard())
				.getSpreadDocument();
		if (spreadDocument != null) {
			spreadDocument.close();
			spreadDocument = null;
		}
		super.dispose();
	}

}
