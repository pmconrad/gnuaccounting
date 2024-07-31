package GUILayer;

/**
 * is used by recordswindow and duewindow to allow a refresh of the table to
 * continuations, which has a popup with actions that may need this refresh
 * */
public interface tableWindow {
	abstract public void refreshTable();

}
