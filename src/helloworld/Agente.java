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


/**
 *
 * @author Dani
 */
public class Agente extends SingleAgent {
    private String loginKey;
    
    public Agente(AgentID aid) throws Exception {
        super(aid);
        this.loginKey = "";
    }
    
    //public void init();
    @Override
    public void execute(){
        
        ACLMessage outbox = new ACLMessage(); 
        
        
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
        
        System.out.println("\n\nMoviendose");
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Bellatrix"));       
        JSONObject jsonMove=new JSONObject();
        JSONObject message;
        
        try {
            jsonMove.put("command", "moveW");
            jsonMove.put("key", this.loginKey);
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        outbox.setContent(jsonMove.toString());
        System.out.println(jsonMove.toString());
        this.send(outbox);
        
        message = this.getMessage();
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
            
            //System.out.println("Recibiendo Traza");
            ACLMessage inbox = this.receiveACLMessage();
            System.out.println("Recibido mensaje " +inbox.getContent()+ " de "
                    +inbox.getSender().getLocalName());

        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        /**********************************************************************
         * LOGOUT
         **********************************************************************/
}
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
    
}
