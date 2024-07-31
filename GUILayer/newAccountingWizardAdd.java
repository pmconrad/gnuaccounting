package GUILayer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import appLayer.client;
import appLayer.configs;
import appLayer.transactionCellModifier;
import appLayer.transactionLabelProvider;
import appLayer.taxRelated.taxList;
import appLayer.transactionRelated.appTransaction;
import appLayer.transactionRelated.receiptIncoming;

/* The code used for the table editor (Viewer, ContentProvider etc) is based on
 * http://www.eclipse.org/articles/Article-Table-viewer/table_viewer.html
 * We need
 *  1) an empty class (entryList) because otherwise we can not use tableViewer.setInput, which we obviously need
 *  2) a bookingLabelProvider (extends LabelProvider implements ITableLabelProvider)
 *  3) a bookingCellModifier implements ICellModifier
 *  4) the normal table columns objects
 *  5) to set up an ordinary tableViewer (TableViewer tableViewer= new TableViewer(table);)
 *  6) let the tableviewer know about the names of the columns: tableViewer.setColumnProperties(item.getColumnNames());
 *  7) the celleditors (		CellEditor[] editors = new CellEditor[new entry().getColumnNames().length];	editors[0] = new TextCellEditor(table);)
 *  8 a contentProvider
 *  9) finally, we put all the things together
 *  	tableViewer.setCellEditors(editors);
 tableViewer.setCellModifier(new bookingCellModifier(this));
 contentProvider=new bookingContentProvider();
 tableViewer.setContentProvider(contentProvider);
 allEntryList=new entryList();
 tableViewer.setInput(allEntryList); 
 tableViewer.setLabelProvider(new bookingLabelProvider());

 *  Notice: the table can afterwards be completely reloaded (e.g. when an item has been deleted) using  tableViewer.getTable().removeAll();	tableViewer.refresh();

 */

public class newAccountingWizardAdd extends WizardPage {

	private Button btnDropBook;
	private Button btnDropDelete;
	private Button btnDropMore;
	private Button btnDropStandard;

	private newAccountingWizard parentWizard;
	protected Table table;
	protected TableViewer tableViewer = null;
	private taxList taxes = client.getTaxes();
	private tableSorter tabSort;

	// this is the same tablesorter as for accountingEditWindow
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
			receiptIncoming ri1 = (receiptIncoming) e1;
			receiptIncoming ri2 = (receiptIncoming) e2;
			int rc = 0;
			switch (propertyIndex) {
			case 0:
				rc = ri1.getIssueDate().compareTo(ri2.getIssueDate());
				break;
			case 1:
				rc = ri1.getDefaultDescription().compareTo(
						ri2.getDefaultDescription());
				break;
			case 2:
				rc = ri1.getGrossValue().compareTo(ri2.getGrossValue());
				break;
			case 3:
				rc = ri1.getDefaultCreditAccount().getCode()
						.compareTo(ri2.getDefaultCreditAccount().getCode());
				break;
			case 4:
				rc = ri1.getDefaultDebitAccount().getCode()
						.compareTo(ri2.getDefaultDebitAccount().getCode());
				break;
			case 5:
				rc = ri1.getVAT().getDescription()
						.compareTo(ri2.getVAT().getDescription());
				break;
			case 6:
				rc = ri1.getDefaultReference().compareTo(
						ri2.getDefaultReference());
				break;
			case 7:
				rc = ri1.getDefaultComment().compareTo(ri2.getDefaultComment());
				break;		
			case 8:
				rc = ri1.getContact().getName().compareTo(ri2.getContact().getName());
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

	String prefix = "transaction://"; //$NON-NLS-1$

	/**
	 * Create the wizard
	 */
	public newAccountingWizardAdd(newAccountingWizard parentWizard,
			boolean doImport) {
		super(Messages.getString("newAccountingWizardAdd.bookkeeping")); //$NON-NLS-1$
		this.parentWizard = parentWizard;
		setTitle(Messages
				.getString("newAccountingWizardAdd.ImportQueuePageTitle")); //$NON-NLS-1$
		setDescription(Messages
				.getString("newAccountingWizardAdd.addSelectedEntries")); //$NON-NLS-1$
		taxes.setEmptyElementName(Messages
				.getString("newAccountingWizardAdd.NoVAT")); //$NON-NLS-1$
		taxes.getTaxesFromDatabase();

	}

	// we seem need this empty class because otherwise we can not use
	// tableViewer.setInput
	// ... anybody knows why? (jstaerk@usegroup.de)
	private class entryList {
		public entryList() {

		}

	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			refreshTable();
		}
		super.setVisible(visible);
	}

