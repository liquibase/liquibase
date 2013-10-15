# Settings
%define packagedby "Nathan Voxland <nathan@voxland.net>"
# Enable / Disable sub-packages
%define mysql        0
%define oracle       0
%define postgresql   0
%define mssql        0
%define sqlite       0
# Liquibase Package
%define lqver        2.0.0
%define buildnum     1
## MySQL Jar
%define mysqljar     mysql-connector-java-5.1.10.jar
%define gpl2license  gpl-2.0.txt
## Oracle Files
%define oraLicense   oracle-license.txt 
%define orajarjdbc   ojdbc14.jar
%define orajari18n   orai18n.jar
## Postgresql
%define pgsqljar     postgresql-8.4-701.jdbc4.jar
%define bsdLicense   BSD-License.txt
## Microsoft SQL
%define mssqljar     mssql-sqljdbc4-2.0.jar
%define msLicense    MsSQLLicense.txt
## SQLite
%define sqlitejar    sqlite-jdbc-3.6.20.1.jar
%define apache2license apache2license.txt

Name: liquibase
Summary: Liquibase Database Refactoring Tool
Version: %{lqver}
Release: %{buildnum}%{?dist}
License: Apache 2.0
Group: Applications/Databases
Source0: %{name}-%{version}.tar.gz
Source1: %{mysqljar}
Source2: %{oraLicense}
Source3: %{orajarjdbc}
Source4: %{orajari18n}
Source5: %{pgsqljar}
Source6: %{bsdLicense}
Source7: %{mssqljar}
Source8: %{msLicense}
Source9: %{gpl2license}
Source10: %{sqlitejar}
Source11: %{apache2license}

