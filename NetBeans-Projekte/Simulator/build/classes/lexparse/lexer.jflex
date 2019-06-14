package lexparse;

import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java.io.StringReader;

%%

%public
%class Scanner
%line
%column
%char

%cup

%{
    private ComplexSymbolFactory symbolFactory; //Verwalten des Symbolstroms
    private String scannerAusgabe = ""; //Fehlermeldungen des Scanners

    public Scanner(StringReader in, ComplexSymbolFactory sf){
		this(in);
		symbolFactory = sf;
    }
    
    //Speichern der Position des Symbols in ComplexSymbolFactory von CUP
    private Symbol symbol(String name, int sym) {
        Location left = new Location(yyline+1,yycolumn+1,yychar);
        Location right = new Location(yyline+1,yycolumn+yylength(),yychar+yylength());
        return symbolFactory.newSymbol(name, sym, left, right);
    }

    //Speichern der Position des Symbols in ComplexSymbolFactory von CUP mit Wert (nur bei Terminal und Nichtterminal)
    private Symbol symbol(String name, int sym, String val) {
        Location left = new Location(yyline+1,yycolumn+1,yychar);
        Location right= new Location(yyline+1,yycolumn+yylength(), yychar+yylength());
        return symbolFactory.newSymbol(name, sym, left, right,val);
    }
    
    //lexikalischer Fehler (Eingabe unerlaubter Zeichen)
    private void error(String message) {
        if(scannerAusgabe.equals("")) {
            scannerAusgabe = message;
        } else if(!message.equals("")) {
            scannerAusgabe = scannerAusgabe + "\n" + message;
        }
    }

    public String getScannerAusgabe() {
        return scannerAusgabe;
    }

%}

%eofval{
     Location left = new Location(yyline+1,yycolumn+1,yychar);
     Location right = new Location(yyline+1,yycolumn+1,yychar+1);
     return symbolFactory.newSymbol("EOF", ParserSym.EOF, left, right);
%eofval}

%%

[ \t\r\f\n]+                    { break; }
"="                             { return symbol("ZUWEISUNG \"=\"", ParserSym.ZUWEISUNG); }
"."                             { return symbol("ENDE \".\"", ParserSym.ENDE); }
"("                             { return symbol("KLAMMER_AUF \"(\"", ParserSym.KLAMMER_AUF); }
")"                             { return symbol("KLAMMER_ZU \")\"", ParserSym.KLAMMER_ZU); }
"|"                             { return symbol("ODER \"|\"", ParserSym.ODER); }
"["                             { return symbol("OPTION_AUF \"[\"", ParserSym.OPTION_AUF); }
"]"                             { return symbol("OPTION_ZU \"]\"", ParserSym.OPTION_ZU); }
"{"                             { return symbol("WDH_AUF \"{\"", ParserSym.WDH_AUF); }
"}"                             { return symbol("WDH_ZU \"}\"", ParserSym.WDH_ZU); }

"\""(([^\"]+)|\")"\""           { return symbol("TERMINAL \""+yytext()+"\"", ParserSym.TERMINAL, new String(yytext().substring(1, yytext().length()-1))); }
[a-zA-Z][a-zA-Z0-9]*            { return symbol("NICHTTERMINAL \""+yytext()+"\"", ParserSym.NICHTTERMINAL, new String(yytext())); }

.                               { error("Error at line "+(yyline+1)+", column "+(yycolumn+1)+": Illegal character \""+yytext()+"\" (ignored)"); }