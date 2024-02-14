//package org.cardanofoundation.rosetta.common.util;
//
//import java.io.BufferedInputStream;
//import java.io.FileInputStream;
//
//public class FileUtil {
//
//  public static String readFile(String url) {
//    StringBuilder content = new StringBuilder();
//    try (BufferedInputStream fileGenesis = new BufferedInputStream(new FileInputStream(url))) {
//      byte[] bytes = new byte[500];
//      while (fileGenesis.available() != 0) {
//        fileGenesis.read(bytes);
//        content.append(new String(bytes));
//      }
//    } catch (Exception exception) {
//
//    }
//    return content.toString();
//  }
//}