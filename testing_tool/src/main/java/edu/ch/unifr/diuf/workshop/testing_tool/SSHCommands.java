package edu.ch.unifr.diuf.workshop.testing_tool;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import java.io.IOException;
import java.io.InputStream;

import net.schmizz.sshj.transport.TransportException;

/** This examples demonstrates how a remote command can be executed. */
public class SSHCommands 
{      
    /**
     * It just runs a dummy remote command to be sure the connection is working
     * 
     * @param machine
     * @throws TransportException
     * @throws IOException 
     */
    public static void testConnection(Machine machine) throws TransportException, IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(machine.getIpAddress(), machine.getPort());   
                
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(machine.getSSHUsername(), machine.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                // run just a dummy command
                final Command cmd = session.exec("date");
                //System.out.println(IOUtils.readFully(cmd.getInputStream()).toString());
                //cmd.join(5, TimeUnit.SECONDS);
                //System.out.println("\n** exit status: " + cmd.getExitStatus());
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }
    
    /**
     * 
     * @param machine
     * @param localFile
     * @param remoteFile
     * @throws TransportException
     * @throws IOException 
     */
    public static void uploadRemoteFile(Machine machine, String localFile, String remoteFile) 
            throws IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(machine.getIpAddress(), machine.getPort());   
        
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(machine.getSSHUsername(), machine.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                // Compression = significant speedup for large file transfers on fast links
                // present here to demo algorithm renegotiation - could have just put this before connect()
                ssh.useCompression();
                ssh.newSCPFileTransfer().upload(localFile, remoteFile);
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }
    
    
    /**
     * 
     * @param machine
     * @param localFile
     * @param remoteFile
     * @throws TransportException
     * @throws IOException 
     */
    public static void downloadRemoteFile(Machine machine, String localFile, String remoteFile) 
            throws IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(machine.getIpAddress(), machine.getPort());   
        
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(machine.getSSHUsername(), machine.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                // Compression = significant speedup for large file transfers on fast links
                // present here to demo algorithm renegotiation - could have just put this before connect()
                ssh.useCompression();
                ssh.newSCPFileTransfer().download(localFile, remoteFile);
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }
    
    /**
     * 
     * @param machine
     * @param remoteFile
     * @return 0 if the file exists
     * @throws TransportException
     * @throws IOException 
     */
    public static int testRemoteFileExists(Machine machine, String remoteFile) throws TransportException, IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(machine.getIpAddress(), machine.getPort());   
                
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(machine.getSSHUsername(), machine.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                // run just a dummy command
                final Command cmd = session.exec("test -e " +remoteFile);
                cmd.join();
                return cmd.getExitStatus();
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }    

    /**
     * 
     * @param machine
     * @param remoteFile
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int deleteRemoteFile(Machine machine, String remoteFile) throws TransportException, IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(machine.getIpAddress(), machine.getPort());   
                
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(machine.getSSHUsername(), machine.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                // run just a dummy command
                final Command cmd = session.exec("rm -r " + remoteFile);
                cmd.join();
                return cmd.getExitStatus();
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }
    
    /**
     * Test if a client can ping the server => it has network connection.
     * 
     * @param machine
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int clientPingServer(Client machine) throws TransportException, IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(machine.getIpAddress(), machine.getPort());   
                
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(machine.getSSHUsername(), machine.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                // run just a dummy command
                final Command cmd = session.exec("ping -c 5 " + machine.getServerIpAddress());
                //System.out.println(IOUtils.readFully(cmd.getInputStream()).toString());
                cmd.join();
                return cmd.getExitStatus();
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }
    
    /**
     * 
     * @param client
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int startClientProgram(Client client, Server server) throws TransportException, IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(client.getIpAddress(), client.getPort());   
                
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(client.getSSHUsername(), client.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("java -jar "); 
                sb.append(Utils.CLIENT_PROGRAM_REMOTE_FILENAME);
                // flag for distributed mode enabled (to enable the synch mechanism via files)
                sb.append(" yes ");  
                sb.append(server.getIpAddress());
                sb.append(" ");
                sb.append(server.getServerHttpPort());
                sb.append(" ");
                // because distributed mode is yes, then add client id here 
                sb.append(client.getUUID());
                sb.append(" ");
                sb.append(client.getNoThreads());
                sb.append(" ");
                sb.append(client.getDelay());
                sb.append(" ");
                sb.append(client.getNoReq());
                // output to a log file 
                sb.append(" &> ");
                sb.append(Utils.getClientLogRemoteFilename(client));
                sb.append(" & ");
                
                final Command cmd = session.exec(sb.toString());
                //System.out.println(IOUtils.readFully(cmd.getInputStream()).toString());
                cmd.join();
                return cmd.getExitStatus();
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }
    
    /**
     * 
     * @param server
     * @return PID of server program 
     * @throws TransportException
     * @throws IOException 
     */
    public static int startServerProgram(Server server) throws TransportException, IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(server.getIpAddress(), server.getPort());   
                
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(server.getSSHUsername(), server.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("java -jar "); 
                sb.append(Utils.SERVER_PROGRAM_REMOTE_FILENAME);
                sb.append(" ");
                sb.append(server.getServerType());
                sb.append(" ");
                sb.append(server.getServerMode());
                sb.append(" ");
                sb.append(server.getServerHttpPort());
                sb.append(" ");
                // output to a log file 
                sb.append(" &> ");
                sb.append(Utils.getServerLogRemoteFilename(server));
                sb.append(" & ");
                
                //System.out.println("Run command: " + sb.toString());
                final Command cmd = session.exec(sb.toString());
                cmd.join();
                return cmd.getExitStatus();
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }
    
