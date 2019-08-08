#!/bin/sh -e

# trigger release script on travis-ci.org

echo "This will trigger a release job on Travis, building the SNAPSHOT version of master as a release and incrementing the new SNAPSHOT version by 0.0.1. Are you sure you want to continue? (Y/n)"
read CONTINUE_RELEASE

read -p "Please enter a valid travis token:" TRAVIS_TOKEN

if [ "$CONTINUE_RELEASE" = "Y" ]; then
  TRAVIS_REQUEST='{
   "request": {
   "message": "Perform Maven Release",
   "branch":"master",
   "config": {
     "script": "./.travis/travis-release.sh"
    }
  }}'

  curl -v -s -X POST \
   -H "Content-Type: application/json" \
   -H "Accept: application/json" \
   -H "Travis-API-Version: 3" \
   -H "Authorization: token $TRAVIS_TOKEN" \
   -d "$TRAVIS_REQUEST" \
   https://api.travis-ci.com/repo/AODocs%2Fendpoints-authenticators/requests
else
  echo "Aborted."
fi

