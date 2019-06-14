/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ebnfChecker.simulator.interpreter;

import java.util.Stack;

/**
 *
 * @author Admin
 */
public class ListElement {
    private int tiefe; //Tiefe
    private Stack<String> werte = new Stack<String>(); //genommene Zeichen bei entsprechender Tiefe
    
    public ListElement(int tiefe, String value) {
        this.tiefe = tiefe;
        werte.push(value);
    }

    public int getTiefe() {
        return tiefe;
    }

    public void setTiefe(int tiefe) {
        this.tiefe = tiefe;
    }

    public Stack<String> getWerte() {
        return werte;
    }

    
}
