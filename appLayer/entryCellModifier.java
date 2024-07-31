package appLayer;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;

/**
 * the booking cell modifier of accounting entries
 * */
public class entryCellModifier implements ICellModifier {

	private TableViewer tableViewer;

	public entryCellModifier(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
		client.getImportQueue().setDirty();
	}

	public boolean canModify(Object element, String property) {
		return true;
	}

	public void modify(Object element, String property, Object value) {

		TableItem item = (TableItem) element;
		entry currentEntry = (entry) item.getData();
		currentEntry.setColumnObject(property, value);
		tableViewer.refresh();
	}

	public Object getValue(Object element, String property) {

		return ((entry) element).getColumnObject(property);
	}

}
