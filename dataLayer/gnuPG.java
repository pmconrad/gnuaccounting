package dataLayer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import appLayer.configs;

public class gnuPG {
	// source: http://www.macnews.co.il/mageworks/java/gnupg/
	// Konstants:
	private String kGnuPGCommand = null;

	// Class vars:
	private File tmpFile;
	private int gpg_exitCode = -1;
	private String gpg_result;
	private String gpg_err;

	/**
	 * Reads an output stream from an external process. Imeplemented as a thred.
	 */
	class ProcessStreamReader extends Thread {
		StringBuffer stream;
		InputStreamReader in;

		final static int BUFFER_SIZE = 1024;

		/**
		 * Creates new ProcessStreamReader object.
		 * 
		 * @param in
		 */
		ProcessStreamReader(InputStream in) {
			super();

			this.in = new InputStreamReader(in);

			this.stream = new StringBuffer();
		}

		public void run() {
			try {
				int read;
				char[] c = new char[BUFFER_SIZE];

				while ((read = in.read(c, 0, BUFFER_SIZE - 1)) > 0) {
					stream.append(c, 0, read);
					if (read < BUFFER_SIZE - 1)
						break;
				}
			} catch (IOException io) {
			}
		}

		String getString() {
			return stream.toString();
		}
	}

	/**
	 * Sign
	 * 
	 * @param inStr
	 *            input string to sign
	 * @param passPhrase
	 *            passphrase for the personal private key to sign with
	 * @return true upon success
	 */
	public boolean sign(String inStr, String passPhrase) {
		boolean success;

		success = createTempFile(inStr);

		if (success) {
			success = runGnuPG(
					"--passphrase-fd 0 --sign " + this.tmpFile.getAbsolutePath(), passPhrase); //$NON-NLS-1$
			this.tmpFile.delete();
			if (success && this.gpg_exitCode != 0)
				success = false;
		}
		return success;
	}

	/**
	 * ClearSign
	 * 
	 * @param inStr
	 *            input string to sign
	 * @param passPhrase
	 *            passphrase for the personal private key to sign with
	 * @return true upon success
	 */
	public boolean clearSign(String inStr, String passPhrase) {
		boolean success;

		success = createTempFile(inStr);

		if (success) {
			success = runGnuPG(
					"--passphrase-fd 0 --clearsign " + this.tmpFile.getAbsolutePath(), passPhrase); //$NON-NLS-1$
			this.tmpFile.delete();
			if (success && this.gpg_exitCode != 0)
				success = false;
		}
		return success;
	}

	/**
	 * Signs and encrypts a string
	 * 
	 * @param inStr
	 *            input string to encrypt
	 * @param keyID
	 *            key ID of the key in GnuPG's key database to encrypt with
	 * @param passPhrase
	 *            passphrase for the personal private key to sign with
	 * @return true upon success
	 */
	public boolean signAndEncrypt(String inStr, String keyID, String passPhrase) {
		boolean success;

		success = createTempFile(inStr);

		if (success) {
			success = runGnuPG(
					"-r "	+ keyID + " --passphrase-fd 0 -se " + this.tmpFile.getAbsolutePath(), passPhrase); //$NON-NLS-1$ //$NON-NLS-2$
			this.tmpFile.delete();
			if (success && this.gpg_exitCode != 0)
				success = false;
		}
		return success;
	}

	/**
	 * Encrypt
	 * 
	 * @param inStr
	 *            input string to encrypt
	 * @param keyID
	 *            key ID of the key in GnuPG's key database to encrypt with
	 * @return true upon success
	 */
	public boolean encrypt(String inStr, String keyID) {
		boolean success;

		success = runGnuPG("-r " + keyID + " --encrypt", inStr); //$NON-NLS-1$ //$NON-NLS-2$
		if (success && this.gpg_exitCode != 0)
			success = false;
		return success;
	}

	/**
	 * Decrypt
	 * 
	 * @param inStr
	 *            input string to decrypt
	 * @param passPhrase
	 *            passphrase for the personal private key to sign with
	 * @return true upon success
	 */
	public boolean decrypt(String inStr, String passPhrase) {
		boolean success;

		success = createTempFile(inStr);

		if (success) {
			success = runGnuPG(
					"--passphrase-fd 0 --decrypt " + this.tmpFile.getAbsolutePath(), passPhrase); //$NON-NLS-1$
			this.tmpFile.delete();
			if (success && this.gpg_exitCode != 0)
				success = false;
		}
		return success;
	}

