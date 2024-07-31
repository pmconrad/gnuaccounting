package appLayer;

import java.math.BigDecimal;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ag.ion.bion.officelayer.desktop.GlobalCommands;
import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextRange;
import ag.ion.bion.officelayer.text.ITextTable;
import ag.ion.bion.officelayer.text.ITextTableCell;
import ag.ion.bion.officelayer.text.ITextTableCellRange;
import ag.ion.bion.officelayer.text.table.IFormula;
import ag.ion.bion.officelayer.text.table.IFormulaService;
import ag.ion.bion.officelayer.text.table.ITextTableCellReferencesService;
import ag.ion.bion.officelayer.text.table.TextTableCellNameHelper;
import ag.ion.noa.text.TextRangeSelection;
import ag.ion.noa.text.XInterfaceObjectSelection;
import appLayer.taxRelated.tax;
import appLayer.transactionRelated.appTransaction;

/**
 * The processing class for items within a table.
 * 
 * @author Markus Kr�ger
 * @version $Revision: 11615 $
 * @date 11.01.2008
 */
public class itemTableList {

	private Shell shell = null;
	private ITextDocument document = null;
	private appTransaction theTransaction = null;
	private placeholderManager placeholderReplacements = null;
	private ITextField[] placeholderFields = null;

	// ----------------------------------------------------------------------------
	/**
	 * Constructs new itemTableList.
	 * 
	 * @param shell
	 *            the shell to be used for messages
	 * @param document
	 *            the current document
	 * @param theTransaction
	 *            the current transaction
	 * @param placeHolderManager
	 *            the placeholder manager be used
	 * @param placeholderFields
	 *            the current placeholders
	 * 
	 * @author Markus Kr�ger
	 * @date 11.01.2008
	 */
	public itemTableList(Shell shell, ITextDocument document,
			appTransaction theTransaction,
			placeholderManager placeHolderManager,
			ITextField[] placeholderFields) {
		this.shell = shell;
		this.document = document;
		this.theTransaction = theTransaction;
		this.placeholderReplacements = placeHolderManager;
		placeholderReplacements.setTransaction(theTransaction);
		this.placeholderFields = placeholderFields;
	}

