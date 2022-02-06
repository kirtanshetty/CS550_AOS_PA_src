package com.lib.interfaces;

import java.util.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServerInterface extends Remote {
  public String register(String lookupString, Vector<String> filenames, String identifier) throws RemoteException;
  public ArrayList<Integer> search(String filename) throws RemoteException;
  public int deregister(String identifier, Vector<String> filenames) throws RemoteException;
}
