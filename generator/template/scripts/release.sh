__STONECRAFT_#AUTOMATED_RELEASES__
#!/bin/sh
set -eu

VERSION=${1:?A release version is required}

echo "Replacing version with ${VERSION}"
sed -e "s/0.0-SNAPSHOT/${VERSION}/" -i gradle.properties

./gradlew __STONECRAFT_#DATAGEN__runDatagen __STONECRAFT_/DATAGEN____STONECRAFT_#GAMETESTS__runGameTestServer __STONECRAFT_/GAMETESTS__buildAndCollect__STONECRAFT_#PUBLISHING__ publishMods__STONECRAFT_/PUBLISHING__ --stacktrace
__STONECRAFT_/AUTOMATED_RELEASES__
