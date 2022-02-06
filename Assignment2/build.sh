#!/bin/bash

mkdir build
# javac -d build src/com/lib/interfaces/RMIServerInterface.java src/com/lib/index_server/RegisteredPeerInfo.java src/com/lib/index_server/IndexServer.java src/com/test/IndexTest.java
# javac -d build src/com/lib/interfaces/RMIClientInterface.java src/com/lib/peer_client/PeerClient.java src/com/test/PeerTest.java

# Building Server
# javac -d build src/com/lib/peer_client/FileInfo.java src/com/lib/interfaces/RMIClientInterface.java src/com/lib/interfaces/RMIServerInterface.java src/com/lib/index_server/RegisteredPeerInfo.java src/com/lib/index_server/IndexServer.java src/com/lib/peer_client/PeerClient.java src/com/app/CentralServer.java

# # Building Client
# javac -d build src/com/lib/watch_dir/WatchDir.java src/com/lib/peer_client/FileInfo.java src/com/lib/interfaces/RMIClientInterface.java src/com/lib/interfaces/RMIServerInterface.java src/com/lib/peer_client/PeerClient.java src/com/app/Client.java

# # Building test case
# javac -d build src/com/lib/watch_dir/WatchDir.java src/com/lib/peer_client/FileInfo.java src/com/lib/interfaces/RMIClientInterface.java src/com/lib/interfaces/RMIServerInterface.java src/com/lib/peer_client/PeerClient.java  src/com/test/test_cases/PeerTest1.java

# javac -d build src/com/test/WatchTest.java src/com/lib/watch_dir/WatchDir.java

javac -d build -cp src/com/lib/thirdparty/json-20210307.jar: src/com/lib/index_server/MessageID.java src/com/lib/index_server/Query.java src/com/lib/index_server/QueryHit.java src/com/lib/peer_client/FileInfo.java src/com/lib/watch_dir/WatchDir.java src/com/lib/interfaces/RMIClientInterface.java src/com/lib/interfaces/RMIServerInterface.java src/com/lib/index_server/RegisteredPeerInfo.java src/com/lib/index_server/IndexServer.java src/com/lib/peer_client/PeerClient.java src/com/app/CentralServer.java src/com/app/Client.java src/com/test/test_cases/PeerTest1.java
