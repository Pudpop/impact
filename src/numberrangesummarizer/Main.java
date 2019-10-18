package numberrangesummarizer;

import java.util.Random;
import java.util.Arrays;
import java.util.Collection;

/**
* The class Main represents a simple driver class containing unit tests. It also
* contains two timekeeping methods and a method to generate a random list of integers.
*
*
* @author David Kube
*/
public class Main{
  /**
  * The minimum of the range of integers that intList() should generqate
  */
  private static int min = 1;
  /**
  * The range of the integers that intList() should generate. The max is equal to
  * min + range
  */
  private static int range = 100;
  /**
  * A long used for timekeeping when testing the times of the algorithms.
  */
  private static long startTime;

  /**
  * Generates a comme delimited list of (size) integers in the format necessary for testing.
  * Uses the variables min and range to determine which integers should be generated.
  *
  * @param size
  *       The number of integers in the list.
  * @return String
  *       An unsorted comma delimited list of integers which can be summarized.
  */
  private static String intList(int size){
    String ret = "";
    for (int i = 0; i < size; i ++){
      Random rand = new Random();
      rand.setSeed(System.nanoTime());
      ret += (rand.nextInt(range)+min) + ",";
    }
    return ret.substring(0,ret.length()-1);
  }

  /**
  * Stores the current time in nanoseconds in the variable startTime
  */
  private static void tick(){
		startTime = System.nanoTime();
	}

  /**
  * Records the time by finding the difference between starttime and now, and
  * returns the length in milliseconds
  *
  * @return float
  *       the time taken for execution
  */
	private static float tock(){

		return (System.nanoTime() - startTime) / 1000.0f;
	}

  /**
  * A sequential version of the SummarizeIntegerList task which uses the same algorithm
  * as the task but doesn't split the array. This method was used to test that
  * the parallelized version gave equivalent answers to the serial version
  *
  * @param input
  *       A comme delimited list of integers. Need not be sorted.
  * @return String
  *       A summarized version of these integers.
  *
  */
  private static String sequentialSummarize(String input){
    int [] ints = Arrays.stream(input.split(",")).mapToInt(Integer::parseInt).toArray();
    Arrays.sort(ints);
    String sumList = ints[0] + "";

    int rangeMin = ints[0];
    int rangeCheck = ints[0];

    for (int i = 1; i < ints.length; i++){
      if (rangeCheck + 1 == ints[i]){
        if (i == ints.length-1)
          sumList+= "-" + ints[i];
        else
          rangeCheck++;
      }
      else if (rangeCheck == ints[i]){
        if (i == ints.length-1 && rangeCheck != rangeMin)
          sumList+= "-" + ints[i];
        else{
          continue;
        }
      }
      else{
        if (rangeMin == rangeCheck)
          sumList += ", " + ints[i];
        else
          sumList += "-" + rangeCheck + ", " + ints[i];

        rangeMin = ints[i];
        rangeCheck= ints[i];
      }
    }
    return sumList;
  }

  /**
  * Tests the example given in the spec
  *
  * @return boolean
  *       True if the algorithm output is the same as the correct answer
  */
  public static boolean testGiven(){
    String s = "1,3,6,7,8,12,13,14,15,21,22,23,24,31";
    String ans = "1, 3, 6-8, 12-15, 21-24, 31";
    Summarizer sum = new Summarizer(s);
    return ans.matches(sum.summarizeCollection(sum.collect(s)));
  }

  /**
  * Tests if an input of a sequence {1,2,3,...,999,1000} returns a range 1-1000
  *
  * @return boolean
  *       True if the algorithm output is the same as the correct answer
  */
  public static boolean testSequence(){
    String s = "";
    for (int i = 1; i <= 1000; i ++){
      s += i + ",";
    }
    s = s.substring(0,s.length()-1);
    Summarizer sum = new Summarizer(s);
    String ans = "1-1000";
    return ans.matches(sum.summarizeCollection(sum.collect(s)));
  }

