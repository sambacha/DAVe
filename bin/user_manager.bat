@echo off

IF "%DAVE_ROOT%"=="" (
 set DAVE_ROOT=%0\..\
)

set DAVE_LIB=%DAVE_ROOT%\lib
set DAVE_ETC=%DAVE_ROOT%\etc

IF "%1"=="insert" (
set option_fragment=-Dcmd=insert -DuserName=%2 -DuserPassword=%3
GOTO process
)
IF "%1"=="list" (
set option_fragment=-Dcmd=list
GOTO process
)
IF "%1"=="delete" (
set option_fragment=-Dcmd=delete -DuserName=%2
GOTO process
)
echo Usage %0 CMD OPTIONS (where CMD must be in: [insert, delete, list])
GOTO end

:process
java %JAVA_OPTS% -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory %option_fragment% -cp %DAVE_LIB%\dave-1.0-SNAPSHOT-fat.jar com.deutscheboerse.risk.dave.util.UserManagerVerticle -conf %DAVE_ETC%\dave.json
GOTO end

:end


