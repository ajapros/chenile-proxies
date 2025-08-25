package org.chenile.proxy.test1.service;

public class Baz2 implements Baz{
    private int value = 20;
    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int value) {
        this.value = value;
    }
}
