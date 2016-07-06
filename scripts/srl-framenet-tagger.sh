set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib
VERSION=`python $DIR/pom_version.py`

#pass naf file as input stream and catch the naf  output stream
# for example> "cat example-naf.xml | srl-framenet.sh > naf.srl-fn.xml"

java -Xmx1812m -cp "$LIB/ontotagger-$VERSION-jar-with-dependencies.jar" eu.kyotoproject.main.SrlFrameNetTagger --frame-ns "fn:" --role-ns "fn-role:;pb-role:;fn-pb-role:;eso-role:" --ili-ns "mcr:ili" --sense-conf 0.05 --frame-conf 30 
