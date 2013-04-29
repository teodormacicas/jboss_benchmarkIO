package edu.ch.unifr.diuf.workshop.testing_tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    // set here the name of the tests to be run 
    private List<String> tests;
    
    public Client(String ipAddress, int port, String sshUsername, String sshPassword) 
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
     */
    public void uploadProgram(String file) throws FileNotFoundException, IOException {
        this.uploadFile(file, Utils.getClientProgramRemoteFilename(this));
    }                //System.out.println(IOUtils.readFully(cmd.getInputStream()).toString());
    
    //TODO: 
    public void checkClientServerConnectivity() { 
        
    }
    
    /**
     * 
     * @throws ClientNotProperlyInitException
     * @throws TransportException
     * @throws IOException 
     */
    public void startProgram() throws ClientNotProperlyInitException, 
            TransportException, IOException {
        // check that at least server ip and port are set 
        if( serverIpAddress.equals("0.0.0.0") || serverPort == 0 )
            throw new ClientNotProperlyInitException("Please set the server IP address "
                    + " as well as the server port for the client " + this.getIpAddress() 
                    + " UUID: " + this.getUUID());
        SSHCommands.startClientProgram(this);
        
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
     */
    public void deletePreviousRemoteMessages() 
            throws TransportException, IOException { 
        SSHCommands.deleteRemoteFile(this, "*"+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_THREADS_SYNCH);
        SSHCommands.deleteRemoteFile(this, "*"+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_START_SENDING_REQUESTS);
        SSHCommands.deleteRemoteFile(this, "*"+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_FINISHED);
    }
    
    /**
     * 
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public int runClientRemotely() throws TransportException, IOException { 
        return SSHCommands.startClientProgram(this);
    }
}

class ClientNotProperlyInitException extends Exception 
{
    public ClientNotProperlyInitException(String string) {
        super(string);
    }
}
