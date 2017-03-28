@echo off

IF "%DAVE_ROOT%"=="" (
 set DAVE_ROOT=%0\..\
)

set DAVE_LIB=%DAVE_ROOT%\lib
set DAVE_ETC=%DAVE_ROOT%\etc
set DAVE_LOG=%DAVE_ROOT%\log

java %JAVA_OPTS% -DDAVE_LOG=%DAVE_LOG% -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory -Dlogback.configurationFile=%DAVE_ETC%/logback.xml -jar %DAVE_LIB%/dave-1.0-SNAPSHOT-fat.jar -conf %DAVE_ETC%/dave.json
