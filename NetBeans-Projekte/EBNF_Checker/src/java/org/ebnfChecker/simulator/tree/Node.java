/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ebnfChecker.simulator.tree;

import org.ebnfChecker.simulator.interpreter.ListElement;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author Admin
 */
public class Node {

    //Attribute zur Syntaxbaumerzeugung

    private Type typ; //Typ des Knotens
    private String value; //Inhalt des Knotens (bei Terminal und Nichtterminal)
    private Node top; //Vorgänger
    private Node left; //linker Nachfolger
    private Node right; //rechterNachfolger

    //Attribute, die für das Interpretieren notwendig sind
    //ALTERNATIVE
    private Stack<String> altStack; //Stack, der das Zurücklegen bei ALTERNATIVE regelt
    private boolean altStackBesucht;
    private boolean altGedreht; //gibt an, ob einer Alternative schon umgedreht wurde

    //OPTION
    private boolean besucht;
    private boolean optionBesucht;
    private Stack<String> opStack; //Stack, der das Zurücklegen bei OPTION regelt

    //WDH
    private Stack<String> wdhStack; //Stack, der das Zurücklegen bei WDH regelt
    private boolean wdhBesucht;
    private int countWdh;
    private boolean aktiveWdh;

    //Rekursion
    private int tiefe; //Tiefe der Rekursion
    private ArrayList<ListElement> rekursionListe; //ListElement-Liste (speichert Tiefe und dort genommene Zeichen)

    //mittige Rekursion
    private int countRekWdh;
    private boolean rekWdh;
    private boolean mittigeRekursion;
    private int counterMittigRek;

    public Node(Type typ, String value, Node top, Node left, Node right) {
        //Syntaxbaumerzeugung
        this.typ = typ;
        this.value = value;
        this.top = top;
        this.left = left;
        this.right = right;

        //ALTERNATIVE
        altStack = new Stack<String>();
        altStackBesucht = false;
        altGedreht = false;

        //OPTION
        besucht = false;
        optionBesucht = false;
        opStack = new Stack<String>();

        //WDH
        wdhStack = new Stack<String>();
        wdhBesucht = false;
        countWdh = 0;
        aktiveWdh = false;

        //Rekursion
        tiefe = -1;
        rekursionListe = new ArrayList<ListElement>();

        //MittigeRekursion
        countRekWdh = 0;
        rekWdh = false;
        mittigeRekursion = false;
        counterMittigRek = 0;
    }

    public Type getTyp() {
        return typ;
    }

    public String getValue() {
        return value;
    }

    public boolean isBesucht() {
        return besucht;
    }

    public void setBesucht(boolean besucht) {
        this.besucht = besucht;
    }

    public Node getTop() {
        return top;
    }

    public void setTop(Node top) {
        this.top = top;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public boolean isOptionBesucht() {
        return optionBesucht;
    }

    public void setOptionBesucht(boolean optionBesucht) {
        this.optionBesucht = optionBesucht;
    }

    public Stack<String> getOpStack() {
        return opStack;
    }

    public Stack<String> getAltStack() {
        return altStack;
    }

    public boolean isAltStackBesucht() {
        return altStackBesucht;
    }

    public void setAltStackBesucht(boolean altStackBesucht) {
        this.altStackBesucht = altStackBesucht;
    }

    public int getCountWdh() {
        return countWdh;
    }

    public void setCountWdh(int countWdh) {
        this.countWdh = countWdh;
    }

    public Stack<String> getWdhStack() {
        return wdhStack;
    }

    public boolean isWdhBesucht() {
        return wdhBesucht;
    }

    public void setWdhBesucht(boolean wdhBesucht) {
        this.wdhBesucht = wdhBesucht;
    }

    public int getTiefe() {
        return tiefe;
    }

    public void setTiefe(int tiefe) {
        this.tiefe = tiefe;
    }

    public ArrayList<ListElement> getRekursionListe() {
        return rekursionListe;
    }

    public int getCountRekWdh() {
        return countRekWdh;
    }

    public void setCountRekWdh(int countRekWdh) {
        this.countRekWdh = countRekWdh;
    }

    public boolean isMittigeRekursion() {
        return mittigeRekursion;
    }

    public void setMittigeRekursion(boolean mittigeRekursion) {
        this.mittigeRekursion = mittigeRekursion;
    }

    public boolean isRekWdh() {
        return rekWdh;
    }

    public void setRekWdh(boolean rekWdh) {
        this.rekWdh = rekWdh;
    }

    public boolean isAktiveWdh() {
        return aktiveWdh;
    }

    public void setAktiveWdh(boolean aktiveWdh) {
        this.aktiveWdh = aktiveWdh;
    }

    public int getCounterMittigRek() {
        return counterMittigRek;
    }

    public void setCounterMittigRek(int counterMittigRek) {
        this.counterMittigRek = counterMittigRek;
    }

    public boolean isAltGedreht() {
        return altGedreht;
    }

    public void setAltGedreht(boolean altGedreht) {
        this.altGedreht = altGedreht;
    }
    
}
