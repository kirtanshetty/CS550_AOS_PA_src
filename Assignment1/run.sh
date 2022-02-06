#!/bin/bash

# cd src
# java -cp ./build com.app.server.CentralServer
# # java -cp ./build com.app.client.Client

# java -cp ./build com.test.test_cases.PeerTest1



OPTION=$1
SHARED_DIRECTORY=$2

function exit_err {
  exit 1;
}

function client_usage {
  echo "Usage:"
  echo "    $0 c [SHARED_DIRECTORY_PATH]"
}

function usage {
  echo "Usage:"
  echo "    $0 [OPTION s|c]"
  echo "    s - run server."
  echo "    c - run client."
}

if [ -z "$OPTION" ]; then
  echo "Invalid: Option parameter missing.";
  usage;
  exit_err;
fi

if [ "$OPTION" = "s" ]; then
  java -cp ./build com.app.server.CentralServer
fi

if [ "$OPTION" = "c" ]; then
  if [ -z "$SHARED_DIRECTORY" ]; then
    client_usage;
    exit_err;
  fi

  java -cp ./build com.app.client.Client $SHARED_DIRECTORY
fi