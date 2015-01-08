package json2CsvStream

import scala.util.{ Try, Failure }
import scala.io.Source

import com.github.tototoshi.csv.CSVWriter

import jawn.ast._
import jawn.ast.JParser._
import jawn.AsyncParser

import java.io.{ File, OutputStream, FileNotFoundException }

object Json2CsvStream {

  def convert(file: File, resultOutputStream: OutputStream): Try[Long] = {
    if (!file.isFile()) {
      Failure(new FileNotFoundException("The file " + file.getCanonicalPath() + " does not exists"))
    } else {
      convert(Source.fromFile(file, "UTF-8").getLines().toStream, resultOutputStream)
    }
  }

  def convert(chunks: ⇒ Stream[String], resultOutputStream: OutputStream): Try[Long] = {
    val csvWriter = CSVWriter.open(resultOutputStream)
    val parser = jawn.Parser.async[JValue](mode = AsyncParser.UnwrapArray)
    val finalProgress = Converter.consume(chunks, parser, csvWriter)
    csvWriter.close()
    finalProgress.map(_.rowCount)
  }
}