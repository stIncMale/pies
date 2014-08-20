#!/bin/bash

#This file aggregates variables and constants that MAY be useful for the Administrator.
#
#One MAY use variable APP_HOME in order to refer to Application Home.
#One SHOULD use getOperatingSystemType() function to obtain value of OPERATING_SYSTEM_TYPE.
#One SHOULD use getFilePathSeparator() function to obtain value of FILE_PATH_SEPARATOR.
#
#Due to the fact that Harness scripts can be executed not only in UN*X shell, but also in Windows Cygwin shell,
#we need to distinguish shell-specific path and OS-specific path. Shell-specific path is always a UN*X path,
#but OS-specific path is UN*X for UN*X shell and Windows for Windows Cygwin shell.
#You are allowed to always specify OS-specific path variables in UN*X style,
#and Harness will convert the path to OS-specific value for you.
#OS-specific variables that are processed by Harness are marked with "#Auto-OS-specific" comment.
#If you use a value of OS-specific variable X in some other shell-specific variable Y,
#then Harness will not harm Y and it's value will be shell-specific.

#Application Home
#By default in this script APP_HOME is a parent of the directory which contains Harness start.sh script,
#but you MAY specify any path you need.
#Auto-OS-specific
APP_HOME="${APP_HOME}"

#Specifies if logging is enabled in Harness scripts that use environment.sh.
#Possible values:
#	"true"
#	"false"
readonly VERBOSE="true"

#Operating system type that includes knowledge about OS and shell.
#If by some reason function getOperatingSystemType() fails to correctly determine execution environment the value
#MAY be set manually.
#Possible values:
#	"Windows-Cygwin"
#	"UN*X"
readonly OPERATING_SYSTEM_TYPE=`getOperatingSystemType`

#A character that separates elements in a list of file paths.
#Value is based on the value of OPERATING_SYSTEM_TYPE variable.
#If by some reason function getFilePathSeparator() fails to correctly determine execution environment the value
#MAY be set manually.
readonly FILE_PATH_SEPARATOR=`getFilePathSeparator`

#A path to Java executable file.
readonly JAVA_EXECUTABLE="/usr/bin/java"

#Java options. Note that PIES Application-specific -D Java options are configured in separate variables.
JAVA_OPTIONS="${JAVA_OPTIONS} -server"
JAVA_OPTIONS="${JAVA_OPTIONS} -d64"
##EOL marker that will be used by JRE. This is a standard Java option.
##Particularly this EOL marker will be used for standard output and error streams and for Application logs.
##You MAY comment out this option in order to use OS-specific EOL marker. For example:
##	\r\n (Unicode code points U+000DU+000A) for Windows
##	\n (Unicode code point U+000A) for UN*X
JAVA_OPTIONS="${JAVA_OPTIONS} -Dline.separator=$'\n'"
##Oracle HotSpot JVM: memory options.
#JAVA_OPTIONS="${JAVA_OPTIONS} -Xms64M"
#JAVA_OPTIONS="${JAVA_OPTIONS} -Xmx64M"
##Oracle HotSpot JVM: debugging options.
#JAVA_OPTIONS="${JAVA_OPTIONS} -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n"
##Oracle HotSpot JVM: JMX server options.
#JAVA_OPTIONS="${JAVA_OPTIONS} -Dcom.sun.management.jmxremote.port=3000"
#JAVA_OPTIONS="${JAVA_OPTIONS} -Dcom.sun.management.jmxremote.ssl=false"
#JAVA_OPTIONS="${JAVA_OPTIONS} -Dcom.sun.management.jmxremote.authenticate=false"
##Oracle HotSpot JVM: allow Java Mission Control profiling.
#JAVA_OPTIONS="${JAVA_OPTIONS} -XX:+UnlockCommercialFeatures -XX:+FlightRecorder"

#Class path for Java executable.
#Note that Java require this to be an OS-specific path, and Harness do the job for you.
#Auto-OS-specific
JAVA_CLASSPATH=""
JAVA_CLASSPATH="${JAVA_CLASSPATH}${FILE_PATH_SEPARATOR}${APP_HOME}/lib-pies/*"
JAVA_CLASSPATH="${JAVA_CLASSPATH}${FILE_PATH_SEPARATOR}${APP_HOME}/lib-3rdparty/*"

#Name of the file where to write standard output and error streams of the Java process.
#Only name without path parts MUST be specified.
readonly JAVA_CONSOLE_LOG="console.log"

#Name of a charset to use for the standard output and error streams (that is for JAVA_CONSOLE_LOG file).
#Refer to IANA Charset Registry (http://www.iana.org/assignments/character-sets/character-sets.xhtml)
#for all theoretically possible values.
#Java option -Dfile.encoding is not used because it doesn't seem to be a standard property
#according to specification of the method java.lang.System.getProperties()
readonly JAVA_CONSOLE_CHARSET="UTF-8"

#Specifies if file specified by variable JAVA_CONSOLE_LOG need to be renamed at Application startup
#so that it will remain for history.
#The renamed file contains current date in the format year-month-day_hour-minute-second_timeZone.
#Possible values are:
#	"true"
#	"false"
readonly JAVA_CONSOLE_LOG_HISTORY="true"

#Logs Location - directory that by default contains all Application's log files,
#such as JAVA_CONSOLE_LOG_NAME (location of other logs MAY be changed via log4j2.xml located in APP_CONFIGS_LOCATION).
#Auto-OS-specific
APP_LOGS_LOCATION="${APP_HOME}/log"

#Configs Location - directory that contains all Application's configuration files.
#Auto-OS-specific
APP_CONFIGS_LOCATION="${APP_HOME}/config"

#Runtime Files Location - directory that contains all Application's files that are relevant to running Application,
#such as APP_PID_FILE_NAME and APP_LOCK_FILE_NAME files.
#Auto-OS-specific
APP_RUNTIME_LOCATION="${APP_HOME}/runtime"