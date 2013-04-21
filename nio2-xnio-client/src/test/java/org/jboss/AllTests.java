package org.jboss;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;
import java.util.Properties;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ClientDelayTest.class })
public class AllTests {

    public static String HOSTNAME;
    public static int PORT;
    public static int NUM_THREADS;
    public static int NUM_REQUESTS;

    @BeforeClass
    public static void setupAllTests() throws IOException{

        Properties props = new Properties();
        props.load(ClientDelayTest.class.getClassLoader().getResourceAsStream("test.properties"));

        HOSTNAME = props.getProperty("hostname");
        PORT = Integer.valueOf(props.getProperty("port"));
        NUM_THREADS = Integer.valueOf(props.getProperty("numthreads"));
        NUM_REQUESTS = Integer.valueOf(props.getProperty("numrequests"));
    }
}
