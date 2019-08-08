#!/bin/sh -e

echo "Checkout master branch.."
git checkout -qf master;

echo "Starting Maven release"
mvn --settings ./.travis/settings.xml release:prepare release:perform
