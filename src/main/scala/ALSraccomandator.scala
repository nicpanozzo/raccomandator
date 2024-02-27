package com.example

// import org.apache.spark._
// import org.apache.spark.SparkContext._
// import org.apache.log4j._
// import scala.io.Source
// import java.nio.charset.CodingErrorAction
// import scala.io.Codec
import org.apache.spark.mllib.recommendation._
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext

// type userRatings = (Int, Array[Double])
//   type dataset = RDD[(String, Array[Double])]

class ALSraccomandator(sc: SparkContext, user: (Int, Array[Double]), dataset: RDD[(String, Iterable[(Int, Double)])]) {
  
  
  
  /** Our main function where the action happens */
  def evaluate(numIterations:Int = 20, rank: Int = 8, numberOfResults: Int = 10): Array[Rating] = {
    
    // Set the log level to only print errors
    // Logger.getLogger("org").setLevel(Level.ERROR)
    
     // Create a SparkContext using every core of the local machine

    // sc.setLogLevel("INFO")
    
    println("Raccomending movies ...")
    
    val ratings = dataset.flatMap( x => x._2.map( y => Rating(x._1.toInt, y._1, y._2) ) )
    
    // Build the recommendation model using Alternating Least Squares
    println("\nTraining recommendation model...")
    
    
    val model = ALS.train(ratings, rank, numIterations)
    
    val userID: Int = user._1

    
    println("\nRatings for user ID " + userID + ":")

    // val userRatings = ratings.filter(x => x.user == userID)
    
    // val myRatings = userRatings.collect()
    
    // for (rating <- myRatings) {
    //   println(nameDict(rating.product.toInt) + ": " + rating.rating.toString)
    // }
    
    println("\nTop 10 recommendations:")
    
    val recommendations = model.recommendProducts(userID, numberOfResults + user._2.filter(x => x != 0.0).size)

    // remove already rated movies
    // val recommendationsFiltered = recommendations.filter(x => user._2(x.product - 1) == 0.0).take(numberOfResults)

    // for (recommendation <- recommendations) {
      // println( nameDict(recommendation.product.toInt) + " score " + recommendation.rating )
    // }
    return recommendations

  }
}