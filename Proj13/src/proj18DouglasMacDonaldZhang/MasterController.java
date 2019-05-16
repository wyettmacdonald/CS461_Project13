/*
 * File: MasterController.java
 * Names: Kevin Ahn, Matt Jones, Jackie Hang, Kevin Zhou
 * Class: CS 361
 * Project 4
 * Date: October 2, 2018
 * ---------------------------
 * Edited By: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 * ---------------------------
 * Edited By: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 *  ---------------------------
 * Edited By: Zeb Keith-Hardy, Danqing Zhao, Tia Zhang
 * Class: CS 461
 * Project 11
 * Date: February 13, 2019
 *  ---------------------------
 * Edited By: Tia Zhang and Danqing Zhao
 * Class: CS 461
 * Project 12
 * Date: February 25, 2019
 */

package proj18DouglasMacDonaldZhang;

import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.event.Event;
import org.fxmisc.richtext.CodeArea;

import java.io.*;
import java.util.Optional;

/**
 * This is the master controller for the program. it references
 * the other controllers for proper menu functionality.
 *
 * @author  Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * @version 2.0
 * @since   10-3-2018
 */
public class MasterController {
    @FXML private Menu editMenu;
    @FXML private CodeTabPane codeTabPane;
    @FXML private VBox vBox;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem closeMenuItem;
    @FXML private Console console;
    @FXML private TabPane structureTabPane;
    @FXML private CheckMenuItem fileStructureItem;
    @FXML private CheckMenuItem directoryTreeItem;
    @FXML private CheckMenuItem suggestionsModeItem;

    @FXML private Button scanButton;
    @FXML private Button scanParseButton;
    @FXML private Button scanParseCheckButton;
    @FXML private Button compileButton;

    @FXML private Button assembleButton;
    @FXML private Button assembleAndRunButton;
    @FXML private Button stopButton;

    @FXML private Button prettyPrintButton;

    @FXML private Button findUsesButton;
    @FXML private Button findUnusedButton;
    @FXML private Button suggestNamesButton;
    @FXML private Button headerDocButton;
    @FXML private Button prevErrorButton;
    @FXML private Button nextErrorButton;
    @FXML private Button prevMethodButton;
    @FXML private Button nextMethodButton;


    @FXML private TreeView<String> directoryTree;
    @FXML private TreeView<String> fileStructureTree;

    private EditController editController;
    private FileController fileController;
    private ToolbarController toolbarController;
    private PreferenceController preferenceController;
    private DirectoryController directoryController;
    private HelpMenuController helpMenuController;
    private StructureViewController structureViewController;


    /**
     * Initializes all the controllers and binds some properties
     */
    public void initialize(){
        //make sure the user doesn't close the structure tabs
        this.structureTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        //initialize the popup menus that are clicked on CodeArea, tab, and console
        CodeAreaContextMenu codeAreaContextMenu = new CodeAreaContextMenu(this);
        TabContextMenu tabContextMenu = new TabContextMenu(this);
        ConsoleContextMenu consoleContextMenu = new ConsoleContextMenu(this);

        //initialize the controllers
        this.editController = new EditController(this.codeTabPane);
        this.fileController = new FileController(this.vBox,this.codeTabPane,this);
        this.toolbarController = new ToolbarController(this.console, this.codeTabPane);

        this.preferenceController = new PreferenceController(this.vBox, this.console, structureTabPane,
                fileStructureItem,directoryTreeItem, this.suggestionsModeItem, this.fileStructureTree,this.directoryTree);
        this.helpMenuController = new HelpMenuController();

        this.directoryController = new DirectoryController(this.directoryTree,this.codeTabPane,
                this.codeTabPane.getFileNames());
        this.structureViewController=new StructureViewController(this.fileStructureTree, this.codeTabPane);

        //bind the edit, save, saveAs, close menus to the property of a list of opened tabs
        SimpleListProperty<Tab> tabsProperty = new SimpleListProperty<> (this.codeTabPane.getTabs());

        this.editMenu.disableProperty().bind(tabsProperty.emptyProperty());
        this.saveMenuItem.disableProperty().bind(tabsProperty.emptyProperty());
        this.saveAsMenuItem.disableProperty().bind(tabsProperty.emptyProperty());
        this.closeMenuItem.disableProperty().bind(tabsProperty.emptyProperty());

        //disable Toolbar items to start and pass controllers and context menu to console
        this.disableToolbar();
        this.console.setToolbarController(this.toolbarController);
        this.console.setContextMenu(consoleContextMenu);

        this.codeTabPane.passControllerContextMenus(this,codeAreaContextMenu,tabContextMenu);

    }


