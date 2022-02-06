package com.lib.peer_client;

import java.util.*;
import java.io.Serializable;

public class FileInfo implements Serializable {
  public String filename;
  public byte[] data;
  public int len;
}
