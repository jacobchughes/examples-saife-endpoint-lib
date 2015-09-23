#!/bin/bash

export SAIFE_HOME="."
g++ -std=c++0x -I$SAIFE_HOME/include -L$SAIFE_HOME/lib/saife -o bin/SAIFENS src/SAIFENS.cpp -l saife -l CecCryptoEngine -l roxml


