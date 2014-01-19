package org.abdr.node.master;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class StoreUseComparator implements Comparator<Entry<Integer, AtomicInteger>> {

	@Override
	public int compare(Entry<Integer, AtomicInteger> arg0, Entry<Integer, AtomicInteger> arg1) {
		return arg0.getValue().get() - arg1.getValue().get();
	}

}
