package com.test;

import java.util.*;

import com.lib.index_server.IndexServer;
import com.lib.interfaces.RMIServerInterface;

public class IndexTest {
  public static void main(String[] args) {
    System.out.println("main started!");

    // Handle RMI registration for the TA when he runs the project
    try{
      // IndexServer is = new IndexServer("//localhost/MyServer");
      RMIServerInterface is = new IndexServer("//localhost/central_server");
      // is.start("//localhost/MyServer");

      Vector<String> testFiles = new Vector<String>();

      testFiles.add("foo");
      testFiles.add("moo");
      testFiles.add("boo");

      is.register("//localhost/str", testFiles);
      is.register("//localhost/str2", testFiles);

      is.search("foo");
      is.deregister(1);
    }
    catch (Exception ex) {
      System.err.println("IndexTest Exception: " + ex.toString());
      ex.printStackTrace();
    }

  }
}