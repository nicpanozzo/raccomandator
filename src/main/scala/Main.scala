import org.apache.spark.SparkContext

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

/**
 * Test IO to wasb
 */
object Main {
  def main (arg: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Hellooo")
    val sc = new SparkContext(conf)

    println("Hello world!")

    print(sc.startTime)
  }
}