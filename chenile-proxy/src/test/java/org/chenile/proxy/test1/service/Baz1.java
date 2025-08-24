package org.chenile.proxy.test1.service;

public class Baz1 implements Baz{
    private int value = 10;
    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int value) {
        this.value = value;
    }
}
