/*
 * Class name	:	Server.java
 * Description	:	This class defines the server class of the protocol.
 * Institution	:	University of Florida
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class Server extends Thread
{
	public ServerSocket serverSocket;
	public PeerParameters peerPara = new PeerParameters();
	private volatile boolean exit = false;
	
	Server(PeerParameters peerParameters)
	{
		peerPara = peerParameters;
	}
	
	/*
   	 * Function name : initialise()
   	 * Parameters    : Port number - the port number for the socket to be created.
   	 * Description   : The function initializes the socket. 
   	 * return type   : void 
   	 */
	public void initialise(int portNumber)
	{
		
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
   	 * Function name : run()
   	 * Parameters    : none
   	 * Description   : The function initializes the socket and soon after that begins the listening on the socket. 
   	 * return type   : void 
   	 */
	public void run()
	{
		initialise(peerPara.portNumber);
		while(!exit)
		{
			startAccept(peerPara.peerID);
		}
	}
	
	/*
   	 * Function name : startAccept()
   	 * Parameters    : Peer ID - The peerID of the peer that is now the server
   	 * Description   : The function is responsible for accepting the connection of the server.
   	 * 					The function on accepting a connection will create a thread to handle all the 
   	 * 					the incoming messages of the connected peer
   	 * return type   : void 
   	 */
	public void startAccept(int peerID)
	{
		Socket socket = null;
		try
		{
			socket = serverSocket.accept();
			ObjectOutputStream obOutStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream obInStream = new ObjectInputStream(socket.getInputStream()); 
           
            PeerHandler threadClient = new PeerHandler(socket,obInStream,obOutStream);
            CommonConfigurations.myPeerHandlerThreads.add(threadClient);

      		threadClient.start();
			
		}
		catch(SocketException ex)
		{
		}
		catch(Exception ex)
		{
			try 
			{
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			//ex.printStackTrace();
		}
	}
	
	public void stopThread()
	{
        exit = true;
    }
}
