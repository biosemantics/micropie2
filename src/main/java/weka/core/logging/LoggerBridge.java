package weka.core.logging;

import java.util.Date;

import edu.arizona.biosemantics.micropie.log.LogLevel;

public class LoggerBridge extends Logger {

	@Override
	public String getRevision() {
		return "";
	}

	@Override
	protected void doLog(Level level, String msg, String cls, String method,
			int lineno) {
		edu.arizona.biosemantics.micropie.log.LogLevel newLevel = LogLevel.FATAL;
		switch(level) {
		case ALL:
			newLevel = LogLevel.TRACE;
			break;
		case FINE:
			newLevel = LogLevel.DEBUG;
			break;
		case FINER:
			newLevel = LogLevel.TRACE;
			break;
		case FINEST:
			newLevel = LogLevel.TRACE;
			break;
		case INFO:
			newLevel = LogLevel.INFO;
			break;
		case OFF:
			newLevel = LogLevel.FATAL;
			break;
		case SEVERE:
			newLevel = LogLevel.ERROR;
			break;
		case WARNING:
			newLevel = LogLevel.WARN;
			break;
		}
		
		log(newLevel, cls + " " + method + " " + level + ": " + msg);
	}
}
