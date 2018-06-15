package networking;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server extends NetworkCommunicator implements Runnable, Closeable {
    /**
     * Responsible for sending a file/directory to a Client over a network.
     */

    public enum Status{IDLE, ACCEPTING, CLIENT_CONNECTED, FINISHED}

    private Status status;
    private boolean closed; // False until the close() method is called

    private ServerSocket server_socket;
    private Socket sock;
    private String directory;
    private ArrayList<String> filelist;

    public Server(int port_num) throws IOException {
        server_socket = new ServerSocket(port_num);
        status = Status.IDLE;
        closed = false;
    }

    public Status getStatus(){
        return status;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String name){
        directory = name;
    }

    public boolean generateFileList(){
        filelist = new ArrayList<>();
        File f = new File(directory);
        if(f.exists()){
            File[] files = f.listFiles(); // Returns null if dir isn't a directory (i.e. is a file)
                                          // (may be useful when trying to transfer a single file)
            if(files != null) {
                for (File current : files) {
                    if(current.isFile()){
                        // Removes the parent directory from the file path and adds it to
                        // the file list
                        filelist.add(current.toString().replace(directory, ""));
                    }
                    else if(current.isDirectory()){
                        if(!generateFileList(current)){
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        else{
            return false;
        }
    }

    private boolean generateFileList(File dir){
        // Recursively called by generateFileList(). Does mostly the same thing with some
        // important changes (e.g. does not create a new ArrayList).
        if(dir.exists()){
            File[] files = dir.listFiles();
            if(files != null) {
                for (File current : files) {
                    if(current.isFile()){
                        filelist.add(current.toString().replace(directory, ""));
                    }
                    else if(current.isDirectory()){
                        if(!generateFileList(current)){
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        else{
            return false;
        }
    }

    private void server_mainloop(){
        try(DataOutputStream socket_out = new DataOutputStream(sock.getOutputStream());
            ObjectInputStream object_in = new ObjectInputStream(sock.getInputStream());
            ObjectOutputStream object_out = new ObjectOutputStream(sock.getOutputStream()))
        {
            String received;
            File file;
            Request request;
            long file_size;
            status = Status.CLIENT_CONNECTED;
            while (status == Status.CLIENT_CONNECTED) {
                request = recvRequest(object_in); // Will throw ArrayIndexOutOfBoundsException if the server
                                                  // receives an invalid request (i.e. server and client are no
                                                  // longer synchronized for whatever reason).
                switch (request) {
                    case END:
                        // End current connection and go back to waiting for another client to connect
                        status = Status.ACCEPTING;
                        break;
                    case DIRECTORY_NAME:
                        sendString(object_out, directory);
                        break;
                    case FILE_LIST:
                        sendFileList(object_out, filelist);
                        break;
                    case FILE:
                        received = recvString(object_in);
                        file = new File(directory + received);
                        if(file.exists() && file.isFile()){
                            file_size = file.length();
                            sendFileSize(object_out, file_size);
                            sendFile(socket_out, file);
                        }
                        break;
                }
            }
        }
        catch(ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            System.err.println("An invalid request was made. Continuing transfer will result in undefined behavior.");
            System.err.println("Make sure the client is actually requesting something defined in the Request enum.");
            status = Status.ACCEPTING;
        }
        catch(IOException e){
            e.printStackTrace();
            status = Status.ACCEPTING;
        }
        finally{
            System.out.println("Closing connection");
            closeConnection();
        }
    }

    private void closeConnection(){
        // Like close(), but this only closes sock. Leaves server_socket alone.
        try {
            if (sock != null) {
                sock.close();
            }
        }
        catch(IOException ignored) { }
        finally{
            sock = null;
        }
    }

    @Override
    public void close(){
        // If the Server object is closed while inside its accept() loop, the Thread it's
        // running in will throw a SocketException. That exception should be handled.
        try{
            if(sock != null){
                sock.close();
            }
            if(server_socket != null){
                server_socket.close();
            }
        }
        catch(IOException ignored){ }
        finally{
            closed = true;
        }
    }

    @Override
    public void run(){
        status = Status.ACCEPTING;
        try{
            while(status == Status.ACCEPTING) {
                sock = server_socket.accept();
                System.out.println("Client Connected");
                server_mainloop();
            }
        }
        catch(IOException e){
            if(!closed) {
                // If the exception wasn't caused by calling the close() method
                e.printStackTrace();
            }
        }
        finally{
            status = Status.FINISHED;
        }
    }


//    public static void main(String[] args) throws IOException{
        // Test Main
        /*
        // Testing how to stop the server after it started

        Server server = new Server(8000);
        server.setDirectory("D:\\[Documents]\\Java");
        server.generateFileList();
        for(String s : server.filelist){
            System.out.println(s);
        }
        Thread t = new Thread(server);
        t.start();
        server.close();
        */

        /*
        // Testing how to create a new file from the pathname alone

        String pathname = "D:\\[Documents]\\Java\\Test\\a.txt";
        String parent = pathname.substring(0, pathname.lastIndexOf('\\'));
        File p = new File (parent);
        File f = new File (pathname);

        if(!p.exists()){
            p.mkdirs();
        }
        f.createNewFile();
        */

        /*
        System.out.println(getLocalHost().getHostAddress()); // 192.168.1.237
        System.out.println(getLocalHost().getHostName());    // DESKTOP-GCCELNJ
        */
//    }

}
