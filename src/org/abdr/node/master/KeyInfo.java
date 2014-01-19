package org.abdr.node.master;

import java.util.concurrent.atomic.AtomicInteger;

public class KeyInfo {
	private Mutex mutex;
	private Integer store;
	private AtomicInteger use;

	public KeyInfo(Integer store) {
		super();
		this.store = store;
		this.use = new AtomicInteger();
		this.mutex = new Mutex();
		// lock mutex ??
	}

	public Mutex getMutex() {
		return mutex;
	}

	public Integer getStore() {
		return store;
	}

	public void setStore(Integer store) {
		this.store = store;
	}

	public AtomicInteger getUse() {
		return use;
	}
}
