#!/bin/bash

NUM_CLIENTS=$1
SHARE_DIR_NAME="peer_shared_directories";

function exit_err {
  exit 1;
}

function usage {
  echo "Usage:"
  echo "    $0 [number of clients]"
}

if [ -z "$NUM_CLIENTS" ]; then
  echo "Invalid: Number of clients parameter missing.";
  usage;
  exit_err;
fi

num_regex='^[0-9]+$'
if ! [[ $NUM_CLIENTS =~ $num_regex ]]; then
   echo "Invalid: Number of clients parameter should be a \"number\".";
   usage;
   exit_err;
fi

./createTestEnv.sh $NUM_CLIENTS
java -cp ../build com.test.test_cases.PeerTest1 ./$SHARE_DIR_NAME $NUM_CLIENTS