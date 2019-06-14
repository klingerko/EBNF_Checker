/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ebnfChecker.simulator.interpreter;

/**
 *
 * @author Admin
 */
import org.ebnfChecker.simulator.tree.*;
import org.ebnfChecker.simulator.lexparse.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;
import java_cup.runtime.ComplexSymbolFactory;

public class Interpreter {

    private Parser myParser; //Scanner-Instanz
    private Scanner myScanner; //Parser-Inszanz
    private String lexParseAusgabe; //Ausgabe des Scanners und Parsers
    private String interpreterAusgabe = ""; //Ausgabe des Interpreters
    private String fehlerAusgabe = ""; //Fehlerkette beim Interpretieren
    private int countFehler = 0; //Anzahl der Regeln in Fehlerkette
    private HashMap<String, BinaryTree> treeMap; //Syntaxbäume der einzelnen Regeln
    private String eingabe; //zu interpretierende Eingabe
    private String grammatik; //eingegebene EBNF-Grammatik
    private boolean syntaxError = false; //zeigt an, dass ein Syntaxfehler aufgetreten ist
    private boolean ruleNotFound = false; //zeigt an, wenn eine Regel (nicht Startregel) nicht gefunden wurde
    private boolean interpretError = false; //zeigt an, dass ein Interpreterfehler aufgetreten ist
    private boolean ignoreWSInput = false; //wenn true, dann werden in eingabe die WS " " und "\n" ignoriert

    private Stack<String> textStapel; //"eingabe" wird zeichenweise auf Stack gelegt und dann interpretiert

    //Zwischenspeicher bei Terminalauswertung (T)
    private String temp = null;
    private String speicher = "";

    //OPTION
    private int count = 0;
    private ArrayList<ArrayList<Node>> op = new ArrayList<ArrayList<Node>>();
    private Stack<ArrayList<Node>> myStack = new Stack<ArrayList<Node>>();
    private boolean opChanged = false;
    private boolean optionAlternative = false;
    private boolean zweiterVersuch = false; //Umdrehen aller Alternativen in einem 2. Interpretierversuch

    //Rekursion
    private ArrayList<BinaryTree> aufgerufeneRegeln = new ArrayList<BinaryTree>();

    public Interpreter() {
    }

    //Parsen
    public void initParsing() {
        //grammatik muss vorher gesetzt werden
        BinaryTree.count_klammer = 0;
        syntaxError = false;
        interpretError = false;
        interpreterAusgabe = "";
        fehlerAusgabe = "";
        countFehler = 0;
        ComplexSymbolFactory csf = new ComplexSymbolFactory();
        myScanner = new Scanner(new StringReader(this.grammatik), csf);
        myParser = new Parser(myScanner, csf);
        try {
            //Parsen
            treeMap = (HashMap<String, BinaryTree>) myParser.parse().value;
        } catch (Exception e) {
        }
        syntaxError = myParser.isSyntaxError();
        if (!myScanner.getScannerAusgabe().equals("")) {
            lexParseAusgabe = myScanner.getScannerAusgabe() + "\n" + myParser.getParserAusgabe();
        } else {
            lexParseAusgabe = myParser.getParserAusgabe();
        }
        if (!syntaxError) {
            lexParseAusgabe += "Result: CORRECT (parsed without errors)";
        }
    }

