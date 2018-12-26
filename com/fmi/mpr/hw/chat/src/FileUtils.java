import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
	
	public static byte[] FileToByteArray(String url) throws IOException {
		
		File file = new File(url);
		 
		FileInputStream fis = new FileInputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		for (int readNum; (readNum = fis.read(buf)) != -1;) {
		    bos.write(buf, 0, readNum); 
		}

		byte[] bytes = bos.toByteArray();
		fis.close();

		return bytes;
	}
	
	public static void ByteArrayToFile(String directory, byte[] bytes, String name) throws IOException {
		
		File dir = new File(directory);
	    	if (!dir.exists()) {
	            dir.mkdirs();
	  	}
		
		File resultFile = new File(directory + File.separator + name);
		FileOutputStream fos = new FileOutputStream(resultFile);
		fos.write(bytes);
		fos.flush();
		fos.close();
	}
}
