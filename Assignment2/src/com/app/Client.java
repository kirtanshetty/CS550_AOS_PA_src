package com.app.client;

import java.util.*;
import java.rmi.Naming;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.concurrent.Callable;

import com.lib.watch_dir.WatchDir;
import com.lib.interfaces.*;
import com.lib.peer_client.*;
import com.lib.index_server.Query;
import com.lib.index_server.QueryHit;
import com.lib.index_server.MessageID;

import org.json.*;

public class Client {
  final static String rmiStr = "//localhost/peer/";
  final static String rmiServerStr = "//localhost/central_server";

  private static String dir;
  private static String peerIdStr; // used to identify peer at the server
  private static String topologyType;
  private static int myPeerId;
  private static WatchDir wd;
  private static RMIServerInterface centralServer;
  private static Vector<String> sharedFiles;

  private static String jsonConfig;
  private static int configId; // for using as an index into JSON config ONLY!
  private static int superpeerId;
  private static int timeToLive;
  private static int seq = 0;

  public static void PrintMessageLn(String str){
    System.out.println("PEER(" + myPeerId + "): " + str);
  }

  public static void PrintClientOptions(){
    PrintMessageLn("Enter the file name you would like to search. (type \"q\" to quit.)");
  }

  public static Vector<String> ReadSharedDirectory(String dir){
    System.out.println("Reading the shared directory: " + dir);
    File folder = new File(dir);

    if(!folder.isDirectory() || !folder.exists()) {
      System.err.println("ERROR: The folder you entered \"" + dir + "\" is not a valid directory.");
      System.exit(0);
    }

    // Read all the files in the directory and return
    Vector<String> filenames = new Vector<String>();

    for(File f : folder.listFiles()) {
      if(!f.isDirectory() && f.length() <= 1024*1024) {
        System.out.println("Sharing: " + f.getName());
        filenames.add(f.getName());
      }
    }

    return filenames;
  }

  public static void retrieveFile(final String filename, final int peerId, final RMIClientInterface peer) {
    PrintMessageLn("Retrieving file \"" + filename + "\" from 'peer " + peerId + "'. You'll be notified when it finishes.");

    try{
      Thread t_retrieve = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            System.out.println("Peer " + peerId + ": Retrieve start time =" + System.currentTimeMillis());
            // Get file
            FileInfo fileinfo = peer.retrieve(filename, peerId);
            System.out.println("Peer " + peerId + ": Retrieve end time =" + System.currentTimeMillis());

            // Save file
            File f = new File(dir, fileinfo.filename);
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f,true);

            if(fileinfo.len > 0)
              out.write(fileinfo.data, 0, fileinfo.len);
            else
              out.write(fileinfo.data, 0, 0);

            out.flush();
            out.close();
            // PrintMessageLn("Done writing data...");
          }
          catch(Exception e){
            e.printStackTrace();
          }

