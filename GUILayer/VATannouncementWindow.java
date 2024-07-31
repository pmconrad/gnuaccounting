package GUILayer;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;

import java.text.DecimalFormat;
import java.util.Calendar;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.taxRelated.IRSoffice;
import appLayer.taxRelated.IRSoffices;
import appLayer.taxRelated.state;
import appLayer.taxRelated.states;
import appLayer.taxRelated.taxCalculator;

public class VATannouncementWindow extends ApplicationWindow {

	private Combo cmbYear;
	private Combo cmbPeriod;

	/**
	 * Create the application window
	 */
	public VATannouncementWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	public boolean isOpenable() {
		if (client.getEntries().isEmpty()) {
			MessageDialog
					.openError(
							this.getShell(),
							Messages.getString("VATannouncementWindow.missingEntries"), Messages.getString("VATannouncementWindow.addAccounting")); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		return true;

	}

	private class exportAction extends SelectionAdapter {
		protected VATannouncementWindow parentWindow;

		public exportAction(VATannouncementWindow parentWindow) {
			this.parentWindow = parentWindow;
		}

		public void widgetSelected(SelectionEvent e) {
			states allStates = new states();
			if ((configs.getTaxOfficeListIDX() == -1)
					|| (configs.getStateListIDX() == -1)
					|| (configs.getTaxID().length() == 0)
					|| (configs.getOrganisationName().length() == 0)) {
				MessageDialog
						.openError(
								parentWindow.getShell(),
								Messages.getString("VATannouncementWindow.missingConfig"), Messages.getString("VATannouncementWindow.enterTaxDetails")); //$NON-NLS-1$ //$NON-NLS-2$
				settingsWindow sw = new settingsWindow();
				sw.open();
				parentWindow.close();
				return;
			}
			state currentState = allStates.getStates()[configs
					.getStateListIDX()];
			DecimalFormat formatter = new DecimalFormat("00"); //$NON-NLS-1$

			IRSoffice irsOffice = IRSoffices.getIRSOffices()[configs
					.getTaxOfficeListIDX()];
			String orderNumber = currentState.getOrderNumber(irsOffice);
			/*
			 * the order number is important and it depends on the format of
			 * your tax ID (which is dependent to your state=Bundesland) and the
			 * number of your local IRS office
			 */

			String yearPart = formatter.format(Integer.valueOf(cmbYear
					.getText()) % 100);
			int period = cmbPeriod.getSelectionIndex() + 1;
			int monthsToCover = 1;

			if (!configs.getVATPeriod().equals("monthly")) { //$NON-NLS-1$
				// quarterly delivery
				period += 40;
				monthsToCover = 3;
			}
			String periodPart = formatter.format(period);
			String path = configs.getWinstonPath();
			if (path == "") { //$NON-NLS-1$
				MessageDialog
						.openError(
								parentWindow.getShell(),
								Messages.getString("VATannouncementWindow.missingConfig"), Messages.getString("VATannouncementWindow.enterWinstonOutbox")); //$NON-NLS-1$ //$NON-NLS-2$
				configWindow cw = new configWindow();
				cw.show();
				parentWindow.close();
				return;
			}
			if ((!new File(path).exists()) || (!new File(path).isDirectory())) {
				MessageDialog
						.openError(
								parentWindow.getShell(),
								Messages.getString("VATannouncementWindow.missingConfig"), String.format(Messages.getString("VATannouncementWindow.WinstonOutputDirectoryMissingErrorText"), path)); //$NON-NLS-1$ //$NON-NLS-2$ 
				configWindow cw = new configWindow();
				cw.show();
				parentWindow.close();
				return;

			}

			String filename = path
					+ System.getProperty("file.separator") + "U" + periodPart + yearPart + orderNumber + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			// now we have the required data in place for an export, so go get
			// the values

			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, Integer.valueOf(cmbYear.getText()));
			c.set(Calendar.MONTH,
					Integer.valueOf(cmbPeriod.getSelectionIndex()));
			c.set(Calendar.DAY_OF_MONTH, 1);
			taxCalculator tc = new taxCalculator();
			tc.calculateVAT(monthsToCover, c);

			tc.writeVATExportFile(orderNumber, cmbYear.getText(), periodPart,
					filename);
			MessageDialog
					.openInformation(
							parentWindow.getShell(),
							Messages.getString("VATannouncementWindow.exportSuccessful"), Messages.getString("VATannouncementWindow.exportSuccessfulDetails")); //$NON-NLS-1$ //$NON-NLS-2$
			parentWindow.close();

		}

	} // end of private class

	/**
	 * Create contents of the application window
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());

		Composite container = new Composite(parent, SWT.NONE);
		final Button exportButton = new Button(container, SWT.NONE);
		exportButton.setFont(configs.getDefaultFont());
		exportButton.addSelectionListener(new exportAction(this));
		exportButton
				.setText(Messages.getString("VATannouncementWindow.export")); //$NON-NLS-1$
		exportButton.setBounds(139, 150, 86, 25);
		Calendar c = Calendar.getInstance();
		String currentYear = Integer.toString(c.get(Calendar.YEAR));
		int previousMonth = c.get(Calendar.MONTH) - 1;
		if (previousMonth < 0) {
			previousMonth = 11;
		}

		cmbPeriod = new Combo(container, SWT.READ_ONLY);
		cmbPeriod.setBounds(139, 77, 109, 24);
		cmbPeriod.setFont(configs.getDefaultFont());
		if (configs.getVATPeriod().equals(
				Messages.getString("VATannouncementWindow.quarterly"))) { //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.quarter1")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.quarter2")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.quarter3")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.quarter4")); //$NON-NLS-1$
			cmbPeriod.select(0);
		} else {
			cmbPeriod.add(Messages.getString("VATannouncementWindow.january")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.february")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.march")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.april")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.may")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.june")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.july")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.august")); //$NON-NLS-1$
			cmbPeriod
					.add(Messages.getString("VATannouncementWindow.september")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.october")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.november")); //$NON-NLS-1$
			cmbPeriod.add(Messages.getString("VATannouncementWindow.december")); //$NON-NLS-1$
			cmbPeriod.select(previousMonth);
		}

		final Label periodLabel = new Label(container, SWT.NONE);
		periodLabel.setAlignment(SWT.RIGHT);
		periodLabel.setText(Messages.getString("VATannouncementWindow.period")); //$NON-NLS-1$
		periodLabel.setBounds(10, 80, 109, 15);
		periodLabel.setFont(configs.getDefaultFont());
		cmbYear = new Combo(container, SWT.READ_ONLY);
		cmbYear.setBounds(254, 77, 67, 23);
		cmbYear.setFont(configs.getDefaultFont());

		cmbYear.setItems(client.getEntries().getYearsCovered());
		int indexSelectedYear = 0;
		int indexComparedYear = 0;
		for (String coveredYearString : client.getEntries().getYearsCovered()) {
			if (coveredYearString.equals(currentYear)) {
				indexSelectedYear = indexComparedYear;
			}
			indexComparedYear++;
		}
		cmbYear.select(indexSelectedYear);

		//
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
				Messages.getString("VATannouncementWindow.menu")); //$NON-NLS-1$
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
			VATannouncementWindow window = new VATannouncementWindow();
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
		return new Point(409, 294);
	}

}
