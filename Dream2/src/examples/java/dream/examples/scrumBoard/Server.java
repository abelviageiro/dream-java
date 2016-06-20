package dream.examples.scrumBoard;

import java.util.Set;

import dream.client.DreamClient;
import dream.client.RemoteVar;
import dream.client.Signal;
import dream.examples.util.Client;
import dream.examples.util.Pair;

/**
 * Holds a list of tasks and a list of developers each indicated by a simple
 * integer. Searches for new clients (TaskCreater) and registers to their
 * "task creation channels"
 * 
 * @author Min Yang
 * @author Tobias Becker
 */
public class Server extends Client {
	public static final String NAME = "ServerNode";
	public static final String VAR_developers = "developers";
	public static final String VAR_tasks = "tasks";

	private Pair<String, String> creator1;
	private Pair<String, String> creator2;

	public static void main(String... args) {
		new Server();
	}

	public Server() {
		super(NAME);
		detectClients();
	}

	private void detectClients() {
		Set<String> vars = DreamClient.instance.listVariables();
		vars.stream().map(x -> new Pair<String, String>(x.split("@")[1], x.split("@")[0]))
				.filter(x -> (creator1 == null || !creator1.equals(toVar(x)))
						&& (creator2 == null || !creator2.equals(toVar(x)))
						&& x.getSecond().equalsIgnoreCase(Creator.VAR_newAssignment))
				.forEach(x -> foundCreator(x));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// stop looking after two creators have been found
		if (creator1 == null || creator2 == null)
			detectClients();
	}

	private void foundCreator(Pair<String, String> creator) {
		logger.info("found creator instance " + creator);
		if (creator1 == null)
			creator1 = creator;
		else if (creator2 == null)
			creator2 = creator;

		if (creator1 != null && creator2 != null)
			initDependencies();
	}

	private void initDependencies() {

		RemoteVar<Assignment> rv1 = new RemoteVar<>(creator1.getFirst(), creator1.getSecond());
		RemoteVar<Assignment> rv2 = new RemoteVar<>(creator2.getFirst(), creator2.getSecond());

		Signal<String> developers = new Signal<String>(VAR_developers, () -> {
			// TODO
			return "";
		}, rv1, rv2);

		Signal<String> tasks = new Signal<String>(VAR_tasks, () -> {
			// TODO
			return "";
		}, rv1, rv2);

	}

}