# 
# spec file for gnuaccounting
#
# Copyright (c) 2012 SUSE LINUX Products GmbH, Nuernberg, Germany.
#
# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon. The license for this file, and modifications and additions to the
# file, is the same license as for the pristine package itself (unless the
# license for the pristine package is not an Open Source License, in which
# case the license is the MIT License). An "Open Source License" is a
# license that conforms to the Open Source Definition (Version 1.9)
# published by the Open Source Initiative.

# Please submit bugfixes or comments via http://bugs.opensuse.org/#
#

Name:          	gnuaccounting
Summary:       	A open source invoicing and accounting application
Version:       	0.8.9
Release:       	1
Group:         	Productivity/Office/Finance
License:       	GPLv3
URL:           	http://www.gnuaccounting.org
Requires:      	java >= 1.6.0
#Requires:     	pdfrenderer
Requires:       gnuaccounting-libs
#Requires:	hsqldb
#Requires:	jargs
#Requires:	dom4j
#Requires:	hibernate, hibernate-annotations
#Requires:	barbecue, 
#mail, registry
Requires:	mysql-connector-java
#Requires:      postgresql-jdbc4
#Requires:	junit, juh, jut, jta, jurt, ridl, slf4j, sandbox
#Requires:	hbci4java
BuildRequires:  unzip
BuildRequires:  update-alternatives
BuildRequires:  java-devel
BuildRequires:  ant >= 1.7.1
BuildRequires:  ant-nodeps
BuildRequires:  dos2unix
BuildRequires:  update-desktop-files
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#Source0:    http://www.gnuaccounting.org/gnuaccounting-nightly.tar.gz
Source0:        %{name}082.zip
Source1:	%{name}.desktop
Source2:	%{name}.png
Source3:	%{name}.sh
#BuildArch:	noarch

%description
A java open source invoicing and accounting application.
It creates and administrates invoices, credit memos, delivery notes, 
bills etc.
It utilizes OpenOffice for viewing and printing generated documents.

Features

  integrating
  -   OpenOffice.org,
  -   HBCI support
  -   HSQLDB or MySQL databases
  providing imports or exports from respectively to
  -   Winston (german online tax declaration software)
  -   Moneyplex and Starmoney (home banking software)
  -   Taskcoach and KTimeTracker (time tracking software)

%package libs
Summary:        GNUaccounting additional binary libs
License:        GPL
Requires:       gnuaccounting
Group:          Productivity/Office/Finance

%description libs
This package contains binary libs required by gnuaccounting.

%package doc
Summary:        GNUaccounting documentation package
License:        GPL
Requires:       gnuaccounting
Group:          Productivity/Office/Finance
BuildArch:	noarch

%description doc
This package contains documentation for gnuaccounting.

%package samples
Summary:	GNUaccounting samples
Requires:       gnuaccounting

%description samples
This package contains some sample files for testing


%prep
# cp gnuaccounting-nightly.tar.gz $RPM_SOURCE_DIR

%setup -n %{name}
# clean sources
%__rm *.bat
%__rm *.nsi
%__rm *.ico
%__rm appLayer/*.java
%__rm dataLayer/*.java
%__rm GUILayer/*.java
#%__rm libs/*win32*.jar 
%__rm libs/hbci/*win32*
%__rm libs/hbci/*mac*
%__rm libs/noa/*.dll
#%__rm libs/PDFRenderer.jar
%__rm libs/noa/*.zip
#%__rm */*.java

dos2unix README TODO HISTORY RELEASE_NOTES

%build
%ant init compile 


