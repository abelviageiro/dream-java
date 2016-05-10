/**
 * 
 */
package dream.examples.tasks;

import dream.client.RemoteVar;
import dream.client.Signal;
import dream.client.Var;
import dream.common.Consts;

/**
 * @author Ram
 *
 */
public class WorkerProcess {
	static int i = 0;
	/**
	 * @param args
	 */
	private String processName;

	/**
	 * @return the processName
	 */
	public String getProcessName() {
		return processName;
	}

	/**
	 * @param processName
	 *            the processName to set
	 */
	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public WorkerProcess(String processName, String host) {
		this.setProcessName(processName);
	}

	public static void main(String[] args) {

		Consts.hostName = "Host2";

		RemoteVar<String> rv = new RemoteVar<String>("Host1", "TASK");
		Var<String> myVar = new Var<String>("TASK_ASSIGNED", "");

		Signal<String> s = new Signal<String>("s", () -> {
			System.out.println("received New Object" + rv.get());
			return rv.get();
		} , rv);

		// Register a handler which will be executed upon receiving the signal
		s.change().addHandler((oldVal, val) -> {
			System.out.println("Deligating Task : " + val);
			myVar.set(val + "@" + i++);
		});
	}

}
