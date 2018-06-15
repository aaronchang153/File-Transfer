package networking;


import java.util.Scanner;

public class ClientTest {
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        String hostname;
        String destination;
        int port;

        System.out.print("Enter a host name: ");
        hostname = input.nextLine();
        //input.next();
        System.out.print("Enter a port number: ");
        port = input.nextInt();
        System.out.print("Enter the destination directory: ");
        destination = input.nextLine();
        input.next();

        try{
            System.out.println("Connecting to server.");
            Client client = new Client(hostname, port);
            System.out.println("Getting directory name");
            client.retrieveDirectoryName();
            System.out.println("Retrieving file list");
            client.retrieveFileList();
            client.setDestination(destination);
            Thread client_thread = new Thread(client);
            System.out.println("Retrieving files...");
            client_thread.start();
            client_thread.join();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Done");
        input.close();
    }
}
