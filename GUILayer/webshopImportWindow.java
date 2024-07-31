/**
 * Graphical user interface to import order from a web shop using OBDX
 *  
 * @author Gerd Bartelt
 */

package GUILayer;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.transactionRelated.appTransaction;
import appLayer.transactionRelated.offer;
import dataLayer.webshopImporter;

public class webshopImportWindow extends ApplicationWindow {

	private Browser browser;
	private Table table;
	private webshopImporter webshopimporter;

	/**
	 * Create the window
	 */
	public webshopImportWindow() {
		super(null);
		if (configs.getWebShopURL().length() == 0) {
			MessageDialog
					.openError(
							getShell(),
							Messages.getString("webshopImportWindow.notConfiguredHeading"), Messages.getString("webshopImportWindow.setWebShopURL")); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (configs.getWebShopUser().length() == 0) {
			MessageDialog
					.openError(
							getShell(),
							Messages.getString("webshopImportWindow.notConfiguredHeading"), Messages.getString("webshopImportWindow.setWebshopUser")); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (configs.getWebShopPassword().length() == 0) {
			MessageDialog
					.openError(
							getShell(),
							Messages.getString("webshopImportWindow.notConfiguredHeading"), Messages.getString("webshopImportWindow.setWebshopPassword")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Synchronize the data with the web shop.
	 */
	public void syncWithWebshop() {
		try {
			table.removeAll();
			browser.setText(""); //$NON-NLS-1$
			new ProgressMonitorDialog(getShell()).run(true, true,
					webshopimporter);
			webshopimporter.fillTableWithOrderInformation(table);
			browser.setText(webshopimporter.getRunResult());
		} catch (InvocationTargetException ite) {
			ite.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

	}

	/**
	 * Generate a invoice from a specified order ID. Show a warning, if the
	 * state is not "pending"
	 * 
	 * @param order_id
	 *            order ID to export
	 */
	public void checkAndGenerateInvoiceFromOrderID(String order_id) {
		offer selectedOffer = null;
		for (appTransaction currentStatus : client.getTransactions()
				.getTransactions()) {
			if (currentStatus.getTypeID() == offer.getType()) {
				offer current = (offer) currentStatus;

				if (current.getOrderID().equalsIgnoreCase(order_id)) {
					selectedOffer = current;
				}
			}
		}

		boolean bgeninvoice = true;
		// test if the order is in state pending
		if (selectedOffer.isPending()) {
			// change state from pending to processing
			// webshopimporter.setOrderStatusById(order_id,orderstatus);
			webshopimporter.setTableStatus(table, order_id, 2);
			// display modified order
			browser.setText(webshopimporter.getHTMLPreview(order_id));
		}
		// show a warning, if the order is not in state pending
		else {
			MessageBox messageBox = new MessageBox(getShell(),
					SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			messageBox.setText(Messages
					.getString("webshopImportWindow.Warning")); //$NON-NLS-1$
			messageBox
					.setMessage(Messages
							.getString("webshopImportWindow.This_order_has_the_status") + " \n\"" + selectedOffer.getStatusString() + "\".\n" + Messages.getString("webshopImportWindow.Want_to_create_an_invoice")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			int response = messageBox.open();
			if (response != SWT.YES)
				bgeninvoice = false;
		}
		if (bgeninvoice) {
			webshopimporter.generateInvoiceFromOrderID(order_id);
		}
	}

	/**
	 * Create contents of the window
	 * 
	 * @param parent
	 *            Parent composite
	 */
	@Override
	protected Control createContents(Composite parent) {

		webshopimporter = new webshopImporter();

		// generate new window
		final Composite container = new Composite(parent, SWT.NONE);
		container.getShell().setSize(getInitialSize());

		// use SWT grid layout with 2 columns
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		GridData gridData;

		// use SWT browser widget to display the order details
		try {
			browser = new Browser(container, SWT.NONE);// SWT.Webkit for
														// non-mozilla browser
														// engine
		} catch (SWTError e) {
			System.out
					.println(Messages
							.getString("webshopImportWindow.Could_not_instantiate_Browser") + e.getMessage()); //$NON-NLS-1$
		}
		// set the browser's layout data
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.verticalSpan = 1;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		browser.setLayoutData(gridData);

		// use SWT table to display an overview of all orders
		table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// set the table's layout data
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 200;
		table.setLayoutData(gridData);

		// generate the table with 5 columns
		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText(Messages.getString("webshopImportWindow.ID")); //$NON-NLS-1$
		column1.setWidth(50);
		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setText(Messages.getString("webshopImportWindow.Name")); //$NON-NLS-1$
		column2.setWidth(200);
		TableColumn column3 = new TableColumn(table, SWT.NONE);
		column3.setText(Messages.getString("webshopImportWindow.Date")); //$NON-NLS-1$
		column3.setWidth(180);
		TableColumn column4 = new TableColumn(table, SWT.NONE);
		column4.setText(Messages.getString("webshopImportWindow.Total")); //$NON-NLS-1$
		column4.setWidth(100);
		TableColumn column5 = new TableColumn(table, SWT.NONE);
		column5.setText(Messages
				.getString("webshopImportWindow.Payment_Method")); //$NON-NLS-1$
		column5.setWidth(120);
		TableColumn column6 = new TableColumn(table, SWT.NONE);
		column6.setText(Messages.getString("webshopImportWindow.Status")); //$NON-NLS-1$
		column6.setWidth(120);

		// show the order details by selecting a table row
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem tableitem;
				tableitem = (TableItem) e.item;
				if (tableitem != null)
					browser.setText(webshopimporter.getHTMLPreview(tableitem
							.getText(0)));
			}
		});

		// generate a context menu
		final Menu menu = new Menu(table);
		final MenuItem menuItemGenerateInvoice = new MenuItem(menu, SWT.PUSH);
		menuItemGenerateInvoice.setText(Messages
				.getString("webshopImportWindow.Create_invoice")); //$NON-NLS-1$
		new MenuItem(menu, SWT.SEPARATOR);
		final MenuItem menuItemMarkAsPending = new MenuItem(menu, SWT.PUSH);
		menuItemMarkAsPending.setText(Messages
				.getString("webshopImportWindow.Mark_as_pending")); //$NON-NLS-1$
		final MenuItem menuItemMarkAsProcessing = new MenuItem(menu, SWT.PUSH);
		menuItemMarkAsProcessing.setText(Messages
				.getString("webshopImportWindow.Mark_as_processing")); //$NON-NLS-1$
		final MenuItem menuItemMarkAsShipped = new MenuItem(menu, SWT.PUSH);
		menuItemMarkAsShipped.setText(Messages
				.getString("webshopImportWindow.Mark_as_shipped")); //$NON-NLS-1$

		// generate a invoice using the context menu
		menuItemGenerateInvoice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelection().length == 1) {
					String order_id = table.getSelection()[0].getText(0);
					checkAndGenerateInvoiceFromOrderID(order_id);
				}
			}
		});

		// mark the order as pending
		menuItemMarkAsPending.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String order_id = new String();
				order_id = table.getSelection()[0].getText(0);
				offer selectedOffer = null;
				for (appTransaction currentStatus : client.getTransactions()
						.getTransactions()) {
					if (currentStatus.getTypeID() == offer.getType()) {
						offer current = (offer) currentStatus;

						if (current.getOrderID().equalsIgnoreCase(order_id)) {
							selectedOffer = current;
						}
					}
				}
				// webshopimporter.setOrderStatusById(order_id,orderstatus);
				webshopimporter.setTableStatus(table, order_id, 1);
				selectedOffer.setStatus(1);
				selectedOffer.save();
				browser.setText(webshopimporter.getHTMLPreview(order_id));
			}
		});

		// mark the order as processing
		menuItemMarkAsProcessing.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String order_id = new String();
				order_id = table.getSelection()[0].getText(0);
				offer selectedOffer = null;
				for (appTransaction currentStatus : client.getTransactions()
						.getTransactions()) {
					if (currentStatus.getTypeID() == offer.getType()) {
						offer current = (offer) currentStatus;

						if (current.getOrderID().equalsIgnoreCase(order_id)) {
							selectedOffer = current;
						}
					}
				}
				// webshopimporter.setOrderStatusById(order_id,orderstatus);
				webshopimporter.setTableStatus(table, order_id, 2);
				selectedOffer.setStatus(2);
				selectedOffer.save();

				browser.setText(webshopimporter.getHTMLPreview(order_id));
			}
		});

		// mark the order as shipped
		menuItemMarkAsShipped.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String order_id = new String();
				order_id = table.getSelection()[0].getText(0);
				offer selectedOffer = null;
				for (appTransaction currentStatus : client.getTransactions()
						.getTransactions()) {
					if (currentStatus.getTypeID() == offer.getType()) {
						offer current = (offer) currentStatus;

						if (current.getOrderID().equalsIgnoreCase(order_id)) {
							selectedOffer = current;
						}
					}
				}
				selectedOffer.setStatus(3);
				selectedOffer.save();

				// webshopimporter.setOrderStatusById(order_id,orderstatus);
				webshopimporter.setTableStatus(table, order_id, 3);
				browser.setText(webshopimporter.getHTMLPreview(order_id));
			}
		});

		// generate the context menu, depending on the state of the order
		table.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(MenuDetectEvent e) {
				Point clickedPoint = new Point(e.x, e.y);

				TableItem[] ti = table.getSelection();
				String order_id = new String();
				String orderstatus = new String();
				if (table.getSelectionCount() == 1) {
					order_id = ti[0].getText(0);

					offer selectedOffer = null;
					for (appTransaction currentStatus : client
							.getTransactions().getTransactions()) {
						if (currentStatus.getTypeID() == offer.getType()) {
							offer current = (offer) currentStatus;

							if (current.getOrderID().equalsIgnoreCase(order_id)) {
								selectedOffer = current;
							}
						}
					}

					menuItemMarkAsPending.setEnabled(selectedOffer.getStatus() != 1); //$NON-NLS-1$
					menuItemMarkAsProcessing.setEnabled(selectedOffer
							.getStatus() != 2); //$NON-NLS-1$
					menuItemMarkAsShipped.setEnabled(selectedOffer.getStatus() != 3); //$NON-NLS-1$
					menu.setLocation(clickedPoint);
					menu.setVisible(true);
				}
			}
		});

		// create the reload button
		final Button btnReload;
		btnReload = new Button(container, SWT.NONE);
		btnReload.setText(Messages
				.getString("webshopImportWindow.Get_data_from_web_shop")); //$NON-NLS-1$
		btnReload.setImage(new Image(getShell().getDisplay(), getClass()
				.getResourceAsStream("/libs/icon_reload.png"))); //$NON-NLS-1$

		// set the button's layout data
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.LEFT;
		gridData.verticalAlignment = SWT.TOP;
		gridData.horizontalSpan = 1;
		gridData.verticalSpan = 1;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = false;
		btnReload.setLayoutData(gridData);

		btnReload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				syncWithWebshop();
			}
		});

		// create the source for drag and drop
		final DragSource tableDragSource = new DragSource(table, DND.DROP_MOVE);
		tableDragSource
				.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		tableDragSource.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(final DragSourceEvent event) {
				TableItem[] ti = table.getSelection();
				String data = new String();
				for (int i = 0; i < table.getSelectionCount(); i++) {
					data += ti[i].getText(0) + ","; //$NON-NLS-1$
				}
				event.data = data;
			}

			@Override
			public void dragStart(final DragSourceEvent event) {
				if (table.getSelectionCount() > 0) {
					event.doit = true;
				}
			}
		});

		// create the drop zone to drop down the table entry
		final Group tableItemDropGroup = new Group(container, SWT.NONE);
		tableItemDropGroup.setBackground(new Color(getShell().getDisplay(),
				191, 191, 191));

		// set the drop zone's layout data
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.RIGHT;
		gridData.verticalAlignment = SWT.BOTTOM;
		gridData.horizontalSpan = 1;
		gridData.verticalSpan = 2;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = false;
		tableItemDropGroup.setLayoutData(gridData);

		tableItemDropGroup.setText(Messages
				.getString("documentsWindow.dropzone")); //$NON-NLS-1$
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.HORIZONTAL;
		fillLayout.marginHeight = 10;
		fillLayout.marginWidth = 10;
		tableItemDropGroup.setLayout(fillLayout);

		// create the button to create a invoice on dropping down a table entry
		final Button btnDropGenerateInvoice = new Button(tableItemDropGroup,
				SWT.NONE);
		btnDropGenerateInvoice.setImage(new Image(getShell().getDisplay(),
				getClass().getResourceAsStream(
						"/libs/sarxos_Simple_Folder_Documents_small.png"))); //$NON-NLS-1$
		btnDropGenerateInvoice.setText(Messages
				.getString("webshopImportWindow.Create_invoice")); //$NON-NLS-1$

		btnDropGenerateInvoice.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(final FocusEvent e) {
				getShell().setFocus();
				MessageDialog.openInformation(
						getShell(),
						Messages.getString("documentsWindow.draganddropcaption"), Messages.getString("documentsWindow.dranganddropInfotext")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		btnDropGenerateInvoice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelection().length == 1) {
					String order_id = table.getSelection()[0].getText(0);
					checkAndGenerateInvoiceFromOrderID(order_id);
				}
			}
		});

		// create the drop target
		final DropTarget btnDeleteDropTarget = new DropTarget(
				btnDropGenerateInvoice, DND.DROP_MOVE);
		btnDeleteDropTarget.setTransfer(new Transfer[] { TextTransfer
				.getInstance() });
		btnDeleteDropTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(final DropTargetEvent event) {
				String theEntries = (String) event.data;
				String[] elems = theEntries.split(","); //$NON-NLS-1$
				for (String elem : elems) {
					checkAndGenerateInvoiceFromOrderID(elem);
				}
			}
		});

		// create combo to select, which orders with shipped status should be
		// displayed
		final Combo comboShippedIntervall;
		comboShippedIntervall = new Combo(container, SWT.READ_ONLY);
		comboShippedIntervall
				.setItems(new String[] {
						Messages.getString("webshopImportWindow.hide_shipped"), Messages.getString("webshopImportWindow.shipped_last_week"), Messages.getString("webshopImportWindow.shipped_last_month"), Messages.getString("webshopImportWindow.shipped_last_years"), Messages.getString("webshopImportWindow.ever_shipped") }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		comboShippedIntervall.select(0);
		// set the combo's layout data
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.LEFT;
		gridData.verticalAlignment = SWT.TOP;
		gridData.horizontalSpan = 1;
		gridData.verticalSpan = 1;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = false;
		comboShippedIntervall.setLayoutData(gridData);

		comboShippedIntervall.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// webshopimporter.setShippedInterval();
				if (comboShippedIntervall.getSelectionIndex() == 0)
					webshopimporter.setShippedInterval(""); //$NON-NLS-1$
				if (comboShippedIntervall.getSelectionIndex() == 1)
					webshopimporter.setShippedInterval("1 week"); //$NON-NLS-1$
				if (comboShippedIntervall.getSelectionIndex() == 2)
					webshopimporter.setShippedInterval("1 month"); //$NON-NLS-1$
				if (comboShippedIntervall.getSelectionIndex() == 3)
					webshopimporter.setShippedInterval("1 year"); //$NON-NLS-1$
				if (comboShippedIntervall.getSelectionIndex() == 4)
					webshopimporter.setShippedInterval("ever"); //$NON-NLS-1$
			}
		});

		container.pack();
		return container;
	}

	/**
	 * Configure the shell. Use the applications name.
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(application.getAppName());
	}

	/**
	 * Return the initial size of the window.
	 * 
	 * @return size in pixel
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(800, 700);
	}

}