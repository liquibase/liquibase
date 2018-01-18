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
            String test = s.trim().toLowerCase();
            if("true".equalsIgnoreCase(test)){
                return true;
            }
            else if("t".equalsIgnoreCase(test)){
                return true;
            }
            else if("yes".equalsIgnoreCase(test)){
                return true;
            }
            else if("y".equalsIgnoreCase(test)){
                return true;
            }
            else if("false".equalsIgnoreCase(test)){
                return false;
            }
            else if("f".equalsIgnoreCase(test)){
                return false;
            }
            else if("no".equalsIgnoreCase(test)){
                return false;
            }
            else if("n".equalsIgnoreCase(test)){
                return false;
            }
            else{
                return false;
            }
            
        }
    }
    
}

