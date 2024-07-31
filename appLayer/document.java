package appLayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;

import GUILayer.BufferedImageLuminanceSource;
import GUILayer.documentLabelProvider;
import appLayer.transactionRelated.appTransaction;
import dataLayer.DB;
import dataLayer.fileUtils;

@Entity
public class document {
	private String importFilename, copyFilename, originalFilename;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id = -1;
	@OneToOne(cascade = CascadeType.PERSIST)
	private appTransaction relatedTransaction = null;
	@Temporal(value = TemporalType.DATE)
	private Date date_entry, date_document = null;
	private String number, sha1, subject;
	@Column(precision = 16, scale = 6)
	private BigDecimal value = new BigDecimal(0);
	private boolean isLinked = false;
	private Timestamp outdated = null;
	/** @var XML containing OCRed text in HOCR standard */
	@Lob
	private String hocr = new String();
	@Lob
	private String metaData = new String();
	private boolean bezahlcodeParsed = false;
	private String metaIBAN, metaBIC, metaPurpose = null, metaHolder,
			metaBankName;
	@Column(precision = 16, scale = 6)
	private BigDecimal metaAmount = null;

	public document() {

	}

	public document(String importFilename) {
		setImportFilename(importFilename, null);
		setValue(new BigDecimal(0));
		setNumber(""); //$NON-NLS-1$
		setSubject(""); //$NON-NLS-1$
		setIsLinked(false);
		outdated = null;
		setDateEntry(Calendar.getInstance().getTime());
		setDateDocument(Calendar.getInstance().getTime());
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	/**
	 * sets import file name, scans for meta information in the PDF, scans for
	 * bezahlcode and might OCR the document
	 * */
	public void setImportFilename(String importFilename, Shell sh) {
		this.importFilename = importFilename;
		scanForMeta(sh);
		parseMetadata();
	}

	public String getImportFilename() {
		return importFilename;
	}

	public void save() {

		if (id == -1) {
			//			values.put("date_entry", "NOW()"); //$NON-NLS-1$ //$NON-NLS-2$
			Calendar now = Calendar.getInstance();
			setDateEntry(now.getTime());
			//		setID(DB.insertRaw("documents", values)); //$NON-NLS-1$

		}
		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();

	}

	public void setID(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public void setIsLinked(boolean b) {
		isLinked = b;
	}

	public boolean isLinked() {
		return isLinked;
	}

	public void setDateEntry(Date date_entry) {
		this.date_entry = date_entry;
	}

	public Date getDateEntry() {
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
		// return sdf.format(date_entry);
		return date_entry;
	}

	public void setDateDocument(Date date_document) {
		this.date_document = date_document;
	}

	public Date getDateDocument() {
		return date_document;
	}

	public void setNumber(String number) {
		if (number == null) {
			this.number = ""; //$NON-NLS-1$
		} else {
			this.number = number;
		}
	}

	public String getNumber() {
		return number;
	}

	public void setSourceSHA1(String sha1) {
		this.sha1 = sha1;
	}

	public String getSourceSHA1() {
		return sha1;
	}

	public void setCopy(String newFilename) {
		copyFilename = newFilename;
	}

	public String getCopy() {
		return copyFilename;
	}

	public String getStringValueForColumnIndex(Object arg0, int arg1) {
		document doc = (document) arg0;

		switch (arg1) {
		case 0:
			return doc.getImportFilename();
		case 1:
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
			if (doc.getDateDocument() == null) {
				return sdf.format(new java.util.Date());
			} else {
				return sdf.format(doc.getDateDocument());
			}
		case 2:
			return doc.getNumber();
		case 3:
			return doc.getSubject();
		case 4:
			return String.valueOf(doc.getValue());
		case 5:
			// will not happen, not editable return "<a href=''>test</a>me";

		}
		return Messages.getString("document.notAvailableCellValue"); //$NON-NLS-1$

	}

	public Object getChangeValueForColumnName(String arg1) {
		if (arg1.equals(documentLabelProvider.getCaption(0))) {
			return null;
		} else if (arg1.equals(documentLabelProvider.getCaption(1))) {
			return getDateDocument();
		} else if (arg1.equals(documentLabelProvider.getCaption(2))) {
			return getNumber();
		} else if (arg1.equals(documentLabelProvider.getCaption(3))) {
			return getSubject();
		} else if (arg1.equals(documentLabelProvider.getCaption(4))) {
			return String.valueOf(getValue());
		} else if (arg1.equals(documentLabelProvider.getCaption(5))) {
			return null;
		}
		return Messages.getString("document.notAvailablePrefix") + arg1; //$NON-NLS-1$
	}

	public void setChangedValueForColumnName(String arg1, Object arg2) {
		if (arg1.equals(documentLabelProvider.getCaption(0))) {
			// can not be updated
		} else if (arg1.equals(documentLabelProvider.getCaption(1))) {
			setDateDocument((Date) arg2);
			save();
			client.getDocuments().signalChange(this);
		} else if (arg1.equals(documentLabelProvider.getCaption(2))) {
			setNumber((String) arg2);
			save();
			client.getDocuments().signalChange(this);
		} else if (arg1.equals(documentLabelProvider.getCaption(3))) {
			setSubject((String) arg2);
			save();
			client.getDocuments().signalChange(this);
		} else if (arg1.equals(documentLabelProvider.getCaption(4))) {
			setValue(new BigDecimal((String) arg2));
			save();
			client.getDocuments().signalChange(this);
		}

	}

	/**
	 * tries to find barcode or qrcode in image (expensive) or at least a
	 * document number in the filename
	 * */
	public void scanForMeta(Shell sh) {
		StringBuffer xmp = new StringBuffer();
		if (getImportFilename() != null) {

			File importFile = new File(getImportFilename());
			String code = BufferedImageLuminanceSource
					.scanForBarcode(importFile.toURI());
			if ((code != null) && (code.length() != 0)) {
				setMeta(code);
			}

			if (importFilename.toLowerCase().contains(".pdf")) { //$NON-NLS-1$
				ZUGFeRDImporter zi = new ZUGFeRDImporter();
				zi.extract(importFilename);
				if (zi.containsMeta()) {
					setMeta(zi.getMeta());

				} else {
					// doctag
					try {

						PDDocument doc = PDDocument.load(getImportFilename());
						// XMPMetadata xmp = new XMPMetadata();
						// XMPSchemaPDFAId pdfaid = new XMPSchemaPDFAId(xmp);
						// pdfaid.setConformance();

						PDDocumentInformation info = doc
								.getDocumentInformation();
						/*
						 * System.out.println( "Page Count=" +
						 * doc.getNumberOfPages() ); System.out.println(
						 * "Title=" + info.getTitle() ); System.out.println(
						 * "Author=" + info.getAuthor() ); System.out.println(
						 * "Subject=" + info.getSubject() ); System.out.println(
						 * "Keywords=" + info.getKeywords() );
						 * System.out.println( "Creator=" + info.getCreator() );
						 * System.out.println( "Producer=" + info.getProducer()
						 * ); System.out.println( "Creation Date=" +
						 * info.getCreationDate() ); System.out.println(
						 * "Modification Date=" + info.getModificationDate());
						 * System.out.println( "Trapped=" + info.getTrapped() );
						 */

						PDDocumentCatalog catalog = doc.getDocumentCatalog();
						PDMetadata metadata = catalog.getMetadata();
						if (metadata != null) {
							// read the XMP XML metadata
							InputStream xmlInputStream = metadata
									.createInputStream();
							BufferedReader in = new BufferedReader(
									new InputStreamReader(xmlInputStream));
							String inputLine;
							while ((inputLine = in.readLine()) != null) {
								xmp.append(inputLine);
							}
							in.close();
						}
					} catch (IOException e) {
						// ignore pdf IOExceptions, as maybe the document is no
						// valid PDF at all
					}

					if ((xmp.length() > 0)
							&& (xmp.toString().contains("<doctag>"))) { //$NON-NLS-1$
						// doctag

						setMeta(xmp.toString());
					}
				}

			} else {
				// if the file is NO pdf it makes sense to try some OCR...
				// tesseract text recognition

				if (configs.shallOCR()) {

					try {

						Tesseract instance = Tesseract.getInstance(); // JNA
						// Interface
						// Mapping
						instance.setLanguage(configs.getOCRlang());
						instance.setHocr(true);
						instance.setDatapath(configs.getOCRlangPath());
						// Tesseract1 instance = new Tesseract1(); // JNA Direct
						// Mapping
						String result = instance.doOCR(importFile);
						setHOCR(result);
					} catch (UnsatisfiedLinkError unsL) {
						String errText = Messages.getString("document.TesNotFoundErrMessage")+unsL.getLocalizedMessage(); //$NON-NLS-1$
						if (sh != null) {
							MessageDialog.openError(sh, Messages.getString("document.TesNotFoundErrHeadline"), //$NON-NLS-1$
									errText);
						} else {
							System.err.println(errText);
						}

					} catch (TesseractException e1) {
						System.err.println(e1.getMessage());
					}

				}

			}
			// parse potential metadata from filename
			String recognizedNumber = client.getDocuments()
					.recognizeDocumentNumberInString(getImportFilename());
			if (recognizedNumber != null) {
				metaPurpose = recognizedNumber;
			}
		}

	}

	/**
	 * when importing files, this will copy the imported file to the documents
	 * directory
	 * 
	 * @return error String, null if copy was successfull
	 * */
	public String copyOver() {
		File importFile = new File(getImportFilename());
		client.prepareDocumentsPath();
		String sha = null;
		try {
			sha = fileUtils
					.SHA1(fileUtils.readFileAsBytes(getImportFilename()));
			if (client.getDocuments().getForSHA(sha) != null) {
				return Messages.getString("document.alreadyImportedErrorPart1") + importFilename + Messages.getString("document.alreadyImportedErrorPart2") //$NON-NLS-1$ //$NON-NLS-2$
						+ client.getDocuments().getForSHA(sha).getCopy();
			}
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			String copyNameFirstAttempt = client.getDocumentPath()
					+ File.separator + importFile.getName();
			String indexStr = ""; //$NON-NLS-1$

			// the path+filename before file extension
			String base = copyNameFirstAttempt.substring(0,
					copyNameFirstAttempt.lastIndexOf(".")); //$NON-NLS-1$

			// the filename file extension
			String extension = copyNameFirstAttempt
					.substring(copyNameFirstAttempt.lastIndexOf(".")); //$NON-NLS-1$

			int filenameIndex = 0;
			String newFilename = base + indexStr + extension;
			while (new File(newFilename).exists()) {
				filenameIndex++;
				indexStr = "_" + filenameIndex; //$NON-NLS-1$
				newFilename = base + indexStr + extension;
			}
			fileUtils.copyFile(importFile, new File(newFilename));
			setCopy(newFilename);
			setSourceSHA1(sha);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	private void setMeta(String code) {
		metaData = code;
	}

	public boolean hasMeta() {
		return metaData.length() > 0;
	}

	public boolean hasBezahlcode() {
		return hasMeta() && getMeta().startsWith("bank://"); //$NON-NLS-1$
	}

	private void parseBezahlcode() {
		String code = getMeta();

		String prefix = "bank://singlepayment?"; //$NON-NLS-1$
		if (code.startsWith(prefix)) {
			code = code.substring(prefix.length());

			String[] params = code.split("&"); //$NON-NLS-1$
			Map<String, String> map = new HashMap<String, String>();
			for (String param : params) {
				String name = param.split("=")[0].toLowerCase(); //$NON-NLS-1$

				String value;
				try {
					value = URLDecoder.decode(param.split("=")[1], "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
					map.put(name, value);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			metaAmount = utils.String2BD(map.get("amount")); //$NON-NLS-1$
			metaIBAN = map.get("account"); //$NON-NLS-1$
			metaBIC = map.get("bnc"); //$NON-NLS-1$
			metaHolder = map.get("name"); //$NON-NLS-1$
			metaPurpose = map.get("reason"); //$NON-NLS-1$

		}

		bezahlcodeParsed = true;
	}

	public BigDecimal getMetaAmount() {
		if (!bezahlcodeParsed) {
			parseBezahlcode();
		}
		return metaAmount;
	}

	public String getMetaIBAN() {
		if (!bezahlcodeParsed) {
			parseBezahlcode();
		}
		return metaIBAN;
	}

	public String getMetaBIC() {
		if (!bezahlcodeParsed) {
			parseBezahlcode();
		}
		return metaBIC;

	}

	public String getMetaPurpose() {
		if (!bezahlcodeParsed) {
			parseBezahlcode();
		}
		return metaPurpose;
	}

	public String getMetaHolder() {
		if (!bezahlcodeParsed) {
			parseBezahlcode();
		}
		return metaHolder;

	}

	public String getMeta() {
		return metaData;
	}

	/**
	 * removes the document (logically) from the database
	 * */
	public void delete() {
		Calendar c = Calendar.getInstance();

		outdated = new Timestamp(c.getTime().getTime());
		save();
	}

	public void setTransaction(appTransaction at) {
		relatedTransaction = at;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public void parseMetadata() {

		parseZugferd();
		parseBezahlcode();
		parseDocTag();
	}

	private void parseZugferd() {
		ZUGFeRDImporter zi = new ZUGFeRDImporter();
		zi.setMeta(getMeta());
		if (zi.canParse()) {
			zi.parse();
			if (zi.getAmount() != null) {
				metaAmount = new BigDecimal(zi.getAmount()); //$NON-NLS-1$
			}
			if (zi.getBIC() != null) {
				metaBIC = zi.getBIC();
			}
			if (zi.getBankName() != null) {
				metaBankName = zi.getBankName();
			}
			if (zi.getForeignReference() != null) {
				metaPurpose = zi.getForeignReference();
			}
			if (zi.getHolder() != null) {
				metaHolder = zi.getHolder();
			}
			if (zi.getIBAN() != null) {
				metaIBAN = zi.getIBAN();
			}

		}

	}

	private void parseDocTag() {
		// 1: from PDF
		if (metaData.contains("<doctag>")) { //$NON-NLS-1$

			String docTagJSON = metaData.replace("<doctag>", ""); //$NON-NLS-1$ //$NON-NLS-2$
			docTagJSON = docTagJSON.replace("</doctag>", ""); //$NON-NLS-1$ //$NON-NLS-2$
			parseDocTagMetadataFromRaw(docTagJSON);
		} else {
			// alternatively e.g. from QR code
			if (metaData.contains("doctag_version")) { //$NON-NLS-1$
				/*
				 * working around broken doctag sample
				 * metaData=metaData.replace("\"gross\":696.62",
				 * "\"gross\":696.62");
				 * metaData=metaData.replace("\"amount\": 13,93",
				 * "\"amount\": 13.93");
				 */
				parseDocTagMetadataFromRaw(metaData);
			}
		}

	}

	private void parseDocTagMetadataFromRaw(String docTagJSON) {
		Object jsonObjectUntyped = JSONValue.parse(docTagJSON);
		JSONObject jsonObject = (JSONObject) jsonObjectUntyped;
		Object numberObject = jsonObject.get("number"); //$NON-NLS-1$
		if (numberObject != null) {
			// number is mandatory, so if it isn't there there is no doctag
			metaPurpose = (String) numberObject; //$NON-NLS-1$
			JSONObject jsonTotal = (JSONObject) jsonObject.get("total"); //$NON-NLS-1$
			metaAmount = new BigDecimal((Double) jsonTotal.get("gross")); //$NON-NLS-1$
			// unfortunately, no sender (payment recipient) is specified in
			// Doctag 0.6
		}

	}

	/***
	 * get OCR recognized text in HOCR XML format
	 */
	public String getHOCR() {
		return hocr;
	}

	/***
	 * set OCR recognized text in HOCR XML format
	 */
	public void setHOCR(String hocr) {
		this.hocr = hocr;
	}

	public String getMetaBankName() {
		return metaBankName;
	}

	public void setMetaBankName(String metaBankName) {
		this.metaBankName = metaBankName;
	}

}
