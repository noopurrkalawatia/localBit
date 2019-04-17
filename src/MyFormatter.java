/*
 * Class name	:	MyFormatter.java
 * Description	:	This class is responsible for formatting the log messages.
 * Institution	:	University of Florida
 */

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MyFormatter extends Formatter {
// [%2$-3s]

	private static final String format = "[%1$tF %1$tT] %3$s %n";

	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public synchronized String format(LogRecord lr) 
	{
		return String.format(format,
				new Date(lr.getMillis()),
				lr.getLevel().getLocalizedName(),
				lr.getMessage()
				);
	}
}


