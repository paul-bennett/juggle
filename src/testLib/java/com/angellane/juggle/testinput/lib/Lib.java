/*
 *  Juggle -- a declarative search tool for Java
 *
 *  Copyright 2020,2024 Paul Bennett
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.angellane.juggle.testinput.lib;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)  @interface SourceAnnotation  {}
@Retention(RetentionPolicy.CLASS)   @interface ClassAnnotation   {}
@Retention(RetentionPolicy.RUNTIME) @interface RuntimeAnnotation {}

@SuppressWarnings("unused")
public class Lib {
  public static Lib libFactory() {
    return new Lib();
  }

  // This is useful to show a method that has no modifiers
  Lib() {}

  public String toString() {
    return "Lib";
  }

  // This function lets us try various features of member queries
  public static void someFunction(
          @SourceAnnotation @ClassAnnotation @RuntimeAnnotation int foo,
          final String bar) {}

  // This method demonstrates a type parameter with multiple bounds.
  public static <T extends Number> void singleBound(T t) {}
  public static <T extends Number & Comparable<T>> void multipleBounds(T t) {}
}
