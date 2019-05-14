#!/bin/sh

cd ../
sbt fullOptJS
mkdir -p snapshot/js/gen
cp -rf site/css       snapshot/
cp -rf site/fonts     snapshot/
cp -f  site/favicon.* snapshot/
cp -rf site/js/static snapshot/js/
cp localJS/target/scala-2.12/local_js-opt.js* snapshot/js/gen/

echo "Done. Open 'index.html' to try the current version."