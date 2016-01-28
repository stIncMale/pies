#!/bin/bash

#Set environment
readonly MANUAL_TEST_LOCATION="/Users/male/Documents/programming/projects/pies/misc/manual test"
readonly PIES_INITIATOR_APP_HOME_DIR_NAME="pies-app-initiator"

less "${MANUAL_TEST_LOCATION}/${PIES_INITIATOR_APP_HOME_DIR_NAME}/log/application.log"
