package edu.ch.unifr.diuf.workshop.testing_tool;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import net.schmizz.sshj.transport.TransportException;

/**
 *
 * @author Teodor Macicas
 */
public class Server extends Machine
{    
     // a path to a folder containing data to be used by the client requests
     private String dataFolderPath;
     // running parameters of the server 
     private String serverType; 
     private String serverMode; 
     // this is also used by the clients to know where to connect to
     private int serverPort;
     // if set to 'yes', in case of failure restartAttempts is tried 
     private String faultTolerant;
     private int restartAttempts;
    
     public Server(String ipAddress, int port, String sshUsername, String sshPassword) 
            throws WrongIpAddressException, WrongPortNumberException {
        super();
        this.setIpAddress(ipAddress);
        this.setPort(port);
        this.setSSHUsername(sshUsername);
        this.setSSHPassword(sshPassword);
     }
     
     
     /**
      * 
      * @param path 
      */
     public void setDataFolderPath(String path) { 
         this.dataFolderPath = path;
     }
     
     /**
      * 
      */
     public String getDataFolderPath() { 
         return this.dataFolderPath;
     }
     
     /**
      * 
      * @param serverType
      * @throws WrongServerTypeException 
      */
     public void setServerType(String serverType) throws WrongServerTypeException { 
        if( ! Utils.validateServerTypes(serverType) ) 
            throw new WrongServerTypeException("Please use one of the following "
                    + "server types: xnio3, nio2, netty. ");
        this.serverType = serverType;
    }
    
     /**
      * 
      * @return 
      */
     public String getServerType() { 
        return this.serverType;
     }
     
     /**
      * 
      * @param serverMode
      * @throws WrongServerModeException 
      */
     public void setServerMode(String serverMode) throws WrongServerModeException { 
         if( ! Utils.validateServerMode(serverMode) ) 
             throw new WrongServerModeException("Please use one of the following "
                     + "server modes: asynch/synch. However, Netty is only available "
                     + "for asynch type.");
         this.serverMode = serverMode;
     }
     
     /**
      * 
      * @return 
      */
     public String getServerMode() { 
         return this.serverMode;
     }
    
     /**
      * 
      * @param httpPort
      * @throws WrongPortNumberException 
      */
    public void setServerHttpPort(int httpPort) throws WrongPortNumberException { 
        if( ! Utils.validateRemotePort(httpPort) )
            throw new WrongPortNumberException("Server http port number is "
                    + "not valid.");
        this.serverPort = httpPort;
    }
     
    /**
     * 
     * @return 
     */
    public int getServerHttpPort() { 
        return this.serverPort;
    }
    
    /**
     * 
     * @param faultTolerant 
     */
    public void setFaultTolerant(String faultTolerant) { 
        this.faultTolerant = faultTolerant;
    }
    
    /**
     * 
     * @return 
     */
    public String getFaultTolerant() { 
        return this.faultTolerant;
    }
    
    /**
     * 
     * @param restartAttempts 
     */
    public void setRestartAttempts(int restartAttempts) { 
        this.restartAttempts = restartAttempts;
    }
    
    /**
     * 
     * @return 
     */
    public int getRestartAttempts() { 
        return this.restartAttempts;
    }
    
    /**
      * 
      * @param file 
      */
    public void uploadProgram(String file) throws FileNotFoundException, IOException {
        this.uploadFile(file, Utils.SERVER_PROGRAM_REMOTE_FILENAME);
    }
    
    /**
     * 
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public int runServerRemotely() throws TransportException, IOException { 
        int r = SSHCommands.startServerProgram(this);
        if( r != 0 ) { 
            System.out.println("[ERROR] Server could not be properly started! "
                    + "Exit code: " + r);
            getServerLogAndPrintIt();
            return -1;
        }
        this.setPID(SSHCommands.getProgramPID(this));
        return 0;
    }
    
    /**
     * 
     * @throws IOException 
     */
    private void getServerLogAndPrintIt() throws IOException { 
        SSHCommands.downloadRemoteFile(this, Utils.getServerLogRemoteFilename(this),
                Utils.getServerLogRemoteFilename(this));
        int no_lines = 15;
        System.out.println("[INFO] Print maximum " + no_lines + " from the server log. "
                + "Please check the local log for more information! ");
        BufferedReader br = new BufferedReader(
                new FileReader(Utils.getServerLogRemoteFilename(this)));
        String line;
        while ((line = br.readLine()) != null) {
            if( --no_lines == 0 )
                break; 
            System.out.println(line);
        }
    }
    
    /**
     * 
     * @returns a more comprehensible status message 
     */
    public String getStatusMessage() { 
        StringBuilder sb = new StringBuilder();
        sb.append("Server ").append(this.getIpAddress());
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
        
        sb.append("\n\t\tFAULT TOLERANCE: ");
        if( getFaultTolerant().equals("yes") ) {
            sb.append(" yes ");
            sb.append(getRestartAttempts() + " retrials");
        }
        else
            sb.append(" no ");
        
        return sb.toString();
    }
    
    /**
     * 
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public int killServer() throws TransportException, IOException { 
        if( getPID() != 0 )
            return SSHCommands.killProgram(this);
        return 1;
    }
}

class ServerNotProperlyInitException extends Exception 
{
    public ServerNotProperlyInitException(String string) {
        super(string);
    }
}

class WrongServerModeException extends Exception 
{
    public WrongServerModeException(String string) {
        super(string);
    }
}

class WrongServerTypeException extends Exception 
{
    public WrongServerTypeException(String string) {
        super(string);
    }
}