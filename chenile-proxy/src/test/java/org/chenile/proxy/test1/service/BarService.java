package org.chenile.proxy.test1.service;

public interface BarService<T extends Baz> {
	BarModel<T> doubleIt(T entity);
}