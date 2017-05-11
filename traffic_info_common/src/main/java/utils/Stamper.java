package utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author david.mail
 * following http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
 */
public class Stamper {
	
	static final Logger log = Logger.getLogger(Stamper.class.getName());
    
   public static String md5( File file) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
       InputStream fis =  new FileInputStream( file);

       byte[] buffer = new byte[1024];
       MessageDigest complete = MessageDigest.getInstance( "MD5");
       int numRead;

       do {
           numRead = fis.read(buffer);
           if (numRead > 0) {
               complete.update(buffer, 0, numRead);
           }
       } while (numRead != -1);

       fis.close();
       return bytesToString(complete.digest());
   }    
   
   public static String md5( String str) throws NoSuchAlgorithmException {
       return Stamper.md5( str.getBytes());
   }
   
   public static String md5( byte[] bytes) throws NoSuchAlgorithmException {
       MessageDigest complete = MessageDigest.getInstance( "MD5");
       complete.update( bytes, 0, bytes.length);
       return bytesToString( complete.digest());
   }
   
   public static String md5Utf8( String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
       byte[] bytesOfMessage = str.getBytes("UTF-8");
       MessageDigest complete = MessageDigest.getInstance( "MD5");
       byte[] theDigest = complete.digest(bytesOfMessage);
       return bytesToString( theDigest);
   }
   
   
   private static String bytesToString( byte[] b){
       String result = "";

       for (int i=0; i < b.length; i++) {
           result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
       }
       return result;
   }
   
   private static String bytesToBase64( byte[] b){
	   
	   return Base64.encodeBase64String(b);
   
   }
   
   
   public static String sha1( String str) throws NoSuchAlgorithmException {
	   String key = Stamper.sha1( str.getBytes());
	   log.debug("build SHA1 cache key from " + str + " -> " + key);
       return (key);
   }
   
   public static String sha1( byte[] bytes) throws NoSuchAlgorithmException {
       MessageDigest complete = MessageDigest.getInstance( "SHA-1");
       complete.update( bytes, 0, bytes.length);
       return bytesToBase64( complete.digest());
   }
   
   public static void main(String[] args) throws Exception {
       try{
            System.out.println( Stamper.md5(
//                "http://www.enterogermina.it/?utm_source=Circuito.Donna&utm_medium=Display&utm_campaign=Enterogermina_dic2012_Circuito.Donna"
                    new String("http://tao.etao.com/auction?keyword=Á¹Ð¬&clk1=984f5a7cf2dc84c178c134c4cb3c33c9".getBytes(), "UTF-8")
             ));
            
            // expected result: 
            // b697d433ccae06ef1f57e44562dc58a8
       } catch( Exception e){
           e.printStackTrace();
       }
   }
    
}