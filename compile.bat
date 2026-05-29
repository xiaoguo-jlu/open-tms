@echo off
set JAVA_HOME=D:\Program Files\Java\jdk-21.0.11
set PATH=%JAVA_HOME%\bin;%PATH%
D:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\plugins\maven\lib\maven3\bin\mvn.cmd compile -pl basedata -am -DskipTests