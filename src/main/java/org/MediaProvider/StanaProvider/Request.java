package org.MediaProvider.StanaProvider;

public enum Request {
	MediaUploadId(0) , AddChunk(1);
	
	Request(int number0)
	{
		this.number = startingPoint + number0;
	}
	public String toString()
	{
		return String.valueOf(number);
	}
	public int number;
	
	//the client already receives request from it's own client. I differentiate the the two types of request by their starting point
	private int startingPoint = 100;
}
