package com.iii.facetoface.webservice;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encrypt {
	   public static String getMD5(String input) {
	        try {
	            MessageDigest md = MessageDigest.getInstance("MD5");
	            byte[] messageDigest = md.digest(input.getBytes());
	            BigInteger number = new BigInteger(1, messageDigest);
	            String hashtext = number.toString(16);
	            // Now we need to zero pad it if you actually want the full 32 chars.
	            while (hashtext.length() < 32) {
	                hashtext = "0" + hashtext;
	            }
	            return hashtext;
	        }
	        catch (NoSuchAlgorithmException e) {
	            throw new RuntimeException(e);
	        }
	   }
	   public static Boolean Compare(String strMd5,String str){
		   return strMd5.equals(getMD5(str));
	   }
//	   public static String getMD5(String input) {
//		   return input;
//	   }
//	   public static Boolean Compare(String strMd5,String str){
//		   return strMd5.equals(str);
//	   }
	    
}
