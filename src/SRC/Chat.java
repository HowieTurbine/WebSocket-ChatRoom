package SRC;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value="/chat")
public class Chat {
	public static final String GUEST_PREFIX="Vistor";
	private static final AtomicInteger connectionIds=new AtomicInteger(0);
	//Save the client set
	private static final Set<Chat> clientSet=new CopyOnWriteArraySet<>();
	private final String nickName;
	private Session session;
	public Chat()
	{
		nickName=GUEST_PREFIX+connectionIds.getAndIncrement();
	}
	@OnOpen
	public void start(Session session)
	{
		System.out.println("Start!");
		this.session=session;
		clientSet.add(this);
		String message="["+nickName+"]"+" enter the chat room!";
		//Send the message
		broadcast(message);
	}
	@OnClose
	public void end()
	{
		clientSet.add(this);
		String message="["+nickName+"}"+" left the chat room!";
		//Send the message
		broadcast(message);
	}
	@OnMessage
	public void inComing(String message)
	{
		System.out.println("Get Message!");
		String filteredMessage=nickName+" : "+message;
		
		broadcast(filteredMessage);
	}
	@OnError
	public void onError(Throwable t) throws Throwable
	{
		System.out.println("WebSocket Error!");
	}
	
	private static void broadcast(String msg)
	{
		for(Chat client:clientSet)
		{
			try
			{
				synchronized(client)
				{
					client.session.getBasicRemote().sendText(msg);
					System.out.println("Sended!");
				}
			}
			catch(IOException e)
			{
				System.out.println("Meet Error when "+client+" sended the message");
				clientSet.remove(client);
				try
				{
					client.session.close();
				}
				catch(IOException e1) {}
				String message=client+" has been removed";
				broadcast(message);
			}
		}
	}
}
