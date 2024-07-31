package GUILayer;

import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import appLayer.application;
import appLayer.asset;
import appLayer.client;
import appLayer.configs;
import appLayer.utils;

public class assetWindow extends ApplicationWindow {
	class AssetContentProvider implements IStructuredContentProvider {
		public AssetContentProvider() {
			super();
		}

		public Object[] getElements(Object inputElement) {
			return client.getAssets().getAssets(true).toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class assetSelectionChangedListener implements ISelectionChangedListener {
		protected assetWindow parent;

		public assetSelectionChangedListener(assetWindow parent) {
			this.parent = parent;

		}

		public void selectionChanged(SelectionChangedEvent arg0) {
			parent.updateControlsFromSelection();

		}
	}

	// //////////// CLASS assetWindow

	private Text txtFldPrice;
	private Text txtFldName;
	private Label lblID = null;
	private DateTime dteFrom = null;
	private DateTime dteTo = null;
	private TableViewer tableViewer = null;
	private ComboViewer cmbStatus = null;

	private Button btnDelete;
	private Text txtLifetime;
	private Text txtNumber;
	private Text txtRemark;
	private Text txtLocation;
	private Table table;

	public assetWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	public void updateControlsFromSelection() {

		Object[] objects = ((AssetContentProvider) tableViewer
				.getContentProvider()).getElements(null);
		asset defaultAsset = (asset) objects[0];

		IStructuredSelection selection = (IStructuredSelection) tableViewer
				.getSelection();
		asset selectedAsset = (asset) selection.getFirstElement();
		if (selection.isEmpty()) {
			// default select first element (new asset), e.g. if current element
			// wasd deleted
			selectedAsset = defaultAsset;
		}

		txtFldName.setText(selectedAsset.getName());
		txtNumber.setText(selectedAsset.getNumber());
		txtRemark.setText(selectedAsset.getRemark());
		txtLocation.setText(selectedAsset.getLocation());

		txtFldPrice.setText(selectedAsset.getValue().toString());

		lblID.setText(Integer.toString(selectedAsset.getID()));
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(selectedAsset.getDepreciationStart());
		dteFrom.setYear(cal.get(java.util.Calendar.YEAR));
		dteFrom.setMonth(cal.get(java.util.Calendar.MONTH));
		dteFrom.setDay(cal.get(java.util.Calendar.DAY_OF_MONTH));

		cal.setTime(selectedAsset.getDepreciationEnd());
		dteTo.setYear(cal.get(java.util.Calendar.YEAR));
		dteTo.setMonth(cal.get(java.util.Calendar.MONTH));
		dteTo.setDay(cal.get(java.util.Calendar.DAY_OF_MONTH));

		txtLifetime.setText(Integer.toString(selectedAsset.getLifetime()));
		cmbStatus.getCombo().select(selectedAsset.getStatus());

		if (selectedAsset == defaultAsset) {
			btnDelete.setEnabled(false);
			txtNumber.setText(client.getAssets().getNextNumber());
			txtFldName.setText(""); //$NON-NLS-1$
			txtFldName.forceFocus();
		} else {
			btnDelete.setEnabled(true);
		}

	}

	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());

		Composite container = new Composite(parent, SWT.NONE);

		container.setFont(configs.getDefaultFont());
		GridLayoutFactory.swtDefaults().numColumns(2).margins(10, 5)
		.applyTo(container);

		

		tableViewer = new TableViewer(container, SWT.V_SCROLL | SWT.BORDER);
		tableViewer
				.addSelectionChangedListener(new assetSelectionChangedListener(
						this));
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		GridDataFactory.fillDefaults().hint(200, 200).span(2,1).grab(true, true)
		.applyTo(table);

		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer,
				SWT.NONE);
		nameColumn.setLabelProvider(new CellLabelProvider() {

			@Override
			public void update(ViewerCell arg0) {

				asset currentAsset = (asset) arg0.getElement();
				arg0.setText(currentAsset.getName());

			}
		});
		TableColumn tblclmnName = nameColumn.getColumn();
		tblclmnName.setWidth(200);
		tblclmnName.setText(Messages.getString("assetWindow.tblclmnName.text")); //$NON-NLS-1$

