package dataLayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import GUILayer.Messages;
import appLayer.client;
import appLayer.utils;
import appLayer.transactionRelated.appTransaction;

public class CSVImporter extends Thread implements IRunnableWithProgress {

	private String filename;

	public CSVImporter(String filename) {
		this.filename = filename;

	}

	@Override
	public void run(IProgressMonitor ipm) throws InvocationTargetException,
			InterruptedException {
		ipm.beginTask(
				Messages.getString("newAccountingWizardImport.reading") + filename, 100); //$NON-NLS-1$
		File importFile = new File(filename);
		FileReader importReader;
		try {
			importReader = new FileReader(importFile);
			BufferedReader bufferedImportReader = new BufferedReader(
					importReader);

			appTransaction inv = null;
			String physLine;
			boolean firstLine = true;
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$

			String logLine = "";// there is a physical and a logical line, since a CSV file can contain linefeeds in quotes of elements //$NON-NLS-1$
			while ((physLine = bufferedImportReader.readLine()) != null) {
				logLine += physLine;
				if (logLine.endsWith(";\"Umsatz\";\" \"") || logLine.endsWith(";\"H\"") || logLine.endsWith(";\"S\"")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					if (!firstLine) {// first logical line (some physical ones)
										// has headers only
						String[] elements = fileUtils
								.parseCSVLineSemicolon(logLine);
						int idx = 0;
						for (String currentElem : elements) {

							if (currentElem == null) {
								elements[idx] = ""; //$NON-NLS-1$
							}
							idx++;

						}
						if (!elements[7].equals("Anfangssaldo") && !elements[7].equals("Endsaldo") && !elements[6].startsWith("ABSCHLUSSABSCHLUSS PER ")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

							try {
								Date when = sdf.parse(elements[0]);
								BigDecimal value = utils.String2BD(elements[8]);// value
																				// is
																				// german
																				// notation
																				// with
																				// comma,
																				// thus
																				// use
																				// our
																				// string2bd
								if (elements[9].equals("S")) {// S for german Soll, debit //$NON-NLS-1$
									value = value.negate();
								}
								String name = elements[3];
								String account = elements[4];
								String bankcode = elements[5];
								String description = elements[6];

								client.getImportQueue().add(description, name,
										"", account, bankcode, when, value); //$NON-NLS-1$
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}

					logLine = ""; //$NON-NLS-1$
					firstLine = false;
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ipm.worked(100);
		ipm.beginTask(
				Messages.getString("newAccountingWizardImport.importing") + filename, 101); //$NON-NLS-1$
		for (int bookingIndex = 0; bookingIndex < 101; bookingIndex++) {
			if (isInterrupted())
				break;
			ipm.worked(bookingIndex);

		}

	}

}
