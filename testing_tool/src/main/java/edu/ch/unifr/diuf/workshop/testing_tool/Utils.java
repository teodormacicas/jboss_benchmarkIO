package edu.ch.unifr.diuf.workshop.testing_tool;

import java.util.regex.Pattern;
import java.util.UUID;

/**
 *
 * @author Teodor Macicas
 */
public class Utils 
{   
    public static final String PROPERTIES_FILENAME = "server_clients.properties";
    
    public static final Integer DELAY_CHECK_CONN_MS = 2000;
    public static final String STATUS_FILENAME = "status.log";
    
    // these 2 must be set during the parsing of the properties file
    public static String CLIENT_PROGRAM_LOCAL_FILENAME;
    public static String SERVER_PROGRAM_LOCAL_FILENAME;
    
    // this must be a jar file ... 
    public static final String CLIENT_PROGRAM_REMOTE_FILENAME = "client.jar";
    public static final String SERVER_PROGRAM_REMOTE_FILENAME = "server.jar";
    
    // some suffixes used to create the remote files for signaling different statuses
    // IMPORTANT: the algorithm using these will fail if the clients do not use the same names!!!
    public static final String CLIENT_REMOTE_FILENAME_SUFFIX_THREADS_SYNCH = "-threads-are-synched";
    public static final String CLIENT_REMOTE_FILENAME_SUFFIX_START_SENDING_REQUESTS = "-start-sending-requests";
    public static final String CLIENT_REMOTE_FILENAME_SUFFIX_FINISHED = "-finished";
    
    // when a client creates this file it means it has reached the point where the threads are synch 
    public static final String LOCAL_SYNCH_CLIENT_REMOTE_FILENAME = "threads_are_synched";
    
    public static boolean validateIpAddress(String ipAddress) {
        final Pattern IP_PATTERN = Pattern.compile(
        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        
        return IP_PATTERN.matcher(ipAddress).matches();
    }
    
    public static boolean isLoopbackIpAddress(String ipAddress) {
        if( ipAddress.startsWith("127.") ) 
            return true;
        return false;
    }

    public static boolean validateLocalPort(int port) { 
        if( port <= 0 || port > 65536 ) { 
            return false;
        }
        return true;
    }
    
    public static boolean validateRemotePort(int port) { 
        if( port <= 1024 || port > 65536 ) { 
            return false;
        }
        return true;
    }
    
    public static String generateRandomUUID() { 
        return UUID.randomUUID().toString();
    }
    
    public static String getClientProgramRemoteFilename(Client c) { 
        //return Utils.CLIENT_PROGRAM_REMOTE_BASENAME+"-"+c.getUUID()+".jar";
        return Utils.CLIENT_PROGRAM_REMOTE_FILENAME;
    }
    
    public static String getServerProgramRemoteFilename(Server s) { 
        return Utils.SERVER_PROGRAM_REMOTE_FILENAME;
    }
    
    public static String getClientLogRemoteFilename(Client c) {
        return "log-"+c.getUUID()+".data";
    }
    
    public static String getServerLogRemoteFilename(Server s) { 
        return "log-"+s.getIpAddress()+"-"+s.getPort()+".data";
    }
    
    public static String getClientRemoteSynchThreadsFilename(Client c) {
        return c.getUUID()+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_THREADS_SYNCH;
    }
    
    public static String getClientRemoteStartRequestsFilename(Client c) {
        return c.getUUID()+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_START_SENDING_REQUESTS;
    }
    
    public static String getClientRemoteDoneFilename(Client c) {
        return c.getUUID()+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_FINISHED;
    }

    public static boolean validateServerTypes(String serverType) { 
        if( serverType.equals("xnio3") || 
            serverType.equals("nio2") || 
            serverType.equals("netty") ) {
            return true;
        }
        return false;
    }
    
    public static boolean validateServerMode(String serverMode) { 
        if( serverMode.equals("sync") ||
            serverMode.equals("async") ) {
            return true;
        }
        return false;
    }
}