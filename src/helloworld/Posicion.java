/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloworld;

/**
 *
 * @author nacho
 */
public class Posicion {
        int x;
        int y;
        int num_veces;
        
        Posicion(){
            
        }

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
    
}
