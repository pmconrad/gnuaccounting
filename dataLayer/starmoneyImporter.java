package dataLayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import appLayer.client;

public class starmoneyImporter extends Thread implements IRunnableWithProgress {

	private BufferedReader bufferedImportReader = null;

	/** Construct a regex-based CSV parser. */
	public starmoneyImporter(String filename) {
		File importFile = new File(filename);

		try {
			if (!importFile.exists()) {
				throw new FileNotFoundException();
			}
			FileReader importReader = new FileReader(importFile);
			bufferedImportReader = new BufferedReader(importReader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Process one file. Delegates to parse() a line at a time
	 * 
	 * @throws invalidImportFormatException
	 */
	public void run(IProgressMonitor ipm) {
		String line;

		int lineIndex = 0;
		// For each line...
		try {
			while ((line = bufferedImportReader.readLine()) != null) {
				if (isInterrupted())
					break;
				String[] stringList = fileUtils.parseCSVLineSemicolon(line);
				if (stringList.length != 33) {
					throw new invalidImportFormatException();
				}
				/*
				 * the 33 starmoney fields (txt export starmoney v5.0) are
				 * Saldo(
				 * 1)|SdoWaehr(2)|AgBlz(3)|AgKto(4)|AgName1(5)|Storno(6)|OrigBtg
				 * (
				 * 7)|Betrag(8)|BtgWaehr(9)|OCMTBetr(10)|OCMTWaehr(11)|Textschl(
				 * 12
				 * )|VWZ1(13)|VWZ2(14)|VWZ3(15)|VWZ4(16)|VWZ5(17)|VWZ6(18)|VWZ7
				 * (19
				 * )|VWZ8(20)|VWZ9(21)|VWZ10(22)|VWZ11(23)|VWZ12(24)|VWZ13(25)
				 * |VWZ14
				 * (26)|BuchDatum(27)|WertDatum(28)|Primanota(29)|Kategorie
				 * (30)|Unterkat(31)|Kostenst(32)|BuchText(33)| our import cache
				 * field are
				 * description(1)|subjectName(2)|subjectBank(3)|subjectAccount
				 * (4)|subjectBankCode(5)|date(6)|value(7) So the mappings are
				 * (13..26)->(1) (5)->(2), (3)->(3), (4)->(4), (3)->(5),
				 * (28)->(6), (8)->(7)
				 */
				if (lineIndex > 0) { // first line are the column headings

					String description = ""; //$NON-NLS-1$
					for (int purposeIndex = 12; purposeIndex < 26; purposeIndex++) {
						if (stringList[purposeIndex] != null) {
							description = description
									+ "@" + stringList[purposeIndex]; //$NON-NLS-1$
						}

					}
					if (description.length() > 0) {
						description = description.substring(1);
					}

					String name = stringList[4];
					String bank = stringList[2];
					String account = stringList[3];
					String bankcode = stringList[2];
					String amount = stringList[7];
					String date = stringList[27];
					String[] dateParts = date.split("\\.");// an ordinary explode would do (no regex required) but java doesnt seem to provide one //$NON-NLS-1$
					String day = dateParts[0];
					if (day.length() < 2) {
						day = "0" + day; //$NON-NLS-1$
					}
					String month = dateParts[1];
					if (month.length() < 2) {
						month = "0" + month; //$NON-NLS-1$
					}
					String year = dateParts[2];

					String ISOdate = year + "-" + month + "-" + day; //$NON-NLS-1$ //$NON-NLS-2$
					amount = amount.replaceAll(",", "\\."); //$NON-NLS-1$ //$NON-NLS-2$
					SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
					Date when;
					try {
						when = ISO.parse(ISOdate);
						client.getImportQueue()
								.add(description, name, bank, account,
										bankcode, when, new BigDecimal(amount));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				lineIndex++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (invalidImportFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