          System.out.println("[####################] 100% : Download '" + filename + "'' complete!");
          return;
        }
      });
      t_retrieve.start();
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while CONNECTING to peer client: " + ex.toString());
      ex.printStackTrace();
    }
  }

  public static void main(String[] args) {
    // Check if the name to the shared directory is provided
    if(args.length != 3) {
      System.err.println("ARGS: [working dir] [json config file] [config id]");
      System.exit(0);
    }

    // get the command line args
    jsonConfig = args[0];
    configId = Integer.parseInt(args[1]);
    dir = args[2];

    // Get all non-directory files with file size less than MAX FILE SIZE. see retrieve().
    sharedFiles = ReadSharedDirectory(dir);

    // parse config
    try {
      Path filePath = Paths.get(jsonConfig);
      // String rawJson = Files.readString(filePath);
      String rawJson = new String(Files.readAllBytes(Paths.get(jsonConfig)));
      JSONObject json = new JSONObject(rawJson);
      superpeerId = json.getJSONArray("peers").getInt(configId);
      timeToLive = json.getInt("timeToLive");
      topologyType = json.getString("topologyType");
      System.out.println("Topology type: " + topologyType);
    } catch(Exception ex) {
      System.err.println("EXCEPTION: Client Exception while PARSING json config: " + ex.toString());
      ex.printStackTrace();
    }

    if(timeToLive <= 0) {
      System.err.println("ERROR: TTL should be a positive value.");
      System.exit(0);
    }

    try{
      centralServer = (RMIServerInterface)Naming.lookup(rmiServerStr + superpeerId); // connect to index server
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while CONNECTING to central sever: " + ex.toString());
      ex.printStackTrace();
    }

    try {
      peerIdStr = centralServer.register(rmiStr, sharedFiles, null);         // registering the files
      System.out.println("My peer identifier is " + peerIdStr);

      // need to get these strings dynamically
      myPeerId = Integer.parseInt(peerIdStr.split("_")[1]);
      PeerClient pc = new PeerClient(rmiStr + superpeerId + "/", myPeerId, dir);
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while REGISTERING with central server: " + ex.toString());
      ex.printStackTrace();
    }

    Path p = Paths.get(dir);
    wd = new WatchDir(p);
    // Watching the shared directory for changes on a separate thread.
    Thread t_watch = new Thread(new Runnable() {
      @Override
      public void run() {
        wd.BeginWatch(new Callable<Void>() {
          public Void call() {
            try{
              // If any change is detected, the peer needs to read the directory again
              System.out.println("Change detected in the shared directory.");
              sharedFiles = ReadSharedDirectory(dir);
              centralServer.register(rmiStr, sharedFiles, peerIdStr);
            }
            catch (Exception ex) {
              System.err.println("EXCEPTION: Client Exception while RE-REGISTERING files: " + ex.toString());
              ex.printStackTrace();
            }

            return null;
          }
        });
      }
    });
    t_watch.start(); // starting the thread

    Runtime.getRuntime().addShutdownHook(new Thread(){
      @Override
      public void run(){
        System.out.println("Shutting down the client.");
        try{
          centralServer.deregister(peerIdStr, sharedFiles);
        }
        catch (Exception ex) {
          System.err.println("EXCEPTION: Client Exception while DE-REGISTERING files during shutdown: " + ex.toString());
          ex.printStackTrace();
        }
      }
    });

    // User interface
    Scanner sc = new Scanner(System.in);
    String strInput;

    PrintMessageLn("Hello Client.");
    while(true){
      // Initial prompt
      PrintClientOptions();
      strInput = sc.nextLine();

      if(strInput.length() != 0){
        if(strInput.equals("q")){
          PrintMessageLn("Quitting.");
          System.exit(0); // This will trigger the shutdown hook so files will be regregistered.
        }

        if(strInput.equals("runEvaluationTest")){
          PrintMessageLn("Test requested");
          runEvaluationTest();
          continue;
          // System.exit(0);
        }

        try{
          // Get list of peer IDs who have desired file
          //ArrayList<Integer> clientList = centralServer.search(strInput);
          Query query = new Query();
          query.messageId = new MessageID();
          query.messageId.superpeerId = superpeerId;
          query.messageId.peerId = myPeerId;
          query.messageId.seq = seq++;
          query.timeToLive = timeToLive;
          query.filename = strInput;
          QueryHit queryHit = centralServer.forwardQuery(query);

          if(queryHit.peerId == null || queryHit.peerId.size() == 0){
            System.err.println("ERROR: OOPS! None of the clients have file " + strInput);
            continue;
          }

          if(queryHit.peerId.size() != queryHit.superpeerId.size()){
            System.err.println("ERROR: OOPS! Invalid number of peer response received for " + strInput);
            continue;
          }

          // PrintMessageLn("Client list start");
          String cliList = "";
          for(int i = 0; i < queryHit.peerId.size(); i++){
            // System.out.print(clientList.get(i));
            cliList += "Peer " + queryHit.peerId.get(i) + " at superpeer " + queryHit.superpeerId.get(i);
            if(i < queryHit.peerId.size() - 1)
              cliList += "\n";
          }
          PrintMessageLn("Following clients have '"+ strInput +"' :\n" + cliList);

          // prompt user to select a peer
          PrintMessageLn("Enter a client number (Download will proceed in background) \"b\" to go back:");
          int selectedClient;
          try {
            selectedClient = Integer.parseInt(sc.nextLine());
          } catch(Exception e) {
            continue;
          }

          PrintMessageLn("Enter the superpeer number:");
          int selectedClientSuperpeerId;
          try {
            selectedClientSuperpeerId = Integer.parseInt(sc.nextLine());
          } catch(Exception e) {
            continue;
          }


          if(selectedClient == myPeerId && selectedClientSuperpeerId == superpeerId){ // Check if the selected this client
            System.err.println("ERROR: File already exists.");
            continue;
          }
          else if(!queryHit.peerId.contains(selectedClient) || !queryHit.superpeerId.contains(selectedClientSuperpeerId)) {
            System.err.println("ERROR: Invalid client selection.");
            continue;
          }

          try{
            PrintMessageLn("Connecting to peer " + selectedClient);
            RMIClientInterface peer = (RMIClientInterface)Naming.lookup(rmiStr + selectedClientSuperpeerId + "/" + selectedClient);
            if(peer != null)
              retrieveFile(strInput, selectedClient, peer);
            else
              PrintMessageLn("Unable to connect to peer " + selectedClient + ".");
          }
          catch(Exception e){
            // e.printStackTrace();
          }
        }
        catch (Exception ex) {
          System.err.println("EXCEPTION: Client Exception while SEARCHING to central sever: " + ex.toString());
          ex.printStackTrace();
        }
      }
      else{
        System.err.println("ERROR: Empty string received in the input!");
      }
    }
  }

  public static void testIterations(int peerCount, int fileCount, int iterations){
    int fileOffset = superpeerId + myPeerId;
    int i = 0;

    try{
      for(int k = 0; k < iterations; k++){
        for (i = 0; i < peerCount; i++) {
          if(i == fileOffset){
            continue;
          }

          for(int j = 0; j < fileCount; j++){
            String strInput = "peer" + i + "_small_file" + j;

            Query query = new Query();
            query.messageId = new MessageID();
            query.messageId.superpeerId = superpeerId;
            query.messageId.peerId = myPeerId;
            query.messageId.seq = seq++;
            query.timeToLive = timeToLive;
            query.filename = strInput;
            QueryHit queryHit = centralServer.forwardQuery(query);
          }
        }
      }
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while SEARCHING to central sever: " + ex.toString());
      ex.printStackTrace();
    }
  }

  public static void runEvaluationTest(){
    System.out.println("========================== TEST LINEAR " + topologyType + " STARTED RESULT ==========================");
    int peerCount = 17, fileCount = 4, iterations = 0;
    long startTs, endTs;
    long diff;

    startTs = System.currentTimeMillis();
    iterations = 1;
    testIterations(peerCount, fileCount, iterations);
    endTs = System.currentTimeMillis();

    diff = endTs - startTs;
    System.out.println("TEST RESULT 1:");
    System.out.println("Number of queries = " + peerCount*fileCount*iterations + ", time = " + diff + " ms");
    System.out.println("Average time per request = " + diff/(peerCount*fileCount) + " ms");

    System.out.println("-----------------------------------------------------------------------------------------");

    startTs = System.currentTimeMillis();
    iterations = 2;
    testIterations(peerCount, fileCount, iterations);
    endTs = System.currentTimeMillis();

    diff = endTs - startTs;
    System.out.println("TEST RESULT 2:");
    System.out.println("Number of queries = " + peerCount*fileCount*iterations + ", time = " + diff + " ms");
    System.out.println("Average time per request = " + diff/(peerCount*fileCount) + " ms");

    System.out.println("-----------------------------------------------------------------------------------------");

    startTs = System.currentTimeMillis();
    iterations = 3;
    testIterations(peerCount, fileCount, iterations);
    endTs = System.currentTimeMillis();

    diff = endTs - startTs;
    System.out.println("TEST RESULT 3:");
    System.out.println("Number of queries = " + peerCount*fileCount*iterations + ", time = " + diff + " ms");
    System.out.println("Average time per request = " + diff/(peerCount*fileCount) + " ms");

    System.out.println("-----------------------------------------------------------------------------------------");

    startTs = System.currentTimeMillis();
    iterations = 4;
    testIterations(peerCount, fileCount, iterations);
    endTs = System.currentTimeMillis();

    diff = endTs - startTs;
    System.out.println("TEST RESULT 4:");
    System.out.println("Number of queries = " + peerCount*fileCount*iterations + ", time = " + diff + " ms");
    System.out.println("Average time per request = " + diff/(peerCount*fileCount) + " ms");
    System.out.println("================================= TEST LINEAR " + topologyType + " END ==============================");
  }
}
