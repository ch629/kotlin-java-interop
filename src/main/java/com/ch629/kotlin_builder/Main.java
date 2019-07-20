package com.ch629.kotlin_builder;

import com.example.TestDataBuilder;

public class Main {
  public static void main(String[] args) {
    TestData testData = new TestDataBuilder().name("name").c(true).test(5).build();
    System.out.println(testData);
  }
}
