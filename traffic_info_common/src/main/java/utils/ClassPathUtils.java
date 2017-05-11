package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;

public class ClassPathUtils {
	
	
	
	/**
	 * gets an inputstream to a resource in classpath
	 * @param relativePathInClassPath
	 * @return a stream that should be closed after being used
	 */
	public static InputStream getResourceInputStream(String relativePathInClassPath)
	{
		return ClassLoader.getSystemResourceAsStream(relativePathInClassPath);
	}
	
	/**
	 * copies a file from classpath to the file system
	 * @param relativePathInClassPath
	 * @param targetFile
	 * @return
	 * @throws IOException
	 */
	public static boolean copyResourceFromClassPathToFileSystem(String relativePathInClassPath, File targetFile) throws IOException 
	{
		InputStream is =  getResourceInputStream( relativePathInClassPath);
		if(is == null)
		{
			return false;
		}
		try
		{
			if(targetFile.exists())
			{
				targetFile.delete();
			}
			Files.copy(is, targetFile.toPath());
			return true;
		}
		finally
		{
			try {
				is.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	/**
	 * needed when the URI is in a jar file
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	public static Path UriToPath(URI uri) throws IOException
	{
		try
		{
			
			return  Paths.get(uri);
		}
		catch(FileSystemNotFoundException e)
		{
			FileSystems.newFileSystem(uri, Collections.<String, Object> emptyMap());
			return Paths.get(uri);
			
		}
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String readFileFromClasspath(final String fileName) throws IOException, URISyntaxException {
		
		URI uri = ClassPathUtils.class.getClassLoader()
        .getResource(fileName)
        .toURI();
		
	
		
	    return new String(Files.readAllBytes(
	    		UriToPath(uri)));
	}
	
	public static void main(String[] args) throws MalformedURLException
	{
		
		String str = "http://click.siga.uol.com.br/LINKS?id=02f088dbabf648e8a9460981b042895a&amp;msg=IKeZyb8WTYww6qtdsptXtoGy9%2BbMJo9DTyUrbwlR%2B76qN1SSRKLKspU5TygafwKGkWJQy9L2kGX6TIqhSpbEuZQllI8yxjFpblOv7bbSx2rxmr3ZQGzsqib3Ndz66WbBvn0A9fa%2F4gAEyzL2XLa%2BLlPa0%2Fqmi9RsG%2FilXbScHEzPzd5YFuEBWqGtgNy8H2w%2FhyzaSUbOGfKq9Rxamy0xfqF8wqhnTxOn4pZ4GRT%2Bev2%2F7b4btscYttabtPLIKDvUxqNICBfitG9WJMKoGQ5xCbosxrb5cAubRdgCISIcFwAUO8qMQBBdONpR3TYQCWZV3fFEwR4gXqCse3Lab6jcip5t68uA8T2L9YA20yRY0a8CWs87OWnhWO1w5AAEnfljTZHfU6cvurK43PfKQYQlPXAiPjzHdjjdoz2b6RjQ5JVsF8GGj0x9RKCqvmQK5LaUAuRXLW8UJNLYiyxMq%2FWQZu3u%2Bw39LjAbesOvCRN0%2Fyuzg5pqV8l3LppxDOxicla6&amp;urlReferer=http%3A%2F%2Fblog.desprotetor.com.br%2Fsobre%2F&amp;caf=530bb5543fdb40568aa7fcdd958d0ed8&amp;idtAd=0&amp;idtAdv=1036786&amp;idtCr=1378306&amp;idtFl=3216535&amp;urlRedir=http%3A%2F%2Fadfarm.mediaplex.com%2Fad%2Fck%2F12255-212176-27513-0";
		str= "http://www.google.com/kuku";
		System.out.println(new URL(str).getHost());
		
		
	}


}
