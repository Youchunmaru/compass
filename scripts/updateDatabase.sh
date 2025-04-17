#!/usr/bin/env bash


source config.properties

folders=$(ls ./db/"$version")

for folder in $folders ; do
  scripts=$(ls ./db/"$version"/"$folder")
  for script in $scripts ; do
    echo "$folder" "$script"
    mysql -u"$db_user" -p"$db_pass" -P"$db_port" -h"$db_host" < ./db/"$version"/"$folder"/"$script"
  done
done