# Cosmos DB Workshop

## Agenda/Labs
0. Azure Cosmos DB Overview (See [Deck/Slides](slides/))
1. Setting Up Cosmos DB instance with SQL API
    - Azure Portal
    - Primary DB Model/API Settings
    - Geo Replication
    - Setting Consistency Models
2. Uploading Data to Cosmos DB with Azure Data Factory v2 and Blob Storage

-- Break --

3. Inserting/Updating/Querying via the Azure Portal as the Primary Interface
4. Demo: accessing via a different wire protocol API (i.e. Mongo && SQL)
5. Demo: Deploying a Node.js Web API that can access your Cosmos instance via Mongo API
6. Advanced Features 1:
    - Partitions/Partition Keys
    - Request Units Explained
    - Indexing (Automatic and Custom)
    - Unique Keys
    

-- Lunch --

7. Advanced Features 2:
    - Stored Procedures
    - Triggers
    - User Defined Functions (UDFs)
8. Overview of the Cosmos DB Change Feed (See [Deck/Slides](slides/))
9. Deploy an Azure Functions App that montiors and reacts to Changes/Events in a Cosmos DB Collection via Cosmos DB Change Feed
10. Connecting an Apache Spark Cluster to Cosmos DB with Databricks on Azure