package org.abdr.node;

import java.util.Comparator;

import oracle.kv.Version;

public class KeyValueVersion implements Comparator<KeyValueVersion>{
	private String key;
	private String cat;
	private byte[] data;
	private Version version;

	public KeyValueVersion(String key, String cat, byte[] data, Version version) {
		super();
		this.key = key;
		this.cat = cat;
		this.data = data;
		this.version = version;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	@Override
	public int compare(KeyValueVersion arg0, KeyValueVersion arg1) {
//		sort by (key,cat)
		int c = arg0.getKey().compareTo(arg1.getKey());
		if (c == 0)
			return arg0.getCat().compareTo(arg1.getCat());
		else
			return c;
	}
}
