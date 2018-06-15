package networking;


import java.util.Scanner;

public class ServerTest {

    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        int port;
        String directory;

        System.out.print("Enter a port number: ");
        port = input.nextInt();
        System.out.print("Enter the directory to send: ");
        directory = input.nextLine();
        input.next();

        try {
            System.out.println("Starting server.");
            Server server = new Server(port);
            server.setDirectory(directory);
            server.generateFileList();
            Thread server_thread = new Thread(server);
            System.out.println("Accepting Connections.");
            server_thread.start();
            server_thread.join();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        input.close();
    }
}