    //Interpretieren
    public void initInterpreting(String startregel) {
        interpreterAusgabe = "";
        fehlerAusgabe = "";
        countFehler = 0;
        interpretError = false;
        ruleNotFound = false;
        //Startregel suchen
        BinaryTree start = treeMap.get(startregel);
        if (start == null) {
            interpreterAusgabe += "Error: Couldn't find your start rule\n";
            interpretError = true;
            return;
        } else {
            initCounterMittigRek();
            initStack();
            aufgerufeneRegeln.add(start);
            start.incTiefe(start.getRoot());
            int result = interpret(start.getRoot().getLeft());
            for (ListElement le : start.getRoot().getRekursionListe()) {
                //Element aus Tiefe löschen, falls vorhanden
                if (le.getTiefe() == start.getRoot().getTiefe()) {
                    start.getRoot().getRekursionListe().remove(le);
                    break;
                }
            }
            start.decTiefe(start.getRoot());
            aufgerufeneRegeln.remove(start);
            if (!textStapel.empty()) {
                result = -1;
            }
            if (ruleNotFound) {
                //Eine Regel wurde nicht gefunden => Abbrechen des Interpreter-Vorgangs
                interpretError = true;
                return;
            }
            //Mittige Rekursion
            if (start.getRoot().isMittigeRekursion()) {
                while (start.getRoot().getCounterMittigRek() >= 0) {
                    start.setCounterMittigRekTree(start.getRoot(), start.getRoot().getCounterMittigRek() - 1); //counterMittigRek--
                    if (start.checkMittigeRek()) {
                        break;
                    }
                    resetMittigRekCounter(); //Counter auf 0 zurücksetzen
                    leereWdhStack();
                    initStack();
                    aufgerufeneRegeln.add(start);
                    start.incTiefe(start.getRoot());
                    result = interpret(start.getRoot().getLeft());
                    for (ListElement le : start.getRoot().getRekursionListe()) {
                        //Element aus Tiefe löschen, falls vorhanden
                        if (le.getTiefe() == start.getRoot().getTiefe()) {
                            start.getRoot().getRekursionListe().remove(le);
                            break;
                        }
                    }
                    start.decTiefe(start.getRoot());
                    aufgerufeneRegeln.remove(start);
                }
                if (!textStapel.empty()) {
                    result = -1;
                }
            }
            //Ende Mittige Rekursion
            if (result == 1) {
                interpreterAusgabe += "Result: CORRECT (intepreted without errors)\n";
                return;
            }
            //2. Versuch (Umdrehen aller Alternativen)
            zweiterVersuch = true;
            initStack();
            aufgerufeneRegeln.add(start);
            start.incTiefe(start.getRoot());
            result = interpret(start.getRoot().getLeft());
            for (ListElement le : start.getRoot().getRekursionListe()) {
                //Element aus Tiefe löschen, falls vorhanden
                if (le.getTiefe() == start.getRoot().getTiefe()) {
                    start.getRoot().getRekursionListe().remove(le);
                    break;
                }
            }
            start.decTiefe(start.getRoot());
            aufgerufeneRegeln.remove(start);
            if (!textStapel.empty()) {
                result = -1;
            }
            zweiterVersuch = false;
            //Ausprobieren aller verschiedenen Kombinationen der auftretenden Optionen
            while (result != 1 && count < 50) {
                opChanged = false;
                for (ArrayList<Node> current : op) {
                    //Markiere besetzt
                    for (Node n : current) {
                        n.setBesucht(true);
                    }
                    initStack();
                    aufgerufeneRegeln.add(start);
                    start.incTiefe(start.getRoot());
                    result = interpret(start.getRoot().getLeft());
                    for (ListElement le : start.getRoot().getRekursionListe()) {
                        //Element aus Tiefe löschen, falls vorhanden
                        if (le.getTiefe() == start.getRoot().getTiefe()) {
                            start.getRoot().getRekursionListe().remove(le);
                            break;
                        }
                    }
                    start.decTiefe(start.getRoot());
                    aufgerufeneRegeln.remove(start);
                    if (!textStapel.empty()) {
                        result = -1;
                    }
                    if (result == 1) {
                        break;
                    }
                    zweiterVersuch = true;
                    initStack();
                    aufgerufeneRegeln.add(start);
                    start.incTiefe(start.getRoot());
                    result = interpret(start.getRoot().getLeft());
                    for (ListElement le : start.getRoot().getRekursionListe()) {
                        //Element aus Tiefe löschen, falls vorhanden
                        if (le.getTiefe() == start.getRoot().getTiefe()) {
                            start.getRoot().getRekursionListe().remove(le);
                            break;
                        }
                    }
                    start.decTiefe(start.getRoot());
                    aufgerufeneRegeln.remove(start);
                    if (!textStapel.empty()) {
                        result = -1;
                    }
                    zweiterVersuch = false;
                    loescheBesetzt();
                    if (opChanged || result == 1) {
                        break;
                    }
                }
                count++;
            }
            if (result == 1) {
                interpreterAusgabe += "Result: CORRECT (intepreted without errors)\n";
            } else {
                fehlerAusgabe += " --> " + startregel;
                interpreterAusgabe += "Error: Couldn't interpret your input (error sequence:" + fehlerAusgabe + ")\n";
                interpretError = true;
            }
        }
    }

