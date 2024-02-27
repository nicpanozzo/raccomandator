import scala.io.Source
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import com.example.ALSraccomandator

object UserSimilarities1 {

    val coOccuranceThreshold = 0.0
    val ratingThreshold = 4.0
    val numReviewsThreshold = 0
    val numSimilarUsers = 5
    val numRecommendations = 10
  
  /** 
   * Load up a Map of movie IDs to movie names.
   * dataPath: Path to the directory containing the movie data files.
   */
  def loadMovieNames(dataPath:String) : Map[Int, String] = {
    
    // Handle character encoding issues:
    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    // Create a Map of Ints to Strings, and populate it from movies.dat file.
    var movieNames:Map[Int, String] = Map()
    
    val lines = Source.fromFile(dataPath + "movies.dat").getLines()
    for (line <- lines) {
      var fields = line.split("::")
      if (fields.length > 1) {
        movieNames += (fields(0).toInt -> fields(1))
      }
    }
    
    return movieNames
  }

  def mapToArray[K](inputMap: Map[K, Double], arrayLength: Int, defaultValue: Double): Array[Double] = {
  // Initialize an array with default values
  val resultArray = Array.fill(arrayLength)(0.0)

  // Update the array with values from the map
  inputMap.foreach { case (key, value) =>
    val index = key.asInstanceOf[Int] // Assuming the keys are integers, adjust if needed
    resultArray(index) = value
  }

  resultArray
}
  /**
   * Computes the cosine similarity between two vectors.
   * v1: First vector.
   * v2: Second vector.
   * Returns: Tuple containing the cosine similarity and the number of co-occurrences.
   */
  def computeCosineSimilarity(v1: Array[Double], v2: Array[Double]): (Double, Int) = {
    var coOccurance = 0
    var dotProduct = 0.0
    var magnitude1 = 0.0
    var magnitude2 = 0.0
    for (i <- 0 until v1.length) {
      dotProduct += v1(i) * v2(i)
      magnitude1 += v1(i) * v1(i)
      magnitude2 += v2(i) * v2(i)

      if (v1(i) != 0 && v2(i) != 0) {
        coOccurance += 1
      }
    }
    magnitude1 = math.sqrt(magnitude1)
    magnitude2 = math.sqrt(magnitude2)
    if (magnitude1 != 0 && magnitude2 != 0) {
      return (dotProduct / (magnitude1 * magnitude2), coOccurance)
    } else {
      return (0.0, coOccurance)
    }
  }
  
