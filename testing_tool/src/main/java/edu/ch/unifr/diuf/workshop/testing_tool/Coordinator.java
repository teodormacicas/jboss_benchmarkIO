package edu.ch.unifr.diuf.workshop.testing_tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.schmizz.sshj.transport.TransportException;
import org.apache.commons.configuration.ConfigurationException;

/**
 * This object is supposed to control everything it is running.
 *
 * @author Teodor Macicas
 */
public class Coordinator 
{   
    private final static Logger LOGGER = Logger.getLogger(
            Coordinator.class.getName());
    
    public static void main(String... args) {
        MachineManager mm = new MachineManager();
        
        try {
            System.out.println("Parsing properties file ...");
            mm.parsePropertiesFile();
            
        }
        catch( WrongIpAddressException ex ) {
            LOGGER.log(Level.SEVERE, "Exception setting up a machine.", ex);
            System.exit(1);
        }
        catch( WrongPortNumberException ex2 ) {
            LOGGER.log(Level.SEVERE, "Exception setting up a machine.", ex2);
            System.exit(2);
        }
        catch( ClientNotProperlyInitException ex3 ) {
            LOGGER.log(Level.SEVERE, "Exception setting up a machine.", ex3);
            System.exit(3);
        }
        catch( ConfigurationException ex4 ) { 
            LOGGER.log(Level.SEVERE, "Exception reading the properties file.", ex4);
            System.exit(4);
        }
        catch( FileNotFoundException ex5 ) {
            LOGGER.log(Level.SEVERE, "Either server local program or the client"
                    + " local program could not be found.", ex5);
            System.exit(5);
        }
        // print the machines that have been created according to the properties file
        System.out.println(mm.printMachines());
        
        // are both server and clients set up?
        if( ! mm.checkIfClientAndServerSet() ) { 
            LOGGER.severe("Either clients or the server is not yet configured. "
                    + "Please do so before you start once again.");
            System.exit(6);
        }
        // are either all or none loopback addresses used?
        if( ! mm.checkIfAllOrNoneLoopbackAddresses() ) { 
            LOGGER.severe("Please either use loopback addresses for all clients "
                    + "and server OR non-loopback for all machines. This will be "
                    + "more probably they can reach other.");
            System.exit(7);
        }
        
        System.out.println("Checking if all clients can ping the server ...");
        try {
            // can all clients, at least, ping the server?
            if( ! mm.checkClientsCanAccessServer() ) {
                LOGGER.severe("Not all clients can ping the server. Check once again "
                        + "the IP addresses and/or the network status.");
                System.exit(8);
            }
        } catch (TransportException ex) {
            LOGGER.log(Level.SEVERE, "Exception catched while checking clients "
                    + "network connection to the server.", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception catched while checking clients "
                    + "network connection to the server.", ex);
        }
        System.out.println("Deleting any data from previous run ...");
        // delete from each client the files that might have been used before for 
        // sending different messages
        try {
            mm.deleteClientPreviouslyMessages();
        } catch (TransportException ex) {
            LOGGER.log(Level.SEVERE, "Exception raised while deleting old messages "
                    + "files from clients.", ex);
            System.exit(11);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception raised while deleting old messages "
                    + "files from clients.", ex);
            System.exit(12);
        }
        System.out.println("Starting the threads for checking connectivity and status ...");
       // now start the connectivity and status threads       
        mm.startStatusThreads();
        
        System.out.println("Checking if all machines are in a runnable state ...");
        // check if all are ok ... 
        try {
            // sleep a bit before checking, to allow some time for the coordinator 
            // to contact each client
            Thread.sleep(2000);
            int retries = 10;
            while( ! mm.allAreOk() && retries > 0 ) {
                --retries;
                LOGGER.info("There are some machines that have either ssh problems "
                    + "or just connectivity problems. Wait and retry (left "
                    + "#retries: " + retries + ").");
                Thread.sleep(10000);
            }   
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    
        // upload the programs to clients and to the server
        try {
            System.out.println("Start uploading the program to clients ...");
            mm.uploadProgramToClients();
            System.out.println("Start uploading the program to server ...");
            mm.uploadProgramToServer();
        } catch (TransportException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                System.exit(9);
        } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                System.exit(10);
        }
        
        System.out.println("Start the server ... ");
        int serverPID;
        try {
            // run the server remotely 
            serverPID = mm.getServer().runServerRemotely();
            System.out.println("Server remote PID is " + serverPID);
        } catch (TransportException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        //TODO
        System.out.println("Start the clients ...  TODO !!!");
        
        // synchronization and result gathering TODO !! 
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } 
        
        // now join the connectivity threads
        mm.joinStatusThreads(); 
        
        // maybe delete here the client & server files ...
    }
    
}
