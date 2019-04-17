/*
 * Class name	:	ConfigReader.java
 * Description	:	This class defines the configuration reader, which acts like a utility to read the parameters to configure the peer
 * Institution	:	University of Florida
 */


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ConfigReader 
{
	/*
	 * Function name : readCommonConfigFromFile()
	 * Parameters    : path to the file.
	 * Description   : The function is responsible to read the common configurations extracted from the file. 
	 * Return type   : ConfigParameters - the common configurations of the protocol.
	 */
	public ConfigParameters readCommonConfigFromFile(String path)
	{
		//System.out.println("readCommonConfigFromFile entered " +path);
		ConfigParameters p2pPara = new ConfigParameters();
		String rawData;
		try
		{
			BufferedReader bufferReader = new BufferedReader(new FileReader(path));
			
			while((rawData = bufferReader.readLine()) != null)
			{
				if(rawData.contains("NumberOfPreferredNeighbors"))
				{
					String[] result = rawData.split("\\s");
					int PreferredNeighbors = Integer.parseInt(result[1]);
					ConfigParameters.NumberOfPreferredNeighbors = PreferredNeighbors;	
				}
				else if(rawData.contains("OptimisticUnchokingInterval"))
				{
					String[] result = rawData.split("\\s");
					int ounChokingInterval = Integer.parseInt(result[1]);
					ConfigParameters.OptimisticUnchokingInterval = ounChokingInterval;	
				}
				else if(rawData.contains("UnchokingInterval"))
				{
					String[] result = rawData.split("\\s");
					int unChokingInterval = Integer.parseInt(result[1]);
					ConfigParameters.UnchokingInterval = unChokingInterval;
				}
				else if(rawData.contains("FileName"))
				{
					String[] result = rawData.split("\\s");
					ConfigParameters.FileName = result[1];	
				}
				else if(rawData.contains("FileSize"))
				{
					String[] result = rawData.split("\\s");
					long number = Long.parseLong(result[1]);
					p2pPara.FileSize = number;
				}
				else
				{	
					String[] result = rawData.split("\\s");
					int pieceSize = Integer.parseInt(result[1]);
					ConfigParameters.PieceSize = pieceSize;	
				}
				rawData = null;
			}
			
			bufferReader.close();
		}
		catch(Exception ex)
		{
			System.out.println("There was an exception in readCommonConfigFromFile");
		}
		
		return p2pPara;
	}
	
	/*
	 * Function name : readPeerConfiguration()
	 * Parameters    : path to the file.
	 * Description   : The function is responsible to read the peer configurations extracted from the file. 
	 * Return type   : an array list consisting of peer configurations.
	 */
	public ArrayList<P2PPeer> readPeerConfiguration(String path)
	{
		//System.out.println("readPeerConfiguration entered");
		String rawData;
		ArrayList<P2PPeer> p2pParaArray = new ArrayList<P2PPeer>();
		try
		{
			BufferedReader bufferReader = new BufferedReader(new FileReader(path));
			
			while((rawData = bufferReader.readLine()) != null)
			{
				P2PPeer p2pParameters = new P2PPeer();
				String[] result = rawData.split("\\s");
				p2pParameters.peerParameters.peerID = Integer.parseInt(result[0]);
				
				p2pParameters.peerParameters.peerName = result[1];
				
			    p2pParameters.peerParameters.portNumber = Integer.parseInt(result[2]);
			    
			    p2pParameters.peerParameters.completeFile = Integer.parseInt(result[3]);
			    
			    p2pParaArray.add(p2pParameters);
			}
			
			bufferReader.close();
		}
		catch(Exception ex)
		{
			System.out.println("There was an exception in readPeerConfiguration()");
			ex.printStackTrace();
		}
		
		return p2pParaArray;	
	}
}
