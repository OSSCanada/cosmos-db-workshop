// Databricks notebook source
// MAGIC %md
// MAGIC # On-Time Flight Performance with Spark and Cosmos DB (Seattle)
// MAGIC ## On-Time Flight Performance Background
// MAGIC This notebook provides an analysis of On-Time Flight Performance and Departure Delays data using GraphFrames for Apache Spark.
// MAGIC - Original blog post: [On-Time Flight Performance with GraphFrames with Apache Spark Blog Post](https://databricks.com/blog/2016/03/16/on-time-flight-performance-with-graphframes-for-apache-spark.html)
// MAGIC - Original Notebook: [On-Time Flight Performance with GraphFrames with Apache Spark Notebook](http://cdn2.hubspot.net/hubfs/438089/notebooks/Samples/Miscellaneous/On-Time_Flight_Performance.html)
// MAGIC 
// MAGIC ![](https://github.com/dennyglee/databricks/blob/master/images/airports-d3-m.gif?raw=true)
// MAGIC 
// MAGIC Source Data:
// MAGIC 
// MAGIC - [OpenFlights: Airport, airline and route data](http://openflights.org/data.html)
// MAGIC - [United States Department of Transportation: Bureau of Transportation Statistics (TranStats)](http://www.transtats.bts.gov/DL_SelectFields.asp?Table_ID=236&DB_Short_Name=On-Time)
// MAGIC     - Note, the data used here was extracted from the US DOT:BTS between 1/1/2014 and 3/31/2014*
// MAGIC 
// MAGIC References:
// MAGIC - [GraphFrames User Guide](http://graphframes.github.io/user-guide.html)
// MAGIC - [GraphFrames: DataFrame-based Graphs (GitHub)](https://github.com/graphframes/graphframes)
// MAGIC - [D3 Airports Example](http://mbostock.github.io/d3/talk/20111116/airports.html)
// MAGIC 
// MAGIC ## Spark to Cosmos DB Connector
// MAGIC Connecting Apache Spark to Azure Cosmos DB accelerates your ability to solve your fast moving Data Sciences problems where your data can be quickly persisted and retrieved using Azure Cosmos DB's DocumentDB API. With the Spark to Cosmos DB conector, you can more easily solve scenarios including (but not limited to) blazing fast IoT scenarios, update-able columns when performing analytics, push-down predicate filtering, and performing advanced analytics to data sciences against your fast changing data against a geo-replicated managed document store with guaranteed SLAs for consistency, availability, low latency, and throughput.
// MAGIC 
// MAGIC The Spark to Cosmos DB connector utilizes the [Azure DocumentDB Java SDK](https://github.com/Azure/azure-documentdb-java) will utilize the following flow:
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/dennyglee/notebooks/master/images/Azure-DocumentDB-Spark_Connector_600x266.png)
// MAGIC 
// MAGIC The data flow is as follows:
// MAGIC 
// MAGIC 1. Connection is made from Spark master node to Cosmos DB gateway node to obtain the partition map. Note, user only specifies Spark and Cosmos DB connections, the fact that it connects to the respective master and gateway nodes is transparent to the user.
// MAGIC 2. This information is provided back to the Spark master node. At this point, we should be able to parse the query to determine which partitions (and their locations) within Cosmos DB we need to access.
// MAGIC 3. This information is transmitted to the Spark worker nodes ...
// MAGIC 4. Thus allowing the Spark worker nodes to connect directly to the Cosmos DB partitions directly to extract the data that is needed and bring the data back to the Spark partitions within the Spark worker nodes.

// COMMAND ----------

// MAGIC %scala
// MAGIC 
// MAGIC import com.microsoft.azure.cosmosdb.spark.schema._
// MAGIC import com.microsoft.azure.cosmosdb.spark.CosmosDBSpark
// MAGIC import com.microsoft.azure.cosmosdb.spark.config.Config
// MAGIC 
// MAGIC import org.apache.spark.sql.functions._

// COMMAND ----------

# Connection
%scala