    public int interpret(Node regel) {
        switch (regel.getTyp()) {
            case T:
                if (textStapel.empty()) {
                    return -1;
                }
                for (int i = 0; i < regel.getValue().length(); i++) {
                    if (textStapel.empty()) {
                        //Zeichenlänge zu kurz => falsche Regel => breche ab & lege alle zurück
                        pushBackToStack();
                        return -1;
                    }
                    temp = textStapel.pop();
                    speicher += temp;
                    if (!regel.getValue().substring(i, i + 1).equals(temp)) {
                        //Zeichen falsch => breche ab & lege alle zurück
                        pushBackToStack();
                        return -1;
                    }
                    //Zeichen richtig => mache weiter
                }
                pushToAltStack(temp);
                pushToOptionStack(temp);
                pushToWdhStack(temp);

                for (BinaryTree tree : aufgerufeneRegeln) {
                    //in alle Rekursion-Listen eintragen
                    boolean neu = true;
                    for (ListElement le : tree.getRoot().getRekursionListe()) {
                        //Überall Hinzufügen
                        le.getWerte().push(temp);
                        if (le.getTiefe() == tree.getRoot().getTiefe()) {
                            //Tiefe schon vorhanden
                            neu = false;
                        }
                    }
                    if (neu) {
                        //Tiefe noch nicht vorhanden
                        tree.getRoot().getRekursionListe().add(new ListElement(tree.getRoot().getTiefe(), temp));
                    }
                }
                speicher = "";
                return 1;

            case NT:
                //Springe in NT-Regel
                BinaryTree tree = treeMap.get(regel.getValue());
                if (tree == null) {
                    //Regel nicht vorhanden
                    interpreterAusgabe += "Error: Couldn't find rule \"" + regel.getValue() + "\"\n";
                    ruleNotFound = true;
                    return -1; //Hier Abbrechen (z.B. bei Reihe) bzw. aufhören in diesem Zweig (z.B. bei Alternative)
                } else {
                    //Regel vorhanden
                    tree.incTiefe(tree.getRoot()); // Tiefe++
                    if (!aufgerufeneRegeln.contains(tree)) {
                        //Regel vorher noch nicht aufgerufen
                        aufgerufeneRegeln.add(tree);
                    }
                    if (tree.getRoot().getTiefe() > 500) {
                        //Grenze für Rekursion
                        interpreterAusgabe = "Warning: The rule \"" + regel.getValue() + "\" has been called to often recursively\n";
                        return -1;
                    }
                    int nt = interpret(tree.getRoot().getLeft()); //starte mit erstem Knoten nach P
                    for (ListElement le : tree.getRoot().getRekursionListe()) {
                        //Element aus Tiefe löschen, falls vorhanden
                        if (le.getTiefe() == tree.getRoot().getTiefe()) {
                            tree.getRoot().getRekursionListe().remove(le);
                            break;
                        }
                    }
                    tree.decTiefe(tree.getRoot()); // Tiefe--
                    if (tree.getRoot().getTiefe() == -1) {
                        aufgerufeneRegeln.remove(tree); //nur wenn Tiefe = -1
                        tree.resetAltStack(tree.getRoot());
                    }
                    if (nt == -1) {
                        //nicht mehr als 30 Regeln in Fehlerkette
                        if (countFehler++ < 30) {
                            fehlerAusgabe += " --> " + regel.getValue();
                        }
                    }
                    return nt;
                }

            case REIHE:
                //Speziallbehandlung für linker Nachfolger WDH: evtl. (mehrmaliges) Zurücklegen notwendig
                if (regel.getLeft().getTyp() == Type.WDH) {
                    regel.getLeft().setWdhBesucht(true);
                    regel.getLeft().setCountWdh(regel.getLeft().getCounterMittigRek());

                    while (regel.getLeft().getCountWdh() >= 0) {
                        int lR = interpret(regel.getLeft()); //es kommt immer 1 zurück
                        int rR = interpret(regel.getRight());
                        if (rR == -1) {
                            //nextRound
                            regel.getLeft().setCountWdh(regel.getLeft().getCountWdh() - 1);
                            while (!regel.getLeft().getWdhStack().empty()) {
                                textStapel.push(regel.getLeft().getWdhStack().pop()); //Zurücklegen
                                //hier alle anderen aktiven WDH-Stacks einmal pop()
                                regel.getLeft().setAktiveWdh(true);
                                popAktiveWdhStacks(regel);
                                regel.getLeft().setAktiveWdh(false);
                                //mittige Rekursion
                                if (regel.isMittigeRekursion() && regel.getLeft().isRekWdh() && regel.getLeft().getCountRekWdh() > 0) {
                                    regel.getLeft().setCountRekWdh(regel.getLeft().getCountRekWdh() - 1);
                                    // immer erniedrigen, wenn WDH zurückgelegt
                                }
                            }
                        } else {
                            //lR == 1 & rR == 1 => Erfolg
                            regel.getLeft().setWdhBesucht(false);
                            return 1;
                        }
                    }
                    regel.getLeft().setWdhBesucht(false);
                    return -1;
                }

                //links: NT oder T oder WDH oder OPTION
                int linksReihe = interpret(regel.getLeft());
                if (linksReihe == -1) {
                    return -1;
                }
                //rechts: naechste Regel(REIHE, ALTERNATIVE, OPTION, WDH) oder NT oder T
                int rechtsReihe = interpret(regel.getRight());
                // links & rechts müssen erfüllt werden
                if (linksReihe == 1 && rechtsReihe == 1) {
                    return 1;
                } else {
                    return -1;
                }

            case ALTERNATIVE:
                //links: NT oder T oder WDH oder OPTION
                int linksAlt;
                //2. Versuch (alle Alternativen umdrehen)
                if (zweiterVersuch) {
                    regel.setAltStackBesucht(true);
                    linksAlt = interpret(regel.getRight());
                    if (linksAlt == -1) {
                        //genommene Zeichen zurücklegen
                        regel.setAltStackBesucht(false);
                        if (regel.getTiefe() == 0) {
                            while (!regel.getAltStack().empty()) {
                                textStapel.push(regel.getAltStack().pop());
                            }
                        } else {
                            //genommene Zeichen der eigenen Tiefe zurücklegen
                            Node top = regel;
                            while (top.getTop() != null) {
                                top = top.getTop();
                            }
                            int anzahl = 0;
                            for (ListElement le : top.getRekursionListe()) {
                                if (le.getTiefe() == top.getTiefe()) {
                                    //gefunden
                                    while (!le.getWerte().empty()) {
                                        textStapel.push(le.getWerte().pop());
                                        anzahl++;
                                    }
                                }
                            }
                            for (ListElement le : top.getRekursionListe()) {
                                if (le.getTiefe() < top.getTiefe()) {
                                    for (int i = 0; i < anzahl; i++) {
                                        le.getWerte().pop(); //aus höheren Tiefen zurückgelegte Element löschen
                                    }
                                }
                            }
                        }
                    } else {
                        //genommene Zeichen nicht zurücklegen
                        regel.setAltStackBesucht(false);
                        if (regel.getTiefe() == 0) {
                            while (!regel.getAltStack().empty()) {
                                regel.getAltStack().pop();
                            }
                        } else {
                            Node top = regel;
                            while (top.getTop() != null) {
                                top = top.getTop();
                            }
                            for (ListElement le : top.getRekursionListe()) {
                                if (le.getTiefe() == top.getTiefe()) {
                                    //gefunden
                                    while (!le.getWerte().empty()) {
                                        le.getWerte().pop();
                                    }
                                }
                            }
                        }
                    }
                } else {
                    //1. Versuch (alle Alternativen normal)
                    regel.setAltStackBesucht(true);
                    linksAlt = interpret(regel.getLeft());
                    if (linksAlt == -1) {
                        //genommene Zeichen zurücklegen
                        regel.setAltStackBesucht(false);
                        if (regel.getTiefe() == 0) {
                            while (!regel.getAltStack().empty()) {
                                textStapel.push(regel.getAltStack().pop());
                            }
                        } else {
                            Node top = regel;
                            while (top.getTop() != null) {
                                top = top.getTop();
                            }
                            int anzahl = 0;
                            for (ListElement le : top.getRekursionListe()) {
                                if (le.getTiefe() == top.getTiefe()) {
                                    //gefunden
                                    while (!le.getWerte().empty()) {
                                        textStapel.push(le.getWerte().pop());
                                        anzahl++;
                                    }
                                }
                            }
                            for (ListElement le : top.getRekursionListe()) {
                                if (le.getTiefe() < top.getTiefe()) {
                                    for (int i = 0; i < anzahl; i++) {
                                        le.getWerte().pop(); //aus höheren Tiefen zurückgelegte Element löschen
                                    }
                                }
                            }
                        }
                    } else {
                        //genommene Zeichen nicht zurücklegen
                        regel.setAltStackBesucht(false);
                        if (regel.getTiefe() == 0) {
                            while (!regel.getAltStack().empty()) {
                                regel.getAltStack().pop();
                            }
                        } else {
                            Node top = regel;
                            while (top.getTop() != null) {
                                top = top.getTop();
                            }
                            for (ListElement le : top.getRekursionListe()) {
                                if (le.getTiefe() == top.getTiefe()) {
                                    //gefunden
                                    while (!le.getWerte().empty()) {
                                        le.getWerte().pop();
                                    }
                                }
                            }
                        }
                    }
                }

                //if (linksAlt == 1 && !optionAlternative) { //=> 16.06.15
                if (linksAlt == 1) {
                    return 1;
                }
                //rechts: naechste Regel(REIHE, ALTERNATIVE, OPTION, WDH) oder NT oder T
                optionAlternative = false;
                int rechtsAlt;
                //links oder rechts müssen erfüllt werden (nur eins von beiden oder beide)
                if (zweiterVersuch) {
                    rechtsAlt = interpret(regel.getLeft());
                } else {
                    rechtsAlt = interpret(regel.getRight());
                }
                if (rechtsAlt == 1) {
                    return 1;
                } else {
                    return -1;
                }

            case OPTION:
                if (textStapel.empty()) {
                    return 1; //kein Zeichen mehr da => Option kann in diesem Fall nur nicht genommen werden
                }
                if (regel.isBesucht()) {
                    regel.setOptionBesucht(false);
                    while (!regel.getOpStack().empty()) {
                        regel.getOpStack().pop();
                    }
                    return 1;
                } else {
                    regel.setOptionBesucht(true);
                    int linksOption = interpret(regel.getLeft());
                    if (linksOption == 1) {
                        //Option genommen
                        //Fall 1: Option richtig
                        //Fall 2: Zurücklegen erforderlich
                        fuegeOptionHinzu(regel);
                        regel.setOptionBesucht(false);
                        optionAlternative = false;
                        while (!regel.getOpStack().empty()) {
                            regel.getOpStack().pop();
                        }
                        return 1; //da in diesem Moment noch kein Fehler vorliegt
                    } else {
                        //Option nicht genommen
                        regel.setOptionBesucht(false);
                        while (!regel.getOpStack().empty()) {
                            textStapel.push(regel.getOpStack().pop());
                        }
                        optionAlternative = true;
                        return 1; //da kein Fehler vorliegt
                    }
                }

            case WDH:
                if (!regel.isWdhBesucht()) {
                    regel.setCountWdh(regel.getCounterMittigRek());
                    regel.setWdhBesucht(true);
                }
                for (int i = regel.getCountWdh(); i > 0; i--) {
                    //WDH genommen
                    int linksWdh = interpret(regel.getLeft());
                    if (linksWdh == 1 && regel.getTop().getTyp() != Type.REIHE) {
                        //Stacks (von allen) leeren => da Zurücklegen nicht mehr nötig
                        leereWdhStack();
                    }
                    if (linksWdh == 1) {
                        //mittige Rekursion
                        if (regel.isMittigeRekursion() && regel.isRekWdh()) {
                            regel.setCountRekWdh(regel.getCountRekWdh() + 1);
                        }
                    }
                    if (linksWdh == -1) {
                        //genommene Zeichen zurücklegen
                        if (regel.getTop().getTyp() != Type.REIHE) {
                            while (!regel.getWdhStack().empty()) {
                                textStapel.push(regel.getWdhStack().pop());
                                //mittige Rekursion
                                if (regel.isMittigeRekursion() && regel.isRekWdh() && regel.getCountRekWdh() > 0) {
                                    regel.setCountRekWdh(regel.getCountRekWdh() - 1);
                                    //immer erniedrigen, wenn zurücklegen
                                }
                            }
                            regel.setWdhBesucht(false);
                        }
                        break; //WDH nicht mehr genommen
                    }
                }
                return 1; //immer richtig
            default:
                break;
        }
        return -1;
    }

