package javareact.examples.remote;

import java.util.List;

import javareact.common.Consts;
import javareact.common.types.RemoteVar;
import javareact.common.types.Signal;

public class RemoteSignalExample {

  public static void main(String args[]) {
    Consts.hostName = "Signal";

    final RemoteVar<Integer> remoteInt = new RemoteVar<Integer>("Remote", "remoteInt");
    final RemoteVar<String> remoteString1 = new RemoteVar<String>("Remote", "remoteString1");
    final RemoteVar<String> remoteString2 = new RemoteVar<String>("Remote", "remoteString2");
    final RemoteVar<List<Integer>> remoteList = new RemoteVar<List<Integer>>("Remote", "remoteList");

    final Signal<Integer> signal1 = new Signal<Integer>("signal1", () -> remoteInt.get() + remoteString1.get().length(), remoteInt, remoteString1);
    final Signal<Integer> signal2 = new Signal<Integer>("signal2", () -> remoteInt.get(), remoteInt);
    final Signal<String> signal3 = new Signal<String>("signal3", () -> remoteString1.get() + remoteString2.get(), remoteString1, remoteString2);
    final Signal<Integer> signal4 = new Signal<Integer>("signal4", () -> remoteString1.get().length() + remoteList.get().size(), remoteString1, remoteList);

    signal1.addReactiveChangeListener(val -> System.out.println("Signal1: " + val));
    signal2.addReactiveChangeListener(val -> System.out.println("Signal2: " + val));
    signal3.addReactiveChangeListener(val -> System.out.println("Signal3: " + val));
    signal4.addReactiveChangeListener(val -> System.out.println("Signal4: " + val));
  }
}
