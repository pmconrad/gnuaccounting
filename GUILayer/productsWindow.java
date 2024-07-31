package GUILayer;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Vector;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.hashMapContentProvider;
import appLayer.product;
import appLayer.products;
import appLayer.utils;
import appLayer.taxRelated.taxNotFoundException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code39Writer;

public class productsWindow extends ApplicationWindow {

	class ContentProvider implements IStructuredContentProvider {
		protected products allProducts = null;

		public ContentProvider() {
			super();
			allProducts = client.getProducts();
		}

		public Object[] getElements(Object inputElement) {
			return allProducts.getProducts().toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class productSelectionChangedListener implements ISelectionChangedListener {
		protected productsWindow parent;

		public productSelectionChangedListener(productsWindow parent) {
			this.parent = parent;

		}

		public void selectionChanged(SelectionChangedEvent arg0) {
			parent.updateControlsFromSelection();

		}
	}

	private ListViewer listViewer = null;
	private ComboViewer cvUnit = null;
	private Combo cmbVAT;
	private Combo cmbTVQ;
	private Text txtFldPrice;
	private Text txtFldName;
	private StyledText txtAreaDescription;
	private Label lblIDValue = null;

	static Vector<Integer> vatIDs = new Vector<Integer>();
	static Vector<Integer> productIDs = new Vector<Integer>();
	private Button btnDelete = null;
	private Combo cmbType = null;
	private Text txtBarcode;
	private Button btnPrintLabel;
	private Combo cmbUnit;

	public productsWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	static ImageData convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage
					.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(),
					colorModel.getGreenMask(), colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int rgb = bufferedImage.getRGB(x, y);
					int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF,
							(rgb >> 8) & 0xFF, rgb & 0xFF));
					data.setPixel(x, y, pixel);
					if (colorModel.hasAlpha()) {
						data.setAlpha(x, y, (rgb >> 24) & 0xFF);
					}
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) bufferedImage
					.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
						blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		}
		return null;
	}

	private void checkEAN() {
		if (txtBarcode.getText().length() == 0) {
			txtBarcode.setForeground(new Color(getShell().getDisplay(), 0x00,
					0x00, 0x00));
			btnPrintLabel.setEnabled(false);
		} else {
			btnPrintLabel.setEnabled(true);

		}
		if (utils.checkEAN(txtBarcode.getText())) {
			txtBarcode.setForeground(new Color(getShell().getDisplay(), 0x00,
					0x80, 0x00));
		} else {
			txtBarcode.setForeground(new Color(getShell().getDisplay(), 0x80,
					0x00, 0x00));
		}
	}

	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());

		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.NONE);
		scroll.setSize(getInitialSize());
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setAlwaysShowScrollBars(true);
		// scroll.setMinSize(200, 400);

		Composite container = new Composite(scroll, SWT.NONE);
		container.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
				.margins(10, 5).create());

		scroll.setContent(container);

		listViewer = new ListViewer(container, SWT.V_SCROLL | SWT.BORDER);
		listViewer
				.addSelectionChangedListener(new productSelectionChangedListener(
						this));
		listViewer.setContentProvider(new ContentProvider());
		Object[] toBeSelectedItems = new Object[1];
		toBeSelectedItems[0] = client.getProducts().getProduct(
				product.newProductID);
		// IStructuredSelection selection = new
		// StructuredSelection(toBeSelectedItems);
		// listViewer.setSelection(selection);
		listViewer.setInput(new Object());

		List list = listViewer.getList();
		list.setFont(configs.getDefaultFont());
		list.select(0);

		GridDataFactory.fillDefaults().span(2, 1).grab(true, true)
				.applyTo(list);

		final Label lblID = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(lblID);
		lblID.setAlignment(SWT.RIGHT);
		lblID.setFont(configs.getDefaultFont());
		lblID.setText(Messages.getString("productsWindow.id")); //$NON-NLS-1$

		lblIDValue = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(lblIDValue);
		lblIDValue.setFont(configs.getDefaultFont());
		lblIDValue.setText(Messages.getString("productsWindow.label")); //$NON-NLS-1$

		final Label lblName = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(lblName);
		lblName.setAlignment(SWT.RIGHT);
		lblName.setFont(configs.getDefaultFont());
		lblName.setText(Messages.getString("productsWindow.name")); //$NON-NLS-1$

		txtFldName = new Text(container, SWT.BORDER);
		txtFldName.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().applyTo(txtFldName);

		final Label lblDescription = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(lblDescription);
		lblDescription.setAlignment(SWT.RIGHT);
		lblDescription.setFont(configs.getDefaultFont());
		lblDescription
				.setText(Messages.getString("productsWindow.description")); //$NON-NLS-1$

		txtAreaDescription = new StyledText(container, SWT.BORDER);
		txtAreaDescription.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().grab(false, true)
				.applyTo(txtAreaDescription);

		Label lblBarcode = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(lblBarcode);
		lblBarcode
				.setText(Messages.getString("productsWindow.lblBarcode.text")); //$NON-NLS-1$
		lblBarcode.setFont(configs.getDefaultFont());
		lblBarcode.setAlignment(SWT.RIGHT);
		lblBarcode.setFont(configs.getDefaultFont());

		txtBarcode = new Text(container, SWT.BORDER);
		txtBarcode.setBounds(125, 239, 135, 22);
		GridDataFactory.fillDefaults().applyTo(txtBarcode);
		txtBarcode.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				checkEAN();
			}

		});
		txtBarcode.setFont(configs.getDefaultFont());

		final Label lblUnit = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(lblUnit);
		lblUnit.setAlignment(SWT.RIGHT);
		lblUnit.setFont(configs.getDefaultFont());
		lblUnit.setText(Messages.getString("productsWindow.unit")); //$NON-NLS-1$

		cvUnit = new ComboViewer(container);
		cmbUnit = cvUnit.getCombo();
		GridDataFactory.fillDefaults().applyTo(cmbUnit);
		cmbUnit.setFont(configs.getDefaultFont());

		hashMapContentProvider hmp = new hashMapContentProvider();
		cvUnit.setContentProvider(hmp);
		cvUnit.setInput(client.getProducts().getUnitCodes());

		final Label lblType = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(lblType);
		lblType.setAlignment(SWT.RIGHT);
		lblType.setFont(configs.getDefaultFont());
		lblType.setText(Messages.getString("productsWindow.typeLabel")); //$NON-NLS-1$

		cmbType = new Combo(container, SWT.READ_ONLY);
		GridDataFactory.fillDefaults().applyTo(cmbType);
		cmbType.setFont(configs.getDefaultFont());
		cmbType.setItems(products.types);

		final Label lblPrice = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(lblPrice);
		lblPrice.setAlignment(SWT.RIGHT);
		lblPrice.setFont(configs.getDefaultFont());
		lblPrice.setText(Messages.getString("productsWindow.netprice")); //$NON-NLS-1$

		txtFldPrice = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().applyTo(txtFldPrice);
		txtFldPrice.setFont(configs.getDefaultFont());

		final Label vatLabel = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(vatLabel);
		vatLabel.setAlignment(SWT.RIGHT);
		vatLabel.setFont(configs.getDefaultFont());
		vatLabel.setText(Messages.getString("productsWindow.vat")); //$NON-NLS-1$

		cmbVAT = new Combo(container, SWT.READ_ONLY);
		GridDataFactory.fillDefaults().applyTo(cmbVAT);
		cmbVAT.setFont(configs.getDefaultFont());
		client.getTaxes().getTaxesFromDatabase();
		cmbVAT.setItems(client.getTaxes().getStringArray());

		if (configs.hasSalesTax()) {
			final Label tvqLabel = new Label(container, SWT.NONE);
			GridDataFactory.fillDefaults().applyTo(tvqLabel);
			tvqLabel.setAlignment(SWT.RIGHT);
			tvqLabel.setFont(configs.getDefaultFont());
			tvqLabel.setText(Messages.getString("productsWindow.TVQ")); //$NON-NLS-1$

			cmbTVQ = new Combo(container, SWT.READ_ONLY);
			GridDataFactory.fillDefaults().applyTo(cmbTVQ);
			cmbTVQ.setFont(configs.getDefaultFont());
			cmbTVQ.setItems(client.getTaxes().getStringArray());
		}

		Link lnkEditVAT = new Link(container, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(lnkEditVAT);
		lnkEditVAT.setFont(configs.getDefaultFont());
		lnkEditVAT.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				taxesWindow win = new taxesWindow();
				win.open();
			}
		});
		lnkEditVAT.setText(Messages.getString("productsWindow.editVATLink")); //$NON-NLS-1$

		btnPrintLabel = new Button(container, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(btnPrintLabel);
		btnPrintLabel.setFont(configs.getDefaultFont());
		btnPrintLabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// PrinterJob job = null;
				// try {
				// b =
				// BarcodeFactory.createEAN13(txtBarcode.getText().substring(0,12));
				// job = PrinterJob.getPrinterJob();

				PrinterData printerData = null;
				for (PrinterData current : Printer.getPrinterList()) {
					if (current.name
							.equalsIgnoreCase("DYMO LabelWriter 450 Turbo")) { //$NON-NLS-1$
						printerData = current;
					}
					System.err.println(current.name);

				}
				printerData.orientation = PrinterData.LANDSCAPE;
				Printer printer = new Printer(printerData);
				Point dpi = printer.getDPI();

				Code39Writer writer = new Code39Writer();
				BitMatrix bitMatrix = null;

				int qrCodeWidth = 900;
				int qrCodeHeight = 300;

				// could be supported in the future: bezahlcode
				// executiondate=ddmmyyyy
				try {
					bitMatrix = writer.encode("E-1234", //$NON-NLS-1$
							BarcodeFormat.CODE_39, qrCodeWidth, qrCodeHeight);
				} catch (WriterException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				BufferedImage bi = MatrixToImageWriter
						.toBufferedImage(bitMatrix);

				Rectangle rect = printer.getClientArea();
				if (printer.startJob("SWT Printing Snippet")) { //$NON-NLS-1$

					GC gc = new GC(printer);
					if (printer.startPage()) {
						int oneInch = dpi.x;
						gc.drawString("Hello World!", 0, 0); //$NON-NLS-1$
						ImageData data = convertToSWT(bi);
						final Image swtImage = new Image(printer, data);
						gc.drawImage(swtImage, 1, 1);

						/*
						 * gc.drawString("Printed on " + printerData.name +
						 * " using SWT on " + SWT.getPlatform(), oneInch,
						 * oneInch * 5 / 2);
						 */
						printer.endPage();
					}
					gc.dispose();
					printer.endJob();
				}
				printer.dispose();
				// job.setPrintable(b);
				/*
				 * } catch (BarcodeException e1) { // TODO Auto-generated catch
				 * block e1.printStackTrace(); }
				 */

				/*
				 * try { job.print(); } catch (PrinterException e1) { // TODO
				 * Auto-generated catch block e1.printStackTrace(); }
				 */

			}
		});
		btnPrintLabel.setText(Messages
				.getString("productsWindow.btnPrintLabel.text")); //$NON-NLS-1$

		Button btnOK;
		btnOK = new Button(container, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(btnOK);
		btnOK.setFont(configs.getDefaultFont());
		btnOK.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer
						.getSelection();
				product selectedProduct = (product) selection.getFirstElement();
				boolean newProductCreated = false;
				if (selectedProduct.getID() == product.newProductID) {
					selectedProduct = product.getNewProduct();
					client.getProducts().add(selectedProduct);
					selectedProduct.setParent(client.getProducts());
					newProductCreated = true;
				}
				int errorCount = 0;
				String priceStr = txtFldPrice.getText();
				if (priceStr.equalsIgnoreCase("")) { //$NON-NLS-1$
					errorCount++;
					getStatusLineManager().setErrorMessage(
							Messages.getString("productsWindow.emptyPrice")); //$NON-NLS-1$
				}
				try {

					selectedProduct.setPrice(utils.String2BD(priceStr));
				} catch (NumberFormatException ex) {
					getStatusLineManager()
							.setErrorMessage(
									Messages.getString("productsWindow.priceInvalid") + priceStr + Messages.getString("productsWindow.wassetto0")); //$NON-NLS-1$ //$NON-NLS-2$
					priceStr = "0"; //$NON-NLS-1$
					// ignore text entered in number field
				}

				if (txtFldName.getText().equalsIgnoreCase("")) { //$NON-NLS-1$
					errorCount++;
					getStatusLineManager()
							.setErrorMessage(
									Messages.getString("productsWindow.productEmptyName")); //$NON-NLS-1$
				} else {
					selectedProduct.setName(txtFldName.getText());

				}
				selectedProduct.setDescription(txtAreaDescription.getText());
				selection = (IStructuredSelection) cvUnit.getSelection();
				String selected = (String) selection.getFirstElement();
				selectedProduct.setUnit(client.getProducts().getUnitCode(
						selected));

				selectedProduct.setBarcode(txtBarcode.getText());
				selectedProduct.setType(cmbType.getSelectionIndex());
				globalFilterKeyListener.refreshBarcodes();
				try {
					selectedProduct.setTax(client.getTaxes().getVATAtListIndex(
							cmbVAT.getSelectionIndex()));
					if (configs.hasSalesTax()) {
						selectedProduct.setSalesTax(client.getTaxes()
								.getVATAtListIndex(cmbTVQ.getSelectionIndex()));
					}
				} catch (taxNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (errorCount == 0) {
					selectedProduct.save();
					listViewer.refresh();

					todoWindow.refreshToDoList();
					if (newProductCreated) {
						selection = new StructuredSelection(selectedProduct);
						listViewer.setSelection(selection);
					}

					getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$

				}

			}
		});
		btnOK.setText(Messages.getString("productsWindow.ok")); //$NON-NLS-1$

		btnDelete = new Button(container, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(btnDelete);
		btnDelete.setFont(configs.getDefaultFont());
		btnDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer
						.getSelection();
				product selectedProduct = (product) selection.getFirstElement();

				int errorCount = 0;

				if (listViewer.getList().getItemCount() <= 2) {
					// make sure the user does not delete the last product
					getStatusLineManager()
							.setErrorMessage(
									Messages.getString("productsWindow.oneProductNeeded")); //$NON-NLS-1$
					errorCount++;
				}
				if (errorCount == 0) {
					selectedProduct.delete();
					listViewer.remove(selectedProduct);
					listViewer.refresh();
					listViewer.getList().select(0);

					getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$

				}

			}
		});
		btnDelete.setText(Messages.getString("productsWindow.delete")); //$NON-NLS-1$

		/*
		 * ScrolledComposite sc = new ScrolledComposite(container, SWT.NONE);
		 * GridDataFactory.fillDefaults().span(2,1).applyTo(sc);
		 * sc.setSize(100,30); sc.setBackground(new
		 * Color(getShell().getDisplay(), new RGB(255,0,0))); sc.setLayout(new
		 * FillLayout()); sc.setExpandHorizontal(true);
		 * sc.setExpandVertical(true); Composite c=new Composite(sc, SWT.None);
		 * c
		 * .setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(10,
		 * 5).create()); sc.setContent(c);
		 * 
		 * 
		 * Button btn = new Button(c, SWT.NONE);
		 * GridDataFactory.fillDefaults().applyTo(btn); btn.setText("huch");
		 * 
		 * 
		 * Button btn2 = new Button(c, SWT.NONE);
		 * GridDataFactory.fillDefaults().applyTo(btn2); btn2.setText("huch2");
		 */
		updateControlsFromSelection();

		//
		return container;
	}

	public void updateControlsFromSelection() {

		Object[] objects = ((ContentProvider) listViewer.getContentProvider())
				.getElements(null);
		product defaultProduct = (product) objects[0];

		IStructuredSelection selection = (IStructuredSelection) listViewer
				.getSelection();
		product selectedProduct = (product) selection.getFirstElement();
		if (selection.isEmpty()) {
			// default select first element (new asset), e.g. if current element
			// wasd deleted
			selectedProduct = defaultProduct;
		}

		if ((selectedProduct == defaultProduct)
				|| (selectedProduct.getID() == product.newProductID)) {
			btnDelete.setEnabled(false);
		} else {
			btnDelete.setEnabled(true);
		}

		StructuredSelection sel = new StructuredSelection(client.getProducts()
				.getUnitName(selectedProduct.getUnit()));
		cvUnit.setSelection(sel);
		lblIDValue.setText(Integer.toString(selectedProduct.getID()));
		cmbType.select(selectedProduct.getType());
		txtFldName.setText(selectedProduct.getName());
		txtAreaDescription.setText(selectedProduct.getDescription());
		txtFldPrice.setText(selectedProduct.getPrice().toString());
		cmbVAT.select(selectedProduct.getVAT().getIDinList());
		if (configs.hasSalesTax()) {
			cmbTVQ.select(selectedProduct.getSalesTax().getIDinList());
		}

		String barcode = selectedProduct.getBarcode();
		if (barcode == null) {
			barcode = ""; //$NON-NLS-1$
		}
		txtBarcode.setText(barcode);
		checkEAN();
	}

	private void createActions() {
	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("productsWindow.menu")); //$NON-NLS-1$
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
		return new Point(331, 787);
	}
}