	public void refreshTable() {
		/*
		 * we had to create and show the (empty) table in createcontrol (wizard
		 * instantiation), now the entries to be imported are there (previous
		 * wizard page done), so reload the table
		 */
		tableViewer.getTable().removeAll();
		tableViewer.refresh();

	}

	private void addToDeleteQueue(receiptIncoming en) {
		parentWizard.getDeleteVector().add(en);
		btnDropDelete.setText(Integer.toString(parentWizard.getDeleteVector()
				.size()));
		client.getImportQueue().remove(en); // if the user clicked on the button
											// instead of drag&dropping items on
											// it we need to manually remove it
											// from the tableviewer here,
											// otherwise it's already removed on
											// dragstart
		tableViewer.refresh();
		client.getImportQueue().removeImportID(en.getImportID());
	}

	private void addToMoreQueue(receiptIncoming en) {
		parentWizard.getMoreVector().add(en);
		btnDropMore.setText(Integer.toString(parentWizard.getMoreVector()
				.size()));
		client.getImportQueue().remove(en); // if the user clicked on the button
											// instead of drag&dropping items on
											// it we need to manually remove it
											// from the tableviewer here,
											// otherwise it's already removed on
											// dragstart
		tableViewer.refresh();
		client.getImportQueue().removeImportID(en.getImportID());
	}

	private void addToBookQueue(receiptIncoming en) {
		parentWizard.getBookVector().add(en);
		btnDropBook.setText(Integer.toString(parentWizard.getBookVector()
				.size()));
		client.getImportQueue().remove(en); // if the user clicked on the button
											// instead of drag&dropping items on
											// it we need to manually remove it
											// from the tableviewer here,
											// otherwise it's already removed on
											// dragstart
		tableViewer.refresh();
		client.getImportQueue().removeImportID(en.getImportID());
	}

	private void addToStandardQueue(receiptIncoming en) {
		parentWizard.getBookStandardVector().add(en);
		btnDropStandard.setText(Integer.toString(parentWizard
				.getBookStandardVector().size()));
		client.getImportQueue().remove(en); // if the user clicked on the button
											// instead of drag&dropping items on
											// it we need to manually remove it
											// from the tableviewer here,
											// otherwise it's already removed on
											// dragstart
		tableViewer.refresh();
		client.getImportQueue().removeImportID(en.getImportID());
	}

