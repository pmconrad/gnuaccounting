package appLayer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ag.ion.bion.officelayer.desktop.GlobalCommands;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextRange;
import ag.ion.bion.officelayer.text.IViewCursor;
import ag.ion.noa.NOAException;
import ag.ion.noa.frame.IDispatch;
import appLayer.transactionRelated.appTransaction;

/**
 * The processing class for items within simple text.
 * 
 * @author Markus Kr�ger
 * @version $Revision: 11615 $
 * @date 11.01.2008
 */
public class itemTextList {

	private Shell shell = null;
	private ITextDocument document = null;
	private appTransaction theTransaction = null;
	private placeholderManager placeholderReplacements = null;
	private ITextField[] placeholderFields = null;

	// ----------------------------------------------------------------------------
	/**
	 * Constructs new itemTextList.
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
	public itemTextList(Shell shell, ITextDocument document,
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
		item currentItem = null;
		int numItems = theTransaction.getItems().size();
		getItemTemplate();
		for (int itemIndex = 0; itemIndex < numItems; itemIndex++) {
			currentItem = theTransaction.getItems().get(itemIndex);
			if (currentItem != null) {
				placeholderReplacements.setItem(currentItem, itemIndex+1);
				insertItem(currentItem, theTransaction);
			}
		}
	}

	// ----------------------------------------------------------------------------
	/**
	 * This will cut the text between placeholderManager.ITEMS_START_PLACEHOLDER
	 * and placeholderManager.ITEMS_END_PLACEHOLDER into the ooo.o clipboard.
	 * 
	 * @throws Throwable
	 *             if something fails
	 * 
	 * @author Markus Kr�ger
	 * @date 11.01.2008
	 */
	private void getItemTemplate() throws Throwable {
		ITextField itemsStartPlaceholder = null;
		ITextField itemsEndPlaceholder = null;

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
			}
		}
		if (itemsStartPlaceholder != null) {

			ITextRange textRangeStart = itemsStartPlaceholder.getTextRange();
			if (itemsEndPlaceholder != null) {

				ITextRange textRangeEnd = itemsEndPlaceholder.getTextRange();

				IDispatch cut = document.getFrame().getDispatch(".uno:Cut"); //$NON-NLS-1$
				IViewCursor viewCursor = document.getViewCursorService()
						.getViewCursor();

				// remove the placeholderManager.ITEMS_START_PLACEHOLDER and
				// placeholderManager.ITEMS_END_PLACEHOLDER parts of the tag
				// before we start
				textRangeStart.setText(""); //$NON-NLS-1$
				textRangeEnd.setText(""); //$NON-NLS-1$

				// with the (invisible) text cursor, go, without marking (the
				// false parameter), to the first occurence
				// select from end of placeholderManager.ITEMS_START_PLACEHOLDER
				// to beginning of placeholderManager.ITEMS_END_PLACEHOLDER
				viewCursor.goToRange(textRangeStart, false);

				// now start marking (true) to the second occurence
				viewCursor.goToRange(textRangeEnd, true);
				// now select from beginning of
				// placeholderManager.ITEMS_START_PLACEHOLDER to end of
				// placeholderManager.ITEMS_END_PLACEHOLDER to remove the
				// template

				// now cut it into the clipboard -- it will be pasted by
				// insertItem
				cut.dispatch();

			} else {
				MessageDialog
						.openError(
								shell,
								Messages.getString("itemTextList.invalidTemplate"), Messages.getString("itemTextList.templateReqs1")); //$NON-NLS-1$ //$NON-NLS-2$
			}

		} else {
			MessageDialog
					.openError(
							shell,
							Messages.getString("itemTextList.invalidTemplate"), Messages.getString("itemTextList.templateReqs2")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	// ----------------------------------------------------------------------------
	/**
	 * This inserts the line for the given item.
	 * 
	 * @param currentItem
	 *            the item to be inserted
	 * @param currentTransaction
	 *            the transaction to be used
	 * 
	 * @throws Throwable
	 *             if something fails
	 * 
	 * @author Markus Kr�ger
	 * @date 11.01.2008
	 */
	private void insertItem(item currentItem, appTransaction currentTransaction)
			throws Throwable {
		try {
			IDispatch paste = document.getFrame().getDispatch(
					GlobalCommands.PASTE);
			paste.dispatch();
		} catch (NOAException e1) {
			e1.printStackTrace();
		}
		ITextField[] placeholderFields = document.getTextFieldService()
				.getPlaceholderFields();
		for (int i = 0; i < placeholderFields.length; i++) {
			ITextField placeholder = placeholderFields[i];
			ITextRange range = placeholder.getTextRange();
			String displayText = placeholder.getDisplayText();
			String value = placeholderReplacements
					.getPlaceholderValue(placeholder);

			range.setText(value);
		}
	}
	// ----------------------------------------------------------------------------

}
