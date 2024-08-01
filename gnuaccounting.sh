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
ARGUMENTS="-Djava.library.path=./libs"

CP=.
for i in swt/swt \
		swt/org.eclipse.core.commands \
		swt/org.eclipse.equinox.common \
		swt/org.eclipse.jface \
		swt/org.eclipse.osgi \
		noa/noa-libre \
		noa/ridl \
		noa/bootstrapconnector \
		noa/jurt \
		noa/sandbox \
		noa/java_uno_accessbridge \
		noa/jut \
		noa/unoil \
		noa/officebean \
		noa/unoloader \
		noa/juh \
		noa/registry \
		mail \
		hbci/hbci4java \
		barcode-google-zxing-2_2_core \
		barcode-google-zxing-2_2_javase \
		uk.co.mmscomputing.device.sane \
		uk.co.mmscomputing.device.twain \
		PDFRenderer \
		persistence/derby- \
		persistence/hsqldb \
		persistence/eclipselink \
		persistence/javax.persistence \
		persistence/mysql-connector-j \
		persistence/postgresql \
		jargs \
		ical/commons-codec \
		ical/commons-logging \
		ical/ical4j-vcard \
		ical/backport-util-concurrent \
		ical/commons-lang \
		ical/gmaven-common \
		ical/gmaven-feature-api \
		ical/gmaven-feature-support \
		ical/gmaven-runtime \
		ical/gmaven-runtime-api \
		ical/gmaven-runtime-support \
		ical/ical4j \
		pdfbox \
		preflight-app \
		xmpbox \
		json-simple \
		tesseract-ocr/tess4j \
		tesseract-ocr/ghost4j \
		tesseract-ocr/jai_imageio \
		tesseract-ocr/jna \
		mustang \
		xb-api \
		jaxb-api
		do
	case "$i" in
		*/*) CP="$CP:$(find "libs/${i%/*}" -name "${i##*/}"\*.jar | head -1)"; ;;
		*) CP="$CP:$(find libs -name "$i"\*.jar | head -1)"; ;;
	esac
done

if uname -a | grep -q Darwin; then
	ARGUMENTS="-XstartOnFirstThread ${ARGUMENTS}"
fi

if [ -f "$FILE" ];
then
# java_home is set
"$FILE" $JAVA_OPTS $ARGUMENTS -cp $CP GUILayer/MainWindow "$@"
else
# lets hope java is at least in the classpath
java $JAVA_OPTS $ARGUMENTS -cp $CP GUILayer/MainWindow "$@"
fi
