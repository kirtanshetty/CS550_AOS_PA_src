# CS550
CS 550 Fall 2021 semester project


To compile the demo. Run the following command.

    ./build.sh

Make sure that you turn on your RMI registry. For most Linux based OS and Mac, following command works.

    rmiregistry &

Run server and client separately. Enter the `src` file and run the following commands.

    ./run.sh s                         # for server
    ./run.sh c [SHARED_DIRECTORY_PATH] # for client

For simplicity, we have pushed a sample shared directory for all 3 peer clients. To start 3 peer clients, run these commands on separate terminals

    ./run.sh c ./peer_shared_directories/0 # for peer client 0
    ./run.sh c ./peer_shared_directories/1 # for peer client 1
    ./run.sh c ./peer_shared_directories/2 # for peer client 2

Each directory is named after the peer number and has files of different sizes as specified in the PA1 document.
In order to generate the default shared directory, run the following script.

    ./createTestEnv.sh 3 # where 3 is the number of peer client directories

# Test cases
To run test cases, enter test directory 

    cd ./test

and run following commands.

    ./test_parallelRetrieve.sh 3 # where 3 is number of parallel peer clients
    
# Sample Outputs

    $ ./run.sh s
    Binding Server RMI Interface to //localhost/central_server
    Register called: lookupString = //localhost/peer
    Register file: peer = 0 file = "peer0_small_file1"
    Register file: peer = 0 file = "peer0_small_file0"
    Register file: peer = 0 file = "peer0_medium_file1"
    Register file: peer = 0 file = "peer0_medium_file0"
    Register file: peer = 0 file = "peer0_large_file0"
    Register file: peer = 0 file = "peer0_large_file1"
    
    $ ./run.sh c ./peer_shared_directories/0
    Reading the shared directory: ./peer_shared_directories/0
    Sharing: peer0_small_file1
    Sharing: peer0_small_file0
    Sharing: peer0_medium_file1
    Sharing: peer0_medium_file0
    Sharing: peer0_large_file0
    Sharing: peer0_large_file1
    My peer identifier is 56521_0
    Binding Client 0 RMI Interface to //localhost/peer0
    PEER(0): Hello Client.
    PEER(0): Enter the file name you would like to search. (type "q" to quit.)
    
    
    PEER(0): Enter the file name you would like to search. (type "q" to quit.)
    peer1_large_file1
    PEER(0): Following clients have 'peer1_large_file1' :
    Peer 1
    Peer 2
    PEER(0): Enter a client number (Download will proceed in background) "b" to go back:
    
    Search called: filename = peer1_large_file1
    Search called: filename = foo

    PEER(0): Following clients have 'peer1_large_file1' :
    Peer 1
    PEER(0): Enter a client number (Download will proceed in background) "b" to go back:
    5
    ERROR: Invalid client selection.

    PEER(0): Following clients have 'peer1_large_file1' :
    Peer 1
    Peer 2
    PEER(0): Enter a client number (Download will proceed in background) "b" to go back:
    1
    PEER(0): Connecting to peer 1
    PEER(0): Retrieving file "peer1_large_file1" from 'peer 1'. You'll be notified when it finishes.
    PEER(0): Enter the file name you would like to search. (type "q" to quit.)
    Peer 1: Retrieve start time =1633248465838
    Peer 1: Retrieve end time =1633248465856
    [####################] 100% : Download 'peer1_large_file1'' complete!
    Change detected in the shared directory.
    Reading the shared directory: ./peer_shared_directories/0
    Sharing: peer0_small_file1
    Sharing: peer0_small_file0
    Sharing: peer1_large_file1
    Sharing: peer0_medium_file1
    Sharing: peer0_medium_file0
    Sharing: peer0_large_file0
    Sharing: peer0_large_file1
    PEER(0): Enter the file name you would like to search. (type "q" to quit.)
    
    PEER(0): Enter the file name you would like to search. (type "q" to quit.)
    q
    PEER(0): Quitting.
    Shutting down the client.
    $|
    
    
    Deregister called: peer = 0, filename = peer0_small_file1
    Deregister called: peer = 0, filename = peer0_small_file0
    Deregister called: peer = 0, filename = peer1_large_file1
    Deregister called: peer = 0, filename = peer0_medium_file1
    Deregister called: peer = 0, filename = peer0_medium_file0
    Deregister called: peer = 0, filename = peer0_large_file0
    Deregister called: peer = 0, filename = peer0_large_file1
    
Test case log
    
    $ ./test_parallelRetrieve.sh 3
    Deleting any existing directories.
    Creating Sharable Directories for 3 peers.
    done
    Starting test in 3 seconds...
    Binding Server RMI Interface to //localhost/central_server
    Initializing 3 peers.
    Reading the shared directory: ./peer_shared_directories/0
    Sharing: peer0_small_file1
    Sharing: peer0_small_file0
    Sharing: peer0_medium_file1
    Sharing: peer0_medium_file0
    Sharing: peer0_large_file0
    Sharing: peer0_large_file1
    Register called: lookupString = //localhost/peer
    Register file: peer = 0 file = "peer0_small_file1"
    Register file: peer = 0 file = "peer0_small_file0"
    Register file: peer = 0 file = "peer0_medium_file1"
    Register file: peer = 0 file = "peer0_medium_file0"
    Register file: peer = 0 file = "peer0_large_file0"
    Register file: peer = 0 file = "peer0_large_file1"
    My peer identifier is 2849257_0
    Binding Client 0 RMI Interface to //localhost/peer0
    PEER(0): Peer 0 ready!
    Reading the shared directory: ./peer_shared_directories/1
    Sharing: peer1_medium_file1
    Sharing: peer1_large_file1
    Sharing: peer1_medium_file0
    Sharing: peer1_large_file0
    Sharing: peer1_small_file0
    Sharing: peer1_small_file1
    Register called: lookupString = //localhost/peer
    Register file: peer = 1 file = "peer1_medium_file1"
    Register file: peer = 1 file = "peer1_large_file1"
    Register file: peer = 1 file = "peer1_medium_file0"
    Register file: peer = 1 file = "peer1_large_file0"
    Register file: peer = 1 file = "peer1_small_file0"
    Register file: peer = 1 file = "peer1_small_file1"
    My peer identifier is 3632113_1
    Binding Client 1 RMI Interface to //localhost/peer1
    PEER(1): Peer 1 ready!
    Reading the shared directory: ./peer_shared_directories/2
    Sharing: peer2_large_file0
    Sharing: peer2_large_file1
    Sharing: peer2_medium_file1
    Sharing: peer2_medium_file0
    Sharing: peer2_small_file1
    Sharing: peer2_small_file0
    Register called: lookupString = //localhost/peer
    Register file: peer = 2 file = "peer2_large_file0"
    Register file: peer = 2 file = "peer2_large_file1"
    Register file: peer = 2 file = "peer2_medium_file1"
    Register file: peer = 2 file = "peer2_medium_file0"
    Register file: peer = 2 file = "peer2_small_file1"
    Register file: peer = 2 file = "peer2_small_file0"
    My peer identifier is 3886455_2
    Binding Client 2 RMI Interface to //localhost/peer2
    PEER(2): Peer 2 ready!
    Peer 0 thread is running...
    Staring test.
    Peer 0: thread_end_count = 0, num_peers = 3
    Peer 1 thread is running...
    Peer 2 thread is running...
    Search called: filename = peer0_large_file0
    Search called: filename = peer0_large_file0
    Peer 2: Retrieve start time = 1633249143018
    Peer 1: Retrieve start time = 1633249143020
    Client 0 retrieve called: filename = peer0_large_file0
    Client 0 retrieve called: filename = peer0_large_file0
    Peer 1: Retrieve end time = 1633249143029
    Peer 2: Retrieve end time = 1633249143033
    Exiting Peer 1
    Exiting Peer 2
    ======================== Test Results ================================
    Peer 1: start = 1633249143020, end = 1633249143033
    Peer 2: start = 1633249143020, end = 1633249143033
    Average time but for peer = 13
    File size = 10K
    =====================================================================
    Exiting Peer 0
    Ended test.
    ^CShutting down the client.
    Shutting down the client.
    Shutting down the client.
    Deregister called: peer = 2, filename = peer2_large_file0
    Deregister called: peer = 2, filename = peer2_large_file1
    Deregister called: peer = 2, filename = peer2_medium_file1
    Deregister called: peer = 2, filename = peer2_medium_file0
    Deregister called: peer = 2, filename = peer2_small_file1
    Deregister called: peer = 2, filename = peer2_small_file0
    Deregister called: peer = 0, filename = peer0_small_file1
    Deregister called: peer = 0, filename = peer0_small_file0
    Deregister called: peer = 0, filename = peer0_medium_file1
    Deregister called: peer = 0, filename = peer0_medium_file0
    Deregister called: peer = 0, filename = peer0_large_file0
    Deregister called: peer = 0, filename = peer0_large_file1
    Deregister called: peer = 1, filename = peer1_medium_file1
    Deregister called: peer = 1, filename = peer1_large_file1
    Deregister called: peer = 1, filename = peer1_medium_file0
    Deregister called: peer = 1, filename = peer1_large_file0
    Deregister called: peer = 1, filename = peer1_small_file0
    Deregister called: peer = 1, filename = peer1_small_file1