    /**
     * Handler for the "About" menu item in the "File" menu.
     * Creates an Information alert dialog to display author and information of this program
     */
    @FXML public void handleAbout() { this.helpMenuController.handleAbout(); }

    /**
     * Handler for the "New" menu item in the "File" menu.
     * Adds a new Tab to the TabPane, and also adds null to the HashMap
     * Also sets the current tab for both the file and edit controllers.
     */
    @FXML public void handleNew() {
        fileController.handleNew();
        if(toolbarController.scanIsDone()) {
            this.scanButton.setDisable(false);
            this.scanParseButton.setDisable(false);
            this.scanParseCheckButton.setDisable(false);
            this.assembleButton.setDisable(false);
            this.assembleAndRunButton.setDisable(false);
            this.stopButton.setDisable(false);
//            this.findUsesButton.setDisable(false);
//            this.findUnusedButton.setDisable(false);
//            this.suggestNamesButton.setDisable(false);
            this.prettyPrintButton.setDisable(false);
            this.headerDocButton.setDisable(false);
            this.prevErrorButton.setDisable(false);
            this.nextErrorButton.setDisable(false);
            this.prevMethodButton.setDisable(false);
            this.nextMethodButton.setDisable(false);


        }
        this.updateStructureView();
    }

    /**
     * Handler for the "Open" menu item in the "File" menu.
     * Creates a FileChooser to select a file
     * Use scanner to read the file and write it into a new tab.
     * Also sets the current tab for both the file and edit controllers.
     */
    @FXML public void handleOpen() {
        File file = fileController.handleOpenDialog();
        fileController.handleOpen(file);
        if(toolbarController.scanIsDone() && !this.codeTabPane.getTabs().isEmpty()) {
            /*this.scanButton.setDisable(false); //Leaving these here in case I screwed up, to make reverting easy -Tia
            this.scanParseButton.setDisable(false);
            this.scanParseCheckButton.setDisable(false);
            this.assembleButton.setDisable(false);
            this.assembleAndRunButton.setDisable(false);
            this.stopButton.setDisable(false);
            this.findUsesButton.setDisable(false);
            this.findUnusedButton.setDisable(false);
            this.suggestNamesButton.setDisable(false);*/

            if(file.getName().endsWith(".asm") || file.getName().endsWith(".s")) {

               setMipsButtons();
            }
            else if(file.getName().endsWith(".btm")) {
               setBantamButtons();
            }

        }
        this.updateStructureView();
        this.createDirectoryTree();
    }


    /*
    * Helper method that disables Bantam-only buttons and enables the MIPS only ones on the toolbar
    */
    public void setMipsButtons(){
        //Enable MIPS only options
        this.assembleButton.setDisable(false);
        this.assembleAndRunButton.setDisable(false);
        this.stopButton.setDisable(false);

        //Disabling Bantam only
        this.scanButton.setDisable(true);
        this.scanParseButton.setDisable(true);
        this.scanParseCheckButton.setDisable(true);
//        this.findUsesButton.setDisable(true);
//        this.findUnusedButton.setDisable(true);
//        this.suggestNamesButton.setDisable(true);
        this.compileButton.setDisable(true);
        this.prettyPrintButton.setDisable(true);
        this.headerDocButton.setDisable(true);
        this.prevErrorButton.setDisable(true);
        this.nextErrorButton.setDisable(true);
        this.prevMethodButton.setDisable(true);
        this.nextMethodButton.setDisable(true);

    }


