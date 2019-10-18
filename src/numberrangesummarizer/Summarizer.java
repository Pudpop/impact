package numberrangesummarizer;

import java.util.concurrent.ForkJoinPool;
import java.util.Arrays;
import java.util.*;
import java.util.stream.*;

/**
* Object which stores a list of integers in a string and allows the user to
* summarize the integers into ranges.
*
* Sample Input: {1,3,6,7,8,12,13,14,15,21,22,23,24,31}
* Result: "1, 3, 6-8, 12-15, 21-24, 31"
*
*
* @author David Kube
*/
public class Summarizer implements NumberRangeSummarizer{
  /**
  * The original String given by the user. Usually only stored for comparison
  * and printing purposes.
  */
  private String original;
  /**
  * The summarized string which converts the list into a list of ranges as per
  * the brief.
  */
  private String summary;

  /**
  * Constructs a summarizer which allows the user to summarize a given list
  * using the summarizeCollection method.
  */
  public Summarizer(String list){
    this.original = list;
    this.summary="";
  }

  /**
  * Setter for original String which also resets the summary.
  *
  * @param original
  *       The new string which will be summarized
  */
  public void setOriginal(String original){
    this.original = original;
    this.summary = "";
  }

  /**
  * Getter for original string.
  *
  * @return
  *       The original string, unsummarized.
  */
  public String getOriginal(){
    return original;
  }

  /**
  * This method removes white space from the string and then converts it into a
  * collection of integers. The method is synchronized on 'this' as no two threads
  * can change the input of 'this' while another is accessing it.
  *
  * @param input
  *       The string representing a list of integers
  * @return
  *       The collection of the integers in the list given by input.
  */
  public synchronized Collection<Integer> collect(String input){
    String temp = input.replaceAll("\\s+","");
    Stream<Integer> stream =  Arrays.asList(input.split(",")).stream()
                                                          .mapToInt(Integer::parseInt)
                                                          .boxed();
    List<Integer> arrList = stream.collect(Collectors.toList());
    return arrList;
  }

  /**
  * This gets the summarized string. It invokes a ForkJoinPool which parallelizes
  * the task using the RecursiveTask SummarizeIntegerList.
  *
  * Note that this method uses invoke instead of execute, meaning that the thead
  * calling it will be blocked while it is running.
  *
  * The method is also synchornized on 'this'. This is to prevent another thread
  * from chaning the input or the summary while it is running
  *
  * @return
  *       The summarized string.
  */
  public synchronized String summarizeCollection(Collection<Integer> input){
      if (this.summary == ""){
        ForkJoinPool fj = new ForkJoinPool();
        this.summary = fj.invoke(new SummarizeIntegerList(input,false));
        return this.summary;
      }
      else
        return this.summary;
  }
}
