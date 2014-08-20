#!/bin/bash

#Set environment
readonly MANUAL_TEST_LOCATION="/Users/male/Documents/programming/projects/pies/default/misc/manual test"
readonly PIES_PROXY_APP_HOME_DIR_NAME="pies-app-proxy"

less "${MANUAL_TEST_LOCATION}/${PIES_PROXY_APP_HOME_DIR_NAME}/log/application.log"
