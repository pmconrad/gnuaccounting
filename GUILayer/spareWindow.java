package GUILayer;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import appLayer.application;
import GUILayer.Messages;


public class spareWindow extends ApplicationWindow {

	/**
	 * Create the application window.
	 */
	public spareWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}
	
	
	
	/* konik.io embed test:
	Button btnNewButton = new Button(container, SWT.NONE);
	btnNewButton.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {

			   Invoice invoice = new Invoice(ConformanceLevel.BASIC);   
			   Date d=new Date();
			   invoice.setHeader(new Header()
			      .setInvoiceNumber("20131122-42")
			      .setCode(DocumentCode._380)
			      .setIssued(new ZfDateDay(d.getTime()))
			      .setName("Rechnung"));

			   Trade trade = new Trade();
			   trade.setAgreement(new Agreement()    
			         .setSeller(new TradeParty()
			               .setName("Seller Inc.")
			               .setAddress(new Address("80331", "Marienplatz 1", "München", CountryCode.DE))
			               .addTaxRegistrations(new TaxRegistration("DE122...", Reference.FC)))
			         .setBuyer(new TradeParty()
			               .setName("Buyer Inc.")
			               .setAddress(new Address("50667", "Domkloster 4", "Köln", CountryCode.DE))
			               .addTaxRegistrations(new TaxRegistration("DE123...", Reference.FC))));

			   trade.setDelivery(new Delivery(new ZfDateDay(d.getTime())));

			   trade.setSettlement(new Settlement()
			         .setPaymentReference("20131122-42")
			         .setCurrency(CurrencyCode.EUR)
			         .addPaymentMeans(new PaymentMeans()
			            .setPayerAccount(new FinancialAccount("DE01234.."))
			               .setPayerInstitution(new FinancialInstitution("GENO...")))
			         .setMonetarySummation(new MonetarySummation()
			            .setLineTotal(new Amount(100, CurrencyCode.EUR))
			            .setTaxTotal(new Amount(19, CurrencyCode.EUR))
			            .setGrandTotal(new Amount(119, CurrencyCode.EUR))));

			   trade.addItem(new Item()
			      .setProduct(new Product().setName("Saddle"))
			      .setDelivery(new SpecifiedDelivery(new Quantity(1, UnitOfMeasurement.UNIT))));
			   invoice.setTrade(trade);
			   
			   PdfHandler handler = new PdfHandler();    
			   
			   InputStream inputPdf;
					   
			   OutputStream resultingPdf;
			try {
				inputPdf = new FileInputStream("C:\\Users\\jstaerk\\Downloads\\MustangGnuaccountingBeispielRE-20140628_502blanko.pdf");
				resultingPdf = new FileOutputStream("C:\\Users\\jstaerk\\Downloads\\MustangGnuaccountingBeispielRE-20140628_502konik.pdf");
				   handler.appendInvoice(invoice, inputPdf, resultingPdf);     
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
		}
	});
	btnNewButton.setText(Messages.getString("spareWindow.btnNewButton.text")); //$NON-NLS-1$
}
new Label(container, SWT.NONE);
{
	Button btnRead = new Button(container, SWT.NONE);
	btnRead.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			PDFBoxInvoiceExtractor x=new PDFBoxInvoiceExtractor();
			try {
				byte [] bytes=x.extract(new FileInputStream("C:\\Users\\jstaerk\\Downloads\\MustangGnuaccountingBeispielRE-20140628_502konik.pdf"));
				System.err.println(bytes);
				
				InputStream bis = x.extractToStream(new FileInputStream("C:\\Users\\jstaerk\\Downloads\\MustangGnuaccountingBeispielRE-20140628_502konik.pdf"));
				  final char[] buffer = new char[1024];
				  final StringBuilder out = new StringBuilder();
				  try {
				    final Reader in = new InputStreamReader(bis, "UTF-8");
				    try {
				      for (;;) {
				        int rsz = in.read(buffer, 0, buffer.length);
				        if (rsz < 0)
				          break;
				        out.append(buffer, 0, rsz);
				      }
				    }
				    finally {
//				      in.close();
				    }
				  }
				  catch (UnsupportedEncodingException ex) {
				    * ... *
				  }
				  catch (IOException ex) {
				      * ... *
				  }

				System.err.println(out.toString());
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	});
	btnRead.setText(Messages.getString("spareWindow.btnRead.text")); //$NON-NLS-1$
}
		*/
