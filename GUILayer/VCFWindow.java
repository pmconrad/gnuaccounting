package GUILayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardOutputter;
import net.fortuna.ical4j.vcard.parameter.Type;
import net.fortuna.ical4j.vcard.property.Address;
import net.fortuna.ical4j.vcard.property.Email;
import net.fortuna.ical4j.vcard.property.Fn;
import net.fortuna.ical4j.vcard.property.N;
import net.fortuna.ical4j.vcard.property.Revision;
import net.fortuna.ical4j.vcard.property.Telephone;
import net.fortuna.ical4j.vcard.property.Version;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.contact;
import dataLayer.VCFinterface;

public class VCFWindow extends ApplicationWindow {

	/**
	 * Create the application window.
	 */
	public VCFWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	/**
	 * Create contents of the application window.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		TabFolder tabFolder = new TabFolder(container, SWT.NONE);

		TabItem tbtmImportVcf = new TabItem(tabFolder, SWT.NONE);

		tbtmImportVcf.setText(Messages.getString("VCFWindow.tabHeadingImport")); //$NON-NLS-1$

		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmImportVcf.setControl(composite);

		Button btnImport = new Button(composite, SWT.NONE);
		btnImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(),
						SWT.OPEN | SWT.MULTI);
				String name = dialog.open();
				if (name != null) {
					VCFinterface importer = new VCFinterface();
					importer.setSrcVcfDir(new File(name));
					try {
						importer.init();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					importer.doImport();

				}

			}
		});
		btnImport.setBounds(37, 42, 90, 30);
		btnImport.setText(Messages.getString("VCFWindow.buttonImport")); //$NON-NLS-1$
		btnImport.setFont(configs.getDefaultFont());

		TabItem tbtmExportVcf = new TabItem(tabFolder, SWT.NONE);
		tbtmExportVcf.setText(Messages.getString("VCFWindow.tabHeadingExport")); //$NON-NLS-1$

		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmExportVcf.setControl(composite_1);

		Button btnExport = new Button(composite_1, SWT.NONE);
		btnExport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(),
						SWT.SAVE);
				String name = dialog.open();
				if (name != null) {
					List<net.fortuna.ical4j.vcard.Property> props = null;

					int contactIdx = 0;
					for (contact currentContact : client.getContacts()
							.getContacts()) {
						contactIdx++;
						props = new ArrayList<net.fortuna.ical4j.vcard.Property>();
						props.add(new Revision(
								new net.fortuna.ical4j.model.Date()));
						props.add(new Version("3.0")); //$NON-NLS-1$
						props.add(new Fn("")); //$NON-NLS-1$
						props.add(new N(currentContact.getName(), null, null,
								null, null));
						props.add(new Email(currentContact.getEmail()));
						props.add(new Address("", currentContact //$NON-NLS-1$
								.getAdditionalAddressLine(), currentContact
								.getStreet(), currentContact.getLocation(), "", //$NON-NLS-1$
								currentContact.getZIP(), currentContact
										.getCountry(), Type.WORK));

						try {
							props.add(new Telephone(new ArrayList<Parameter>(),
									currentContact.getPhone()));
						} catch (URISyntaxException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						props.add(new Email(currentContact.getEmail()));

						VCard vcard = new VCard(props);
						VCard v = new VCard(props);
						VCardOutputter vo = new VCardOutputter();

						try {
							FileWriter fw = new FileWriter(new File(name
									+ File.separator
									+ Integer.toString(contactIdx) + ".vcf")); //$NON-NLS-1$
							vo.output(v, fw);
							fw.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (ValidationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					}

				}

			}
		});
		btnExport.setBounds(41, 41, 90, 30);
		btnExport.setText(Messages.getString("VCFWindow.buttonExport")); //$NON-NLS-1$
		btnExport.setFont(configs.getDefaultFont());

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
			VCFWindow window = new VCFWindow();
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
		newShell.setText(application.getAppName()); //$NON-NLS-1$
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
}
