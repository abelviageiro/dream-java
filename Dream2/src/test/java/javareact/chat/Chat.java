package javareact.chat;

import java.awt.EventQueue;

import javareact.common.Consts;
import javareact.common.types.ChangeEventHandler;
import javareact.common.types.RemoteVar;
import javareact.common.types.Signal;
import javareact.common.types.Var;

public class Chat {

	private RemoteVar<String> remoteMessages;
	private Var<String> myMessages;
	private String userName;
	private ChatGUI gui;

	public Chat(String username) throws Exception {
		this.userName = username;

		Consts.hostName = userName;
		// Establish new session with server
		RemoteVar<String> id = new RemoteVar<String>(ChatServer.NAME, ChatServer.NEW_ID);
		RemoteVar<String> var = new RemoteVar<String>(ChatServer.NAME, ChatServer.NEW_VAR);
		Signal<String> setup = new Signal<String>("setup", () -> {
			if (id.get() == null || var.get() == null)
				return "";
			else
				return id.get() + "@" + var.get();
		}, id, var);
		ChangeEventHandler<String> ceh = (o, n) -> {
			String[] t = n.split("@", 2);
			setup(t[0], t[1]);
		};
		setup.change().addOneTimeHandler((o, n) -> {
			if (n.equals("")) {
				setup.change().addOneTimeHandler(ceh);
			} else
				ceh.handle(o, n);
		});
		System.out.println("Setup: Waiting for Setup information from Server ...");
	}

	private void setup(String setup_id, String setup_var) {
		System.out.println("Setup: Setup information received!");
		System.out.println("Setup: ID: " + setup_id);
		System.out.println("Setup: VAR: " + setup_var);
		String serverVar = ChatServer.getRandom();
		// Consts.hostName = setup_id;
		Var<String> init = new Var<String>(setup_var, "message@" + userName + "@" + serverVar);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// should have correct value now
		System.out.println("init: " + init.get());
		Consts.hostName = userName;
		myMessages = new Var<String>("message", "");

		remoteMessages = new RemoteVar<String>(serverVar + "@" + ChatServer.NAME);

		Signal<String> display = new Signal<String>("display", () -> {
			if (remoteMessages.get() != null)
				return remoteMessages.get();
			else
				return "";
		}, remoteMessages);

		gui = new ChatGUI(userName);
		gui.setListener(this);

		display.change().addHandler((oldValue, newValue) -> {
			gui.displayMessage(newValue);
		});
	}

	protected void sendMessage() {
		myMessages.set(userName + ":" + gui.getTypedText());
		gui.displayMessage("You: " + gui.getTypedText());
		gui.resetTypedText();
	}

	public static void main(String[] args) {
		try {
			if (args.length < 1)
				System.out.println("username missing");
			// Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.ALL);
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {

						new Chat(args[0]);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}