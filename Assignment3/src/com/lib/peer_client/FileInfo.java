package com.lib.peer_client;

import java.util.*;
import java.io.Serializable;

public class FileInfo implements Serializable {
  public String filename;
  public byte[] data;
  public int len;
  /* --- start PA3 change --- */
  public RetrievedFileInfo retrievedFileInfo;
  /* ---- end PA3 change ---- */
}
