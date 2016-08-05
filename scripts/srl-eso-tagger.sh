#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib
RESOURCES="../vua-resources"
DATA="../data2"

#pass a folder with input NAF file. It adds eso mappings for predicates and writes to the same fie

java -Xmx1812m -cp "$LIB/ontotagger-v3.1.1-jar-with-dependencies.jar" eu.kyotoproject.main.SrlEsoTagger --input "$DATA" --extension ".naf" --events "$RESOURCES/dutch_verbs_eso_types.txt"
