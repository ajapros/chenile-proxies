package org.chenile.proxy.test1.service;

public interface Baz1Service extends BarService<Baz1>{
    BarModel<Baz1> doubleIt(Baz1 entity);
}
