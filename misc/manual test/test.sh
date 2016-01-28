#!/bin/bash

log() {
	echo ""
	echo "Test: ${1} ------------------------------------------------------------------------"
	echo ""
}

#Execution levels
readonly EXEC_LEVEL_BUILD="0"
readonly EXEC_LEVEL_REPLACE="1"
readonly EXEC_LEVEL_START="2"
readonly EXEC_LEVEL_SHUTDOWN="3"

#Set environment
readonly MANUAL_TEST_LOCATION="/Users/male/Documents/programming/projects/pies/misc/manual test"
readonly PROJECT_LOCATION="/Users/male/Documents/programming/projects/pies"
readonly PROJECT_POM="${PROJECT_LOCATION}/pom.xml"
readonly ASSEMBLY_LOCATION="${PROJECT_LOCATION}/target"
readonly ASSEMBLY_NAME="pies-0.0.0-SNAPSHOT-pies.assembly.zip"
readonly PIES_ECHO_APP_HOME_DIR_NAME="pies-app-echo"
readonly PIES_INITIATOR_APP_HOME_DIR_NAME="pies-app-initiator"
readonly PIES_PROXY_APP_HOME_DIR_NAME="pies-app-proxy"

#Process command-line arguments
EXEC_LEVEL="${EXEC_LEVEL_BUILD}"
if [ "${1}" = "build" ]
then
	EXEC_LEVEL="${EXEC_LEVEL_BUILD}"
fi
if [ "${1}" = "replace" ]
then
	EXEC_LEVEL="${EXEC_LEVEL_REPLACE}"
fi
if [ "${1}" = "start" ]
then
	EXEC_LEVEL="${EXEC_LEVEL_START}"
fi
if [ "${1}" = "stop" ]
then
	EXEC_LEVEL="${EXEC_LEVEL_SHUTDOWN}"
fi
MVN_CLEAN="${2}"

#Build project
if [ "${EXEC_LEVEL_BUILD}" -ge "${EXEC_LEVEL}" ]
then
	log "Build project"
	mvn -f ${PROJECT_POM} ${MVN_CLEAN} package -P assembly
	echo ""
fi

#Replace assemblies
if [ "${EXEC_LEVEL_REPLACE}" -ge "${EXEC_LEVEL}" ]
then
	log "Replace assemblies"
	rm -R "${MANUAL_TEST_LOCATION}/${PIES_ECHO_APP_HOME_DIR_NAME}"
	rm -R "${MANUAL_TEST_LOCATION}/${PIES_INITIATOR_APP_HOME_DIR_NAME}"
	rm -R "${MANUAL_TEST_LOCATION}/${PIES_PROXY_APP_HOME_DIR_NAME}"
	cp  "${ASSEMBLY_LOCATION}/${ASSEMBLY_NAME}" "${MANUAL_TEST_LOCATION}/${ASSEMBLY_NAME}"
	unzip "${MANUAL_TEST_LOCATION}/${ASSEMBLY_NAME}" -d "${MANUAL_TEST_LOCATION}"
	rm  "${MANUAL_TEST_LOCATION}/${ASSEMBLY_NAME}"
	chmod u+x "${MANUAL_TEST_LOCATION}/${PIES_ECHO_APP_HOME_DIR_NAME}/harness/"*.sh
	chmod u+x "${MANUAL_TEST_LOCATION}/${PIES_INITIATOR_APP_HOME_DIR_NAME}/harness/"*.sh
	chmod u+x "${MANUAL_TEST_LOCATION}/${PIES_PROXY_APP_HOME_DIR_NAME}/harness/"*.sh
	echo ""
fi

#Start Applications
if [ "${EXEC_LEVEL_START}" -ge "${EXEC_LEVEL}" ]
then
	log "Start Applications"
	"${MANUAL_TEST_LOCATION}/${PIES_ECHO_APP_HOME_DIR_NAME}/harness/start.sh"
	log ""
	"${MANUAL_TEST_LOCATION}/${PIES_INITIATOR_APP_HOME_DIR_NAME}/harness/start.sh"
	log ""
	"${MANUAL_TEST_LOCATION}/${PIES_PROXY_APP_HOME_DIR_NAME}/harness/start.sh"
	exit
fi

#Shutdown Applications
if [ "${EXEC_LEVEL_SHUTDOWN}" -ge "${EXEC_LEVEL}" ]
then
	log "Shutdown Applications"
	"${MANUAL_TEST_LOCATION}/${PIES_ECHO_APP_HOME_DIR_NAME}/harness/shutdown.sh"
	log ""
	"${MANUAL_TEST_LOCATION}/${PIES_INITIATOR_APP_HOME_DIR_NAME}/harness/shutdown.sh"
	log ""
	"${MANUAL_TEST_LOCATION}/${PIES_PROXY_APP_HOME_DIR_NAME}/harness/shutdown.sh"
fi
