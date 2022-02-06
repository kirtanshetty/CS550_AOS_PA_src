package com.app.server;

import java.util.*;
import java.rmi.Naming;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import com.lib.interfaces.RMIServerInterface;
import com.lib.index_server.IndexServer;
import com.lib.peer_client.PeerClient;

import org.json.*;

public class CentralServer {
  final static String rmiServerStr = "//localhost/central_server";
  // static String dir;
  static String jsonConfig;
  static int id;

  public static void main(String[] args) {
    // Check if the name to the shared directory is provided
    if(args.length != 2) {
      System.err.println("ARGS: [CONFIG FILE] [SUPERPEER ID]");
      System.exit(0);
    }

    // dir = args[0];
    jsonConfig = args[0];
    id = Integer.parseInt(args[1]);
    System.out.println("Starting super peer " + id);

    ArrayList<Integer> neighbors = new ArrayList<Integer>();
    int bufferSize = 0;
    int timeToLive = 0;

    // parse config
    try {
      Path filePath = Paths.get(jsonConfig);
      // String rawJson = Files.readString(filePath);
      String rawJson = new String(Files.readAllBytes(Paths.get(jsonConfig)));
      JSONObject json = new JSONObject(rawJson);
      for(int i = 0; i < json.getJSONArray("superpeers").getJSONArray(id).length(); i++) {
        neighbors.add(json.getJSONArray("superpeers").getJSONArray(id).getInt(i));
      }
      bufferSize = json.getInt("bufferSize");
      timeToLive = json.getInt("timeToLive");
      System.out.println("Topology type: " + json.getString("topologyType"));

    } catch(Exception ex) {
      System.err.println("EXCEPTION: Client Exception while PARSING json config: " + ex.toString());
      ex.printStackTrace();
    }

    try{
      RMIServerInterface is = new IndexServer(id, rmiServerStr + id, neighbors, bufferSize);
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: CentralServer Exception while creating server: " + ex.toString());
      ex.printStackTrace();
    }

    try{
      // Register with itself as any other peer, right now just use current directory
      // String rmiPeerStr = "//localhost/peer";
      // RMIServerInterface centralServer = (RMIServerInterface)Naming.lookup(rmiServerStr);
      // String myPeerIdStr = centralServer.register(rmiPeerStr, new Vector<String>());
      // PeerClient pc = new PeerClient(rmiPeerStr, myPeerId, ".");
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: CentralServer Exception while creating client: " + ex.toString());
      ex.printStackTrace();
    }
  }
}
