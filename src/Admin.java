/**
 * UNIVERSIDAD DE COSTA RICA
 * ESCUELA DE CIENCIAS DE LA COMPUTACION E INFORMATICA
 * CI-1310 SISTEMAS OPERATIVOS
 * SEGUNDA TAREA PROGRAMADA, CARLOS DELGADO ROJAS (B52368)
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class Admin {

    private String hostName;
    private int portNumber;
    private Socket kkSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String password;
    private List<Thread> subordinates;

    /**
     * The constructor
     * @param hostName the host to connect to a server
     * @param portNumber the port to connect to a server
     */
    public Admin(String hostName, int portNumber){
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.subordinates = new LinkedList<>();
    }

    /**
     * Allows connection to a server
     * @throws Exception
     */
    public void connect() throws Exception {
        this.kkSocket = new Socket(hostName, portNumber);
        this.out = new PrintWriter(this.kkSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.kkSocket.getInputStream()));
    }

    /**
     * Allows get a hash password from the server
     * @throws Exception
     */
    public void communicate() throws Exception {
        String fromServer;
        if ((fromServer = in.readLine()) != null) {
            System.out.println("Hash to be hack: " + fromServer);
            this.password = fromServer;
        } else{
            System.out.println("The server did not sent a password");
            System.out.println("command >>");
        }
    }

    /**
     * Connect a subordinate to this server
     * @param host
     * @param port
     * @throws Exception
     */
    public void connectSubordinate(String host, int port) throws Exception{
        Thread conect = new ConnectionThread(host,port);
        conect.start();
        this.subordinates.add(conect);
    }

    /**
     * Function that divide the users commands into tokens
     * @param comando the user command
     * @return A list that has the tokens
     */
    public List<String> divide_command(String comando){
        StringTokenizer st = new StringTokenizer(comando," ");
        List<String> comands = new LinkedList<>(); //this is the answered List
        while (st.hasMoreElements()){
            comands.add(st.nextToken()); //and adding the parameters
        }
        return comands;
    }

    public static void main(String args[]) {
        try{
            System.out.println("Notice: Creating client");
            Admin client = new Admin(args[0], Integer.parseInt(args[1]));
            System.out.println("Notice: Creation successful");
            System.out.println("Notice: Connecting to server");
            client.connect();
            System.out.println("Notice: Connection successful");
            System.out.println("Notice: You are connected with " + args[0] + " through port: " + args[1]);

            String commandLine; //to read console line
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("command >> ");
                commandLine = console.readLine();
                switch (commandLine) { //add more admin commands here
                    case "exit":
                        System.out.println("Shutting down");
                        System.exit(0);
                    case "hash":
                        client.communicate();
                        break;
                    case "man":
                        System.out.println("Use the command [exit] to shutdown this client");
                        System.out.println("Use the command [hash] to get a hash from the server");
                        System.out.println("Use the command [subordinate X1 X2]  to connect a Subordinate in the host [X1], port [X2]");
                        break;
                    default:
                        List<String> tokens = client.divide_command(commandLine); //tokenized input
                        if (tokens.get(0).equals("subordinate") && client.password != null){
                            client.connectSubordinate(tokens.get(1),Integer.parseInt(tokens.get(2)));
                        } else if(client.password != null){
                            System.out.println("Before this, get a hash from server");
                            System.out.print("command >>");
                        } else{
                            System.out.println("Unknown command");
                            System.out.print("command >>");
                        }
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Ocurrio un error! Consulte: " + e.getCause());
        }
    }

    class ConnectionThread extends Thread{

        private String host;
        private int port;

        /**
         * The constructor
         * @param host the host of the subordinate to connect
         * @param port the port of the subordinate
         */
        public ConnectionThread(String host,int port){
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            try{
                Registry reg = LocateRegistry.getRegistry(host,port);
                HackService service=(HackService)reg.lookup("HackService");
                service.setHash(password);
                if (service.hackear()){
                    out.println(service.getAnswer());
                    System.out.println("Server: " + in.readLine());
                    System.out.print("command >>");
                } else{
                    System.out.println("No se encontró el password asociado al hash");
                    System.out.print("command >>");
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
}
