# Lab 9 - Deploy an Azure Functions App that montiors and reacts to Changes/Events in a Cosmos DB Collection via Cosmos DB Change Feed

Example Video: [Trigger Azure Functions using changes to Azure Cosmos DB items](https://azure.microsoft.com/en-us/resources/videos/trigger-azure-functions-using-changes-to-azure-cosmos-db-items/)

Learn how to create a function triggered when data is added to or changed in Azure Cosmos DB. To learn more about Azure Cosmos DB, see [Azure Cosmos DB: Serverless database computing using Azure Functions](..\cosmos-db\serverless-computing-database.md).

![View message in the logs.](./images/functions-create-cosmos-db-triggered-function/quickstart-completed.png)

## Create an Azure Function app

[!INCLUDE [Create function app Azure portal](../../includes/functions-create-function-app-portal.md)]

Next, you create a function in the new function app.

<a name="create-function"></a>

## Create Azure Cosmos DB trigger

1. Expand your function app and click the **+** button next to **Functions**. If this is the first function in your function app, select **Custom function**. This displays the complete set of function templates.

    ![Functions quickstart page in the Azure portal](./images/functions-create-cosmos-db-triggered-function/add-first-function.png)

2. In the search field, type `cosmos` and then choose your desired language for the Azure Cosmos DB trigger template.

    ![Choose the Azure Cosmos DB trigger](./images/functions-create-cosmos-db-triggered-function/select-cosmos-db-trigger-portal.png)

3. Configure the new trigger with the settings as specified in the table below the image.

    ![Create the Azure Cosmos DB triggered function](./images/functions-create-cosmos-db-triggered-function/functions-cosmosdb-trigger-settings.png)
    
    | Setting      | Suggested value  | Description                                |
    | ------------ | ---------------- | ------------------------------------------ |
    | **Name** | Default | Use the default function name suggested by the template. |
    | **Collection name** | Items | Name of collection to be monitored. |
    | **Create lease collection if it doesn't exist** | Checked | The collection doesn't already exist, so create it. |
    | **Database name** | Tasks | Name of database with the collection to be monitored. |

4. Select **New** next to the **Azure Cosmos DB account connection** label, and choose an existing Cosmos DB account or **+ Create new**. 
 
    ![Configure Azure Cosmos DB connection](./images/functions-create-cosmos-db-triggered-function/functions-create-CosmosDB.png)

6. When creating a new Cosmos DB account, use the **New account** settings as specified in the table.

    | Setting      | Suggested value  | Description                                |
    | ------------ | ---------------- | ------------------------------------------ |
    | **ID** | Name of database | Unique ID for the Azure Cosmos DB database  |
    | **API** | SQL | This topic uses the SQL API.  |
    | **Subscription** | Azure Subscription | The subscription under which this new Cosmos DB account is created.  |
    | **Resource Group** | myResourceGroup |  Use the existing resource group that contains your function app. |
    | **Location**  | WestEurope | Select a location near to either your function app or to other apps that use the stored documents.  |

6. Click **OK** to create the database. It may take a few minutes to create the database. After the database is created, the database connection string is stored as a function app setting. The name of this app setting is inserted in **Azure Cosmos DB account connection**. 

7. Click **Create** to create your Azure Cosmos DB triggered function. After the function is created, the template-based function code is displayed.  

    ![Cosmos DB function template in C#](./images/functions-create-cosmos-db-triggered-function/function-cosmosdb-template.png)

    This function template writes the number of documents and the first document ID to the logs. 

Next, you connect to your Azure Cosmos DB account and create the **Tasks** collection in the database. 

## Create the Items collection

1. Open a second instance of the [Azure portal](https://portal.azure.com) in a new tab in the browser. 

2. On the left side of the portal, expand the icon bar, type `cosmos` in the search field, and select **Azure Cosmos DB**.

    ![Search for the Azure Cosmos DB service](./images/functions-create-cosmos-db-triggered-function/functions-search-cosmos-db.png)

2. Choose your Azure Cosmos DB account, then select the **Data Explorer**. 
 
3. In **Collections**, choose **taskDatabase** and select **New Collection**.

    ![Create a collection](./images/functions-create-cosmos-db-triggered-function/cosmosdb-create-collection.png)

4. In **Add Collection**, use the settings shown in the table below the image. 
 
    ![Define the taskCollection](./images/functions-create-cosmos-db-triggered-function/cosmosdb-create-collection2.png)
 
    | Setting|Suggested value|Description |
    | ---|---|--- |
    | **Database ID** | Tasks |The name for your new database. This must match the name defined in your function binding. |
    | **Collection ID** | Items | The name for the new collection. This must match the name defined in your function binding.  |
    | **Storage capacity** | Fixed (10 GB)|Use the default value. This value is the storage capacity of the database. |
    | **Throughput** |400 RU| Use the default value. If you want to reduce latency, you can scale up the throughput later. |
    | **[Partition key](../cosmos-db/partition-data.md#best-practices-when-choosing-a-partition-key)** | /category|A partition key that distributes data evenly to each partition. Selecting the correct partition key is important in creating a performant collection. | 

1. Click **OK** to create the **Tasks** collection. It may take a short time for the collection to get created.

After the collection specified in the function binding exists, you can test the function by adding documents to this new collection.

## Test the function

1. Expand the new **taskCollection** collection in Data Explorer, choose **Documents**, then select **New Document**.

    ![Create a document in taskCollection](./images/functions-create-cosmos-db-triggered-function/create-document-in-collection.png)

2. Replace the contents of the new document with the following content, then choose **Save**.

        {
            "id": "task1",
            "category": "general",
            "description": "some task"
        }

1. Switch to the first browser tab that contains your function in the portal. Expand the function logs and verify that the new document has triggered the function. See that the `task1` document ID value is written to the logs. 

    ![View message in the logs.](./images/functions-create-cosmos-db-triggered-function/functions-cosmosdb-trigger-view-logs.png)

4. (Optional) Go back to your document, make a change, and click **Update**. Then, go back to the function logs and verify that the update has also triggered the function.
11. Delete the newly created Document using the Data Explorer in Azure Cosmos DB Portal once you have seen the output in Azure Functions Console
12. You may now move on to the final lab

### Code

Below is the default code for the Azure Function that is triggered by a Cosmos DB event

```csharp
#r "Microsoft.Azure.Documents.Client"
using System;
using System.Collections.Generic;
using Microsoft.Azure.Documents;

public static void Run(IReadOnlyList<Document> documents, TraceWriter log)
{
    if (documents != null && documents.Count > 0)
    {
        log.Verbose("Documents modified " + documents.Count);
        log.Verbose("First document Id " + documents[0].Id);
    }
}
```