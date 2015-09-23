#!/bin/bash

export SAIFE_HOME="."
#g++ -std=c++11 -I$SAIFE_HOME/include -L$SAIFE_HOME/lib/SAIFE -o SAIFEDAR src/SAIFEDar.cpp -l saife -l CecCryptoEngine -l roxml
g++ -std=c++0x -I$SAIFE_HOME/include -L$SAIFE_HOME/lib/SAIFE -o bin/SAIFEDAR src/SAIFEDar.cpp -l saife -l CecCryptoEngine -l roxml

