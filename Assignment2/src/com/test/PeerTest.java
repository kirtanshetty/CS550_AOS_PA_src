package com.test;

import java.util.*;

import com.lib.peer_client.PeerClient;

public class PeerTest {
  public static void main(String[] args) {
    System.out.println("main started!");

    // Handle RMI registration for the TA when he runs the project
    try{
      PeerClient pc = new PeerClient("//localhost/peer");
      pc.retrieve("foo");
    }
    catch (Exception ex) {
      System.err.println("PeerTest Exception: " + ex.toString());
      ex.printStackTrace();
    }

  }
}