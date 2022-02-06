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
