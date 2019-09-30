#!/bin/sh

# stop if something fails
set -e

# reference directory is where the script runs - the root of the project
cd ${BASH_SOURCE%/*}

# clean files that will be overridden by symbolic links
echo "Creating symbolic links to the site content"
mkdir -p site/js/gen
mkdir -p site/content
cd site/
echo "- linking generated JavaScript files"
cd js/gen/
ln -sf ../../../localJS/target/scala-2.12/local_js-fastopt.js     
ln -sf ../../../localJS/target/scala-2.12/local_js-fastopt.js.map 
ln -sf ../../../localJS/target/scala-2.12/local_js-opt.js         
ln -sf ../../../localJS/target/scala-2.12/local_js-opt.js.map     
ln -sf ../../../remoteJS/target/scala-2.12/remote_js-fastopt.js     
ln -sf ../../../remoteJS/target/scala-2.12/remote_js-fastopt.js.map 
ln -sf ../../../remoteJS/target/scala-2.12/remote_js-opt.js         
ln -sf ../../../remoteJS/target/scala-2.12/remote_js-opt.js.map     
cd ../../../
echo "Updating links at the server"
mkdir -p server/public
cd server/public
echo "- removing old static content in the server"
rm -rf *
echo "- linking content from the site, ignoring index.html"
# ln -s ../../snapshot/content # use for optimised JS
ln -sf ../../site/content  # use for fast JS
ln -sf ../../site/css
ln -sf ../../site/favicon.ico
ln -sf ../../site/favicon.svg
ln -sf ../../site/fonts
ln -sf ../../site/js
ln -sf ../../site/index.html
ln -sf ../../site/content/svg
cd ../../


# warning
echo "--------------------------------------------------------------"
echo "- Run snapshot/update.sh if you want to update the JS and CS -"
echo "-   before publishing the snapshot. Not needed by the server.-"
echo "- Compiling everything - the first time may take a while.    -"
echo "--------------------------------------------------------------"

# Compile all JavaScript (JS), both in localJS and in remoteJS
sbt fastOptJS server/compile
# sbt fullOptJS server/compile

echo ""
echo "Compilation done."
echo "- Open 'site/index.html'"
echo "- Run the server using 'sbt server/run' to access the 'online' tabs of the site."