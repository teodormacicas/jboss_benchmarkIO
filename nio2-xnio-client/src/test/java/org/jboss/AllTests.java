package org.jboss;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ClientDelayTest.class })
public class AllTests {
    public static final String HOSTNAME = "localhost";
    public static final int PORT = 8081;
    public static final int NUM_THREADS = 10;
    public static final int NUM_REQUESTS = 100;
}
