package org.mrdlib.partnerContentManager.general;
    
public class QuotaReachedException extends Exception {
    private int waitTime;
    public QuotaReachedException(int waitTime) {
	this.waitTime = waitTime;
    }
    public int getWaitTime() {
	return this.waitTime;
    }
}