	public void warnIncompleteReference() {
		MessageDialog
				.openError(
						getShell(),
						Messages.getString("accountingWizard.errorCaptionBookingErrors"), Messages.getString("accountingWizard.errorMissingBookingAttributes")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void warnIncompleteContraAccount() {
		MessageDialog
				.openError(
						getShell(),
						Messages.getString("accountingWizard.errorCaptionBookingErrors"), Messages.getString("newAccountingWizardAdd.errorMissingContraAccount")); //$NON-NLS-1$ //$NON-NLS-2$ 
	}

	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());
		//
		setControl(container);

		final ScrolledComposite scrolledComposite = new ScrolledComposite(
				container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		final FormData formData_2 = new FormData();
		formData_2.bottom = new FormAttachment(100, -75);
		formData_2.top = new FormAttachment(0, 5);
		formData_2.right = new FormAttachment(100, -5);
		formData_2.left = new FormAttachment(0, 5);
		scrolledComposite.setLayoutData(formData_2);
		// scrolledComposite.setLayoutDeferred(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);

		table = new Table(scrolledComposite, SWT.FULL_SELECTION | SWT.MULTI
				| SWT.BORDER);

		table.setLocation(0, 0);
		table.setSize(468, 118);
		scrolledComposite.setContent(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		int colIndex = 0;

		for (String columnName : appTransaction.getColumnNames()) {
			final TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(100);
			column.setText(columnName);
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

		tableViewer.setColumnProperties(appTransaction.getColumnNames());
		CellEditor[] editors = new CellEditor[appTransaction.getColumnNames().length];

		editors[0] = new DateCellEditor(table);
		editors[1] = new TextCellEditor(table);
		editors[2] = new TextCellEditor(table);
		/*ComboBoxViewerCellEditor accountEditor = new ComboBoxViewerCellEditor(
				table, SWT.READ_ONLY);// account
		accountEditor.setContentProvider(ArrayContentProvider.getInstance());
		accountEditor.setInput(client.getAccounts().getCurrentChart()
				.getAccounts(false));
		
		editors[3] = accountEditor;*/
		editors[3] = new AccountTextCellEditor(table);
/*
		ComboBoxViewerCellEditor contraAccountEditor = new ComboBoxViewerCellEditor(
				table, SWT.READ_ONLY);// account
		contraAccountEditor.setContentProvider(ArrayContentProvider
				.getInstance());
		contraAccountEditor.setInput(client.getAccounts().getCurrentChart()
				.getAccounts(false));

		editors[4] = contraAccountEditor;*/
		editors[4] = new AccountTextCellEditor(table);
		
		editors[5] = new ComboBoxCellEditor(table, taxes.getStringArray(),
				SWT.READ_ONLY);// VATs

		String[] unlinkedTransactionNumbers = client.getDocuments()
				.getUnlinkedNumbers();
		editors[6] = new CComboBoxCellEditor(table, unlinkedTransactionNumbers);// reference
																				// to
																				// document
		editors[7] = new TextCellEditor(table);// comment
		editors[8] = new ContactTextCellEditor(table);

		final DragSource tableDragSource = new DragSource(table, DND.DROP_MOVE);
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
					receiptIncoming currentEntry = (receiptIncoming) ti[i]
							.getData();
					data += prefix + currentEntry.getImportID() + ","; //$NON-NLS-1$ //$NON-NLS-2$

				}
				event.data = data;

			}
		});
		tableDragSource
				.setTransfer(new Transfer[] { TextTransfer.getInstance() });

		tableViewer.setCellEditors(editors);
		tabSort = new tableSorter();
		tableViewer.setSorter(tabSort);
		tableViewer.setCellModifier(new transactionCellModifier(tableViewer));
		tableViewer.setContentProvider(client.getImportQueue());
		entryList allEntryList = new entryList();
		tableViewer.setLabelProvider(new transactionLabelProvider(getShell()
				.getDisplay()));
		tableViewer.setInput(allEntryList);

		Group tableItemDropGroup;
		tableItemDropGroup = new Group(container, SWT.NONE);

