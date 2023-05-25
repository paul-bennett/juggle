#   Juggle -- an API search tool for Java
#
#   Copyright 2020,2023 Paul Bennett
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

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
