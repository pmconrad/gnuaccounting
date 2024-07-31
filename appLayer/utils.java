package appLayer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

public class utils {
	// real class

	public static int findIndexOfStringInStringArray(String[] haystack,
			String needle) throws elementNotFoundException {
		int colIndex = 0;
		for (String currentElement : haystack) {
			if (currentElement.equals(needle)) {
				return colIndex;
			}
			colIndex++;
		}
		throw new elementNotFoundException(
				Messages.getString("utils.element") + needle + Messages.getString("utils.notfoundinarray") + haystack); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public static String quoteForXML(String from) {
		String to = from.replace("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		to = to.replace("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		to = to.replace(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		to = to.replace("'", "&#39;"); //$NON-NLS-1$ //$NON-NLS-2$
		to = to.replace("\"", "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$
		return to;
	}

	/**
	 * based on http://code.google.com/p/zxing/issues/detail?id=1376
	 * @throws Exception 
	 * */
	private static int getCheckSum(CharSequence s) throws Exception {
		int length = s.length();
		int sum = 0;
		for (int i = length - 1; i >= 0; i -= 2) {

			int digit = (int) s.charAt(i) - (int) '0';
			if (digit < 0 || digit > 9) {
				throw new Exception(
						Messages.getString("utils.invalidDigitException")); //$NON-NLS-1$
			}
			sum += digit;
		}
		sum *= 3;
		for (int i = length - 2; i >= 0; i -= 2) {
			int digit = (int) s.charAt(i) - (int) '0';
			if (digit < 0 || digit > 9) {
				throw new Exception(
						Messages.getString("utils.invalidDigitException")); //$NON-NLS-1$
			}
			sum += digit;
		}
		int chkdig = 0;
		if (sum % 10 != 0) {
			chkdig = 10 - sum % 10;
		}
		return chkdig;
	}

	public static boolean checkEAN(String EAN) {

		StringBuffer sb = new StringBuffer(EAN);
		if (sb.length() != 13) {
			return false;
		}
		try {
			return getCheckSum(sb.substring(0, 12)) == (sb.charAt(sb.length() - 1) - 48);
		} catch (Exception e) {
//			e.printStackTrace();
			// invalid digit in the EAN: maybe it has been abused e.g. for ISBN
		}
		return false;

	}

	private static Random rn = new Random();

	public static int rand(int lo, int hi) {
		int n = hi - lo + 1;
		int i = rn.nextInt() % n;
		if (i < 0)
			i = -i;
		return lo + i;
	}

	public static byte[] randomBytes(int minimumCharacters,
			int maximumCharacters) {
		int n = rand(minimumCharacters, maximumCharacters);
		byte b[] = new byte[n];
		for (int i = 0; i < n; i++)
			b[i] = (byte) rand('a', 'z');
		return b;
	}

	public static String makeRelativePathAbsolute(String relativePath) {
		File f = new File(relativePath);
		String prefix = f.getAbsolutePath();
		if (!prefix.endsWith(File.separator)) {
			prefix = prefix + File.separator;
		}
		int lastSlash = prefix.lastIndexOf(File.separator);
		if (lastSlash != -1) {
			return prefix.substring(0, lastSlash + 1);
		}
		return null;
	}

	/**
	 * Get an attribute's value and return an empty string, of the attribute is
	 * not specified
	 * 
	 * @param attributes
	 *            Attributes node
	 * @param name
	 *            Name of the attribute
	 * @return Attributes value
	 */
	public static String getAttributeAsString(NamedNodeMap attributes,
			String name) {
		Attr attribute;
		String value = ""; //$NON-NLS-1$
		attribute = (Attr) attributes.getNamedItem(name);
		if (attribute != null) {
			value = attribute.getValue();
		}
		return value;
	}

	public static void logAndShowException(final Shell sh, final Exception ex1) {
		FileWriter fstream;
		try {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(
							sh,
							Messages.getString("utils.errorHeading"), Messages.getString("utils.errorText") + ex1.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			});

			fstream = new FileWriter("log.txt", true); //$NON-NLS-1$
			BufferedWriter out = new BufferedWriter(fstream);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
			out.write("Exception @ " + sdf.format(new Date()) + "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
			out.write("Message: " + ex1.getMessage() + "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
			out.write("Stacktrace\r\n"); //$NON-NLS-1$
			out.write("===============================\r\n"); //$NON-NLS-1$
			StackTraceElement[] ste = ex1.getStackTrace();
			for (StackTraceElement currentStackTraceElement : ste) {
				out.write("\tIn method " //$NON-NLS-1$
						+ currentStackTraceElement.getMethodName() + " line " //$NON-NLS-1$
						+ currentStackTraceElement.getLineNumber() + " in " //$NON-NLS-1$
						+ currentStackTraceElement.getFileName() + "\r\n"); //$NON-NLS-1$
			}
			out.write("===============================\r\n"); //$NON-NLS-1$
			// Close the output stream
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// number (,math and currency) utils
	// ...start...
	// ...here:

	/**
	 * takes a string and returns a BD, no matter if , or . was used as decimal
	 * separator in the String
	 * */
	public static BigDecimal String2BD(String value) {
		if ((value == null) || (value.length() == 0)) {
			return new BigDecimal(0);
		}
		value = value.replace(',', '.');
		BigDecimal q = new BigDecimal(value);

		return q;

	}

	public static String BD2String(BigDecimal value, char decimalDelimiter) {
		return value.toPlainString().replace('.', decimalDelimiter);
	}

	public static String BD2Currency(BigDecimal value) {
		NumberFormat form = NumberFormat.getCurrencyInstance();
		return form.format(value);

	}

	/**
	 * round a number to an arbitrary precision based on
	 * http://www.crazysquirrel.com/computing/java/basics/rounding.jspx
	 * */
	/*
	 * public static Double round(Double d, int precision) { //BigDecimal b=new
	 * BigDecimal(d); //MathContext context = new MathContext( ( b.precision() -
	 * b.scale() + precision ), RoundingMode.HALF_UP); //return
	 * b.round(context).doubleValue(); double exp=Math.pow(10, precision);
	 * d*=exp; long dlong=Math.round(d);
	 * 
	 * return dlong/exp; }
	 */

	public static BigDecimal round(BigDecimal b, int precision) {
		// BigDecimal b=new BigDecimal(d);
		MathContext context = new MathContext(
				(b.precision() - b.scale() + precision), RoundingMode.HALF_UP);
		return b.round(context);
	}

	/**
	 * currency BigDecimal to String in current locale (might include currency symbol) 
	 * */
	public static String currencyFormat(BigDecimal value) {
		NumberFormat form = NumberFormat.getCurrencyInstance();
		return form.format(value);
	}

	/**
	 * currency BigDecimal to String with specified decimal delimiter (without currency symbol) 
	 * */
	public static String currencyFormat(BigDecimal value, char decimalDelimiter) {
		/*
		 * I needed 123,45, locale independent.I tried
		 * NumberFormat.getCurrencyInstance().format( 12345.6789 ); but that is
		 * locale specific.I also tried DecimalFormat df = new DecimalFormat(
		 * "0,00" ); df.setDecimalSeparatorAlwaysShown(true);
		 * df.setGroupingUsed(false); DecimalFormatSymbols symbols = new
		 * DecimalFormatSymbols(); symbols.setDecimalSeparator(',');
		 * symbols.setGroupingSeparator(' ');
		 * df.setDecimalFormatSymbols(symbols);
		 * 
		 * but that would not switch off grouping. Although I liked very much
		 * the (incomplete) "BNF diagram" in
		 * http://docs.oracle.com/javase/tutorial/i18n/format/decimalFormat.html
		 * in the end I decided to calculate myself and take eur+sparator+cents
		 * 
		 * This function will cut off, i.e. floor() subcent values Tests:
		 * System.err.println(utils.currencyFormat(new BigDecimal(0),
		 * ".")+"\n"+utils.currencyFormat(new BigDecimal("-1.10"),
		 * ",")+"\n"+utils.currencyFormat(new BigDecimal("-1.1"),
		 * ",")+"\n"+utils.currencyFormat(new BigDecimal("-1.01"),
		 * ",")+"\n"+utils.currencyFormat(new BigDecimal("20000123.3489"),
		 * ",")+"\n"+utils.currencyFormat(new BigDecimal("20000123.3419"),
		 * ",")+"\n"+utils.currencyFormat(new BigDecimal("12"), ","));
		 * 
		 * results 0.00 -1,10 -1,10 -1,01 20000123,34 20000123,34 12,00
		 */
		value=value.setScale( 2, BigDecimal.ROUND_HALF_UP ); // first, round so that e.g. 1.189999999999999946709294817992486059665679931640625 becomes 1.19  
		long totalCent = value.multiply(new BigDecimal(100)).intValue(); //now get the cents
		long eurOnly = value.longValue();
		long centOnly = Math.abs(totalCent % 100);
		StringBuffer res = new StringBuffer();
		res.append(eurOnly);
		res.append(decimalDelimiter);
		if (centOnly < 10) {
			res.append('0');
		}
		res.append(centOnly);
		return res.toString();
	}
	
	public static Calendar easterSunday( int year )
	   {
	    int i = year % 19;
	    int j = year / 100;
	    int k = year % 100;

	    int l = (19 * i + j - (j / 4) - ((j - ((j + 8) / 25) + 1) / 3) + 15) % 30;
	    int m = (32 + 2 * (j % 4) + 2 * (k / 4) - l - (k % 4)) % 7;
	    int n = l + m - 7 * ((i + 11 * l + 22 * m) / 451) + 114;

	    int month = n / 31;
	    int day   = (n % 31) + 1;

	    return  new GregorianCalendar( year, month-1, day );
	  }

	public static boolean isWeekDay(Calendar c) {
	
		int dow=c.get(Calendar.DAY_OF_WEEK);
		return  ((dow >= Calendar.MONDAY) && (dow <= Calendar.FRIDAY));
	}

	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
	    if (cal1 == null || cal2 == null)
	        return false;
	    return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
	            && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) 
	            && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}
	/**
	 * target2 working days: weekdays except 1.1., 1.5., 25.12., 26.12., Good Friday, Easter Monday
	 * */
	public static boolean isWorkingDay(Calendar c) {
		int day=c.get(Calendar.DAY_OF_MONTH);
		int mon=c.get(Calendar.MONTH);

		DateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd" ); //$NON-NLS-1$
		Calendar easterSunday=easterSunday(c.get(Calendar.YEAR));
		Calendar easterMonday=(Calendar)easterSunday.clone();
		easterMonday.add(Calendar.DATE, 1);
		Calendar goodFriday=(Calendar)easterSunday.clone();
		goodFriday.add(Calendar.DATE, -2);
		return isWeekDay(c)&&
				((day!=1)||(mon!=1-1))&&
				((day!=1)||(mon!=5-1))&&
				((day!=25)||(mon!=12-1))&&
				((day!=26)||(mon!=12-1))&&
				(!isSameDay(c,goodFriday)) &&
				(!isSameDay(c,easterMonday));
	}

}
