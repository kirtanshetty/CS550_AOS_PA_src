package com.lib.index_server;

import java.util.*;
import java.util.Map.Entry;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
// import java.rmi.server.hostname;

import com.lib.interfaces.RMIServerInterface;

public class IndexServer extends UnicastRemoteObject implements RMIServerInterface {
  static int incomingPeerId;

  private Map<String, ArrayList<Integer>> fileIndex; // to search for peer ids for a particular file
  private Map<Integer, RegisteredPeerInfo> rpiIndex; // to search for peer info from peer ids

  private final int RANDOM_UPPER_BOUND = 9999999;
  private Random rand = new Random(); // generate randon numbers to assign to each client.

  public IndexServer(String rmiInterfaceString) throws RemoteException {
    super();

    incomingPeerId = 0; // setting the initital count as 0
    fileIndex = new HashMap<String, ArrayList<Integer>>();
    rpiIndex = new HashMap<Integer, RegisteredPeerInfo>();

    try{
      System.out.println("Binding Server RMI Interface to " + rmiInterfaceString);
      // Binding to the RMI Interface
      Naming.rebind(rmiInterfaceString, this);
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: IndexServer Exception while binding RMI Interface: " + ex.toString());
      ex.printStackTrace();
    }
  }

  @Override
  public String register(String lookupString, Vector<String> filenames, String identifier) throws RemoteException {
    System.out.println("Register called: lookupString = " + lookupString);
    RegisteredPeerInfo rpi = null;

    if(identifier != null){
      Iterator<Entry<Integer, RegisteredPeerInfo>> it = rpiIndex.entrySet().iterator();

      while (it.hasNext()){
        HashMap.Entry<Integer, RegisteredPeerInfo> m = (Map.Entry<Integer, RegisteredPeerInfo>)it.next();
        rpi = m.getValue();
        if(rpi.peerIdStr.equals(identifier)){
          System.out.println("Updating file list for peer " + rpi.peerId + ".");
          rpi.filenames = filenames;
          updateFileList(filenames, rpi);
        }
      }

      return (rpi != null ? rpi.peerIdStr : null);
    }

    // add the peer info to the index
    rpi = new RegisteredPeerInfo();

    rpi.lookupString = lookupString;
    rpi.peerIdStr = Integer.toString(rand.nextInt(RANDOM_UPPER_BOUND)) + "_" + Integer.toString(incomingPeerId);
    rpi.peerId = incomingPeerId;
    incomingPeerId++;
    rpi.filenames = filenames;

    rpiIndex.put(rpi.peerId, rpi);
    updateFileList(filenames, rpi);

    // for(String f : filenames) {
    //   System.out.println("Register file: peer = " + rpi.peerId + " file = \"" + f + "\"");

    //   // check if the file is new in the index
    //   if(!fileIndex.containsKey(f)) {
    //     fileIndex.put(f, new ArrayList<Integer>());
    //   }

    //   // check if the peerId is already present for that particular file.
    //   if(!fileIndex.get(f).contains(rpi.peerId)){
    //     fileIndex.get(f).add(rpi.peerId);
    //   }
    // }

    return rpi.peerIdStr;
  }

  @Override
  public ArrayList<Integer> search(String filename) throws RemoteException {
    System.out.println("Search called: filename = " + filename);
    return fileIndex.get(filename); // returns list of peers
  }

  @Override
  public int deregister(String identifier, Vector<String> filenames) throws RemoteException {
    if(identifier == null){
      return -1;
    }

    RegisteredPeerInfo rpi = null;
    Iterator<Entry<Integer, RegisteredPeerInfo>> it = rpiIndex.entrySet().iterator();

    while (it.hasNext()){
      HashMap.Entry<Integer, RegisteredPeerInfo> m = (Map.Entry<Integer, RegisteredPeerInfo>)it.next();
      rpi = m.getValue();
      if(rpi.peerIdStr.equals(identifier)){
        for(String f : filenames) {
          System.out.println("Deregister called: peer = " + rpi.peerId + ", filename = " + f);
          fileIndex.get(f).remove(fileIndex.get(f).indexOf(rpi.peerId));
          rpiIndex.get(rpi.peerId).filenames.remove(f);
        }
      }
    }

    // System.out.println("Deregister called: peer = " + peerId + ", filename = " + filename);
    // fileIndex.get(filename).remove(fileIndex.get(filename).indexOf(peerId));
    // rpiIndex.get(peerId).filenames.remove(filename);
    return 0;
  }

  private void updateFileList(Vector<String> filenames, RegisteredPeerInfo rpi){
    for(String f : filenames) {
      System.out.println("Register file: peer = " + rpi.peerId + " file = \"" + f + "\"");

      // check if the file is new in the index
      if(!fileIndex.containsKey(f)) {
        fileIndex.put(f, new ArrayList<Integer>());
      }

      // check if the peerId is already present for that particular file.
      if(!fileIndex.get(f).contains(rpi.peerId)){
        fileIndex.get(f).add(rpi.peerId);
      }
    }
  }
}
