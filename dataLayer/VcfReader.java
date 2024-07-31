package dataLayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;

/**
 * Reads a couple of VCF files.
 * 
 * @author peter
 */
public class VcfReader {

	private static final Logger logger = Logger.getLogger(VcfReader.class
			.getName());
	private File vcfDirectory;
	private String fileExt = null;

	public String getFileExt() {
		return fileExt;
	}

	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}

	public File getVcfDirectory() {
		return vcfDirectory;
	}

	public void setVcfDirectory(File vcfDirectory) {
		this.vcfDirectory = vcfDirectory;
	}

	public List<VCard> readVcfs() {
		File[] vcfFiles;
		if (fileExt == null) {
			vcfFiles = vcfDirectory.listFiles();
		} else {
			vcfFiles = vcfDirectory.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(fileExt);
				}
			});
		}
		List<VCard> vcards = new ArrayList<VCard>();
		for (File vcfFile : vcfFiles) {
			FileInputStream inp;
			try {
				inp = new FileInputStream(vcfFile);
			} catch (FileNotFoundException ex) {
				Logger.getLogger(VcfReader.class.getName()).log(Level.SEVERE,
						null, ex);
				continue;
			}
			VCardBuilder builder = new VCardBuilder(inp);
			try {
				for (VCard vcard : builder.buildAll()) {
					vcards.add(vcard);
				}
			} catch (IOException ex) {
				logger.log(Level.SEVERE, ex.getMessage(), ex);
			} catch (ParserException ex) {
				logger.log(
						Level.WARNING,
						Messages.getString("VcfReader.parseError") + vcfFile + "\n" + ex. //$NON-NLS-1$ //$NON-NLS-2$
										getMessage(), ex);
			}
		}
		return vcards;
	}
}
