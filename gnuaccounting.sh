#!/bin/bash
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.
FILE=$JAVA_HOME/bin/java
if [[ ("$0" == "/usr/share/gnuaccounting/gnuaccounting") || ("$0" == "/usr/bin/gnuaccounting")]]
then
    cd /usr/share/gnuaccounting/
else
    # if a user starts e.g. sh test/me/gnuaccounting.sh get the relative dir part "test/me" 
    RELATIVEDIR=`echo $0|sed s/gnuaccounting.sh//g`
fi
if [ $RELATIVEDIR ];
then
    cd $RELATIVEDIR
fi
ARGUMENTS="-Djava.library.path=./libs -cp ./libs/swt/swt.jar:./libs/swt/org.eclipse.equinox.common_3.7.0.v20150402-1709.jar:./libs/swt/org.eclipse.jface_3.11.1.v20160128-1644.jar:./libs/swt/org.eclipse.osgi_3.10.102.v20160118-1700.jar:./libs/swt/org.eclipse.swt_3.104.2.v20160212-1350.jar:./libs/noa/noa-libre.jar:./libs/noa/junit-3.8.1.jar:./libs/noa/ridl.jar:./libs/noa/bootstrapconnector.jar:./libs/noa/jurt.jar:./libs/noa/sandbox.jar:./libs/noa/java_uno_accessbridge.jar:./libs/noa/jut.jar:./libs/noa/unoil.jar:./libs/noa/officebean.jar:./libs/noa/unoloader.jar:./libs/noa/juh.jar:./libs/noa/registry-3.1.3.jar:./libs/mail.jar:./libs/hbci/hbci4java.jar:./libs/barcode-google-zxing-2_2_core.jar:./libs/barcode-google-zxing-2_2_javase.jar:./libs/uk.co.mmscomputing.device.sane.jar:./libs/uk.co.mmscomputing.device.twain.jar:./libs/PDFRenderer.jar:"
ARGUMENTS="${ARGUMENTS}./libs/persistence/derby.jar:./libs/persistence/hsqldb.jar:./libs/persistence/eclipselink.jar:./libs/persistence/ejb3-persistence.jar:./libs/persistence/javax.persistence_2.0.3.v201010191057.jar:./libs/persistence/mysql-connector-java-5.1.6-bin.jar:./libs/persistence/postgresql-8.4-701.jdbc4.jar:./libs/jargs.jar:./libs/ical/commons-codec-1.3.jar:./libs/ical/commons-logging-1.1.1.jar:./libs/ical/ical4j-vcard-0.9.5.jar:./libs/ical/backport-util-concurrent-3.1.jar:./libs/ical/commons-lang-2.4.jar:./libs/ical/gmaven-common-1.0-rc-5.jar:./libs/ical/gmaven-feature-api-1.0-rc-5.jar:./libs/ical/gmaven-feature-support-1.0-rc-5.jar:./libs/ical/gmaven-runtime-1.6-1.0-rc-5.jar:./libs/ical/gmaven-runtime-api-1.0-rc-5.jar:./libs/ical/gmaven-runtime-support-1.0-rc-5.jar:./libs/ical/ical4j-1.0.3.jar:./libs/pdfbox-1.8.2.jar:./libs/preflight-app-1.8.2.jar:"
ARGUMENTS="${ARGUMENTS}./libs/xmpbox-1.8.2.jar:./libs/json-simple-1.1.1.jar:./libs/tesseract-ocr/tess4j.jar:./libs/tesseract-ocr/ghost4j-0.3.1.jar:./libs/tesseract-ocr/jai_imageio.jar:./libs/tesseract-ocr/jna.jar:./libs/tesseract-ocr/junit-4.10.jar:./libs/mustang-1.3.1.jar:.:"

# Add correct SWT
#Darwin = Mac OS
if [ `uname -a | grep Darwin | wc -l` -gt 0 ]; then 
	ARGUMENTS="-XstartOnFirstThread ${ARGUMENTS}./libs/swt/org.eclipse.swt.cocoa.macosx.x86_64_3.5.1.v3555.jar"
else
	if [ `uname -a | grep x86_64 | wc -l` -gt 0 ]; then
		ARGUMENTS="${ARGUMENTS}./libs/swt/org.eclipse.swt.gtk.linux.x86_64_3.102.0.v20130605-1544.jar"
	else
		ARGUMENTS="${ARGUMENTS}./libs/swt/org.eclipse.swt.gtk.linux.x86_3.102.0.v20130605-1544.jar"
	fi
fi

ARGUMENTS="${ARGUMENTS} GUILayer/MainWindow $@"

if [ -f $FILE ];
then
# java_home is set
$FILE $ARGUMENTS
else
# lets hope java is at least in the classpath
java $ARGUMENTS
fi