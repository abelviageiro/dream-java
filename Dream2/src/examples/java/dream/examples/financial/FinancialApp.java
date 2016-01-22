package dream.examples.financial;

import dream.client.RemoteVar;
import dream.client.Signal;
import dream.client.ValueChangeListener;
import dream.common.Consts;

public class FinancialApp implements ValueChangeListener<Integer> {
  private Signal<Integer> f1Signal;
  private Signal<Integer> f2Signal;
  private Signal<Integer> f3Signal;

  private RemoteVar<Integer> f1;
  private RemoteVar<Integer> f2;
  private RemoteVar<Integer> f3;

  public static void main(String[] args) {
    new FinancialApp().start();
  }

  public void start() {
    Consts.hostName = "Local";

    try {
      Thread.sleep(2000);
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }

    f1 = new RemoteVar<>("f1@Model1");
    f2 = new RemoteVar<>("f2@Model2");
    f3 = new RemoteVar<>("f3@Model3");

    final RemoteVar<Integer> model1 = new RemoteVar<>("Model1", "model1");

    f1Signal = new Signal<>("f1Signal", () -> f1.get(), f1);
    f2Signal = new Signal<>("f2Signal", () -> f2.get(), f2);
    f3Signal = new Signal<>("f3Signal", () -> f3.get(), f3);

    f1Signal.addValueChangeListener(this);
    f2Signal.addValueChangeListener(this);
    f3Signal.addValueChangeListener(this);

    try {
      Thread.sleep(2000);
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println(f1.get());
    System.out.println(f2.get());
    System.out.println(f3.get());

    System.out.println(model1.get());
  }

  @Override
  public void notifyValueChanged(Integer newValue) {
    System.out.println("Value changed");

    if (f1.get() != null && f2.get() != null && f3.get() != null) {
      if ((f1.get() + f2.get() + f3.get()) / 3.0 > 150) {
        System.out.println("Financial Alert!");
      }
    }
  }
}