/*
 * Class name	:	ConfigParameters.java
 * Description	:	This class defines the configuration parameters for the protocol
 * Institution	:	University of Florida
 */
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigParameters
{
	static int NumberOfPreferredNeighbors;
	static int UnchokingInterval;
	static int OptimisticUnchokingInterval;
	static String FileName;
	long FileSize;
	static int PieceSize;
	public static int numberOfPieces; 
	public static ConcurrentHashMap<Integer,List<Integer>> InterestedPeerPiece = new ConcurrentHashMap<Integer,List<Integer>>();
	
	ConfigParameters()
	{
		NumberOfPreferredNeighbors = 0;
		UnchokingInterval = 0;
		OptimisticUnchokingInterval = 0;
		FileSize = 0;
		PieceSize = 0;
		numberOfPieces=0;
	}
	
	/*
	 * Function name : getFileSize()
	 * Parameters    : none
	 * Description   : The getter function for file size 
	 * Return type   : long
	 */
	public long getFileSize() {
		return FileSize;
	}

	/*
	 * Function name : setNumberOfPieces()
	 * Parameters    : none
	 * Description   : The setter function for number of file pieces
	 * Return type   : long
	 */
	public void setNumberOfPieces(long fileSize) {
		numberOfPieces = (int) Math.ceil(((double)FileSize / (double)PieceSize));
	} //sk

	/*
	 * Function name : getNumberOfPieces()
	 * Parameters    : none
	 * Description   : The getter function for file size
	 * Return type   : int
	 */
	public int getNumberOfPieces() {
		return numberOfPieces;
	}

	/*
	 * Function name : setFileSize()
	 * Parameters    : none
	 * Description   : The setter function for file size 
	 * Return type   : long
	 */
	public void setFileSize(long fileSize) {
		FileSize = fileSize;
	}
	
	/*
	 * Function name : setNumberOfPreferredNeighbors()
	 * Parameters    : none
	 * Description   : The setter function for preferred neighbors. 
	 * Return type   : long
	 */
	public void setNumberOfPreferredNeighbors(int numberOfPreferredNeighbors) {
		NumberOfPreferredNeighbors = numberOfPreferredNeighbors;
	}

	/*
	 * Function name : setUnchokingInterval()
	 * Parameters    : none
	 * Description   : The setter function for unchoking interval. 
	 * Return type   : long
	 */
	public void setUnchokingInterval(int unchokingInterval) {
		UnchokingInterval = unchokingInterval;
	}

	/*
	 * Function name : setOptimisticUnchokingInterval()
	 * Parameters    : none
	 * Description   : The setter function for optimistic unchoking interval. 
	 * Return type   : long
	 */
	public void setOptimisticUnchokingInterval(int optimisticUnchokingInterval) {
		OptimisticUnchokingInterval = optimisticUnchokingInterval;
	}


	/*
	 * Function name : setFileName()
	 * Parameters    : none
	 * Description   : The setter function for file name. 
	 * Return type   : long
	 */
	public void setFileName(String fileName) {
		FileName = fileName;
	}

	/*
	 * Function name : setPieceSize()
	 * Parameters    : none
	 * Description   : The setter function for piece size. 
	 * Return type   : long
	 */
	public void setPieceSize(int pieceSize) {
		PieceSize = pieceSize;
	}

	/*
	 * Function name : getNumberOfPreferredNeighbors()
	 * Parameters    : none
	 * Description   : The getter function for preferred neighbors. 
	 * Return type   : long
	 */
	public static int getNumberOfPreferredNeighbors() {
		return NumberOfPreferredNeighbors;
	}

	/*
	 * Function name : getUnchokingInterval()
	 * Parameters    : none
	 * Description   : The getter function for unchoking interval. 
	 * Return type   : long
	 */
	public static int getUnchokingInterval() {
		return UnchokingInterval;
	}

	/*
	 * Function name : getOptimisticUnchokingInterval()
	 * Parameters    : none
	 * Description   : The getter function for Optimistic Unchoking Interval. 
	 * Return type   : int
	 */
	public int getOptimisticUnchokingInterval() {
		return OptimisticUnchokingInterval;
	}

	/*
	 * Function name : getFileName()
	 * Parameters    : none
	 * Description   : The getter function for file name. 
	 * Return type   : int
	 */
	public String getFileName() {
		return FileName;
	}

	/*
	 * Function name : getFileName()
	 * Parameters    : none
	 * Description   : The getter function for file name. 
	 * Return type   : int
	 */
	public int getPieceSize() {
		return PieceSize;
	}
	
	/*
	 * Function name : displayParameters()
	 * Parameters    : none
	 * Description   : The function is responsible to display the common configurations for the protocol. 
	 * Return type   : void
	 */
	public void displayParameters()
	{
		System.out.println("The Number Of Preferred Neighbors is : " + NumberOfPreferredNeighbors);
		System.out.println("The Unchoking Interval is : " + UnchokingInterval);
		System.out.println("The Optimistic Unchoking Interval is : " + OptimisticUnchokingInterval);
		System.out.println("The file name is : " + FileName);
		System.out.println("The file size is : " + FileSize);
		System.out.println("The piece size is : " + PieceSize);
	}
}
