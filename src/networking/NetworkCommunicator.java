package networking;

import java.io.*;
import java.util.ArrayList;

public class NetworkCommunicator {
    /**
     * Provides methods and variables to allow the server and client to
     * communicate with each other
     *
     * Both Server and Client should extend this class so that
     * they always use the exact same enum for requests.
     *
     * Send enum value as:
     *      int ordinal = Request.FILE.ordinal();
     *
     * Retrieve enum value as:
     *      Request.values[ordinal];
     */

    public enum Request{END, DIRECTORY_NAME, FILE_LIST, FILE}

    private static final int BUFFER_SIZE = 4096;

    public void sendFile(DataOutputStream out, File src) throws IOException {
        try(FileInputStream file_in = new FileInputStream(src))
        {
            byte[] buffer = new byte[BUFFER_SIZE];
            while(file_in.read(buffer) > 0){
                out.write(buffer);
            }
        }
        catch(IOException e){
            e.printStackTrace();
            throw e;
        }
    }

    public void receiveFile(DataInputStream in, File dst, long size) throws IOException {
        File parent = new File(dst.toString().substring(0, dst.toString().lastIndexOf("\\")));
        if(!parent.exists()){
            // If the parent directory/directories for the file do not exist, create them.
            parent.mkdirs();
        }
        dst.createNewFile();
        try(FileOutputStream file_out = new FileOutputStream(dst))
        {
            byte[] buffer = new byte[BUFFER_SIZE];
            long remaining = size;
            int read;
            while(remaining > 0){
                read = in.read(buffer, 0, (int) Math.min(remaining, BUFFER_SIZE));
                file_out.write(buffer, 0, read);
                remaining -= read;
            }
        }
        catch(IOException e){
            e.printStackTrace();
            if(dst.exists()){
                // Deletes the incomplete file if the download was interrupted
                dst.delete();
            }
            throw e;
        }
    }

    public void sendRequest(ObjectOutputStream out, Request request) throws IOException{
        int ordinal = request.ordinal();
        out.writeInt(ordinal);
    }

    public Request recvRequest(ObjectInputStream in) throws IOException, ArrayIndexOutOfBoundsException{
        int ordinal = in.readInt();
        return Request.values()[ordinal];
    }

    public void sendFileSize(ObjectOutputStream out, long data) throws IOException{
        out.writeLong(data);
    }

    public long recvFileSize(ObjectInputStream in) throws IOException{
        return in.readLong();
    }

    public void sendString(ObjectOutputStream out, String data) throws IOException{
        out.writeObject(data);
    }

    public String recvString(ObjectInputStream in) throws IOException{
        String received = null;

        try{
            received = (String) in.readObject();
        }
        catch(ClassNotFoundException ignored) { }

        return received;
    }

    public void sendFileList(ObjectOutputStream out, ArrayList<String> data) throws  IOException{
        out.writeObject(data);
    }

    public ArrayList<String> recvFileList(ObjectInputStream in) throws IOException{
        ArrayList<String> received = null;

        try {
            received = (ArrayList<String>) in.readObject();
        }
        catch(ClassNotFoundException ignored) { }

        return received;
    }
}
