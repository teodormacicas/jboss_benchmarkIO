package edu.ch.unifr.diuf.workshop.testing_tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;

/**
 *
 * @author Teodor Macicas
 */
public class Client extends Machine 
{
    private String serverIpAddress;
    private int serverPort;
    private int noThreads; 
    private int delay; //the delay between requests 
    private int noReq; //total number of requests
    private double restartCond; //percentage of dead clients needed to restart a test
    private int timeoutSec; // after this vlaue, the client is considered dead
    private int lastLogAccessSec;
    private Integer clientId;
    
    // set here the name of the tests to be run 
    private List<String> tests;
    
    public Client(String ipAddress, int port, String sshUsername, 
            String sshPassword, Integer id) 
            throws WrongIpAddressException, WrongPortNumberException {
        super();
        this.setIpAddress(ipAddress);
        this.setPort(port);
        this.setSSHUsername(sshUsername);
        this.setSSHPassword(sshPassword);
        this.tests = new ArrayList<String>();
        this.serverIpAddress = "0.0.0.0";
        this.serverPort = 0;
        // save here some default values
        this.noThreads = 1;
        this.delay = 1000;
        this.noReq = 1;
        this.clientId = id;
    }
    
    /**
     * 
     * @param ipAddress
     * @throws WrongIpAddressException 
     */
    public void setServerIpAddress(String ipAddress) throws WrongIpAddressException { 
        if( Utils.validateIpAddress(ipAddress) )
               this.serverIpAddress = ipAddress;
        else
            throw new WrongIpAddressException(ipAddress + " cannot be set due to"
                    + " validation errors");
    }
    
    /**
     * 
     * @return 
     */
    public String getServerIpAddress() {
        return this.serverIpAddress;
    }
    
    /**
     * 
     * @param port 
     */
    public void setServerPort(Integer port) throws WrongPortNumberException { 
         if( Utils.validateRemotePort(port) ) 
            this.serverPort = port;
        else
            throw new WrongPortNumberException(port + " cannot be set due to "
                    + "validation errors.");
    }
    
    /**
     * 
     * @return 
     */
    public int getServerPort() { 
        return this.serverPort;
    }
    
    /**
     * 
     * @param noThreads 
     */
    public void setNoThreads(int noThreads) {
        this.noThreads = noThreads;
    }
    
    /**
     * 
     * @return 
     */
    public int getNoThreads() { 
        return this.noThreads;
    }
    
    /**
     * 
     * @param delay 
     */
    public void setDelay(int delay) { 
        this.delay = delay;
    }
    
    /**
     * 
     * @return 
     */
    public int getDelay() { 
        return this.delay;
    }
    
    /**
     * 
     * @param noReq 
     */
    public void setNoReq(int noReq) { 
        this.noReq = noReq;
    }
    
    /**
     * 
     * @return 
     */
    public int getNoReq() { 
        return this.noReq;
    }
    
    /**
     * 
     * @param sec 
     */
    public void setTimeoutSec(int sec) {
        this.timeoutSec = sec; 
    }
    
    /**
     * 
     * @return 
     */
    public int getTimeoutSec() { 
        return this.timeoutSec;
    }
    
    /**
     * 
     * @param relativeSec 
     */
    public void setLastLogModification(int relativeSec) { 
        this.lastLogAccessSec = relativeSec;
    }
    
    /**
     * 
     * @return 
     */
    public int getLastLogModification() { 
        return this.lastLogAccessSec;
    }
    
    /**
     * 
     * @param test 
     */
    public void addNewTest(String test) { 
        this.tests.add(test);
    }
    
    /**
     * 
     * @return 
     */
    public List<String> getTests() { 
        return this.tests;
    }
    
    /**
     * 
     * @return 
     */
    public Integer getId() { 
        return this.clientId;
    }
    
    /**
     * 
     * @param sshClient
     * @throws TransportException
     * @throws IOException 
     */
    public int killClient(SSHClient ssh_client) throws TransportException, IOException { 
        if( getPID() != 0 )
            return SSHCommands.killProgram(this, ssh_client);
        return 1;
    }
    
    /**
     * 
     * @param serverIpAddress
     * @param serverPort
     * @throws WrongIpAddressException
     * @throws WrongPortNumberException 
     */
    public void setServerInfo(String serverIpAddress, Integer serverPort) 
            throws WrongIpAddressException, WrongPortNumberException { 
        this.setServerIpAddress(serverIpAddress);
        this.setServerPort(serverPort);
    }
    