		btnDropStandard = new Button(tableItemDropGroup, SWT.NONE);
		btnDropStandard.setBounds(91, 27, 162, 24);
		btnDropStandard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				for (Object currentObject : selection.toArray()) {
					receiptIncoming currentTransaction = (receiptIncoming) currentObject;
					addToStandardQueue(currentTransaction);
				}
			}
		});
		FormData fd_btnDropStandard = new FormData();
		btnDropStandard.setLayoutData(fd_btnDropStandard);
		btnDropStandard.setText(Messages
				.getString("newAccountingWizardAdd.btnDropStandard.text")); //$NON-NLS-1$
		btnDropStandard.setImage(new Image(getShell().getDisplay(), getClass()
				.getResourceAsStream(
						"/libs/sarxos_Simple_Folder_Documents_small.png"))); //$NON-NLS-1$

		btnDropStandard.setFont(configs.getDefaultFont());

		DropTarget txtStandardDropTarget = new DropTarget(btnDropStandard,
				DND.DROP_MOVE);
		txtStandardDropTarget.setTransfer(new Transfer[] { TextTransfer
				.getInstance() });
		txtStandardDropTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {

				String theTransactions = (String) event.data;
				String[] elems = theTransactions.split(","); //$NON-NLS-1$
				for (String elem : elems) {
					int prefixLen = prefix.length();
					int transImportID = Integer.valueOf(elem
							.substring(prefixLen));
					receiptIncoming trans = client.getImportQueue()
							.getTransactionForImportID(transImportID);
					
					if (!trans.hasEmptyReference()) {
						addToStandardQueue(trans);
					} else {
						warnIncompleteReference();

					}
				}
				tableViewer.getTable().removeAll();
				tableViewer.refresh();

			}

		});

		tableItemDropGroup.setBackground(new Color(getShell().getDisplay(),
				191, 191, 191));

		tableItemDropGroup.setText(Messages
				.getString("newAccountingWizardAdd.tableItemDropZone")); //$NON-NLS-1$
		final FormData fd_tableItemDropGroup = new FormData();
		fd_tableItemDropGroup.top = new FormAttachment(100, -70);
		fd_tableItemDropGroup.bottom = new FormAttachment(100, -9);
		fd_tableItemDropGroup.left = new FormAttachment(100, -489);
		fd_tableItemDropGroup.right = new FormAttachment(scrolledComposite, 0,
				SWT.RIGHT);
		tableItemDropGroup.setLayoutData(fd_tableItemDropGroup);

		btnDropBook = new Button(tableItemDropGroup, SWT.NONE);
		btnDropBook.setBounds(0, 27, 89, 24);
		btnDropBook.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				for (Object currentObject : selection.toArray()) {
					receiptIncoming trans = (receiptIncoming) currentObject;
					addToBookQueue(trans);
				}
			}
		});
		fd_btnDropStandard.top = new FormAttachment(btnDropBook, 0, SWT.TOP);
		fd_btnDropStandard.right = new FormAttachment(btnDropBook, 60);
		final FormData fd_btnDropBook = new FormData();
		fd_btnDropBook.top = new FormAttachment(0, 80);
		fd_btnDropBook.bottom = new FormAttachment(100, 80);
		fd_btnDropBook.left = new FormAttachment(0, 176);
		btnDropBook.setLayoutData(fd_btnDropBook);
		btnDropBook.setImage(new Image(getShell().getDisplay(), getClass()
				.getResourceAsStream(
						"/libs/sarxos_Simple_Folder_Documents_small.png"))); //$NON-NLS-1$
		btnDropBook.setFont(configs.getDefaultFont());
		btnDropBook.setText(Messages
				.getString("newAccountingWizardAdd.bookDropTargetCaption")); //$NON-NLS-1$
		final DropTarget txtBookDropTarget = new DropTarget(btnDropBook,
				DND.DROP_MOVE);

		txtBookDropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(final DropTargetEvent event) {

				String theEntries = (String) event.data;
				String[] elems = theEntries.split(","); //$NON-NLS-1$
				for (String elem : elems) {
					int prefixLen = prefix.length();
					int importID = Integer.valueOf(elem.substring(prefixLen));
					receiptIncoming trans = client.getImportQueue()
							.getTransactionForImportID(importID);
					if (trans.hasEmptyReference()) {
						warnIncompleteReference();
					} else if (trans.hasMissingAccounts()) {
						warnIncompleteContraAccount();
					} else {
						addToBookQueue(trans);
					}
				}
				tableViewer.getTable().removeAll();
				tableViewer.refresh();

			}
		});
		txtBookDropTarget.setTransfer(new Transfer[] { TextTransfer
				.getInstance() });

		btnDropMore = new Button(tableItemDropGroup, SWT.NONE);
		btnDropMore.setBounds(259, 27, 89, 24);
		btnDropMore.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				for (Object currentObject : selection.toArray()) {
					receiptIncoming currentEntry = (receiptIncoming) currentObject;
					addToMoreQueue(currentEntry);
				}
			}
		});
		fd_btnDropBook.right = new FormAttachment(btnDropMore, -6);
		final FormData fd_btnDropMore = new FormData();
		fd_btnDropMore.bottom = new FormAttachment(100, 80);
		fd_btnDropMore.top = new FormAttachment(0, 60);
		fd_btnDropMore.left = new FormAttachment(0, 260);
		btnDropMore.setLayoutData(fd_btnDropMore);
		btnDropMore.setImage(new Image(getShell().getDisplay(), getClass()
				.getResourceAsStream(
						"/libs/sarxos_Simple_Folder_Documents_small.png"))); //$NON-NLS-1$
		btnDropMore.setFont(configs.getDefaultFont());
		btnDropMore.setText(Messages
				.getString("newAccountingWizardAdd.moreDropTargetCaption")); //$NON-NLS-1$

		final DropTarget txtMoreDropTarget = new DropTarget(btnDropMore,
				DND.DROP_NONE);
		txtMoreDropTarget.setTransfer(new Transfer[] { TextTransfer
				.getInstance() });
		txtMoreDropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(final DropTargetEvent event) {

				String theEntries = (String) event.data;
				String[] elems = theEntries.split(","); //$NON-NLS-1$
				for (String elem : elems) {
					int prefixLen = prefix.length();
					int entryImportID = Integer.valueOf(elem
							.substring(prefixLen));
					receiptIncoming en = client.getImportQueue()
							.getTransactionForImportID(entryImportID);
					addToMoreQueue(en);
				}
				tableViewer.getTable().removeAll();
				tableViewer.refresh();

			}

		});

		btnDropDelete = new Button(tableItemDropGroup, SWT.NONE);
		btnDropDelete.setBounds(385, 27, 89, 24);
		fd_btnDropMore.right = new FormAttachment(btnDropDelete, -6);
		btnDropDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				for (Object currentObject : selection.toArray()) {
					receiptIncoming currentEntry = (receiptIncoming) currentObject;
					addToDeleteQueue(currentEntry);
				}
			}
		});
		final FormData fd_btnDropDelete = new FormData();
		fd_btnDropDelete.bottom = new FormAttachment(100, 80);
		fd_btnDropDelete.top = new FormAttachment(0, 6);
		fd_btnDropDelete.left = new FormAttachment(0, 341);
		fd_btnDropDelete.right = new FormAttachment(100, 12);
		btnDropDelete.setLayoutData(fd_btnDropDelete);
		btnDropDelete.setImage(new Image(getShell().getDisplay(), getClass()
				.getResourceAsStream(
						"/libs/sarxos_Simple_Folder_Documents_small.png"))); //$NON-NLS-1$
		btnDropDelete.setFont(configs.getDefaultFont());
		btnDropDelete.setText(Messages
				.getString("newAccountingWizardAdd.deleteDropTargetCaption")); //$NON-NLS-1$

		final DropTarget txtDeleteDropTarget = new DropTarget(btnDropDelete,
				DND.DROP_NONE);
		txtDeleteDropTarget.setTransfer(new Transfer[] { TextTransfer
				.getInstance() });
		txtDeleteDropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(final DropTargetEvent event) {

				String theEntries = (String) event.data;
				String[] elems = theEntries.split(","); //$NON-NLS-1$
				for (String elem : elems) {
					int prefixLen = prefix.length();
					int entryImportID = Integer.valueOf(elem
							.substring(prefixLen));
					receiptIncoming en = client.getImportQueue()
							.getTransactionForImportID(entryImportID);
					addToDeleteQueue(en);

				}
				tableViewer.getTable().removeAll();
				tableViewer.refresh();

			}

		});

	}

	@Override
	public IWizardPage getNextPage() {
		parentWizard.performImport();
		return super.getNextPage();
	}
}
