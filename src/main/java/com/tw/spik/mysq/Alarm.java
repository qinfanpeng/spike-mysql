package com.tw.spik.mysq;

public class Alarm {
    private String name;
    private String node;
    private long firstOccur;
    private long windowNo;

    public Alarm(String name, String node, long firstOccur) {
        this.name = name;
        this.node = node;
        this.firstOccur = firstOccur;
    }

    public String name() {
        return name;
    }

    public long windowNo() {
        return windowNo;
    }

    public void setWindowNo(long windowNo) {
        this.windowNo = windowNo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String node() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public long firstOccur() {
        return firstOccur;
    }

    public void setFirstOccur(long firstOccur) {
        this.firstOccur = firstOccur;
    }

    public void setWindowNo() {
        this.windowNo = this.firstOccur / 10;
    }
}
