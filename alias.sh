# Sets up an alias 'juggle' for the juggle command
#
# Run this by:
#   bash$ . alias.sh
#   zsh$ source alias.sh

juggle_dir=$(cd $(dirname $0); pwd)

juggle_jar=$(ls "${juggle_dir}"/build/libs/juggle-*.jar)

if [ $? != 0 ]
then
  echo "Couldn't find JAR in build/libs. Try running \"gradle jar\" first."
else
  alias juggle=java\ -jar\ $juggle_jar\ --format=colour
fi
