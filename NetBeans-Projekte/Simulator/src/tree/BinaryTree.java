/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tree;

import java.util.HashMap;
import java.util.Stack;

/**
 *
 * @author Admin
 */
public class BinaryTree {

    private Node root; //Wurzel des Baums
    private String ausgabe; //Grafische Darstellung des Baumes
    
    private boolean alternativeInBaum = false; //gibt an, ob eine Alternative in der Regel vorkommt

    public static int count_klammer = 0; //Anzahl der Klammern (werden zu eigenen Regeln)
    //nicht private, da GWT ein Problem mit static-Methoden hatte
    //count_klammer muss auf 0 zurückgesetzt werden, bevor die Alternativen einzeln umgedreht werden => 16.06.2015 (kurz vor Ende erst realisiert)

    //Zur Überprüfung der gleichen Anzahl an genommenen WDHs bei umgewandelter mittiger Rekursion
    private static int count1 = 0;
    private static boolean check1 = false;
    private static int count2 = 1;
    private static boolean check2 = false;
    private static boolean result = false;

    public BinaryTree(Node node) {
        root = node;
        ausgabe = "";
    }

    public Node getRoot() {
        return root;
    }

        //Umdrehen aller Alternativen in Regel
    public BinaryTree dreheAlternativen(Node n) {
        BinaryTree kopie = new BinaryTree(kopiereTeilbaum(n, null, false));
        drehe(kopie.getRoot());
        return kopie;
    }

    public void drehe(Node n) {
        if (n == null) {
            return;
        }
        if (n.getTyp() == Type.ALTERNATIVE && !n.isAltGedreht()) {
            Node start = n;
            Node zeiger = n;
            Stack<Node> lager = new Stack<Node>(); //Stack zum Zwischenlagern der Inhalte der Alternativen
            //Vertauschen der Alternativen
            while (zeiger.getTyp() == Type.ALTERNATIVE) {
                lager.push(zeiger.getLeft()); //linken Inhalt der Alternative auf Stack => kommt als letztes dran
                zeiger = zeiger.getRight(); //Bis zur letzten Alternative rechts durchschalten
            }
            lager.push(zeiger); //Alternative beendet => Inhalt kommt als erstes dran
            //Zusammenfügen
            while (lager.size() != 2) {
                Node temp = lager.pop();
                start.setLeft(temp);
                temp.setTop(start);
                start.setRight(new Node(Type.ALTERNATIVE, null, start, null, null));
                start.setAltGedreht(true);
                start = start.getRight();
            }
            //letzte Alternative
            Node l = lager.pop();
            l.setTop(start);
            Node r = lager.pop();
            r.setTop(start);
            start.setLeft(l);
            start.setRight(r);
            start.setAltGedreht(true);
        }
        drehe(n.getLeft());
        drehe(n.getRight());
    }
    
    public void isAlternativeInRegel(Node n) {
        if(n == null) {
            return;
        }
        if(n.getTyp() == Type.ALTERNATIVE) {
            alternativeInBaum = true;
        }
        isAlternativeInRegel(n.getLeft());
        isAlternativeInRegel(n.getRight());
    }
    
    public void setCounterMittigRekTree(Node n, int i) {
        if (n == null) {
            return;
        }
        n.setCounterMittigRek(i);
        setCounterMittigRekTree(n.getLeft(), i);
        setCounterMittigRekTree(n.getRight(), i);
    }

    public void resetCountRekWdh(Node n) {
        if (n == null) {
            return;
        }
        n.setCountRekWdh(0);
        resetCountRekWdh(n.getLeft());
        resetCountRekWdh(n.getRight());
    }

    public boolean checkMittigeRek() {
        pruefeMittigeRekursionWdh(this.getRoot());
        count1 = 0;
        count2 = 1;
        check1 = false;
        check2 = false;
        if (result) {
            result = false;
            return true;
        } else {
            return false;
        }
    }

