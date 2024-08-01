@echo off
if not exist ..\java\bin\javaw.exe goto :ERROR
if not exist ..\OpenOfficePortable\X-ApacheOpenOffice.exe goto :ERROR

REM Everything OK
REM Set vars for portableGit
@set PLINK_PROTOCOL=ssh
@setlocal
@for /F "delims=" %%I in ("%~dp0") do @set git_install_root=%%~fI..\PortableGit\
@set path=%git_install_root%\bin;%git_install_root%\mingw\bin;%git_install_root%\cmd;%PATH%
@if "%HOME%"=="" @set HOME=%USERPROFILE%
REM @cd %HOME%
REM @start %COMSPEC%

if exist .\.git goto :START

:FIRSTRUN
if not exist ..\PortableGit goto :START
echo Initializing repository for first use...
git init .
goto :START 

:START
REM now start GA
..\java\bin\javaw -Djava.library.path=.\libs -cp ".\libs\swt\swt.jar;.\libs\swt\org.eclipse.core.commands-3.12.100.jar;.\libs\swt\org.eclipse.equinox.common-3.19.100.jar;.\libs\swt\org.eclipse.jface-3.34.0.jar;.\libs\swt\org.eclipse.osgi-3.20.0.jar;.\libs\swt\org.eclipse.swt.win32.win32.x86_3.102.0.v20130605-1544.jar;.\libs\swt\org.eclipse.swt_3.102.0.v20130605-1539.jar;.\libs\noa\noa-libre.jar;.\libs\noa\junit-3.8.1.jar;.\libs\noa\ridl.jar;.\libs\noa\bootstrapconnector.jar;.\libs\noa\jurt.jar;.\libs\noa\sandbox.jar;.\libs\noa\java_uno_accessbridge.jar;.\libs\noa\jut.jar;.\libs\noa\unoil.jar;.\libs\noa\officebean.jar;.\libs\noa\unoloader.jar;.\libs\noa\juh.jar;.\libs\noa\registry-3.1.3.jar;.\libs\mail.jar;.\libs\hbci\hbci4java.jar;.\libs\barcode-google-zxing-2_2_core.jar;.\libs\barcode-google-zxing-2_2_javase.jar;.\libs\uk.co.mmscomputing.device.sane.jar;.\libs\uk.co.mmscomputing.device.twain.jar;.\libs\PDFRenderer.jar;.\libs\persistence\derby.jar;.\libs\persistence\hsqldb.jar;.\libs\persistence\eclipselink.jar;.\libs\persistence\ejb3-persistence.jar;.\libs\persistence\javax.persistence_2.0.3.v201010191057.jar;.\libs\persistence\mysql-connector-java-5.1.6-bin.jar;.\libs\persistence\postgresql-8.4-701.jdbc4.jar;.\libs\jargs.jar;.\libs\ical\commons-codec-1.3.jar;.\libs\ical\commons-logging-1.1.1.jar;.\libs\ical\ical4j-vcard-0.9.5.jar;.\libs\ical\backport-util-concurrent-3.1.jar;.\libs\ical\commons-lang-2.4.jar;.\libs\ical\gmaven-common-1.0-rc-5.jar;.\libs\ical\gmaven-feature-api-1.0-rc-5.jar;.\libs\ical\gmaven-feature-support-1.0-rc-5.jar;.\libs\ical\gmaven-runtime-1.6-1.0-rc-5.jar;.\libs\ical\gmaven-runtime-api-1.0-rc-5.jar;.\libs\ical\gmaven-runtime-support-1.0-rc-5.jar;.\libs\ical\ical4j-1.0.3.jar;.\libs\pdfbox-1.8.2.jar;.\libs\preflight-app-1.8.2.jar;.\libs\xmpbox-1.8.2.jar;.\libs\json-simple-1.1.1.jar;.\libs\tesseract-ocr\tess4j.jar;.\libs\tesseract-ocr\ghost4j-0.3.1.jar;.\libs\tesseract-ocr\jai_imageio.jar;.\libs\tesseract-ocr\jna.jar;.\libs\tesseract-ocr\junit-4.10.jar;.\libs\mustang-1.3.0.jar;.;libs\jaxb-api-2.3.1.jar" GUILayer/MainWindow --configPath=. --officePath="../OpenOfficePortable/Bin/OpenOffice 4/" %*
goto :FINISH

:ERROR
echo Required files for Gnuaccounting portable (portable Java or portable OpenOffice.org) seem to be missing.
echo Please refer to the manual (docs\gnuaccounting-manual-en.odt) for further information.
pause
goto :END

:FINISH
if not exist ..\PortableGit goto :END

git add derby0.8.x\*
git add config.xml
git add 0000\*
git commit -m "Gnuaccounting auto-checkin"

:END
