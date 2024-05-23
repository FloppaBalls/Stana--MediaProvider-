package org.MediaProvider.StanaProvider;

public enum InfoToServer {
	MediaUploadId(0) , ChunkAccepted(1) , UploadedFileName(2);
	
	InfoToServer(int number0)
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
