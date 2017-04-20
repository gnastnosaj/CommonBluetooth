package io.jasontsang.commonbluetooth.app;

import android.annotation.SuppressLint;

/**
 * 
 * @author youxz
 *
 */
public class HexUtils {

	/**
	 * int转bytes
	 * @param data int数据
	 * @param size 转换后字节数
	 * @return
	 */
	public static byte[] int2bytes(int data, int size) {

		String tmp = int2HexStr(data, size);

		return hexStringToBytes(tmp);
	}

	public static String int2HexStr(int b, int size) {
		String stmp = "";
		stmp = Integer.toHexString(b);

		while (stmp.length() < size*2) {
			stmp = "0" + stmp;
		}
		return stmp;
	}

	@SuppressLint("DefaultLocale") public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}
	
	public static int byteToInt(byte[] buf){
		String tmp = bytes2HexString(buf);
		return Integer.parseInt(tmp, 16);
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	public static  String bytes2HexString( byte[] b) {
		String a = "";
		  for (int i = 0; i < b.length; i++) { 
		    String hex = Integer.toHexString(b[i] & 0xFF); 
		    if (hex.length() == 1) { 
		      hex = '0' + hex; 
		    }
		   
		    a+=hex;
		  } 
		  
		 return a;
		}
	
	public static void main(String s[]){
		byte[] data = int2bytes(65535,2);
		String sh = bytes2HexString(data);
		System.out.println(Integer.parseInt(sh, 16));
		
	}

}
