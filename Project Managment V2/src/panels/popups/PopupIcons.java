package panels.popups;

import java.awt.Color;

import javax.swing.Icon;

import managment.icons.IconLoader;

public class PopupIcons {
	public static final Icon DELETE_ICON = IconLoader.loadIcon("Exit.png", 12, new Color(225, 125, 125));
	public static final Icon CREATE_ICON = IconLoader.loadIcon("AddBlank.png", 16, new Color(125, 225, 125));
	public static final Icon RESUME_ICON = IconLoader.loadIcon("PlayBlank.png", 16, new Color(125, 225, 225));
	public static final Icon PAUSE_ICON  = IconLoader.loadIcon("PauseBlank.png", 16, new Color(225, 225, 125));
	//IconLoader.loadIcon("AddNew.png", 16);
}
