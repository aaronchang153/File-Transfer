package networking;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends NetworkCommunicator implements Runnable, Closeable {

    private Socket sock;
    private ObjectOutputStream object_out;
    private ObjectInputStream object_in;
    private String directory; // name of the directory being sent over
    private String destination; // destination directory
    private ArrayList<String> filelist;

    public Client(String hostname, int port_num){
        try {
            sock = new Socket(hostname, port_num);
            object_out = new ObjectOutputStream(sock.getOutputStream());
            object_in = new ObjectInputStream(sock.getInputStream());
        }
        catch(Exception e){
            e.printStackTrace();
            sock = null;
        }
    }

    public void retrieveFileList(){
        try{
            sendRequest(object_out, NetworkCommunicator.FILE_LIST);
            filelist = recvFileList(object_in);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void retrieveDirectoryName(){
        try{
            sendRequest(object_out, NetworkCommunicator.DIRECTORY_NAME);
            directory = recvString(object_in);
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
        try(DataInputStream socket_in = new DataInputStream(sock.getInputStream())){
            File file;
            long file_size;
            for(String currentFile : filelist){
                System.out.println("Retrieving file " + destination + directory + currentFile);
                file = new File(destination + directory + currentFile);
                sendRequest(object_out, NetworkCommunicator.FILE);
                sendString(object_out, currentFile);
                file_size = recvFileSize(object_in);
                receiveFile(socket_in, file, file_size);
            }
            sendRequest(object_out, NetworkCommunicator.END);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void close(){
        try {
            sock.close();
            object_in.close();
            object_out.close();
        }
        catch(IOException ignored) {}
    }
}
