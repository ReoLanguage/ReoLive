Analysis of (Featured Extended) Team Automata - FETA
========================

This project uses ScalaJS to generate JavaScript that runs in a browser, and a JVM compiler to generate a server. This is a branch of ReoLive that includes mainly the analysis of Team Automata and its dependencies.

A snapshot of these analysis from the main branch can be found in http://arcatools.org/feta. 

For more information check the main repository with the analysis of Team Automata in https://github.com/arcalab/team-a.


How to compile both the javascript (client) and the JVM (server)
==============

* Pull the git submodules:
> git submodule update --init
* Run the compilation script:
> ./compile.sh


How to run the framework
=====

* Start the server using sbt
> sbt server/run
* Open localhost:9000 in a browser, e.g. in Chrome
> open http://localhost:9000


