#!/bin/bash

SHARE_DIR_NAME="peer_shared_directories";
NUM_CLIENTS=$1

SMALL_FILES_COUNT=2;
MEDIUM_FILES_COUNT=2;
LARGE_FILES_COUNT=2;

# size in bytes
SMALL_FILES_SIZE=1024;  # will create approx 1K
MEDIUM_FILES_SIZE=5120; # will create approx 5K
LARGE_FILES_SIZE=10240; # will create approx 10K

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

echo "Deleting any existing directories.";
rm -rf $(pwd)/$SHARE_DIR_NAME;

echo "Creating Sharable Directories for $NUM_CLIENTS peers.";
for ((i = 0 ; i < $NUM_CLIENTS ; i++)); do
  PEER_DIR=$(pwd)/$SHARE_DIR_NAME/$i;

  SMALL_FILE_NAME="peer$i""_small_file";
  MEDIUM_FILE_NAME="peer$i""_medium_file";
  LARGE_FILE_NAME="peer$i""_large_file";

  mkdir -p "$PEER_DIR";

  FILE_CONTENT_UNIT_SIZE=21;

  SMALL_LIMIT=$(($SMALL_FILES_SIZE / $FILE_CONTENT_UNIT_SIZE));
  for ((j = 0 ; j < $SMALL_FILES_COUNT ; j++)); do
    FILE_NAME=$SMALL_FILE_NAME$j;
    FILE_PATH=$PEER_DIR/$FILE_NAME;
    for ((k = 0 ; k < $SMALL_LIMIT ; k++)); do
      echo -n "This is $FILE_NAME. " >> $FILE_PATH;
    done
  done

  MEDIUM_LIMIT=$(($MEDIUM_FILES_SIZE / $FILE_CONTENT_UNIT_SIZE));
  for ((j = 0 ; j < $MEDIUM_FILES_COUNT ; j++)); do
    FILE_NAME=$MEDIUM_FILE_NAME$j;
    FILE_PATH=$PEER_DIR/$FILE_NAME;
    for ((k = 0 ; k < $MEDIUM_LIMIT ; k++)); do
      echo -n "This is $FILE_NAME. " >> $FILE_PATH;
    done
  done

  LARGE_LIMIT=$(($LARGE_FILES_SIZE / $FILE_CONTENT_UNIT_SIZE));
  for ((j = 0 ; j < $LARGE_FILES_COUNT ; j++)); do
    FILE_NAME=$LARGE_FILE_NAME$j;
    FILE_PATH=$PEER_DIR/$FILE_NAME;
    for ((k = 0 ; k < $LARGE_LIMIT ; k++)); do
      echo -n "This is $FILE_NAME. " >> $FILE_PATH;
    done
  done

done

echo "done";