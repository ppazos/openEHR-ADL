# openEHR-ADL

Project that centralizes all the ADL (openEHR Archetype Definition Language) processing for CaboLabs projects.

## Prerequisites

Java and Groovy installed

Recommended to install via SDKMAN! https://sdkman.io/

## Generate ADL 2 CSV

java -cp "openEHR_ADL.jar:./lib/*:$GROOVY_HOME/lib/*" com.cabolabs.openehr.Main csv ~/Desktop/HiGHmed\ Lab\ Report/archetypes/entry/evaluation
