package sr.ice.server;

import Demo.TestService;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class IceServer {
    public static void main(String[] args) {
        IceServer app = new IceServer();
        app.t1(args);
    }

    public void t1(String[] args) {
        int status = 0;
        Communicator communicator = null;

        try {
            communicator = Util.initialize(args);
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Adapter1", "tcp -h 127.0.0.2 -p 10000");
            TestService testServant = new TestServiceImpl();
            adapter.add(testServant, new Identity("test1", "test"));
            adapter.activate();

            System.out.println("Entering event processing loop...");

            communicator.waitForShutdown();

        } catch (Exception e) {
            e.printStackTrace(System.err);
            status = 1;
        }
        if (communicator != null) {
            try {
                communicator.destroy();
            } catch (Exception e) {
                e.printStackTrace(System.err);
                status = 1;
            }
        }
        System.exit(status);
    }
}