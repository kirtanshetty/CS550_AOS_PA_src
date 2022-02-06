package com.lib.index_server;

import java.io.Serializable;

public class Invalidation implements Serializable {
  public MessageID messageId;
  public int timeToLive; // because this gets forwarded just like Query
  public int originServerId;
  public int originPeerId;
  public String filename;
  public int version;
}