    public void printAllRules() {
        System.out.println();
        System.out.println("---");
        for (Entry<String, BinaryTree> e : treeMap.entrySet()) {
            BinaryTree t = e.getValue();
            t.print(t.getRoot(), 3);
            System.out.println("--");
        }
    }

    public void initStack() {
        textStapel = new Stack<String>();
        for (int i = eingabe.length(); i > 0; i--) {
            textStapel.push(eingabe.substring(i - 1, i));
        }
    }

    public void pushBackToStack() {
        for (int j = speicher.length(); j > 0; j--) {
            textStapel.push(speicher.substring(j - 1, j));
            speicher = speicher.substring(0, j - 1);
        }
        speicher = "";
    }

    public void popAktiveWdhStacks(Node n) {
        //1. Wurzel suchen
        Node wurzel = n;
        while (wurzel.getTop() != null) {
            wurzel = wurzel.getTop();
        }
        //2. bei allen aktiven Wiederholungen einmal pop()
        popAktiv(wurzel);
    }

    public void popAktiv(Node n) {
        if (n == null) {
            return;
        }
        if (n.getTyp() == Type.WDH && n.isWdhBesucht() && !n.isAktiveWdh()) { //wenn Wiederholung aktiv und nicht aktuelle
            if (!n.getWdhStack().empty()) {
                n.getWdhStack().pop();
            }
        }
        popAktiv(n.getLeft());
        popAktiv(n.getRight());
    }

