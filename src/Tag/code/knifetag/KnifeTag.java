package knifetag;

/**
 * Class to manage code.knifetag(TM) course.
 *
 * @version 1.1
 */
public class KnifeTag {
  /**
   * Maximum number of simultaneous competitors
   */
  public static final int MAXCOMPETITORS = 4;

  /**
   * Number of current competitors
   */
  protected int numCompetitors = 0;

  protected String funcPreCle = "PreClean: ";
  protected String funcPostCle = "PostClean: ";
  protected String funcPreCom = "PreCompete: ";
  protected String funcPostCom = "PostCompete: ";


  /**
   * Executes cleaning of the code.knifetag course
   *
   * @param ms   number of milliseconds cleaning takes
   * @param name name of cleaner
   */
  public final void clean(int ms, String name) {
    preClean();
    System.out.println("Start of cleaning for " + name);
    sleep(ms);
    System.out.println("Stop of cleaning for " + name);
    postClean();
  }

  /**
   * Execute before cleaning
   */
  protected void preClean() {
  }

  /**
   * Execute after cleaning
   */
  protected void postClean() {
  }

  /**
   * Executes competitor in the code.knifetag course
   *
   * @param ms   number of milliseconds for competing
   * @param name name of competitor
   */
  public final void compete(int ms, String name) {
    preCompete();
    System.out.println("Start of competition for " + name);
    sleep(ms);
    System.out.println("Stop of competition for " + name);
    postCompete();
  }

  /**
   * Execute before competing
   */
  protected void preCompete() {
  }

  /**
   * Execute after competing
   */
  protected void postCompete() {
  }

  /**
   * Suspend thread
   *
   * @param ms milliseconds to suspend thread
   */
  public static void sleep(int ms) {
    try {
      Thread.sleep(ms);
    } catch(InterruptedException e) {
      System.err.println("Unexpected interruption: " + e.getMessage());
    }
  }
}