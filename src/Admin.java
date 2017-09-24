import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Admin {

    private String hostName;
    private int portNumber;
    private Socket kkSocket;
    private PrintWriter out;
    private BufferedReader in;

    public Admin(String hostName, int portNumber){
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public void connect() throws Exception {
        this.kkSocket = new Socket(hostName, portNumber);
        this.out = new PrintWriter(this.kkSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.kkSocket.getInputStream()));
    }

    public void communicate() throws Exception {
        String fromServer;
        while ((fromServer = in.readLine()) != null) {
            System.out.println("Server: " + fromServer);
            Registry reg = LocateRegistry.getRegistry("192.168.124.1",3333);
            HackService service=(HackService)reg.lookup("HackService");
            service.setHash(fromServer);
            if (service.hackear()){
                out.println(service.getAnswer());
            } else{
                System.out.println("No se encontró el password asociado al hash");
                break;
            }
        }
    }

    public static void main(String args[]) {
        try {
            System.out.println("Creando nuevo cliente...");
            Admin client = new Admin(args[0], Integer.parseInt(args[1]));
            System.out.println("Cliente creado exitosamente");
            System.out.println("Conectando...");
            client.connect();
            System.out.println("Conexion exitosa");
            System.out.println("Comunicando con " + args[0] + " a traves del puerto " + args[1]);
            client.communicate();
        } catch (Exception e) {
            System.out.println("Ocurrio un error! Consulte: " + e.getCause());
        }
    }
}
