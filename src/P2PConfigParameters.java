/*
 * Class name	:	P2PPeer.java
 * Description	:	This class defines the configuration parameters for the peer in the protocol
 * Institution	:	University of Florida
 */
public class P2PConfigParameters 
{
	public ConfigParameters configParameters;
	
	P2PConfigParameters()
	{
		configParameters = new ConfigParameters();
	}

	/**
	 * @return the configParameters
	 */
	public ConfigParameters getConfigParameters() {
		return configParameters;
	}

	/**
	 * @param configParameters the configParameters to set
	 */
	public void setConfigParameters(ConfigParameters configParameters) {
		this.configParameters = configParameters;
	}
}
