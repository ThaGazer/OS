package Tag.table;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class Table {
  private Map<Item, Integer> table = new HashMap<>();

  public final void addStartItems(String name, Item addItem1, Item addItem2) {
    preStartAddItems(addItem1, addItem2);
    addItem(name, addItem1);
    addItem(name, addItem2);
    postStartAddItems(addItem1, addItem2);
  }

  private void addItem(String name, Item item) {
    table.merge(item, 1, Integer::sum);
    System.out.println(name + " adds " + item + " to the table.");
  }

  protected void preStartAddItems(Item addItem1, Item addItem2) {
  }

  protected void postStartAddItems(Item addItem1, Item addItem2) {
  }

  public final void cook(Item item, String name) throws Exception {
    preCook(item);
    addItem(name, item);
    if (table.get(Item.CARTON) == 0) {
      throw new Exception(
          "Cook attempts to prepare an omelet without eggs and burns pan.");
    }
    if (table.get(Item.SPATULA) == 0) {
      throw new Exception(
          "Cook attempts to prepare an omelet without a spatula and burns hand.");
    }
    if (table.get(Item.PAN) == 0) {
      throw new Exception(
          "Cook attempts to prepare an omlet without a pan and burns down house.");
    }
    for (Item i : Item.values()) {
      if (table.get(i) > 1) {
        throw new Exception(
            "Cook fails.  Death blamed on too many " + i + "s available");
      }
    }
    System.out.println(name + " cooks.");
    table.clear();
    postCook(item);
  }

  protected void preCook(Item item) {
  }

  protected void postCook(Item item) {
  }
}
