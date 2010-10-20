package common;

public class Timer {
	private long millis;
	
	public Timer(long millis) {
		this.millis = millis;
	}
	
	@Override
	public String toString() {
		long secs = millis/1000;
		long s = secs % 60;
		long m = (secs/60) % 60;
		long h = secs/3600 % 24;
		long d = secs/(3600 * 24);
		StringBuilder sb = new StringBuilder();
		if (d > 0)
			sb.append(d + "d ");
		if (d > 0 || h > 0)
			sb.append(h + "h ");
		if (d > 0 || m > 0 || h > 0)
			sb.append(m + "m ");
		sb.append(s + "s");
		
		return sb.toString();
	}

}
