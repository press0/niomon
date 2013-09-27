package niomon;
import java.util.Date;
import java.util.Random;

/**
 * NIO session state, calculate unique sessionID, maintain statistics
 */
public class NIOSession {
	private String sessionID;
	private Date beginTime;
	private Date endTime;
	private int count = 0;
	private float avgRTT = 0;
	private float throughput = 0;

	public NIOSession() {
		calcSessionID();
		setStartTime();
	}
	private void calcSessionID() {
		Random generator = new Random();
		int i = generator.nextInt() ;
		if ( i < 0 ) i = i * -1;
		String s = new Integer( i ).toString().substring(0,5);
		this.sessionID = s ;
	}
	public String getSessionID() {
		return sessionID;
	}
	public Date getStartTime() {
		return beginTime;
	}
	private void setStartTime() {
		this.beginTime = new Date();
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime() {
		this.endTime = new Date();
	}
	public int getCount() {
		return count;
	}
	public void setCount( int _count ) {
		this.count = _count ;
	}
	public float getAvgRTT() {
		return avgRTT;
	}
	public void setAvgRTT(float _avgRTT) {
		this.avgRTT = _avgRTT;
	}
	public float getThroughput() {
		return throughput;
	}
	public void setThroughput(float _throughput) {
		this.throughput = _throughput;
	}

}
