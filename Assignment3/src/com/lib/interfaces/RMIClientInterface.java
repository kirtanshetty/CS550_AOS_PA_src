package com.lib.interfaces;

import java.util.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.lib.peer_client.FileInfo;

public interface RMIClientInterface extends Remote {
  public FileInfo retrieve(String filename, int remotePeerId) throws RemoteException;
  /* --- start PA3 change --- */
  public void invalidateFile(String filename) throws RemoteException; // push-based
  public boolean poll(String filename, int version) throws RemoteException; //pull-based
  /* ---- end PA3 change ---- */
}
