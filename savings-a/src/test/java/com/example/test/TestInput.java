package com.example.test;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE) @AllArgsConstructor
public class TestInput {
    String description;
    int numberOfRepetitions;

}
