package srcclient;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ChatController implements PropertyChangeListener 
{
    private ChatView view;
    private ChatModel model;

    public ChatController(ChatView view, ChatModel model) 
    {
        this.view = view;
        this.model = model;

        this.model.addListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent arg0) 
    {
        if (view.getButtonInvia() == arg0.getSource())
        {

        }
    }
}