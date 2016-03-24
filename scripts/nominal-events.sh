DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib

RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources
# assumes vua-resources is installed next to this installation
# git clone https://github.com/cltl/vua-resources.git

#pass naf file as input stream and catch the naf  output stream
# for example> "cat example-naf.xml | predicate-matrix.sh > naf.nm-srl.xml"

java -Xmx812m -cp "$LIB/ontotagger-v3.0-jar-with-dependencies.jar" eu.kyotoproject.main.NominalEventCoreference --framenet-lu "$RESOURCES/nl-luIndex.xml"
