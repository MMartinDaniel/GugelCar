/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloworld;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dani
 */
public class Helloworld {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Agente a;
     //   AgentsConnection.connect("localhost",5672,"test","guest","guest",false);  
       AgentsConnection.connect("isg2.ugr.es",6000,"Bellatrix","Escorpion","Russo",false);
        try {

            a = new Agente(new AgentID(Agente.NOMBRE_AGENTE));
            a .start();
     
        } catch (Exception ex) {
            System.out.println("Error al crear el agente ");
        }

      
    }
    
}
