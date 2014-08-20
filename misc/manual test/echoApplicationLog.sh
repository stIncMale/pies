#!/bin/bash

#Set environment
readonly MANUAL_TEST_LOCATION="/Users/male/Documents/programming/projects/pies/default/misc/manual test"
readonly PIES_ECHO_APP_HOME_DIR_NAME="pies-app-echo"

less "${MANUAL_TEST_LOCATION}/${PIES_ECHO_APP_HOME_DIR_NAME}/log/application.log"
