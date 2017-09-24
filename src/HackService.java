import java.rmi.RemoteException;

public interface HackService extends java.rmi.Remote{

    void setHash(String hash) throws RemoteException;

    boolean hackear() throws RemoteException;

    String getAnswer () throws RemoteException;

    void stop() throws RemoteException;

}
