package networking;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ServerTest {

    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        int port;
        String directory;

        System.out.print("Enter a port number: ");
        port = input.nextInt();
        try {
            System.out.print("Enter the directory to send: ");
            // Scanner nextLine() doesn't read the next line for some reason
            directory = reader.readLine();

            try {
                System.out.println("Starting server.");
                Server server = new Server(port);
                server.setDirectory(directory);
                server.generateFileList();
                Thread server_thread = new Thread(server);
                System.out.println("Accepting Connections.");
                server_thread.start();
                server_thread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch(IOException ignored) { }
        input.close();
    }
}
