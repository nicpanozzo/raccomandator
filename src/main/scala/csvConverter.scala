import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.Encoders



object ReplaceMovieIds {

    val path = "datasets/test/"

    case class Movie(movieId: Int, title: String, genre: String)
    case class UserRating(userId: Int, movieId: Int, rating: Double, timestamp: Long)

  def main(args: Array[String]): Unit = {
    // Create a SparkSession
    val spark = SparkSession.builder()
      .appName("ReplaceMovieIds")
      .config("spark.master", "local")
      .getOrCreate()

    import spark.implicits._
    // Read movies CSV file

    if (args.length < 2) {
      println("Usage: ReplaceMovieIds <movies.csv>")
      System.exit(1)
    }

    // Takes the first argument as the path to the movies CSV file and the second argument as the file extension
    val path = args(0)
    val extension = args(1)
  

    val moviesDF = readMovies(spark, path + "movies." + extension)

    println(moviesDF.show())

    // Add a sequential ID column to movies
    val moviesWithSequentialId = addSequentialId(moviesDF)

    // Create a mapping of original movie IDs to sequential IDs
    val idMapping = moviesWithSequentialId.select("movieId", "sequential_id").as[(Int, Long)]
      .collect()
      .toMap

    // Replace movie IDs with sequential IDs in movies and ratings
    val updatedMovies = replaceMovieIds(moviesWithSequentialId, idMapping)
    val updatedRatings = replaceMovieIds(readRatings(spark, path + "ratings." + extension), idMapping)

    // Write updated movies and ratings CSV files
    saveDataFrameToFile(updatedMovies, path + "updated_movies.csv")
    saveDataFrameToFile(updatedRatings, path + "updated_ratings.csv")

    // Stop SparkSession
    spark.stop()
  }

  // Read CSV file into DataFrame
  def readMovies(spark: SparkSession, path: String): DataFrame = {
    

    val schema = Encoders.product[Movie].schema
    val extension = path.split('.').last

    if (extension == "csv") {
      spark.read.option("header", "true").schema(schema).csv(path)
    } else {
      spark.read.option("header", "false").option("delimiter", "::").schema(schema).csv(path)
    }
  }

  def readRatings(spark: SparkSession, path: String): DataFrame = {
    

    val schema = Encoders.product[UserRating].schema
    val extension = path.split('.').last

    if (extension == "csv") {
      spark.read.option("header", "true").schema(schema).csv(path)
    } else {
      spark.read.option("header", "false").option("delimiter", "::").schema(schema).csv(path)
    }
  }


  // Add a sequential ID column to DataFrame
  def addSequentialId(df: DataFrame): DataFrame = {
    df.withColumn("sequential_id", monotonically_increasing_id())
  }



  // Replace original IDs with sequential IDs in DataFrame
  def replaceMovieIds(df: DataFrame, idMapping: Map[Int, Long]): DataFrame = {
    val replaceId = udf((id: Int) => idMapping(id))
    df.withColumn("movieId", replaceId(col("movieId").cast(IntegerType)))
  }

   def saveDataFrameToFile(df: DataFrame, outputPath: String): Unit = {
    df.write
      .format("csv")
      .option("header", "false") // dont Write header line
      .option("delimiter", "::")
      .mode("overwrite") // Overwrite existing file if any
      .save(outputPath)
  }
}