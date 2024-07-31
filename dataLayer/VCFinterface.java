package dataLayer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.VCard;
import appLayer.client;
import appLayer.contact;

/**
 * Main class to import vcf's into GNUAccounting.
 * 
 * @author peter
 */
public class VCFinterface {

	private VcfReader vcfReader = new VcfReader();
	private VcfToContact vcfToContact = new VcfToContact();
	private File srcVcfDir;

	public void setSrcVcfDir(File srcVcfDir) {
		this.srcVcfDir = srcVcfDir;
	}

	public void setVcfReader(VcfReader vcfReader) {
		this.vcfReader = vcfReader;
	}

	public void setVcfToContact(VcfToContact vcfToContact) {
		this.vcfToContact = vcfToContact;
	}

	public File getSrcVcfDir() {
		return srcVcfDir;
	}

	public VcfReader getVcfReader() {
		return vcfReader;
	}

	public VcfToContact getVcfToContact() {
		return vcfToContact;
	}

	public void init() throws IOException {
		this.vcfReader.setVcfDirectory(srcVcfDir);
	}

	public void doImport() {
		List<VCard> vcards = this.vcfReader.readVcfs();
		for (VCard vcard : vcards) {
			contact contact = client.getContacts().getContactByVCFID(
					vcard.getProperty(Id.UID).getValue());
			try {
				contact = vcfToContact.adaptOrCreate(vcard, contact);

				contact.save();
			} catch (Exception ex) {
				Logger.getLogger(VCFinterface.class.getName()).log(
						Level.SEVERE,
						Messages.getString("VCFinterface.errorPrefix") + vcard. //$NON-NLS-1$
								toString(), ex);
			}
		}
	}

}
