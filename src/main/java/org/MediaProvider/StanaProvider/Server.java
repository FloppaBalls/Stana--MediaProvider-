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
	
	class AzureUploader{
		String connectStr = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
		BlobServiceClient blobServiceClient;
		BlobContainerClient blobContainerClient;
		int count = 0;
		long usedStorage = 0;
		long maxStorage = 5000000000L;
		
		public AzureUploader() {
			blobServiceClient = new BlobServiceClientBuilder().connectionString(connectStr).buildClient();
			blobContainerClient = blobServiceClient.getBlobContainerClient("images");
			for(BlobItem blobItem : blobContainerClient.listBlobs())
			{
				count++;
				usedStorage += blobItem.getProperties().getContentLength();
			}
		}
		public void upload(MediaData data)
		{
			BlobClient blobClient = blobContainerClient.getBlobClient(provideFileName(data.extension));
			blobClient.upload(new ByteArrayInputStream(data.blob) , data.blob.length , true);
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
				countStr += "0" + countStr;
			if(count < 100)
				countStr += "0" + countStr;
			
			fileName += countStr + extensionStr + '.' + extensionToStr(extension);
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
		AzureUploader uploader;
		
		private int byteLimit = 1204;
		
		public ConnectionHandler(Socket socket0)
		{
			client = socket0;
			try {
				writer = new PrintWriter(client.getOutputStream());
				reader = new DataInputStream(client.getInputStream());
				handler = new MediaHandler(client);
				uploader = new AzureUploader();
				
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
				int charsRead = reader.read(data);
				
				System.out.println("Bytes read: " + charsRead);
				//System.out.println("After");
				if(charsRead != -1)
				{
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
					uploader.upload(handler.readyMedia());
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