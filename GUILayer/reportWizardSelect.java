package GUILayer;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;

import appLayer.client;
import appLayer.configs;

public class reportWizardSelect extends WizardPage {

	private DateTime dteFrom = null;
	private DateTime dteTo = null;

	private Calendar calStart;
	private Calendar calEnd;

	protected reportWizard parentWizard = null;

	/**
	 * Create the wizard
	 */
	public reportWizardSelect(reportWizard parentWizard) {
		super(Messages.getString("reportWizardSelect.wizardPage")); //$NON-NLS-1$
		setTitle(Messages.getString("reportWizardSelect.reportWizard")); //$NON-NLS-1$
		setDescription(Messages.getString("reportWizardSelect.selectPeriod")); //$NON-NLS-1$
		this.parentWizard = parentWizard;

	}

	public Calendar getStart() {
		return calStart;
	}

	public Calendar getEnd() {
		return calEnd;
	}

	public void checkDatesValidity() {
		boolean pageComplete = true;
		updateStartEnd();

		if (!calStart.before(calEnd)) {
			pageComplete = false;
			setErrorMessage(Messages
					.getString("reportWizardSelect.periodEndToBeAfterStart")); //$NON-NLS-1$
		} else {
			setErrorMessage(null);
		}

		setPageComplete(pageComplete);
	}

	private void updateStartEnd() {
		calStart = new GregorianCalendar();

		calStart.set(Calendar.YEAR, dteFrom.getYear());
		calStart.set(Calendar.MONTH, dteFrom.getMonth());
		calStart.set(Calendar.DAY_OF_MONTH, dteFrom.getDay());

		calEnd = new GregorianCalendar();

		calEnd.set(Calendar.YEAR, dteTo.getYear());
		calEnd.set(Calendar.MONTH, dteTo.getMonth());
		calEnd.set(Calendar.DAY_OF_MONTH, dteTo.getDay());
	}

	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		//
		setControl(container);

		dteFrom = new DateTime(container, SWT.DATE | SWT.DROP_DOWN);
		// there is no onchange event, addselectionlistener seems only to fire
		// when the + or - button is touched
		dteFrom.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				checkDatesValidity();
			}
		});
		dteFrom.setFont(configs.getDefaultFont());
		dteFrom.setBounds(144, 38, 156, 24);

		dteTo = new DateTime(container, SWT.DATE | SWT.DROP_DOWN);
		dteTo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				checkDatesValidity();
			}
		});
		dteTo.setFont(configs.getDefaultFont());
		dteTo.setBounds(144, 79, 156, 24);

		Calendar cal = new GregorianCalendar();
		if (client.getEntries().getCompletePeriodStart() != null) {
			// is null when no entries are present
			cal.setTime(client.getEntries().getCompletePeriodStart());
		}
		dteFrom.setYear(cal.get(Calendar.YEAR));
		dteFrom.setMonth(cal.get(Calendar.MONTH));
		dteFrom.setDay(cal.get(Calendar.DAY_OF_MONTH));

		if (client.getEntries().getCompletePeriodStart() != null) {
			cal.setTime(client.getEntries().getCompletePeriodEnd());
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		dteTo.setYear(cal.get(Calendar.YEAR));
		dteTo.setMonth(cal.get(Calendar.MONTH));
		dteTo.setDay(cal.get(Calendar.DAY_OF_MONTH));

		final Label lblDateFrom = new Label(container, SWT.NONE);
		lblDateFrom.setAlignment(SWT.RIGHT);
		lblDateFrom.setFont(configs.getDefaultFont());
		lblDateFrom.setText(Messages.getString("reportWizardSelect.dateFrom")); //$NON-NLS-1$
		lblDateFrom.setBounds(10, 38, 128, 24);

		final Label lblDateTo = new Label(container, SWT.NONE);
		lblDateTo.setAlignment(SWT.RIGHT);
		lblDateTo.setFont(configs.getDefaultFont());
		lblDateTo.setText(Messages.getString("reportWizardSelect.dateTo")); //$NON-NLS-1$
		lblDateTo.setBounds(10, 79, 128, 24);

	}

	@Override
	public IWizardPage getNextPage() {

		updateStartEnd();

		Date start = getStart().getTime();

		Date end = getEnd().getTime();
		client.getEntries().setPeriod(start, end, true);
		return super.getNextPage();
	}
}
