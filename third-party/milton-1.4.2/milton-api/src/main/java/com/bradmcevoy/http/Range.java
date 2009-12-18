package com.bradmcevoy.http;
 


public class Range {
    long start;
    long finish;
    
    public Range()
	{
	}

    public Range(long start, long finish) {
        this.start = start;
        this.finish = finish;
    }

    public long getStart() {
        return start;
    }

    public long getFinish() {
        return finish;
    }        
}
