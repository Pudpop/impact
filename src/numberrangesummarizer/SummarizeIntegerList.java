package numberrangesummarizer;

import java.util.Arrays;
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
* Recursive Task to summarize an array of integers by rewriting it using the ranges
* of the sequences inherent in the array. Produces a comma delimited list of numbers,
* grouping the numbers into a range when they are sequential.
*
* Sample Input: {1,3,6,7,8,12,13,14,15,21,22,23,24,31}
* Result: "1, 3, 6-8, 12-15, 21-24, 31"
*
* This task is associative so can be reduced using a simple implementaion of Java's
* concurrency libaray, specifically ForkJoinPool implementation.
*
* @author David Kube
*/

public class SummarizeIntegerList extends RecursiveTask<String>{
    /**
    * Array of ints given as parameter in constructor
    */
    private Collection<Integer> coll;
    /**
    * Integer value representing the array size where the recursive problem is serialized.
    */
    private int cutoff;

    long startTime;

    /**
    * Constructs a SummarizeIntegerList to summarize the given list. Sorts the list if the
    * sorted parameter is false.
    *
    */
    public SummarizeIntegerList(Collection<Integer> ints, boolean sorted){
      if (!sorted)
          ints = ints.stream()
                     .sorted()
                     .collect(Collectors.toList());
      this.coll = ints;
      this.cutoff = 5;
    }

    /**
    * Takes two summarized number ranges and converts it into one summarized number range.
    *
    * This task is necessary due to the parallelization of the algorithm. For large datasets,
    * parallelization of the code has a significant speedup which offsets the increased computation
    * from this join method. Even for small datasets, this method is only called once and has a
    * computational complexity of O(1), so does not severely impact performance.
    *
    * The algorithm is written as a large if ladder.
    * This is done to improve readibility. It is possible to reduce the if ladder, but this
    * would require multiple checks on whether or not commas and hyphens exist in each string.
    * This method requires single checks and allows for easier debugging as all possible
    * situations can be easily traced using the if ladder. The if ladder itself runs in O(1) time,
    * so reducing it is also not necessary. This entire method (except the return statements) has
    * a worst case complexity of O(s) where s is the length of the largest integer in the original string.
    * The return statements have a complexity of O(N) where N is the length of given string, due to the
    * substring method.
    *
    * Description of variables used:
    *   leftCom = position of the last comma in the left string
    *   leftHyp = position of the last hyphen in the left string
    *   rightCom = position of the first comma in the right string
    *   rightHyp = position of the first hyphen in the right string
`   *
    *   leftNoCommas = true if there are no commas in the left string
    *   leftNoHyphens = true if there are no hyphens in the left string
    *   rightNoCommas = true if there are no commas in the right string
    *   rightNoHyphens = true if there are no hyphens in the right string
    *
    *   min = the lowest integer in the right substring
    *   max = the highest integer in the left substring
    *
    * Note that there may be confusion between max and min. The naming is ambiguous,
    * as the max is always less than or equal to the min. For readibility, you could read
    * it as maxLeft and minRight.
    *
    * @param left
    *       The string representing the number range that contains numbers less than
    *       the numbers in 'right'. The last character of this string will be compared
    *       to the first character of 'right'.
    * @param right
    *       The string representing the number range that contains numbers greater than
    *       the numbers in 'left'. The first character of this string will be compared
    *       to the last character of 'left'.
    * @return
    *       The string representing the combined number range
    */
    public String joinStrings(String left, String right){
      int leftCom = left.lastIndexOf(',');
      boolean leftNoCommas = (leftCom == -1);
      int leftHyp = 0;
      if (leftNoCommas)
        leftHyp = left.lastIndexOf('-');
      else{
        String temp = left.substring(leftCom+1);
        if (temp.lastIndexOf('-') == -1)
          leftHyp = -1;
        else
          leftHyp = leftCom + temp.lastIndexOf('-') + 1;
      }

      boolean leftNoHyphens = (leftHyp == -1);

      int rightCom = right.indexOf(',');
      boolean rightNoCommas = (rightCom == -1);
      int rightHyp = 0;
      if (rightNoCommas)
        rightHyp = right.indexOf('-');
      else
        rightHyp = right.substring(0,rightCom).indexOf('-');
      boolean rightNoHyphens = (rightHyp == -1);

      int min;
      int max;

      if (!leftNoHyphens){
        max = Integer.parseInt(left.substring(leftHyp+1).trim());
        if (!rightNoHyphens){
          min = Integer.parseInt(right.substring(0,rightHyp).trim());
          if (max == min - 1 || max == min){
            return left.substring(0,leftHyp) + right.substring(rightHyp);
          }
        }
        else{
          if (!rightNoCommas)
            min = Integer.parseInt(right.substring(0,rightCom).trim());
          else
            min = Integer.parseInt(right.trim());
          if (max == min - 1 || max == min)
            return left.substring(0,leftHyp+1) + right;
        }
      }
      else{
        max = Integer.parseInt(left.substring(leftCom+1).trim());
        if (!rightNoHyphens){
          min = Integer.parseInt(right.substring(0,rightHyp).trim());
          if (max == min - 1 || max == min)
            return left + right.substring(rightHyp);
        }
        else{
          if (!rightNoCommas){
            min = Integer.parseInt(right.substring(0,rightCom).trim());
            if (leftNoCommas){
              if (max == min)
                return right;
            }
            if (max == min - 1)
              return left + "-" + right;
            else if (max == min)
              return left.substring(0,leftCom+1) +" "+ right;
          }
          else{
            min = Integer.parseInt(right.trim());
            if (leftNoCommas){
              if (max == min)
                return right;
            }
            if (max == min - 1)
              return left + "-" + right;
            else if (max == min)
              return left;
          }

        }
      }

      //otherwise they can just be concatenated using a comma
      return left + ", " + right;
    }

