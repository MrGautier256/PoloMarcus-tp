package com.github.polomarcus.main

import com.github.polomarcus.model.NewsKafka
import com.github.polomarcus.utils.{KafkaService, SparkService}
import com.typesafe.scalalogging.Logger
import org.apache.spark.sql.Dataset

object Main {
  def main(args: Array[String]) {
    val logger = Logger(this.getClass)
    logger.info("Used `sbt run` to start the app")

    // Initialisation de la session Spark
    val spark = SparkService.getAndConfigureSparkSession()
    import spark.implicits._

    // Lecture du flux Kafka avec typage de Dataset[NewsKafka]
    val newsDatasets: Dataset[NewsKafka] = KafkaService.read()
    newsDatasets.printSchema()

    // Debug : affichage du flux dans la console
    KafkaService.debugStream(newsDatasets, false)

    // TODO: Appel de la fonction pour écrire dans un fichier Parquet
    KafkaService.writeToParquet(newsDatasets)

    // TODO: Comptage des nouvelles par média et affichage du résultat
    val counted = newsDatasets.groupBy($"news.media").count()
    KafkaService.debugStream(counted)

    // Attente de la terminaison des streams
    spark.streams.awaitAnyTermination()
  }
}
