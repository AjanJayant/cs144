import java.io.*;
import java.security.*;

public class ComputeSHA {

	byte arr[];
    MessageDigest md = null;
    
    public ComputeSHA(String str)
    {
    	try
    	{
    		md = MessageDigest.getInstance(str);
    	}
    	catch(NoSuchAlgorithmException n)
    	{
    		n.getStackTrace();
    	}
    }
	
	public void parseInput(String loc) throws FileNotFoundException, IOException
	{
		try 
		{	
			FileInputStream fileStream = new  FileInputStream(loc);

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = fileStream.read(data)) != -1) 
			{
				md.update(data, 0, nRead);
			}
		}
		catch(FileNotFoundException f)
		{
			System.out.println("No such file or directory");
			throw new FileNotFoundException();
		}
	}
	
	public void printSHA()
	{
		arr = md.digest();
		System.out.println(byteArrayToHexString());
	}
	
	public String byteArrayToHexString() 
	{
		  String result = "";
		  for (int i=0; i < arr.length; i++) 
		  {
		    result +=
		          Integer.toString( ( arr[i] & 0xff ) + 0x100, 16).substring( 1 );
		  }
		  return result;
	}
	
	public static void main(String args[]) throws IOException
	{
		if (args.length != 1)
		{
			throw new IllegalArgumentException();
		}
		String fileLocation = args[0];
		ComputeSHA c = new ComputeSHA("SHA-1");
		try {
			c.parseInput(fileLocation);
		}
		catch(FileNotFoundException f)
		{
			System.out.println("File not present");
		}
		c.printSHA();
	}
}