    /*
     * Helper method that enables Bantam-only buttons and disables the MIPS only ones on the toolbar
     */
    public void setBantamButtons(){
        //Enable Bantam options
        this.scanButton.setDisable(false);
        this.scanParseButton.setDisable(false);
        this.scanParseCheckButton.setDisable(false);
//        this.findUsesButton.setDisable(false);
//        this.findUnusedButton.setDisable(false);
//        this.suggestNamesButton.setDisable(false);
        this.compileButton.setDisable(false);
        this.prettyPrintButton.setDisable(false);
        this.headerDocButton.setDisable(false);
        this.prevErrorButton.setDisable(false);
        this.nextErrorButton.setDisable(false);
        this.prevMethodButton.setDisable(false);
        this.nextMethodButton.setDisable(false);

        //Disabling MIPS only
        this.assembleButton.setDisable(true);
        this.assembleAndRunButton.setDisable(true);
        this.stopButton.setDisable(true);

    }

    /**
     * Handler for the "Close" menu item in the "File" menu.
     * Checks to see if the file has been changed since the last save.
     * If changes have been made, redirect to askSave and then close the tab.
     * Otherwise, just close the tab.
     */
    @FXML public void handleClose(Event event) {
        fileController.handleClose(event);
        if (this.codeTabPane.getTabs().isEmpty()&&toolbarController.scanIsDone()){
            disableToolbar();
        }
    }

    /**
     * Handler for the "Save" menu item in the "File" menu.
     * If the current tab has been saved before, writes out the content to its corresponding
     * file in storage.
     * Else if the file has never been saved, opens a pop-up window that allows the user to
     * choose a filename and directory and then store the content of the tab to storage.
     */
    @FXML public void handleSave() { fileController.handleSave(); }

    /**
     * Handler for the "Save as..." menu item in the "File" menu.
     * Opens a pop-up window that allows the user to choose a filename and directory.
     * Calls writeFile to save the file to memory.
     * Changes the name of the current tab to match the newly saved file's name.
     */
    @FXML public void handleSaveAs( ) { fileController.handleSaveAs(); }

    /**
     * Handler for the "Exit" menu item in the "File" menu.
     * Closes all the tabs using handleClose()
     * Returns when the user cancels exiting any tab.
     * @param event an event
     */
    @FXML public void handleExit(Event event) { fileController.handleExit(event); }

    /**
     * Handler for the "Undo" menu item in the "Edit" menu.
     */
    @FXML
    public void handleUndo() { editController.handleUndo(); }

    /**
     * Handler for the "Redo" menu item in the "Edit" menu.
     */
    @FXML
    public void handleRedo() { editController.handleRedo(); }

    /**
     * Handler for the "Cut" menu item in the "Edit" menu.
     */
    @FXML
    public void handleCut() { editController.handleCut(); }

    /**
     * Handler for the "Copy" menu item in the "Edit" menu.
     */
    @FXML
    public void handleCopy() { editController.handleCopy();}

    /**
     * Handler for the "Paste" menu item in the "Edit" menu.
     */
    @FXML
    public void handlePaste() { editController.handlePaste(); }

    /**
     * Handler for the "Comment with Line Comments" menu item in the "Edit" menu.
     */
    @FXML
    public void handleLineComment(){ editController.handleLineComment(); }

    /**
     * Handler for the "Comment with Block Comments" menu item in the "Edit" menu.
     */
    @FXML
    public void handleBlockComment(){ editController.handleBlockComment(); }

    /**
     * Handler for the "SelectAll" menu item in the "Edit" menu.
     */
    @FXML
    public void handleSelectAll() { editController.handleSelectAll(); }

    @FXML
    public void handleTab() { editController.handleTab(); }

    /**
     * Handler for the "Entab" menu item in the "Edit" menu.
     */
    @FXML
    public void handleEntab() {
        editController.handleEntabOrDetab("entab");
    }

    /**
     * Handler for the "Detab" menu item in the "Edit" menu.
     */
    @FXML
    public void handleDetab() {
        editController.handleEntabOrDetab("detab");
    }

