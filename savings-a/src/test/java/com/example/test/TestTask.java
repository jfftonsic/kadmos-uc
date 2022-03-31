package com.example.test;

public abstract class TestTask<TI extends TestInput, TR extends TestReport> {
    public abstract TR run(TI input);
}