/* headless calc augment test
 			Button btnNewButton = new Button(container, SWT.NONE);
			btnNewButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					IOfficeApplication officeApplication = configs
							.getOfficeApplication();


				try{

System.err.println("deb ld");
					
					IDocument document = officeApplication.getDocumentService()
							.loadDocument(null,"C:\\Users\\jstaerk\\temp\\2014_eingangsbuch.ods");
					System.err.println("deb fld");

					ISpreadsheetDocument spreadsheet = (ISpreadsheetDocument)document;


					XSpreadsheets spreadsheets = spreadsheet.getSpreadsheetDocument().getSheets();
					

					// get the first table
					XSpreadsheet spreadsheet1 = (XSpreadsheet) UnoRuntime.queryInterface(
							XSpreadsheet.class,
							spreadsheets.getByName(spreadsheets.getElementNames()[0]));

					XSheetCellCursor cellCursor = spreadsheet1.createCursor();
					XCell cell = cellCursor.getCellByPosition(0, 0);
					XText cellText = (XText) UnoRuntime.queryInterface(
							XText.class, cell);
					
System.err.println("deb 0 0 is "+cellText.getText().getString());
					

					
				} catch (OfficeApplicationException e1) {
					Logger.getLogger(spareWindow.class.getName()).log(
							Level.SEVERE,
							"Exception", e1);

				} catch (NoSuchElementException e1) {
					Logger.getLogger(spareWindow.class.getName()).log(
							Level.SEVERE,
							"Exception", e1);

				} catch (WrappedTargetException e1) {
					Logger.getLogger(spareWindow.class.getName()).log(
							Level.SEVERE,
							"Exception", e1);

				} catch (DocumentException e1) {
					Logger.getLogger(spareWindow.class.getName()).log(
							Level.SEVERE,
							"Exception", e1);

				} catch (IndexOutOfBoundsException e1) {
					Logger.getLogger(spareWindow.class.getName()).log(
							Level.SEVERE,
							"Exception", e1);

				}

					
				}
			});
			btnNewButton.setText(Messages.getString("spareWindow.btnNewButton.text")); //$NON-NLS-1$
		}


 */
	
	
	
	/**
	 * Create contents of the application window.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		GridLayout gl_container = GridLayoutFactory.fillDefaults().numColumns(2)
				.margins(10, 5).create();
		gl_container.numColumns = 3;
		container.setLayout(gl_container);
		

		
		StyledText styledText = new StyledText(container, SWT.BORDER);
		styledText.setText(Messages
				.getString("spareWindow.intentionallyLeftBlank")); //$NON-NLS-1$

		GridDataFactory.fillDefaults().span(2, 1).grab(true, true)
				.applyTo(styledText);
		
		/*
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		{
			Button btnNewButton = new Button(container, SWT.NONE);
			btnNewButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					IOfficeApplication officeApplication = configs
							.getOfficeApplication();

					IFrame officeFrame;

				
						officeFrame = null;

	

					// get the first table
					try {
						

ISpreadsheetDocument  document = (ISpreadsheetDocument) officeApplication.getDocumentService().loadDocument(officeFrame, "c:\\users\\jstaerk\\downloads\\2015_eingangsbuch.ods");
					if (officeFrame == null) {
						officeFrame = document.getFrame();
					}


					XSpreadsheets spreadsheets = document.getSpreadsheetDocument().getSheets();

						XSpreadsheet spreadsheet1 = (XSpreadsheet) UnoRuntime.queryInterface(
								XSpreadsheet.class,
								spreadsheets.getByName(spreadsheets.getElementNames()[0]));
						
						XCell xc=spreadsheet1.getCellByPosition(1, 1);
						XSheetCellCursor cellCursor = spreadsheet1.createCursor();
						
						short newIndex = (short) (spreadsheets.getElementNames().length + 1);
						XCell cell = cellCursor.getCellByPosition(3, 1);
						XText cellText = (XText) UnoRuntime.queryInterface(XText.class, cell);

						System.err.println(cellText.getString());
						// Now it is time to disable two commands in the frame
						officeFrame.disableDispatch(GlobalCommands.CLOSE_DOCUMENT);
						officeFrame.disableDispatch(GlobalCommands.QUIT_APPLICATION);
						officeFrame.updateDispatches();
						
						officeFrame.close();
						document.close();

					} catch (NoSuchElementException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (WrappedTargetException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (DocumentException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (OfficeApplicationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IndexOutOfBoundsException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

		
				}
			});
			btnNewButton.setText(Messages.getString("spareWindow.btnNewButton.text")); //$NON-NLS-1$
		}
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
	*/
		return container;
	}
	
	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * 
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu"); //$NON-NLS-1$
		return menuManager;
	}

	/**
	 * Create the toolbar manager.
	 * 
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * 
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			spareWindow window = new spareWindow();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
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
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(750, 800);
	}
}