    public void initCounterMittigRek() {
        for (Entry<String, BinaryTree> e : treeMap.entrySet()) {
            BinaryTree t = e.getValue();
            t.setCounterMittigRekTree(t.getRoot(), eingabe.length());
        }
    }

    public void pushToAltStack(String s) {
        for (Entry<String, BinaryTree> e : treeMap.entrySet()) {
            BinaryTree t = e.getValue();
            t.pushToAltStack(t.getRoot(), s);
        }
    }

    public void resetMittigRekCounter() {
        for (Entry<String, BinaryTree> e : treeMap.entrySet()) {
            BinaryTree t = e.getValue();
            t.resetCountRekWdh(t.getRoot());
        }
    }

    public void pushToOptionStack(String s) {
        for (Entry<String, BinaryTree> e : treeMap.entrySet()) {
            BinaryTree t = e.getValue();
            t.pushToOptionStack(t.getRoot(), s);
        }
    }

    public void pushToWdhStack(String s) {
        for (Entry<String, BinaryTree> e : treeMap.entrySet()) {
            BinaryTree t = e.getValue();
            t.pushToWdhStack(t.getRoot(), s);
        }
    }

    public void leereWdhStack() {
        for (Entry<String, BinaryTree> e : treeMap.entrySet()) {
            BinaryTree t = e.getValue();
            t.leereWdhStack(t.getRoot());
        }
    }