	// ----------------------------------------------------------------------------
	/**
	 * Processes the items.
	 * 
	 * @throws Throwable
	 *             if something fails in the process
	 * 
	 * @author Markus Kr�ger
	 * @date 11.01.2008
	 */
	public void process() throws Throwable {
		ITextField itemsStartPlaceholder = null;
		ITextField itemsEndPlaceholder = null;

		ITextField taxesStartPlaceholder = null;
		ITextField taxesEndPlaceholder = null;

		for (int i = 0; i < placeholderFields.length; i++) {
			ITextField placeholder = placeholderFields[i];
			String placeholderDisplayText = placeholder.getDisplayText();
			if (placeholderDisplayText
					.equals(placeholderManager.START_PLACEHOLDER
							+ placeholderManager.ITEMS_START_PLACEHOLDER
							+ placeholderManager.END_PLACEHOLDER)) {
				itemsStartPlaceholder = placeholder;
			} else if (placeholderDisplayText
					.equals(placeholderManager.START_PLACEHOLDER
							+ placeholderManager.ITEMS_END_PLACEHOLDER
							+ placeholderManager.END_PLACEHOLDER)) {
				itemsEndPlaceholder = placeholder;
			} else if (placeholderDisplayText
					.equals(placeholderManager.START_PLACEHOLDER
							+ placeholderManager.TAXES_START_PLACEHOLDER
							+ placeholderManager.END_PLACEHOLDER)) {
				taxesStartPlaceholder = placeholder;
			} else if (placeholderDisplayText
					.equals(placeholderManager.START_PLACEHOLDER
							+ placeholderManager.TAXES_END_PLACEHOLDER
							+ placeholderManager.END_PLACEHOLDER)) {
				taxesEndPlaceholder = placeholder;
			}

		}
		if (itemsEndPlaceholder != null) {
			ITextTableCell startCell = itemsStartPlaceholder.getTextRange()
					.getCell();
			ITextTableCell endCell = itemsEndPlaceholder.getTextRange()
					.getCell();
			boolean endIsInTable1 = endCell != null;
			if (endIsInTable1) {
				ITextTable table = startCell.getTextTable();
				if (table.getName().equals(endCell.getTextTable().getName())) {
					itemsStartPlaceholder.getTextRange().setText(""); //$NON-NLS-1$
					itemsEndPlaceholder.getTextRange().setText(""); //$NON-NLS-1$
					processSingleTableItems(table, startCell, endCell);
				} else {
					MessageDialog
							.openError(
									shell,
									Messages.getString("itemTableList.invalidtemplate"), Messages.getString("itemTableList.placeholdererror")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				MessageDialog
						.openError(
								shell,
								Messages.getString("itemTableList.invalidtemplate"), Messages.getString("itemTableList.templatereqs")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			MessageDialog
					.openError(
							shell,
							Messages.getString("itemTableList.invalidtemplate"), Messages.getString("itemTableList.templareReqs2")); //$NON-NLS-1$ //$NON-NLS-2$

		}

		if (taxesEndPlaceholder != null) {
			ITextTableCell startCell = taxesStartPlaceholder.getTextRange()
					.getCell();
			ITextTableCell endCell = taxesEndPlaceholder.getTextRange()
					.getCell();
			boolean endIsInTable1 = endCell != null;
			if (endIsInTable1) {
				ITextTable table = startCell.getTextTable();
				if (table.getName().equals(endCell.getTextTable().getName())) {
					taxesStartPlaceholder.getTextRange().setText(""); //$NON-NLS-1$
					taxesEndPlaceholder.getTextRange().setText(""); //$NON-NLS-1$
					processSingleTableTaxes(table, startCell, endCell);
				} else {
					MessageDialog
							.openError(
									shell,
									Messages.getString("itemTableList.invalidtemplate"), Messages.getString("itemTableList.taxesNotSameTable")); //$NON-NLS-1$ //$NON-NLS-2$ 
				}
			} else {
				MessageDialog
						.openError(
								shell,
								Messages.getString("itemTableList.invalidtemplate"), Messages.getString("itemTableList.taxesNotInTable")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} // start/end of <taxes:> is not required, contrary to items, so no
			// else error message here
	}

	// ----------------------------------------------------------------------------
	/**
	 * This processes the table for the items.
	 * 
	 * @param table
	 *            the table to be processed
	 * @param startCell
	 *            the start cell used for the items
	 * @param endCell
	 *            the end cell used for the items
	 * 
	 * @throws Throwable
	 *             if something fails
	 * 
	 * @author Markus Kr�ger
	 * @date 11.01.2008
	 */
	private void processSingleTableItems(ITextTable table,
			ITextTableCell startCell, ITextTableCell endCell) throws Throwable {

		int numItems = theTransaction.getItems().size();

		// get data lines indexes
		int start = startCell.getName().getRowIndex();
		int end = endCell.getName().getRowIndex();

		if (numItems > 0) {
			// erstmal gucken
			ITextTableCell[] additionalRowCells = table.getRow(end).getCells();
			String endColumn = TextTableCellNameHelper
					.getColumnCharacter(additionalRowCells.length - 1);

			String cellRangeToCopy = "A" + (start + 1) + ":" + endColumn + (end + 1); //$NON-NLS-1$ //$NON-NLS-2$
			String cellRangeToCopyTo = "A" + (start + 1 + 1) + ":" + endColumn + (end + 1 + 1); //$NON-NLS-1$ //$NON-NLS-2$

			// copy line
			ITextTableCellRange cellRange = table.getCellRange(cellRangeToCopy);
			IFrame frame = document.getFrame();
			for (int i = 0; i < numItems - 1; i++) {
				document.setSelection(new XInterfaceObjectSelection(cellRange
						.getXCellRange()));
				frame.getDispatch(GlobalCommands.COPY).dispatch();
				frame.getDispatch(GlobalCommands.INSTER_ROWS).dispatch();
				// sleep here some time as otherwise the PASTE occurs to fast
				// and so not in the new inserted row
				Thread.sleep(50);
				ITextTableCellRange cellRange2 = table
						.getCellRange(cellRangeToCopyTo);
				document.setSelection(new XInterfaceObjectSelection(cellRange2
						.getXCellRange()));
				frame.getDispatch(GlobalCommands.PASTE).dispatch();
			}

			// set selection somewhere outside of the table to make table
			// toolbar disapear if selection was put to table by user
			document.setSelection(new TextRangeSelection(document
					.getTextService().getText().getTextCursorService()
					.getTextCursor().getStart()));

			// set data
			int lastDataRow = 0;
			for (int i = 0, n = numItems; i < n; i++) {
				item currentItem = theTransaction.getItems().get(i);
				placeholderReplacements.setItem(currentItem, i+1);
				int currentStart = start + i;
				int currentEnd = end + i;
				for (int j = currentStart; j <= currentEnd; j++) {
					ITextTableCell[] cells = table.getRow(j).getCells();
					for (int k = 0; k < cells.length; k++) {
						ITextTableCell cell = cells[k];
						ITextField[] fields = cell.getTextService().getText()
								.getTextContentEnumeration().getTextFields();
						for (int l = 0; l < fields.length; l++) {
							ITextField field = fields[l];
							ITextRange range = field.getTextRange();

							String value = placeholderReplacements
									.getPlaceholderValue(field);
							// maybe only a number inside
							/*
							 * if (cell.getTextService().getText() .getText()
							 * .equals(placeholderDisplay)) { try { double
							 * doubleValue = Double .parseDouble(value);
							 * range.setText(""); //$NON-NLS-1$
							 * cell.setValue(doubleValue); } catch (Throwable e)
							 * { this (exception) is actually the usual case
							 * e..g. for product names which will throw sth like
							 * a unconvertableexception to double
							 * 
							 * range.setText(value); } } else {
							 */

							range.setText(value);

						}
					}
				}
				lastDataRow = currentEnd;
			}

			// handle formulas
			if (numItems > 1) {
				// handle formulas before data
				updateFormulas(table, 0, start, start, end, numItems);
				// handle formulas after data
				int rowCount = table.getRowCount();
				if (rowCount - 1 > lastDataRow) {
					updateFormulas(table, lastDataRow + 1, rowCount, start,
							end, numItems);
				}
			}
		}
	}

	private void processSingleTableTaxes(ITextTable table,
			ITextTableCell startCell, ITextTableCell endCell) throws Throwable {

		Vector<tax> vats = new Vector<tax>();

		theTransaction.prepareVATamounts();
		for (tax currentTax : client.getTaxes().getVATArray()) {
			BigDecimal amount = theTransaction.getVATAmountForTax(currentTax);

			BigDecimal zero=new BigDecimal(0);
			if ((amount != null)&&(zero.compareTo(amount)!=0)) {
				vats.add(currentTax);
			}
		}

		int numItems = vats.size();

		// get data lines indexes
		int start = startCell.getName().getRowIndex();
		int end = endCell.getName().getRowIndex();

		if (numItems > 0) {
			// erstmal gucken
			ITextTableCell[] additionalRowCells = table.getRow(end).getCells();
			String endColumn = TextTableCellNameHelper
					.getColumnCharacter(additionalRowCells.length - 1);

			String cellRangeToCopy = "A" + (start + 1) + ":" + endColumn + (end + 1); //$NON-NLS-1$ //$NON-NLS-2$
			String cellRangeToCopyTo = "A" + (start + 1 + 1) + ":" + endColumn + (end + 1 + 1); //$NON-NLS-1$ //$NON-NLS-2$

			// copy line
			ITextTableCellRange cellRange = table.getCellRange(cellRangeToCopy);
			IFrame frame = document.getFrame();
			for (int i = 0; i < numItems - 1; i++) {
				document.setSelection(new XInterfaceObjectSelection(cellRange
						.getXCellRange()));
				frame.getDispatch(GlobalCommands.COPY).dispatch();
				frame.getDispatch(GlobalCommands.INSTER_ROWS).dispatch();
				// sleep here some time as otherwise the PASTE occurs to fast
				// and so not in the new inserted row
				Thread.sleep(50);
				ITextTableCellRange cellRange2 = table
						.getCellRange(cellRangeToCopyTo);
				document.setSelection(new XInterfaceObjectSelection(cellRange2
						.getXCellRange()));
				frame.getDispatch(GlobalCommands.PASTE).dispatch();
			}

			// set selection somewhere outside of the table to make table
			// toolbar disapear if selection was put to table by user
			document.setSelection(new TextRangeSelection(document
					.getTextService().getText().getTextCursorService()
					.getTextCursor().getStart()));

			// set data
			int lastDataRow = 0;
			for (int i = 0, n = numItems; i < n; i++) {
				tax currentTax = vats.get(i);
				placeholderReplacements.setTax(currentTax);
				int currentStart = start + i;
				int currentEnd = end + i;
				for (int j = currentStart; j <= currentEnd; j++) {
					ITextTableCell[] cells = table.getRow(j).getCells();
					for (int k = 0; k < cells.length; k++) {
						ITextTableCell cell = cells[k];
						ITextField[] fields = cell.getTextService().getText()
								.getTextContentEnumeration().getTextFields();
						for (int l = 0; l < fields.length; l++) {
							ITextField field = fields[l];
							ITextRange range = field.getTextRange();
							String displayText = field.getDisplayText();

							if (displayText
									.startsWith(placeholderManager.START_PLACEHOLDER
											+ placeholderManager.TAX_PLACEHOLDER)) {
								String value = placeholderReplacements
										.getPlaceholderValue(field);
								// maybe only a number inside
								/*
								 * if (cell.getTextService().getText()
								 * .getText() .equals(placeholderDisplay)) { try
								 * { double doubleValue = Double
								 * .parseDouble(value); range.setText("");
								 * //$NON-NLS-1$ cell.setValue(doubleValue); }
								 * catch (Throwable e) { /* this (exception) is
								 * actually the usual case e..g. for product
								 * names which will throw sth like a
								 * unconvertableexception to double
								 * 
								 * range.setText(value); } } else {
								 */

								range.setText(value);

							}
						}
					}
				}
				lastDataRow = currentEnd;
			}

			// handle formulas
			if (numItems > 1) {
				// handle formulas before data
				updateFormulas(table, 0, start, start, end, numItems);
				// handle formulas after data
				int rowCount = table.getRowCount();
				if (rowCount - 1 > lastDataRow) {
					updateFormulas(table, lastDataRow + 1, rowCount, start,
							end, numItems);
				}
			}
		}
	}

	// ----------------------------------------------------------------------------
	/**
	 * This updates formulas in the table.
	 * 
	 * @param table
	 *            the table to be used
	 * @param rowStart
	 *            the start row
	 * @param rowEnd
	 *            the end row
	 * @param dataStart
	 *            the row actual data starts
	 * @param dataEnd
	 *            the row actual data end
	 * @param numItems
	 *            the number of items
	 * 
	 * @throws Throwable
	 *             if something fails
	 * 
	 * @author Markus Kr�ger
	 * @date 11.01.2008
	 */
	private void updateFormulas(ITextTable table, int rowStart, int rowEnd,
			int dataStart, int dataEnd, int numItems) throws Throwable {
		for (int i = rowStart; i < rowEnd; i++) {
			ITextTableCell[] cells = table.getRow(i).getCells();
			for (int j = 0; j < cells.length; j++) {
				IFormulaService formulaService = cells[j].getFormulaService();
				if (formulaService != null) {
					IFormula formula = formulaService.getFormula();
					if (formula != null) {
						ITextTableCellReferencesService referencesService = formula
								.getCellReferencesService();
						boolean changed = false;
						for (int k = dataStart; k <= dataEnd; k++) {
							int[] extendedRows = new int[numItems];
							extendedRows[0] = k;
							for (int l = 1; l < numItems; l++) {
								extendedRows[l] = l * 1 + k;
							}
							if (referencesService.hasRowReferenceTo(k)) {
								referencesService.extendRowReferences(k,
										extendedRows);
								changed = true;
							}
						}
						if (changed) {
							referencesService.applyModifications();
							formulaService.setFormula(formula.getExpression());
						}
					}
				}
			}
		}
	}
	// ----------------------------------------------------------------------------

}
