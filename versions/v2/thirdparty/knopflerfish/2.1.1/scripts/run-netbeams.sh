#!/bin/bash
#####################################################################
# Before executing script, please make sure the following is done:
# 
# 1. Check to make your environment variables:
#
#       DSP_RUNTIME
# 	    KNOPFLERFISH_HOME
#
#    are set.
#
# 2. Create a netbeams subdirectory in ${KNOPFLERFISH_HOME}/jars
#
# 3. Download netbeams.xargs from the repository and place it in 
#	 ${KNOPFLERFISH_HOME}.
#
# 4. Make sure a general build of the sources has been completed.
#####################################################################
#
# To run script:
#
#	$ bash run-netbeams.sh
#
#####################################################################

cd ${KNOPFLERFISH_HOME}
cp -r ${DSP_RUNTIME} ${KNOPFLERFISH_HOME}/jars/netbeams

java -Dlog4j.configuration="file:${KNOPFLERFISH_HOME}/jars/netbeams/runtime/log4j.xml" -jar framework.jar -xargs netbeams.xargs

