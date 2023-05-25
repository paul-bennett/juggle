# Sets up an alias 'juggle' for the juggle command
#
# Run this by:
#   bash$ . alias.sh
#   zsh$ source alias.sh

juggle_dir=$(cd "$(dirname $0)" || exit; pwd)

juggle_jar=$(ls "${juggle_dir}"/build/libs/juggle-*.jar 2>/dev/null) 2>/dev/null

if [ -z "$juggle_jar" ]
then
  echo "Couldn't find JAR in build/libs. Try running \"gradle jar\" first."
else
  alias juggle=java\ -jar\ '$juggle_jar'
fi
