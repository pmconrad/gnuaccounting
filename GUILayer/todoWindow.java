package GUILayer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.entry;
import appLayer.transactionRelated.appTransaction;
import appLayer.transactionRelated.cancelation;
import appLayer.transactionRelated.invoice;
import appLayer.transactionRelated.reminder;
import appLayer.transactionRelated.transactionType;

class todoBalanceSelectionAdapter extends SelectionAdapter {
	private int transID;
	private Shell shell = null;

	public todoBalanceSelectionAdapter(int transID, Shell shell) {
		this.transID = transID;
		this.shell = shell;
	}

	public void widgetSelected(final SelectionEvent e) {

		appTransaction trans = null;
		if (e.text.equals("book")) { //$NON-NLS-1$

			appTransaction theTransaction = client.getTransactions().getByID(
					transID);
			Vector<appTransaction> nextStep = new Vector<appTransaction>();
			nextStep.add(theTransaction);
			transactionDetailWindow tdw = new transactionDetailWindow(nextStep);
			tdw.open();
		} else if (e.text.equals("cancel")) { //$NON-NLS-1$

			newTransactionWizard wizard = new newTransactionWizard();
			wizard.setShowReferencedTypes(true);
			client.getTransactions().setAsCurrentTransaction(transID);

			client.getTransactions().setTransactionListIndex(
					client.getTransactions().getInstanceIndexForTypeID(
							cancelation.getType())); //$NON-NLS-1$
			trans = client.getTransactions().getCurrentTransaction();
			trans.setReferTo(transID);
			String number = trans.getNumber();
			String dateIssue = trans.getIssueDate().toString();
			String dateDue = trans.getDueDate().toString();
			trans.setRemarks(Messages.getString("todoWindow.number") + number + Messages.getString("todoWindow.XOfY") + dateIssue + Messages.getString("todoWindow.dueOn") + dateDue); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			trans.getNewTransactionNumber();

			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.open();

		} else if (e.text.equals("invoice")) { //$NON-NLS-1$
			newTransactionWizard wizard = new newTransactionWizard();
			appTransaction atrans = client.getEntries().getEntryForID(transID)
					.getReferredTransaction();
			client.getTransactions().setInstanceByTypeID(invoice.getType(),
					atrans);

			atrans.setReferTo(atrans.getID());
			invoice inv = new invoice(client.getTransactions());
			inv.cloneFrom(trans);

			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.open();

		} else if (e.text.equals("discard")) { //$NON-NLS-1$
			entry centry = client.getEntries().getEntryForID(transID);
			centry.delete();
			todoWindow.refreshToDoList();

		} else if (e.text.equals("remind")) { //$NON-NLS-1$
			newTransactionWizard wizard = new newTransactionWizard();
			wizard.setShowReferencedTypes(true);

			client.getTransactions().setAsCurrentTransaction(transID);
			client.getTransactions().setTransactionListIndex(
					client.getTransactions().getInstanceIndexForTypeID(
							reminder.getType())); //$NON-NLS-1$
			trans = client.getTransactions().getCurrentTransaction();
			trans.setReferTo(transID);
			String number = trans.getNumber();
			String dateIssue = trans.getIssueDate().toString();
			String dateDue = trans.getDueDate().toString();
			trans.setRemarks(Messages.getString("todoWindow.number") + number + Messages.getString("todoWindow.XOfY") + dateIssue + Messages.getString("todoWindow.dueOn") + dateDue); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			trans.getNewTransactionNumber();

			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.open();

		}
	}

}

public class todoWindow extends ApplicationWindow {

	private static Table table;
	private static Vector<Link> tableEditors = new Vector<Link>();
	private static int toDoItems = 0;
	private static Shell shell = null;

