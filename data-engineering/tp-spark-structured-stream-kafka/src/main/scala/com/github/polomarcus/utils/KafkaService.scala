package com.github.polomarcus.utils

import com.github.polomarcus.conf.ConfService
import com.github.polomarcus.model.NewsKafka
import com.typesafe.scalalogging.Logger
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types._

object KafkaService  {
  private val spark = SparkService.getAndConfigureSparkSession()
  val logger = Logger(this.getClass)
  import spark.implicits._

  // Schéma des données JSON pour la structure News
  val schemaNews = new StructType()
    .add("title", StringType)
    .add("description", StringType)
    .add("media", StringType)
    .add("timestamp", TimestampType)

  /**
   * Fonction de lecture depuis Kafka et transformation des données en Dataset[NewsKafka]
   */
  def read(startingOption: String = "startingOffsets", partitionsAndOffsets: String = "earliest"): Dataset[NewsKafka] = {
    logger.warn(
      s"""
         |Reading from Kafka with these configs :
         |BOOTSTRAP_SERVERS_CONFIG : ${ConfService.BOOTSTRAP_SERVERS_CONFIG}
         |TOPIC_IN : ${ConfService.TOPIC_IN}
         |GROUP_ID : ${ConfService.GROUP_ID}
         |""".stripMargin)

    // Lecture des données depuis Kafka
    val dataframeStreaming = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", ConfService.BOOTSTRAP_SERVERS_CONFIG)
      .option("subscribe", ConfService.TOPIC_IN)
      .option("group.id", ConfService.GROUP_ID)
      .option(startingOption, partitionsAndOffsets)
      .load()

    // Transformation du binaire en JSON et typage des données
    val newsDataset: Dataset[NewsKafka] = dataframeStreaming.withColumn("news",
        from_json($"value".cast(StringType), schemaNews) 
      ).as[NewsKafka]

    newsDataset.filter(_.news != null) // Filtrage des données incorrectes
  }

  /**
   * Affichage des données dans la console pour le débogage.
   */
  def debugStream[T](ds: Dataset[T], completeOutputMode : Boolean = true) = {
    val outputMode = if(completeOutputMode) { OutputMode.Complete() } else { OutputMode.Append() }
    ds
      .writeStream
      .queryName(s"Debug Stream Kafka ${outputMode} ")
      .format("console")
      .outputMode(outputMode)
      .option("truncate", true)
      .start()
  }

  /**
   * Fonction d'écriture des données Kafka dans un fichier Parquet pour la persistance.
   */
  def writeToParquet(ds: Dataset[NewsKafka]) = {
    ds.writeStream
      .format("parquet")
      .option("path", "news_parquet") // Chemin où le fichier Parquet sera sauvegardé
      .option("checkpointLocation", "checkpoints") // Chemin pour stocker les informations de point de contrôle
      .outputMode("append") // Mode Append pour ajouter les nouveaux enregistrements
      .start()
  }
}
