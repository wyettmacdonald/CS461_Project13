/*
 * File: CodeTab.java
 * Edited By: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Project 9
 * Date: November 20, 2018
 */

package proj15DouglasMacDonaldZhang;

import javafx.event.Event;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.LineNumberFactory;

/**
 * This class subclasses Tab to contain a CodeArea
 *
 * Handles close requests and the Context Menus for the code area and the tab
 *
 * @author  Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 */
public class CodeTab extends Tab{

    private MasterController masterController;
    private CodeAreaContextMenu codeAreaContextMenu;
    private CodeTabPane codeTabPane;

    /**
     * Constructor for the codeTab
     * @param masterController the reference to the master controller
     * @param codeAreaContextMenu reference to the codeAreaContextMenu field
     * @param tabContextMenu reference to the tab context menu
     * @param codeTabPane reference to the code tab pane
     * @param filename  the filename reference
     * @param content the content in the codeArea in the format of a string
     */
    public CodeTab(MasterController masterController,CodeAreaContextMenu codeAreaContextMenu,
                   TabContextMenu tabContextMenu, CodeTabPane codeTabPane, String filename, String content){
        super(filename);
        this.masterController = masterController;
        this.codeAreaContextMenu = codeAreaContextMenu;
        this.codeTabPane = codeTabPane;
        if(filename.endsWith(".asm") || filename.endsWith(".s")) {
            MIPSStyle style = new MIPSStyle();
            this.addCodeArea(content, style);
            this.setOnSelectionChanged( event -> {
                if (isSelected()){
                    masterController.setMipsButtons();
                }
            });
        }
        else if(filename.endsWith(".btm")){
            JavaStyle style = new JavaStyle();
            this.addCodeArea(content, style);
            this.setOnSelectionChanged( event -> {
                if (isSelected()){
                    masterController.setBantamButtons();
                }
            });
        }
        this.setOnCloseRequest(event -> this.masterController.handleClose(event));
        this.setContextMenu(tabContextMenu);
    }


    /**
     * Creates a code area, adds it to a VirtualizedScrollPane, and then adds the scroll pane to the tab object.
     * @param content content to add to the code area if opening a file
     */
    public void addCodeArea(String content, Style type){
        // creation of the codeArea
        IDECodeArea codeArea = new IDECodeArea(type);
        codeArea.setOnKeyPressed(event -> this.codeTabPane.resetSaveStatus());
        codeArea.replaceText(content);
        codeArea.setContextMenu(this.codeAreaContextMenu);
        codeArea.setOnContextMenuRequested(event->this.codeAreaContextMenu.bindCutCopyMenuItems(codeArea));
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.getStyleClass().add("code-area");

        VirtualizedScrollPane scrollPane = new VirtualizedScrollPane<>(codeArea,
                ScrollPane.ScrollBarPolicy.ALWAYS,
                ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.getStyleClass().add("virtual-scroll-pane");
        this.setContent(scrollPane);
    }
}
