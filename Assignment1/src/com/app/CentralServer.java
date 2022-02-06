package com.app.server;

import java.util.*;
import java.rmi.Naming;

import com.lib.interfaces.RMIServerInterface;
import com.lib.index_server.IndexServer;
import com.lib.peer_client.PeerClient;

public class CentralServer {
  final static String rmiServerStr = "//localhost/central_server";

  public static void main(String[] args) {
    try{
      RMIServerInterface is = new IndexServer(rmiServerStr);
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
}
