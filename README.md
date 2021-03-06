# Cosmos DB Workshop

## Overview

This is a full day workshop that is designed to demonstrate some key features and strengths of Azure Cosmos DB.

Some features include but are not limited to:
1. Globally Distributed/Multi-Region Database
2. Multi-Model/Multi-Homed API
3. Elastic and Independent Scaling of Compute, Throughput and Storage
4. Automatic Indexing
5. Multiple Defined Consistency Levels [Strong, Bounded-Staleness, Session, Consistent Prefix, Eventual]
6. Cosmos DB Change Feed (Event Bus)
7. Apache Spark Connector for Cosmos DB Change Feed

## Prerequisites
1. An Azure Account/Subscription
    - You can sign-up for a free trial account [here](https://azure.microsoft.com/en-us/free/)
    - If you are using an MSDN or Corporate Subscription ensure you have the ability to create Resource Groups and deploy services into your Account/Subscription
2. A Mac, Linux or Windows device to run the labs
3. A Modern web browser [Chrome, Edge, Safari or Firefox]

### Optional
1. Docker
2. VSCode
3. Mongo CLI
4. Gremlin CLI
5. Cassandra CLI
6. Node.JS, Java and .NET Core installed (You can optionally run these in a Docker Container)


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
    - Indexing (Automatic and Custom)
    - Partitions/Partition Keys
    - Unique Keys
    - Request Units Explained

-- Lunch --

7. Advanced Features 2:
    - Stored Procedures
    - Triggers
    - User Defined Functions (UDFs)
8. Overview of the Cosmos DB Change Feed (See [Deck/Slides](slides/))
9. Deploy an Azure Functions App that montiors and reacts to Changes/Events in a Cosmos DB Collection via Cosmos DB Change Feed
10. Connecting an Apache Spark Cluster to Cosmos DB with Databricks on Azure

## Additional Labs
[Official Azure Cosmos DB Lab Repository](https://github.com/CosmosDB/labs)