    /**
     * Handler for the "Find & Replace" menu item in the "Edit menu.
     */
    @FXML
    public void handleFindReplace(){ editController.handleFindReplace();  }

    /**
     * Handler for the "NightMode" Toggle menu item in the "Preferences" Menu.
     */
    @FXML
    public void handleNightMode(){ preferenceController.handleNightMode(); }

    /**
     * Handler for the "Java Tutorial" menu item in the "Help" Menu.
     * When the item is clicked, a Java tutorial will be opened in a browser.
     */
    @FXML
    public void handleJavaTutorial(){
        this.helpMenuController.handleJavaTutorial();
    }

    /**
     * Calls the method that handles the Keyword color menu item from the PreferenceController.
     */
    @FXML public void handleKeywordColorAction() { this.preferenceController.handleColorAction("Keyword"); }

    /**
     * Calls the method that handles the Parentheses/Brackets color menu item from the PreferenceController.
     */
    @FXML public void handleParenColorAction() { this.preferenceController.handleColorAction("Paren"); }

    /**
     * Calls the method that handles the String color menu item from the PreferenceController.
     */
    @FXML public void handleStrColorAction() { this.preferenceController.handleColorAction("Str"); }

    /**
     * Calls the method that handles the Int color menu item from the PreferenceController.
     */
    @FXML public void handleIntColorAction() { this.preferenceController.handleColorAction("Int"); }

    /**
     * Jump to the line where the selected class/method/field is declared.
     */
    @FXML
    private void handleTreeItemClicked()
    {
        //get the selected tree item and get the codeArea it corresponds to
        TreeItem selectedTreeItem = (TreeItem) this.fileStructureTree.getSelectionModel().getSelectedItem();
        CodeArea currentCodeArea = this.codeTabPane.getCodeArea();

        //jump to the line in the codeArea where the selected class/method/field is declared
        if (selectedTreeItem != null)
        {
            int lineNum = this.structureViewController.getTreeItemLineNum(selectedTreeItem);
            if (currentCodeArea != null) currentCodeArea.showParagraphAtTop(lineNum - 1);
        }
    }

    /**
     * Event handler to open a file selected from the directory
     *
     * @param event a MouseEvent object
     */
    @FXML
    private void handleDirectoryItemClicked(MouseEvent event){
        // only open file if double clicked
        if (event.getClickCount() == 2 && !event.isConsumed()) {
            event.consume();
            TreeItem selectedItem = directoryTree.getSelectionModel().getSelectedItem();

            //fixes bug where pressing arrow throws NullPointerException
            if(selectedItem == null){
                return;
            }
            //check if the selected file is a java file and open it if so
            String fileName = (String) selectedItem.getValue();
            if (fileName.endsWith(".java")) {
                this.fileController.handleOpen(this.directoryController.getTreeItemFileMap().get(selectedItem));
            }
        }
    }

    /**
     * handles checkMenuItem for File Structure Tab
     * Opens/closes the tab
     */
    @FXML
    public void handleFileStructureTab(){
        this.preferenceController.handleFileStructureTab();
    }

    /**
     * handles checkMenuItem for Directory Tree Tab
     * Opens/closes the tab
     */
    @FXML
    public void handleDirectoryTreeTab(){
        this.preferenceController.handleDirectoryTreeTab();
    }

    /**
     * Calls handleMatchBracketOrParen() of the editController
     */
    @FXML
    public void handleMatchBracketOrParen() {
        editController.handleMatchBracketOrParen();
    }


    /**
     * Parses and generates the structure view for the currently open code area
     */
    public void updateStructureView(){
        structureViewController.updateStructureView();
    }


    /**
     * close all tabs when Close All menu item is clicked
     * @param event an action event
     */
    public void handleCloseAll(Event event){
        fileController.handleCloseAll(event);
    }

    /**
     * clears the console
     */
    public void handleClearConsole(){
        this.console.clear();
    }

