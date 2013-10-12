import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import java.io.*;
import org.junit.runners.*;

/**
 * Tests for {@link ComputeSHA}.
 *
 * @author ajan.jayant7@gmail.com (Ajan Jayant)
 */
@RunWith(Parameterized.class)
public class ComputeSHATest {
	
	private String fileName;
	private String SHA1Value;
	private ComputeSHA c;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	public ComputeSHATest(String fName, String SHA1)
	{
		this.fileName = fName;
		this.SHA1Value = SHA1;
		this.c 	= new ComputeSHA("SHA-1");
	}

	@Parameterized.Parameters
	public static Collection textFilePairs()
	{
		/*
		 * 1. sample-input.txt is instructor defined
		 * 2. sample-input2.txt is qwerty
		 * 3. sample-input3.txt is an empty file
		 * 4. sample-input4.txt is a randomly generated text file
		 * 5. MARBLES.GIF is a gif of white marbles
		 * 6. GMARBLES.GIF is a gif of black marbles
		 * 7. WFPCO5.GIF is a gif of the cosmos
		 * 8. Example.jpg is an example jpg
		 */
		return Arrays.asList(new Object[][] 
		{
				{"sample-input.txt", "17a23c746fed66a6f285c665422deafcf51aca40\n"},
				{"sample-input2.txt", "7284dbf86b6fa17d0411e1b1547b950f322650c6\n"},
				{"sample-input3.txt", "da39a3ee5e6b4b0d3255bfef95601890afd80709\n"},
				{"sample-input4.txt", "22003ac4ec8394e06548fd6319dab9b11b08d79d\n"},
				{"MARBLES.GIF", "de64c4d51918c0bbcdf4cabb7425ebd15d93441e\n"},
				{"GMARBLES.GIF", "05a64bd29b50c0e3036c134c52c2a0c9d147ddda\n"},
				{"WFPC05.GIF", "888c80d2ce52f3126ceaa539f1e99b4a3cc6c10b\n"},
				{"Example.jpg", "628a43d996686e654934e27c99e51afe432fc164\n"}
		});
	}
	
	@Test
	public void testComputeSHA() throws IOException
	{
		/*
		 * To test standard output, System.out is redirected to
		 * another output stream, which is converted into a string 
		 * and then rest
		 */
		PrintStream backupStream = System.out;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream stringStream = new PrintStream(out);
		System.setOut(stringStream);
		c.parseInput(fileName);
		c.printSHA();
		String ans = out.toString();
		System.setOut(backupStream);
		assertEquals("Values should match", SHA1Value, ans);
	}
	
	// Fails if file not found excpetion not generated
	@Test
	public void testForNoFile() throws FileNotFoundException, IOException
	{
		exception.expect(FileNotFoundException.class);
		c.parseInput("");
	}
	
	@Test
	public void testForNoAlgorithm()
	{
		ComputeSHA c = new ComputeSHA("!");
	}
	
	// Thrown if no arguements present
	@Test
	public void testIfMainHandlesMalformedArguements() throws IOException
	{
	    exception.expect(IllegalArgumentException.class);
	    ComputeSHA.main(new String[0]);
	}
}
