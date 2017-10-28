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
import javafx.util.Pair;


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
    
    //IMPLEMENTADO CLASE MEMORIA
    //Variables para la memoria
    private ArrayList<ArrayList<Integer>> Mapa;
    private int MenY;
    private int MenX;
    private ArrayList<Pair> Rastro;
    
    //Pasos antes de recargar
    private int Pasos;
    
    
    // RUBEN
    int pasos;
    String [] last_moves;
    int last_move_pointer;
    
    class Posicion {
        int x;
        int y;
        int num_veces;

        Posicion(int new_x, int new_y){
            this.x = new_x;
            this.y = new_y;
            this.num_veces = 1;
        }

        Posicion(int new_x, int new_y, int new_num_veces){
            this.x = new_x;
            this.y = new_y;
            this.num_veces = new_num_veces;
        }

        public void out(){
            System.out.println(" X: " + this.x + "\n Y: " + this.y + "\n Num: " + this.num_veces + "\n");
        }
    };
    
    private ArrayList<Posicion> rastro;
    
    
    //IMPLEMENTADO CLASE MEMORIA
    //Variables para visual
    
        /*
        30 negro
        31 rojo
        32 verde
        33 amarillo
        34 azul
        35 magenta
        36 cyan
        37 blanco
        40 fondo negro
        41 fondo rojo
        42 fondo verde
        43 fondo amarillo
        44 fondo azul
        45 fondo magenta
        46 fondo cyan
        47 fondo blanco
        */
        
        private String ColorMenLibre ="\u001B[40m"; //40
        private String ColorSinOstaculos =  "\u001B[47m";//47
        private String ColorObstaculo= "\u001B[41m";//41
        private String ColorCoche= "\u001B[45m";//45
        private String ColorObjetivo= "\u001B[42m";//42
        private String ColorRecorrido= "\u001B[46m";//46


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
        
        //IMPLEMENTADO CLASE MEMORIA
        //Prueba memoria
        this.InicializarMemoria();
        
        this.Pasos = 0;
        
        //Mapa2.verMapaCoche(40, 40);
        
        // RUBEN
        this.pasos= 99;
        this.last_moves = new String[2];
        
        this.last_moves[0] = "";
        this.last_moves[1] = "";
        this.last_move_pointer = 0;
        
        this.rastro = new ArrayList();
    }
    
    //public void init();
    @Override
    public void execute(){

        setDestinatario("Bellatrix");
        login();
        refuel();
        
        /*boolean exit = false;

        
        //while(!objetivo()){
        for (int i = 0;i<100;i++){
              // if(i % 20 == 0){ refuel();
                System.out.println(availableMovements());
                makeMove(followScanner(availableMovements()));
               };
        //}
       */
        
        String nextMove = estrategia();
        
        makeMove(nextMove);
        
        while(!this.objetivo()){
            nextMove = estrategia();
            makeMove(nextMove);
            this.verMapaCoche(20,20);
        }
        this.verMapaCoche(20,20);
        /*
        makeMove("moveN");
        
        System.out.println("El Sur tiene= " + this.getS());
        
        makeMove("moveN");
        makeMove("moveN");
        makeMove("moveN");
        makeMove("moveS");
        makeMove("moveS");
        makeMove("moveN");
        makeMove("moveN");
        makeMove("moveS");
        makeMove("moveS");
        makeMove("moveN");
        makeMove("moveN");
        makeMove("moveS");
        makeMove("moveS");
        makeMove("moveS");
        makeMove("moveS");
        makeMove("moveS");
        makeMove("moveN");
        makeMove("moveN");
        makeMove("moveN");
        makeMove("moveN");
        makeMove("moveS");
        makeMove("moveS");
        */
      
       
        logout();    
        generarMapaTraza();
        
        //this.verMapaCoche(30,30);
         // this.verMapa(151,151);

        
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
    /**
    * @autor Daniel, Nacho
    */
    public JSONObject getMessage(){
        
        JSONObject obj = null;
        try {
            //System.out.println("Recibiendo Traza");
            ACLMessage inbox = this.receiveACLMessage();
            //System.out.println("Recibido mensaje " +inbox.getContent()+ " de "
                    //+inbox.getSender().getLocalName());
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
    /**
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
    

    

    /**
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
        //System.out.println(jsonRefuel.toString());
        this.send(outbox);
        
        this.pasos = 99;
        
        String respuesta = this.getMessage().toString();
        Parseo(respuesta);
    }
    
    /**
     * @author ¿?¿?
     */
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
    
    /**
     * @author ¿?¿?
     */
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

    /**
    * @author Dani
    */
    public void setDestinatario(String nombre){
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID(nombre));   
    };
    /**
    * @author Dani
    */
    public boolean login(){
 
        JSONObject jsonLogin=new JSONObject();
        
        try {
            jsonLogin.put("command", "login");
            jsonLogin.put("world", "map1");
            jsonLogin.put("radar", "agentep40");
            jsonLogin.put("scanner", "agentep40");
            jsonLogin.put("battery", "agentep40"); 
            
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }

        outbox.setContent(jsonLogin.toString());
        //System.out.println(jsonLogin.toString());
        this.send(outbox);
       
        JSONObject obj = this.getMessage();
            
        try{
            this.loginKey = obj.getString("result");
        }catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //System.out.println("Login Key: " + loginKey);
        return !"".equals(this.loginKey);
        
    };
    
    /**
    * @author Dani
    */
    public boolean logout(){
                
        System.out.println("\n\nLogout");
        JSONObject jsonLogout=new JSONObject();
        try {   
            jsonLogout.put("command", "logout");
            jsonLogout.put("key", loginKey);
            
            outbox.setContent(jsonLogout.toString());
            //System.out.println(jsonLogout.toString());
            this.send(outbox);
        }catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println(getMessage());
        return true;  
    };


    
    /**
     * @author Nacho
     */
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
        //System.out.println("ME HE MOVIDO");
        
        this.pasos--;
        
        this.actuMapa(movementCommand); 
        //this.Autorefuel();
        
        return respuesta;
    };
    
    /**
     * @author Nacho
     */
    public int getRadar (String direccion){
        switch (direccion){
            case "N" : return lecturaRadar[1][2];
            case "NE" : return lecturaRadar[1][3];
            case "E" : return lecturaRadar[2][3];
            case "SE" : return lecturaRadar[3][3];
            case "coche" : return lecturaRadar[2][2]; 
            case "S" : return lecturaRadar[3][2];
            case "SO" : return lecturaRadar[3][2];
            case "O" : return lecturaRadar[2][1];
            case "NO" : return lecturaRadar[1][1];
           // case "coche" : return lecturaRadar[2][2];
        }
        
        return -1000;
        
    }

    
    /**
     * @author Nacho, Dani
     */
    
    public String followScanner(String posibleMovs){
        
        String[] movparts = posibleMovs.split(",");
        String nextMove = "";
        int posX = 0;
        int posY = 0;
        double menor = 1000;
        for (int i = 0; i < movparts.length; i = i+2) {
            int x = Integer.parseInt(movparts[i]);
            int y = Integer.parseInt(movparts[i+1]);
            if(lecturaScanner[x][y] < menor){ menor = lecturaScanner[x][y];   posX = x; posY = y;};
        }

        if(posX == 1){
            if(posY == 1){ nextMove = "moveNW";
            }else if( posY == 2){ nextMove = "moveN";
            }else if( posY == 3){ nextMove = "moveNE";}    
        }else if ( posX == 2){
            if(posY == 1){ return "moveW";
            }else if( posY == 2){ nextMove = "dontMove";
            }else if( posY == 3){ nextMove = "moveE";}    
        }else if ( posX == 3){
            if(posY == 1){ return "moveSW";
            }else if( posY == 2){ nextMove = "moveS";
            }else if( posY == 3){ nextMove = "moveSE";}    
        }
        /*
        
        double n = lecturaScanner[1][2];
        double ne = lecturaScanner[1][3];
        double e = lecturaScanner[2][3];
        double se = lecturaScanner[3][3];
        double s = lecturaScanner[3][2];
        double so = lecturaScanner[3][1];
        double o = lecturaScanner[2][1];
        double no = lecturaScanner[1][1];
        
        if(n <= ne && n <= e && n <= se && n <= s && n <= so && n <= o && n <= no){
            nextMove = "moveN";
        }else if(ne <= n && ne <= e && ne <= se && ne <= s && ne <= so && ne <= o && ne <= no){
            nextMove = "moveNE";
        }else if(e <= n && e <= ne && e <= se && e <= s && e <= so && e <= o && e <= no){
            nextMove = "moveE";
        }else if(se <= n && se <= ne && se <= e && se <= s && se <= so && se <= o && se <= no){
            nextMove = "moveSE";
        }else if(s <= n && s <= ne && s <= e && s <= se && s <= so && s <= o && s <= no){
            nextMove = "moveS";
        }else if(so <= n && so <= ne && so <= e && so <= se && so <= s && so <= o && so <= no){
            nextMove = "moveSW";
        }else if(o <= n && o <= ne && o <= e && o <= se && o <= so && o <= s && o <= no){
            nextMove = "moveW";
        }else if(no <= n && no <= ne && no <= e && no <= se && no <= so && no <= o && no <= s){
            nextMove = "moveNW";
        }
      */
        //System.out.println("NEXT MOVE:" + nextMove);
       return nextMove;
    }
    
    /*
    posiciones array
    n 12
    ne 13
    e 23
    se 33
    s 32
    so 31
    o 21
    no 11  
    */
       
    public int TraducirPosicion(int coordenada_coche, int indice_radar){
        int aux = coordenada_coche;
        
        switch(indice_radar){
            case 1:
                aux -= 1;
                break;
            case 2:
                break;
            case 3:
                aux += 1;
                break;
        }
        
        return aux;
    }
    
    /**
    * @author Daniel
    */
    
    
        public String availableMovements(){
            int posX;
            int posY;
            
            String availables = ""; 
            for (int i = 1; i <= 3; i++) {
                for (int j = 1; j <= 3; j++) {
                    if(lecturaRadar[i][j] != 1 && i != j){
                         posX = TraducirPosicion(this.MenX, i);
                         posY = TraducirPosicion(this.MenY, j);
                         Pair a = new Pair(posY, posX);
                         int rx = (Integer) Rastro.get(Rastro.size()-1).getKey();
                         int ry = (Integer) Rastro.get(Rastro.size()-1).getValue();
                         if(!Rastro.contains(a))
                            availables = availables + i + "," + j + ",";
                    }
                }
            }
            
            
            
          return availables;
    };
    
    public boolean checkObstacle( String tryDirection ){

            for (int i = 1; i <= 3; i++) {
                for (int j = 1; j <= 3; j++) {
                    //System.out.print(lecturaRadar[i][j]);
                }
                //System.out.println("");
           }
            
           switch(tryDirection){
               case "MoveN": if( getRadar("N") != 1 ){ return true;};
               break;
               case "moveNE": if( getRadar("NE") != 1 ){ return true;};
               break;
               case "moveS": if( getRadar("S") != 1 ){ return true;};
               break; 
               case "moveSE": if( getRadar("SE") != 1 ){ return true;};
               break;
               case "moveE":  if( getRadar("E") != 1 ){ return true;};
               break;               
               case "moveW":  if( getRadar("W") != 1 ){ return true;};
               break; 
               case "movSW":  if( getRadar("SW") != 1 ){ return true;};
               break; 
               case "moveNW": if( getRadar("NW") != 1 ){ return true;};
               break;
           }
           
       return false;
    };
    
    /**
     * @author nacho
     * @return 
     */
     public boolean objetivo(){
        if(getRadar("coche") == 2){
            return true;
        }else{
            return false;
        }
        
    }
     

     
    /*
     * @author grego
     */
     
    private void Autorefuel(){
        if(Pasos > 90){
            this.refuel();
            Pasos = 0;
        }
    }
    
     /*
     *---------------------------------------------------------------------------
     *Getter de consulta a nuestro alrededor en el mapa
     *---------------------------------------------------------------------------
     */
    
    /*
    * @author grego
    */
    public int getN(){
        int auxX = this.MenX;
        int auxY = this.MenY - 1;
        return Mapa.get(auxY).get(auxX);
    }
    /*
    * @author grego
    */
    public int getS(){
        int auxX = this.MenX;
        int auxY = this.MenY + 1;
        return Mapa.get(auxY).get(auxX);
    }
    
    /*
    * @author grego
    */
    public int getE(){
        int auxX = this.MenX + 1;
        int auxY = this.MenY;
        return Mapa.get(auxY).get(auxX);
    }
    
    /*
    * @author grego
    */
    public int getW(){
        int auxX = this.MenX - 1;
        int auxY = this.MenY;
        return Mapa.get(auxY).get(auxX);
    }
    /*
    * @author grego
    */
    public int getNE(){
        int auxX = this.MenX + 1;
        int auxY = this.MenY - 1;
        return Mapa.get(auxY).get(auxX);
    }
    
    /*
    * @author grego
    */
    public int getNW(){
        int auxX = this.MenX - 1;
        int auxY = this.MenY - 1;
        return Mapa.get(auxY).get(auxX);
    }
    
    /*
    * @author grego
    */
    public int getSE(){
        int auxX = this.MenX + 1;
        int auxY = this.MenY + 1;
        return Mapa.get(auxY).get(auxX);
    }

    /*
    * @author grego
    */
    public int getSW(){
        int auxX = this.MenX - 1;
        int auxY = this.MenY + 1;
        return Mapa.get(auxY).get(auxX);
    }    
    
     
     
     /*
     *---------------------------------------------------------------------------
     *FUNCIONES DE LA MEMORIA
     *---------------------------------------------------------------------------
     */
     
    
         //IMPLEMENTADO CLASE MEMORIA
    /**
    * @author grego
    */
    
    //Inicializacion de memoria matriz 1000x1000 para no salirnos de los
    //limites maximo de mapa 500x500 "4" indica memoria libre o mapa sin descubrir
    
    private void InicializarMemoria(){
            
    this.Mapa = new ArrayList<ArrayList<Integer>>();
    
    //Nos posicionamos en el centro de la memoria
    this.MenY = 500;
    this.MenX = 500;
    
    this.Rastro = new ArrayList<Pair>();
    Pair PosicionRastro = new Pair(MenX,MenY);
    
  
    
    this.Rastro.add(PosicionRastro);
    
    //System.out.println(Rastro.get(0).getValue());
    
    for(int i = 0; i < 1000; i++){
        this.Mapa.add(new ArrayList<Integer>());
            for (int j = 0; j < 1000; j++){
                this.Mapa.get(i).add(4);
            }
    }
    //System.out.println("Tengo memoria");
    }
    
 
    /**
    * @author grego
    *
    *Visisualiza el mapa desde el centro de la memoria
    *@pre para obtener un dato real usar la funcion con numeros impares
    * @param a {Ancho ncasillar} 
    * @param l {Alto ncasillas}
    */
    public void verMapa(int a, int l){
     
     
        //Delimito el centro
        int L = l/2 + 500;
        int A = a/2 + 500;   
        
       
        //Muestro los datos del centro de la memoria
        for(int i = 500 - l/2 ; i < L; i++){
            for (int j = 500 - a/2; j < A; j++){
                if(4 == Mapa.get(i).get(j)){
                    System.out.print(ColorMenLibre + Mapa.get(i).get(j));
        
                }else if(1 == Mapa.get(i).get(j)){
                
                    System.out.print(ColorObstaculo + Mapa.get(i).get(j));

                }else if(5 == Mapa.get(i).get(j)){
                
                    System.out.print(ColorCoche + Mapa.get(i).get(j));

                }else if(2 == Mapa.get(i).get(j)){
                
                    System.out.print(ColorObjetivo + Mapa.get(i).get(j));

                }else if(Mapa.get(i).get(j) >= 6){
                
                    System.out.print(ColorRecorrido + Mapa.get(i).get(j));

                }else{
                    System.out.print( ColorSinOstaculos + Mapa.get(i).get(j));
                    
                }
            }
            System.out.println();
        }

    }
 
     
     
    /**
    * @author grego
    *
    *Visisualiza el mapa desde el coche
    *@pre para obtener un dato real usar la funcion con numeros impares
    * @param a {Ancho ncasillar} 
    * @param l {Alto ncasillas}
    */
    public void verMapaCoche(int a, int l){
     
     
        //Delimito el centro
        int L = l/2 + this.MenY ;
        int A = a/2 + this.MenX;   

  
        

        
        
        //Muestro los datos del centro de la memoria
        for(int i = this.MenY  - l/2 ; i < L; i++){
            for (int j = this.MenX - a/2; j < A; j++){
                if(4 == Mapa.get(i).get(j)){
                    System.out.print(ColorMenLibre + Mapa.get(i).get(j));
        
                }else if(1 == Mapa.get(i).get(j)){
                
                    System.out.print(ColorObstaculo + Mapa.get(i).get(j));

                }else if(5 == Mapa.get(i).get(j)){
                
                    System.out.print(ColorCoche + Mapa.get(i).get(j));

                }else if(2 == Mapa.get(i).get(j)){
                
                    System.out.print(ColorObjetivo + Mapa.get(i).get(j));

                }else if(Mapa.get(i).get(j) >= 6){
                
                    System.out.print(ColorRecorrido + Mapa.get(i).get(j));

                }else{
                    System.out.print( ColorSinOstaculos + Mapa.get(i).get(j));
                    
                }
            }
            System.out.println();
        }

    }
    
    public boolean buscar(int x, int y){
        boolean encontrado = false;

        if(rastro.size() > 125){
            for(int j=rastro.size()-1; j > rastro.size()-126 && !encontrado; j--){
                Posicion p = rastro.get(j);
                if(p.x == x && p.y == y && p.num_veces >= 2){
                    System.out.println("Ya hemos pasado por " + x + "," + y + " al menos 2 veces. La eliminamos");
                    encontrado = true;
                }
            }
        }
        else {
            for(int j=0; j < rastro.size() && !encontrado; j++){
                Posicion p = rastro.get(j);

                if(p.x == x && p.y == y && p.num_veces >= 5){
                    System.out.println("Ya hemos pasado por " + x + "," + y + " al menos 5 veces. La eliminamos");
                    encontrado = true;
                }
            }
        }
        
        return encontrado;
    }
    
    
    public double comprobarPasada(int y, int x){
        if(Mapa.get(y).get(x) == 0){
            return 5.0;
        }
        return 0;
    }
    
    
    public String estrategia() {
        
        if(this.pasos == 1)
            this.refuel();
        
        String nextMove = "";
        
        boolean encontrado = false;
        int coord_x = -1;
        int coord_y = -1;
        
        // Miramos si el objetivo esta al alcance
        for(int i=1; i < 4 && !encontrado; i++){
            for(int j=1; j < 4 && !encontrado; j++){
                if(lecturaRadar[i][j] == 2){
                    encontrado = true;
                    coord_x = i;
                    coord_y = j;
                }
                
            }
        }
        
        if(encontrado){
            // Nos dirigimos directamente al objetivo
            switch(coord_x){
                case 1:
                    switch(coord_y){
                        case 1:
                            nextMove = "moveNW";
                            break;
                        case 2:
                            nextMove = "moveN";
                            break;
                        case 3:
                            nextMove = "moveNE";
                    }
                    break;
                case 2:
                    switch(coord_y){
                        case 1:
                            nextMove = "moveW";
                            break;
                        case 3:
                            nextMove = "moveE";
                    }
                    break;
                case 3:
                    switch(coord_y){
                        case 1:
                            nextMove = "moveSW";
                            break;
                        case 2:
                            nextMove = "moveS";
                            break;
                        case 3:
                            nextMove = "moveSE";
                    }
                    break;
            }
        } else {
            // Si no lo vemos, filtramos y dejamos SOLO las direcciones donde podemos ir
        //if(!found){
            boolean[] posibles_movimientos = new boolean[8];
            // Puntero auxiliar que nos sirve para rellenar el vector anterior
            int pointer = 0;
            
            for(int i=1; i<4; i++){
                for(int j=1; j<4; j++){
                    // No tenemos en cuenta la posicion donde esta el coche: lecturaRadar[2][2]
                    if(i != 2 || j != 2){
                        System.out.println("Lectura radar: " + lecturaRadar[i][j]);
                        if(lecturaRadar[i][j] == 0)
                            posibles_movimientos[pointer] = true;
                        else
                            posibles_movimientos[pointer] = false;
                        
                        pointer++;
                    }
                }
            }
            
            // Buscamos la direccion en funcion de la menor distancia, siempre que dicha direccion sea posible
            double[] distancias = new double[8];
            
            pointer = 0;
            
            for(int i=1; i<4; i++){
                for(int j=1; j<4; j++){
                    if(i != 2 || j != 2){
                        distancias[pointer] = lecturaScanner[i][j];
                        pointer++;
                    }
                }
            }
            
            boolean posicion_encontrada;
            
            // Pasamos el filtro del Radar
            for(int i=0; i<8; i++){
                if(posibles_movimientos[i] == false)
                    distancias[i] = 1000.0;
            }

            // Pasamos el filtro del rastro
            for(int i=0; i < 8; i++){
                if(posibles_movimientos[i] == true){
                    switch(i){
                        case 0:
                            encontrado = buscar(this.MenY-1, this.MenX-1);  
                            if(encontrado){
                                distancias[i] = 1000.0;
                            }else{
                                 distancias[i] -= comprobarPasada(this.MenY-1, this.MenX-1);
                            }
                            break;
                        case 1:
                            encontrado = buscar(this.MenY-1, this.MenX);
                            
                             if(encontrado){
                                distancias[i] = 1000.0;
                            }else{
                                 distancias[i] -= comprobarPasada(this.MenY-1, this.MenX);
                            }
                            break;
                        case 2:
                            encontrado = buscar(this.MenY-1, this.MenX+1);
                            
                            if(encontrado){
                                distancias[i] = 1000.0;
                            }else{
                                   distancias[i] -= comprobarPasada(this.MenY-1, this.MenX+1);
                            }
                            break;
                        case 3:
                            encontrado = buscar(this.MenY, this.MenX-1);
                            
                          if(encontrado){
                                distancias[i] = 1000.0;
                            }else{
                                  distancias[i] -= comprobarPasada(this.MenY, this.MenX-1);
                            }
                            break;
                        case 4:
                            encontrado = buscar(this.MenY, this.MenX+1);
                            
                            if(encontrado){
                                distancias[i] = 1000.0;
                            }else{
                                distancias[i] -= comprobarPasada(this.MenY, this.MenX+1);
                            }
                            break;
                        case 5:
                            encontrado = buscar(this.MenY+1, this.MenX-1);
                            
                            if(encontrado){
                                distancias[i] = 1000.0;
                            }else{
                                  distancias[i] -= comprobarPasada(this.MenY+1, this.MenX-1);
                            }
                            break;
                        case 6:
                            encontrado = buscar(this.MenY+1, this.MenX);
                            
                             if(encontrado){
                                distancias[i] = 1000.0;
                            }else{
                                 distancias[i] -= comprobarPasada(this.MenY+1, this.MenX);
                            }
                            break;
                        case 7:
                            encontrado = buscar(this.MenY+1, this.MenX+1);
                            
                             if(encontrado){
                                distancias[i] = 1000.0;
                            }else{
                                 distancias[i] -= comprobarPasada(this.MenY+1, this.MenX+1);
                            }
                            break;
                    }
                }
            }
            
            for(int i=0; i < 8; i++)
                System.out.println("Distancia " + i + ": " + distancias[i]);
            
            double smaller = distancias[0];
            int index = 0;
            
            for(int i=1; i<8; i++){
                if(distancias[i] < smaller){
                    smaller = distancias[i];
                    index = i;
                }
            }
            
            switch(index){
                case 0:
                    nextMove="moveNW";
                    break;
                case 1:
                    nextMove="moveN";
                    break;
                case 2:
                    nextMove="moveNE";
                    break;
                case 3:
                    nextMove="moveW";
                    break;
                case 4:
                    nextMove="moveE";
                    break;
                case 5:
                    nextMove="moveSW";
                    break;
                case 6:
                    nextMove="moveS";
                    break;
                case 7:
                    nextMove="moveSE";
                    break;
            }
        }
        
        return nextMove;
    }
    

    
    /**
     *
     * @author grego, kudry
     * @param movementCommand
     */
    public void actuMapa(String movementCommand){
        
        //Contadores para la matriz del radar
        int conRadarI = 0;
        int conRadarJ = 0;
        

        
        
        //Ajusto mi posicion en funcion del movimiento
        if (movementCommand.equals("moveW") ){
            this.MenX--;
            //System.out.println("Voy al Oeste");

        }else if (movementCommand.equals("moveE")){
            this.MenX++;
            //System.out.println("Voy al Este");    

        }else if (movementCommand.equals("moveN")){
            this.MenY--;
            //System.out.println("Voy al Norte");    

        }else if (movementCommand.equals("moveS")){
            this.MenY++;
            //System.out.println("Voy al Sur");    
        }
      
        else if (movementCommand.equals("moveNW")){
            this.MenX--;
            this.MenY--;
            //System.out.println("Voy al NorOeste");    
        
        }else if (movementCommand.equals("moveNE")){
            this.MenX++;
            this.MenY--;
            //System.out.println("Voy al NorEste");    
        
        }else if (movementCommand.equals("moveSW")){
            this.MenX--;
            this.MenY++;
            //System.out.println("Voy al NorOeste");    
        
        }else if (movementCommand.equals("moveSE")){
            this.MenX++;
            this.MenY++;
            //System.out.println("Voy al NorOeste");    
        }        

        //Añado la posicion del rastro
        Pair PosicionRastro = new Pair(MenY,MenX);
        Rastro.add(PosicionRastro);
        
        // RUBEN
        
        Posicion new_posicion = new Posicion(this.MenY, this.MenX);

        System.out.println("Aniadiendo la posicion " + new_posicion.x + "," + new_posicion.y);
        
        boolean encontrado = false;
        int index = -1;
        
        if(rastro.size() > 75){
            for(int i=rastro.size()-1; i > rastro.size()-76; i--){
                Posicion p = rastro.get(i);
            
                if(p.x == new_posicion.x && p.y == new_posicion.y){
                    encontrado = true;
                    index = i;
                }
            }
        } else {
            for(int i=0; i < rastro.size() && !encontrado; i++){
                Posicion p = rastro.get(i);

                if(p.x == new_posicion.x && p.y == new_posicion.y){
                    encontrado = true;
                    index = i;
                }
            }
        }

        if(index == -1)
            rastro.add(new_posicion);
        else {
            //System.out.println("La posicion que intentas aniadir ya se encuentra.");
            Posicion aux = rastro.get(index);
            Posicion new_new_posicion = new Posicion(this.MenY,this.MenX,aux.num_veces+1);
            rastro.set(index, new_new_posicion);
        }
        
        /* OUT
        System.out.println("Rastro contiene: ");
        
        for(Posicion p : rastro){
            p.out();
        }*/
        
        // FIN RUBEN
        
        //Agrego la nueva informacion a la memoria
        for(int i = this.MenY -2 ; i < this.MenY + 3; i++){
            for (int j = this.MenX -2 ; j < this.MenX + 3; j++){
                Mapa.get(i).set(j,lecturaRadar[conRadarI][conRadarJ]);
                conRadarJ++;     
            }
            //System.out.println();
            conRadarI++;
            conRadarJ = 0;     
        }
           
        /*
        *Rastro
        */
        
       //Pego el camino recorrido
            for(int i = 0; i < Rastro.size();i++){
                Mapa.get((Integer)this.Rastro.get(i).getKey()).set((Integer) this.Rastro.get(i).getValue(), 6);
            }
       
       //Ajusto el camino pisado
      
       ArrayList<Integer> comprobados;
       comprobados = new ArrayList<Integer>();
       
       boolean comprobado = false;
    
    /*     
        for(int i = 0; i < Rastro.size(); i++){
            System.out.println("Y = " + Rastro.get(i).getKey() + " X = " + Rastro.get(i).getValue());
        }
    */
    int v;
    if(Rastro.size() < 125){  v = 0; }else{ v = Rastro.size()-20;};
    for(int i = v; i < Rastro.size()-1; i++){
            
            for (int j = i+1; j < (Rastro.size()); j++){
                comprobados.add(i);
                                
                comprobado = false;
                
                //Busco si ya lo he comprobado para no insertar dobles
                for(int h = 0; h < comprobados.size(); h++){
                    if (comprobados.get(h) == j){
                        comprobado= true;
                    }
                }
                
                //Busco repetidos para incrementar        
                if (this.Rastro.get(i).getKey().equals(this.Rastro.get(j).getKey()) && this.Rastro.get(i).getValue().equals(this.Rastro.get(j).getValue()) && comprobado == false){
                    int aux = Mapa.get((Integer)this.Rastro.get(i).getKey()).get((Integer) this.Rastro.get(i).getValue());
                    aux++;
                    Mapa.get((Integer)this.Rastro.get(i).getKey()).set((Integer) this.Rastro.get(i).getValue(), aux);
                    //Inserto el indice en comprobados    
                    comprobados.add(j);
                }

            }
        }

        //System.out.println("------ACTUALIZADO---------");
        
        // Ubico el coche en el mapa
        Mapa.get(this.MenY).set(this.MenX, 5);
        
        //Muestro el mapa
        //this.verMapaCoche(40,40);
        //this.verMapa(40,40);
        
        //Acualizo los pasos
        Pasos++;

        
    }

}