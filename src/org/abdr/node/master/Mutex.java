package org.abdr.node.master;

public class Mutex {

	private int readers;
	private int writers;

	private int writeRequests;

	public Mutex() {
		super();
		readers = 0;
		writers = 0;
		writeRequests = 0;

	}

	public synchronized void lockRead() throws InterruptedException {
		while (writers > 0 || writeRequests > 0) { //
			wait();
		}
		readers++;
	}

	public synchronized void unlockRead() {
		readers--;
		if (readers == 0)
			notifyAll();
	}

	public synchronized void lockWrite() throws InterruptedException {
		writeRequests++;

		while (readers > 0 || writers > 0) {
			wait();
		}
		writeRequests--;
		writers++;
	}

	public synchronized void unlockWrite() throws InterruptedException {
		writers--;
		notifyAll();
	}

	public synchronized void writeToRead() {
		writers--;
		readers++;
		notifyAll();
	}
}