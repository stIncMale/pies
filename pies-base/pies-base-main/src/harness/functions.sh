#!/bin/bash

#This file provides functions that MAY be used by other shell scripts.
#Please note that if execution of a shell script is terminated because of the function provided here,
#then exit status is -1.

#TERM signal is used to designate failure.
trap "exit -1" TERM

#Stops execution of a script in case of failure.
exitError() {
	kill -s TERM $$
}

#Outputs error message with "Error: " prefix to the stderr.
#
#${1} - Error message.
echoError() {
	echo "Error: ${1}" 1>&2
}

#Outputs error message to the stderr.
#Also specifies name of the function that calls echoFuncError() function.
#
#${1} - Error message.
echoFuncError() {
	echo "Error in function ${FUNCNAME[1]}: ${1}" 1>&2
}

#Outputs the provided string by using echo command.
#This function only do output if there is a VERBOSE variable that is set to "true".
#Possible values of VERBOSE:
#	"true"
#	"false"
#
#${1} - String for output.
log() {
	if [ -z "${VERBOSE}" ]
	then
		echoFuncError "VERBOSE variable is not set"
		exitError
	fi
	if [ "${VERBOSE}" == "true" ]
	then
		echo "${1}"
	fi
}

#Checks if a string contains the specified substring.
#Outputs "true" if a string contains the specified substring and "false" otherwise.
#
#${1} - A string to test.
#${2} - A substring to test.
contains() {
	local STRING="${1}"
	local SUBSTRING="${2}"
	case "${STRING}" in
		*"${SUBSTRING}"* ) echo "true";;
		* ) echo "false";;
	esac
}

#Outputs the type of operating system.
#Possible output values:
#	"Windows-Cygwin"
#	"UN*X"
getOperatingSystemType() {
	UNAME=`uname -s`
	RESULT=""
	if [ `contains "${UNAME}" "CYGWIN"` == "true" ]
	then
		RESULT="Windows-Cygwin"
	else
		RESULT="UN*X"
	fi
	echo "${RESULT}"
}

#Outputs character that separates elements in a list of file paths.
#Result is based on the value of OPERATING_SYSTEM_TYPE variable.
#Possible values of OPERATING_SYSTEM_TYPE:
#	"Windows-Cygwin"
#	"UN*X"
getFilePathSeparator() {
	if [ -z "${OPERATING_SYSTEM_TYPE}" ]
	then
		echoFuncError "OPERATING_SYSTEM_TYPE variable is not set"
		exitError
	fi
	local RESULT=""
	if [ "${OPERATING_SYSTEM_TYPE}" == "Windows-Cygwin" ]
	then
		RESULT=";"
	elif [ "${OPERATING_SYSTEM_TYPE}" == "UN*X" ]
	then
		RESULT=":"
	fi
	echo "${RESULT}"
}

#Creates a file and all necessary directories if file not exists.
#
#${1} - Name of the file to create.
createFile() {
	if ! [ -e "${1}" ]
	then
		local DIRECTORY=`dirname "${1}"`
		mkdir -p "${DIRECTORY}"
		#create a file with the name "${1}"
		> "${1}"
	fi
}

#Renames the specified file by adding current date and time.
#For example file ./someFile will be renamed to ./someFile__Y-m-d_H-M-S_z,
#where Y, m, ... represent current date and time.
#
#${1} - Name of the file to rename.
backupFile() {
	local RESULT=""
	if [ -e "${1}" ]
	then
		RESULT="${1}__`date +%Y-%m-%d_%H-%M-%S_%z`"
		mv "${1}" "${RESULT}"
	fi
	echo "${RESULT}"
}

#Transforms list of paths that MAY be in the Windows-Cygwin format to Windows format.
#This method uses FILE_PATH_SEPARATOR variable.
#
#${1} - List of paths that are separated by character specified in the FILE_PATH_SEPARATOR variable.
cygwinToWindowsPath() {
	if [ -z "${FILE_PATH_SEPARATOR}" ]
	then
		echoFuncError "FILE_PATH_SEPARATOR variable is not set"
		exitError
	fi
	local readonly CYGWIN_PREFIX="cygdrive"
	ORIGINAL_IFS="${IFS}"
	IFS="${FILE_PATH_SEPARATOR}"
	read -a PATHS <<< "${1}"
	IFS="${ORIGINAL_IFS}"
	for PATH_WINDOWS_CYGWIN in "${PATHS[@]}"
	do
		#The following operations are performed here by using sed:
		#	remove CYGWIN_PREFIX from the path; by some reason this also removes all backslashes "\"
		#	append ":" to Windows volume label
		#	replace each slash "/" with backslash "\"
		local PATH_WINDOWS=`echo "${PATH_WINDOWS_CYGWIN}" | \
			sed -e "s/^\/${CYGWIN_PREFIX}\/\(.*\)$/\1/" \
			-e "s/^\([^\/]*\)\(\/.*\)$/\1:\2/" \
			-e "s/\//\\\\\/g"`
		if [ -z "${RESULT}" ]
		then
			RESULT="${PATH_WINDOWS}"
		else
			RESULT="${RESULT}${FILE_PATH_SEPARATOR}${PATH_WINDOWS}"
		fi
	done
	echo "${RESULT}"
}

#Replaces all spaces " " with escaped spaces "\ ".
#
#${1} - String where to escape spaces.
escapeSpacesInPath() {
	echo `echo "${1}" | sed -e "s/[[:space:]]/\\\\\ /g"`
}

#Outputs Epoch time the file was last modified.
#
#${1} - Name of the file.
getLastModifyTime() {
	echo `stat -f "%m" "${1}"`
}