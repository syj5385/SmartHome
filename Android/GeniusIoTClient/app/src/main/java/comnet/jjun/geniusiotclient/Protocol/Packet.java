package comnet.jjun.geniusiotclient.Protocol;

/**
 * Created by comm on 2018-01-30.
 */

public class Packet {
    private String header;
    private int sizeofData;
    private int command;
    private byte[] parameter;
    private byte checksum;

    public void setHeader(String header){
        this.header = header;
    }

    public void setSizeofData(int sizeofData){
        this.sizeofData = sizeofData;
    }

    public void setCommand(int command){
        this.command = command;
    }

    public void setParameter(byte[] parameter){
        this.parameter = new byte[sizeofData];
        for(int i=0; i<parameter.length ; i++){
            this.parameter[i] = parameter[i];
        }
    }

    public void setChecksum(byte checksum){
        this.checksum = checksum;
    }

    public int getSizeofData(){
        return sizeofData;
    }

    public int getCommand(){
        return command;
    }

    public byte[] getParameter(){
        return parameter;
    }
}
