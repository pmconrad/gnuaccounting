package appLayer.taxRelated;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import appLayer.AccountNotFoundException;
import appLayer.client;
import appLayer.entry;
import appLayer.utils;

public class taxCalculator {

	private BigDecimal grossIncome = null, grossSpendings = null,
			netIncome = null, netSpendings = null;

	private HashMap<Integer, BigDecimal> fieldValues = new HashMap<Integer, BigDecimal>();
	private BigDecimal VATtoPay = new BigDecimal(0);

	public taxCalculator() {
	}

	/**
	 * calculations for VAT announcement
	 * */

	/**
	 * Fields 81 and 86 need the net turnover, not the balance field 83
	 * (remaining VAT to be payed) has to be calculated
	 * */
	private void finishFieldValues() {
		tax field81 = null;
		tax field86 = null;
		field81 = client.getTaxes().getVATforFieldID(81);
		field86 = client.getTaxes().getVATforFieldID(86);
		if (field81 != null) {
			BigDecimal taxValue = fieldValues.get(81);
			if (taxValue != null) {
				BigDecimal turnover = taxValue.divide(field81.getFactor()
						.subtract(new BigDecimal(1)));
				fieldValues.put(81, turnover);

			}
		}
		if (field86 != null) {
			BigDecimal taxValue = fieldValues.get(86);
			if (taxValue != null) {
				BigDecimal turnover = taxValue.divide(field86.getFactor()
						.subtract(new BigDecimal(1)));
				fieldValues.put(86, turnover);

			}
		}

		fieldValues.put(83, VATtoPay);

	}

	private String getValueForField(int field, BigDecimal value, tax VAT) {
		String res = ""; //$NON-NLS-1$
		DecimalFormat def = null;
		BigDecimal turnover = new BigDecimal(0);
		switch (field) {
		case 66:
			def = new DecimalFormat("#0.00"); //$NON-NLS-1$
			res = def.format(value.abs());
			VATtoPay = VATtoPay.subtract(value);
			res = res.replace('.', ',');
			break;
		case 81:// 19%
			/**
			 * we have the amount of tax. First we calculate the net value e.g.
			 * (value/0.19) with a precision of 2 digits, then we floor the
			 * value (in german tax it is allowed to floor there). If we floored
			 * right away it could happen that e.g. with 7% tax
			 * 7/0.07=99.9999999999999 (instead of 100.00) which would floor to
			 * 99 instead of 100
			 */
			turnover = utils.round(value.divide(VAT.getValue()), 2);
			VATtoPay = VATtoPay.add(value);
			def = new DecimalFormat("#0"); // turnover we need in full euros only //$NON-NLS-1$
			res = def.format(turnover);
			break;
		case 86:// 7%
			// turnover calculation: see comments above
			turnover = utils.round(value.divide(VAT.getValue()), 2);
			VATtoPay = VATtoPay.add(value);
			def = new DecimalFormat("#0"); // turnover we need in full euros only //$NON-NLS-1$
			res = def.format(turnover);
			break;
		}
		return res;
	}

	public void calculateVAT(int monthsToCover, Calendar c) {
		Date start = c.getTime();
		c.add(Calendar.MONTH, monthsToCover);
		Date end = c.getTime();

		Date originalStart = client.getEntries().getStart();
		Date originalEnd = client.getEntries().getEnd();

		client.getEntries().setPeriod(start, end, false);

		/*
		 * it is possible that 2 or more taxes share the same tax ids (e.g. the
		 * german 19% VAT and the predecessor, 16% VAT). This vector will allow
		 * us to process theses fields only once.
		 */

		for (entry currentEntry : client.getEntries().getJournal(true)) {
			if ((currentEntry.getDate().getTime() >= start.getTime())
					&& (currentEntry.getDate().getTime() <= end.getTime())) {
				if (currentEntry.getCreditAccount().getTaxField() != null) {
					BigDecimal currentBalance = fieldValues.get(currentEntry
							.getCreditAccount().getTaxField());
					if (currentBalance == null) {
						currentBalance = new BigDecimal(0);
					}
					currentBalance = currentBalance
							.add(currentEntry.getValue());// remove when BD
															// migration
															// complete
					VATtoPay = VATtoPay.add(currentEntry.getValue());// remove
																		// when
																		// BD
																		// migration
																		// complete
					fieldValues.put(currentEntry.getCreditAccount()
							.getTaxField(), currentBalance);
				}
				if (currentEntry.getDebitAccount().getTaxField() != null) {
					BigDecimal currentBalance = fieldValues.get(currentEntry
							.getDebitAccount().getTaxField());
					if (currentBalance == null) {
						currentBalance = new BigDecimal(0);
					}
					currentBalance = currentBalance
							.add(currentEntry.getValue());// remove when BD
															// migration
															// complete
					VATtoPay = VATtoPay.subtract(currentEntry.getValue());// remove
																			// when
																			// BD
																			// migration
																			// complete
					fieldValues.put(currentEntry.getDebitAccount()
							.getTaxField(), currentBalance);
				}

			}

		}
		finishFieldValues();

		client.getEntries().setPeriod(originalStart, originalEnd, false);

	}