  def main(args: Array[String]): Unit = {


    val sc = new SparkContext("local[*]", "MovieSimilarities1M")

    println("\nLoading movie names...")
    
    // Path to the directory containing the movie data files
    val dataPath = if (sc.master == "local[*]") {
      "datasets/"
    } else {
      "gs://raccomandator/"
    }

    // Load movie names from movies.dat file
    val nameDict = loadMovieNames(dataPath)
    
    val ratingsData = sc.textFile(dataPath + "ratings.dat")
    val ratings = ratingsData.map(l => l.split("::")).map(l => (l(0).toInt, (l(1).toInt, l(2).toDouble)))


    // Example data for demonstration purposes
//     val dataTxt = """1::10::5::900909
// 1::11::5::900909
// 2::11::1::900909
// 3::10::5::909090090
// 3::11::1::909090090"""

    // (UserID, (MovieID, Rating))
    // val data = dataTxt.split('\n').map(l => l.split("::")).map(l => (l(0).toInt, (l(1).toInt, l(2).toDouble)))

    // Example movie data for demonstration purposes
//     val moviesTxt = """10::GoldenEye (1995)::Action|Adventure|Thriller
// 11::American President, The (1995)::Comedy|Drama|Romance
// 12::Dracula: Dead and Loving It (1995)::Comedy|Horror"""

    // (MovieID, MovieName)
    // val movies = moviesTxt.split('\n').map(l => l.split("::")).map(l => (l(0).toInt, l(1)))

    val moviesData = sc.textFile(dataPath + "movies.dat")
    val movies = moviesData.map(l => l.split("::")).map(l => (l(0).toInt, l(1)))

    val moviesIndexList = nameDict.map(x => x._1).toList

    val moviesSize = movies.count().toInt


    // Group ratings by user ID, filter out users with less than numReviewsThreshold reviews
    val allUserRatings: RDD[(String, Iterable[(Int, Double)])] = ratings.groupBy(_._1.toString).mapValues(_.map(_._2))    

    // Filter out users with less than numReviewsThreshold reviews
    val userRatings = allUserRatings // .filter(_._2.size > numReviewsThreshold)

    // Create index mappings for users and movies
    // val usersMapIdToIndex: Map[String, Int] = userRatings.keys.zipWithIndex.toMap
    // val moviesMapIdToIndex: Map[String, Int] = movies.map(_._1.toString).zipWithIndex.toMap

    var userReviews = userRatings.mapValues(x => Array.fill(moviesSize)(0.0))

    val useR = userRatings.map(x => (x._1, x._2.map(x => (moviesIndexList.indexOf(x._1), x._2))))

    // print useR
    println("useR")
    // useR.foreach(x => println(x._1 + " " + x._2.mkString(" ")))
    // Fill userReviews with user ratings
    userReviews = useR.mapValues(x => mapToArray(x.toMap, moviesSize, 0.0))

   





    // print user reviews filled with 0s and scores
    println("userReviews")
    // userReviews.foreach(x => println(x._1 + " " + x._2.mkString(" ")))

    println("moviesMapIdToIndex")
    println()


    val userID = args(0).toInt

    if (useR.lookup(userID.toString).isEmpty) {
      println("User ID not found")
      return
    }
    val userMatrixReviews = userReviews.lookup(userID.toString).headOption.getOrElse(Array.empty[Double])
    
    var filteredUserReviews = userReviews.filter(x => x._2.filter(x => x != 0.0).size > numReviewsThreshold)

    if (filteredUserReviews.isEmpty) {
      println("No users with more than " + numReviewsThreshold + " reviews found")
      return
    }

    // remove user from filteredUserRatings
    filteredUserReviews = filteredUserReviews.filter(x => x._1 != userID.toString)


    // Print the user-movie rating matrix
    println("filteredUserMatrix")
    // filteredUserReviews.foreach(x => println(x._1 + " " + x._2.mkString(" ")))

    // Compute cosine similarity for each user
    val similarities = filteredUserReviews.map(x => (x._1, computeCosineSimilarity(x._2, userMatrixReviews)))


    // filter out users with less than coOccuranceThreshold co-occurrences
    .filter(_._2._2 > coOccuranceThreshold)

    // Print similarities
    // similarities.foreach(x => println(x._2))

    // Find the most similar user to user 3
    val mostSimilarUser = similarities.collect().maxBy(_._2._1)
    println("mostSimilarUser")
    println(mostSimilarUser)

    val mostSimilarUsers = similarities.takeOrdered(numSimilarUsers)(Ordering[Double].reverse.on(x => x._2._1)).map(x => x._1).take(numSimilarUsers)

    println("mostSimilarUsers")
    println(mostSimilarUsers.mkString(" "))

    val mostSimilarRatings = userRatings.filter(x => mostSimilarUsers.contains(x._1))

    // add user ratings to mostSimilarRatings
    val mostSimilarRatingsWithUser = mostSimilarRatings ++ userRatings.filter(x => x._1 == userID.toString)

    println("\nTop 10 recommendations:")

    val raccomandator = new ALSraccomandator(sc, (userID, userMatrixReviews), mostSimilarRatingsWithUser)

    val recommendations = raccomandator.evaluate(20, 8, 10)
    println("DONEEEEE")
    recommendations.foreach(x => println(nameDict(x.product.toInt) + " score " + x.rating))
  }
}