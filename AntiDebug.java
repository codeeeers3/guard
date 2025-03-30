import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;

public class AntiDebug {

    private static final List<String> DEBUGGER_PROCESSES = List.of(
        "jdb", "jstack", "jmap", "jinfo", "hprof", "trace", "strace", "lsof", "gdb", "lldb"
    );

    public static void main(String[] args) {
        if (isDebuggerAttached() || isDebuggerProcessRunning() || isDebugPortOpen()) {
            System.out.println("Debugger detected! Terminating program...");
            System.exit(1);
        } else {
            System.out.println("No debugger detected. Continuing execution...");
            // Your program logic here
        }
    }

    private static boolean isDebuggerAttached() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");
    }

    private static boolean isDebuggerProcessRunning() {
        for (String process : getRunningProcesses()) {
            for (String debugger : DEBUGGER_PROCESSES) {
                if (process.toLowerCase().contains(debugger)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> getRunningProcesses() {
        List<String> processes = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("ps aux");
            try (Scanner scanner = new Scanner(process.getInputStream())) {
                while (scanner.hasNextLine()) {
                    processes.add(scanner.nextLine());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processes;
    }

    private static boolean isDebugPortOpen() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.isReachable(5000);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
