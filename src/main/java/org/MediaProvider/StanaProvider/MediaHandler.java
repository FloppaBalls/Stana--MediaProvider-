package org.MediaProvider.StanaProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.net.*;
import java.io.*;


public class MediaHandler {
	int _sequence;
	boolean _mediaReady = false;
	List<MediaData> mediaList;
	MediaData _readyMedia;
	PrintWriter writer;
	Socket socket;
	
	public MediaHandler(Socket socket0)
	{
		 _mediaReady = false;
		 _sequence = 0;
		 socket = socket0;
		 mediaList = new ArrayList<MediaData>();
		 
		 try {
			 writer = new PrintWriter(socket.getOutputStream());
		 }catch(IOException e)
		 {
			 System.out.println(e.getMessage());
		 }
		 
	}
	public int makeNewSlot( int extension)
	{
		MediaData md = new MediaData(extension , _sequence);
		mediaList.add(md);
		_sequence++;
		
		return md.id;
	}
	public void addChunk(byte[] chunk  , int id) 
	{
		int left = 0, right = mediaList.size() - 1 , mid;
		boolean found = false;
		boolean fin = (chunk[chunk.length - 1] == '1') ? true : false;
		
		chunk = Arrays.copyOfRange(chunk, 0 , chunk.length - 1);

		int pos = 0;
		while(left <= right)
		{
			mid = (left + right) / 2;
			if (mediaList.get(mid).id < id)
			{
				left = mid + 1;
			}
			else if (id < mediaList.get(mid).id)
			{
				right = mid - 1;
			}
			else
			{
				found = true;
				pos = mid;
				break;
			}
		}
		if (found)
		{
			MediaData data = mediaList.get(pos);
			
			byte[] org = data.blob;
			byte[] combined = new byte[org.length + chunk.length];
			
			System.out.println("Array lengtht before adding: " + org.length );
			System.arraycopy(org , 0 , combined , 0 , org.length);
			System.arraycopy(chunk , 0 , combined , org.length, chunk.length);
			
			data.blob = combined;
			mediaList.set(pos, data);
			System.out.println("Array lengtht after adding: " + combined.length );
			
			if (fin)
			{
				System.out.println("~~Received final chunk");
				_readyMedia = mediaList.get(pos);
				System.out.println("~~" +  String.valueOf(combined.length) + " bytes of media uploaded");
				mediaList.remove(pos);
				_mediaReady = true;
			}
			else
				System.out.println("~~Received chunk");

			String message = "(true)";
			message = MessageHandlingBase.createCmd(message, InfoToServer.ChunkAccepted);
			
			//System.out.println("Sending to client : " + message);
			writer.write(message);
			writer.flush();
		}
		else
		{
		
			String message = "(false)";
			message = MessageHandlingBase.createCmd(message, InfoToServer.ChunkAccepted);
			//System.out.println("Sending to client : " + message);
			writer.write(message);
			writer.flush();

		}
	}
	public boolean mediaReady(){
		return _mediaReady;
	}
	public MediaData readyMedia()
	{
		return _readyMedia;
	}
};