package dream.examples.form;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import dream.client.RemoteVar;
import dream.client.Signal;
import dream.examples.util.Client;

public abstract class FormClient extends Client {

	private RemoteVar<Double> salary;
	private RemoteVar<Boolean> settings;
	private Signal<Double> remoteSalary;
	private Signal<Boolean> remoteSettings;

	private FormGUI gui;
	private String labelText;

	public FormClient(String name, String labelText) {
		super(name);
		this.labelText = labelText;
	}

	@Override
	protected List<String> waitForVars() {
		return Arrays.asList("salary@FormServer", "settingsOkay@FormServer");
	}

	protected void start() {
		gui = new FormGUI(getHostName(), labelText);
		gui.setListener(this);

		salary = new RemoteVar<>("FormServer", "salary");
		settings = new RemoteVar<>("FormServer", "settingsOkay");

		remoteSalary = new Signal<>("remoteSalary", () -> {
			if (salary.get() != null)
				return salary.get();
			else
				return 0.0;
		}, salary);

		remoteSettings = new Signal<>("remoteSettings", () -> {
			if (settings.get() != null)
				return settings.get();
			else
				return false;
		}, settings);

		gui.setText("Salary: ");
		gui.setColor(Color.red);
		remoteSalary.change().addHandler((o, n) -> gui.setText("Salary: " + n.toString()));
		remoteSettings.change().addHandler((o, n) -> gui.setColor((n ? Color.green : Color.red)));
	}

	public abstract void typedText(String typedText);
}
