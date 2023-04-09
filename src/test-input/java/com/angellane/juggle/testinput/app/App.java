package com.angellane.juggle.testinput.app;

import com.angellane.juggle.testinput.lib.Lib;

public class App {
  public static void main(String[] args) {
    System.out.println("Hello from " + lib);
  }

  public static Lib lib = Lib.libFactory();
}
