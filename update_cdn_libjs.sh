#!/bin/bash

rm dist/discovery*.js

sbt discoveryJS/fullOptJS

newjs=`ls -lat $(find . -name disc*.js) | head -n1 | awk '{print $NF}'`
echo "Discovery to browserify JS:$newjs"

cp $newjs dist/discovery.js

sed -i "s#$(pwd)#com/github/p2m2#g" dist/discovery.js

if [ ! -f ./node_modules/.bin/browserify ] ;then
  npm install   browserify
fi

./node_modules/.bin/browserify -r ./dist/discovery.js -s discovery > dist/discovery-web.js

# generate md5sum to check js libraries

cat $(find . -name *.scala | sort -V | grep -v SWDiscoveryVersionAtBuildTime.scala) | md5sum > dist/checksum

# var CI defined on circleci at true

if [ -z "$CI" ];then
  echo " -- commit -- "
  git commit dist/discovery.js dist/discovery-web.js dist/checksum -m"update cdn js lib." &>/dev/null
fi
