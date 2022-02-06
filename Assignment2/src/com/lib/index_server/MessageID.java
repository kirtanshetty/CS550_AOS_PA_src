package com.lib.index_server;

import java.io.Serializable;

public class MessageID implements Serializable {
  public int superpeerId;
  public int peerId;
  public int seq;

  @Override
  @Deprecated
  public int hashCode() {
		final int prime = 31; // random prime
		int result = 1;
		result = prime * result + ((superpeerId == 0) ? 0 : (new Integer(superpeerId)).hashCode());
		result = prime * result + ((peerId == 0) ? 0 : (new Integer(peerId)).hashCode());
		result = prime * result + ((seq == 0) ? 0 : (new Integer(seq)).hashCode());
		return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MessageID))
      return false;
    if (obj == this)
      return true;

    MessageID m = (MessageID) obj;
    return (
      this.superpeerId == m.superpeerId &&
      this.peerId == m.peerId &&
      this.seq == m.seq
    );
  }

  public String toString() {
    return "[" + superpeerId + "/" + peerId + ":" + seq + "]";
  }
}
