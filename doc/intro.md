# Introduction to Euronext/CME ETL software
 The goal of the software is to decode historical exchange data for two particular exchanges, data that is delivered by Pico.
 
  The general transformation process is described in the [Abstract ETL view](ETL.md)
 
 The deliverable is a Java CLI application; since the data sources are the same, the underlying exchange formats use the same tools, the project is split into 3 repositories:
 
 * common/ - contains the code representing the transformation process
 * md-data-reader-euronext/ - contains the specific adapters & main function for Euronext, as well as integration tests
 * md-data-reader-cme/ - contains the specific adapters & main function for CME, as well as integration tests
 
 All three are Clojure projects. The main motivation for using Clojure is two-fold: 
 1. The need to agressively inline code for the various adapters.
 2. The reality that, at least for CME, a lot of the adaptation code is similar.
 
 This repo concerns itself with the following topics:
 * the master transformation driver
 * the main PCAP parser
 * the supporting Java objects
 * the main Clojure utilities
 
