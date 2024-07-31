package GUILayer;

import java.util.Vector;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import appLayer.AccountNotFoundException;
import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.entry;
import appLayer.entryCellModifier;
import appLayer.entryLabelProvider;
import appLayer.transactionRelated.appTransaction;
import appLayer.transactionRelated.receiptIncoming;

public class accountingEditWindow extends ApplicationWindow {

	private class tableSorter extends ViewerSorter {
		private int propertyIndex;
		// private static final int ASCENDING = 0;
		private static final int DESCENDING = 1;

		private int direction = DESCENDING;

		public tableSorter() {
			this.propertyIndex = 0;
			direction = DESCENDING;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = DESCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			entry en1 = (entry) e1;
			entry en2 = (entry) e2;
			int rc = 0;
			switch (propertyIndex) {
			case 0:
				rc = en1.getDate().compareTo(en2.getDate());
				break;
			case 1:
				rc = en1.getDescription().compareTo(en2.getDescription());
				break;
			case 2:
				rc = en1.getValue().compareTo(en2.getValue());
				break;
			case 3:
				rc = en1.getDebitAccount().getCode()
						.compareTo(en2.getDebitAccount().getCode());
				break;
			case 4:
				rc = en1.getCreditAccount().getCode()
						.compareTo(en2.getCreditAccount().getCode());
				break;
			case 5:
				rc = en1.getReference().compareTo(en2.getReference());
				break;
			case 6:
				rc = en1.getComment().compareTo(en2.getComment());
				break;
			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}

	}

	private Button btnDelete;
	protected static TableViewer tableViewer = null;
	private static entryList allEntryList = null;
	String prefix = "entry://"; //$NON-NLS-1$

	private Vector<entry> toDelete = new Vector<entry>();

	private class entryList {
		public entryList() {

		}

	}

	private Table table;
	private tableSorter tabSort;
	private static boolean isOpen = false;

	/**
	 * Create the application window
	 */
	public accountingEditWindow() {
		super(null);

		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	public static void refreshAccountingEntries() {
		if (isOpen) {

			/*
			 * tableviewer and contentprovider can be null if invoked via an
			 * accounting import wizard from the main window menu bar
			 */
			if (tableViewer != null) {
				tableViewer.getTable().removeAll();
			}
			if (tableViewer != null) {
				tableViewer.refresh();
			}
		}
	}

	private void addToDeleteQueue(entry en) {
		toDelete.add(en);
		btnDelete.setText(Integer.toString(toDelete.size()));
		client.getEntries().removeEntry(en);
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
		container.setLayout(new FormLayout());

		final ScrolledComposite scrolledComposite = new ScrolledComposite(
				container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		final FormData fd_scrolledComposite = new FormData();
		fd_scrolledComposite.bottom = new FormAttachment(100, -66);
		fd_scrolledComposite.top = new FormAttachment(0, 34);
		fd_scrolledComposite.right = new FormAttachment(100, 0);
		fd_scrolledComposite.left = new FormAttachment(0, 5);
		scrolledComposite.setLayoutData(fd_scrolledComposite);

		table = new Table(scrolledComposite, SWT.FULL_SELECTION | SWT.MULTI
				| SWT.BORDER);
		table.setLocation(0, 0);
		table.setSize(483, 231);
		scrolledComposite.setContent(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final Link lnkImportQueue = new Link(container, SWT.NONE);
		lnkImportQueue.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newAccountingWizard wizard = new newAccountingWizard(false);
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				dialog.open();

			}
		});
		lnkImportQueue.setFont(configs.getDefaultFont());
		final FormData fd_lnkAdd = new FormData();
		lnkImportQueue.setLayoutData(fd_lnkAdd);

		lnkImportQueue
				.setText(Messages
						.getString("accountingEditWindow.addEntriesLink") + " (" + client.getImportQueue().size() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (client.getImportQueue().size() == 0) {
			lnkImportQueue.setEnabled(false);
		}
		// lnkAdd.setFont(configs.getDefaultFont());

		Link lnkImport;
		lnkImport = new Link(container, SWT.NONE);
		fd_lnkAdd.right = new FormAttachment(lnkImport, 120, SWT.RIGHT);
		fd_lnkAdd.left = new FormAttachment(lnkImport, 0, SWT.RIGHT);
		lnkImport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newAccountingWizard wizard = new newAccountingWizard(true);
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				dialog.open();

			}
		});
		final FormData fd_lnkImport = new FormData();
		fd_lnkImport.top = new FormAttachment(lnkImportQueue, -15, SWT.BOTTOM);
		fd_lnkImport.bottom = new FormAttachment(lnkImportQueue, 0, SWT.BOTTOM);
		fd_lnkImport.right = new FormAttachment(scrolledComposite, 115,
				SWT.LEFT);
		fd_lnkImport.left = new FormAttachment(scrolledComposite, 0, SWT.LEFT);
		lnkImport.setLayoutData(fd_lnkImport);
		lnkImport.setText(Messages
				.getString("accountingEditWindow.importEntriesLink")); //$NON-NLS-1$
		lnkImport.setFont(configs.getDefaultFont());
		//

