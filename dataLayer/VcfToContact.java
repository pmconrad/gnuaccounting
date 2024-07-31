package dataLayer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.fortuna.ical4j.model.property.Contact;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.parameter.Type;
import net.fortuna.ical4j.vcard.property.Address;
import net.fortuna.ical4j.vcard.property.N;
import net.fortuna.ical4j.vcard.property.Telephone;
import appLayer.contact;

/**
 * Maps VCard attributes to {@link Contact}.
 * 
 * @author peter
 */
public class VcfToContact {

	private static Logger logger = Logger.getLogger(VcfToContact.class
			.getName());
	private static final DateFormat revFormat = new SimpleDateFormat(
			"yyyyMMdd'T'hhmmss'Z'"); //$NON-NLS-1$

	public contact adaptOrCreate(VCard vcard, contact contact)
			throws ParseException {
		String fn = ((N) vcard.getProperty(Id.N)).getFamilyName()
				+ ", " + ((N) vcard. //$NON-NLS-1$
						getProperty(Id.N)).getGivenName();
		if (contact == null) {
			contact = new contact();
			contact.setVcfId(vcard.getProperty(Id.UID).getValue());
			if (logger.isLoggable(Level.INFO)) {
				logger.info(Messages
						.getString("VcfToContact.logCreatingContact") + fn); //$NON-NLS-1$
			}
		} else {
			if (logger.isLoggable(Level.INFO)) {
				logger.info(Messages
						.getString("VcfToContact.logAdaptingContact") + fn); //$NON-NLS-1$
			}
		}
		Property revProp = vcard.getProperty(Id.REV);
		if (revProp != null) {
			contact.setLastVcfChange(revFormat.parse(revProp.getValue()));
		}
		contact.setName(fn);
		String email = getSimpleProperty(vcard, Id.EMAIL);
		if (email != null) {
			contact.setEmail(email);
		}
		Address addr = (Address) vcard.getProperty(Id.ADR);
		if (addr != null) {
			// contact.setAdditionalAddressLine(???);
			contact.setStreet(addr.getStreet());
			contact.setZIP(addr.getPostcode());
			contact.setLocation(addr.getLocality());
			contact.setCountry(addr.getCountry());
		}
		Telephone firstTel = null;
		for (Property telProp : vcard.getProperties(Id.TEL)) {
			Telephone tel = (Telephone) telProp;
			List<Type> types = (List) tel.getParameters(Parameter.Id.TYPE);
			for (Type type : types) {
				for (String typeStr : type.getTypes()) {
					if (typeStr.equals("HOME")) //$NON-NLS-1$
					{
						contact.setPhone(getTelNumber(tel));
					} else if (typeStr.equals("FAX")) //$NON-NLS-1$
					{
						contact.setFax(getTelNumber(tel));
					} else {
						if (firstTel == null) {
							firstTel = tel;
						}
					}
				}
			}
		}
		if (contact.getPhone() == null && firstTel != null) {
			contact.setPhone(getTelNumber(firstTel));
		}

		return contact;
	}

	private String getTelNumber(Telephone tel) {
		String s = tel.getUri().getSchemeSpecificPart();
		if (s.contains("/")) //$NON-NLS-1$
		{
			s = s.replace(" ", "").replace("/", "-"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		return s;
	}

	private String getSimpleProperty(VCard vcard, Id id) {
		Property p = vcard.getProperty(id);
		if (p != null) {
			return p.getValue();
		}
		return null;
	}
}
