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
    
    public Agente(AgentID aid) throws Exception {
        super(aid);
        this.loginKey = "";
        
        // Variables que guardan las lecturas de los sensores. Se actualizan tras cada movimiento del GugelCar
        this.lecturaRadar = new int[5][5];
        this.lecturaScanner = new double[5][5];
        this.nivelBateria = 0.0;
        
        this.outbox = new ACLMessage();
    }
    
    //public void init();
    @Override
    public void execute(){
        
        //ACLMessage outbox = new ACLMessage(); 
        
        
        System.out.println("\n\n\nHola Mundo soy un agente llamado " + this.getName());

        /**********************************************************************
         * LOGIN
         **********************************************************************/
        
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Bellatrix"));       
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
         
        
        /**********************************************************************
         * REFUEL
         **********************************************************************/
        
        this.refuel();
        
        /**********************************************************************
         * /MOVE
         **********************************************************************/
        
        this.move("moveW");
        
        /* * /
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
            
        /* */
        /**********************************************************************
         * LOGOUT
         **********************************************************************/
            
       
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
        
        this.getMessage();
        
        //Generar traza en .png
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
        /*Traza
        ACLMessage inbox = null;
        try {
        inbox = this.receiveACLMessage();
        } catch (InterruptedException ex) {
        Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("\nTraza: " +inbox.getContent());
         */    
            /*Traza
            ACLMessage inbox = null;
            try {
            inbox = this.receiveACLMessage();
            } catch (InterruptedException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("\nTraza: " +inbox.getContent());
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
    
    private void move(String direccion){
        
        /*
        * @author nacho
        */
        System.out.println("\n\nMoviendose");
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Bellatrix"));       
        JSONObject jsonMove=new JSONObject();
        JSONObject message;
        
        try {
            jsonMove.put("command", direccion);
            jsonMove.put("key", this.loginKey);
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        outbox.setContent(jsonMove.toString());
        System.out.println(jsonMove.toString());
        this.send(outbox);
        
        message = this.getMessage();
        
        // Tras cada movimiento del GugleCar, parsearemos la respuesta de los sensores
        String respuesta = message.toString();
        
        Parseo(respuesta);
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
    
}
