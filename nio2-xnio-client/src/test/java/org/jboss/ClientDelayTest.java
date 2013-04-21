package org.jboss;

import org.jboss.test.client.JioClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class ClientDelayTest {

    private int delay;

    public ClientDelayTest(int delay) {
        this.delay = delay;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {

        return Arrays.asList(new Object[][]{
                {250},
                {225},
                {200},
                {175},
                {150},
                {125},
                {100},
        });
    }

    @Test
    public void testWithDifferentDelays() throws Exception {

        JioClient.N_THREADS = AllTests.NUM_THREADS;
        JioClient clients[] = new JioClient[AllTests.NUM_THREADS];
        int numClientRequests = AllTests.NUM_REQUESTS / AllTests.NUM_THREADS;

        for (int i = 0; i < clients.length; i++) {
            clients[i] = new JioClient(AllTests.HOSTNAME, AllTests.PORT, numClientRequests, delay);
        }

        for (int i = 0; i < clients.length; i++) {
            clients[i].start();
        }

        for (int i = 0; i < clients.length; i++) {
            clients[i].join();
        }
    }


}
