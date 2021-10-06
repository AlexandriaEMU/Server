@echo off
title AlexandriaEMU - SERVER
:loop
"C:\Program Files (x86)\BellSoft\LibericaJRE-17-Full\bin\java.exe" -jar -Xmx1024m -Xms1024m server.jar
goto loop
PAUSE