  /**
  * Tests if disjoint sequences {1,2,3,...,999,1000,2000,2001,...,3999,4000} returns
  * disjoint ranges {1-1000,2000-4000}
  *
  * @return boolean
  *       True if the algorithm output is the same as the correct answer
  */
  public static boolean testMultSequences(){
    String s = "";
    for (int i = 1; i <= 1000; i ++){
      s += i + ",";
    }
    for (int i = 2000; i <= 4000; i ++){
      s += i + ",";
    }
    for (int i = 5000; i <= 6000; i ++){
      s += i + ",";
    }
    for (int i = 6100; i <= 6200; i ++){
      s += i + ",";
    }
    for (int i = 6202; i <= 6204; i ++){
      s += i + ",";
    }
    s = s.substring(0,s.length()-1);
    Summarizer sum = new Summarizer(s);
    String ans = "1-1000, 2000-4000, 5000-6000, 6100-6200, 6202-6204";
    return ans.matches(sum.summarizeCollection(sum.collect(s)));
  }

  /**
  * Test if list of a constant {1,1,1,1,1,...,1,1} returns a singleton {1}
  *
  * @param constant
  *       the constant in the input, ie {c,c,c,...,c,c,c} will be the input
  * @return boolean
  *       True if the algorithm output is the same as the correct answer
  */
  public static boolean testConstant(int constant){
    String s = "";
    for (int i = 1; i <= 1000; i ++){
      s += constant + ",";
    }
    s = s.substring(0,s.length()-1);
    Summarizer sum = new Summarizer(s);
    String ans = "" + constant;
    return ans.matches(sum.summarizeCollection(sum.collect(s)));
  }

  /**
  * Tests if a combination of disjoint sequences and constants returns the correct
  * singletons in union with the correct ranges
  *
  * Eg {1,10,11,..,19,20,25,30,40,41,...,43,45,50,51,...,54,55,60} should return
  * {1,10-20,25,30,40-45,50-55,60}
  *
  * @return boolean
  *       True if the algorithm output is the same as the correct answer
  */
  public static boolean testConstantAndSequences(){
    String s = "1,10,11,12,13,14,15,16,17,18,19,20,25,30,40,41,42,43,44,45,50,51,52,53,54,55,60";
    Summarizer sum = new Summarizer(s);
    String ans = "1, 10-20, 25, 30, 40-45, 50-55, 60";
    return ans.matches(sum.summarizeCollection(sum.collect(s)));
  }

  /**
  * Tests if the parallel version returns the same output as the sequential version.
  * This confirms that the joinString method in SummarizeIntegerList is correct, and that
  * splitting the array does not lose any data.
  *
  * @return boolean
  *       True if the algorithm output is the same as the correct answer
  */
  public static boolean testVsSequential(){
    String list = intList(1000);
    Summarizer sum = new Summarizer(list);

    String seq = sequentialSummarize(list);
    Collection<Integer> temp = sum.collect(list);
    String par = sum.summarizeCollection(temp);
    return seq.matches(par);
  }

  /**
  * This is not a unit test but rather a probabilistic test. It must be run with
  * a small range, ie 1-100 or 1-10, and with a large data set of > 100 000. This means
  * that the theortical probability of leaving out a number in the range is negligible,
  * and in fact with pseudo random numbers this probability is zero. So for a large enough
  * list and a small enough range, this should always return an answer of Min-(Min+Range)
  *
  * @return boolean
  *       True if the algorithm output is the same as the correct answer
  */
  public static boolean testLargeRandom(){
    String list = intList(100000);
    Summarizer sum = new Summarizer(list);
    Collection<Integer> temp = sum.collect(list);
    String par = sum.summarizeCollection(temp);
    String rangeString = min + "-" + (min+range-1);
    return par.matches(rangeString);
  }

  /**
  * Main methdod which runs all unit tests and prints out their success. Note that
  * the sequential test is run 1000 times, and any failures result in the method
  * printing that the sequential test failed.
  *
  * @param args
  *       There are noe argument options in this main method.
  *
  */
  public static void main(String [] args){
    System.out.println("Testing given sequence: " + testGiven());
    System.out.println("Testing 1-1000: " + testSequence());
    System.out.println("Testing 1-1000, 2000-4000, 5000-6000, 6100-6200, 6202-6204: " + testMultSequences());
    System.out.println("Testing 1,1,1,1,1,... :" + testConstant(1));
    System.out.println("Testing Constant and Sequences: " + testConstantAndSequences());
    System.out.print("Testing against sequential algorithm: ");
    boolean seq = true;
    for (int i = 0; i < 1000; i++){
      if (!testVsSequential()){
        seq = false;
        break;
      }
    }
    System.out.println(seq);
    System.out.print("Testing Large Random Sequence: ");
    System.out.println(testLargeRandom());
  }
}
