
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author itsgnegrao
 */


class ShellUtils{

	public static String getStringFromShell(String prompt){
		try{
			System.out.print(prompt);
			return new BufferedReader(new InputStreamReader(System.in)).readLine();		
		}
		catch (IOException e){e.printStackTrace();}
		return null ;
	}

	public static int getIntFromShell(String prompt)
	{
		String line = "" ;
		int num = 0 ;
		while(line.equals(""))
		{
			line = getStringFromShell(prompt);
			try
			{
				num = Integer.parseInt(line);
			}
			catch(NumberFormatException e)
			{
				System.out.println("Error: Invalid number");
				line = "" ;
			}
		}
		return num ;
	}
}