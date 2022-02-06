package com.lib.peer_client;

import java.util.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.*;

import com.lib.interfaces.RMIClientInterface;

public class PeerClient extends UnicastRemoteObject implements RMIClientInterface {
  private int id;
  String dir;

  /* --- start PA3 change --- */
  public static Map<String, RetrievedFileInfo> fileStore;
  /* ---- end PA3 change ---- */

  public PeerClient(String rmiInterfaceString, int peerId, String directory) throws RemoteException {
    id = peerId;
    dir = directory;

    /* --- start PA3 change --- */
    fileStore = new HashMap<String, RetrievedFileInfo>();
    /* ---- end PA3 change ---- */

    try{
      System.out.println("Binding Client " + id + " RMI Interface to " + rmiInterfaceString + id);
      // Binding to the RMI Interface
      Naming.rebind(rmiInterfaceString + id, this);
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: PeerClient Exception while binding RMI Interface: " + ex.toString());
      ex.printStackTrace();
    }

    // Run this on its own thread
    try {
      // register directory and process its events
      Path path = Paths.get(dir);
      //new WatchDir(path, false, id, "").processEvents();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public FileInfo retrieve(String filename, int remotePeerId) throws RemoteException {
    System.out.println("Client " + remotePeerId + " retrieve called: filename = " + filename);

    // All the info the client needs to know to save the file
    FileInfo ret = new FileInfo();
    ret.filename = filename;
    ret.data = new byte[1024*1024]; // MAX FILE SIZE 1024x1024
    ret.len = 0;

    try {
      // Read file
      File f1 = new File(dir, filename);
      FileInputStream in = new FileInputStream(f1);
      ret.len = in.read(ret.data);
    }
    catch(Exception e){
      e.printStackTrace();
    }

    /* --- start PA3 change --- */
    // add RetrievedFileInfo to FileInfo
    ret.retrievedFileInfo = fileStore.get(filename);
    /* ---- end PA3 change ---- */

    return ret;
  }

  /* --- start PA3 change --- */
  public void insertIntoFileStore(String fn, RetrievedFileInfo rfi) {
    System.out.println("Inserting \"" + fn + "\" into the fileStore.");
    fileStore.put(fn, rfi);
  }

  @Override
  public void invalidateFile(String filename) {
    System.out.println("Client " + id + " invalidateFile called: filename = " + filename);
    if(fileStore.containsKey(filename)) {
      RetrievedFileInfo rfi = fileStore.get(filename); // get the record so we can update it
      // make sure we don't invalidate a file that we own
      if(rfi.owner == true) {
        System.out.println("Client " + id + " invalidateFile: we are the owner of the file \"" + filename + "\". Not invalidating.");
        return;
      }
      rfi.valid = false; // invalidate it
      fileStore.replace(filename, rfi); // update the store
    }
    else {
      System.out.println("Client " + id + " invalidateFile: we don't have the file \"" + filename + "\"");
    }
  }

  @Override
  public boolean poll(String filename, int version) {
    //System.out.println("Client " + id + " poll called: filename = " + filename + ", version = " + version);
    if(fileStore.containsKey(filename)) {
      // if true, the polling peer has the latest version, so they still have a valid copy
      // if false, the polling peer has an out-of-date version, so invalidate it
      return fileStore.get(filename).version == version;
    }
    else {
      //System.out.println("Client " + id + " poll: we don't have the file \"" + filename + "\"");
      return true;
    }
  }
  /* ---- end PA3 change ---- */
}
