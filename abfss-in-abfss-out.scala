// Databricks notebook source
import org.apache.spark.sql.types.{StructType, StructField, IntegerType, StringType}

spark.conf.set("spark.openlineage.url.param.code", {{secrets/purview-to-adb-kv/Ol-Output-Api-Key}})

// COMMAND ----------

val storageServiceName = spark.conf.get("spark.openlineage.samplestorageaccount")
val storageContainerName = spark.conf.get("spark.openlineage.samplestoragecontainer")
val abfssRootPath = "abfss://"+storageContainerName+"@"+storageServiceName+".dfs.core.windows.net"

val storageKey = dbutils.secrets.get("purview-to-adb-kv", "storage-account-access-key")

spark.conf.set("fs.azure.account.key."+storageServiceName+".dfs.core.windows.net", storageKey)

// COMMAND ----------

val exampleASchema = StructType(
     StructField("id", IntegerType, true) ::
     StructField("postalCode", StringType, false) ::
     StructField("streetAddress", StringType, false) :: Nil)

val exampleA = (
    spark.read.format("csv")
  .schema(exampleASchema)
  .option("header", true)
  .load(abfssRootPath+"/examples/data/csv/exampleInputA/exampleInputA.csv")
)


val exampleBSchema = StructType(
     StructField("id", IntegerType, true) ::
     StructField("city", StringType, false) ::
     StructField("stateAbbreviation", StringType, false) :: Nil)

val exampleB = (
    spark.read.format("csv")
  .schema(exampleBSchema)
  .option("header", true)
  .load(abfssRootPath+"/examples/data/csv/exampleInputB/exampleInputB.csv")
)

// COMMAND ----------

val outputDf = exampleA.join(exampleB, exampleA("id") === exampleB("id"), "inner").drop(exampleB("id"))

outputDf.repartition(1).write.mode("overwrite").format("csv").save(abfssRootPath+"/examples/data/csv/exampleOutput/")

// COMMAND ----------


