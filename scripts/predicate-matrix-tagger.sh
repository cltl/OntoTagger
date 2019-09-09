DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib

RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources
# assumes vua-resources is installed next to this installation
# git clone https://github.com/cltl/vua-resources.git

#pass naf file as input stream and catch the naf  output stream
# for example> "cat example-naf.xml | predicate-matrix.sh > naf.pm.xml"

java -Xmx1812m -cp "$LIB/ontotagger-v3.1.1-jar-with-dependencies.jar" eu.kyotoproject.main.KafPredicateMatrixTagger --mappings "fn:;ili;eso" --ili --predicate-matrix "$RESOURCES/PredicateMatrix.v1.3.txt.role.odwn.gz" --grammatical-words "$RESOURCES/Grammatical-words.en"
