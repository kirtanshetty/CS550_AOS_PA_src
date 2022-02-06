package com.test.test_cases;

import java.util.*;
import java.rmi.Naming;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.lib.watch_dir.WatchDir;
import com.lib.interfaces.*;
import com.lib.peer_client.*;
import com.lib.index_server.*;

// public int register(String ip, String lookupString, Vector<String> filenames)

public class PeerTest1 implements Runnable {
  static private class TestResult{
    static public long retrieveStartTime;
    static public long retrieveEndTime;
  }

  public static long testStartTime;
  public static long testEndTime;

  final static String rmiStr = "//localhost/peer";
  final static String rmiServerStr = "//localhost/central_server";
  static boolean startTesting = false;

  public String dir;
  public String peerIdStr; // used to identify peer at the server
  public int myPeerId;
  public WatchDir wd;
  public RMIServerInterface centralServer;
  public Vector<String> sharedFiles;

  public static int peerIndex = 0;
  static TestResult[] tr;

  static int num_peers = 0;
  static int thread_end_count = 0;

  public void PrintMessageLn(String str){
    System.out.println("PEER(" + myPeerId + "): " + str);
  }

  public void PrintClientOptions(){
    PrintMessageLn("Enter the file name you would like to search. (type \"q\" to quit.)");
  }

  public Vector<String> ReadSharedDirectory(String dir){
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

  public void retrieveFile(final String filename, final int peerId, final RMIClientInterface peer) {
    PrintMessageLn("Retrieving file \"" + filename + "\" from 'peer " + peerId + "'. You'll be notified when it finishes.");

    try{
      Thread t_retrieve = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            // Get file
            FileInfo fileinfo = peer.retrieve(filename, peerId);

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

  public void watchSharedDirectory(){
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
  }

  public void exitHandler(){
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
  }

  public static void startServer(){
    Thread t_watch = new Thread(new Runnable() {
      @Override
      public void run() {
        try{
          RMIServerInterface is = new IndexServer(-1, rmiServerStr, null, 0);
        }
        catch (Exception ex) {
          System.err.println("EXCEPTION: CentralServer Exception while creating server: " + ex.toString());
          ex.printStackTrace();
        }

        try{
          // Register with itself as any other peer, right now just use current directory
          String rmiPeerStr = "//localhost/peer";
          RMIServerInterface centralServer = (RMIServerInterface)Naming.lookup(rmiServerStr);
          // String myPeerIdStr = centralServer.register(rmiPeerStr, new Vector<String>());
          // PeerClient pc = new PeerClient(rmiPeerStr, myPeerId, ".");
        }
        catch (Exception ex) {
          System.err.println("EXCEPTION: CentralServer Exception while creating client: " + ex.toString());
          ex.printStackTrace();
        }
      }
    });

    t_watch.start();
  }

  public void run(){
    System.out.println("Peer " + myPeerId + " thread is running...");
    while(!startTesting){
      // do nothing
    }

    if(myPeerId == 0){
      System.out.println("Peer "+ myPeerId + ": thread_end_count = " + thread_end_count + ", num_peers = " + num_peers);
      while(thread_end_count < (num_peers - 1)){
        try{
          Thread.sleep(1000);
        }
        catch(Exception e){
          // do nothing
        }
      }

      long avgTime = 0;
      System.out.println("======================== Test Results ================================");
      for (int i = 1; i < num_peers; i++) {
        avgTime = avgTime + (tr[myPeerId].retrieveEndTime - tr[myPeerId].retrieveStartTime);
        System.out.println("Peer " + i + ": start = " + tr[myPeerId].retrieveStartTime + ", end = " + tr[myPeerId].retrieveEndTime);
      }
      avgTime = avgTime / (num_peers - 1);
      System.out.println("Average time but for peer = " + avgTime);
      System.out.println("File size = 10K");
      System.out.println("=====================================================================");
      System.out.println("Exiting Peer " + myPeerId);
      wd.EndWatch();
      return;
    }

    try{
      String searcFile = "peer0_large_file0";
      ArrayList<Integer> clientList = centralServer.search(searcFile);
      RMIClientInterface peer = (RMIClientInterface)Naming.lookup(rmiStr + 0);

      // long retrieveStartTime = System.currentTimeMillis();
      tr[myPeerId].retrieveStartTime = System.currentTimeMillis();
      System.out.println("Peer " + myPeerId + ": Retrieve start time = " + tr[myPeerId].retrieveStartTime);
      FileInfo fileinfo = peer.retrieve(searcFile, 0);
      tr[myPeerId].retrieveEndTime = System.currentTimeMillis();
      System.out.println("Peer " + myPeerId + ": Retrieve end time = " + tr[myPeerId].retrieveEndTime);

      // System.out.println("Peer " + myPeerId + ": Total time = " + (retrieveEndTime - retrieveStartTime));

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

      wd.EndWatch();
      System.out.println("Exiting Peer " + myPeerId);
      thread_end_count += 1;
    }
    catch(Exception e){
      e.printStackTrace();
    }

    return;

  }

  public static void main(String[] args) {
    // Check if the name to the shared directory is provided
    if(args.length != 2) {
      System.err.println("ERROR: Please specify one directory and number of peers." + args[0]);
      System.exit(0);
    }

    String share_dir = args[0];
    num_peers = Integer.parseInt(args[1]);

    startServer();

    System.out.println("Starting test in 3 seconds...");
    try{
      TimeUnit.SECONDS.sleep(3);
    }
    catch(InterruptedException ie){

    }

    System.out.println("Initializing " + num_peers + " peers.");
    PeerTest1[] pt1 = new PeerTest1[num_peers];
    tr = new TestResult[num_peers];

    // Connecting all the clients with the server.
    for(int i = 0; i < num_peers; i++){
      pt1[i] = new PeerTest1();
      tr[i] = new TestResult();
      // The one and only command line arg is the working directory
      pt1[i].dir = share_dir + "/" + Integer.toString(i);

      // Get all non-directory files with file size less than MAX FILE SIZE. see retrieve().
      pt1[i].sharedFiles = pt1[i].ReadSharedDirectory(pt1[i].dir);

      try{
        pt1[i].centralServer = (RMIServerInterface)Naming.lookup(rmiServerStr); // connect to index server
        pt1[i].peerIdStr = pt1[i].centralServer.register(rmiStr, pt1[i].sharedFiles, null);   // registering the files
        System.out.println("My peer identifier is " + pt1[i].peerIdStr);

        // need to get these strings dynamically
        pt1[i].myPeerId = Integer.parseInt(pt1[i].peerIdStr.split("_")[1]);
        PeerClient pc = new PeerClient(rmiStr, pt1[i].myPeerId, pt1[i].dir);
      }
      catch (Exception ex) {
        System.err.println("EXCEPTION: Client Exception while CONNECTING to central sever: " + ex.toString());
        ex.printStackTrace();
      }

      pt1[i].watchSharedDirectory();
      pt1[i].exitHandler();

      pt1[i].PrintMessageLn("Peer " + pt1[i].myPeerId + " ready!");
    }

    Thread[] t_peer = new Thread[num_peers];
    for(int i = 0; i < num_peers; i++){
      t_peer[i] = new Thread(pt1[i]);
      t_peer[i].start();
    }

    System.out.println("Staring test.");
    startTesting = true;

    try{
      for(int i = 0; i < num_peers; i++){
        t_peer[i].join();
      }
      System.out.println("Ended test.");
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
}
