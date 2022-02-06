package com.app.client;

import java.util.*;
import java.rmi.Naming;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import com.lib.watch_dir.WatchDir;
import com.lib.interfaces.*;
import com.lib.peer_client.*;

// public int register(String ip, String lookupString, Vector<String> filenames)

public class Client {
  final static String rmiStr = "//localhost/peer";
  final static String rmiServerStr = "//localhost/central_server";

  private static String dir;
  private static String peerIdStr; // used to identify peer at the server
  private static int myPeerId;
  private static WatchDir wd;
  private static RMIServerInterface centralServer;
  private static Vector<String> sharedFiles;

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
    if(args.length != 1) {
      System.err.println("ERROR: Please specify one directory.");
      System.exit(0);
    }

    // The one and only command line arg is the working directory
    dir = args[0];

    // Get all non-directory files with file size less than MAX FILE SIZE. see retrieve().
    sharedFiles = ReadSharedDirectory(dir);

    try{
      centralServer = (RMIServerInterface)Naming.lookup(rmiServerStr); // connect to index server
      peerIdStr = centralServer.register(rmiStr, sharedFiles, null);         // registering the files
      System.out.println("My peer identifier is " + peerIdStr);

      // need to get these strings dynamically
      myPeerId = Integer.parseInt(peerIdStr.split("_")[1]);
      PeerClient pc = new PeerClient(rmiStr, myPeerId, dir);
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while CONNECTING to central sever: " + ex.toString());
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

        try{
          // Get list of peer IDs who have desired file
          ArrayList<Integer> clientList = centralServer.search(strInput);

          if(clientList == null || clientList.size() == 0){
            System.err.println("ERROR: OOPS! None of the clients have file " + strInput);
            continue;
          }

          // PrintMessageLn("Client list start");
          String cliList = "";
          for(int i = 0; i < clientList.size(); i++){
            // System.out.print(clientList.get(i));
            cliList += "Peer " + clientList.get(i);
            if(i < clientList.size() - 1)
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


          if(selectedClient == myPeerId){ // Check if the selected this client
            System.err.println("ERROR: File already exists.");
            continue;
          }
          else if(!clientList.contains(selectedClient)) {
            System.err.println("ERROR: Invalid client selection.");
            continue;
          }

          try{
            PrintMessageLn("Connecting to peer " + selectedClient);
            RMIClientInterface peer = (RMIClientInterface)Naming.lookup(rmiStr + selectedClient);
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
}