	public void writeVATExportFile(String orderNumber, String year,
			String periodPart, String filename) {
		String fieldsAndValues = ""; //$NON-NLS-1$
		DecimalFormat def = new DecimalFormat("#0.00"); //$NON-NLS-1$
		for (Integer key : fieldValues.keySet()) {

			String res = def.format(fieldValues.get(key));
			res = res.replace('.', ',');

			fieldsAndValues += "<Kennzahl nr=\"Kz" + key + "\">" + res + "</Kennzahl>\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		File exportFile = new File(filename);
		FileWriter fw;
		try {
			fw = new FileWriter(exportFile);
			fw.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" + //$NON-NLS-1$
					"<WinstonAusgang>" + //$NON-NLS-1$
					"<Formular Typ=\"UST\"></Formular>" + //$NON-NLS-1$
					"<Ordnungsnummer>" + orderNumber + "</Ordnungsnummer>" + //$NON-NLS-1$ //$NON-NLS-2$
					"<AnmeldeJahr>" + year + "</AnmeldeJahr>" + //$NON-NLS-1$ //$NON-NLS-2$
					"<AnmeldeZeitraum>" + periodPart + "</AnmeldeZeitraum>" + //$NON-NLS-1$ //$NON-NLS-2$
					fieldsAndValues + "</WinstonAusgang>"); //$NON-NLS-1$
			fw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * calculations for excel spread sheet report
	 * */
	private void calcIncomeSpendings() {
		grossIncome = new BigDecimal(0);
		grossSpendings = new BigDecimal(0);
		netIncome = new BigDecimal(0);
		netSpendings = new BigDecimal(0);

		for (entry currentEntry : client.getEntries().getJournal(true)) {
			if (currentEntry.getCreditAccount().isIncomeAccount()) {

				if (currentEntry.getValue().compareTo(new BigDecimal(0)) > -1) {
					// income
					netIncome = netIncome.add(currentEntry.getValue());
					grossIncome = grossIncome.add(currentEntry.getValue());

				}
			}

			if (currentEntry.getDebitAccount().isExpenseAccount()) {
				if (currentEntry.getValue().compareTo(new BigDecimal(0)) > -1) {

					// spending
					netSpendings = netSpendings.add(currentEntry.getValue());
					grossSpendings = grossSpendings
							.add(currentEntry.getValue());
				}
			}
			try {

				if (currentEntry.getCreditAccount().getCode() == client
						.getAccounts().getTurnoverTaxAccount().getCode()) {
					grossIncome = grossIncome.add(currentEntry.getValue());

				}
				if (currentEntry.getDebitAccount().getCode() == client
						.getAccounts().getInputTaxAccount().getCode()) {
					grossSpendings = grossSpendings
							.add(currentEntry.getValue());

				}
			} catch (AccountNotFoundException ex) {
				ex.printStackTrace();
			}
		}

	}

	/**
	 * calculations for excel spread sheet report
	 * */
	public BigDecimal getGrossIncome() {
		if (grossIncome == null) {
			calcIncomeSpendings();
		}
		return grossIncome;
	}

	/**
	 * calculations for excel spread sheet report
	 * */
	public BigDecimal getNetIncome() {
		if (netIncome == null) {
			calcIncomeSpendings();
		}
		return netIncome;
	}

	/**
	 * calculations for excel spread sheet report
	 * */
	public BigDecimal getGrossSpendings() {
		if (grossSpendings == null) {
			calcIncomeSpendings();
		}
		return grossSpendings;

	}

	/**
	 * calculations for excel spread sheet report
	 * */
	public BigDecimal getNetSpendings() {
		if (netSpendings == null) {
			calcIncomeSpendings();
		}
		return netSpendings;

	}
}
