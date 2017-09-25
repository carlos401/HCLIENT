/**
 * UNIVERSIDAD DE COSTA RICA
 * ESCUELA DE CIENCIAS DE LA COMPUTACION E INFORMATICA
 * CI-1310 SISTEMAS OPERATIVOS
 * SEGUNDA TAREA PROGRAMADA, CARLOS DELGADO ROJAS (B52368)
 */

import java.rmi.RemoteException;

public interface HackService extends java.rmi.Remote{

    void setHash(String hash) throws RemoteException;

    boolean hackear() throws RemoteException;

    String getAnswer () throws RemoteException;

    void stop() throws RemoteException;

}
