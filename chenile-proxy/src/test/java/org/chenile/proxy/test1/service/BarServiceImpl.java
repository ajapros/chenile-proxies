package org.chenile.proxy.test1.service;

public class BarServiceImpl<T extends Baz> implements BarService<T> {
	@Override
	public BarModel<T> doubleIt(T entity) {
		BarModel<T> barModel = new BarModel<>();
		barModel.baz = entity;
		barModel.baz.setValue(entity.getValue()*2);
		return barModel;
	}
}
