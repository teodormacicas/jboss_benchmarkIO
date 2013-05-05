package edu.ch.unifr.diuf.workshop.testing_tool;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public int killServer() throws TransportException, IOException { 
        return SSHCommands.killServerProgram(this);
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