		TableViewerColumn numberColumn = new TableViewerColumn(tableViewer,
				SWT.NONE);
		TableColumn tblclmnNumber = numberColumn.getColumn();
		tblclmnNumber.setWidth(100);
		tblclmnNumber.setText(Messages
				.getString("assetWindow.tblclmnNumber.text")); //$NON-NLS-1$
		numberColumn.setLabelProvider(new CellLabelProvider() {

			@Override
			public void update(ViewerCell arg0) {

				asset currentAsset = (asset) arg0.getElement();
				arg0.setText(currentAsset.getNumber());

			}
		});
		tableViewer.setContentProvider(new AssetContentProvider());
		tableViewer.setInput(new Object());
		


		final Label idLabel = new Label(container, SWT.NONE);
		idLabel.setAlignment(SWT.RIGHT);
		idLabel.setFont(configs.getDefaultFont());
		idLabel.setText(Messages.getString("assetWindow.id")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().applyTo(idLabel);

		lblID = new Label(container, SWT.NONE);
		lblID.setFont(configs.getDefaultFont());
		lblID.setText(Messages.getString("assetWindow.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lblID);

		

		final Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setAlignment(SWT.RIGHT);
		nameLabel.setFont(configs.getDefaultFont());
		nameLabel.setText(Messages.getString("assetWindow.name")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().applyTo(nameLabel);
		
		txtFldName = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtFldName);
		txtFldName.setFont(configs.getDefaultFont());

		final Label priceLabel = new Label(container, SWT.NONE);
		priceLabel.setAlignment(SWT.RIGHT);
		priceLabel.setFont(configs.getDefaultFont());
		priceLabel.setText(Messages.getString("assetWindow.netPrice")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().applyTo(priceLabel);

		txtFldPrice = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtFldPrice);
		txtFldPrice.setFont(configs.getDefaultFont());



		Label lblResidual = new Label(container, SWT.NONE);
		lblResidual.setAlignment(SWT.RIGHT);
		lblResidual.setText(Messages.getString("assetWindow.lblResidual.text")); //$NON-NLS-1$
		lblResidual.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().applyTo(lblResidual);

		Label lblResidualValue = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lblResidualValue);
		lblResidualValue.setText(Messages
				.getString("assetWindow.lblResidualValue.text")); //$NON-NLS-1$
		lblResidualValue.setFont(configs.getDefaultFont());
		
		
		
		Label lblFrom;
		lblFrom = new Label(container, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(lblFrom);
		lblFrom.setAlignment(SWT.RIGHT);
		lblFrom.setFont(configs.getDefaultFont());
		lblFrom.setText(Messages.getString("assetWindow.inusefrom")); //$NON-NLS-1$
		

		dteFrom = new DateTime(container, SWT.DATE | SWT.DROP_DOWN);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(dteFrom);
		dteFrom.setFont(configs.getDefaultFont());


		Label lblLifetime = new Label(container, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(lblLifetime);
		lblLifetime.setAlignment(SWT.RIGHT);
		lblLifetime.setText(Messages.getString("assetWindow.lblLifetime.text")); //$NON-NLS-1$
		lblLifetime.setFont(configs.getDefaultFont());

		txtLifetime = new Text(container, SWT.BORDER);

		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtLifetime);
		txtLifetime.setText(Messages.getString("assetWindow.text.text")); //$NON-NLS-1$
		txtLifetime.setFont(configs.getDefaultFont());

		Label lblStatus = new Label(container, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(lblStatus);
		lblStatus.setAlignment(SWT.RIGHT);
		lblStatus.setText(Messages.getString("assetWindow.lblStatus.text")); //$NON-NLS-1$
		lblStatus.setFont(configs.getDefaultFont());

		cmbStatus = new ComboViewer(container, SWT.READ_ONLY);
		cmbStatus.setContentProvider(ArrayContentProvider.getInstance());
		cmbStatus.setInput(asset.getStati());

		Combo combo = cmbStatus.getCombo();

		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(combo);
		combo.setFont(configs.getDefaultFont());

		Label lblNumber = new Label(container, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(lblNumber);
		lblNumber.setFont(configs.getDefaultFont());
		lblNumber.setAlignment(SWT.RIGHT);
		lblNumber.setText(Messages.getString("assetWindow.lblNumber.text")); //$NON-NLS-1$

		txtNumber = new Text(container, SWT.BORDER);

		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtNumber);
		txtNumber.setFont(configs.getDefaultFont());
		
		
		Label lblRemark = new Label(container, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(lblRemark);
		lblRemark.setFont(configs.getDefaultFont());
		lblRemark.setAlignment(SWT.RIGHT);
		lblRemark.setText(Messages.getString("assetWindow.lblRemark.text")); //$NON-NLS-1$


		txtRemark = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtRemark);

		txtRemark.setFont(configs.getDefaultFont());

		Label lblLocation = new Label(container, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(lblLocation);
		lblLocation.setFont(configs.getDefaultFont());
		lblLocation.setAlignment(SWT.RIGHT);
		lblLocation.setText(Messages.getString("assetWindow.lblLocation.text")); //$NON-NLS-1$


		txtLocation = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtLocation);
		txtLocation.setFont(configs.getDefaultFont());


		Label lbldteTo = new Label(container, SWT.NONE);
		lbldteTo.setText(Messages.getString("assetWindow.endDateLabel")); //$NON-NLS-1$
		lbldteTo.setFont(configs.getDefaultFont());
		lbldteTo.setAlignment(SWT.RIGHT);
		GridDataFactory.swtDefaults().applyTo(lbldteTo);

		dteTo = new DateTime(container, SWT.DROP_DOWN);
		dteTo.setYear(2014);
		dteTo.setMonth(0);
		dteTo.setFont(configs.getDefaultFont());
		dteTo.setDay(7);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(dteTo);

		
		Button btnOK;
		btnOK = new Button(container, SWT.NONE);
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
		.applyTo(btnOK);
		btnOK.setFont(configs.getDefaultFont());
		btnOK.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				asset selectedAsset = (asset) selection.getFirstElement();
				boolean assetCreated = false;
				if (selectedAsset.getID() == asset.newAssetID) {
					assetCreated = true;
					selectedAsset = asset.getNewAsset();
					selectedAsset.setParent(client.getAssets());
				}
				selectedAsset.setName(txtFldName.getText());

				selectedAsset.setNumber(txtNumber.getText());
				selectedAsset.setRemark(txtRemark.getText());
				selectedAsset.setLocation(txtLocation.getText());

				selectedAsset.setValue(utils.String2BD(txtFldPrice.getText()));

				GregorianCalendar cal = new GregorianCalendar();

				cal.set(dteFrom.getYear(), dteFrom.getMonth(), dteFrom.getDay());
				Date fromDte = cal.getTime();
				selectedAsset.setDepreciationStart(fromDte);

				cal.set(dteTo.getYear(), dteTo.getMonth(), dteTo.getDay());

				Date toDte = cal.getTime();
				selectedAsset.setDepreciationEnd(toDte);

				// selectedAsset.setDepreciationEnd(toDte);
				selectedAsset.setLifetime(Integer.valueOf(txtLifetime.getText()));
				selectedAsset.setStatus(cmbStatus.getCombo()
						.getSelectionIndex());

				selectedAsset.save();
				tableViewer.refresh();
				if (assetCreated) {
					selection = new StructuredSelection(selectedAsset);
					tableViewer.setSelection(selection);
				}

				updateControlsFromSelection();

			}
		});

		btnOK.setText(Messages.getString("assetWindow.ok")); //$NON-NLS-1$

		btnDelete = new Button(container, SWT.NONE);
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
		.applyTo(btnDelete);

		btnDelete.setFont(configs.getDefaultFont());
		btnDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				asset selectedAsset = (asset) selection.getFirstElement();
				selectedAsset.delete();
				tableViewer.remove(selectedAsset);
				tableViewer.refresh();
				// list.select(0);
				updateControlsFromSelection();

			}
		});
		btnDelete.setText(Messages.getString("assetWindow.delete")); //$NON-NLS-1$
		
		updateControlsFromSelection();

		//
		return container;
	}

	private void createActions() {
	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("assetWindow.menu")); //$NON-NLS-1$
		return menuManager;
	}

	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		statusLineManager.setMessage(null, ""); //$NON-NLS-1$
		return statusLineManager;
	}

	public static void main(String args[]) {
		try {
			productsWindow window = new productsWindow();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(application.getAppName());
	}

	protected Point getInitialSize() {
		return new Point(478, 753);
	}
}
