/*
 * File: SuggestionsContextMenu.java
 * CS461 Project 13
 * Names: Wyett MacDonald, Kyle Douglas, Tia Zhang
 * Date: 2/28/19
 * This file contains the SuggestionsContextMenu, which makes a menu of spelling suggestions and replaces highlighted text
 * with whatever is selected
 *
 */


//In the future, I'm hoping to have it take in a list of an interface instead of IdentifierInfo to make it more flexible -Tia
//so it could also do stuff like spelling suggestions

package proj13DouglasMacDonaldZhang;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.fxmisc.richtext.CodeArea;
import proj13DouglasMacDonaldZhang.bantam.semant.IdentifierInfo;

import java.util.ArrayList;

public class SuggestionsContextMenu extends ContextMenu {
    ArrayList<MenuItem> menuItems;

    /**
    * Constructor for the SuggestionsContextMenu
    * @param suggestionsList is the ArrayList of IdentifierInfo objects that will
    */
    public SuggestionsContextMenu(ArrayList<IdentifierInfo> suggestionsList, CodeArea codeArea){
        super();
        menuItems = new ArrayList<>();
        suggestionsList.forEach(suggestion ->{
            //System.out.println("Making a new menu item");
            MenuItem suggestionItem = new MenuItem();
            suggestionItem.setText(suggestion.getType() + " " + suggestion.getName());
            suggestionItem.setOnAction(event -> replace(codeArea, suggestion.getName()));
            this.getItems().add(suggestionItem);
            this.setAutoHide(true);
            this.setHideOnEscape(true);
        });


    }

    /**
    * Helper method that replaces selected text with other text
    * @param codeArea is the CodeArea in which text should be replaced
    * @param replacer is the string which will replace it. Should come from menu item
    */
    private void replace(CodeArea codeArea, String replacer){
        codeArea.replaceSelection(replacer);
    }
}
