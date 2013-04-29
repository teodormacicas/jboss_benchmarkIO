package edu.ch.unifr.diuf.workshop.testing_tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;


/**
 *
 * @author Teodor Macicas
 */
public class Machine 
{
    private final static Logger LOGGER = Logger.getLogger(
            Machine.class.getName());
    
    public enum Status {
        SSH_CONN_PROBLEMS, OK,
        SYNCH_THREADS, RUNNING_REQUESTS, FINISHED
    }
            
    private String UUID;
    private String ipAddress; 
    private int port;
    protected Status status;
    
    private String sshUsername;
    private String sshPassword;
    
    public Machine() {
        this.UUID = Utils.generateRandomUUID();
        this.ipAddress = "0.0.0.0"; 
        this.port = 0;
        this.sshUsername = new String();
        this.sshPassword = new String();
    }
    
    /**
     * 
     * @return 
     */
    public String getUUID() { 
        return this.UUID;
    }
    
    /**
     *
     * @param ipAddress
     * @throws WrongIpAddressException
     */
    public void setIpAddress(String ipAddress) throws WrongIpAddressException { 
        if( Utils.validateIpAddress(ipAddress) ) 
            this.ipAddress = ipAddress;
        else
            throw new WrongIpAddressException(ipAddress + " cannot be set due to"
                    + " validation errors");
    }
    
    /**
     *
     * @retrurn ipAddress
     */
    public String getIpAddress() { 
        return this.ipAddress;
    }
    
    /**
     *
     * @param port
     * @throws WrongIpAddressException
     */
    public void setPort(int port) throws WrongPortNumberException { 
        if( Utils.validateLocalPort(port) ) 
            this.port = port;
        else
            throw new WrongPortNumberException(port + " cannot be set due to "
                    + "validation errors.");
    }
    
    /**
     *
     * @return port
     */
    public int getPort() { 
        return this.port;
    }
    
    /**
     *
     * @param status
     */
    public void setStatus(Status status) { 
        this.status = status;
    }
    
    /**
     *
     * @return status
     */
    public Status getStatus() { 
        return this.status;
    }
    
    /**
     *
     * @param sshUsername
     */
    public void setSSHUsername(String sshUsername) { 
        this.sshUsername = sshUsername;
    }
    
    /**
     *
     * @return sshUsername
     */
    public String getSSHUsername() { 
        return this.sshUsername;
    }
    
    /**
     *
     * @param sshPassword
     */
    public void setSSHPassword(String sshPassword) { 
        this.sshPassword = sshPassword;
    }
    
    /**
     *
     * @return status
     */
    public String getSSHPassword() { 
        return this.sshPassword;
    }
    
    /**
     * Checks if an SSH connection is successful. 
     *
     * @throws SSHConnectionException if exceptions were caught
     */
    public void checkSSHConnection() throws SSHConnectionException {
        try {
            SSHCommands.testConnection(this);
        } catch( TransportException ex ) {
            LOGGER.log(Level.SEVERE, "Exception thrown while trying to connect to machine ("
                    + this.getIpAddress()+"). Check ~/.ssh/known_hosts file if "
                    + "it contains the fingerprint of ssh remote key. ", ex);
            throw new SSHConnectionException(this.ipAddress);
        } catch( UserAuthException ex ) { 
             LOGGER.log(Level.SEVERE, "Exception thrown while trying to connect to machine ("
                    + this.getIpAddress()+"). User " + this.getSSHUsername() + 
                    " could not be authenticated.", ex);
            throw new SSHConnectionException(this.ipAddress);
        } catch( IOException ex ) {
             LOGGER.log(Level.SEVERE, "Exception thrown while checking machine ("
                    +this.getIpAddress()+") connectivity.", ex);
            throw new SSHConnectionException(this.ipAddress);
        }  
    }

    /**
     * 
     * @returns true if all needed properties were already set 
     */
    public boolean clientProperlyCreated() { 
        if( this.ipAddress.equals("0.0.0.0") || 
            this.port == 0 || 
            this.sshPassword.isEmpty() || 
            this.sshUsername.isEmpty() )  {
            return false;
        }
        return true;
    }
    
    /**
     * 
     * @returns a more comprehensible status message 
     */
    public String getStatusMessage() { 
        if( status == Status.OK )
            return "Machine " + this.getIpAddress() + ":" + this.getPort() + " is "
                    + "up and running.";
        else if( status == Status.SSH_CONN_PROBLEMS )
            return "Machine " + this.getIpAddress() + ":" + this.getPort() + " has "
                    + "SSH connectivity problems.";
        return "No status known for machine " + this.getIpAddress();
    }

    /**
     * 
     * @param localFile
     * @param remoteFile 
     */
    protected void uploadFile(String localFile, String remoteFile) 
            throws FileNotFoundException, IOException {
        if( ! new File(localFile).exists() )
            throw new FileNotFoundException("Local file " + localFile 
                    +" to upload does not exist.");
        SSHCommands.uploadRemoteFile(this, localFile, remoteFile);
    }
}



class WrongIpAddressException extends Exception 
{
    public WrongIpAddressException(String string) {
        super(string);
    }
}

class WrongPortNumberException extends Exception 
{
    public WrongPortNumberException(String string) {
        super(string);
    }
}

class SSHConnectionException extends Exception 
{
    public SSHConnectionException(String string) {
        super(string);
    }
}