%install
#export NO_BRP_CHECK_BYTECODE_VERSION=true
install -d -m 755 %{buildroot}%{_datadir}/%{name}
# install -d -m 755 %{buildroot}%{_datadir}/%{name}/appLayer
cp -R appLayer/	%{buildroot}%{_datadir}/%{name}/
# install -d -m 755 %{buildroot}%{_datadir}/%{name}/dataLayer
cp -R dataLayer/ %{buildroot}%{_datadir}/%{name}/
# install -d -m 755 %{buildroot}%{_datadir}/%{name}/GUILayer
cp -R GUILayer/ %{buildroot}%{_datadir}/%{name}/
# install -d -m 755 %{buildroot}%{_datadir}/%{name}/init
cp -R init/ %{buildroot}%{_datadir}/%{name}/
install -d -m 755 %{buildroot}%{_libdir}/%{name}
%ifarch %{ix86}
mv libs/noa/libnativeview.so %{buildroot}%{_libdir}/%{name}/
rm libs/noa/64bit/libnativeview.so
%else
mv libs/noa/64bit/libnativeview.so %{buildroot}%{_libdir}/%{name}/
rm libs/noa/libnativeview.so
%endif
# install -d -m 755 %{buildroot}%{_datadir}/%{name}/libs
cp -R libs/ %{buildroot}%{_datadir}/%{name}/
# install -d -m 755 %{buildroot}%{_datadir}/%{name}/pics
# cp -R pics/ %{buildroot}%{_datadir}/%{name}/
# install -d -m 755 %{buildroot}%{_datadir}/%{name}/samples
cp -R samples/ %{buildroot}%{_datadir}/%{name}/

# startscript
install -d -m 755 %{buildroot}%{_bindir}
install -m 755 %{SOURCE3} %{buildroot}%{_bindir}/%{name}
#install -m 755 %{name}.sh %{buildroot}%{_datadir}/%{name}/%{name}
#ln -sf %{_datadir}/%{name}/%{name} %{buildroot}%{_bindir}/%{name}

#$RPM_BUILD_ROOT/usr/local/bin/gnuaccounting.sh
# install -m 644 tac_plus.1 $RPM_BUILD_ROOT/usr/local/man/man1/tac_plus.1
# install -m 644 tac_plus.confg $RPM_BUILD_ROOT/etc/tac_plus.confg 

# Icon
install -d -m 755 %{buildroot}%{_datadir}/pixmaps/
install -D -p -m 644 %{SOURCE2} %{buildroot}%{_datadir}/pixmaps/

# Desktop menu entry
install -d -m 755 %{buildroot}%{_datadir}/applications
install -m 644 %{SOURCE1} %{buildroot}%{_datadir}/applications/%{name}.desktop
%suse_update_desktop_file %{name}

# Documentation
install -d -m 755 %{buildroot}%{_docdir}/%{name}
cp -R docs/* %{buildroot}%{_docdir}/%{name}/

%post
#/sbin/ldconfig

%postun
#/sbin/ldconfig

%clean
%__rm -rf "%{buildroot}"

%files
%defattr(-,root,root,-)
%{_bindir}/*
#%dir %{_datadir}/%{name}
%{_datadir}/%{name}/
#%{_datadir}/%{name}/appLayer
#%{_datadir}/%{name}/dataLayer
#%{_datadir}/%{name}/GUILayer
#%{_datadir}/%{name}/init
#%{_datadir}/%{name}/pics
#%{_datadir}/%{name}/samples
%{_datadir}/applications/%{name}.desktop
%{_datadir}/pixmaps/%{name}.png

%exclude %{_datadir}/%{name}/samples
%exclude %{_datadir}/%{name}/libs
%exclude %{_docdir}/%{name}

#%doc README TODO HISTORY RELEASE_NOTES 

%files libs
%defattr(-,root,root,-)
%{_datadir}/%{name}/libs/
%dir %{_libdir}/%{name}
%{_libdir}/%{name}/libnativeview.so

%files doc
%defattr(-,root,root,-)
%dir %{_docdir}/%{name}
%{_docdir}/%{name}/*

%doc README TODO HISTORY RELEASE_NOTES

%files samples
%defattr(-,root,root,-)
%dir %{_datadir}/%{name}/samples

%changelog
