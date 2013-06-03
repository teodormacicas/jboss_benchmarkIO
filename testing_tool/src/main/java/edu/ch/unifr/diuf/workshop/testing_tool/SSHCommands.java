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
     * @param sshclient
     * @throws TransportException
     * @throws IOException 
     */
    public static void testConnection(Machine machine, SSHClient ssh) 
            throws TransportException, IOException 
    {    
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
    }
    
    /**
     * 
     * @param machine
     * @param localFile
     * @param remoteFile
     * @param sshclient
     * @throws TransportException
     * @throws IOException 
     */
    public static void uploadRemoteFile(Machine machine, String localFile, 
            String remoteFile, SSHClient ssh) throws IOException 
    {
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
    }
    
    /**
     * 
     * @param machine
     * @param localFile
     * @param remoteFile
     * @param sshClient
     * @throws TransportException
     * @throws IOException 
     */
    public static void downloadRemoteFile(Machine machine, String localFile, 
            String remoteFile, SSHClient ssh) throws IOException {
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
    }
    
    /**
     * 
     * @param machine
     * @param remoteFile
     * @param sshClient
     * @return 0 if the file exists
     * @throws TransportException
     * @throws IOException 
     */
    public static int testRemoteFileExists(Machine machine, String remoteFile, 
            SSHClient ssh) throws TransportException, IOException {
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
    }    

    /**
     * 
     * @param machine
     * @param remoteFile
     * @param sshClient
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int deleteRemoteFile(Machine machine, String remoteFile, 
            SSHClient ssh ) throws TransportException, IOException {
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
    }
    
    /**
     * Test if a client can ping the server => it has network connection.
     * 
     * @param machine
     * @param sshClient
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int clientPingServer(Client machine, SSHClient ssh)
            throws TransportException, IOException {   
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
    }
    
    /**
     * 
     * @param client
     * @param sshClient
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int startClientProgram(Client client, Server server, SSHClient ssh) 
            throws TransportException, IOException, InterruptedException {
        final Session session = ssh.startSession();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("java -jar "); 
            sb.append(Utils.getClientProgramRemoteFilename(client));
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
            sb.append(" "); 
            sb.append(client.getWorkingDirectory());
            // output to a log file 
            sb.append(" &> ");
            sb.append(Utils.getClientLogRemoteFilename(client));
            sb.append(" & ");

            final Command cmd = session.exec(sb.toString());
            cmd.join();
            // wait a bit until returning as the client should have enough time to 
            // start and print its PID into the log file (it is checked just after)
            Thread.sleep(2000);
            return cmd.getExitStatus();
        } finally {
            // whatever happens, do not forget to close the session
            session.close();
        }
    }
    
    /**
     * 
     * @param server
     * @param sshClient
     * @return PID of server program 
     * @throws TransportException
     * @throws IOException 
     */
    public static int startServerProgram(Server server, SSHClient ssh) 
            throws TransportException, IOException, InterruptedException {
        final Session session = ssh.startSession();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("java -jar "); 
            sb.append(Utils.getServerProgramRemoteFilename(server));
            sb.append(" ");
            sb.append(server.getServerType());
            sb.append(" ");
            sb.append(server.getServerMode());
            sb.append(" ");
            sb.append(server.getServerHttpPort());
            sb.append(" ");
            sb.append(server.getServerHTTPListenAddress());
            sb.append(" ");
            // output to a log file 
            sb.append(" &> ");
            sb.append(Utils.getServerLogRemoteFilename(server));
            sb.append(" & ");

            //System.out.println("Run command: " + sb.toString());
            final Command cmd = session.exec(sb.toString());
            cmd.join();
            // wait a bit until returning as the server should have enough time to 
            // start and print its PID into the log file (it is checked just after)
            Thread.sleep(2000);
            return cmd.getExitStatus();
        } finally {
            // whatever happens, do not forget to close the session
            session.close();
        }
    }
    
    /**
     * 
     * @param machine
     * @param sshClient
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int killProgram(Machine machine, SSHClient ssh) 
            throws TransportException, IOException {
        final Session session = ssh.startSession();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("kill "); 
            sb.append(machine.getPID());
            //System.out.println("Run command: " + sb.toString());
            final Command cmd = session.exec(sb.toString());
            cmd.join();
            return cmd.getExitStatus();
        } finally {
            // whatever happens, do not forget to close the session
            session.close();
        }
    }
    
    /**
     * 
     * @param server
     * @param sshClient
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int getProgramPID(Machine machine, SSHClient ssh) 
            throws TransportException, IOException {
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
            //System.out.println("Command get PID: " + sb.toString());            
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
    }
    
    /**
     * 
     * @param machine
     * @param sshClient
     * @param filename
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int getTimeSinceLastLogModification(Machine machine, 
            String filename, SSHClient ssh) throws TransportException, IOException {
        final Session session = ssh.startSession();
        try {
            // e.g.: echo $((`stat -c "%Y" log-31cc4e50-1915-4a2d-ad45-02a0e8f79282.data`-`date +%s`))
            StringBuilder sb = new StringBuilder();
            sb.append("echo $(( `stat -c \"%Y\" ");
            sb.append(filename);
            sb.append("`-`date +%s`))");
            final Command cmd = session.exec(sb.toString());
            InputStream is = cmd.getInputStream();
            byte buffer[] = new byte[255];
            is.read(buffer);
            String buf = new String(buffer).trim();
            cmd.join();
            /*System.out.println("Client " + machine.getIpAddress() + " log modified "
                    + ((-1)*Integer.valueOf(buf)) + " seconds ago ... "
                    + machine.getUUID()); */
            return ((-1)*Integer.valueOf(buf));
        } finally {
            // whatever happens, do not forget to close the session
            session.close();
        }
    }
    
    
    /**
     * 
     * @param machine
     * @param filename
     * @param sshClient
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int createRemoteFile(Machine machine, String filename, SSHClient ssh) 
            throws TransportException, IOException {
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
    }

    /**
     * 
     * @param machine
     * @param folderName
     * @param sshClient 
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int createRemoteFolder(Machine machine, String folderName, 
            SSHClient ssh) throws TransportException, IOException {
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
    }
    
    /**
     * 
     * @param machine
     * @param PID
     * @param sshClient
     * @return 0 if the remote PID is still running
     * @throws TransportException
     * @throws IOException 
     */
    public static int checkIfRemotePIDIsRunning(Machine machine, Integer PID,
            SSHClient ssh) throws TransportException, IOException {
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
    }   
    
    /**
     * 
     * @param machine
     * @param remoteDirectory
     * @param ssh
     * @return
     * @throws TransportException
     * @throws IOException 
     */
    public static int checkIfRemoteDirIsWritable(Machine machine, String remoteDirectory,
            SSHClient ssh) throws TransportException, IOException {
        final Session session = ssh.startSession();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("test -w ");
            sb.append(remoteDirectory);

            final Command cmd = session.exec(sb.toString());
            cmd.join();
            // if this is 0, then the given remote directory is writable
            return cmd.getExitStatus();
        } finally {
            // whatever happens, do not forget to close the session
            session.close();
        }
    } 
}