    /**
    * Compute method from RecursiveTask. This is performed when a recursive task is
    * forked or run from within this thread.
    *
    * The compute method starts by checking if the collection is empty, in which case it
    * returns an empty string
    *
    * It then checks if the size of the collection is below the required cutoff value to
    * begin sequential calculation. If this is the case, it converts the collection into
    * the required summarized range string.
    *
    * If the size is larger than the cutoff, the collection (which is already sorted) is
    * partioned into two halves, and the task is recursively parallelized. The method
    * then the returns the joined string of the two parallelized ranges.
    *
    * @return
    *   The final summarized string based on the collection attribute coll
    *
    */
    @Override
    protected String compute(){
        if (coll.size() == 0){
          return "";
        }
        else if (coll.size() <= cutoff){
          //ignore algorithm if single item.
          if (coll.size() == 1)
            return ""+((Integer) coll.iterator().next());

          //initalized summarized string
          String sumList = "";

          //temporary variables used for checking if in the current range
          int rangeCheck = 0;
          int rangeMin = 0;

          //count variable for reference during iteration
          int count = 0;

          /*
          * Loop through the array of integers, adding them to the string output as necesssary
          */
          for (int current:coll){
            /*
            * If this is the first element, perform the base case
            */
            if (count == 0){
              sumList += current + "";
              rangeCheck = current;
              rangeMin = current;
            }
            else{
              /*
              * If the current elements is part of a sequence, increment temporary variable to carry on sequence
              * Else finish range string and start new range
              */
              if (rangeCheck + 1 == current){
                /*
                * If this is the last element then end the range
                * Else increment range check to carry on with range.
                */
                if (count == coll.size()-1)
                  sumList+= "-" + current;
                else
                  rangeCheck++;
              }
              else if (rangeCheck == current){
                if (count == coll.size()-1 && rangeCheck != rangeMin)
                  sumList+= "-" + current;
                else{
                  count++;
                  continue;
                }
              }
              else{
                /*
                * If the current sequence has length one, then don't write as range, add new number
                * Else write as range and add new number
                */
                if (rangeMin == rangeCheck)
                  sumList += ", " + current;
                else
                  sumList += "-" + rangeCheck + ", " + current;

                /*
                * Start new range
                */
                rangeMin = current;
                rangeCheck = current;
              }
            }
            count++;
          }



          return sumList;
        }
        else{
          /*
          * Split the collection into its first and second half. Creates a list out of the collection
          * and then uses the indices of the list to split the list into two using built in stream functionality.
          */
          List<Integer> list =  coll.stream()
                                    .collect(Collectors.toList());
          int midIndex = ((list.size()-1)/2);
          AtomicInteger count = new AtomicInteger(0);
          List<List<Integer>> lists = new ArrayList<>(list.stream()
                                                          .collect(Collectors.partitioningBy(s -> count.getAndIncrement() > midIndex))
                                                          .values());
          /*
          * Use Java ForkJoinPool classes to parallelize the task. This is a simple implementation as the
          * summarize task is associative so we need only split the array and perform the compute task on each.
          */
          SummarizeIntegerList left = new SummarizeIntegerList(lists.get(0),true);
          SummarizeIntegerList right = new SummarizeIntegerList(lists.get(1),true);

          left.fork();
          String rightStr = right.compute();
          String leftStr = left.join();
          /*
          * Return joined version of the two ranges created in parallel.
          */
          return joinStrings(leftStr,rightStr);
        }
    }
}
