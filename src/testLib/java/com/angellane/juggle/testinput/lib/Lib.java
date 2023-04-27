package com.angellane.juggle.testinput.lib;

public class Lib {
  public static Lib libFactory() {
    return new Lib();
  }

  // This is useful to show a method that has no modifiers
  Lib() {}

  public String toString() {
    return "Lib";
  }
}
