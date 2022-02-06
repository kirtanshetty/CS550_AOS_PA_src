package com.lib.index_server;

import java.io.Serializable;

public class Query implements Serializable {
  public MessageID messageId;
  public int timeToLive;
  public String filename;
}
