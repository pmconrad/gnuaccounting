; gnuaccounting.nsi
;
; This script is perhaps one of the simplest NSIs you can make. All of the
; optional settings are left to their default settings. The installer simply 
; prompts the user asking them where to install, and drops a copy of gnuaccounting.nsi
; there. 

;--------------------------------

; The name of the installer
Name "Gnuaccounting"

; The file to write
OutFile "gnuaccounting.exe"

Icon "gnuaccounting.ico"

; The default installation directory
InstallDir $PROGRAMFILES\gnuaccounting

; Request application privileges for Windows Vista
RequestExecutionLevel admin ; need admin, not user, because otherwise no write privs on program files

;--------------------------------

; Pages
Page components
Page directory
Page instfiles

;--------------------------------
; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\gnuaccounting"
  CreateShortCut "$SMPROGRAMS\gnuaccounting\Uninstall.lnk" "$INSTDIR\uninstall.exe"
  CreateShortCut "$SMPROGRAMS\gnuaccounting\gnuaccounting.lnk" "$INSTDIR\gnuaccounting.bat" "" "$INSTDIR\gnuaccounting.bat" 0 SW_SHOWMINIMIZED
  ShellLink::SetShortCutWorkingDirectory $SMPROGRAMS\gnuaccounting\gnuaccounting.lnk $INSTDIR
  ShellLink::SetShortCutIconLocation "$SMPROGRAMS\gnuaccounting\gnuaccounting.lnk" "$INSTDIR\gnuaccounting.ico"

  CreateShortCut "$SMPROGRAMS\gnuaccounting\gnuaccounting-troubleshoot.lnk" "$INSTDIR\gnuaccounting-troubleshoot.bat" "" "$INSTDIR\gnuaccounting-troubleshoot.bat" 0 SW_SHOWNORMAL
  ShellLink::SetShortCutWorkingDirectory $SMPROGRAMS\gnuaccounting\gnuaccounting-troubleshoot.lnk $INSTDIR
  ShellLink::SetShortCutIconLocation "$SMPROGRAMS\gnuaccounting\gnuaccounting-troubleshoot.lnk" "$INSTDIR\gnuaccounting.ico"
  
  
SectionEnd
;--------------------------------

; Uninstaller

Section "Uninstall"
  
  ; Remove files and uninstaller
  RMDir /r "$INSTDIR"

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\gnuaccounting\*.*"

  ; remove from system control add/remove programs
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\gnuaccounting"
  ; Remove directories used
  RMDir "$SMPROGRAMS\gnuaccounting"
  RMDir /r "$%HOMEDRIVE%$%HOMEPATH%\.gnuaccounting"

SectionEnd

; The stuff to install
Section "Gnuaccounting" ;No components page, name is not important
  SectionIn RO ; required

  ; Set output path to the installation directory.
  SetOutPath $INSTDIR

  ; Put file there
  File /r init
  File /r libs
  File /r samples
  File /r docs
  File /r META-INF
  File /r appLayer
  File /r dataLayer
  File /r GUILayer

  File gnuaccounting.bat  
  File gnuaccounting-troubleshoot.bat
  File gnuaccounting.ico
  File LICENSE
  File README
  File HISTORY
  File TODO
  File RELEASE_NOTES

  ; write values in add/remove programs
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\gnuaccounting" \
                 "DisplayName" "Gnuaccounting - open source java accounting"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\gnuaccounting" \
                 "UninstallString" "$\"$INSTDIR\uninstall.exe$\""				 
  WriteRegStr HKLM 	"Software\Microsoft\Windows\CurrentVersion\Uninstall\gnuaccounting" \
				"QuietUninstallString" "$\"$INSTDIR\uninstall.exe$\" /S"  
  WriteRegStr HKLM 	"Software\Microsoft\Windows\CurrentVersion\Uninstall\gnuaccounting" \
				"Publisher" "usegroup"  
  WriteUninstaller "uninstall.exe"
  
SectionEnd ; end the section