		int colIndex = 0;
		for (String columnName : entry.getColumnNames()) {
			final TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(100);
			final int colIndexFinal = colIndex;
			column.setText(columnName);
			// Setting the right sorter
			column.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					tabSort.setColumn(colIndexFinal);
					int dir = tableViewer.getTable().getSortDirection();
					if (tableViewer.getTable().getSortColumn() == column) {
						dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
					} else {

						dir = SWT.DOWN;
					}
					tableViewer.getTable().setSortDirection(dir);
					tableViewer.getTable().setSortColumn(column);
					tableViewer.refresh();
				}
			});
			colIndex++;

		}

		tableViewer = new TableViewer(table);

		tableViewer.setColumnProperties(entry.getColumnNames());
		tabSort = new tableSorter();
		tableViewer.setSorter(tabSort);

		final DragSource tableDragSource = new DragSource(table, DND.DROP_NONE);
		tableDragSource.addDragListener(new DragSourceAdapter() {
			public void dragStart(final DragSourceEvent event) {
				if (table.getSelectionCount() > 0) {
					event.doit = true;
				}
			}

			public void dragSetData(final DragSourceEvent event) {
				TableItem[] ti = table.getSelection();
				String data = new String();
				for (int i = 0; i < table.getSelectionCount(); i++) {
					entry currentEntry = (entry) ti[i].getData();
					data += prefix + currentEntry.getID() + ","; //$NON-NLS-1$ //$NON-NLS-2$

				}
				event.data = data;

			}
		});
		tableDragSource
				.setTransfer(new Transfer[] { TextTransfer.getInstance() });

		CellEditor[] editors = new CellEditor[8];

		editors[0] = (CellEditor) new DateCellEditor(table);

		editors[1] = (CellEditor) new TextCellEditor(table);
		editors[2] = (CellEditor) new TextCellEditor(table);
