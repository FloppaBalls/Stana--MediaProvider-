package org.MediaProvider.StanaProvider;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import com.azure.identity.*;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import java.util.Arrays;


public class Server extends Thread{
	//the request manager of the app Stana
	ConnectionHandler appRequestManager;
	
	@Override
	public void run()
	{
		try {
			ServerSocket ss = new ServerSocket(8500);
			System.out.println("~~Waiting for client to connect...");
			Socket client = ss.accept();
			
			System.out.println("->Client connected! - niggg");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			appRequestManager = new ConnectionHandler(client);
			appRequestManager.go();
			
		}catch(IOException e)
		{
			System.out.println(e.toString());
		}
	}
	
	class Uploader{
		int count = 0;
		long usedStorage = 0;
		long maxStorage = 5000000000L;
		String storageDirectoryName;
		String storagePath;
		public Uploader() {
			storageDirectoryName = "storage";
			storagePath = System.getProperty("user.dir") + '\\' + storageDirectoryName;
			System.out.println(storagePath);
			File dir = new File(storagePath);
			if(!dir.exists())
			{
				boolean created = dir.mkdirs();
				if(created == false)
					System.out.println("Failed to create storage directory");
				else 
					System.out.println("Sucessfully created storage directory");
			}
			
			File list[] = dir.listFiles();
			if(list == null)
			{
				count = list.length;
				for(File f : list)
				{
					if(f.isFile() == true)
					{
						usedStorage += f.length();
					}
				}
			}
			else
				count = 0;
			
			
			
		}
		public String upload(MediaData data)
		{
			String fileName = provideFileName(data.extension);
			try {	
				String filePath = storagePath + '\\' + fileName + '.' + extensionToStr(data.extension);
				File file = new File(filePath);
				if(file.exists() == false)
				{
					boolean result = file.createNewFile();
					if(result == true)
					{
						FileOutputStream writer = new FileOutputStream(filePath);
						writer.write(data.blob);
						writer.flush();
						System.out.println("Succesfully added a new file to the storage");
					}
				}
			}catch(IOException e){
				System.out.println(e.toString());
			}
			
			return fileName;
		}
		public String provideFileName(int extension)
		{
			String fileName = "f_" ;
			
			String extensionStr = String.valueOf(extension);
			if(extension < 10)
			{
				extensionStr = "0" + extensionStr;
			}
			String countStr = String.valueOf(count);
			if(count < 10)
				countStr = "0" + countStr;
			if(count < 100)
				countStr = "0" + countStr;
			
			fileName = fileName + countStr + '_' + extensionStr;
			return fileName;
		}
		public String extensionToStr(int extension)
		{
			switch(extension)
			{
			case 1:
				return "png";
			case 2:
				return "jpg";
			default:
				return "";
			}
		}
	}
	class ConnectionHandler {
		DataInputStream reader;
		PrintWriter writer;
		Socket client;
		MediaHandler handler;
		Uploader uploader;
		
		private int byteLimit = 1204;
		
		public ConnectionHandler(Socket socket0)
		{
			client = socket0;
			try {
				writer = new PrintWriter(client.getOutputStream());
				reader = new DataInputStream(client.getInputStream());
				handler = new MediaHandler(client);
				uploader = new Uploader();
				
			}catch(IOException e)
			{
				System.out.println(e.toString());
			}
			
		}
		public void go() throws IOException {
			long bytesReceived = 0;
			while(client.isConnected())
			{
				//System.out.println("Waiting for message from client");
				byte[] data = new byte[byteLimit];
				
				//System.out.println("Before");
				int bytesRead = reader.read(data);
				
				System.out.println("Bytes read: " + bytesRead);
				//System.out.println("After");
				if(bytesRead != -1)
				{
					data = Arrays.copyOfRange(data, 0 , bytesRead);
					handleMessage(data);
				}
				
			}
		}
		public void handleMessage(byte[] message) throws IOException
		{
			int begin = Utility.indexOf(message, (byte)'(');
			byte[] str = Arrays.copyOfRange(message, 0 , begin);
			int req = Utility.parseInt(str);
			
			List<byte[]> parameterList;
			String sendingCmd = "";
			String aux = "";
			switch(req) 
			{
			//request chunkId
			case 0:
				parameterList = extractParameters(message);
				System.out.println("Requesting upload id");
				sendingCmd = InfoToServer.MediaUploadId.toString() + 
						'(' + String.valueOf(handler.makeNewSlot(Utility.parseInt(parameterList.get(0)))) + ')';
				
				System.out.println(sendingCmd);
				writer.write(sendingCmd);
				writer.flush();
				break;
			//add new chunk
			case 1:
				parameterList = extractMediaParameters(message);
				System.out.println("Requesting to add chunk");
				handler.addChunk(parameterList.get(1) , Utility.parseInt(parameterList.get(0)));
				if(handler.mediaReady())
				{
					aux = uploader.upload(handler.readyMedia());
					aux = InfoToServer.UploadedFileName.toString() + '(' + String.valueOf(handler.readyMedia().id) + ',' + aux + ')';
					writer.write(aux);
					writer.flush();
				}
				break;
			default:
				System.out.println("Unknown command");
				break;
			}
			
		}
		private List<byte[]> extractParameters(byte[] str)
		{
			int paramBeg = Utility.indexOf(str, (byte)'(');
			int paramEnd = Utility.indexOf(str , (byte)')');

			if(paramEnd == -1 || paramBeg == -1)
			    System.out.println( " ! Error: Did not provide parantheses for parameters");

			List<byte[]> parameterList = new ArrayList<byte[]>();
			
			int i = paramBeg + 1;	
			int next = Utility.indexOf(str, (byte)',');
			while(next != -1)
			{
				parameterList.add(Arrays.copyOfRange(str, i, next));
				i = next;
				next = Utility.indexOf(str, (byte)',');
			}
			
			parameterList.add(Arrays.copyOfRange(str, i, paramEnd));

			return parameterList;
		}
		private List<byte[]> extractMediaParameters(byte[] str)
		{
		    int paramBeg = Utility.indexOf(str, (byte)'(');
		    int sepPos = Utility.indexOf(str , (byte)',');
		    List<byte[]> parameterList = new ArrayList<byte[]>();
		    
		    parameterList.add(Arrays.copyOfRange(str, paramBeg + 1, sepPos));
		    parameterList.add(Arrays.copyOfRange(str, sepPos + 1 , str.length - 2));

		    return parameterList;
		}
	}
}