    public void pruefeMittigeRekursionWdh(Node n) {
        if (n == null) {
            return;
        }
        if (n.getTyp() == Type.WDH && n.isRekWdh() && n.isMittigeRekursion()) {
            if (!check1) {
                //WDH1
                check1 = true;
                count1 = n.getCountRekWdh();
            } else if (!check2) {
                //WDH2
                check2 = true;
                count2 = n.getCountRekWdh();
                if (count1 == count2) {
                    result = true;
                }
            } else {
                //Fehler: WDH1 und WDH2 schon besetzt
                System.out.println("FEHLER!!!");
            }
        }
        pruefeMittigeRekursionWdh(n.getLeft());
        pruefeMittigeRekursionWdh(n.getRight());
    }

    public void setzeMittigeRekursion(Node n) {
        if (n == null) {
            return;
        }
        n.setMittigeRekursion(true);
        setzeMittigeRekursion(n.getLeft());
        setzeMittigeRekursion(n.getRight());
    }

    //kopiert einen Teilbaum
    public Node kopiereTeilbaum(Node aktuell, Node top, boolean left) {
        if (aktuell == null) {
            return null;
        }
        Node copy = new Node(aktuell.getTyp(), aktuell.getValue(), top, null, null);
        if (top != null) {
            if (left) {
                top.setLeft(copy);
            } else {
                top.setRight(copy);
            }
        }
        Node copyLeft = kopiereTeilbaum(aktuell.getLeft(), copy, true);
        Node copyRight = kopiereTeilbaum(aktuell.getRight(), copy, false);
        copy.setLeft(copyLeft);
        copy.setRight(copyRight);
        return copy;
    }

    //Erkennt Links-Rekursion
    public boolean erkenneLinksRekursion(Node wurzel) {
        if (wurzel == null || wurzel.getTyp() != Type.P || wurzel.getLeft() == null) {
            return false;
        }
        Node n = wurzel.getLeft();
        while(n.getLeft() != null) {
            n = n.getLeft();
        }
        if(n.getTyp() == Type.NT && n.getValue().equals(wurzel.getValue())) {
            return true;
        }
        return false;
    }

    //Erkennt mittige Rekursionen und ersetzt sie durch eine spezielle WDH-Struktur
    public void ersetzeMittigeRekursion(Node n) {
        if (n == null) {
            return;
        }
        if (n.getTop() != null && n.getTop().getTop() != null) {
            if (n.getTop().getTyp() == Type.REIHE && n.getTop().getTop().getTyp() == Type.REIHE && n.getTyp() == Type.NT && n.getValue().equals(this.getRoot().getValue())) {
                //mittige Rekursion möglich
                Node nach;
                Node vor = n.getTop().getTop();
                Node mitte = n.getTop();
                if (mitte.getLeft() == n) {
                    //n ist links
                    nach = n.getTop().getRight();
                } else {
                    //keine mittige Rekursion
                    return;
                }
                Node ersetzung = n.getTop();
                while (ersetzung.getTop().getTyp() != Type.ALTERNATIVE) {
                    ersetzung = ersetzung.getTop();
                }
                if (ersetzung.getTop().getLeft() == ersetzung) {
                    //links in Alt steht Rekursion
                    ersetzung = kopiereTeilbaum(ersetzung.getTop().getRight(), null, false);
                } else {
                    ersetzung = kopiereTeilbaum(ersetzung.getTop().getLeft(), null, false);
                }
                mitte.setLeft(ersetzung);
                ersetzung.setTop(mitte);
                //
                Node vorHelp = vor;
                vorHelp.setRight(null); //damit dieser Teil nicht mitkopiert wird
                while (vorHelp.getTop().getTyp() != Type.ALTERNATIVE) {
                    vorHelp = vorHelp.getTop();
                }
                Node wdhInhaltVor = kopiereTeilbaum(vorHelp, null, false);
                //Node wdhInhaltVor = kopiereTeilbaum(vor.getLeft(), null, false); //FALSCH!!!
                //
                Node wdhInhaltNach = kopiereTeilbaum(nach, null, false);
                Node wdhVor = new Node(Type.WDH, null, null, wdhInhaltVor, null);
                wdhVor.setRekWdh(true);
                wdhInhaltVor.setTop(wdhVor);
                Node wdhNach = new Node(Type.WDH, null, null, wdhInhaltNach, null);
                wdhNach.setRekWdh(true);
                wdhInhaltNach.setTop(wdhNach);

                Node reiheVor = new Node(Type.REIHE, null, vor, wdhVor, mitte);
                vor.setRight(reiheVor);
                wdhVor.setTop(reiheVor);
                mitte.setTop(reiheVor);

                Node reiheNach = new Node(Type.REIHE, null, mitte, wdhNach, nach);
                mitte.setRight(reiheNach);
                wdhNach.setTop(reiheNach);
                nach.setTop(reiheNach);
                //mittigeRekursion auf true setzen, damit in WDH gezählt wird (in ganzem Baum)
                setzeMittigeRekursion(this.getRoot());
            }
        }
        ersetzeMittigeRekursion(n.getLeft());
        ersetzeMittigeRekursion(n.getRight());
    }

