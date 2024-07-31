package GUILayer;

//based on: https://bugs.eclipse.org/bugs/attachment.cgi?id=82327 of https://bugs.eclipse.org/bugs/show_bug.cgi?id=209022
//modified to support Dates Objects instead of Time Strings
import java.util.Date;
import java.util.Calendar;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;

public class DateCellEditor extends CellEditor {

	private DateTime dateTime;

	/**
	 * Default style
	 */
	private static final int defaultStyle = SWT.DATE | SWT.DROP_DOWN;

	public DateCellEditor(Composite parent) {
		super(parent, defaultStyle);
	}

	/*
	 * (non-Javadoc) Method declared on CellEditor.
	 */
	protected Control createControl(Composite parent) {

		int style = getStyle() | SWT.TIME;

		dateTime = new DateTime(parent, style);

		dateTime.addKeyListener(new KeyAdapter() {
			// hook key pressed - see PR 14201
			public void keyPressed(KeyEvent e) {
				keyReleaseOccured(e);
			}
		});

		dateTime.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				applyEditorValueAndDeactivate();
			}

		});

		dateTime.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE
						|| e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
				}
			}
		});

		dateTime.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				DateCellEditor.this.focusLost();
			}
		});
		return dateTime;
	}

	/*
	 * (non-Javadoc) Method declared on CellEditor.
	 */
	protected Object doGetValue() {
		Calendar selectedCalendarDate = Calendar.getInstance();
		selectedCalendarDate.set(Calendar.YEAR, dateTime.getYear());
		selectedCalendarDate.set(Calendar.MONTH, dateTime.getMonth());
		selectedCalendarDate.set(Calendar.DAY_OF_MONTH, dateTime.getDay());
		Date selectedDate = selectedCalendarDate.getTime();
		return selectedDate;
	}

	/*
	 * (non-Javadoc) Method declared on CellEditor.
	 */
	protected void doSetFocus() {
		dateTime.setFocus();
	}

	/*
	 * (non-Javadoc) Method declared on CellEditor.
	 */
	protected void doSetValue(Object value) {
		Date dateToSet = (Date) value;
		Calendar cal = Calendar.getInstance();
		if (dateToSet != null) {
			cal.setTime(dateToSet);
		}

		dateTime.setYear(cal.get(Calendar.YEAR));
		dateTime.setMonth(cal.get(Calendar.MONTH));
		dateTime.setDay(cal.get(Calendar.DAY_OF_MONTH));

	}

	/**
	 * Applies the currently selected value and deactivates the cell editor
	 */
	void applyEditorValueAndDeactivate() {
		Object newValue = doGetValue();
		markDirty();
		boolean isValid = isCorrect(newValue);
		setValueValid(isValid);
		fireApplyEditorValue();
		deactivate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor#focusLost()
	 */
	protected void focusLost() {
		if (isActivated()) {
			applyEditorValueAndDeactivate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.CellEditor#keyReleaseOccured(org.eclipse.swt
	 * .events.KeyEvent)
	 */
	protected void keyReleaseOccured(KeyEvent keyEvent) {
		if (keyEvent.character == '\u001b') { // Escape character
			fireCancelEditor();
		} else if (keyEvent.character == '\t') { // tab key
			applyEditorValueAndDeactivate();
		}
	}

}