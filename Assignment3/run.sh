#!/bin/bash

OPTION=$1;
DIR=$(pwd);

function exit_err {
  exit 1;
}

T="    ";
VERTICAL_MARGIN="------------------------------------------------------------------";
function usage {
  echo "$VERTICAL_MARGIN";
  echo "Usage:";
  echo "$VERTICAL_MARGIN";
  echo "$T#$0 [OPTION s|c] [CONFIG_FILE] [CONFIG_ID] [SHARED_DIRECTORY]";
  echo "$T    s - run server.";
  echo "$T    c - run client.";
  echo "$T";
  echo "$T#Example:";
  echo "$T    $0 s [CONFIG_FILE] [CONFIG_ID]";
  echo "$T    $0 c [CONFIG_FILE] [CONFIG_ID] [SHARED_DIRECTORY]";
  echo "$T    CONFIG_FILE must be in the same directory as $0";

  echo "$T    ----------------------------------------------------------";
  echo "$T#Test runs:";
  echo "$T    simple_test_servers_start  - run servers for simple test.";
  echo "$T    linear_test_servers_start  - run 8 servers for linear topology test.";
  echo "$T    all2all_test_servers_start - run 8 servers for all 2 all topology test.";
  echo "$T    test_servers_stop          - kill all servers started using test options.";
  echo "$VERTICAL_MARGIN";
}

if [ -z "$OPTION" ]; then
  echo "Invalid: Parameters missing.";
  usage;
  exit_err;
fi

SIMPLE_TEST_PID_FILENAME="test_servers.pid";
SERVER_OUTPUT="servers.output";

cd ./build

CONFIG_FILE=$2
CONFIG_ID=$3
SHARED_DIRECTORY=$4

TEST_CONFIG_FILE="";
NUM_SERVERS="";
if [ "$OPTION" = "simple_test_servers_start" ]; then
  TEST_CONFIG_FILE="simple.config.json";
  NUM_SERVERS=2;
fi

if [ "$OPTION" = "linear_test_servers_start" ]; then
  TEST_CONFIG_FILE="linear.config.json";
  NUM_SERVERS=8;
fi

if [ "$OPTION" = "all2all_test_servers_start" ]; then
  TEST_CONFIG_FILE="all2all.config.json";
  NUM_SERVERS=8;
fi

if [ ! -z "$TEST_CONFIG_FILE" ]; then
  if [ -s $DIR/$SIMPLE_TEST_PID_FILENAME ]; then
    # The file is not-empty.
    echo "Stop running servers first"
    cat $DIR/$SIMPLE_TEST_PID_FILENAME
  else
    for (( s = 0; s < $NUM_SERVERS; s++ ))
    do
      nohup java -cp $DIR/src/com/lib/thirdparty/json-20210307.jar: com.app.server.CentralServer $DIR/$TEST_CONFIG_FILE $s > $DIR/$SERVER_OUTPUT$s 2>&1 &
      echo $! >> $DIR/$SIMPLE_TEST_PID_FILENAME
    done

    echo "All Super Peers have started SUCCESSFULLY.";
    echo "1. Check $DIR/$SERVER_OUTPUT file for server logs."
    echo "$T Example: "
    echo "$T # tail -f $DIR/$SERVER_OUTPUT"
    echo "2. Check $DIR/$SIMPLE_TEST_PID_FILENAME for Process IDs."
    echo ""
    echo "Successfull."
  fi

  exit 0;
fi

if [ "$OPTION" = "test_servers_stop" ]; then
  if [ -f $DIR/$SIMPLE_TEST_PID_FILENAME ]; then
    while IFS= read -r pid
    do
      echo "Stopping Super Peer $pid"
      kill -9 "$pid"
    done < "$DIR/$SIMPLE_TEST_PID_FILENAME"

    rm -rf $DIR/$SIMPLE_TEST_PID_FILENAME
  fi

  echo "Done."
  exit 0;
fi

if [ -z "$CONFIG_FILE" ] || [ -z "$CONFIG_ID" ]; then
  echo "Invalid: Parameters missing.";
  usage;
  exit_err;
fi

if [ "$OPTION" = "s" ]; then
  java -cp $DIR/src/com/lib/thirdparty/json-20210307.jar: com.app.server.CentralServer $DIR/$CONFIG_FILE $CONFIG_ID
fi

if [ "$OPTION" = "c" ]; then
  if [ -z "$SHARED_DIRECTORY" ]; then
    echo "Invalid: Parameters missing.";
    usage;
    exit_err;
  fi

  java -cp $DIR/src/com/lib/thirdparty/json-20210307.jar: com.app.client.Client $DIR/$CONFIG_FILE $CONFIG_ID $DIR/$SHARED_DIRECTORY
fi
