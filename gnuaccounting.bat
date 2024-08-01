@echo off

REM FOR /F "tokens=2*" %%a IN ('REG QUERY "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\1.8" /v JavaHome') DO set "JavaHome=%%b"

REM We need to find the install path of a 32 bit JRE also on a 64 bit system 
REM (since OpenOffice is always 32 bit on windows and the officebean does some JNI calls)

REM First we look in native locations which could be 32 bit JRE on 32 bit system or 64 bit JRE on 64 bit system
REM afterwards we look in the registry place for 32 bit apps on 64 bit systems (Wow6432Node) which will 
REM overwrite the variable with a potential 64 bit JRE  

for %%p in (HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\JavaSoft) do ( 
REM we're looking for JRE versions (%%v) 6-8 in their default locations
	for %%v in (6 7 8) do (
		FOR /F "tokens=2*" %%a IN ('REG QUERY "%%p\Java Runtime Environment\1.%%v" /v JavaHome 2^>nul') DO (
			set Located=%%b
		)
	)
)
rem check if JRE was located
if "%Located%"=="" goto else
rem if JRE located display message to user
rem update %JAVA_HOME%
set JAVA_HOME=%Located%
rem echo     Located JRE %jre_Version%
rem echo     located has been set to:
rem echo         %Located%
rem echo     JAVA_HOME has been set to:
rem echo         %JAVA_HOME%
goto endif

:else
rem if JRE was not located
rem if %JAVA_HOME% has been defined then use the existing value
echo     Could not locate Java
echo 	 Please download and install the Java Runtime Environment
echo     from https://www.java.com/en/download/
echo     in order to be able to start Gnuaccounting.
pause
if "%JAVA_HOME%"=="" goto NoExistingJavaHome
echo     Existing value of JAVA_HOME will be used:
echo         %JAVA_HOME%
goto endif

:NoExistingJavaHome
rem display message to the user that %JAVA_HOME% is not available
echo     No Existing value of JAVA_HOME is available
goto endif

:endif
rem clear the variables used by this script
set JRE_Version=
set Located=

"%JAVA_HOME%\bin\javaw" -Djava.library.path=.\libs -cp ".\libs\swt\swt.jar;.\libs\swt\org.eclipse.core.commands-3.12.100.jar;.\libs\swt\org.eclipse.equinox.common-3.19.100.jar;.\libs\swt\org.eclipse.jface-3.34.0.jar;.\libs\swt\org.eclipse.osgi-3.20.0.jar;.\libs\swt\org.eclipse.swt.win32.win32.x86_3.104.2.v20160212-1350.jar;.\libs\swt\org.eclipse.swt_3.104.2.v20160212-1350.jar;.\libs\noa\noa-libre.jar;.\libs\noa\junit-3.8.1.jar;.\libs\noa\ridl.jar;.\libs\noa\bootstrapconnector.jar;.\libs\noa\jurt.jar;.\libs\noa\sandbox.jar;.\libs\noa\java_uno_accessbridge.jar;.\libs\noa\jut.jar;.\libs\noa\unoil.jar;.\libs\noa\officebean.jar;.\libs\noa\unoloader.jar;.\libs\noa\juh.jar;.\libs\noa\registry-3.1.3.jar;.\libs\mail.jar;.\libs\hbci\hbci4java.jar;.\libs\barcode-google-zxing-2_2_core.jar;.\libs\barcode-google-zxing-2_2_javase.jar;.\libs\uk.co.mmscomputing.device.sane.jar;.\libs\uk.co.mmscomputing.device.twain.jar;.\libs\PDFRenderer.jar;.\libs\persistence\derby.jar;.\libs\persistence\hsqldb.jar;.\libs\persistence\eclipselink.jar;.\libs\persistence\ejb3-persistence.jar;.\libs\persistence\javax.persistence_2.0.3.v201010191057.jar;.\libs\persistence\mysql-connector-java-5.1.6-bin.jar;.\libs\persistence\postgresql-8.4-701.jdbc4.jar;.\libs\jargs.jar;.\libs\ical\commons-codec-1.3.jar;.\libs\ical\commons-logging-1.1.1.jar;.\libs\ical\ical4j-vcard-0.9.5.jar;.\libs\ical\backport-util-concurrent-3.1.jar;.\libs\ical\commons-lang-2.4.jar;.\libs\ical\gmaven-common-1.0-rc-5.jar;.\libs\ical\gmaven-feature-api-1.0-rc-5.jar;.\libs\ical\gmaven-feature-support-1.0-rc-5.jar;.\libs\ical\gmaven-runtime-1.6-1.0-rc-5.jar;.\libs\ical\gmaven-runtime-api-1.0-rc-5.jar;.\libs\ical\gmaven-runtime-support-1.0-rc-5.jar;.\libs\ical\ical4j-1.0.3.jar;.\libs\pdfbox-1.8.2.jar;.\libs\preflight-app-1.8.2.jar;.\libs\xmpbox-1.8.2.jar;.\libs\json-simple-1.1.1.jar;.\libs\tesseract-ocr\tess4j.jar;.\libs\tesseract-ocr\ghost4j-0.3.1.jar;.\libs\tesseract-ocr\jai_imageio.jar;.\libs\tesseract-ocr\jna.jar;.\libs\tesseract-ocr\junit-4.10.jar;.\libs\mustang-1.3.1.jar;.;libs\jaxb-api-2.3.1.jar" GUILayer/MainWindow %*
