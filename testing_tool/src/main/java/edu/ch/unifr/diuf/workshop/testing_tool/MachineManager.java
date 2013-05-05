package edu.ch.unifr.diuf.workshop.testing_tool;

import edu.ch.unifr.diuf.workshop.testing_tool.Machine.Status;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.schmizz.sshj.transport.TransportException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author Teodor Macicas
 */
public class MachineManager 
{
    private final static Logger LOGGER = Logger.getLogger(
            MachineManager.class.getName());
    
    private ArrayList<Client> clients;
    private Server server;
    
    private MachineConnectivityThread cct;
    private WriteStatusThread wst;
    private CheckMessagesThread cmt;
    private CheckRunningPIDsThread crpt;
    
    // it regulary checks if the machines are still reachable
    protected class MachineConnectivityThread extends Thread 
    {
        // sleep this time between two consecutive checks
        private int delay;
        private boolean finish;
        
        public MachineConnectivityThread(int delay) {
            this.delay = delay;
            this.finish = false;
        }
        
        public void setFinished(boolean finish) {
            this.finish = finish;
        }
        
        // regulary check the connection status and update a flag accordingly
        public void run() {
            boolean server_up = false;
            while( !finish ) {
                // check server connection
                try {
                    server.checkSSHConnection();
                    server.setStatusConnection(Machine.Status.OK);
                    server_up = true;
                } catch (SSHConnectionException ex) {
                    server.setStatusConnection(Machine.Status.SSH_CONN_PROBLEMS);
                    LOGGER.log(Level.SEVERE, "Connectivity problems with the "
                            + "server ("+server.getIpAddress()+"). Check logs. "
                            + "Retry in "+delay+"ms.", 
                            ex);
                    server_up = false;
                }

                if( server_up ) { 
                    // now check client connectivity only if the server is up
                    for(Iterator it=clients.iterator(); it.hasNext(); ) {
                        Client c = (Client)it.next();

                        try {
                           c.checkSSHConnection();
                           c.setStatusConnection(Machine.Status.OK);
                        } catch (SSHConnectionException ex) {         
                            c.setStatusConnection(Machine.Status.SSH_CONN_PROBLEMS);
                            LOGGER.log(Level.SEVERE, "Connectivity problems with the client "
                                    +c.getIpAddress()+"). Check logs. "
                                    + "Retry in "+delay+"ms.", ex);
                        }
                    }
                }
                
                try {
                    // now sleep a bit until next check
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
      
    // it regulary writes to a log file the status of the running machines
    protected class WriteStatusThread extends Thread 
    {
        // sleep this time between two consecutive checks
        private int delay;
        private boolean finish;
        private FileOutputStream fis;
        private DateFormat dateFormat;
        
        public WriteStatusThread(int delay, String filePath) {
            this.delay = delay;
            this.finish = false;
            try {
                File f = new File(filePath);
                if( f.exists() ) 
                    f.delete();
                f.createNewFile();
                this.fis = new FileOutputStream(f);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            this.dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        }
        
        public void setFinished(boolean finish) {
            this.finish = finish;
        }
        
        //regulary write the status on an output file
        @Override
        public void run() {
            while( !finish ) {
                StringBuilder sb = new StringBuilder();
                sb.append("\nMachine status @ ");
                sb.append(dateFormat.format(new Date())+"\n");
                // write server status 
                sb.append("\tServer: \n\t\t");
                sb.append(server.getStatusMessage());
                sb.append("\n\t");
                
                // write clients status
                sb.append("Clients: \n\t\t");
                for(Iterator it=clients.iterator(); it.hasNext(); ) {
                    Client c = (Client)it.next();
                    sb.append(c.getStatusMessage()+"\n\t\t");
                }
                
                try {
                    // write to file 
                    fis.write(sb.toString().getBytes());
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Exception while writing status to "
                            + "output file.", ex);
                }
                
                try {
                    // now sleep a bit 
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    // it regulary checks remotely for messages (e.g. if the clients are ready to 
    // start requests or if they are finished )
    protected class CheckMessagesThread extends Thread 
    {
        // sleep this time between two consecutive checks
        private int delay;
        private boolean finish;
        private FileOutputStream fis;
        
        public CheckMessagesThread(int delay) {
            this.delay = delay;
            this.finish = false;
        }
        
        public void setFinished(boolean finish) {
            this.finish = finish;
        }
        
        /*
         * regulary check the status of remote clients and set it accordingly to 
         * out local objects
         */
        @Override
        public void run() {
            int r;
            while( !finish ) {
                try {
                    for(Iterator it=clients.iterator(); it.hasNext(); ) {
                        Client c = (Client)it.next();
                        // check if the remote client is done with the requests
                        r = SSHCommands.testRemoteFileExists(c, Utils.getClientRemoteDoneFilename(c));
                        if( r == 0 ) {
                            c.setStatusSynch(Status.DONE);
                            continue;
                        }
                        // if not, test if the cleint is still sending requests 
                        r = SSHCommands.testRemoteFileExists(c, Utils.getClientRemoteStartRequestsFilename(c));
                        if( r == 0 ) {
                            c.setStatusSynch(Status.RUNNING_REQUESTS);
                            continue;
                        }
                        // if not, check if the client has its threads synchronized
                        r = SSHCommands.testRemoteFileExists(c, Utils.getClientRemoteSynchThreadsFilename(c));
                        if( r == 0 ) {
                            c.setStatusSynch(Status.SYNCH_THREADS);
                            continue;
                        }
                        // if not, then no info yet about client synch
                        c.setStatusSynch(Status.NOT_INIT);
                    }
                } catch (TransportException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    // now sleep a bit 
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    // it regulary checks if server and client PIDs are still running
    // and it changes the status accordingly 
    protected class CheckRunningPIDsThread extends Thread 
    {
        // sleep this time between two consecutive checks
        private int delay;
        private boolean finish;
        private FileOutputStream fis;
        
        public CheckRunningPIDsThread(int delay) {
            this.delay = delay;
            this.finish = false;
        }
        
        public void setFinished(boolean finish) {
            this.finish = finish;
        }
        
        /*
         * regulary check the status of remote clients and set it accordingly to 
         * out local objects
         */
        @Override
        public void run() {
            int r;
            while( !finish ) {
                try {
                    // check server
                    if( server.getPID() != 0 ) {
                        r = SSHCommands.checkIfRemotePIDIsRunning(server, 
                                server.getPID());
                        if( r == 0 )
                            server.setStatusProcess(Status.PID_RUNNING);
                        else
                            server.setStatusProcess(Status.PID_NOT_RUNNING);
                    }
                    else
                        server.setStatusProcess(Status.NOT_INIT);
                    
                    // check clients 
                    for(Iterator it=clients.iterator(); it.hasNext(); ) {
                        Client c = (Client)it.next();
                        // check only if the program has been started and the PID gathered
                        if( c.getPID() != 0 ) {
                            r = SSHCommands.checkIfRemotePIDIsRunning(c, c.getPID());
                            if( r == 0 )
                                c.setStatusProcess(Status.PID_RUNNING);
                            else
                                c.setStatusProcess(Status.PID_NOT_RUNNING);
                        }
                        else 
                            c.setStatusProcess(Status.NOT_INIT);
                    }
                } catch (TransportException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    // now sleep a bit 
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    public MachineManager() {
        this.clients = new ArrayList<Client>();
        this.server = null;
        this.cct = new MachineConnectivityThread(Utils.DELAY_CHECK_CONN_MS);
        this.wst = new WriteStatusThread(Utils.DELAY_CHECK_CONN_MS, Utils.STATUS_FILENAME);
        this.cmt = new CheckMessagesThread(Utils.DELAY_CHECK_CONN_MS);
        this.crpt = new CheckRunningPIDsThread(Utils.DELAY_CHECK_CONN_MS);
    }
    
     /**
     *
     * @param a new client machine
     */
    public void addNewClient(Client new_client) throws ClientNotProperlyInitException { 
        if( new_client.clientProperlyCreated() )
            this.clients.add(new_client);
        else 
            throw new ClientNotProperlyInitException("Client " + new_client.getIpAddress()
                    + " does not have all needed params set up.");
    }
    
     /**
     *
     * @return a client from the list of them
     */
    public Client getClientNo(int no) { 
        if( no < 0 || no > clients.size() ) 
            return null;
        return clients.get(no);
    }
    
     /**
     *
     * @param the server that will be used by the clients
     */
    public void setServer(Server s) { 
        this.server = s; 
    }
    
     /**
     *
     * @return server
     */
    public Server getServer() { 
        return this.server;
    }
    
    /**
     * Parse the .properties file and create the server and clients objects.
     * 
     * @throws ConfigurationException
     * @throws WrongIpAddressException
     * @throws WrongPortNumberException
     * @throws ClientNotProperlyInitException 
     * @throws FileNotFoundException
     */
    public void parsePropertiesFile() throws ConfigurationException, 
            WrongIpAddressException, WrongPortNumberException, ClientNotProperlyInitException, FileNotFoundException { 
        Configuration config = new PropertiesConfiguration(
                Utils.PROPERTIES_FILENAME);
        
        // get server program filename 
        Utils.SERVER_PROGRAM_LOCAL_FILENAME = config.getString("server.programJarFile").trim();
        if( Utils.SERVER_PROGRAM_LOCAL_FILENAME.isEmpty() ||
            new File(Utils.SERVER_PROGRAM_LOCAL_FILENAME).exists() == false ) {
            
            throw new FileNotFoundException("Server local program file (" + 
                    Utils.SERVER_PROGRAM_LOCAL_FILENAME + ") does not exist. "
                    + "Please pass an already existing file or a correct path.");
        }
        
        // get server info 
        String serverDataFolder = config.getString("server.dataFolder");
        String serverSSHIpPort = config.getString("server.sshIpPort");
        String serverSSHUsername = config.getString("server.sshUsername");
        String serverSSHPassword = config.getString("server.sshPassword");
        String serverRunningParams = config.getString("server.runningParams");
        StringTokenizer st = new StringTokenizer(serverSSHIpPort, ":");
        if( st.countTokens() != 2 ) { 
            LOGGER.severe("Parsing error of server.sshIpPort. Please pass data "
                    + "using the pattern IP:PORT and nothing more. " + serverSSHIpPort);
            throw new ConfigurationException("Parsing error of server.sshIpPort. "
                    + "Please pass data using the pattern IP:PORT and nothing more. " 
                    + serverSSHIpPort);
        }
        // create the server here
        this.server = new Server(st.nextToken(), Integer.valueOf(st.nextToken()), 
                serverSSHUsername, serverSSHPassword);
        st = new StringTokenizer(serverRunningParams, " "); 
        if( st.countTokens() != 3 ) {
            LOGGER.severe("Parsing error of server.runningParams. Please check the "
                    + "right format in the properties file and re-run this.");
            throw new ConfigurationException("Parsing error of server.runningParams. Please check the "
                    + "right format in the properties file and re-run this.");
        }
        try {
            this.server.setServerType(st.nextToken());
        } catch( WrongServerTypeException ex ){
            LOGGER.severe(ex.getMessage());
            throw new ConfigurationException(ex.getMessage());
            
        }
        try {
            this.server.setServerMode(st.nextToken()); 
        } catch( WrongServerModeException ex ) {
            LOGGER.severe(ex.getMessage());
            throw new ConfigurationException(ex.getMessage());
        }
        this.server.setDataFolderPath(serverDataFolder);
        this.server.setServerHttpPort(Integer.valueOf(st.nextToken()));
        
        // get clients program filename 
        Utils.CLIENT_PROGRAM_LOCAL_FILENAME = config.getString("clients.programJarFile").trim();
        if( Utils.CLIENT_PROGRAM_LOCAL_FILENAME.isEmpty() ||
            ! new File(Utils.CLIENT_PROGRAM_LOCAL_FILENAME).exists() ) {
            throw new FileNotFoundException("Client local program file (" + 
                    Utils.CLIENT_PROGRAM_LOCAL_FILENAME + ") does not exist. "
                    + "Please pass an already existing file or a correct path.");
        }
        
        // this is used by the clients 
        List clientsIpPort = config.getList("clients.sshIpPort");
        List clientsSSHUsername = config.getList("clients.sshUsername");
        List clientsSSHPassword = config.getList("clients.sshPassword");
        List clientsSSHRunningParams = config.getList("clients.runningParams");
        List clientsTests = config.getList("clients.tests");
        
        if( clientsIpPort.size() != clientsSSHUsername.size() || 
            clientsIpPort.size() != clientsSSHPassword.size() || 
            clientsIpPort.size() != clientsSSHRunningParams.size() ) {
            LOGGER.severe("Please give sshIpPort, sshUsername, sshPassword and "
                    + "sshRunningParams parameters for each client.");
            throw new ConfigurationException("Please give sshIpPort, sshUsername, sshPassword and "
                    + "sshRunningParams parameters for each client.");
            
        }
        
        // iterate the list and create the clients
        int counter = -1;
        for(Iterator it=clientsIpPort.iterator(); it.hasNext(); ) {
            ++counter;
            String client_address = (String)it.next();
            String client_ssh_username = (String)clientsSSHUsername.get(counter);
            String client_ssh_password = (String)clientsSSHPassword.get(counter);
            String client_running_params = (String)clientsSSHRunningParams.get(counter);
            
            st = new StringTokenizer(client_address, ":");
            if( st.countTokens() > 2 ) { 
                LOGGER.severe("Parsing error of client.sshIpPort. Please pass data "
                    + "using the pattern IP:PORT and nothing more. " + client_address);
                throw new ConfigurationException("Parsing error of server.sshIpPort. "
                    + "Please pass data using the pattern IP:PORT and nothing more. "
                    + client_address);
            }
            Client c = new Client(st.nextToken(), Integer.valueOf(st.nextToken()), 
                    client_ssh_username, client_ssh_password);
            // set server info 
            c.setServerInfo(server.getIpAddress(), server.getServerHttpPort());
            // set running parameters
            st = new StringTokenizer(client_running_params, " ");
            if( st.countTokens() != 3 ) { 
                LOGGER.severe("Please pass all three running parameters for "
                        + "client with IP address " + c.getIpAddress());
                throw new ConfigurationException("Please pass all three running "
                        + "parameters for client with IP address " + c.getIpAddress());
            }
            // add running params
            c.setNoThreads(Integer.valueOf(st.nextToken()));
            c.setDelay(Integer.valueOf(st.nextToken()));
            c.setNoReq(Integer.valueOf(st.nextToken()));
            // add tests
            for( Iterator it2=clientsTests.iterator(); it2.hasNext(); ) {
                c.addNewTest((String)it2.next());
            }
            // store this client 
            this.addNewClient(c);
        }
    }
    
    /**
     *
     * @return false if either the server or clients are not yet set
     */
    public boolean checkIfClientAndServerSet() { 
        if( this.server == null || this.clients.isEmpty() ) 
            return false;
        return true;
    }
    
    /**
     * Either all or none ip addresses are from 127.0.0.0/8 subnetwork.
     * 
     * @return true if either all or none are loopback
     */
    public boolean checkIfAllOrNoneLoopbackAddresses() { 
        boolean all_loopback = true;
        if( ! Utils.isLoopbackIpAddress(server.getIpAddress()) )
            all_loopback = false;
        for(Iterator it=clients.iterator(); it.hasNext(); ) { 
            Client c = (Client)it.next();
            if( all_loopback && !Utils.isLoopbackIpAddress(c.getIpAddress()) ) {
                // at least one is not loopback, but the others are loopback
                return false;
            }
            if( !all_loopback && Utils.isLoopbackIpAddress(c.getIpAddress()) ) {
                // at least one is loopback, but the others are not 
                return false; 
            }
        }
        return true;
    }
    
    
    /**
     * All clients must have network connection with the server. Check it by 
     * remotely running a ping command. 
     * 
     * @return 
     */
    public boolean checkClientsCanAccessServer()
            throws TransportException, IOException { 
        int r;
        // iterate through clients and check whether they can ping the server
        for(Iterator it=clients.iterator(); it.hasNext(); ) {
            Client c = (Client) it.next(); 
            r = SSHCommands.clientPingServer(c);
            if( r != 0 )
                return false;
        }
        return true;
    }
    
    
    /**
     * Upload the program to the clients
     * 
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void uploadProgramToClients() 
            throws FileNotFoundException, IOException {        
        for(Iterator it=clients.iterator(); it.hasNext(); ) {
            Client c = (Client)it.next(); 
            c.uploadProgram(Utils.CLIENT_PROGRAM_LOCAL_FILENAME);
            // test the remote file exists
            int r = SSHCommands.testRemoteFileExists(c, Utils.getClientProgramRemoteFilename(c));
            if( r == 0 )
                System.out.println("[INFO] Remote file successfuly uploaded on client " + 
                        c.getIpAddress() + ".");
            else 
                System.out.println("[INFO] Remote file could not be uploaded on client " + 
                        c.getIpAddress() + "." + " Exit code: " + r);
        }
    }
    
    /**
     * Upload the program to the clients
     * 
     */
    public void uploadProgramToServer() 
            throws FileNotFoundException, IOException {        
        server.uploadProgram(Utils.SERVER_PROGRAM_LOCAL_FILENAME);
        // test the remote file exists
        int r = SSHCommands.testRemoteFileExists(server, Utils.getServerProgramRemoteFilename(server));
        if( r == 0 )
            System.out.println("[INFO] Remote file successfuly uploaded on server " + 
                        server.getIpAddress() + ".");
        else 
            System.out.println("[INFO] Remote file could not be uploaded on server " + 
                        server.getIpAddress() + "." + " Error code: " + r);
        
        // make remote dir 'data' to copy all the files 
        SSHCommands.createRemoteFolder(server, "data/");
        
        String files;
        File folder = new File(server.getDataFolderPath());
        File[] listOfFiles = folder.listFiles();  
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                files = listOfFiles[i].getName();
                // and now upload the data folder as well 
                SSHCommands.uploadRemoteFile(server, server.getDataFolderPath()+"/"+files, 
                        "data/"+files);
            }
        }
    }
    
    /**
     * 
     * @return true if all connectivity statuses are OK
     */
    public boolean allAreConnectionsOK() {
        if( server.getStatusConnection() != Status.OK ) {
            System.err.println("[ERROR] Server has network connection problem.");
            return false;
        }
        for(Iterator it=clients.iterator(); it.hasNext(); ) {
            Client c = (Client)it.next();
            if( c.getStatusConnection() != Status.OK ) {
                System.err.println("[ERROR] Client "+c.getIpAddress()
                        +" has network connection problem.");
                return false;
            }
        }
       return true;
    }
    
    /**
     * 
     * @throws TransportException
     * @throws IOException 
     */
    public void startServer() throws TransportException, IOException { 
        server.runServerRemotely();
    }
    
    /**
     * 
     * @throws TransportException
     * @throws IOException 
     */
    public void startAllClients() throws TransportException, IOException { 
        for(Iterator it=clients.iterator(); it.hasNext(); ) { 
            Client c = (Client)it.next();
            c.runClientRemotely(this.server);
        }
    }
    
    //TODO
    public void getAllLogFiles() { 
        
    }
    
    /**
     * Start only the connectivity thread firstly.
     */
    public void startConnectivityThread() {
         this.cct.start();
    }
    
    /**
     * 
     * Just run the status threads. 
     */
    public void startOtherThreads() {
        try {
             this.wst.start();
             Thread.sleep(200);
             this.cmt.start();
             Thread.sleep(200);
             this.crpt.start();
             Thread.sleep(400);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     * @return  true if all clients are synchronized
     */
    public boolean checkClientsSynch() { 
        //check every client synch status
        for(Iterator it=clients.iterator(); it.hasNext(); ) { 
            Client c = (Client)it.next();
            if( c.getStatusSynch() != Status.SYNCH_THREADS ) 
                return false;
        }
        return true;
    }
    
    /**
     * 
     * @throws TransportException
     * @throws IOException 
     */
    public void sendClientsMsgToStartRequests() throws TransportException, IOException { 
        for(Iterator it=clients.iterator(); it.hasNext(); ) { 
            Client c = (Client)it.next();
            SSHCommands.createRemoteFile(c, Utils.getClientRemoteStartRequestsFilename(c));
        }
    }
    
    /**
     * 
     * @return  true if all clients finished their tests
     */
    public boolean checkTestsCompletion() { 
        //check every client synch status
        for(Iterator it=clients.iterator(); it.hasNext(); ) { 
            Client c = (Client)it.next();
            if( c.getStatusSynch() != Status.DONE ) 
                return false;
        }
        return true;
    }
    
    /**
     * Download on local folder all the logs from server and clients.
     * @throws IOException 
     */
    public void downloadAllLogs() throws IOException { 
        SSHCommands.downloadRemoteFile(server, Utils.getServerLogRemoteFilename(server), 
                Utils.getServerLocalFilename());
        System.out.println("[INFO] Server log file " + Utils.getServerLocalFilename() + 
                " is locally downloaded. Please check it." );
        int counter=-1;
        for(Iterator it=clients.iterator(); it.hasNext(); ) { 
            Client c = (Client)it.next();
            SSHCommands.downloadRemoteFile(c, Utils.getClientLogRemoteFilename(c), 
                    Utils.getClientLocalFilename(++counter));
        }
    }
    
    /**
     * 
     * Join the status threads;
     */
    public void joinAllThreads() {
        this.cct.setFinished(true);
        this.wst.setFinished(true);
        this.cmt.setFinished(true);
        this.crpt.setFinished(true);
        try {
            this.cct.join();
            this.wst.join();
            this.cmt.join();
            this.crpt.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 
     * @throws TransportException
     * @throws IOException 
     */
    public void deleteClientPreviouslyMessages() 
            throws TransportException, IOException { 
        for(Iterator it=clients.iterator(); it.hasNext(); ) {
            Client c = (Client)it.next();
            c.deletePreviousRemoteMessages();
            // also the logs
            SSHCommands.deleteRemoteFile(c, "log*.data");
        }
    }
    
    /**
     * Print information about server and all clients.
     * 
     * @return 
     */
    public String printMachines() { 
        StringBuilder sb = new StringBuilder();
        sb.append("\nServer: "); 
        sb.append("\n\t address: ");
        sb.append(server.getIpAddress()).append(":").append(server.getPort());
        sb.append("\n\t ssh info: ");
        sb.append(server.getSSHUsername()).append(" ").append(server.getSSHPassword());
        sb.append("\n\t running params: "); 
        sb.append(server.getServerType()).append(" ");
        sb.append(server.getServerMode()).append(" ");
        sb.append(server.getServerHttpPort());
        sb.append("\nClients:");
        
        int counter = -1;
        for(Iterator it=clients.iterator(); it.hasNext(); ) {
            Client c = (Client)it.next();
            ++counter;
            sb.append("\n\tClient ").append(counter);
            sb.append("\n\t\t address:");
            sb.append(c.getIpAddress()).append(":").append(c.getPort());
            sb.append("\n\t\t ssh info: ");
            sb.append(c.getSSHUsername()).append(" ").append(c.getSSHPassword());
            sb.append("\n\t\t server info: ");
            sb.append(c.getServerIpAddress()).append(" ").append(c.getServerPort());
            sb.append("\n\t\t running params: "); 
            sb.append(c.getNoThreads() + " " + c.getDelay() + " " + c.getNoReq());
            sb.append("\n\t\t running tests: "); 
            sb.append(c.testsToString());
        }
        sb.append("\n");
        return sb.toString();
    }
}