	/**
	 * Create the application window
	 */
	public todoWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
		todoWindow.shell = getShell();
	}

	private static void addTodoItem(String itemText, String linkText,
			SelectionAdapter sel) {
		toDoItems++;
		if ((MainWindow.isTodoWindowOpen()) && (table != null)) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(itemText);
			// item.setText(1,"ME");

			TableEditor editor = new TableEditor(table);
			Control oldEditor = editor.getEditor();
			if (oldEditor != null)
				oldEditor.dispose();

			final Link nativeLink = new Link(table, SWT.NONE);
			nativeLink.addSelectionListener(sel);
			nativeLink.setText(linkText);
			editor.grabHorizontal = true;
			editor.setEditor(nativeLink, item, 1);
			tableEditors.add(nativeLink);
		}

	}

	public static int refreshToDoList() {
		toDoItems = 0;
		if (MainWindow.isTodoWindowOpen()) {
			// we need to dispose them to disappear
			for (Link currentLink : tableEditors) {
				currentLink.dispose();
			}
			tableEditors.removeAllElements();
			table.removeAll();
			table.redraw();

		}

	
		if (client.getContacts().containsInstallationDefault()) {
			addTodoItem(
					Messages.getString("todoWindow.insertCustomers"), Messages.getString("todoWindow.editCustomers"), new SelectionAdapter() { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						public void widgetSelected(final SelectionEvent e) {
							contactsWindow win = new contactsWindow();
							win.open();
						}
					});
		}

		if (client.getProducts().containsInstallationDefault()) {
			addTodoItem(
					Messages.getString("todoWindow.pleaseInsertProducts"), Messages.getString("todoWindow.editProducts"), new SelectionAdapter() { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						public void widgetSelected(final SelectionEvent e) {
							productsWindow win = new productsWindow();
							win.open();
						}
					});

		}

		toDoItems += checkTemplates(
				MainWindow.isTodoWindowOpen(),
				Messages.getString("todoWindow.adjustTemplates"), Messages.getString("todoWindow.editTemplates"), new SelectionAdapter() { //$NON-NLS-1$ //$NON-NLS-2$
					public void widgetSelected(final SelectionEvent e) {
						designerWindow win = new designerWindow();
						win.open();
					}
				});
		// client.getEntries().getEntriesFromDatabase();
		toDoItems += checkUnbalancedInvoices(MainWindow.isTodoWindowOpen());
		toDoItems += checkUnbalancedCreditnotes(MainWindow.isTodoWindowOpen());
		toDoItems += checkUnreferencedEntries(MainWindow.isTodoWindowOpen());

		MainWindow.updateNrTodoItems(toDoItems);
		return toDoItems;

	}

	/**
	 * Create contents of the application window
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());

		Composite container = new Composite(parent, SWT.NONE);

		container.setFont(configs.getDefaultFont());
		container.setLayout(new FormLayout());

		table = new Table(container, SWT.FULL_SELECTION | SWT.BORDER);
		final FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(100, -5);
		fd_table.right = new FormAttachment(100, -5);
		fd_table.top = new FormAttachment(0, 5);
		fd_table.left = new FormAttachment(0, 5);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		final TableColumn newColumnTableColumn = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn.setWidth(300);
		newColumnTableColumn.setText(Messages.getString("todoWindow.todo")); //$NON-NLS-1$
		final TableColumn newColumnTableColumn_1 = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn_1.setWidth(300);
		newColumnTableColumn_1.setText(Messages.getString("todoWindow.action")); //$NON-NLS-1$

		MainWindow.signalTodoWindowOpen(true);
		refreshToDoList();

		/*
		 * sample for multi row tables final int COLUMN_COUNT = 4; final int
		 * ITEM_COUNT = 8; final int TEXT_MARGIN = 3;
		 * table.setHeaderVisible(true); table.setLinesVisible(true); for (int i
		 * = 0; i < COLUMN_COUNT; i++) { new TableColumn(table, SWT.NONE); } for
		 * (int i = 0; i < ITEM_COUNT; i++) { TableItem item = new
		 * TableItem(table, SWT.NONE); for (int j = 0; j < COLUMN_COUNT; j++) {
		 * String string = "item " + i + " col " + j; if ((i + j) % 3 == 1) {
		 * string +="\nnew line1"; } if ((i + j) % 3 == 2) { string
		 * +="\nnew line1\nnew line2"; } item.setText(j, string); } }
		 * 
		 * table.addListener(SWT.MeasureItem, new Listener() { public void
		 * handleEvent(Event event) { TableItem item = (TableItem)event.item;
		 * String text = item.getText(event.index); Point size =
		 * event.gc.textExtent(text); event.width = size.x + 2 * TEXT_MARGIN;
		 * event.height = Math.max(event.height, size.y + TEXT_MARGIN); } });
		 * table.addListener(SWT.EraseItem, new Listener() { public void
		 * handleEvent(Event event) { event.detail &= ~SWT.FOREGROUND; } });
		 * table.addListener(SWT.PaintItem, new Listener() { public void
		 * handleEvent(Event event) { TableItem item = (TableItem)event.item;
		 * String text = item.getText(event.index); // center column 1
		 * vertically int yOffset = 0; if (event.index == 1) { Point size =
		 * event.gc.textExtent(text); yOffset = Math.max(0, (event.height -
		 * size.y) / 2); } event.gc.drawText(text, event.x + TEXT_MARGIN,
		 * event.y + yOffset, true); } });
		 * 
		 * for (int i = 0; i < COLUMN_COUNT; i++) { table.getColumn(i).pack(); }
		 */

		//
		return container;
	}

	private static int checkTemplates(boolean addTableItems, String message,
			String link, SelectionAdapter toExecute) {
		int numRows = 0;

		String templateFilename;
		templateFilename = "./init/defaulttemplate-1.odt"; //$NON-NLS-1$
		long defaultFileSize = new File(templateFilename).length(); //$NON-NLS-1$
		for (transactionType currentType : client.getTransactions()
				.getAllTypes()) {
			long currentFileSize = (new File(client.getDataPath()
					+ currentType.getTypePrefix() + "template1.odt")).length(); //$NON-NLS-1$
			if (currentFileSize == defaultFileSize) {
				numRows = 1;
			}

		}

		if ((numRows > 0) && (addTableItems)) {

			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(message);

			TableEditor editor = new TableEditor(table);
			Control oldEditor = editor.getEditor();
			if (oldEditor != null)
				oldEditor.dispose();

			final Link nativeLink = new Link(table, SWT.NONE);
			nativeLink.addSelectionListener(toExecute);
			nativeLink.setText(link);
			editor.grabHorizontal = true;
			editor.setEditor(nativeLink, item, 1);
			tableEditors.add(nativeLink);
		}

		return numRows;
	}

	private static int checkUnbalancedInvoices(boolean addTableItems) {
		HashMap<Integer, String> unbalancedInvoices = client.getTransactions()
				.getUnbalancedInvoices();
		if (addTableItems) {
			for (Integer unbalancedInvoiceID : unbalancedInvoices.keySet()) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(Messages.getString("todoWindow.invoice") + unbalancedInvoices.get(unbalancedInvoiceID) + Messages.getString("todoWindow.notYetBalanced")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

				TableEditor editor = new TableEditor(table);
				Control oldEditor = editor.getEditor();
				if (oldEditor != null)
					oldEditor.dispose();

				final Link nativeLink = new Link(table, SWT.NONE);
				nativeLink
						.addSelectionListener(new todoBalanceSelectionAdapter(
								unbalancedInvoiceID, shell)); //$NON-NLS-1$
				nativeLink
						.setText(Messages.getString("todoWindow.bookPayment")); //$NON-NLS-1$
				editor.grabHorizontal = true;
				editor.setEditor(nativeLink, item, 1);
				tableEditors.add(nativeLink);

			}
		}
		if (unbalancedInvoices != null) {
			return unbalancedInvoices.size();
		} else {
			return 0;
		}
	}

	/**
	 * This will check if any entries exist where the referenced document number
	 * is empty or <todo/>
	 * */
	private static int checkUnreferencedEntries(boolean addTableItems) {
		int emptyOrTodoReferences = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
		for (entry currentEntry : client.getEntries().getJournal(false)) {
			if (currentEntry.getReference().equals("") //$NON-NLS-1$
					|| currentEntry.getReference().equalsIgnoreCase("<todo/>")) { //$NON-NLS-1$
				emptyOrTodoReferences++;
				if (addTableItems) {

					TableItem item = new TableItem(table, SWT.NONE);
					if (currentEntry.getReference().equals("")) { //$NON-NLS-1$
						item.setText(Messages
								.getString("todoWindow.emptyReferenceFound") + sdf.format(currentEntry.getDate())); //$NON-NLS-1$
					}
					if (currentEntry.getReference().equalsIgnoreCase("<todo/>")) { //$NON-NLS-1$
						item.setText(Messages
								.getString("todoWindow.todoInsertReference") + sdf.format(currentEntry.getDate())); //$NON-NLS-1$
					}

					TableEditor editor = new TableEditor(table);
					Control oldEditor = editor.getEditor();
					if (oldEditor != null)
						oldEditor.dispose();

					final Link nativeLink = new Link(table, SWT.NONE);
					nativeLink.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(final SelectionEvent e) {
							accountingEditWindow win = new accountingEditWindow();
							win.open();
						}
					});
					nativeLink.setText(Messages
							.getString("todoWindow.editEntryLink")); //$NON-NLS-1$
					editor.grabHorizontal = true;
					editor.setEditor(nativeLink, item, 1);
					tableEditors.add(nativeLink);
				}

			}

		}

		return emptyOrTodoReferences;
	}

	public Shell getShell() {
		return super.getShell();
	}

	private static int checkUnbalancedCreditnotes(boolean addTableItems) {
		HashMap<Integer, String> unbalancedCreditNotes = client
				.getTransactions().getUnbalancedCreditNotes();

		if (addTableItems) {
			for (Integer unbalancedCreditNoteID : unbalancedCreditNotes
					.keySet()) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(Messages.getString("todoWindow.creditNote") + unbalancedCreditNotes.get(unbalancedCreditNoteID) + Messages.getString("todoWindow.notYetBalanced")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

				TableEditor editor = new TableEditor(table);
				Control oldEditor = editor.getEditor();
				if (oldEditor != null)
					oldEditor.dispose();

				Link nativeLink = new Link(table, SWT.NONE);
				nativeLink
						.addSelectionListener(new todoBalanceSelectionAdapter(
								unbalancedCreditNoteID, shell)); //$NON-NLS-1$
				nativeLink.setText(Messages
						.getString("todoWindow.bookPaymentLink")); //$NON-NLS-1$
				editor.grabHorizontal = true;
				editor.setEditor(nativeLink, item, 1);
				tableEditors.add(nativeLink);

			}
		}
		if (unbalancedCreditNotes != null) {
			return unbalancedCreditNotes.size();
		} else {
			return 0;
		}
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager
	 * 
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("todoWindow.menu")); //$NON-NLS-1$
		return menuManager;
	}

	/**
	 * Create the toolbar manager
	 * 
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager
	 * 
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		statusLineManager.setMessage(null, ""); //$NON-NLS-1$
		return statusLineManager;
	}

	/**
	 * Launch the application
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			todoWindow window = new todoWindow();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(application.getAppName());
	}

	/**
	 * Return the initial size of the window
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(600, 375);
	}

	@Override
	public boolean close() {
		MainWindow.signalTodoWindowOpen(false);
		// TODO Auto-generated method stub
		return super.close();
	}

}
