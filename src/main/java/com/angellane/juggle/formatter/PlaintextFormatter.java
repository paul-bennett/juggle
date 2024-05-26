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
package com.angellane.juggle.formatter;

public class PlaintextFormatter implements Formatter {
    @Override public String formatPunctuation   (String s) { return s; }
    @Override public String formatKeyword       (String s) { return s; }
    @Override public String formatPackageName   (String s) { return s; }
    @Override public String formatClassName     (String s) { return s; }
    @Override public String formatMethodName    (String s) { return s; }
    @Override public String formatType          (String s) { return s; }
}
