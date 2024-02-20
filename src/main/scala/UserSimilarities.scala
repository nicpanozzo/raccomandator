// import org.apache.spark._
// import org.apache.log4j._
import scala.io.Source
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.HashPartitioner
import java.util.Dictionary

// import scala.math.sqrt

object UserSimilarities {

    /** Load up a Map of movie IDs to movie names. */
    def loadMovieNames(dataPath:String) : Map[Int, String] = {

    // Handle character encoding issues:
    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    // Create a Map of Ints to Strings, and populate it from u.item.
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
  
    
    def main(args: Array[String]): Unit = {
        println("Hello world!")

        val dataPath = "datasets/"
        
        val nameDict = loadMovieNames(dataPath)
        
        
        
        val sc = new SparkContext("local[*]", "UserSimilarities")
        
        val data = sc.textFile(dataPath + "ratings.dat")

        val movies = sc.textFile(dataPath + "movies.dat")

        val movieIDs = movies.map(l => l.split("::")).map(l => (l(0).toInt, l(1)))

        
        // Map ratings to key / value pairs: user ID => movie ID, rating
        val ratings = data.map(l => l.split("::")).map(l => (l(0).toInt, (l(1).toInt, l(2).toDouble))).partitionBy(new HashPartitioner(100))

        
        type MovieRating = (Int, Double)
        val userRatings: RDD[(Int, Iterable[MovieRating])] = ratings.groupByKey()

        val matrix: Array[Array[Double]] = Array.ofDim[Double](userRatings.count().toInt, movieIDs.count().toInt)

        val userMatrix = matrix.map(x => x.map(y => -1.0))
        var usersWithRatings = userRatings.collect().map(x => x._2.toList.sortBy(y => y._1))
        for (userIndex <- usersWithRatings.indices) {
            for (rating <- usersWithRatings(userIndex)) {
                userMatrix(userIndex)(rating._1.toInt) = rating._2
            }
        }


        print(userMatrix(args(0).toInt))

        
        // At this point our RDD consists of userID => ((movieID, rating), (movieID, rating))
        
    }
}