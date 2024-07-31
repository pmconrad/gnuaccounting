package tests;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import appLayer.utils;

public class utilsTest {
	@Test
	public void checkDates() {

	   
		Calendar c = new GregorianCalendar();
		c.set(Calendar.YEAR, 2014);
		c.set(Calendar.MONTH, 11);
		c.set(Calendar.DAY_OF_MONTH, 11);
		
		assertEquals("is weekday",utils.isWeekDay(c),true); //$NON-NLS-1$
		DateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd" ); //$NON-NLS-1$

		Calendar d = new GregorianCalendar();
		try {
			d.setTime(formatter.parse( "2014-11-08" )); //$NON-NLS-1$
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		assertEquals("is no weekday",!utils.isWeekDay(d),true); //$NON-NLS-1$

		try {
			d.setTime(formatter.parse( "2014-11-09" )); //$NON-NLS-1$
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		assertEquals("is no workingday",!utils.isWorkingDay(d),true); //$NON-NLS-1$

		try {
			d.setTime(formatter.parse( "2014-05-01" )); //$NON-NLS-1$
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		assertEquals("is no workingday",!utils.isWorkingDay(d),true); //$NON-NLS-1$
		

		try {
			d.setTime(formatter.parse( "2015-04-06" )); //$NON-NLS-1$
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		assertEquals("eastermoday is no workingday",!utils.isWorkingDay(d),true); //$NON-NLS-1$
		

		try {
			d.setTime(formatter.parse( "2015-04-07" )); //$NON-NLS-1$
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		assertEquals(Messages.utilsTest_11,utils.isWorkingDay(d),true);
		
	} 
}