/*		ComboBoxViewerCellEditor accountEditor = new ComboBoxViewerCellEditor(
				table, SWT.READ_ONLY);// account
		accountEditor.setContentProvider(ArrayContentProvider.getInstance());
		accountEditor.setInput(client.getAccounts().getCurrentChart()
				.getAccounts(false));
		editors[3] = accountEditor;
		ComboBoxViewerCellEditor contraAccountEditor = new ComboBoxViewerCellEditor(
				table, SWT.READ_ONLY);// contra account
		contraAccountEditor.setContentProvider(ArrayContentProvider
				.getInstance());
		contraAccountEditor.setInput(client.getAccounts().getCurrentChart()
				.getAccounts(false));
		editors[4] = contraAccountEditor;*/
		
		editors[3] = new AccountTextCellEditor(table);
		editors[4] = new AccountTextCellEditor(table);
		
		editors[5] = new CComboBoxCellEditor(table, client.getDocuments()
				.getUnlinkedNumbers()); // reference

		editors[6] = new TextCellEditor(table);// reference
		editors[7] = new TextCellEditor(table);// comment

		tableViewer.setCellEditors(editors);
		// tableViewer.set
		entryCellModifier bcm = new entryCellModifier(tableViewer);
		tableViewer.setCellModifier(bcm);
		tableViewer.setContentProvider(client.getEntries());
		allEntryList = new entryList();
		tableViewer.setInput(allEntryList);
		tableViewer.setLabelProvider(new entryLabelProvider(getShell()
				.getDisplay()));

		isOpen = true;
		refreshAccountingEntries();
		/*
		 * we had to create and show the (empty) table in createcontrol (wizard
		 * instantiation), now the entries to be imported are there (previous
		 * wizard page done), so reload the table
		 */
		tableViewer.getTable().removeAll();
		tableViewer.refresh();

		Link lnkExport;
		lnkExport = new Link(container, SWT.NONE);
		fd_lnkAdd.top = new FormAttachment(lnkExport, -15, SWT.BOTTOM);
		fd_lnkAdd.bottom = new FormAttachment(lnkExport, 0, SWT.BOTTOM);
		lnkExport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				reportWizard wizard = new reportWizard(
						reportWizard.format.formatODF);
				if (wizard.isOpenable()) {
					WizardDialog dialog = new WizardDialog(getShell(), wizard);
					dialog.open();
				}

			}
		});
		final FormData fd_lnkExport = new FormData();
		fd_lnkExport.left = new FormAttachment(0, 245);
		fd_lnkExport.top = new FormAttachment(scrolledComposite, -21, SWT.TOP);
		fd_lnkExport.bottom = new FormAttachment(scrolledComposite, -5, SWT.TOP);
		fd_lnkExport.right = new FormAttachment(0, 380);
		lnkExport.setLayoutData(fd_lnkExport);
		lnkExport.setText(Messages
				.getString("accountingEditWindow.exportEntriesLink")); //$NON-NLS-1$
		lnkExport.setFont(configs.getDefaultFont());

		final Button btnAdd = new Button(container, SWT.NONE);
		btnAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				
				
				receiptIncoming t = new receiptIncoming();
				try {
					t.setDefaultCreditAccount(client.getAccounts().getRevenuesAccount());
					t.setDefaultDebitAccount(client.getAccounts().getCashAccount());
				} catch (AccountNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				t.setType(client.getTransactions().getTypeByTypeID(
						receiptIncoming.getType()));

				Vector<appTransaction> v = new Vector<appTransaction>();
				v.add(t);
				transactionDetailWindow tdw=new transactionDetailWindow(v);
				tdw.autoUpdateGrossAmount(true);
				
				tdw.open();
			}
		});
		final FormData fd_btnAdd = new FormData();
		fd_btnAdd.left = new FormAttachment(100, -85);
		fd_btnAdd.right = new FormAttachment(100, -11);
		fd_btnAdd.top = new FormAttachment(scrolledComposite, -29, SWT.TOP);
		fd_btnAdd.bottom = new FormAttachment(scrolledComposite, 0, SWT.TOP);
		btnAdd.setLayoutData(fd_btnAdd);
		btnAdd.setFont(configs.getDefaultFont());
		btnAdd.setText(Messages
				.getString("accountingEditWindow.addEntryButton")); //$NON-NLS-1$

		final Group tableitemDropZoneGroup = new Group(container, SWT.NONE);
		tableitemDropZoneGroup.setBackground(new Color(getShell().getDisplay(),
				191, 191, 191));
		tableitemDropZoneGroup.setText(Messages
				.getString("accountingEditWindow.tableItemDropZone")); //$NON-NLS-1$
		final FormData fd_tableitemDropZoneGroup = new FormData();
		fd_tableitemDropZoneGroup.right = new FormAttachment(btnAdd, 0,
				SWT.RIGHT);
		fd_tableitemDropZoneGroup.left = new FormAttachment(100, -173);
		fd_tableitemDropZoneGroup.top = new FormAttachment(scrolledComposite,
				5, SWT.BOTTOM);
		tableitemDropZoneGroup.setLayoutData(fd_tableitemDropZoneGroup);
		tableitemDropZoneGroup.setLayout(new FormLayout());

		btnDelete = new Button(tableitemDropZoneGroup, SWT.NONE);
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				for (Object currentObject : selection.toArray()) {
					entry currentEntry = (entry) currentObject;
					addToDeleteQueue(currentEntry);
				}
				tableViewer.getTable().removeAll();
				tableViewer.refresh();

			}
		});
		final FormData fd_btnDropDelete = new FormData();
		fd_btnDropDelete.left = new FormAttachment(0, 15);
		fd_btnDropDelete.right = new FormAttachment(0, 140);
		btnDelete.setLayoutData(fd_btnDropDelete);
		btnDelete.setImage(new Image(getShell().getDisplay(), getClass()
				.getResourceAsStream(
						"/libs/sarxos_Simple_Folder_Documents_small.png"))); //$NON-NLS-1$
		btnDelete.setFont(configs.getDefaultFont());
		btnDelete.setText(Messages
				.getString("accountingEditWindow.deleteDropTargetText")); //$NON-NLS-1$

		final DropTarget deleteTextDropTarget = new DropTarget(btnDelete,
				DND.DROP_MOVE);
		deleteTextDropTarget.setTransfer(new Transfer[] { TextTransfer
				.getInstance() });
		deleteTextDropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(final DropTargetEvent event) {

				String theEntries = (String) event.data;
				String[] elems = theEntries.split(","); //$NON-NLS-1$
				for (String elem : elems) {
					int prefixLen = prefix.length();
					int entryImportID = Integer.valueOf(elem
							.substring(prefixLen));

					entry en = client.getEntries().getEntryForID(entryImportID);

					addToDeleteQueue(en);

				}
				tableViewer.getTable().removeAll();
				tableViewer.refresh();

			}

		});

		return container;
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
				Messages.getString("accountingEditWindow.menu")); //$NON-NLS-1$
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
			accountingEditWindow window = new accountingEditWindow();
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
		return new Point(500, 421);
	}

	@Override
	public boolean close() {
		isOpen = false;
		for (entry currententry : toDelete) {
			currententry.delete();
			client.getEntries().removeEntry(currententry);

		}
		return super.close();
	}
}
