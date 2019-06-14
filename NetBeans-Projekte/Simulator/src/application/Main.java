/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

/**
 *
 * @author Admin
 */
import interpreter.Interpreter;
import java.util.Map.Entry;
import java.util.Stack;
import tree.BinaryTree;

public class Main {

    static public void main(String argv[]) {
        String ausgabe = "";
        boolean ergebnis = false;

        //Grammatik MiniB
        /*String grammatik = "program = { definition }.\n"
         + "definition = identifier \"(\" [parameter { \",\" parameter } ] \")\" \"{\" expression \";\" \"}\".\n"
         + "parameter = identifier.\n"
         + "expression = condition.\n"
         + "condition = sum | comparisson [ \"?\" expression \":\" expression ].\n"
         + "comparisson = sum CompareOp sum.\n"
         + "CompareOp = \"<\" | \"<=\" | \"==\" | \"!=\" | \">=\" | \">\".\n"
         + "sum = term { (\"+\" | \"-\") term }.\n"
         + "term = factor { (\"*\" | \"/\" | \"%\") factor }.\n"
         + "factor = [\"+\" | \"-\"] number | parameter | functionCall | \"(\" expression \")\".\n"
         + "functionCall = identifier \"(\" [expression { \",\" expression } ] \")\".\n"
         + "number = digit {digit} [ \".\" digit {digit} ].\n"
         + "identifier = letter { letter }.\n"
         + "letter = \"a\" | \"b\" | \"c\" | \"d\" | \"e\" | \"f\" | \"g\" | \"h\" | \"i\" | \"j\" | \"k\" | \"l\" | \"m\" | \"n\" | \"o\" | \"p\" | \"q\" | \"r\" | \"s\" | \"t\" | \"u\" | \"v\" | \"w\" | \"x\" | \"y\" | \"z\" | \"A\" | \"B\" | \"C\" | \"D\" | \"E\" | \"F\" | \"G\" | \"H\" | \"I\" | \"J\" | \"K\" | \"L\" | \"M\" | \"N\" | \"O\" | \"P\" | \"Q\" | \"R\" | \"S\" | \"T\" | \"U\" | \"V\" | \"W\" | \"X\" | \"Y\" | \"Z\".\n"
         + "digit = \"0\" | \"1\" | \"2\" | \"3\" | \"4\" | \"5\" | \"6\" | \"7\" | \"8\" | \"9\".";*/
        
        //EBNF-Selbstdefinition
        /*String grammatik = "syntax = { production }.\n"
                + "production = identifier \"=\" expression \".\".\n"
                + "expression = term { \"|\" term }.\n"
                + "term = factor { factor }.\n"
                + "factor = identifier | string | \"(\" expression \")\" | \"[\" expression \"]\" | \"{\" expression \"}\".\n"
                + "identifier = letter { digit | letter }.\n"
                + "string = \"\"\"{character}\"\"\".\n"
                + "letter = \"a\" | \"b\" | \"c\" | \"d\" | \"e\" | \"f\" | \"g\" | \"h\" | \"i\" | \"j\" | \"k\" | \"l\" | \"m\" | \"n\" | \"o\" | \"p\" | \"q\" | \"r\" | \"s\" | \"t\" | \"u\" | \"v\" | \"w\" | \"x\" | \"y\" | \"z\" | \"A\" | \"B\" | \"C\" | \"D\" | \"E\" | \"F\" | \"G\" | \"H\" | \"I\" | \"J\" | \"K\" | \"L\" | \"M\" | \"N\" | \"O\" | \"P\" | \"Q\" | \"R\" | \"S\" | \"T\" | \"U\" | \"V\" | \"W\" | \"X\" | \"Y\" | \"Z\".\n"
                + "digit = \"0\" | \"1\" | \"2\" | \"3\" | \"4\" | \"5\" | \"6\" | \"7\" | \"8\" | \"9\".\n"
                + "character = digit | letter.";*/
        
        //Java-Integerliterale
        String grammatik = "intLiteral = [\"+\" | \"-\"](octLiteral | decLiteral | hexLiteral)[\"l\" | \"L\"].\n" +
"octLiteral = \"0\" octDigit { octDigit }.\n" +
"octDigit = \"0\" | \"1\" | \"2\" | \"3\" | \"4\" | \"5\" | \"6\" | \"7\".\n" +
"decLiteral = digit1 { digit } | \"0\".\n" +
"hexLiteral = (\"0x\" | \"0X\") hexDigit { hexDigit }.\n" +
"hexDigit = digit | \"a\" | \"A\" | \"b\" | \"B\" | \"c\" | \"C\" | \"d\" | \"D\" | \"e\" | \"E\" | \"f\" | \"F\".\n" +
"digit = \"0\" | digit1.\n" +
"digit1 = \"1\" | \"2\" | \"3\" | \"4\" | \"5\" | \"6\" | \"7\" | \"8\" | \"9\".";

        //MiniB
        //String eingabe = "fakultaet(n){n<=1?1:n*fakultaet(n-1);}";
        //String eingabe = "main(i){fakultaet(i);}";
        //String eingabe = "fakultaet(n){fak(n-1);}main(i){fakultaet(i);}";
        
        //EBNF-Selbstdefinition
        //String eingabe = "regel=([\"a\"|\"b\"]test1{test2|test3})|\"TERMINAL\".";
        //String eingabe = "regel=([\"a\"|\"b\"]test1{test2|test3})|\"TERMINAL\".regel2={test4[true|false]}.number=digit{digit}[\"PUNKT\"digit{digit}].";
        
        //Java-Integerliterale
        //String eingabe = "-2015L";
        //String eingabe = "0xFFAB";
        String eingabe = "-017l";
         
        String start = "intLiteral";
        Interpreter i = new Interpreter();
        i.setGrammatik(grammatik);
        i.initParsing(); //Parsen
        System.out.println(i.getLexParseAusgabe()); //Anzeigen der Parserausgabe
        i.setEingabe(eingabe);
        //i.printAllRules(); //Anzeigen aller Syntaxbäume
        i.initInterpreting(start); //Interpretieren mit Angabe der Startregel
        ausgabe = i.getInterpreterAusgabe();
        ergebnis = i.isInterpretError();
        //Alternativen einzeln umdrehen => 16.06.2016
        if (ergebnis) {
            Stack<BinaryTree> myRuleStack = new Stack<BinaryTree>();
            //alle Regeln in Stack einfügen
            for (Entry<String, BinaryTree> e : i.getTreeMap().entrySet()) {
                BinaryTree t = e.getValue();
                t.isAlternativeInRegel(t.getRoot());
                if (t.isAlternativeInBaum()) {
                    myRuleStack.push(t);
                }
            }
            while (!myRuleStack.isEmpty()) {
                //Initalisieren
                Interpreter i2 = new Interpreter();
                i2.setGrammatik(i.getGrammatik());
                i2.setEingabe(eingabe);
                i2.initParsing();
                //Umdrehen
                String name = myRuleStack.pop().getRoot().getValue();
                BinaryTree tree = i2.getTreeMap().get(name);
                tree = tree.dreheAlternativen(tree.getRoot());
                i2.getTreeMap().remove(name);
                i2.getTreeMap().put(name, tree);
                //Interpretieren
                i2.initInterpreting(start);
                if (!i2.isInterpretError()) {
                    ausgabe = i2.getInterpreterAusgabe();
                    ergebnis = i2.isInterpretError();
                    break;
                }
            }
        }
        System.out.println(ausgabe); //Anzeigen der Interpreterausgabe
    }
}
