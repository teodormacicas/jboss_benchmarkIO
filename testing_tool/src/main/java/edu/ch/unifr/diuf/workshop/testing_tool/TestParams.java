package edu.ch.unifr.diuf.workshop.testing_tool;


public class TestParams {

    private String serverType;
    private int testNum;
    private String[] serverModes;
    private String[] requestNum;
    private String[] delays;
    private int threadsNum;

    public TestParams(String serverType, String[] serverModes, int testNum, String[] requestNum, String[] delays, int threadsNum) {
        this.serverType = serverType;
        this.testNum = testNum;
        this.serverModes = serverModes;
        this.requestNum = requestNum;
        this.delays = delays;
        this.threadsNum = threadsNum;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public int getTestNum() {
        return testNum;
    }

    public void setTestNum(int testNum) {
        this.testNum = testNum;
    }

    public String[] getServerModes() {
        return serverModes;
    }

    public void setServerModes(String[] serverModes) {
        this.serverModes = serverModes;
    }

    public String[] getRequestNum() {
        return requestNum;
    }

    public void setRequestNum(String[] requestNum) {
        this.requestNum = requestNum;
    }

    public String[] getDelays() {
        return delays;
    }

    public void setDelays(String[] delays) {
        this.delays = delays;
    }

    public int getThreadsNum() {
        return threadsNum;
    }

    public void setThreadsNum(int threadsNum) {
        this.threadsNum = threadsNum;
    }
}
