import org.apache.log4j._
import org.apache.spark._

/** Find the movies with the most ratings. */
object popMov {

  /** Our main function where the action happens */
  def main(args: Array[String]): Unit = {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.INFO)

    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "PopularMovies")

    sc.setLogLevel("INFO")

    val dataPath = if (sc.master == "local[*]") {
      "datasets/"
    } else {
      "gs://raccomandator/"
    }
    val lines = sc.textFile(dataPath + "u.data")

    // Map to (movieID, 1) tuples
    val movies = lines.map(x => (x.split("\t")(1).toInt, 1))

    // Count up all the 1's for each movie
    val movieCounts = movies.reduceByKey( (x, y) => x + y )

    // Flip (movieID, count) to (count, movieID)
    val flipped = movieCounts.map( x => (x._2, x._1) )

    // Sort
    val sortedMovies = flipped.sortByKey()

    // Collect and print results
    val results = sortedMovies.collect()

    results.foreach(println)
  }

}

