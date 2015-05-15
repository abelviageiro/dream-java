package javareact.generator;

import java.util.List;
import java.util.function.Function;
import javareact.common.types.RemoteVar;
import javareact.common.types.Types;

/**
 * A GraphGeneratorListener receives events from a GraphGenerator. In particular, it is notified about the observable
 * and reactive objects defined in a specific node.
 */
public interface GraphGeneratorListener {

  /**
   * Notifies the presence of an observable object with the given name and type.
   * 
   * @param observableName the observable name
   * @param type the observable type
   */
  public void notifyObservable(String observableName, Types type);

  /**
   * Notifies the presence of a reactive object with the given expression, name, and type.
   * 
   * @param reactiveExpression the expression that defines the reactive object
   * @param observableName the name of the associated observable object
   * @param type the type of the associated observable object
   */
  public <T> void notifyReactive(String observableName, Function<List<RemoteVar<T>>, T> expression, List<String> proxyNames);

}