    /**
     * Pops up a dialog asking if the user wants to save the changes
     * and return a string indicating which button the user clicked
     * @param title the title of the dialog
     * @param header the header of the dialog
     * @param context the context of the dialog
     * @return a string indicating which button the user clicked
     */
    public String askSaveDialog(String title, String header, String context){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(context);
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == yesButton){
            return "yesButton";
        }
        else if(result.get() == noButton){
            return "noButton";
        }
        else{
            return "cancelButton";
        }
    }

    /**
     * Disables the Compile, Compile and Run, and Stop buttons in the toolbar
     */
    private void disableToolbar(){
        this.scanButton.setDisable(true);
        this.scanParseButton.setDisable(true);
        this.scanParseCheckButton.setDisable(true);
        this.assembleButton.setDisable(true);
        this.assembleAndRunButton.setDisable(true);
        this.stopButton.setDisable(true);
        this.prettyPrintButton.setDisable(true);
//        this.findUsesButton.setDisable(true);
//        this.findUnusedButton.setDisable(true);
//        this.suggestNamesButton.setDisable(true);
        this.headerDocButton.setDisable(true);
        this.prevErrorButton.setDisable(true);
        this.nextErrorButton.setDisable(true);
        this.prevMethodButton.setDisable(true);
        this.nextMethodButton.setDisable(true);
        this.compileButton.setDisable(true);

    }

    /**
     * Call the createDirectoryTree function in the directory controller
     */
    public void createDirectoryTree(){
        this.directoryController.createDirectoryTree();
    }

    /**
     * Handles scanning the current CodeArea
     */
    @FXML
    public void handleScan(){
        this.handleScanOrScanParse("scan", true, null);
    }

    /**
     * Handles parsing the current CodeArea
     */
    @FXML
    private void handleScanAndParse(){
        this.handleScanOrScanParse("scanParse", true, null);
    }

    /**
     * Handles scanning and parsing on the current tab when a button is clicked
     * Checks to make sure the current tab is saved first and if not, asks if you want to save
     * If canceled, breaks. If no, works off saved file. If yes, saves first and then moves on to scanning/parsing
     * @param method is the string that indicates whether it should be just scanning, scanning and parsing,
     * or scanning and parsing followed by a visitor performing some action
     * @param additionalFunc is a String indicating an additional function to be run after semantic analysis.
     * Options are "uses", "unused", and "suggestions"
     * Pass in null if no additional functions are needed
     */
    private void handleScanOrScanParse(String method, boolean errorMode, String additionalFunc){
        Tab curTab = this.codeTabPane.getSelectionModel().getSelectedItem();
        if(this.codeTabPane.getSaveStatus(curTab)) {
            toolbarController.handleScanOrScanParse(method, errorMode, additionalFunc);
        } else {
            String saveResult = askIfSave(curTab);
            if("unsaved".equals(saveResult) || "saved".equals(saveResult)){
                toolbarController.handleScanOrScanParse(method, errorMode, additionalFunc);
            }

        }

    }


    /**
     * handles the main method finder button
     */
    @FXML
    private void handleSemanticCheck(){
        this.handleScanOrScanParse("semanticCheck", true, null);

    }

    /**
     * Calls the method that handles the Find Uses button action from
     * the toolBarController
     *
     * @param event Event object
     */
    @FXML
    public void handleFindUsesButtonAction(Event event) {

        handleScanOrScanParse("semanticCheck", false, "uses");
        //this.toolbarController.handleFindUses();
    }



    /**
     * Calls the method that handles the Find Unused button action from
     * the toolBarController
     *
     * @param event Event object
     */
    @FXML
    public void handleFindUnusedButtonAction(Event event) {

        handleScanOrScanParse("semanticCheck", false, "unused");
        // this.toolbarController.handleFindUnused();
    }



    /**
     * Calls the method that handles the Find Unused button action from
     * the toolBarController
     *
     * @param event Event object
     */
    @FXML
    public void handleCompileBantam(Event event) {

        handleScanOrScanParse("semanticCheck", false, "compile");
    }




    /**
     * Calls the method that handles the Suggest Names button action from
     * the toolBarController
     *
     * @param event Event object
     */
    @FXML
    public void handleSuggestions(Event event) {
        //I don't want it to ask for it to be saved (because it's probably semantically incorrect at that point)
        //So not running handleScanOrScanParse here
        toolbarController.handleScanOrScanParse("semanticCheck", false, "suggestions");
    }

    @FXML
    public void handleDocHeaders(Event event) {
        toolbarController.handleDocHeaders();
    }

    @FXML
    public void handleNextError(Event event) {
        toolbarController.handleNextError();
    }

    @FXML
    public void handlePrevError(Event event) {
        toolbarController.handlePrevError();
    }

    @FXML
    public void handleNextMethod(Event event) {
        toolbarController.handleNextMethod();
    }

    @FXML
    public void handlePrevMethod(Event event) {
        toolbarController.handlePrevMethod();
    }


    /**
     * Calls the method that handles the Assemble button action from
     * the toolBarController
     *
     * @param event Event object
     */
    @FXML
    public void handleAssemble(Event event) {
        Tab curTab = this.codeTabPane.getSelectionModel().getSelectedItem();
        if(this.codeTabPane.getSaveStatus(curTab)) {
            File currentFile = codeTabPane.getCurrentFile();
            //System.out.println(currentFile);
            toolbarController.handleAssemble(currentFile);
        }
        else {
            String saveResult = askIfSave(curTab);
            if ("unsaved".equals(saveResult) || "saved".equals(saveResult)) {
                File currentFile = codeTabPane.getCurrentFile();
                toolbarController.handleAssemble(currentFile);
            }
        }


    }





    /**
     * Calls the method that handles the Assemble and Run button action from
     * the toolBarController
     *
     * @param event Event object
     */
    @FXML
    public void handleAssembleAndRun(Event event) {
        Tab curTab = this.codeTabPane.getSelectionModel().getSelectedItem();
        if(this.codeTabPane.getSaveStatus(curTab)) {
            File currentFile = codeTabPane.getCurrentFile();
            toolbarController.handleAssembleAndRun(currentFile);
        } else {
            String saveResult = askIfSave(curTab);
            if ("unsaved".equals(saveResult) || "saved".equals(saveResult)) {
                File currentFile = codeTabPane.getCurrentFile();
                toolbarController.handleAssembleAndRun(currentFile);
            }
        }
    }



    /**
     * Calls the method that handles the Assemble and Run button action from
     * the toolBarController
     *
     * @param event Event object
     */
    @FXML
    public void handleStop(Event event) {
        toolbarController.handleStop();
    }

    /**
     * Calls the method that handles Pretty Print from the ToolbarController
     *
     * @param event Event object
     */
    @FXML
    public void handlePrettyPrint(Event event) {
        Tab curTab = this.codeTabPane.getSelectionModel().getSelectedItem();
        if(this.codeTabPane.getSaveStatus(curTab)) {
            File currentFile = codeTabPane.getCurrentFile();
            toolbarController.handlePrettyPrint(currentFile);
        }
        else {
            String saveResult = askIfSave(curTab);
            if ("unsaved".equals(saveResult) || "saved".equals(saveResult)) {
                File currentFile = codeTabPane.getCurrentFile();
                toolbarController.handlePrettyPrint(currentFile);
            }
        }
    }


    /*
    * Helper method that calls up the save dialogue for a tab, saves if needed, and returns what choice the user made
    * @param curTab is the Tab which dialogue should be called for. It should've already been determined to be unsaved
    */
    private String askIfSave(Tab curTab) {

        String saveResult = this.askSaveDialog(null,
                "Do you want to save your changes?", null);
        switch (saveResult) {
            case ("yesButton"):
                boolean isNotCancelled = fileController.handleSave();
                if (isNotCancelled) {
                    return "saved";
                }
                break;
            case ("noButton"):
                if (this.codeTabPane.getFileName() == null) {
                    this.console.writeToConsole("File Not Found: " + curTab.getText() + "\n", "Error");
                    this.console.writeToConsole("File must be saved to scan \n", "Error");
                }
                return "unsaved";
            case ("cancelButton"):
                return "canceled";
        }
        return "dummy"; //This should never be reached, just needed an else case somewhere to make Java happy
    }
}