val configMap = Map(
  "Endpoint" -> "https://<your_cosmos_account>.documents.azure.com:443/",
  "Masterkey" -> "<primary_or_secondary_key>",
  "Database" -> "flights",
  "Collection" -> "flights",
  "preferredRegions" -> "East US 2")
val config = Config(configMap)

// COMMAND ----------

// MAGIC %scala
// MAGIC 
// MAGIC val flights = spark.read.format("com.microsoft.azure.cosmosdb.spark").options(configMap).load()
// MAGIC flights.cache()
// MAGIC flights.createOrReplaceTempView("flights")
// MAGIC flights.count()

// COMMAND ----------

// MAGIC %md ### Obtain Airport Information

// COMMAND ----------

// MAGIC %python
// MAGIC # Set File Paths
// MAGIC airportsnaFilePath = "wasb://data@doctorwhostore.blob.core.windows.net/airport-codes-na.txt"
// MAGIC 
// MAGIC # Obtain airports dataset
// MAGIC airportsna = spark.read.csv(airportsnaFilePath, header='true', inferSchema='true', sep='\t')
// MAGIC airportsna.createOrReplaceTempView("airports")

// COMMAND ----------

// MAGIC %sql
// MAGIC select count(1) from airports where IATA = 'SEA'

// COMMAND ----------

// MAGIC %md ### Flights departing from Seattle

// COMMAND ----------

// MAGIC %sql
// MAGIC select count(1) from flights where origin = 'SEA'

// COMMAND ----------

// MAGIC %md ### Top 10 Delayed Destinations originating from Seattle

// COMMAND ----------

// MAGIC %sql
// MAGIC select a.city as destination, sum(f.DepDelayMinutes) as TotalDelays, count(1) as Trips
// MAGIC from flights f
// MAGIC join airports a
// MAGIC   on a.IATA = f.dest
// MAGIC where f.origin = 'SEA'
// MAGIC and f.DepDelayMinutes > 0
// MAGIC group by a.city 
// MAGIC order by sum(DepDelayMinutes) desc limit 10

// COMMAND ----------

// MAGIC %md ### Calculate median delays by destination cities departing from Seattle

// COMMAND ----------

// MAGIC %sql
// MAGIC select a.city as destination, percentile_approx(f.DepDelay, 0.5) as median_delay
// MAGIC from flights f
// MAGIC join airports a
// MAGIC   on a.IATA = f.dest
// MAGIC where f.origin = 'SEA'
// MAGIC group by a.city 
// MAGIC order by percentile_approx(f.DepDelay, 0.5)

// COMMAND ----------

// MAGIC %md ## Building up a GraphFrames
// MAGIC Using GraphFrames for Apache Spark to run degree and motif queries against Cosmos DB

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC # Build `departureDelays` DataFrame
// MAGIC departureDelays = spark.sql("select cast(f.FlightDate as int) as tripid, cast(concat(concat(concat(concat(concat(concat('2014-', concat(concat(substr(cast(f.FlightDate as string), 1, 2), '-')), substr(cast(f.FlightDate as string), 3, 2)), ' '), substr(cast(f.FlightDate as string), 5, 2)), ':'), substr(cast(f.FlightDate as string), 7, 2)), ':00') as timestamp) as `localdate`, cast(f.DepDelay as int) as delay, cast(f.Distance as int), f.origin as src, f.dest as dst, f.OriginCityName as city_src, F.DestCityName as city_dst, F.OriginStateName as state_src, F.DestStateName as state_dst from flights f join airports o on o.iata = f.origin join airports d on d.iata = f.dest") 
// MAGIC 
// MAGIC # Create Temporary View and cache
// MAGIC departureDelays.createOrReplaceTempView("departureDelays")
// MAGIC departureDelays.cache()

// COMMAND ----------

