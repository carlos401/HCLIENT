/**
 * UNIVERSIDAD DE COSTA RICA
 * ESCUELA DE CIENCIAS DE LA COMPUTACION E INFORMATICA
 * CI-1310 SISTEMAS OPERATIVOS
 * SEGUNDA TAREA PROGRAMADA, CARLOS DELGADO ROJAS (B52368)
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Subordinate extends UnicastRemoteObject implements HackService {

    private String hash; //the hash to be hack
    private String answer; //if this subordinate found the password
    private String fileToRead; //the name of the file
    private Boolean found; //stop signal

    /**
     * The constructor
     * @param file the name of the file with passwords
     * @throws RemoteException
     */
    public Subordinate (String file) throws RemoteException, FileNotFoundException{
        File archivo = new File(file);
        if (archivo.exists()){
            this.fileToRead = file;
        }else{
            throw new FileNotFoundException();
        }
        this.found = false;
    }

    @Override
    public void setHash(String hash) throws RemoteException {
        this.hash = hash;
    }

    @Override
    public boolean hackear() throws RemoteException{
        System.out.println(">> An instruction has been received, hacking password");
        try (BufferedReader br = new BufferedReader(new FileReader(fileToRead))) {
            String line; //where the line is buffered
            while ((line = br.readLine()) != null && !found) {
                List<String> list = new ArrayList<>(200);
                list.add(line);
                while((list.size()<200)&&((line=br.readLine())!=null) && !found){
                    list.add(line);
                }
                if (!found){
                    HasherThread hst = new HasherThread(list); //for each 200 lines, creates a hasher thread
                    hst.start();
                    if ((list.size()<200)){
                        hst.join();
                    }
                }else{
                    return true;
                }
            }
            return found;
        } catch (Exception e){
            System.out.print(e.getMessage());
            return false;
        }
    }

    @Override
    public String getAnswer() throws RemoteException {
        return this.answer;
    }

    @Override
    public void stop() throws RemoteException {
        //cierra cosas y bye bye
    }

    public static void main (String args[]) throws Exception
    {
        try{
            Registry reg = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            // Create an instance of our power service server ...
            Subordinate svr = new Subordinate(args[1]);
            // ... and bind it with the RMI Registry
            reg.rebind ("HackService", svr);
            System.out.println (">> Service bound....");
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    class HasherThread extends Thread{

        List<String> lines;//

        /**
         * The constructor
         * @param lines the lines to be hack
         */
        public HasherThread(List<String> lines){
            this.lines = lines;
        }

        @Override
        public void run(){
            Iterator<String> ite = this.lines.iterator();
            String line;
            while (ite.hasNext() && !found) {
                try{
                    line = ite.next();
                    if (hash.equals(getHash(line))) {
                        found = true;
                        answer = line;
                        System.out.println(">> Password found: "+line);
                    }
                } catch (Exception e){
                    System.out.println(">> Error con las comparaciones hash");
                }
            }
        }
    }

    /**
     * Function that returns the hash associate with a string
     * @param s the string to hash
     * @return the hash associate
     * @throws NoSuchAlgorithmException
     */
    public static String getHash(String s) throws NoSuchAlgorithmException {
        MessageDigest m= MessageDigest.getInstance("MD5");
        m.update(s.getBytes(),0,s.length());
        return new BigInteger(1,m.digest()).toString(16);
    }
}
