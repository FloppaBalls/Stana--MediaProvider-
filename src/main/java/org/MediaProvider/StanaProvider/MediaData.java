package org.MediaProvider.StanaProvider;

public class MediaData {
	public MediaData()
	{
		id = extension = 0;
		blob = new byte[0];
	}
	public MediaData(int extension0 , int id0) {
		id = id0; extension = extension0;
		blob = new byte[0];
	}
	public byte[] blob;
	public int extension;
	public int id;
}