BuildRoot: %{_tmppath}/build-root-%{name}
BuildArch: noarch
Packager: %{packagedby}
Url: http://liquibase.org/
Vendor: LiquiBase (http://www.liquibase.org)
Provides: liquibase = %{version}-%{release}

%description
LiquiBase is an open source (Apache 2.0 License), database-independent library for tracking,
managing and applying database changes. It is built on a simple premise: All 
database changes are stored in a human readable yet trackable form and checked 
into source control.

%if %{mysql}
%package mysql
Summary:        MySQL Jar file
Group:          Development/Languages
Requires:       liquibase
License:        GPL
Url:            http://dev.mysql.com/downloads/connector/j/

%description mysql
This package includes the MySQL jar file required by liquibase
%endif

%if %{oracle}
%package oracle
Summary:        Oracle Jar and license files
Group:          Development/Languages
Requires:       liquibase
License:        Commerical
Url:            http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/htdocs/jdbc_10201.html

%description oracle
This package includes the Oracle jar file required by liquibase
%endif

%if %{postgresql}
%package postgresql
Summary:        PostgreSQL Jar file
Group:          Development/Languages
Requires:       liquibase
License:        BSD
Url:            http://jdbc.postgresql.org/download.html

%description postgresql
This package includes the PostgreSQL jar file required by liquibase as well
as the BSD license that PostgreSQL is licensed under.
%endif

%if %{mssql}
%package mssql
Summary:        Microsoft SQL Jar file
Group:          Development/Languages
Requires:       liquibase
License:        Commercial
Url:            http://www.microsoft.com/downloads/details.aspx?FamilyID=99b21b65-e98f-4a61-b811-19912601fdc9&displaylang=en

%description mssql
This package includes the Microsoft JDBC 2.0 jar file required by liquibase 
as well as the license the jar is licensed under. This release of the JDBC 
Driver is JDBC 4.0 compliant and runs on the Java Development Kit (JDK) 
version 5.0 or later.
%endif

%if %{sqlite}
%package sqlite
Summary:        Sqlite Jar file
Group:          Development/Languages
Requires:       liquibase
License:        Apache License
Url:            http://www.xerial.org/trac/Xerial/wiki/SQLiteJDBC

%description sqlite
This package includes the Sqlite jar file required by liquibase as well
as the Apache 2 license. The jar file includes native libraries for Linux
(i386 and x86_64), Windows (i386 and x86_64), and MacOSX (i386 and x86_64).
If the library is used on a platform where native extensions are not available,
the pure java version will be used instead.
%endif

%pre

%prep
%setup -q -n %{name}-%{version}
%patch0 -p1

%build

%install
%{__rm} -rf %{buildroot}
%{__mkdir} -p %{buildroot}%{_libdir}/%{name}/lib/
%{__mkdir} -p %{buildroot}%{_bindir}
%{__install} -m 0644 -D -p %{name}-%{version}.jar %{buildroot}%{_libdir}/%{name}
%{__install} -m 0755 -D -p %{name} %{buildroot}%{_bindir}

# Profile.d file
%{__mkdir} -p %{buildroot}%{_sysconfdir}/profile.d/
%{__cat} <<EOF >%{buildroot}%{_sysconfdir}/profile.d/liquibase.sh
export LIQUIBASE_HOME=%{_libdir}/%{name}/
EOF

# Sub-packages
%if %{mysql}
%{__install} -m 0644 -D -p %{SOURCE1} %{buildroot}%{_libdir}/%{name}/lib/
%{__install} -m 0644 -D -p %{SOURCE9} %{buildroot}%{_libdir}/%{name}/lib/
%endif

%if %{oracle}
%{__install} -m 0644 -D -p %{SOURCE2} %{buildroot}%{_libdir}/%{name}/lib/
%{__install} -m 0644 -D -p %{SOURCE3} %{buildroot}%{_libdir}/%{name}/lib/
%{__install} -m 0644 -D -p %{SOURCE4} %{buildroot}%{_libdir}/%{name}/lib/
%endif

%if %{postgresql}
%{__install} -m 0644 -D -p %{SOURCE5} %{buildroot}%{_libdir}/%{name}/lib/
%{__install} -m 0644 -D -p %{SOURCE6} %{buildroot}%{_libdir}/%{name}/lib/
%endif

%if %{mssql}
%{__install} -m 0644 -D -p %{SOURCE7} %{buildroot}%{_libdir}/%{name}/lib/
%{__install} -m 0644 -D -p %{SOURCE8} %{buildroot}%{_libdir}/%{name}/lib/
%endif

%if %{sqlite}
%{__install} -m 0644 -D -p %{SOURCE10} %{buildroot}%{_libdir}/%{name}/lib/
%{__install} -m 0644 -D -p %{SOURCE11} %{buildroot}%{_libdir}/%{name}/lib/
%endif

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%attr(0755,root,root) %{_sysconfdir}/profile.d/%{name}.sh
%doc docs/* changelog.txt LICENSE.txt
%{_libdir}/%{name}
%{_bindir}/%{name}

%if %{mysql}
%exclude %{_libdir}/%{name}/lib/%{mysqljar}
%exclude %{_libdir}/%{name}/lib/%{gpl2license}
%endif

%if %{oracle}
%exclude %{_libdir}/%{name}/lib/%{oraLicense}
%exclude %{_libdir}/%{name}/lib/%{orajarjdbc}
%exclude %{_libdir}/%{name}/lib/%{orajari18n}
%endif

%if %{postgresql}
%exclude %{_libdir}/%{name}/lib/%{pgsqljar}
%exclude %{_libdir}/%{name}/lib/%{bsdLicense}
%endif

%if %{mssql}
%exclude %{_libdir}/%{name}/lib/%{mssqljar}
%exclude %{_libdir}/%{name}/lib/%{msLicense}
%endif

%if %{sqlite}
%exclude %{_libdir}/%{name}/lib/%{sqlitejar}
%exclude %{_libdir}/%{name}/lib/%{apache2license}
%endif

%if %{mysql}
%files mysql
%defattr(0644,root,root)
%{_libdir}/%{name}/lib/%{mysqljar}
%{_libdir}/%{name}/lib/%{gpl2license}
%endif

%if %{oracle}
%files oracle
%defattr(0644,root,root)
%{_libdir}/%{name}/lib/%{oraLicense}
%{_libdir}/%{name}/lib/%{orajarjdbc}
%{_libdir}/%{name}/lib/%{orajari18n}
%endif

%if %{postgresql}
%files postgresql
%defattr(0644,root,root)
%{_libdir}/%{name}/lib/%{pgsqljar}
%{_libdir}/%{name}/lib/%{bsdLicense}
%endif

%if %{mssql}
%files mssql
%defattr(0644,root,root)
%{_libdir}/%{name}/lib/%{mssqljar}
%{_libdir}/%{name}/lib/%{msLicense}
%endif

%if %{sqlite}
%files sqlite
%defattr(0644,root,root)
%{_libdir}/%{name}/lib/%{sqlitejar}
%{_libdir}/%{name}/lib/%{apache2license}
%endif


%post

%changelog
* Mon Jan 25 2010 William Lovins <rpmbuild@icainformatics.com> - 1.9.5-1
- initial public version of spec


