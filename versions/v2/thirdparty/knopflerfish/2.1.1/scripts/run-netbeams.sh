#!/usr/local/bin/bash
#####################################################################
# To use script, please make sure the following is done:
# 
# 1. Modify the 1st line of this script, if Bash is located somewhere
#    else on your filesytem.
#
# 2. Check to make your environment variables:
#
#		DSP_RUNTIME
# 	    KNOPFLERFISH_HOME
#
#    are set.
#
# 2. Create a netbeams subdirectory in ${KNOPFLERFISH_HOME}/jars
#
# 3. Download netbeams.xargs from the repository and place it in 
#	 ${KNOPFLERFISH_HOME}.
#
# 4. This script can be executed from within your HOME directory.
#
# 5. Make sure a general build of the sources has been completed.
#
#####################################################################

cd ${KNOPFLERFISH_HOME}
cp -r ${DSP_RUNTIME} ${KNOPFLERFISH_HOME}/jars/netbeams

java -Dlog4j.configuration="file:${KNOPFLERFISH_HOME}/jars/netbeams/runtime/log4j.xml" -jar framework.jar -xargs netbeams.xargs

