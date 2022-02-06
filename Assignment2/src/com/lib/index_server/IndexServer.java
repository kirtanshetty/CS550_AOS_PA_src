package com.lib.index_server;

import java.util.*;
import java.util.Map.Entry;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
// import java.rmi.server.hostname;

import com.lib.interfaces.RMIServerInterface;

public class IndexServer extends UnicastRemoteObject implements RMIServerInterface {
  final static String rmiServerStr = "//localhost/central_server";
  static int incomingPeerId;

  private Map<String, ArrayList<Integer>> fileIndex; // to search for peer ids for a particular file
  private Map<Integer, RegisteredPeerInfo> rpiIndex; // to search for peer info from peer ids

  private final int RANDOM_UPPER_BOUND = 9999999;
  private Random rand = new Random(); // generate randon numbers to assign to each client.

  private ArrayList<RMIServerInterface> neighbors; // holds connections to all neighbor superpeers
  private int id; // this superpeer ID
  private Map<MessageID, Integer> queries; // buffer of query messages received from neighbors and forwarded

  public IndexServer(int superpeerId, String rmiInterfaceString, ArrayList<Integer> neighborIds, int bufferSize) throws RemoteException {
    super();

    System.out.println("neighborIds " + neighborIds);

    // If this check is true, then either the config was not properly parsed or code has not yet been implemented to parse the config yet
    if(bufferSize <= 0) {
      System.err.println("ERROR: IndexServer bufferSize is <= 0.");
      System.exit(0);
    }

    incomingPeerId = 0; // setting the initital count as 0
    fileIndex = new HashMap<String, ArrayList<Integer>>();
    rpiIndex = new HashMap<Integer, RegisteredPeerInfo>();
    id = superpeerId;

    // a LinkedHashMap allows us to efficiently remove the oldest element
    // automatically when a new element is inserted and it exceeds bufferSize
    queries = new LinkedHashMap<MessageID, Integer>() {
      @Override
      protected boolean removeEldestEntry(final Map.Entry eldest) {
        return size() > bufferSize;
      }
    };

    try{
      System.out.println("Binding Server RMI Interface to " + rmiInterfaceString);
      // Binding to the RMI Interface
      Naming.rebind(rmiInterfaceString, this);
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: IndexServer Exception while binding RMI Interface: " + ex.toString());
      ex.printStackTrace();
    }

    // connect and test
    neighbors = new ArrayList<RMIServerInterface>();
    // int i = 0;
    RMIServerInterface s;

    Set<Integer> neighborSet = new HashSet<Integer>();

    for (int i = 0; i < neighborIds.size(); i++) {
      neighborSet.add(neighborIds.get(i));
    }

    while(neighborSet.size() > 0){
      Iterator<Integer> itr = neighborSet.iterator();
      while(itr.hasNext()){
        Integer ns = itr.next();
        try {
          System.out.println("Connecting to the superpeer neighbour: " + rmiServerStr + ns);
          s = (RMIServerInterface)Naming.lookup(rmiServerStr + ns); // get the server
          s.testCall(); // if this throws an exception, then we know we're not connected, so try again
          neighbors.add(s); // if we get here, test was successful so we can add it
          itr.remove();
          System.out.println("Successfully connected to superpeer neighbour: " + ns);
        }
        catch(Exception e) {
          System.out.println("Connection unsuccessfull for Super peer neighbour :" + rmiServerStr + ns);
          continue;
        }
        finally{
          System.out.println("Attempting reconnect in 1 second...");
        }
      }
      try{ Thread.sleep(1000); }catch(Exception ex){}
    }

    System.out.println("Done.");
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

  @Override
  public void testCall() throws RemoteException {}

  @Override
  public QueryHit forwardQuery(Query q) throws RemoteException {
    if(queries.containsKey(q.messageId)) {
      // we've seen this query before, so we can return an empty
      // queryhit because we returned a real queryhit in the past
      return null;
    }

    QueryHit qh = new QueryHit();
    qh.superpeerId = new ArrayList<Integer>();
    qh.peerId = new ArrayList<Integer>();

    System.out.println("Query received: message id = " + q.messageId + ", filename = " + q.filename + ", timeToLive = " + q.timeToLive);
    queries.put(q.messageId, 0); // LinkedHashMap so size is controlled automatically

    try{
      ArrayList<Integer> result = search(q.filename);
      if(result != null) {
        for(int p : result) {
          qh.superpeerId.add(id);
          qh.peerId.add(p);
        }
      }

      if(q.timeToLive > 0) {
        System.out.println("Forwarding query: message id = " + q.messageId + ", filename = " + q.filename + ", timeToLive = " + q.timeToLive);
        for(RMIServerInterface n : neighbors) {
          QueryHit response = n.forwardQuery(q);
          if(response != null){
            qh.superpeerId.addAll(response.superpeerId);
            qh.peerId.addAll(response.peerId);
          }
        }
      }
    }
    catch(Exception e) { System.out.println("\n\n"); e.printStackTrace(); System.out.println("\n\n"); }

    return qh;
  }
}
