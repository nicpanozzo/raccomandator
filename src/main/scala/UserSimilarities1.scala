// import org.apache.spark._
// import org.apache.log4j._
import scala.io.Source
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import java.util.Dictionary

// import scala.math.sqrt

object UserSimilarities1 {

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
        println("Hello world!")

        val dataPath = "datasets/"
        
        val nameDict = loadMovieNames(dataPath)
        
        
        
        
        val dataTxt = """1::10::5::900909
1::11::5::900909
2::10::1::900909
2::11::1::900909
3::10::5::909090090
3::11::1::909090090"""

        // (UserID, (MovieID, Rating))
        val data = dataTxt.split('\n').map(l => l.split("::")).map(l => (l(0).toInt, (l(1).toInt, l(2).toDouble)))


        val moviesTxt = """10::GoldenEye (1995)::Action|Adventure|Thriller
11::American President, The (1995)::Comedy|Drama|Romance
12::Dracula: Dead and Loving It (1995)::Comedy|Horror"""

        // (MovieID, MovieName)
        val movies = moviesTxt.split('\n').map(l => l.split("::")).map(l => (l(0).toInt, l(1)))



        
        type MovieRating = (Int, Double)
        val userRatings: Map[String, Iterable[MovieRating]] = data.groupBy(_._1.toString).mapValues(_.map(_._2))


        val usersMapIndex: Map[String, Int] = userRatings.keys.zipWithIndex.toMap
        val moviesMapIndex: Map[String, Int] = movies.map(_._1.toString).zipWithIndex.toMap

        val matrix: Array[Array[Double]] = Array.ofDim[Double](userRatings.count(_ => true), movies.count(_ => true))

        val userMatrix = matrix.map(x => x.map(y => 0.0))

        print(usersMapIndex)
        print(userRatings)
        for (user <- userRatings) {
            for (rating <- user._2) {
                userMatrix(usersMapIndex(user._1))(moviesMapIndex(rating._1.toString())) = rating._2
            }
        }


        userMatrix.foreach(x=>println(x.mkString(" ")))

        // compute cosine similarity on userMatrix for each user
        // for user 3, find the most similar user

        val user3 = userMatrix(2)
        val similarities = userMatrix.map(x => (x, computeCosineSimilarity(x, user3)._1))

        similarities.foreach(x=>println(x._2))

        // take the most similar user and recommend the movies that the user 3 has not seen yet

        val mostSimilarUser = similarities.maxBy(_._2)
        
        // At this point our RDD consists of userID => ((movieID, rating), (movieID, rating))
        
    }
}