package appLayer;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;

import appLayer.transactionRelated.appTransaction;

/**
 * the booking cell modifier of accounting entries
 * */
public class transactionCellModifier implements ICellModifier {

	private TableViewer tableViewer;

	public transactionCellModifier(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
		client.getImportQueue().setDirty();
	}

	public boolean canModify(Object element, String property) {
		return true;
	}

	public void modify(Object element, String property, Object value) {

		TableItem item = (TableItem) element;
		appTransaction currentTransaction = (appTransaction) item.getData();
		currentTransaction.setColumnObject(property, value);
		// enhance or remove: JS 2012-02-14
		transactionFromBankAccountImport tba = client.getImportQueue()
				.getForID(currentTransaction.getImportID());
		// tba.setReference(currentTransaction.getDefaultReference());
		// tba.save();
		// finish

		tableViewer.refresh();
	}

	public Object getValue(Object element, String property) {

		return ((appTransaction) element).getColumnObject(property);
	}

}
