package com.ch629.kotlin_builder;

public class Main {
  public static void main(String[] args) {
    // Can't add a @JvmStatic extension function to allow for TestData.builder()
    // Only way to deal with this, would be to generate the class from scratch
    // to be handled similarly to Immutables
    TestData testData = new TestDataBuilder().name("name").c(true).test(5).build();
    System.out.println(testData);
  }
}
