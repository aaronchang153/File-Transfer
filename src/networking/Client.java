package networking;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends NetworkCommunicator implements Runnable, Closeable {

    private Socket sock;
    private String directory; // name of the directory being sent over
    private String destination; // destination directory
    private ArrayList<String> filelist;

    public Client(String hostname, int port_num){
        try {
            sock = new Socket(hostname, port_num);
        }
        catch(Exception e){
            e.printStackTrace();
            sock = null;
        }
    }

    public void retrieveFileList(){
        try(ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream()))
        {
            sendRequest(out, Request.FILE_LIST);
            filelist = recvFileList(in);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void retrieveDirectoryName(){
        try(ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream()))
        {
            sendRequest(out, Request.DIRECTORY_NAME);
            directory = recvString(in);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void setDirectory(String directory){
        this.directory = directory;
    }

    public void setDestination(String destination){
        this.destination = destination;
    }

    @Override
    public void run(){
        try(DataInputStream socket_in = new DataInputStream(sock.getInputStream());
            ObjectInputStream object_in = new ObjectInputStream(sock.getInputStream());
            ObjectOutputStream object_out = new ObjectOutputStream(sock.getOutputStream()))
        {
            File file;
            long file_size;
            for(String currentFile : filelist){
                System.out.println("Retrieving file " + destination + directory + currentFile);
                file = new File(destination + directory + currentFile);
                sendRequest(object_out, Request.FILE);
                file_size = recvFileSize(object_in);
                receiveFile(socket_in, file, file_size);
            }
            sendRequest(object_out, Request.END);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void close(){
        try {
            sock.close();
        }
        catch(IOException ignored) {}
    }
}
