Snapshot information
================

This folder contains:
 - a copy of the compiled JavaScript files
 - a variation of the static html from the `../site` that uses the optimised JS and does not include server-based functionality.


How to update the snapshot
=====

* Run the `update.sh` script to:
   - Compile the JS files and copy them to the snapshot
   - Copy the CSS files from the `../site`
   - Copy the static JS from the `../site`

* Modify manually any html file that is relevant, possibly based on the html files in `../site`.

* Open the local `index.html` to confirm everything works


Difference between Site and Snapshot
====================================
This folder includes copies of a version of the project, that are periodically updated and included in the Git repository, and include references to only the static JS (which does not rely on the server). 

The Site folder includes html files that are used during development, and uses symbolic links to keep the JS files up-to-date. It is used by the server via symbolic links as well.