    public void loescheBesetzt() {
        for (Entry<String, BinaryTree> e : treeMap.entrySet()) {
            BinaryTree t = e.getValue();
            t.loescheBesetzt(t.getRoot());
        }
    }

    public void fuegeOptionHinzu(Node n) {
        for (ArrayList<Node> a : op) {
            if (a.contains(n)) {
                return; //Regel schon vorhanden
            }
        }
        opChanged = true;
        for (ArrayList<Node> a : op) {
            ArrayList<Node> copy = new ArrayList<Node>();
            for (Node rule : a) {
                copy.add(rule);
            }
            copy.add(n);
            myStack.push(copy);
        }
        while (!myStack.empty()) {
            op.add(myStack.pop());
        }
        //einzelne Regel hinzufügen
        ArrayList<Node> single = new ArrayList<Node>();
        single.add(n);
        op.add(single);
    }

    public HashMap<String, BinaryTree> getTreeMap() {
        return treeMap;
    }

    public void setEingabe(String eingabe) {
        if (ignoreWSInput) {
            //Leerzeichen und Zeilenumbrüche herausfiltern
            String ergebnis = "";
            for (int i = 0; i < eingabe.length(); i++) {
                String s = eingabe.substring(i, i + 1);
                if (s.equals(" ") | s.equals("\n")) {
                    continue; //Leerezeichen und Zeilenumbrüche ignorieren
                } else {
                    ergebnis += s; //Zeichen angängen
                }
            }
            this.eingabe = ergebnis; //neuen String zurückgeben
        } else {
            //eingabe kann direkt übergeben werden
            this.eingabe = eingabe;
        }
    }

    public String getLexParseAusgabe() {
        return lexParseAusgabe;
    }

    public String getInterpreterAusgabe() {
        return interpreterAusgabe;
    }

    public String getGrammatik() {
        return grammatik;
    }

    public void setGrammatik(String grammatik) {
        this.grammatik = grammatik;
    }

    public boolean isSyntaxError() {
        return syntaxError;
    }

    public boolean isInterpretError() {
        return interpretError;
    }

    public boolean isIgnoreWSInput() {
        return ignoreWSInput;
    }

    public void setIgnoreWSInput(boolean ignoreWSInput) {
        this.ignoreWSInput = ignoreWSInput;
    }
}
