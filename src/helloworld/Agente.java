/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloworld;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.internal.parser.JSONParser;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.eclipsesource.json.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.RoundingMode;
import java.util.ArrayList;


/**
 *
 * @author Dani
 */
public class Agente extends SingleAgent {
    private String loginKey;
    private int[][] lecturaRadar;
    private double[][] lecturaScanner;
    private double nivelBateria;
    private ACLMessage outbox;
    
    //Variables para la memoria
    private ArrayList<ArrayList<Integer>> Mapa;
    private int MenY;
    private int MenX;

    public Agente() throws Exception {
        super(null);
    }

    
    public Agente(AgentID aid) throws Exception {
        super(aid);
        this.loginKey = "";
        
        // Variables que guardan las lecturas de los sensores. Se actualizan tras cada movimiento del GugelCar
        this.lecturaRadar = new int[5][5];
        this.lecturaScanner = new double[5][5];
        this.nivelBateria = 0.0;  
        this.outbox = new ACLMessage();
        System.out.println("\n\n\nHola Mundo soy un agente llamado " + this.getName());
        
        //Prueba memoria
        this.InicializarMemoria();
        
    }
    
    //public void init();
    @Override
    public void execute(){

         setDestinatario("Bellatrix");
         login();
        // refuel();
        
        boolean exit = false;
        for (int i = 0; i < 49; i++) {
               makeMove("moveSW");
        }

        logout();    
        generarMapaTraza();
        
        this.verMapa(151,151);
        
        /*
        String [] array = null;
        int [] arrayInt = null;
        try {
            array = message.getString("radar").split(",");
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (int i = 0; i< array.length; i++){
             arrayInt[i] = Integer.getInteger(array[i]);
        }
                        
        int [][] matriz = transform(arrayInt, 5);
        */
}
    
/******************************************************************************
 * Funciones
 ******************************************************************************/
//git
    @Override
    public AgentID getAid() {
        return super.getAid(); //To change body of generated methods, choose Tools | Templates.
    }  
    /*
    * @autor Daniel, Nacho
    */
    public JSONObject getMessage(){
        
        JSONObject obj = null;
        try {
            //System.out.println("Recibiendo Traza");
            ACLMessage inbox = this.receiveACLMessage();
            System.out.println("Recibido mensaje " +inbox.getContent()+ " de "
                    +inbox.getSender().getLocalName());
            obj = new JSONObject(inbox.getContent());
            
             if(obj.has("result") && !obj.get("result").equals("CRASHED") 
                     && !obj.get("result").equals("BAD_COMMAND")
                     && !obj.get("result").equals("BAD_PROTOCOL")
                     && !obj.get("result").equals("BAD_KEY")){
                 
                 
                 obj.accumulate("radar", this.getMessage().get("radar"));
                 obj.accumulate("scanner", this.getMessage().get("scanner"));
                 obj.accumulate("battery", this.getMessage().get("battery"));
                 
                 
             }
         
        } catch (InterruptedException ex) {
            System.err.println("Error procesando traza");
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return obj;
    }
    /*
    * @author Ruben
    */
    private void Parseo(String respuesta){
        // Parseamos el String original y lo guardamos en un objeto
        JsonObject objeto = Json.parse(respuesta).asObject();
        
        // Extraemos los valores asociados a cada clave
        JsonArray vector = objeto.get("radar").asArray();
        JsonArray vector2 = objeto.get("scanner").asArray();
        this.nivelBateria = objeto.get("battery").asDouble();
        
        int aux = 0;
        
        // Actualizamos las lecturas de los sensores: el Radar y el Scanner
        for(int i=0; i < 5; i++){
            for(int j=0; j < 5; j++){
                this.lecturaRadar[i][j] = vector.get(aux).asInt();
                this.lecturaScanner[i][j] = vector2.get(aux).asDouble();
                aux++;
            }
        }
        
        // Mostramos los datos proporcionados por los sensores
        System.out.println("Los datos recibidos son: ");
        System.out.println("\n Radar: ");
        
        for(int i=0; i < 5; i++)
            System.out.println(this.lecturaRadar[i][0] + " " + this.lecturaRadar[i][1] + " " + 
                    this.lecturaRadar[i][2] + " " + this.lecturaRadar[i][3] + " " + this.lecturaRadar[i][4]);
        
        System.out.println("\n Scanner: ");
        
        for(int i=0; i < 5; i++)
            System.out.println(this.lecturaScanner[i][0] + " " + this.lecturaScanner[i][1] + " " + 
                    this.lecturaScanner[i][2] + " " + this.lecturaScanner[i][3] + " " + this.lecturaScanner[i][4]);
        
        System.out.println("\n Battery: ");
        System.out.println(this.nivelBateria);
    }
    
    /*
    * @author grego
    */
    
    //Inicializacion de memoria matriz 1000x1000 para no salirnos de los
    //limites maximo de mapa 500x500 "4" indica memoria libre o mapa sin descubrir
    
    private void InicializarMemoria(){
            
    this.Mapa = new ArrayList<ArrayList<Integer>>();
    
    //Nos posicionamos en el centro de la memoria
    this.MenY = 500;
    this.MenX = 500;

    for(int i = 0; i < 1000; i++){
        this.Mapa.add(new ArrayList<Integer>());
            for (int j = 0; j < 1000; j++){
                this.Mapa.get(i).add(4);
            }
    }
    System.out.println("Tengo memoria");
    }
    
    
    /*
    * @author grego
    */
    /*
    *Visisualiza el mapa desde el centro de la memoria
    *@pre para obtener un dato real usar la funcion con numeros impares
    *@param a {Ancho ncasillar} l {Alto ncasillas}
    */
    public void verMapa(int a, int l){
     
     
        //Delimito el centro
        int L = l/2 + 500;
        int A = a/2 + 500;   


        //Muestro los datos del centro de la memoria
        for(int i = 500 - l/2 ; i < L; i++){
            for (int j = 500 - a/2; j < A; j++){
                System.out.print(Mapa.get(i).get(j));
            }
            System.out.println();
        }

    }
    
    /*
    * @author grego, kudry
    */
    
   
    public void actuMapa(String movementCommand){
        
        //Contadores para la matriz del radar
        int conRadarI = 0;
        int conRadarJ = 0;
        
        //Ajusto mi posicion en funcion del movimiento
        if (movementCommand.equals("moveW") ){
            this.MenX--;
            System.out.println("Voy al Oeste");

        }else if (movementCommand.equals("moveE")){
            this.MenX++;
            System.out.println("Voy al Este");    

        }else if (movementCommand.equals("moveN")){
            this.MenY--;
            System.out.println("Voy al Norte");    

        }else if (movementCommand.equals("moveS")){
            this.MenY++;
            System.out.println("Voy al Sur");    
        }
      
        else if (movementCommand.equals("moveNW")){
            this.MenX--;
            this.MenY--;
            System.out.println("Voy al NorOeste");    
        
        }else if (movementCommand.equals("moveNE")){
            this.MenX++;
            this.MenY--;
            System.out.println("Voy al NorEste");    
        
        }else if (movementCommand.equals("moveSW")){
            this.MenX--;
            this.MenY++;
            System.out.println("Voy al NorOeste");    
        
        }else if (movementCommand.equals("moveSE")){
            this.MenX++;
            this.MenY++;
            System.out.println("Voy al NorOeste");    
        }        

        //Agrego la nueva informacion a la memoria
        for(int i = this.MenY -2 ; i < this.MenY + 3; i++){
            for (int j = this.MenX -2 ; j < this.MenX + 3; j++){
                Mapa.get(i).set(j,this.lecturaRadar[conRadarI][conRadarJ]);
                conRadarJ++;     
            }
            System.out.println();
            conRadarI++;
            conRadarJ = 0;     
        }
        
        // Ubico el coche en el mapa
        Mapa.get(this.MenY).set(this.MenX, 8);
        
        System.out.println("------ACTUALIZADO---------");
        
        //Muestro el mapa
        this.verMapa(40,40);

        
    }
    

    /*
    * @author Nacho
    */
    public void refuel(){
        
        System.out.println("\n\nRefuel");
        ACLMessage outbox = new ACLMessage(); 
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Bellatrix"));       
        JSONObject jsonRefuel=new JSONObject();
        
        try {
            jsonRefuel.put("command", "refuel");
            jsonRefuel.put("key", this.loginKey);
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        outbox.setContent(jsonRefuel.toString());
        System.out.println(jsonRefuel.toString());
        this.send(outbox);
        
        this.getMessage();
    }
    public void generarMapaTraza(){
          try {
           
             System.out.println("Recibiendo traza");
             ACLMessage inbox = this.receiveACLMessage();
             JsonObject injson = Json.parse(inbox.getContent()).asObject();
             JsonArray ja = injson.get("trace").asArray();
             
             byte data[] = new byte [ja.size()];
             for(int i = 0; i<data.length; i++){
                 data[i] = (byte) ja.get(i).asInt();
             }
             FileOutputStream fos = new FileOutputStream("mitraza.png");
             fos.write(data);
             fos.close();
             System.out.println("Traza Guardada en mitraza.png");

         } catch (InterruptedException | FileNotFoundException ex) {
             Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
    static int[][] transform(int[] arr, int N) {
      int M = (arr.length + N - 1) / N;
      int[][] mat = new int[M][];
      int start = 0;
      for (int r = 0; r < M; r++) {
          int L = Math.min(N, arr.length - start);
          mat[r] = java.util.Arrays.copyOfRange(arr, start, start + L);
          start += L;
      }
      return mat;
  }

     /*
    * @author Dani
    */
    public void setDestinatario(String nombre){
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID(nombre));   
    };
    /*
    * @author Dani
    */
    public boolean login(){
 
        JSONObject jsonLogin=new JSONObject();
        
        try {
            jsonLogin.put("command", "login");
            jsonLogin.put("world", "map1");
            jsonLogin.put("radar", "agentep3");
            jsonLogin.put("scanner", "agentep3");
            jsonLogin.put("battery", "agentep3"); 
            
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }

        outbox.setContent(jsonLogin.toString());
        System.out.println(jsonLogin.toString());
        this.send(outbox);
       
        JSONObject obj = this.getMessage();
            
        try{
            this.loginKey = obj.getString("result");
        }catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Login Key: " + loginKey);
        return !"".equals(this.loginKey);
        
    };
    
        /*
    * @author Dani
    */
    public boolean logout(){
                
        System.out.println("\n\nLogout");
        JSONObject jsonLogout=new JSONObject();
        try {   
            jsonLogout.put("command", "logout");
            jsonLogout.put("key", loginKey);
            
            outbox.setContent(jsonLogout.toString());
            System.out.println(jsonLogout.toString());
            this.send(outbox);
        }catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(getMessage());
        return true;  
    };

    public String nextMoveIs(JSONObject mensajeMov){
       
        try {
        int[][] multi = new int[5][5];
        String scannerMatrix = mensajeMov.getString("scanner");

        String[] scannerMatrixArray = scannerMatrix.split(",");
            System.out.println("");    
  
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
       return mensajeMov.toString();
    };
    
    
    public String makeMove(String movementCommand){
        
        System.out.println("\n\nMoviendose"); 
        setDestinatario("Bellatrix");
        JSONObject jsonMove=new JSONObject();
        JSONObject message;
        
        try {
            jsonMove.put("command", movementCommand);
            jsonMove.put("key", this.loginKey);
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        outbox.setContent(jsonMove.toString());
        System.out.println(jsonMove.toString());
        this.send(outbox);
        
        message = this.getMessage();
        String respuesta = message.toString();
        Parseo(respuesta);
        
        //Actualizacion de memoria pegamos en memoria la nueva traza del escaner
        this.actuMapa(movementCommand); 

        return nextMoveIs(message);
    };
       
    
}
