/*
 * Class name	:	LoggingUtil.java
 * Description	:	This class is responsible for the logging of messages.
 * Institution	:	University of Florida
 */

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingUtil 
{
	static Logger LOGGER =  
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	/*
	 * Function name : LoggingUtil()
	 * Parameters    : none
	 * Description   : Constructor 
	 * Return type   : void
	 */
	public static void initializeLogging(int peerID)
	{
		boolean append = true;
        FileHandler handler;
        ConsoleHandler handler2;
        String currentDir = System.getProperty("user.dir");
        String path = currentDir+File.separator+ "log_peer_"+ peerID + ".log";
        
        
		try {
			LOGGER.setUseParentHandlers(false);
			
			handler = new FileHandler(path, append);
			LOGGER.addHandler(handler);
			handler.setFormatter(new MyFormatter());
			
			handler2 = new ConsoleHandler();
			LOGGER.addHandler(handler2);
			handler2.setFormatter(new MyFormatter());
			
		} 
		catch (SecurityException e) {
			System.out.println("Exception in initializeLogging : SecurityException");
		} 
		catch (IOException e) 
		{
			System.out.println("Exception in initializeLogging : IOException");
		}
	}
	
	/*
	 * Function name : logMessage()
	 * Parameters    : none
	 * Description   : Function to log the message.
	 * Return type   : void
	 */
	public void log(String message) 
    { 
		LOGGER.log(Level.INFO, message);   
    }

	/**
	 * @return the lOGGER
	 */
	public static Logger getLOGGER() {
		return LOGGER;
	}

	/**
	 * @param lOGGER the lOGGER to set
	 */
	public static void setLOGGER(Logger lOGGER) {
		LOGGER = lOGGER;
	} 
}
