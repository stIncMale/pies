#!/bin/bash
#This script performs search for compiled Java classes.
#TODO check why it doesn't work

#Outputs how to use the script.
printHelp() {
	echo "The script performs search for compiled Java classes."
	echo "Parameters:"
	echo "	lookFor"
	echo "		Name of Java class to search for."
	echo "		This parameter is case insensitive."
	echo "		One MAY specify class name without package name, with package name,"
	echo "		or with ending of package name."
	echo "		Alphanumerics, \"_\", \"$\" MAY be used as part of package name or a class name."
	echo "		\".\" or \"/\" MAY be used as part of a package name."
	echo "		One SHOULD specify value of this parameter within single quotes '...'."
	echo "	searchRoot"
	echo "		Search will be performed in this directory and all subdirectories."
	echo "		If value of this parameter is not specified then 'thisScriptLocation/../' is used."
	echo "Examples:"
	echo "	./findClass.sh 'MyClass' - class name"
	echo "	./findClass.sh 'some/package/MyClass' - search for package name and class name"
	echo "	./findClass.sh 'ckage.MyClass' - search for part of package name (ending) and class name"
	echo "	./findClass.sh 'some.package.MyClass\$MyNestedClass' - search for nested class"
	echo "	./findClass.sh 'some/package/MyClass' /my/projects - search for class in '/my/projects' directory"
	echo "	./my/scripts/findClass.sh 'package.MyClass' . - search for class in current directory"
	echo "Notes:"
	echo "	Execution of this script isn't fully pipelined,"
	echo "	for example at first it collects a list of all archive files under 'searchRoot' directory"
	echo "	and then performs search in each file from the list."
	echo "	Because of this drawback it MAY take quite a time before you'll start to see results"
	echo "	if there are many files under the 'searchRoot' directory."
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

#Suppress errors output, e.g. "access denied" messages.
#exec 2>/dev/null

#Print help if no arguments are specified
if [ -z "${1}" ] || [ ${1} == "-h" ] || [ ${1} == "-help" ] || [ ${1} == "--help" ]
then
	printHelp
	exit 0
fi

#Directory that contains this script.
readonly THIS_SCRIPT_DIRNAME=`cd "$(dirname "${0}")"; pwd`

readonly INPUT_LOOK_FOR="${1}"
readonly INPUT_SEARCH_ROOT="${2}"

#This action will help us if only class name (without a package) is specified
if [ `contains "${INPUT_LOOK_FOR}" "."` == "true" ] || [ `contains "${INPUT_LOOK_FOR}" "/"` == "true" ]
then
	readonly LOOK_FOR="${INPUT_LOOK_FOR}"
else
	readonly LOOK_FOR=".${INPUT_LOOK_FOR}"
fi

#Name of the class without package
readonly SHORT_CLASS_NAME=`echo "${LOOK_FOR}" | sed -e "s/^.*\(\.\|\/\)\([^\.^\/\]*\)$/\2/"`
#Name of the class package. "/" is used for separation of package name elements, "$" are escaped.
readonly PACKAGE_NAME_ESCAPED_FOR_GREP=`echo "${LOOK_FOR}" | sed -e "s/^\(.*\(\.\|\/\)\)[^\.^\/\]*$/\1/" \
		-e "s/\./\//g" -e "s/\\\\$/\\\\\\\\$/g"`
readonly CLASS_FILE_ENDING_FOR_GREP="\.class$"
readonly JAR_FILE_ENDING_FOR_GREP=".*\.jar$"
readonly ZIP_FILE_ENDING_FOR_GREP=".*\.zip$"

#Search will be performed in SEARCH_ROOT directory and all subdirectories.
if [ -z "${INPUT_SEARCH_ROOT}" ]
then
	readonly SEARCH_ROOT=`dirname "${THIS_SCRIPT_DIRNAME}"`
else
	readonly SEARCH_ROOT="${INPUT_SEARCH_ROOT}"
fi

#TODO use `readlink --canonicalize \"${SEARCH_ROOT}\"` instead of \"${SEARCH_ROOT}\" when it will be fixed in Mac OS X
echo "Search for ${INPUT_LOOK_FOR} in \"${SEARCH_ROOT}\""

#Looking for LOOK_FOR_IN_ARCHIVE pattern in archive file contents by using grep
#Replace all periods "." with slashes "/", escape dollars "$" and add CLASS_FILE_ENDING_FOR_GREP
readonly LOOK_FOR_IN_ARCHIVE=`echo "${LOOK_FOR}" | \
		sed -e "s/\./\//g" -e "s/\\\\$/\\\\\\\\$/g"`"${CLASS_FILE_ENDING_FOR_GREP}"
echo "	Search inside archive files (JAR and ZIP)"
echo "	Results:"
readonly ARCHIVE_FILES=`find "${SEARCH_ROOT}" -type f | \
		grep -i "${JAR_FILE_ENDING_FOR_GREP}\|${ZIP_FILE_ENDING_FOR_GREP}"`
FOUND="false"
#Read ARCHIVE_FILES line by line in ARCHIVE_FILE
while read ARCHIVE_FILE
do
	if ! [ -z "${ARCHIVE_FILE}" ]
	then
		unzip -v "${ARCHIVE_FILE}" | grep -i "${LOOK_FOR_IN_ARCHIVE}" > /dev/null
		#${?} is the return status of the last command; grep in our case
		if [ "${?}" == 0 ]
		then
			FOUND="true"
			echo "		${ARCHIVE_FILE}"
		fi
	fi
done <<<"${ARCHIVE_FILES}"
if [ "${FOUND}" == "false" ]
then
	echo "		no results"
fi
echo ""

#Looking for LOOK_FOR_CLASS_FILE pattern in class file names by using grep (search of files that aren't packed)
readonly SHORT_CLASS_NAME_ESCAPED_FOR_GREP=`echo "${SHORT_CLASS_NAME}" | sed -e "s/\\\\$/\\\\\\\\$/g"`
readonly LOOK_FOR_CLASS_FILE="/${SHORT_CLASS_NAME_ESCAPED_FOR_GREP}${CLASS_FILE_ENDING_FOR_GREP}"
echo "	Search outside archive files"
echo "	Results:"
readonly CLASS_FILES=`find "${SEARCH_ROOT}" -type f | grep -i "${LOOK_FOR_CLASS_FILE}"`
FOUND="false"
#Read CLASS_FILES line by line in CLASS_FILE
while read CLASS_FILE
do
	if ! [ -z "${CLASS_FILE}" ]
	then
		less -f "${CLASS_FILE}" | grep -i "${PACKAGE_NAME_ESCAPED_FOR_GREP}${SHORT_CLASS_NAME_ESCAPED_FOR_GREP}" \
				> /dev/null
		#${?} is the return status of the last command; grep in our case
		if [ "${?}" == 0 ]
		then
			FOUND="true"
			echo "		${CLASS_FILE}"
		fi
	fi
done <<<"${CLASS_FILES}"
if [ "${FOUND}" == "false" ]
then
	echo "		no results"
fi