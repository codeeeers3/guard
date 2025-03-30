import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class EnhancedAntiDebug {

    private static final List<String> DEBUGGER_PROCESSES = List.of(
        "jdb", "jstack", "jmap", "jinfo", "hprof", "trace", "strace", "lsof", "gdb", "lldb", "debuggerd", "frida-server"
    );

    public static void main(String[] args) {
        if (isDebuggerAttached() || isDebuggerProcessRunning() || isDebugPortOpen() || isBeingTraced() || isFridaRunning() || isEmulator()) {
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
            String line;
            Process process = Runtime.getRuntime().exec("ps -e");
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while ((line = input.readLine()) != null) {
                    processes.add(line);
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
            for (int port = 5000; port <= 5100; port++) { // Checking a range of common debug ports
                try (Socket socket = new Socket(localhost, port)) {
                    return true;
                } catch (IOException ignored) {
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isBeingTraced() {
        String[] command = {"/bin/sh", "-c", "cat /proc/self/status | grep TracerPid"};
        try {
            Process process = Runtime.getRuntime().exec(command);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && !line.endsWith("0")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isFridaRunning() {
        for (String process : getRunningProcesses()) {
            if (process.toLowerCase().contains("frida-server")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEmulator() {
        String manufacturer = System.getProperty("ro.product.manufacturer");
        String model = System.getProperty("ro.product.model");
        return ("Genymotion".equals(manufacturer) || "Android SDK built for x86".equals(model));
    }
}
