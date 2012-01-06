set RESTART_EXIT_STATUS=121

:loop
java -jar bs-tester.jar %*
set status=%ERRORLEVEL%
if %status%==%RESTART_EXIT_STATUS% goto loop

