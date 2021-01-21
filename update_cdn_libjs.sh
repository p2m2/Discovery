#!/bin/bash

sbt discoveryJS/fullOptJS
cp js/target/scala-2.13/discovery-opt.js dist/discovery.js
browserify -r ./dist/discovery.js -s discovery > dist/discovery-web.js
git commit dist/discovery.js dist/discovery-web.js -m"update cdn js lib."
