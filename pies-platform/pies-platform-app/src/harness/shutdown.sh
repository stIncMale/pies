#!/bin/bash

#This script MAY be used to shutdown the Application.
#This script MAY return -1 in if execution of function from functions.sh failed.

#Directory that contains this script.
readonly THIS_SCRIPT_DIRNAME=`cd "$(dirname "${0}")"; pwd`
#Application Home directory.
APP_HOME=`dirname "${THIS_SCRIPT_DIRNAME}"`

source "${THIS_SCRIPT_DIRNAME}/functions.sh"
source "${THIS_SCRIPT_DIRNAME}/environment.sh"

#Lock File - a file used to prevent repeated start of the same instance of Application.
readonly APP_LOCK_FILE="${APP_RUNTIME_LOCATION}/application.lock"
readonly APP_LOCK_FILE_LAST_MODIFICATION_DATE=`getLastModifyTime "${APP_LOCK_FILE}"`

#Check that APP_LOCK_FILE exists.
if ! [ -e "${APP_LOCK_FILE}" ]
then
	echoError "Can't shutdown Application because it's not working: Lock File ${APP_LOCK_FILE} doesn't exist"
	exitError
fi

#Control Server address.
CONTROL_SERVER_ADDRESS=`cat "${APP_LOCK_FILE}"`

#Shutdown the application.
log \
"Shutting down the Application by executing the following command:"
SHUTDOWN_COMMAND="curl \
http://${CONTROL_SERVER_ADDRESS}/shutdown/ -XPUT \
-s -S --connect-timeout 1 --max-time 5"
log "	${SHUTDOWN_COMMAND}"
SHUTDOWN_RESPONSE=`eval "${SHUTDOWN_COMMAND}"`
SHUTDOWN_STATUS="${?}"
log ""
if [ "${SHUTDOWN_STATUS}" == "0" ] #last process is completed successfully, i.e. with exit status 0
then
	log "Control Server response:"
	log "	${SHUTDOWN_RESPONSE}"
	log ""
fi

#Wait till Lock File will be deleted.
while : ; do
	#Break is Lock File doesn't exist.
	! [[ -f ${APP_LOCK_FILE} ]] && break
	#Break if Lock File was modified. We assume, that in this case Lock File was removed and recreated
	#because Application has stopped and was started again.
	if ! [ "${APP_LOCK_FILE_LAST_MODIFICATION_DATE}" == `getLastModifyTime "${APP_LOCK_FILE}"` ]
	then
		break
	fi
	log "Waiting for shutdown completion..."
	sleep 1
done