	/**
	 * Verify a signature
	 * 
	 * @param inStr
	 *            signature to verify
	 * @param keyID
	 *            key ID of the key in GnuPG's key database
	 * @return true if verified.
	 */
	/*
	 * public boolean verifySignature (String inStr, String keyID) { boolean
	 * success;
	 * 
	 * success = runGnuPG ("--sign " + keyID, inStr); if (success &&
	 * this.gpg_exitCode != 0) success = false; return success; }
	 */

	/**
	 * Get processing result
	 * 
	 * @return result string.
	 */
	public String getResult() {
		return gpg_result;
	}

	/**
	 * Get error output from GnuPG process
	 * 
	 * @return error string.
	 */
	public String getErrorString() {
		return gpg_err;
	}

	/**
	 * Get GnuPG exit code
	 * 
	 * @return exit code.
	 */
	public int getExitCode() {
		return gpg_exitCode;
	}

	/**
	 * Runs GnuPG external program
	 * 
	 * @param commandArgs
	 *            command line arguments
	 * @param inputStr
	 *            key ID of the key in GnuPG's key database
	 * @return true if success.
	 */
	private boolean runGnuPG(String commandArgs, String inputStr) {
		Process p;
		String fullCommand = kGnuPGCommand + " " + commandArgs; //$NON-NLS-1$
		// String fullCommand = commandArgs;

		System.out.println(fullCommand);

		try {
			p = Runtime.getRuntime().exec(fullCommand);
		} catch (IOException io) {
			System.out
					.println(Messages.getString("gnuPG.ioError") + io.getMessage()); //$NON-NLS-1$
			return false;
		}

		ProcessStreamReader psr_stdout = new ProcessStreamReader(
				p.getInputStream());
		ProcessStreamReader psr_stderr = new ProcessStreamReader(
				p.getErrorStream());
		psr_stdout.start();
		psr_stderr.start();
		if (inputStr != null) {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					p.getOutputStream()));
			try {
				out.write(inputStr);
				out.close();
			} catch (IOException io) {
				System.out
						.println(Messages.getString("gnuPG.writeException") + io.getMessage()); //$NON-NLS-1$
				return false;
			}
		}

		try {
			p.waitFor();

			psr_stdout.join();
			psr_stderr.join();
		} catch (InterruptedException i) {
			System.out
					.println(Messages.getString("gnuPG.waitForException") + i.getMessage()); //$NON-NLS-1$
			return false;
		}

		try {
			gpg_exitCode = p.exitValue();
		} catch (IllegalThreadStateException itse) {
			return false;
		}

		gpg_result = psr_stdout.getString();
		gpg_err = psr_stderr.getString();

		return true;
	}

	/**
	 * A utility method for creating a unique temporary file when needed by one
	 * of the main methods.<BR>
	 * The file handle is store in tmpFile object var.
	 * 
	 * @param inStr
	 *            data to write into the file.
	 * @return true if success
	 */
	private boolean createTempFile(String inStr) {
		this.tmpFile = null;
		FileWriter fw;

		try {
			this.tmpFile = File.createTempFile("YGnuPG", null); //$NON-NLS-1$
		} catch (Exception e) {
			System.out
					.println(Messages.getString("gnuPG.cannotCreateTempFile") + e.getMessage()); //$NON-NLS-1$
			return false;
		}

		try {
			fw = new FileWriter(this.tmpFile);
			fw.write(inStr);
			fw.flush();
			fw.close();
		} catch (Exception e) {
			// delete our file:
			tmpFile.delete();

			System.out
					.println(Messages.getString("gnuPG.cannotWriteTempFile") + e.getMessage()); //$NON-NLS-1$
			return false;
		}

		return true;
	}

	/*
	 * public static void main (String args[]) { // use this to check:
	 * System.out.println("Hello World!"); GnuPG pgp = new GnuPG (); if
	 * (args[0].equals ("sign")) pgp.sign (args[1], args[2]); else if
	 * (args[0].equals ("clearsign")) pgp.clearSign (args[1], args[2]); else if
	 * (args[0].equals ("se")) pgp.signAndEncrypt (args[1], args[2],args[3]);
	 * else if (args[0].equals ("encrypt")) pgp.encrypt (args[1], args[2]); else
	 * if (args[0].equals ("decrypt")) pgp.decrypt (args[1], args[2]);
	 * System.out.println("result: " + pgp.gpg_result + "\n\n");
	 * System.out.println("error: " + pgp.gpg_err + "\n\n");
	 * System.out.println("exit: " + pgp.gpg_exitCode + "\n\n"); }
	 */

	public gnuPG() {
		kGnuPGCommand = configs.getGPGPath()
				+ System.getProperty("file.separator") + "gpg --batch --armor --output -"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
