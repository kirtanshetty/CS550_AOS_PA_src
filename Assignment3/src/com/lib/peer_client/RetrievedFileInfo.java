package com.lib.peer_client;

import java.util.*;
import java.io.Serializable;

public class RetrievedFileInfo implements Serializable {
  public String filename; // I think this can function as the last-modified-time
  public int version; // I think this can function as the last-modified-time
  public int originServerId;
  public int originPeerId;
  public boolean valid;
  public Date lastVerified;
  public int timeToRefresh; // in seconds.
  public boolean owner;
}
