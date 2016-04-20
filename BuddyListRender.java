 import javax.swing.DefaultListCellRenderer;
 import javax.swing.*;
 import java.awt.Component;
 import java.awt.*;
 public class BuddyListRender extends DefaultListCellRenderer
{
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,boolean isSelected, boolean cellHasFocus) 
    {
      setOpaque(true);
      Color background = Color.WHITE;
      Color foreground = Color.BLACK;;
      if(value instanceof BuddyLabel)
      {
        this.setText(((BuddyLabel)value).getText());
        this.setIcon(((BuddyLabel)value).getIcon());
        if (isSelected) 
        {
           background = Color.LIGHT_GRAY;
           foreground = Color.WHITE;
        } 
        else 
        {
         background = Color.WHITE;
         foreground = Color.BLACK;
        }
        this.setBackground(background);
        this.setForeground(foreground);
      }
      return this;
    }
}