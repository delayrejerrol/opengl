############################################################
#                                                          #             
#              A build script for Windows                  #
#                                                          #
############################################################

# Run this build script on the project root directory.

import subprocess
import shutil
import os
import sys

# Call this function to build the apk via gradle.
def build_cmd():

    # This will build your debug apk.
    buildProject = "gradlew.bat clean --profile --recompile-scripts --offline --rerun-tasks assembleDebug";

    # If you want to build a release apk use this instead.
    # buildProject = "gradlew.bat clean --profile --recompile-scripts --offline --rerun-tasks assembleRelease -Pandroid.injected.signing.store.file=" + os.path.abspath("store_key.jks") + " -Pandroid.injected.signing.store.password=pass123 -Pandroid.injected.signing.key.alias=key_alias -Pandroid.injected.signing.key.password=pass123"
    
    # As of Python 3 use run() instead of call() function
    # https://docs.python.org/3/library/subprocess.html#older-high-level-api
    subprocess.run(buildProject, shell=True)

# Call this function to move the builded apk to your desire directory directory.
def moveDir():
    # Get the generated apk
    # New directory of release apk
    apk_path = "./app/build/outputs/apk/debug/app-debug.apk"

    # apk will rename and move to asset directory
    shutil.move(apk_path, "./app/src/main/assets/app-debug.apk")


if __name__ == '__main__':

    build_cmd()
    moveDir()
        

