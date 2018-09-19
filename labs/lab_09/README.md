# Lab 9 - Deploy an Azure Functions App that montiors and reacts to Changes/Events in a Cosmos DB Collection via Cosmos DB Change Feed

Example Video: [Trigger Azure Functions using changes to Azure Cosmos DB items](https://azure.microsoft.com/en-us/resources/videos/trigger-azure-functions-using-changes-to-azure-cosmos-db-items/)

## Creating an Azure Function

1. Go to your Azure Portal
2. Click on **Create Resource**
3. In the search field type **function**
4. In the Everything Search results choose **Function App**
5. In the new Blade click the **Create** button on the bottom of the blade
6. In the Function App blade enter the following:
    - In **App name** give your app a unique name e.g. ```<yourname>-cosmoslab```
    - In **Subscription** keep this as your default Subscription
    - In **Resource Group** choose existing and enter or find the resource group you've been using for this lab
    - In **Hosting Plan** keep the ```consumption plan``` option
    - In **Location** choose ```East US 2```
    - In **Storage** create new
    - In **Application Insights** turn it off
7. When the Azure Function deploys:
    - click on **Functions** in the left hand menu
    - at the top of the new blade click on **New function**
    - Follow the steps accordingly to target your flights database and collection
8. When ready run the function
9. Now create a new Document using the Data Explorer in Azure Cosmos DB Portal for your database
10. wait/view the changes in the Azure Function Console
11. Delete the newly created Document using the Data Explorer in Azure Cosmos DB Portal
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