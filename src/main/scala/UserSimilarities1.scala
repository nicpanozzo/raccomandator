import scala.io.Source
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import com.example.ALSraccomandator
import com.example.Raccomandator

object UserSimilarities1 {

    val coOccuranceThreshold = 0.0
    // val ratingThreshold = 4.0
    val numReviewsThreshold = 10
    val numSimilarUsers = 50
    val numRecommendations = 10

    val localPath = "datasets/"
    val remotePath = "gs://raccomandator/datasets/"

    val smallDatasetLocation = "small/"
    val largeDatasetLocation = "large/"
    val testDatasetLocation = "test/"

    val moviesFile = "movies.dat"
    val ratingsFile = "ratings.dat"

    val dataPath = localPath + testDatasetLocation
  
  /** 
   * Load up a Map of movie IDs to movie names.
   * dataPath: Path to the directory containing the movie data files.
   */
  def loadMovieNames(sc: SparkContext) : RDD[(Int, String)] = {
    
    // Handle character encoding issues:
    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    // Create a Map of Ints to Strings, and populate it from movies.dat file.
    val file = sc.textFile(dataPath + moviesFile)
    val movieNames = file.map(l => l.split("::")).map(l => (l(0).toInt -> l(1)))

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
    
    val ratingsData = sc.textFile(dataPath + ratingsFile)
    
    // (UserID, (MovieID, Rating))
    val ratings = ratingsData.map(l => l.split("::")).map(l => (l(0).toInt, (l(1).toInt, l(2).toDouble)))

    // (MovieID, Title)
    // Load movie names from movies.dat file
    val movies = loadMovieNames(sc)

    // Create index mappings for movies List[Int]
    // val moviesIndexList = movies.map(_._1).collect().toList.sorted

    val moviesSize = movies.count().toInt


    // (UserID, Iterable[(MovieID, Rating)]
    // Group ratings by user ID, filter out users with less than numReviewsThreshold reviews
    val allUserRatings: RDD[(String, Iterable[(Int, Double)])] = ratings.groupBy(_._1.toString).mapValues(_.map(_._2))    

    // (UserID, Iterable[(MovieID, Rating)]
    // Filter out users with less than numReviewsThreshold reviews
    val userRatings = allUserRatings // .filter(_._2.size > numReviewsThreshold)

    // Create index mappings for users and movies
    // val usersMapIdToIndex: Map[String, Int] = userRatings.keys.zipWithIndex.toMap
    // val moviesMapIdToIndex: Map[String, Int] = movies.map(_._1.toString).zipWithIndex.toMap

    // (UserID, Array[Double]) array riempito di 0.0
    // var userReviews = userRatings.mapValues(x => Array.fill(moviesSize)(0.0))

    // (UserID, Array[Double]) array riempito con i voti
    // val useR = userRatings.map(x => (x._1, x._2.map(x => (moviesIndexList.indexOf(x._1), x._2))))

    // print useR
    println("useR")
    // useR.foreach(x => println(x._1 + " " + x._2.mkString(" ")))
    // Fill userReviews with user ratings
    val userReviews = userRatings.mapValues(x => mapToArray(x.toMap, moviesSize, 0.0))

   





    // print user reviews filled with 0s and scores
    println("userReviews")
    // userReviews.foreach(x => println(x._1 + " " + x._2.mkString(" ")))

    println("moviesMapIdToIndex")
    println()


    val userID = args(0).toInt

    val userReviewsArray = userReviews.lookup(userID.toString).headOption.getOrElse(Array.empty[Double])

    if (userReviewsArray.isEmpty) {
      println("User ID not found")
      return
    }
    // val userMatrixReviews = userReviews.lookup(userID.toString).headOption.getOrElse(Array.empty[Double])
    
    var filteredUserReviews = userReviews.filter(x => x._2.filter(x => x != 0.0).size > numReviewsThreshold)

    if (filteredUserReviews.isEmpty) {
      println("No users with more than " + numReviewsThreshold + " reviews found")
      return
    }

    // remove user from filteredUserRatings
    // filteredUserReviews = filteredUserReviews.filter(x => x._1 != userID.toString)


    // Print the user-movie rating matrix
    println("filteredUserMatrix")
    // filteredUserReviews.foreach(x => println(x._1 + " " + x._2.mkString(" ")))

    // Compute cosine similarity for each user
    val similarities = filteredUserReviews.map(x => (x._1, computeCosineSimilarity(x._2, userReviewsArray)))


    // filter out users with less than coOccuranceThreshold co-occurrences
    .filter(_._2._2 > coOccuranceThreshold)


    // (UserID, (CosineSimilarity, Co-occurrences))
    // Find the most similar user to the input user
    val cosineSimilarities = similarities.sortBy(_._2._1, false)

    // val mostSimilarUser = cosineSimilarities.first()
    // println("mostSimilarUser")
    // println(mostSimilarUser)

    val bestSimilarities = cosineSimilarities.take(numSimilarUsers)

    println("mostSimilarUsers")
    println(bestSimilarities.mkString(" "))

    val mostSimilarUsers = sc.parallelize(bestSimilarities)

    val mostSimilarRatings = userRatings.join(mostSimilarUsers)

    // add user ratings to mostSimilarRatings

    println("\nTop 10 recommendations:")

    val raccomandator: Raccomandator = new ALSraccomandator(sc, (userID, userReviewsArray), mostSimilarRatings.map(x => (x._1, x._2._1)))

    val recommendations = raccomandator.evaluate(20, 8, 10)
    println("DONEEEEE")
    recommendations.foreach(x => println(movies.collect()(x.product.toInt) + " score " + x.rating))
  }
}