****************************************************************************************
**                                                                                    **
**  Response file to perform silent install of Oracle Database 10g Express Edition    **
**                                                                                    **
**  Values for the following variables are configurable:                              **
**  szDir - Provide a valid path                                                      **
**  TNSPort - Provide any valid available port number                                 **
**  MTSPort - Provide any valid available port number                                 **
**  HTTPPort - Provide any valid available port number                                **
**  SYSPassword - Provide a valid password string                                     **
**  bOpt1 - 1 or 0 : 1 launches the Database Homepage at end of install and 0 doesn't **
**                                                                                    **
****************************************************************************************

[{F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-DlgOrder]
Dlg0={F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-SdWelcome-0
Count=9
Dlg1={F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-SdLicense2Rtf-0
Dlg2={F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-SdComponentDialog-0
Dlg3={F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-AskTNSPort-13013
Dlg4={F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-AskMTSPort-13012
Dlg5={F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-AskHTTPPort-13014
Dlg3={F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-AskSYSPassword-13011
Dlg4={F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-SdStartCopy-0
Dlg5={F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-SdFinish-0
[{F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-SdWelcome-0]
Result=1
[{F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-SdLicense2Rtf-0]
Result=1
[{F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-SdComponentDialog-0]
szDir=C:\Oracle\OracleXE\
Component-type=string
Component-count=1
Component-0=DefaultFeature
Result=1
[{F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-AskTNSPort-13013]
TNSPort=1521
Result=1
[{F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-AskMTSPort-13012]
MTSPort=2030
Result=1
[{F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-AskHTTPPort-13014]
HTTPPort=8083
Result=1
[{F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-AskSYSPassword-13011]
SYSPassword=oracle
Result=1
[{F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-SdStartCopy-0]
Result=1
[{F0BC0F9E-C4A8-485C-93ED-424DB9EA3F75}-SdFinish-0]
Result=1
bOpt1=1
bOpt2=0
