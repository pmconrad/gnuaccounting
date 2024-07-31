package appLayer;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import appLayer.transactionRelated.transactionType;

import dataLayer.DB;

public class documents implements IStructuredContentProvider {
	private Vector<document> allDocuments = new Vector<document>();
	private Vector<Pattern> regexFormatMatchers = null;

	/**
	 * returns regular expressions that match the defined number formats. This
	 * is used e.g. in the bank account import to check if the purpose field
	 * contains a document reference in ones own format --> most likely the
	 * import is then supposed to refer to this transaction
	 * */
	private Vector<Pattern> getDocumentNumberMatchers() {
		if (regexFormatMatchers == null) {
			regexFormatMatchers = new Vector<Pattern>();
			// prepare the search for references, first go through all
			// formats and use a regex to create
			// a regular expression to match the texts with
			// (e.g. create from IN-<format:number/> to IN-\d+)
			transactionType[] types = client.getTransactions().getAllTypes();
			for (transactionType currentType : types) {
				// first
				String matchStr = currentType.getTypeFormat();
				Pattern p = Pattern.compile("<.*?>");// replace everything in curly //$NON-NLS-1$
														// brackets with at
														// least a
														// digit
				Matcher m = p.matcher(matchStr);
				StringBuffer sb = new StringBuffer(""); //$NON-NLS-1$

				boolean result = m.find();
				// Loop through and create a new String
				// with the replacements
				while (result) {
					m.appendReplacement(sb, "\\\\d+"); //$NON-NLS-1$
					result = m.find();
				}
				// Add the last segment of input to
				// the new String
				// m.appendTail(sb);
				regexFormatMatchers.add(Pattern.compile(sb.toString()));
			}

		}
		return regexFormatMatchers;
	}

	/*
	 * Find a document number matching a user defined format in a string and
	 * returns the result. Returns null if not found.
	 * 
	 * // search from the beginning of line, after sender(:), after a
	 * whitespache (\w) and as beginning of fields (the four "purpose" fields in
	 * german HBCI bank data are delimited by @) Samples that should be found
	 * (try e.g. http://www.gskinner.com/RegExr/) RE-<number:year/><number:month
	 * /><number:dayofmonth/>/<number:value/> should be converted to
	 * RE-\d+\d+\d+/\d+ and found in GULP CONSULTING SERVICES,GM:RE.NR.
	 * RE-20080301/00383@V. 01.03.08 /@PEV NR. 02-066690@UEBERWEISUNGSGUTSCHRIFT
	 * RE-20080301/00383@V. 01.03.08 GULP CONSULTING
	 * SERVICES,GM:RE-20080301/00383@V. 01.03.08 /@PEV NR.
	 * 02-066690@UEBERWEISUNGSGUTSCHRIFT
	 * 
	 * For the filename: test E-\d+ should be found in "E-537-111422.JPG" and
	 * "doc E-537.jpg" but not in "EN-537.jpg"
	 * 
	 * In german, when formats E-<number/> and <number:year/><number:month
	 * /><number:dayofmonth/>/<number:value/> are defined, from
	 * "any string containing RE-2012/12/12/105 and some other stuff"
	 * RE-2012/12/12/105 (and not its E-matching substring E-2012 should be
	 * returned
	 */
	public String recognizeDocumentNumberInString(String haystack) {
		Vector<String> results = new Vector<String>();
		int maxMatchingLength = 0;
		String bestRes = null;

		for (Pattern currentPattern : getDocumentNumberMatchers()) {
			Matcher m = currentPattern.matcher(haystack);
			m.reset();
			if (m.find()) {
				int patternStart = m.start();

				String res = haystack.substring(patternStart, m.end());
				bestRes = res;
				results.add(res);
				if (res.length() > maxMatchingLength) {
					maxMatchingLength = res.length();
				}
			}

		}

		if (results.size() > 1) {
			/*
			 * more than one match of document nr, assume the longer wins: e.g.
			 * in the german version default formats are incomingTransaction
			 * E-<number/> and invoice sth like RE-yyyy/mm/dd/number in which
			 * case a RE-2012/12/12/105 should be favored again it's substring
			 * matching E-<number/>, E-2012
			 */
			for (String currentResult : results) {
				if (currentResult.length() == maxMatchingLength) {
					bestRes = currentResult;
				}
			}
		}
		return bestRes;

	}

	public document getNewDocument(String importFilename) {
		document d = new document(importFilename);
		allDocuments.add(d);
		return d;
	}

	public document[] getAsArray() {
		document[] res = new document[allDocuments.size()];
		for (int docIndex = 0; docIndex < allDocuments.size(); docIndex++) {
			res[docIndex] = allDocuments.get(docIndex);
		}

		return res;
	}

	public void getDocumentsFromDatabase() {
		allDocuments.clear();

		List<document> retrievals = DB
				.getEntityManager()
				.createQuery(
						"SELECT d FROM document d WHERE d.outdated IS NULL").getResultList(); //$NON-NLS-1$
		for (Iterator<document> iter = retrievals.iterator(); iter.hasNext();) {
			document currentlyRetrieved = iter.next();
			allDocuments.add(currentlyRetrieved);
		}
	}

	public Object[] getElements(Object arg0) {
		return getAsArray();
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub
	}

	/**
	 * returns the document object for a transaction number, e.g.
	 * "RE-20081212-201"
	 */
	public document getDocumentForNumber(String number) {
		if ((number == null) || (number.length() == 0)) {
			return null;
		}
		for (document currentDoc : allDocuments) {
			if (currentDoc.getNumber().equals(number)) {
				return currentDoc;
			}
		}
		return null;

	}

	/**
	 * Returns a stringlist of all transacion numbers which have so far not yet
	 * been balanced, e.g. {"RE-20081212-201","RE-20081212-204"}
	 * */
	public String[] getUnlinkedNumbers() {
		getDocumentsFromDatabase();
		Vector<String> unlinked = new Vector<String>();

		for (document currentDoc : allDocuments) {
			if (!currentDoc.isLinked() && (currentDoc.getNumber().length() > 0)) {
				unlinked.add(currentDoc.getNumber());
			}

		}
		String[] unlinkedNumbers = new String[unlinked.size()];
		for (int docIndex = 0; docIndex < unlinked.size(); docIndex++) {
			unlinkedNumbers[docIndex] = unlinked.get(docIndex);

		}
		return unlinkedNumbers;
	}

	public document getForSHA(String sha1) {
		for (document currentDoc : allDocuments) {
			if ((currentDoc.getSourceSHA1() != null)
					&& (currentDoc.getSourceSHA1().equals(sha1))) {
				return currentDoc;
			}
		}
		return null;
	}

	public void signalChange(document document) {
		for (document currentDoc : allDocuments) {
			if (currentDoc.getID() == document.getID()) {
				currentDoc = document;
			}
		}
		// getDocumentsFromDatabase();

	}

	public document getDocumentForID(int documentID) {
		for (document currentDoc : allDocuments) {
			if (currentDoc.getID() == documentID) {
				return currentDoc;
			}
		}
		return null;
	}

	/**
	 * removes the ID from the list (not from the database)
	 * */
	public void remove(document docToRemove) {
		allDocuments.remove(docToRemove);

	}
}