// MAGIC %sh
// MAGIC pip install "git+https://github.com/munro/graphframes.git@release-0.5.0#egg=graphframes&subdirectory=python"

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC # Note, ensure you have already installed the GraphFrames spack-package
// MAGIC from pyspark.sql.functions import *
// MAGIC from graphframes import *
// MAGIC 
// MAGIC # Create Vertices (airports) and Edges (flights)
// MAGIC tripVertices = airportsna.withColumnRenamed("IATA", "id").distinct()
// MAGIC tripEdges = departureDelays.select("tripid", "delay", "src", "dst", "city_dst", "state_dst")
// MAGIC 
// MAGIC # Cache Vertices and Edges
// MAGIC tripEdges.cache()
// MAGIC tripVertices.cache()
// MAGIC 
// MAGIC # Create TripGraph
// MAGIC tripGraph = GraphFrame(tripVertices, tripEdges)

// COMMAND ----------

// MAGIC %md ### What flights departing SEA with the most significant average delays
// MAGIC Note, the joins are there to see the city name instead of the IATA codes.  The `rank()` code is there to help order the data correctly when viewed in Jupyter notebooks.

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC flightDelays = tripGraph.edges.filter("src = 'SEA' and DepDelay > 0").groupBy("src", "dst").avg("delay").sort(desc("avg(delay)"))
// MAGIC flightDelays.createOrReplaceTempView("flightDelays")

// COMMAND ----------

// MAGIC %sql
// MAGIC select a.city, `avg(delay)` as avg_delay 
// MAGIC from flightDelays f
// MAGIC join airports a
// MAGIC on f.dst = a.iata
// MAGIC order by `avg(delay)` 
// MAGIC desc limit 10

// COMMAND ----------

// MAGIC %md ### Which is the most important airport (in terms of connections)
// MAGIC It would take a relatively complicated SQL statement to calculate all of the edges to a single vertex, grouped by the vertices.  Instead, we can use the graph `degree` method.

// COMMAND ----------

// MAGIC %python 
// MAGIC 
// MAGIC airportConnections = tripGraph.degrees.sort(desc("degree"))
// MAGIC airportConnections.createOrReplaceTempView("airportConnections")

// COMMAND ----------

// MAGIC %sql
// MAGIC select a.city, f.degree 
// MAGIC from airportConnections f 
// MAGIC join airports a
// MAGIC   on a.iata = f.id
// MAGIC order by f.degree desc 
// MAGIC limit 10

// COMMAND ----------

// MAGIC %md ### Are there direct flights between Seattle and San Jose?

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC filteredPaths = tripGraph.bfs(
// MAGIC     fromExpr = "id = 'SEA'",
// MAGIC     toExpr = "id = 'SJC'",
// MAGIC     maxPathLength = 1)
// MAGIC display(filteredPaths)

// COMMAND ----------

// MAGIC %md ### But are there any direct flights between San Jose and Buffalo?
// MAGIC * Try maxPathLength = 1 which means one edge (i.e. one flight) between `SJC` and `BUF`, i.e. direct flight
// MAGIC * Try maxPathLength = 2 which means two edges between `SJC` and `BUF`, i.e. all the different variations of flights between San Jose and Buffalo with only one stop oever in between?

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC filteredPaths = tripGraph.bfs(
// MAGIC   fromExpr = "id = 'SJC'",
// MAGIC   toExpr = "id = 'BUF'",
// MAGIC   maxPathLength = 1)
// MAGIC display(filteredPaths)

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC filteredPaths = tripGraph.bfs(
// MAGIC   fromExpr = "id = 'SJC'",
// MAGIC   toExpr = "id = 'BUF'",
// MAGIC   maxPathLength = 2)
// MAGIC display(filteredPaths)

// COMMAND ----------

// MAGIC %md ### In that case, what is the most common transfer point between San Jose and Buffalo?

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC filteredPaths = tripGraph.bfs(
// MAGIC   fromExpr = "id = 'SJC'",
// MAGIC   toExpr = "id = 'BUF'",
// MAGIC   maxPathLength = 2)
// MAGIC 
// MAGIC filteredPaths.cache()

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC display(filteredPaths.groupBy("v1.id", "v1.city").count().orderBy(desc("count")).limit(10))
