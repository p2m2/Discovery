#!/bin/bash

sbt discoveryJS/fullOptJS
cp js/target/scala-2.13/discovery-opt.js dist/discovery.js

sed -i "s#$(pwd)#com/github/p2m2#g" dist/discovery.js

./node_modules/.bin/browserify -r ./dist/discovery.js -s discovery > dist/discovery-web.js

if [ -z "$CI" ];then
  echo " -- commit -- "
  git commit dist/discovery.js dist/discovery-web.js -m"update cdn js lib." &>/dev/null
fi
