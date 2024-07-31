package GUILayer;

import java.util.Vector;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.noa.text.TextRangeSelection;
import appLayer.IbarcodeListener;
import appLayer.client;
import appLayer.configs;
import appLayer.item;
import appLayer.product;

public class newTransactionSelectItemDetails extends WizardPage {

	private Table table;

	private Button btnDropDelete;
	private entryList allEntryList = null;
	private TableViewer tableViewer = null;
	private Vector<item> toDelete = null;
	protected item activeItem;
	private newTransactionWizard parentWizard;

	private class entryList {
		public entryList() {

		}

	}
		

	private class transactionCellModifier implements ICellModifier {

		private ComboBoxViewerCellEditor productViewerEditor; 
		public transactionCellModifier(ComboBoxViewerCellEditor productViewerEditor) {
			this.productViewerEditor=productViewerEditor;
		}

		public boolean canModify(Object element, String property) {

			return !property.equals("Total"); // everything is editable except total which is qty*price //$NON-NLS-1$
		}

		public void modify(Object element, String property, Object value) {
			TableItem item = (TableItem) element;
			item currentItem = (item) item.getData();
			int propertyCol = -1;
			try {
				propertyCol = appLayer.utils.findIndexOfStringInStringArray(
						appLayer.item.getColumnNames(), property);
			} catch (appLayer.elementNotFoundException e) {
				e.printStackTrace();
			}
			if (currentItem.getColumnType(propertyCol) == 1) {// not account
																// which is a
																// combobox
				currentItem.setColumn(propertyCol, (String) value);
			} else if (currentItem.getColumnType(propertyCol) == 2) { // products
																		// combo
																		// box
				if (value==null) { // no valid product name
					//
					CCombo combo=(CCombo)productViewerEditor.getControl();
					value=combo.getText(); // propagate a String instead of a Product to setColumn
				}

				currentItem.setColumn(propertyCol, value);
			} else {
				try {
					throw new Exception(
							Messages.getString("newTransactionSelectItemDetails.columnTypeException") + Integer.toString(currentItem.getColumnType(propertyCol))); //$NON-NLS-1$
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			tableViewer.refresh();
		}

		public Object getValue(Object element, String property) {
			item currentEntry = (item) element;
			int propertyCol = -1;
			try {
				propertyCol = appLayer.utils.findIndexOfStringInStringArray(
						appLayer.item.getColumnNames(), property);
			} catch (appLayer.elementNotFoundException e) {
				e.printStackTrace();
			}
			return currentEntry.getColumn(propertyCol);
		}

	}

	private class transactionLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}

		public String getColumnText(Object arg0, int arg1) {
			// return null;
			item e = (item) arg0;

			return e.getColumnString(arg1);
		}

	}

	// ////////////////////////
	// real class newTransactionSelectItemDetails

	public newTransactionSelectItemDetails(newTransactionWizard parent) {
		super(Messages.getString("newTransactionSelectItemDetails.Transaction")); //$NON-NLS-1$
		setTitle(Messages
				.getString("newTransactionSelectItemDetails.newTransaction")); //$NON-NLS-1$
		setDescription(Messages
				.getString("newTransactionSelectItemDetails.selectTransactionDetails")); //$NON-NLS-1$
		checkPageComplete();
		toDelete = new Vector<item>();
		parentWizard = parent;
	}

	private void addToDeleteQueue(item i) {
		toDelete.add(i);
		btnDropDelete.setText(Integer.toString(toDelete.size()));
		client.getTransactions().getCurrentTransaction()
				.removeIDonPage(i.getIDonPage());
		tableViewer.refresh();
	}

	public void checkPageComplete() {
		if (client.getTransactions().getCurrentTransaction().getItems() == null) {
			setPageComplete(false);
			return;
		} else if (client.getTransactions().getCurrentTransaction().getItems()
				.size() == 0) {
			setPageComplete(false);
			setErrorMessage(Messages
					.getString("newTransactionSelectItemDetails.addItems")); //$NON-NLS-1$
			return;
		} else {

			setErrorMessage(null);
			setPageComplete(true);

		}

	}

	private void addItem() {
		item newItem = client.getTransactions().getCurrentTransaction()
				.addItem();
		activeItem = newItem;
		tableViewer.getTable().removeAll();
		tableViewer.refresh();
		checkPageComplete();
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());
		//
		setControl(container);
		/**
		 * it is possible that a transaction was loaded, in this case it was !=
		 * null. Otherwise, construct the default transaction
		 */