    /**
     * 
     * @param threshold 
     */
    public void setRestartConditionPropThreadsDead(double threshold) { 
        this.restartCond = threshold;
    }
    
    /**
     * 
     * @return 
     */
    public double getRestartConditionPropThreadsDead() { 
        return this.restartCond;
    }
    
    /**
     * 
     * @param noThreads
     * @param delay
     * @param noReq 
     */
    public void setRunningParams(Integer noThreads, Integer delay, Integer noReq) {
        this.setNoThreads(noThreads);
        this.setDelay(delay);
        this.setNoReq(noReq);
    }
    
    /**
     * 
     * @param file
     * @param sshclient
     */
    public void uploadProgram(String file, SSHClient ssh_client) 
            throws FileNotFoundException, IOException {
        this.uploadFile(file, Utils.getClientProgramRemoteFilename(this), ssh_client);
    }              
    
    /**
     * 
     * @return 
     */
    public String testsToString() { 
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<tests.size(); ++i) {
            sb.append(tests.get(i)).append(" ");
        }
        return sb.toString();
    }
    
    /**
     * Actually delete some empty files that might have been previously used.
     * 
     * @param sshClient
     */
    public void deletePreviousRemoteMessages(SSHClient ssh_client) 
            throws TransportException, IOException { 
        SSHCommands.deleteRemoteFile(this, "*"+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_THREADS_SYNCH, 
                ssh_client);
        SSHCommands.deleteRemoteFile(this, "*"+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_START_SENDING_REQUESTS,
                ssh_client);
        SSHCommands.deleteRemoteFile(this, "*"+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_FINISHED,
                ssh_client);
    }
    
    /**
     * 
     * @param sshClient
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public int runClientRemotely(Server server, SSHClient ssh_client) 
            throws TransportException, IOException { 
        int r = SSHCommands.startClientProgram(this, server, ssh_client);
        if( r != 0 ) { 
            System.out.println("[ERROR] Client could not be properly started! "
                    + "Exit code: " + r);
            return -1;
        }
        this.setPID(SSHCommands.getProgramPID(this, ssh_client));
        return 0;
    }
    
    /**
     * 
     * @param sshClient
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public boolean isProgressing(SSHClient ssh_client) throws TransportException, IOException { 
        lastLogAccessSec = SSHCommands.getTimeSinceLastLogModification(this, 
                Utils.getClientLogRemoteFilename(this), ssh_client);
        if( lastLogAccessSec > this.timeoutSec ) { 
            return false;
        }
        return true;
    }
    
    /**
     * 
     * @returns a more comprehensible status message 
     */
    public String getStatusMessage() { 
        StringBuilder sb = new StringBuilder();
        sb.append("Client ").append(this.getIpAddress());
        sb.append(":").append(this.getPort()).append("\n\t\tCONNECTION: ");
        if( status_connection == Status.OK )
            sb.append("up and running.");
        else if( status_connection == Status.SSH_CONN_PROBLEMS )
            sb.append("SSH connectivity problems.");
        else 
            sb.append("no connection status known for machine yet.");
        
        sb.append("\n\t\tPROGRAM STATUS: ");
        if( status_process == Status.PID_RUNNING ) 
            sb.append("running with PID " + this.getPID());
        else if ( status_process == Status.PID_NOT_RUNNING ) 
            sb.append("not running yet.");
        else
            sb.append("no info available yet.");
        
        sb.append("\n\t\tSYNCH STATUS: ");
        if( status_synch == Status.SYNCH_THREADS ) 
            sb.append("threads are synchronized.");
        else if( status_synch == Status.RUNNING_REQUESTS ) 
            sb.append("clients are synchronized; sending requests ...");
        else 
            sb.append("no info available yet.");
        
        sb.append("\n\t\tTESTS: ");
        sb.append(testsToString());
        
        sb.append("\n\t\tFAULT TOLERANCE: ");
        sb.append(this.getRestartConditionPropThreadsDead()+ " percentage of "
                + "needed dead clients to restart.");
        
        return sb.toString();
    }
}

class ClientNotProperlyInitException extends Exception 
{
    public ClientNotProperlyInitException(String string) {
        super(string);
    }
}
