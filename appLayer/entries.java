package appLayer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import GUILayer.accountingEditWindow;
import GUILayer.todoWindow;

import dataLayer.DB;

public class entries implements IStructuredContentProvider, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConcurrentHashMap journal = new ConcurrentHashMap();
	private ArrayList<entry> periodEntries = new ArrayList<entry>();
	private boolean isLoaded = false;
	private Calendar displaySince = null;
	private Calendar displayTo = null;
	private HashMap<account, BigDecimal> balances = new HashMap<account, BigDecimal>();
	private HashMap<account, BigDecimal> openingBalances = new HashMap<account, BigDecimal>();
	private Date firstEntry;
	private Date lastEntry;

	public entries() {
		Calendar cal = Calendar.getInstance();

		firstEntry = cal.getTime();
		lastEntry = cal.getTime();
	}

	/**
	 * returns a calendar with the date of the first entry in the journal
	 * */
	public Date getCompletePeriodStart() {
		Calendar cal = Calendar.getInstance();

		Date startDate = cal.getTime();
		for (entry currentEntry : client.getEntries().getJournal(false)) {
			if (currentEntry.getDate().getTime() < startDate.getTime()) {
				startDate = currentEntry.getDate();
			}
		}
		return startDate;
	}

	/**
	 * returns a calendar with the date of the last entry in the journal
	 * */
	public Date getCompletePeriodEnd() {
		Calendar cal = Calendar.getInstance();

		Date endDate = cal.getTime();
		for (entry currentEntry : client.getEntries().getJournal(false)) {
			if (currentEntry.getDate().getTime() > endDate.getTime()) {
				endDate = currentEntry.getDate();
			}
		}
		return endDate;
	}

	/**
	 * returns an array of string with the years in which entries have been
	 * booked
	 */
	public String[] getYearsCovered() {
		Vector<Integer> years = new Vector<Integer>();
		for (entry currentEntry : client.getEntries().getJournal(false)) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(currentEntry.getDate());
			Integer currentYear = cal.get(Calendar.YEAR);
			if (!years.contains(currentYear)) {
				years.add(currentYear);
			}
		}

		String[] results = new String[years.size()];
		for (int i = 0; i < years.size(); i++) {
			results[i] = years.get(i).toString();
		}
		return results;
	}

	/**
	 * update an existing entry in the journal or add a new one
	 * */
	public void updateEntryInJournal(entry currentEntry) {
		if (currentEntry.getDate().getTime() < firstEntry.getTime()) {
			firstEntry = currentEntry.getDate();
		}
		if (currentEntry.getDate().getTime() > lastEntry.getTime()) {
			lastEntry = currentEntry.getDate();
		}

		journal.put(currentEntry.getID(), currentEntry);
	}

	public void removeEntry(entry currentEntry) {
		journal.remove(currentEntry.getID());
		periodEntries.remove(currentEntry);
	}

	public HashMap<account, BigDecimal> getBalances() {
		return balances;
	}

	/**
	 * discards all existing entries and loads the entries, opening and closing
	 * balances for the set period from the database
	 * */
	public void getEntriesFromDatabase() {

		/*
		 * calculate opening balance: sum of credit account payments on that
		 * account ./. sum of debit account payments on that account before the
		 * given date
		 */
		int entryIndex = 1;

		// here previous entries should be added to get the correct ID if a
		// period restriction is done
		/*
		 * now get all entries in given period
		 */
		journal.clear();
		// issuedate>="+periodStartSQL+" AND issuedate<="+periodEndSQL+"
		List<entry> retrievals = DB
				.getEntityManager()
				.createQuery("SELECT e FROM entry e WHERE e.outdated IS NULL").getResultList(); //$NON-NLS-1$
		for (Iterator<entry> iter = retrievals.iterator(); iter.hasNext();) {
			entry currentlyRetrieved = (entry) iter.next();
			currentlyRetrieved.setNumber(entryIndex);
			updateEntryInJournal(currentlyRetrieved);
			entryIndex++;
		}
		isLoaded = true;
		setFullPeriod(true);

	}

	/**
	 * returns all entries of all accounts. If restrictToPeriod is set it
	 * returns only this in the period selected by setPeriod (including the
	 * start, excluding the end date) openingbalances are calculated here and in
	 * getJournalForAccount
	 */
	public Collection<entry> getJournal(boolean restrictToPeriod) {
		if (restrictToPeriod) {
			return periodEntries;
		} else {
			return journal.values();
		}
	}

	public entry getEntryForID(int id) {
		if (!isLoaded) {
			getEntriesFromDatabase();
		}
		return (entry)journal.get(Integer.valueOf(id));
	}

	public boolean isJournalLoaded() {
		return (journal != null) && (journal.size() > 0);
	}

	@Override
	public Object[] getElements(Object arg0) {
		if (!isLoaded) {
			getEntriesFromDatabase();
		}
		if (displaySince == null) {
			return journal.values().toArray();
		} else {

			return periodEntries.toArray();
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	public void filerByDate() {

		Date start = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
		Date end = null;
		try {
			if (!configs.getDisplaySince().equals("")) { //$NON-NLS-1$
				start = sdf.parse(configs.getDisplaySince());

			}
			if (!configs.getDisplayTo().equals("")) { //$NON-NLS-1$
				end = sdf.parse(configs.getDisplayTo());
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setPeriod(start, end, true);
	}

	public int getFirstYearWithEntries() {
		if (!isLoaded) {
			getEntriesFromDatabase();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(firstEntry);
		return cal.get(Calendar.YEAR);
	}

	public int getLastYearWithEntries() {
		if (!isLoaded) {
			getEntriesFromDatabase();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastEntry);
		return cal.get(Calendar.YEAR);

	}

	public void setPeriod(Date start, Date end, boolean signalChange) {
		// the accounts should reset their journal caches here
		if (start == null) {
			displaySince = null;
		} else {
			Calendar c = Calendar.getInstance();
			c.setTime(start);
			displaySince = c;
		}
		if (end == null) {
			displayTo = null;
		} else {
			Calendar d = Calendar.getInstance();
			d.setTime(end);
			displayTo = d;
		}

		if (signalChange) { 
			signalChange();
		}
	}

	/**
	 * something has been changed in a period which the user is likely to
	 * monitor, this will refresh the cache and some potentially open windows
	 * */
	public void signalChange() {
		for (account currentAccount : client.getAccounts().getCurrentChart().accounts) {
			currentAccount.resetBalance();
		}

		periodEntries.clear();

		SimpleDateFormat sdf = new SimpleDateFormat(
				Messages.getString("entries.dateFormat")); //$NON-NLS-1$

		String fromStr = sdf.format(displaySince.getTime());// it's hard to just
															// compare days with
															// getDate.after()
															// and
															// getDate.before()
		String toStr = sdf.format(displayTo.getTime());
		
		if (configs.getDisplaySince()==null||configs.getDisplayTo()==null||(displaySince==null)&&(displayTo==null)) {
			fromStr = "0001-01-01"; //$NON-NLS-1$
			toStr = "9999-12-31"; //$NON-NLS-1$
		}


		Vector<entry> completeJournal = new Vector<entry>();

		for (Object currentEntry : journal.values()) {
			
			completeJournal.add((entry)currentEntry);

		}
		Collections.sort(completeJournal, new Comparator<entry>() {
			public int compare(entry o1, entry o2) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
				return sdf.format(o1.getDate()).compareTo(
						sdf.format(o2.getDate()));
			}
		});

		for (entry currentEntry : completeJournal) {
			String currentStr = sdf.format(currentEntry.getDate());

			if ((currentStr.compareTo(fromStr) >= 0)
					&& (currentStr.compareTo(toStr) < 0)) {
				periodEntries.add(currentEntry);
				currentEntry.getDebitAccount().addAccountBalance(
						currentEntry.getValue(), true);
				currentEntry.getCreditAccount().addAccountBalance(
						currentEntry.getValue(), false);
			}
			if (currentStr.compareTo(fromStr) < 0) {
				currentEntry.getDebitAccount().addOpeningBalance(
						currentEntry.getValue(), true);
				currentEntry.getCreditAccount().addOpeningBalance(
						currentEntry.getValue(), false);

			}
		}
		Collections.sort(periodEntries, new Comparator<entry>() {
			public int compare(entry o1, entry o2) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
				return sdf.format(o1.getDate()).compareTo(
						sdf.format(o2.getDate()));
			}
		});

		// non cache related
		accountingEditWindow.refreshAccountingEntries(); // the accounting
															// window might be
															// open--refresh it

		todoWindow.refreshToDoList(); // we might have booked a payment

	}

	public void setFullPeriod(boolean signalChange) {

		Calendar d = Calendar.getInstance();
		d.setTime(getCompletePeriodEnd());
		d.add(Calendar.DAY_OF_MONTH, 1); // the period end is the first day no
											// longer included, so add one day
											// here

		setPeriod(getCompletePeriodStart(), d.getTime(), signalChange);

	}

	public Date getStart() {
		if (displaySince == null) {
			return null;
		}
		return displaySince.getTime();
	}

	public Date getEnd() {
		if (displayTo == null) {
			return null;
		}
		return displayTo.getTime();
	}

	public boolean isEmpty() {
		if (!isLoaded) {
			getEntriesFromDatabase();
		}
		return getJournal(false).size() == 0;
	}

	public int getHighestNumber() {
		int highestNumber = -1;
		for (entry currenEntry : getJournal(false)) {

			try {
				if (currenEntry.getNumber() > highestNumber) {
					highestNumber = currenEntry.getNumber();
				}
			} catch (entry.undefinedNumberException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return highestNumber;
	}

	public void removeAutoVATs() {		
		Iterator<entry> iter = periodEntries.iterator();
			while (iter.hasNext()) {
					
				entry currentEntry = iter.next();
				try {
					if ((currentEntry.getDebitAccount().equals(client.getAccounts().getInputTaxAccount()))||(currentEntry.getDebitAccount().equals(client.getAccounts().getTurnoverTaxAccount()))
							|| (currentEntry.getCreditAccount().equals(client.getAccounts().getInputTaxAccount()))||(currentEntry.getCreditAccount().equals(client.getAccounts().getTurnoverTaxAccount()))) {
						// this is something VATish
						for (entry otherEntry : currentEntry.getReferredTransaction().getEntries()) {
							if (!(otherEntry.equals(currentEntry))&& (otherEntry.getCreditAccount().isAutoVAT()||otherEntry.getDebitAccount().isAutoVAT())) {
								// and another entry of the same transaction is referring to some auto VAT
								// --> remove the entry
								iter.remove();
								continue;
							}
						} 
						
					}
				} catch (AccountNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
			}
		
	
	}
}