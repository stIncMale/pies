#!/bin/bash

#This script MAY be used to initiate interaction (dialog) between PIES Initiator application and other PIES Applications.
#This script MAY return -1 in if execution of function from functions.sh failed.

#Directory that contains this script.
readonly THIS_SCRIPT_DIRNAME=`cd "$(dirname "${0}")"; pwd`
#Application Home directory.
APP_HOME=`dirname "${THIS_SCRIPT_DIRNAME}"`

source "${THIS_SCRIPT_DIRNAME}/functions.sh"
source "${THIS_SCRIPT_DIRNAME}/environment.sh"

#A phrase to say.
readonly PHRASE="${1}"
#Lock File - a file used to prevent repeated start of the same instance of Application.
readonly APP_LOCK_FILE="${APP_RUNTIME_LOCATION}/application.lock"
readonly APP_LOCK_FILE_LAST_MODIFICATION_DATE=`getLastModifyTime "${APP_LOCK_FILE}"`

#Check that APP_LOCK_FILE exists.
if ! [ -e "${APP_LOCK_FILE}" ]
then
	echoError "Can't initiate dialog because Initiator isn't working: Lock File ${APP_LOCK_FILE} doesn't exist"
	exitError
fi

#Control Server address.
CONTROL_SERVER_ADDRESS=`cat "${APP_LOCK_FILE}"`

#Initiate dialog.
log \
"Initiating dialog by executing the following command:"
SAY_COMMAND="curl \
http://${CONTROL_SERVER_ADDRESS}/utf8string/${PHRASE} -XPOST \
-s -S --connect-timeout 1 --max-time 5"
log "	${SAY_COMMAND}"
SAY_RESPONSE=`eval "${SAY_COMMAND}"`
SAY_STATUS="${?}"
log ""
if [ "${SAY_STATUS}" == "0" ] #last process is completed successfully, i.e. with exit status 0
then
	log "Control Server response:"
	log "	${SAY_RESPONSE}"
	log ""
fi