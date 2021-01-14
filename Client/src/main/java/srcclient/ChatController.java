package srcclient;

public class ChatController 
{
    private ChatView view;
    private ChatModel model;

    public ChatController(ChatView view, ChatModel model) 
    {
        this.view = view;
        this.model = model;
    }
}