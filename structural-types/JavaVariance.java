import java.util.ArrayList;
import java.util.List;

public class JavaVariance {
  private final Engineer engineer = new Engineer("Johnny McDoe");
  public List<Employee>           invariant()     { return new ArrayList<Employee>(){{ add(engineer); }}; }
  public List<? extends Employee> covariant()     { return new ArrayList<Engineer>(){{ add(engineer); }}; }
  public List<? super Engineer>   contravariant() { return new ArrayList<Employee>(){{ add(engineer); }}; }
}

