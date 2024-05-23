package org.MediaProvider.StanaProvider;

public class Utility {
	static public int indexOf(byte[] arr , byte val)
	{
		for(int i = 0 ; i < arr.length ; i++)
		{
			if(arr[i] == val)
				return i;
		}
		return -1;
	}
	static public int parseInt(byte[] arr)
	{
		int num = 0;
		for(byte b : arr)
		{
			if( b >= (byte)'0' && b <= (byte)'9')
			{
				num = num * 10 + (b - 48);
			}
		}
		return num;
	}
}
