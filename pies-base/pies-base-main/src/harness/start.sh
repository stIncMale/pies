#!/bin/bash

#This script is a RECOMMENDED way to start the Application.
#This script MAY return -1 in if execution of function from functions.sh failed.

#Directory that contains this script.
readonly THIS_SCRIPT_DIRNAME=`cd "$(dirname "${0}")"; pwd`
#Application Home directory.
APP_HOME=`dirname "${THIS_SCRIPT_DIRNAME}"`

source "${THIS_SCRIPT_DIRNAME}/functions.sh"
source "${THIS_SCRIPT_DIRNAME}/environment.sh"

#A file where to write standard output and error streams of the Java process.
readonly JAVA_CONSOLE_LOG_FILE="${APP_LOGS_LOCATION}/${JAVA_CONSOLE_LOG}"

#PID File - a file where to write PID of the Java process.
readonly APP_PID_FILE="${APP_RUNTIME_LOCATION}/application.pid"

#Lock File - a file used to prevent repeated start of the same instance of Application.
readonly APP_LOCK_FILE="${APP_RUNTIME_LOCATION}/application.lock"

#Class that contains entry point to Java application.
readonly APP_MAIN_CLASS="com.gl.vn.me.ko.pies.base.main.Main"

#Transform paths according to execution environment.
if [ "${OPERATING_SYSTEM_TYPE}" == "Windows-Cygwin" ]
then
	JAVA_CLASSPATH=`cygwinToWindowsPath "${JAVA_CLASSPATH}"`
	APP_HOME=`cygwinToWindowsPath "${APP_HOME}"`
	APP_LOGS_LOCATION=`cygwinToWindowsPath "${APP_LOGS_LOCATION}"`
	APP_CONFIGS_LOCATION=`cygwinToWindowsPath "${APP_CONFIGS_LOCATION}"`
	APP_RUNTIME_LOCATION=`cygwinToWindowsPath "${APP_RUNTIME_LOCATION}"`
fi

#Append Application-specific -D Java options to JAVA_OPTIONS.
JAVA_OPTIONS="${JAVA_OPTIONS} -Dpies.appHome=\"${APP_HOME}\""
JAVA_OPTIONS="${JAVA_OPTIONS} -Dpies.logsLocation=\"${APP_LOGS_LOCATION}\""
JAVA_OPTIONS="${JAVA_OPTIONS} -Dpies.configsLocation=\"${APP_CONFIGS_LOCATION}\""
JAVA_OPTIONS="${JAVA_OPTIONS} -Dpies.runtimeLocation=\"${APP_RUNTIME_LOCATION}\""
JAVA_OPTIONS="${JAVA_OPTIONS} -Dpies.consoleCharset=\"${JAVA_CONSOLE_CHARSET}\""

#Output environment variables.
log "Environment:"
log "	OPERATING_SYSTEM_TYPE = ${OPERATING_SYSTEM_TYPE}"
log "	FILE_PATH_SEPARATOR = ${FILE_PATH_SEPARATOR}"
log "	JAVA_EXECUTABLE = ${JAVA_EXECUTABLE}"
log "	JAVA_OPTIONS = ${JAVA_OPTIONS}"
log "	JAVA_CLASSPATH = ${JAVA_CLASSPATH}"
log "	JAVA_CONSOLE_CHARSET = ${JAVA_CONSOLE_CHARSET}"
log "	JAVA_CONSOLE_LOG_HISTORY = ${JAVA_CONSOLE_LOG_HISTORY}"
log "	JAVA_CONSOLE_LOG = ${JAVA_CONSOLE_LOG}"
log "	APP_HOME = ${APP_HOME}"
log "	APP_LOGS_LOCATION = ${APP_LOGS_LOCATION}"
log "	APP_CONFIGS_LOCATION = ${APP_CONFIGS_LOCATION}"
log "	APP_RUNTIME_LOCATION = ${APP_RUNTIME_LOCATION}"
log "	APP_MAIN_CLASS = ${APP_MAIN_CLASS}"
log ""

#Check that APP_LOCK_FILE doesn't exist.
#This is just a auxiliary check, because Application performs the same check.
if [ -e "${APP_LOCK_FILE}" ]
then
	echoError "Application can't be started because Lock File ${APP_LOCK_FILE} exists"
	exitError
fi

#Check if file JAVA_EXECUTABLE exists.
if ! [ -e "${JAVA_EXECUTABLE}" ]
then
	echoError "Application can't be started because file ${JAVA_EXECUTABLE} doesn't exist"
	exitError
fi

#Deal with JAVA_CONSOLE_LOG_FILE.
log "Log files:"
if [ -e "${JAVA_CONSOLE_LOG_FILE}" ]
then
	#Backup log file if necessary.
	if [ "${JAVA_CONSOLE_LOG_HISTORY}" == "true" ]
	then
		JAVA_CONSOLE_LOG_FILE_BACKUP=`backupFile "${JAVA_CONSOLE_LOG_FILE}"`
		log "	File ${JAVA_CONSOLE_LOG_FILE} is moved to ${JAVA_CONSOLE_LOG_FILE_BACKUP}"
		createFile "${JAVA_CONSOLE_LOG_FILE}"
		log "	File ${JAVA_CONSOLE_LOG_FILE} is created"
	else
		log "	Console log history is disabled"
	fi
else
	createFile "${JAVA_CONSOLE_LOG_FILE}"
	log "	File ${JAVA_CONSOLE_LOG_FILE} is created"
fi
log ""

#Start the application.
log \
"Starting the Application by executing the following command:"
START_COMMAND="nohup \
\"${JAVA_EXECUTABLE}\" \
${JAVA_OPTIONS} \
-cp \"${JAVA_CLASSPATH}\" \
${APP_MAIN_CLASS} \
&>\"${JAVA_CONSOLE_LOG_FILE}\" &"
log "	${START_COMMAND}"
eval "${START_COMMAND}"
readonly JAVA_PID="${!}"
log ""

#Write APP_PID_FILE
createFile "${APP_PID_FILE}"
echo "${JAVA_PID}" > "${APP_PID_FILE}"
log "PID of the Java process is ${JAVA_PID}"