    /**
     * 
     * @param server
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int killServerProgram(Server server) throws TransportException, IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(server.getIpAddress(), server.getPort());   
                
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(server.getSSHUsername(), server.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("kill "); 
                sb.append(server.getPID());
                //System.out.println("Run command: " + sb.toString());
                final Command cmd = session.exec(sb.toString());
                cmd.join();
                return cmd.getExitStatus();
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }
    
    /**
     * 
     * @param server
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int getProgramPID(Machine machine) throws TransportException, IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(machine.getIpAddress(), machine.getPort());   
                
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(machine.getSSHUsername(), machine.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("head ");
                if( machine instanceof Server ) 
                    sb.append(Utils.getServerLogRemoteFilename(machine));
                else if ( machine instanceof Client ) 
                    sb.append(Utils.getClientLogRemoteFilename(machine));
                sb.append(" | grep PID | cut -d ' ' -f 2 ");
                final Command cmd = session.exec(sb.toString());
                InputStream is = cmd.getInputStream();
                byte buffer[] = new byte[255];
                is.read(buffer);
                String buf = new String(buffer).trim();
                //System.out.println(new String(buffer));
                cmd.join();
                return Integer.valueOf(buf);
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }
    
    /**
     * 
     * @param machine
     * @param filename
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int createRemoteFile(Machine machine, String filename) throws TransportException, IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(machine.getIpAddress(), machine.getPort());   
                
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(machine.getSSHUsername(), machine.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("touch ");
                sb.append(filename);
                final Command cmd = session.exec(sb.toString());
                cmd.join();
                return cmd.getExitStatus();
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }

    /**
     * 
     * @param machine
     * @param folderName
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int createRemoteFolder(Machine machine, String folderName) throws TransportException, IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(machine.getIpAddress(), machine.getPort());   
                
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(machine.getSSHUsername(), machine.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                // run just a dummy command
                final Command cmd = session.exec("mkdir " + folderName);
                cmd.join();
                return cmd.getExitStatus();
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }
    
    /**
     * 
     * @param machine
     * @param PID
     * @return 0 if the remote PID is still running
     * @throws TransportException
     * @throws IOException 
     */
    public static int checkIfRemotePIDIsRunning(Machine machine, Integer PID) throws TransportException, IOException 
    {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts(); 
        ssh.connect(machine.getIpAddress(), machine.getPort());   
                
        try {
            // UNFORTUNATELY, authPublicKey could not make it working! :(
            ssh.authPassword(machine.getSSHUsername(), machine.getSSHPassword());
            final Session session = ssh.startSession();
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("ps cax | grep java | grep -o '^[ ]*[0-9]*' | grep ");
                sb.append(PID);
                
                final Command cmd = session.exec(sb.toString());
                cmd.join();
                // if this is 0, then the given PID is still running 
                return cmd.getExitStatus();
            } finally {
                // whatever happens, do not forget to close the session
                session.close();
            }
        } finally {
            // whatever happens, do not forget to disconnect the client
            ssh.disconnect();
        }
    }   
}