    //Erhöhen der Rekursionstiefe
    public void incTiefe(Node n) {
        if (n == null) {
            return;
        }
        n.setTiefe(n.getTiefe() + 1);
        incTiefe(n.getLeft());
        incTiefe(n.getRight());
    }

    //Erniedrigen der Rekursionstiefe
    public void decTiefe(Node n) {
        if (n == null) {
            return;
        }
        n.setTiefe(n.getTiefe() - 1);
        decTiefe(n.getLeft());
        decTiefe(n.getRight());
    }

    //Erkennt Klammern in Bäumen und erzeugt für diese eigene Regeln
    public void entferneKlammern(Node n, HashMap<String, BinaryTree> map) {
        if (n == null) {
            return;
        }
        if (n.getTyp() == Type.KLAMMER && n.getRight() == null) {
            Node top = n.getTop();
            String name = "klammer" + count_klammer;
            count_klammer++;
            Node klammer = new Node(Type.NT, name, top, null, null);
            if (top.getLeft() == n) {
                top.setLeft(klammer);
            } else {
                top.setRight(klammer);
            }
            Node production = new Node(Type.P, name, null, n.getLeft(), null);
            n.getLeft().setTop(production);
            Node ende = new Node(Type.E, null, production, null, null);
            production.setRight(ende);
            BinaryTree tree = new BinaryTree(production);
            map.put(tree.getRoot().getValue(), tree);
        }
        entferneKlammern(n.getLeft(), map);
        entferneKlammern(n.getRight(), map);
    }

    public void pushToAltStack(Node n, String s) {
        if (n == null) {
            return;
        }
        if (n.getTyp() == Type.ALTERNATIVE && n.isAltStackBesucht()) {
            //fuege zu Stack hinzu
            n.getAltStack().push(s);
        }
        pushToAltStack(n.getLeft(), s);
        pushToAltStack(n.getRight(), s);
    }

    //Löscht alle evtl. vorhandenen Zeichen aus dem AltStack des BinaryTrees
    public void resetAltStack(Node n) {
        if (n == null) {
            return;
        }
        if (n.getTyp() == Type.ALTERNATIVE) {
            while (!n.getAltStack().empty()) {
                n.getAltStack().pop();
            }
        }
        resetAltStack(n.getLeft());
        resetAltStack(n.getRight());
    }

    public void pushToOptionStack(Node n, String s) {
        if (n == null) {
            return;
        }
        if (n.getTyp() == Type.OPTION && n.isOptionBesucht()) {
            //fuege zu Stack hinzu
            n.getOpStack().push(s);
        }
        pushToOptionStack(n.getLeft(), s);
        pushToOptionStack(n.getRight(), s);
    }

