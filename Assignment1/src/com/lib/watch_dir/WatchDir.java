package com.lib.watch_dir;


import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Callable;
import com.sun.nio.file.SensitivityWatchEventModifier;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchDir {
  private WatchService ws;
  private Path p;
  private boolean keepWatching;

  @SuppressWarnings("unchecked")
  private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
    return (WatchEvent<T>)event;
  }

  public WatchDir(Path watchDir){
    try{
      p = watchDir;
      ws = FileSystems.getDefault().newWatchService();
      p.register(ws, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
    }
    catch(IOException ioe){
      System.err.println("WatchDir: IOException while creating Watch Service: " + ioe.toString());
      ioe.printStackTrace();
    }
  }

  public void BeginWatch(Callable<Void> cb){
    keepWatching = true;
    while (keepWatching) {
      WatchKey wk;
      try {
        wk = ws.take();

        for (WatchEvent<?> e : wk.pollEvents()) {
          WatchEvent<Path> ev = cast(e);
          Path filename = ev.context();

          try{
            cb.call();
          }
          catch(Exception exp){
            System.out.printf("Exception while calling the callback " + exp.toString());
          }

          // Uncomment for testing
          // if(e.kind() == ENTRY_CREATE) {
          //   System.out.printf("ENTRY_CREATE " + filename.getFileName());
          // }
          // else if(e.kind() == ENTRY_DELETE) {
          //   System.out.printf("ENTRY_DELETE " + filename.getFileName());
          // }
          // else if(e.kind() == ENTRY_MODIFY) {
          //   System.out.printf("ENTRY_MODIFY " + filename.getFileName());
          // }
        }
        wk.reset();

      } catch (InterruptedException ie) {
        System.err.println("WatchDir: InterruptedException while starting the Watch Service: " + ie.toString());
        ie.printStackTrace();
        return;
      }
      catch (ClosedWatchServiceException cwse) {
        // No need to log it as it is expected.
      }
    }
  }

  public void EndWatch(){
    keepWatching = false;
    try{
      ws.close();
    }
    catch(IOException ioe){
      System.err.println("WatchDir: IOException while ending Watch Service: " + ioe.toString());
      ioe.printStackTrace();
    }
  }
}