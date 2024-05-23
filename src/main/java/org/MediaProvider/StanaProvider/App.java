package org.MediaProvider.StanaProvider;

import java.io.*;
import java.net.*;


public class App 
{
    public static void main( String[] args )
    {
    	//System.out.print(System.getenv("AZURE_STORAGE_CONNECTION_STRING"));
    	
    	//String connectStr = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
    	//BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectStr).buildClient();
    	//String filePath = "C:\\Users\\Const\\Documents\\Lightshot";
    	//String fileName = "hehe.png";
    	
    	//BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient("images");
    	
    	//BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
    	//blobClient.uploadFromFile(filePath + "\\" + fileName);
    	
         
        //System.out.println( "Hello World!" );
    	Server server = new Server();
    	server.start();
    }
}
