echo "Make sure you first modify the 'server/conf/application.conf' file."
echo "Modify the session key 'play.http.secret.key'."
echo ""
# echo "## Removing previous standalone"
# rm -rf server/target/universal/server-1.0*
echo "## Removing previous compilation"
rm -rf server/target
echo "## Building standalone - assuming 'compile.sh' was executed at least once to generate links."
sbt -mem 2048 fullOptJS server/compile dist
echo "## Running the server"
sbt -mem 2048 -J-Xms2048m -J-Xmx2048m -Dlog4j2.formatMsgNoLookups=true server/run

### Problem 
# echo "## Unzipping standalone"
# cd server/target/universal
# yes | unzip server-1.0.zip
# cd ../../../
# echo "## Executing  the server - './server/target/univeral/server-1.0/bin/server"
# ./server/target/universal/server-1.0/bin/server -J-Xms2048m -J-Xmx2048m -Dlog4j2.formatMsgNoLookups=true
