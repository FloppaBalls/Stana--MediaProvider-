package org.MediaProvider.StanaProvider;

public class MessageHandlingBase {
	static String chunk_idSep = "\"chunkId\":";
	static String chunk_acceptedSep = "\"chunkAccepted\":";
	
	
	static String quoteIt(String str)
	{
		return "\"" + str + "\""; 
	}
	static String createCmd(String message , InfoToServer type)
	{
		return type.toString() + message;
	}
	
}
