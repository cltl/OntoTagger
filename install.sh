#Requires an installation of maven 2.x and Java 1.6 or higher

# define the location of the install scipt
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources

echo "#1. compiling the library from source code and dependencies"
mvn install
echo "#2. moving binary to lib folder"
mv "$DIR/target/ontotagger-v3.0-jar-with-dependencies.jar" "$DIR/lib"
echo "#3. installing the vua-resources"
cd "$RESOURCES"
git clone https://github.com/cltl/vua-resources.git
echo "#4. cleaning up"
cd "$DIR"
rm -R target
rm -R src


jar from target to lib
remove installation sources
checkout the resources

git clone https://github.com/cltl/vua-resources.git

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

