/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package liquibase.util;

/**
 *
 * @author asales
 */
public class BooleanParser {
    public static boolean parseBoolean(String s){
        // test if we have a integer : if it's = 0 then return true, else return false
        if(s == null){
            return false;
        }
        try{
            int tmp = Integer.parseInt(s.trim());
            // it's an int !
            if(tmp <= 0){
                return false;
            }
            else{
                return true;
            }
        }
        catch(NumberFormatException ex){
            // it's not a number
            // cast it as a String
            String test = s.toString().trim().toLowerCase();
            if(test.equalsIgnoreCase("true")){
                return true;
            }
            else if(test.equalsIgnoreCase("t")){
                return true;
            }
            else if(test.equalsIgnoreCase("yes")){
                return true;
            }
            else if(test.equalsIgnoreCase("y")){
                return true;
            }
            else if(test.equalsIgnoreCase("false")){
                return false;
            }
            else if(test.equalsIgnoreCase("f")){
                return false;
            }
            else if(test.equalsIgnoreCase("no")){
                return false;
            }
            else if(test.equalsIgnoreCase("n")){
                return false;
            }
            else{
                return false;
            }
            
        }
    }
    
}