    public void pushToWdhStack(Node n, String s) {
        if (n == null) {
            return;
        }
        if (n.getTyp() == Type.WDH && n.isWdhBesucht()) {
            //fuege zu Stack hinzu
            n.getWdhStack().push(s);
        }
        pushToWdhStack(n.getLeft(), s);
        pushToWdhStack(n.getRight(), s);
    }

    public void leereWdhStack(Node n) {
        if (n == null) {
            return;
        }
        while (!n.getWdhStack().empty()) {
            n.getWdhStack().pop();
        }
        leereWdhStack(n.getLeft());
        leereWdhStack(n.getRight());
    }

    public void loescheBesetzt(Node n) {
        if (n == null) {
            return;
        }
        n.setBesucht(false);
        loescheBesetzt(n.getLeft());
        loescheBesetzt(n.getRight());
    }

    //Baumausgabe zum Testen im Simulator (System.out)
    public void print(Node n, int indent) {
        if (n == null) {
            return;
        }
        //Einrücken
        for (int i = 0; i < indent; ++i) {
            System.out.print("  ");
        }
        //Wurzel ausgeben
        System.out.print("|-- ");
        String s = null;
        Type t = n.getTyp();
        if (t == Type.P || t == Type.T || t == Type.NT) {
            s = t + ": " + n.getValue();
            System.out.println(s);
        } else {
            System.out.println(t);
        }
        //linken und rechten Teilbaum ausgeben (rekursiv)
        print(n.getLeft(), indent + 3);
        print(n.getRight(), indent + 3);
    }

    //Baumausgabe für GWT (in String)
    public void printAusgabe(Node n, int indent) {
        if (n == null) {
            return;
        }
        //Einrücken
        for (int i = 0; i < indent; ++i) {
            ausgabe += "&nbsp;&nbsp;&nbsp;&nbsp;";
        }
        //Wurzel ausgeben
        ausgabe += "|--&nbsp;&nbsp;";
        String s = null;
        Type t = n.getTyp();
        if (t == Type.P || t == Type.T || t == Type.NT) {
            s = t + ":&nbsp;&nbsp;" + n.getValue();
            ausgabe += s + "<br>";
        } else {
            ausgabe += t + "<br>";
        }
        //linken und rechten Teilbaum ausgeben (rekursiv)
        printAusgabe(n.getLeft(), indent + 3);
        printAusgabe(n.getRight(), indent + 3);
    }

    //Entferne überflüssige Alternativen
    public void optimiereAlternative(Node n) {
        if (n == null) {
            return;
        }
        if (n.getTyp() == Type.ALTERNATIVE && n.getRight() == null) {
            Node top = n.getTop();
            Node left = n.getLeft();
            boolean l = false;
            if (top.getLeft() == n) {
                l = true;
            }
            //Node aus dem Baum lösen
            if (l) {
                top.setLeft(left);
            } else {
                top.setRight(left);
            }
            left.setTop(top);
        }
        optimiereAlternative(n.getLeft());
        optimiereAlternative(n.getRight());
    }

    //Entferne überflüssige Reihen
    public void optimiereReihe(Node n) {
        if (n == null) {
            return;
        }
        if (n.getTyp() == Type.REIHE && n.getRight() == null) {
            Node top = n.getTop();
            Node left = n.getLeft();
            boolean l = false;
            if (top.getLeft() == n) {
                l = true;
            }
            //Node aus dem Baum lösen
            if (l) {
                top.setLeft(left);
            } else {
                top.setRight(left);
            }
            left.setTop(top);
        }
        optimiereReihe(n.getLeft());
        optimiereReihe(n.getRight());
    }

    public String getAusgabe() {
        return ausgabe;
    }

    public boolean isAlternativeInBaum() {
        return alternativeInBaum;
    }

}
