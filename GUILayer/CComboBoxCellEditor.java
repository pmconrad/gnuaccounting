package GUILayer;

import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Composite;

//source http://dev.eclipse.org/newslists/news.eclipse.platform/msg61820.html
// reference https://bugs.eclipse.org/bugs/show_bug.cgi?id=104225
/**
 * Custom JFace style ComboBoxCellEditor implementation Stores values based on
 * Strings, not indexes so that custom user-defined text can be entered and
 * stored
 * 
 * @author John Mason
 * 
 */
public class CComboBoxCellEditor extends ComboBoxCellEditor {

	/**
	 * Single constructor to create ComboBoxCellEditor
	 */
	public CComboBoxCellEditor() {
		super();
	}

	/**
	 * Single constructor to create ComboBoxCellEditor with items
	 * 
	 * @param parent
	 * @param items
	 */
	public CComboBoxCellEditor(Composite parent, String[] items) {
		super(parent, items);
	}

	/**
	 * Single constructor to create ComboBoxCellEditor with items and style
	 * 
	 * @param parent
	 * @param items
	 * @param style
	 */
	public CComboBoxCellEditor(Composite parent, String[] items, int style) {
		super(parent, items, style);
	}

	/**
	 * doGetValue overwrites default return of Integer index to String
	 */
	protected Object doGetValue() {
		return ((CCombo) this.getControl()).getText();
	}

	/**
	 * doSetValue overwrites default setting of Integer index to String value
	 * 
	 * @param value
	 *            - MUST BE A String! (will set the index if string's match)
	 */
	protected void doSetValue(final Object value) {
		if (value == null) {
			return;
			// this is a work around
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=104225#c21
		}
		// Assert.isTrue(value instanceof String);
		assert value instanceof String;
		int selection = -1;

		for (int i = 0; i < ((CCombo) this.getControl()).getItemCount(); i++) {
			final String currentItem = ((CCombo) this.getControl()).getItem(i);

			if (currentItem.equals(value.toString())) {
				((CCombo) this.getControl()).select(selection);
				break;
			}
		}
		((CCombo) this.getControl()).setText(value.toString());
	}
}
