import com.intel.bluetooth.EmulatorTestsHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BluetoothTest {
    private static final UUID uuid = new UUID(0x2108);

    private static Thread serverThread;

    private static final String echoGreeting = "I echo";
    @BeforeAll
    static void setUp() throws Exception {
        EmulatorTestsHelper.startInProcessServer();
        EmulatorTestsHelper.useThreadLocalEmulator();
        serverThread = EmulatorTestsHelper.runNewEmulatorStack(new EchoServerRunnable());
    }
    @AfterAll
    static void tearDown() throws Exception {
        if ((serverThread != null) && (serverThread.isAlive())) {
            serverThread.interrupt();
            serverThread.join();
        }
        EmulatorTestsHelper.stopInProcessServer();
    }

    private static class EchoServerRunnable implements Runnable {

        public void run() {

            StreamConnectionNotifier service = null;

            try {
                String url = "btspp://localhost:" + uuid.toString() + ";name=TServer";
                service = (StreamConnectionNotifier) Connector.open(url);
                StreamConnection conn = (StreamConnection) service.acceptAndOpen();
                System.out.println("Server received connection");
                DataOutputStream dos = conn.openDataOutputStream();
                DataInputStream dis = conn.openDataInputStream();
                dos.writeUTF(echoGreeting);
                dos.flush();
                String received = dis.readUTF();
                System.out.println("Server received:" + received);
                dos.writeUTF(received);
                dos.flush();
                dos.close();
                dis.close();
                conn.close();
            } catch (Throwable e) {
                System.err.print(e.toString());
                e.printStackTrace();
            } finally {
                if (service != null) {
                    try {
                        service.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }
    @Test
    public void testConnection() throws Exception {
        DiscoveryAgent discoveryAgent = LocalDevice.getLocalDevice().getDiscoveryAgent();
        // Find service
        String serverURL = discoveryAgent.selectService(uuid, ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
        assertNotNull("service not found", serverURL);

        StreamConnection conn = null;
        try {
            conn = (StreamConnection) Connector.open(serverURL);
            DataOutputStream dos = conn.openDataOutputStream();
            DataInputStream dis = conn.openDataInputStream();

            String received = dis.readUTF();
            assertEquals("handshake", echoGreeting, received);

            String message = "TestMe";
            System.out.println("Client Sending message:" + message);
            dos.writeUTF(message);

            received = dis.readUTF();
            assertEquals("echo", received, message);
            dos.close();
            dis.close();
        } catch (IOException e) {
            System.err.print(e.toString());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}

