import scala.io.Source
import java.io.PrintWriter

object CSVConverter {
  def main(args: Array[String]): Unit = {
    // Read the CSV file
    
    // obtein the path of this file
    val path = new java.io.File(".").getCanonicalPath()
    val inputFile = "datasets/large/ratings.csv"
    val outputFile = "datasets/large/ratings.dat"

    val lines = Source.fromFile(inputFile).getLines().drop(1)

    // Open a PrintWriter to write to the output file
    val writer = new PrintWriter(outputFile)

    // Iterate through each line in the CSV file
    for (line <- lines) {
      // Split the line by ','
      
      val parts = line.split(",")
    
      val tail = parts.last
      val head = parts.head

      val center = parts.drop(1).dropRight(1)
      

      // Replace '"' with '' in the movie title (handling the case where the title contains ',')
        val title = center.mkString("::").replace(""""""", """""")


      // Combine the parts back together with '::' as the delimiter
      val newLine = s"${head}::$title::${tail}"

      // Write the modified line to the output file
      writer.println(newLine)
    }

    // Close the PrintWriter
    writer.close()

    println("CSV conversion complete.")
  }
}