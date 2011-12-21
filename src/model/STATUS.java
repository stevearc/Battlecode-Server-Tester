/**
 * 
 */
package model;

public enum STATUS {QUEUED, RUNNING, COMPLETE, ERROR, CANCELED;

	STATUS () {
		
	}

	public String toString() {
		switch (this) {
		case CANCELED:
			return "Canceled";
		case COMPLETE:
			return "Complete";
		case ERROR:
			return "Error";
		case QUEUED:
			return "Queued";
		case RUNNING:
			return "Running";
		default:
			return "UNKNOWN";
		}
	}
}