		final ScrolledComposite scrolledComposite = new ScrolledComposite(
				container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		final FormData formData_2 = new FormData();
		formData_2.top = new FormAttachment(0, 40);
		formData_2.bottom = new FormAttachment(100, -68);
		formData_2.right = new FormAttachment(100, -5);
		formData_2.left = new FormAttachment(0, 5);
		scrolledComposite.setLayoutData(formData_2);

		tableViewer = new TableViewer(scrolledComposite, SWT.FULL_SELECTION
				| SWT.MULTI);
		table = tableViewer.getTable();
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				TableItem item = table.getItem(new Point(e.x, e.y));
				if (item == null) {
					// add an item when user clicks on empty space in table
					// first we check if already an item without selected
					// product exists
					// if yes, we focus (set selection) on that, otherwise we
					// add an item
					boolean createNewItem = true;
					int visibleItemIndex = 0;
					for (int itemIndex = 0; itemIndex < client
							.getTransactions().getCurrentTransaction()
							.getItems().size(); itemIndex++) {
						item currentItem = client.getTransactions()
								.getCurrentTransaction().getItems()
								.get(itemIndex);
						if ((currentItem != null) && (toDelete != null)
								&& (!toDelete.contains(currentItem))
								&& (currentItem.isEmpty())) {
							// isEmpty=the <please select> product is selected
							createNewItem = false;
							TableItem itemToBeSelected = tableViewer.getTable()
									.getItem(visibleItemIndex);
							tableViewer.setSelection(new StructuredSelection(
									itemToBeSelected.getData()));
						}
						if (!toDelete.contains(currentItem)) {
							visibleItemIndex++;
						}
					}
					if (createNewItem) {
						addItem();

					}
				}
			}
		});
		table.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent arg0) {
				/*
				 * this empty function will catch any CR which may happen e.g.
				 * when scanning barcodes
				 */
			}
		});
		table.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// when barcodes are scanned we need to know which product to
				// change
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				item selectedItem = (item) selection.getFirstElement();

				activeItem = selectedItem;
			}
		});
		CellEditor[] editors = new CellEditor[item.getColumnNames().length];
		editors[0] = new TextCellEditor(table);// qty
		// editors[1] = new ComboBoxCellEditor(table,
		// allProducts.getStringArray(), SWT.READ_ONLY);//article
		ComboBoxViewerCellEditor cbv = new ComboBoxViewerCellEditor(table);
		cbv.setContentProvider(new ArrayContentProvider());
		cbv.setInput(client.getProducts().getProductsWithoutNew());
		
		editors[1] = cbv;

		editors[2] = new TextCellEditor(table);// remarks
		editors[3] = new TextCellEditor(table);// price
		editors[4] = new TextCellEditor(table);// total
		tableViewer.setCellEditors(editors);

		tableViewer.setColumnProperties(item.getColumnNames());

		tableViewer.setCellModifier(new transactionCellModifier(cbv));
		tableViewer.setContentProvider(client.getTransactions()
				.getCurrentTransaction());
		allEntryList = new entryList();
		tableViewer.setInput(allEntryList);
		tableViewer.setLabelProvider(new transactionLabelProvider());

		client.getTransactions().getCurrentTransaction()
				.bindTableViewer(tableViewer);
		client.getTransactions().getCurrentTransaction().bindToPage(this);

		table.setSize(472, 154);
		scrolledComposite.setContent(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn newColumnTableColumn = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn.setWidth(100);
		newColumnTableColumn.setText(Messages
				.getString("newTransactionSelectItemDetails.quantityAbbrev")); //$NON-NLS-1$

		final TableColumn newColumnTableColumn_1 = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn_1.setWidth(100);
		newColumnTableColumn_1.setText(Messages
				.getString("newTransactionSelectItemDetails.article")); //$NON-NLS-1$

		final TableColumn newColumnTableColumn_4 = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn_4.setWidth(100);
		newColumnTableColumn_4.setText(Messages
				.getString("newTransactionSelectItemDetails.remarks")); //$NON-NLS-1$

		final TableColumn newColumnTableColumn_2 = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn_2.setWidth(100);
		newColumnTableColumn_2.setText(Messages
				.getString("newTransactionSelectItemDetails.price")); //$NON-NLS-1$

		final TableColumn newColumnTableColumn_3 = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn_3.setWidth(100);
		newColumnTableColumn_3.setText(Messages
				.getString("newTransactionSelectItemDetails.total")); //$NON-NLS-1$

		globalFilterKeyListener.addListener(new IbarcodeListener() {

			@Override
			public void barcodeReceived(String barcode, product attachedProduct) {
				if (activeItem != null) {
					activeItem.setProduct(attachedProduct);
				}
			}
		});

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
					item currentItem = (item) ti[i].getData();
					data += "item://" + currentItem.getIDonPage() + ","; //$NON-NLS-1$ //$NON-NLS-2$
				}
				event.data = data;
			}
		});
		tableDragSource
				.setTransfer(new Transfer[] { TextTransfer.getInstance() });

		final FormData formData_3 = new FormData();
		formData_3.left = new FormAttachment(100, -174);
		formData_3.top = new FormAttachment(scrolledComposite, -35, SWT.TOP);
		formData_3.bottom = new FormAttachment(scrolledComposite, -5, SWT.TOP);
		formData_3.right = new FormAttachment(100, -61);

		Button addArticleButton;
		addArticleButton = new Button(container, SWT.NONE);
		addArticleButton.setLayoutData(formData_3);
		addArticleButton.setText(Messages
				.getString("newTransactionSelectItemDetails.addItem")); //$NON-NLS-1$
		addArticleButton.setFont(configs.getDefaultFont());
		addArticleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				addItem();
			}

		});
		addArticleButton.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent arg0) {
				/*
				 * this empty function will catch any CR which may happen e.g.
				 * when scanning barcodes
				 */
			}
		});

		// in case a transaction was loaded (e.g. a invoice was loaded from a
		// offer) the next line is helpful...
		tableViewer.refresh();

		final Group tableItemDropGroup = new Group(container, SWT.NONE);
		tableItemDropGroup
				.setText(Messages
						.getString("newTransactionSelectItemDetails.tableItemDropZoneCaption")); //$NON-NLS-1$
		tableItemDropGroup.setBackground(new Color(getShell().getDisplay(),
				191, 191, 191));

		final FormData fd_tableItemDropGroup = new FormData();
		fd_tableItemDropGroup.left = new FormAttachment(100, -180);
		fd_tableItemDropGroup.right = new FormAttachment(100, -37);
		fd_tableItemDropGroup.bottom = new FormAttachment(100, -5);
		fd_tableItemDropGroup.top = new FormAttachment(scrolledComposite, 5,
				SWT.BOTTOM);
		tableItemDropGroup.setLayoutData(fd_tableItemDropGroup);
		tableItemDropGroup.setLayout(new FormLayout());
		btnDropDelete = new Button(tableItemDropGroup, SWT.NONE);
		btnDropDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				for (Object currentObject : selection.toArray()) {
					item currentItem = (item) currentObject;
					addToDeleteQueue(currentItem);
				}

			}
		});
		final FormData fd_btnDelete = new FormData();
		fd_btnDelete.right = new FormAttachment(0, 116);
		fd_btnDelete.top = new FormAttachment(0, 5);
		fd_btnDelete.left = new FormAttachment(0, 22);
		btnDropDelete.setLayoutData(fd_btnDelete);
		btnDropDelete
				.setText(Messages
						.getString("newTransactionSelectItemDetails.deleteButtonCaption")); //$NON-NLS-1$
		btnDropDelete.setImage(new Image(getShell().getDisplay(), getClass()
				.getResourceAsStream(
						"/libs/sarxos_Simple_Folder_Documents_small.png"))); //$NON-NLS-1$
		btnDropDelete.setFont(configs.getDefaultFont());
		final DropTarget btnDeleteDropTarget = new DropTarget(btnDropDelete,
				DND.DROP_NONE);
		btnDeleteDropTarget.setTransfer(new Transfer[] { TextTransfer
				.getInstance() });
		btnDeleteDropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(final DropTargetEvent event) {

				String theEntries = (String) event.data;
				String[] elems = theEntries.split(","); //$NON-NLS-1$
				for (String elem : elems) {
					String prefix = "item://"; //$NON-NLS-1$
					int prefixLen = prefix.length();
					int itemID = Integer.valueOf(elem.substring(prefixLen));
					item i = client.getTransactions().getCurrentTransaction()
							.getItemForIDonPage(itemID);

					addToDeleteQueue(i);

				}
				checkPageComplete();// possibly the user just deleted the last
									// entry w/o product
				tableViewer.getTable().removeAll();
				tableViewer.refresh();

			}

		});

	}

	@Override
	public String getName() {
		return "selectItems"; //$NON-NLS-1$
	}

	@Override
	public void setVisible(boolean visible) {
		try {
			// set selection somewhere outside of the table to make table
			// toolbar disapear if selection was put to table by user
			ITextDocument document = parentWizard.getDocument();
			if (document != null)
				document.setSelection(new TextRangeSelection(document
						.getTextService().getText().getTextCursorService()
						.getTextCursor().getStart()));
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (true) {
			// this is the second page -- as soon as it's visible the
			// transaction has to be dirty
			client.getTransactions().getCurrentTransaction().setDirty(true);

			tableViewer.setContentProvider(client.getTransactions()
					.getCurrentTransaction());
			tableViewer.refresh();
			checkPageComplete();
		}
		super.setVisible(visible);
	}

}
