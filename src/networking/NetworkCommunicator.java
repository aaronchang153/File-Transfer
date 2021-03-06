package networking;

import java.io.*;
import java.util.ArrayList;

public class NetworkCommunicator {
    /**
     * Provides methods and variables to allow the server and client to
     * communicate with each other
     *
     * Both Server and Client should extend this class so that
     * they always use the exact same format for requests.
     */

    public static final byte END = 0;
    public static final byte DIRECTORY_NAME = 1;
    public static final byte FILE_LIST = 2;
    public static final byte FILE = 3;

    private static final int BUFFER_SIZE = 4096;

    public void sendFile(DataOutputStream out, File src) throws IOException {
        try(FileInputStream file_in = new FileInputStream(src))
        {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while((read = file_in.read(buffer)) > 0){
                out.write(buffer, 0, read);
            }
            out.flush();
        }
        catch(IOException e){
            e.printStackTrace();
            throw e;
        }
    }

    public void receiveFile(DataInputStream in, File dst, long size) throws IOException {
        dst.getParentFile().mkdirs();
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

    public void sendRequest(ObjectOutputStream out, byte request) throws IOException{
        out.writeByte(request);
        out.flush();
    }

    public byte recvRequest(ObjectInputStream in) throws IOException, ArrayIndexOutOfBoundsException{
        byte request = in.readByte();
        if(request < END || request > FILE)
            // Throw ArrayIndexOutOfBoundsException on invalid request to stay consistent
            // with an old version of this method
            throw new ArrayIndexOutOfBoundsException();
        return request;
    }

    public void sendFileSize(ObjectOutputStream out, long data) throws IOException{
        out.writeLong(data);
        out.flush();
    }

    public long recvFileSize(ObjectInputStream in) throws IOException{
        return in.readLong();
    }

    public void sendString(ObjectOutputStream out, String data) throws IOException{
        out.writeObject(data);
        out.flush();
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
        out.flush();
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
