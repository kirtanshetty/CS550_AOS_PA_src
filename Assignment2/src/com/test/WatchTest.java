package com.test;

import java.util.concurrent.TimeUnit;

import java.nio.file.Path;
import java.nio.file.Paths;
import com.lib.watch_dir.WatchDir;
import java.util.concurrent.Callable;

public class WatchTest {
  static private WatchDir wd;

  public static void main(String[] args) {
    System.out.println("Begin");
    Path dir = Paths.get("/Users/kirtan/iit/cs550/gitrepos/CS550/peer1_share");

    wd = new WatchDir(dir);

    Thread t_watch = new Thread(new Runnable() {
      @Override
      public void run() {
        wd.BeginWatch(new Callable<Void>() {

          public Void call() {
            System.out.println("The Begin callback called!");
            return null;
          }
        });
      }
    });

    t_watch.start();

    System.out.println("After thread");

    try{
      TimeUnit.SECONDS.sleep(5);
    }
    catch(InterruptedException ie){

    }

    wd.EndWatch();
    System.out.println("End");
  }
}