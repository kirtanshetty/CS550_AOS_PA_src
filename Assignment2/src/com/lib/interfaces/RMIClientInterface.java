package com.lib.interfaces;

import java.util.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.lib.peer_client.FileInfo;

public interface RMIClientInterface extends Remote {
  public FileInfo retrieve(String filename, int remotePeerId) throws RemoteException;
}
