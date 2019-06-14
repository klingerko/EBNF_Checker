/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ebnfChecker.gui.client;

import com.ait.toolkit.clientio.client.ClientIO;
import com.ait.toolkit.clientio.client.eventhandling.ClientIoFileSelectHandler;
import com.ait.toolkit.flash.core.client.utils.ByteArray;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.Map.Entry;
import java.util.Stack;
import org.ebnfChecker.simulator.interpreter.Interpreter;
import org.ebnfChecker.simulator.tree.*;

/**
 * Main entry point.
 *
 * @author Admin
 */
public class EBNF_CheckerEntryPoint implements EntryPoint {

    /**
     * Creates a new instance of EBNF_CheckerEntryPoint
     */
    public EBNF_CheckerEntryPoint() {
    }

    /**
     * The entry point method, called automatically by loading a module that
     * declares an implementing class as an entry-point
     */
    @Override
    public void onModuleLoad() {
        Resources.INSTANCE.css().ensureInjected();
        Window.setTitle("EBNF-Checker");
        ClientIO.init(); //Initialisierung der ClientIO (wird zum Speichern und Laden von Grammatiken verwendet)
        final Interpreter myInterpreter = new Interpreter(); //Simulator (Interpreter, Parser und Scanner)

        //Widgets für Grammatik-Teil
        final TextArea textGrammatik = new TextArea(); //Textfeld für Eingabe der EBNF-Grammatik
        final Button buttonGrammatik = new Button("Parse"); //Beginn des Scan- und Parsevorgangs
        DOM.setElementAttribute(buttonGrammatik.getElement(), "id", "buttonGrammatik-id");
        final Button buttonSpeichern = new Button("Save"); //Button zum Speichern einer Grammatik mittels ClientIO
        final Button buttonLaden = new Button("Upload"); //Button zum Hochladen einer Grammatik mittels ClientIO
        final DecoratedPopupPanel ausgabeGrammatik = new DecoratedPopupPanel(true); //Hier wird die Parserausgabe dargestellt

        //Widgets für Baum-Teil
        final DisclosurePanel baumAnzeige = new DisclosurePanel("Syntax trees"); //Anzeige optional (Auf- und Zuklappen)
        DOM.setElementAttribute(baumAnzeige.getElement(), "id", "baumAnzeige-id");
        final HorizontalPanel baumAuswahl1 = new HorizontalPanel(); //Regeln 1-10
        final HorizontalPanel baumAuswahl2 = new HorizontalPanel(); //Regeln 10-20
        final HorizontalPanel baumAuswahl3 = new HorizontalPanel(); //Regeln 20-...
        final HorizontalPanel baumText = new HorizontalPanel();

        //Widgets für Eingabe-Teil
        final TextArea textEingabe = new TextArea(); //Eingabe zur eingegebenen EBNF-Grammtik
        final TextArea textStartregel = new TextArea();//Startregel Angabe
        final Button buttonEingabe = new Button("Interpret"); //Beginn des Interpreter-Durchlaufs
        DOM.setElementAttribute(buttonEingabe.getElement(), "id", "buttonEingabe-id");
        final DecoratedPopupPanel ausgabeEingabe = new DecoratedPopupPanel(true); //Hier wird die Interpreterausgabe dargestellt
        final Button resetEingabe = new Button("Reset"); //Zurücksetzen der Ein- und Ausgabe im Eingabe-Teil (nicht im Grammatik-Teil)

        //Logos & Widgets für das obere und untere Ende der Seite
        final Button resetAll = new Button("Reset all"); //Zurücksetzen aller Eingaben und Ausgaben (zurück auf Startzustand)
        DOM.setElementAttribute(resetAll.getElement(), "id", "resetAll-id");
        final Image unibwLogo = new Image();
        final Image ebnfCheckerLogo = new Image();
        DOM.setElementAttribute(ebnfCheckerLogo.getElement(), "id", "ebnfCheckerLogo-id");
        unibwLogo.setUrl("unibw_logo.png");
        ebnfCheckerLogo.setUrl("ebnfChecker_logo.png");
        final Anchor anchorKontact = new Anchor("konstantin.klinger@unibw.de", "mailto:konstantin.klinger@unibw.de");
        final Button buttonBeispiel = new Button("Show example");

        //Hilfe / About
        final DecoratedPopupPanel about = new DecoratedPopupPanel(true);
        final Button aboutButton = new Button("About");
        String s = "<b>EBNF-Checker</b><br /><br />"
                + "\"EBNF-Checker\" is a simulator for the Extended Backus–Naur Form."
                + "You can parse your personal EBNF grammar rules, written in \"Wirth syntax notation\". Optionally you can view the syntax trees of your productions.<br />"
                + "After that you can type in a specific input for your grammar and start to interpret with a start rule.<br /><br />"
                + "If you still don't know what to do, the example will help you.<br /><br />"
                + "<u>Note:</u><br />"
                + "<ul><li>You need at least \"Adobe Flash Player 11\" installed to use save and load function for your grammar input.</li>"
                + "<li>There is no support for left-recursive syntax interpretation right now.</li></ul>";
        final HTML aboutText = new HTML(s);
        DOM.setElementAttribute(aboutText.getElement(), "id", "aboutText-id");
        about.setWidget(aboutText);

        //Beschränkungen & Aktionen & Sichtbarkeit
        //textGrammatik auf feste Größe setzen
        textGrammatik.getElement().getStyle().setProperty("maxWidth", "530px");
        textGrammatik.getElement().getStyle().setProperty("maxHeight", "300px");
        textGrammatik.getElement().getStyle().setProperty("minWidth", "530px");
        textGrammatik.getElement().getStyle().setProperty("minHeight", "300px");
        //textEingabe auf feste Größe setzen
        textEingabe.getElement().getStyle().setProperty("maxWidth", "420px");
        textEingabe.getElement().getStyle().setProperty("maxHeight", "50px");
        textEingabe.getElement().getStyle().setProperty("minWidth", "420px");
        textEingabe.getElement().getStyle().setProperty("minHeight", "50px");
        //textStartregel auf feste Größe setzen
        textStartregel.getElement().getStyle().setProperty("maxWidth", "103px");
        textStartregel.getElement().getStyle().setProperty("maxHeight", "50px");
        textStartregel.getElement().getStyle().setProperty("minWidth", "103px");
        textStartregel.getElement().getStyle().setProperty("minHeight", "50px");

        textEingabe.setEnabled(false); //zu Beginn nicht beschreibar
        textStartregel.setEnabled(false); // zu Beginn nicht beschreibar
        baumAnzeige.setVisible(false); //zu Beginn nicht sichtbar
        buttonEingabe.setEnabled(false); //erst nach Parsen bedienbar
        resetEingabe.setEnabled(false); //erst nach Parsen bedienbar

        //buttonBeispiel-Aktion ("Show example")
        buttonBeispiel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                textGrammatik.setText("digit = \"0\" | \"1\" | \"2\" | \"3\" | \"4\" | \"5\" | \"6\" | \"7\" | \"8\" | \"9\".\n"
                        + "number = digit {digit}.\n"
                        + "float = number  [\".\" number].\n"
                        + "date = [digit] digit \".\" [digit] digit [\".\"[digit digit] digit digit].");
                textEingabe.setText("13.07.2014");
                textStartregel.setText("date");
                //zeurst wieder Parsen
                DOM.setElementAttribute(buttonGrammatik.getElement(), "id", "buttonGrammatik-id");
                DOM.setElementAttribute(buttonEingabe.getElement(), "id", "buttonEingabe-id");
                textEingabe.setEnabled(false);
                textStartregel.setEnabled(false);
                buttonEingabe.setEnabled(false);
                resetEingabe.setEnabled(false);
            }
        });

        //buttonGrammatik-Aktion ("Parse")
        buttonGrammatik.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                buttonGrammatik.setEnabled(false); //nicht bedienbar für die Zeit des Parsens
                DOM.setElementAttribute(buttonEingabe.getElement(), "id", "buttonEingabe-id");
                String s = textGrammatik.getText(); //Eingabe EBNF-Grammatik
                //Übergabe an Simulator und Holen der Parserausgabe
                myInterpreter.setGrammatik(s);
                myInterpreter.initParsing();
                //evtl. vorhandenen Bäume entfernen
                baumAuswahl1.clear();
                baumAuswahl2.clear();
                baumAuswahl3.clear();
                baumText.clear();
                //Erfolg überprüfen
                if (!myInterpreter.isSyntaxError()) {
                    //Parsen erfolgreich
                    DOM.setElementAttribute(buttonGrammatik.getElement(), "id", "buttonGrammatikRichtig-id");
                    HTML popUpText = new HTML(new SafeHtmlBuilder().appendEscapedLines(myInterpreter.getLexParseAusgabe()).toSafeHtml());
                    DOM.setElementAttribute(popUpText.getElement(), "id", "popUpTextRichtig-id");
                    ausgabeGrammatik.setWidget(popUpText);
                    ausgabeGrammatik.center();
                    buttonEingabe.setEnabled(true);
                    resetEingabe.setEnabled(true);
                    textEingabe.setEnabled(true);
                    textStartregel.setEnabled(true);
                    //Syntaxbäume hinzufügen
                    int gesamtAnzahl = myInterpreter.getTreeMap().size();
                    if (gesamtAnzahl < 21) {
                        baumAuswahl3.setVisible(false);
                    } else if (gesamtAnzahl < 11) {
                        baumAuswahl2.setVisible(false);
                        baumAuswahl3.setVisible(false);
                    }
                    int anzahl = 0;
                    for (Entry<String, BinaryTree> e : myInterpreter.getTreeMap().entrySet()) {
                        anzahl++;
                        //die Bäume aller Regeln durchlaufen
                        BinaryTree tree = e.getValue();
                        tree.printAusgabe(tree.getRoot(), 3); //im Objekt der Klasse BinaryTree wird der String "ausgabe" gefüllt
                        final Button button = new Button(tree.getRoot().getValue());
                        DOM.setElementAttribute(button.getElement(), "id", "button-id");
                        final HTML text = new HTML(tree.getAusgabe());
                        //text.setVisible(false);
                        DOM.setElementAttribute(text.getElement(), "id", "baumText-id");

                        if (anzahl < 11) {
                            baumAuswahl1.add(button);
                        } else if (anzahl < 21) {
                            baumAuswahl2.add(button);
                        } else {
                            baumAuswahl3.add(button);
                        }
                        button.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                baumText.clear();
                                baumText.add(text);
                            }
                        });
                    }
                    baumAnzeige.setVisible(true); //Bäume sichtbar machen
                } else {
                    //Parsen nicht erfolgreich
                    DOM.setElementAttribute(buttonGrammatik.getElement(), "id", "buttonGrammatikFalsch-id");
                    HTML popUpText = new HTML(new SafeHtmlBuilder().appendEscapedLines(myInterpreter.getLexParseAusgabe()).toSafeHtml());
                    DOM.setElementAttribute(popUpText.getElement(), "id", "popUpTextFalsch-id");
                    ausgabeGrammatik.setWidget(popUpText);
                    ausgabeGrammatik.center();
                    buttonEingabe.setEnabled(false);
                    resetEingabe.setEnabled(false);
                    textEingabe.setEnabled(false);
                    textStartregel.setEnabled(false);
                    baumAnzeige.setVisible(false);
                }
                buttonGrammatik.setEnabled(true); //wieder bedienbar für nächsten Parse-Vorgang
            }
        });

        //buttonSpeichern-Aktion ("Save")
        buttonSpeichern.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClientIO.saveFile(textGrammatik.getText(), "file.txt"); //speichert den aktuellen Inhalt der Grammatik-Eingabe
            }
        });

        //buttonLaden-Aktion ("Upload")
        buttonLaden.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClientIO.addFileSelectHandler(new ClientIoFileSelectHandler() {
                    @Override
                    public void onFileLoaded(String fileName, String fileType, ByteArray data, double fileSize) {
                        textGrammatik.setText(data.readMultiByte(fileSize, "UTF-8"));
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onIoError(String errorMessage) {
                    }
                });
                ClientIO.browse();
            }
        });

        //buttonEingabe-Aktion ("Interpret")
        buttonEingabe.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boolean ergebnis = false;
                String ausgabe = "";
                buttonEingabe.setEnabled(false); //nicht bedienbar für die Zeit des Interpretierens
                //nur bedienbar, wenn schon erfolgreich geparst wurde
                String eingabe = textEingabe.getText(); //zu überprüfende Eingabe
                String start = textStartregel.getText(); //Startregel
                //Übergabe an Simulator und Holen der Interpreterausgabe
                Interpreter i = new Interpreter();
                i.setGrammatik(myInterpreter.getGrammatik());
                i.initParsing();
                i.setEingabe(eingabe);
                i.initInterpreting(start);
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

                HTML popUpText;
                if (ergebnis) {
                    //rote Ausgabe (falsch)
                    DOM.setElementAttribute(buttonEingabe.getElement(), "id", "buttonEingabeFalsch-id");
                    popUpText = new HTML(new SafeHtmlBuilder().appendEscapedLines(ausgabe).toSafeHtml());
                    DOM.setElementAttribute(popUpText.getElement(), "id", "popUpTextFalsch-id");
                } else {
                    //grüne Ausgabe (richtig)
                    DOM.setElementAttribute(buttonEingabe.getElement(), "id", "buttonEingabeRichtig-id");
                    popUpText = new HTML(new SafeHtmlBuilder().appendEscapedLines(ausgabe).toSafeHtml());
                    DOM.setElementAttribute(popUpText.getElement(), "id", "popUpTextRichtig-id");
                }
                ausgabeEingabe.setWidget(popUpText);
                ausgabeEingabe.center(); //mittiges PopUp
                buttonEingabe.setEnabled(true); //wieder bedienbar für erneutes Interpretieren
            }

        });

        //resetEingabe-Aktion ("Reset")
        resetEingabe.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                DOM.setElementAttribute(buttonEingabe.getElement(), "id", "buttonEingabe-id");
                textEingabe.setText(""); //Reset TextArea für Eingabe-Teil
                textStartregel.setText(""); //Reset TextArea für Startregel
            }

        });

        //resetAll-Aktion ("Reset all")
        resetAll.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                DOM.setElementAttribute(buttonEingabe.getElement(), "id", "buttonEingabe-id");
                DOM.setElementAttribute(buttonGrammatik.getElement(), "id", "buttonGrammatik-id"); //Button wieder normal gefärbt
                textGrammatik.setText(""); //Reset TextArea für EBNF-Grammatik
                baumAnzeige.setVisible(false); //Bäume wieder verstecken (gelöscht werden sie beim erneuten Parsen)
                textEingabe.setText(""); //Reset TextArea für Eingabe-Teil
                textEingabe.setEnabled(false); //nicht beschreibbar
                textStartregel.setText(""); //Reset TextArea für Startregel
                textStartregel.setEnabled(false); //nicht beschreibbar
                buttonEingabe.setEnabled(false);
                resetEingabe.setEnabled(false);
            }
        });

        //aboutButton-Aktion ("About")
        aboutButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                about.center();
            }
        });

        //Einrichten der Seite
        //Panels
        final RootPanel rootPanel = RootPanel.get(); //rootPanel (gesamte Seite)
        final HorizontalPanel hSeite = new HorizontalPanel(); //Unterteilung der Seite in linke und rechte Hälfte
        DOM.setElementAttribute(hSeite.getElement(), "id", "hSeite-id");
        final VerticalPanel vLinks = new VerticalPanel(); //linke Hälfte
        final VerticalPanel vRechts = new VerticalPanel(); //rechte Hälfte
        DOM.setElementAttribute(vRechts.getElement(), "id", "vRechts-id");

        //Baum-Teil
        VerticalPanel vBaum = new VerticalPanel();
        vBaum.add(baumAuswahl1);
        vBaum.add(baumAuswahl2);
        vBaum.add(baumAuswahl3);
        vBaum.add(baumText);
        baumAnzeige.add(vBaum);

        //Grammatik-Teil
        final HorizontalPanel hGrammatikButton = new HorizontalPanel(); //Einfügen der Button im Grammatik-Teil
        hGrammatikButton.add(buttonGrammatik);
        hGrammatikButton.add(buttonSpeichern);
        hGrammatikButton.add(buttonLaden);

        //Eingabe-Teil
        final HorizontalPanel hEingabeButton = new HorizontalPanel(); //Einfügen der Button im Eingabe-Teil
        hEingabeButton.add(buttonEingabe);
        hEingabeButton.add(resetEingabe);
        final HorizontalPanel hEingabe = new HorizontalPanel(); //Unterteilung in Eingabe und Startregel
        final VerticalPanel vEingabeLinks = new VerticalPanel(); //linke Hälfte (Eingabe)
        final VerticalPanel vEingabeRechts = new VerticalPanel(); //rechte Hälfte (Startregel)
        vEingabeLinks.add(new Label("Please type in your input for your EBNF-Grammar:"));
        vEingabeLinks.add(textEingabe);
        vEingabeRechts.add(new Label("Start with rule:"));
        vEingabeRechts.add(textStartregel);
        hEingabe.add(vEingabeLinks);
        hEingabe.add(vEingabeRechts);
        vEingabeRechts.setCellHorizontalAlignment(textStartregel, HorizontalPanel.ALIGN_RIGHT); //rechtsbündig

        //Zusammenfügen und Einfügen aller Abschnitte im Grammatik- und Eingabeteil
        final VerticalPanel vSimulator = new VerticalPanel();
        vSimulator.add(new Label("Please type in your EBNF-Grammar:"));
        vSimulator.add(textGrammatik);
        vSimulator.add(hGrammatikButton);
        vSimulator.add(hEingabe);
        vSimulator.add(hEingabeButton);
        vSimulator.add(resetAll);
        //Ausrichtungen der Button rechtsbündig
        vSimulator.setCellHorizontalAlignment(hGrammatikButton, HorizontalPanel.ALIGN_RIGHT);
        vSimulator.setCellHorizontalAlignment(hEingabeButton, HorizontalPanel.ALIGN_RIGHT);
        vSimulator.setCellHorizontalAlignment(resetAll, HorizontalPanel.ALIGN_RIGHT);

        //Kontaktaufnahme mit Entwickler
        final HorizontalPanel hKontakt = new HorizontalPanel();
        hKontakt.add(new Label("For feedback, bug and issue support: "));
        hKontakt.add(anchorKontact);

        //Einfügen der Widgets und Panels
        //linke Seite
        vLinks.add(ebnfCheckerLogo);
        vLinks.add(buttonBeispiel);
        vLinks.setCellHorizontalAlignment(buttonBeispiel, HorizontalPanel.ALIGN_RIGHT);
        vLinks.add(vSimulator);
        vLinks.add(unibwLogo);
        vLinks.add(new Label("Developed by Konstantin Klinger (2015)"));
        vLinks.add(hKontakt);
        vLinks.add(aboutButton);

        //rechte Seite
        String beschreibung = "\"EBNF-Checker\" is a simulator for the Extended Backus–Naur Form.\n"
                + "You can parse your personal grammar rules and interpret a specific input for it.";
        //+ "(Note: You need \"Flash\" to use save and load function for your grammar input.)";
        HTML textBeschreibung = new HTML(new SafeHtmlBuilder().appendEscapedLines(beschreibung).toSafeHtml());
        DOM.setElementAttribute(textBeschreibung.getElement(), "id", "textBeschreibung-id");
        vRechts.add(textBeschreibung);
        vRechts.add(baumAnzeige);

        //linke und rechte Seite zusammenführen
        hSeite.add(vLinks);
        hSeite.add(vRechts);
        rootPanel.add(hSeite);
    }
}
