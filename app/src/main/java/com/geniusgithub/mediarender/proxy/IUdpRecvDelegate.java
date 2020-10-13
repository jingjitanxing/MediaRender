package com.geniusgithub.mediarender.proxy;

public interface IUdpRecvDelegate {
    public abstract void processUdpRecvData(byte data[], long msgId);
    public abstract void collectLog();
}
