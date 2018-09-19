# Lab 7 - Advanced Features 2 (Demo)
- Stored Procedures
- Triggers
- User Defined Functions (UDFs)

## Stored Procedure trivial example:

```javascript
var helloWorldStoredProc = {
    id: "helloWorld",
    serverScript: function () {
        var context = getContext();
        var response = context.getResponse();
        var collectionLink = collection.getSelfLink();


        response.setBody("Hello, World");
    }
}
```

**Notes**:
- Stored Procedures can get access to the ```context``` in which it's running in: The current database and all actions that can be performed on it, as well as the response object that will be returned to the Client.
- Is bound to the same timeout limitations for execution as everything else
- All actions are ACID compliant
- Commits and rollbacks are implicit
- Executed on the Primary replica

## Triggers

Triggers are similar to Stored Procedures, but are executed on certain types of activities/actions (or all) and can hook into these before (pre) or after (post) an operation is done.
Triggers do not take any input parameters/arguments.
Triggers can get access to the context and are bounded by the same principals as Stored Procedures

## User Defined Functions

Are meant to be an extension of the Cosmos SQL API and used within SQL queries.  Essentially you can use them to apply additonal logic/filtering on queries.

```javascript
var taxUdf = {
    id: "tax",
    serverScript: function tax(income) {

        if(income == undefined) 
            throw 'no input';

        if (income < 1000) 
            return income * 0.1;
        else if (income < 10000) 
            return income * 0.2;
        else
            return income * 0.4;
    }
}
```

```javascript
var query = 'SELECT * FROM TaxPayers t WHERE udf.tax(t.income) > 20000'; 
```

### Reference
- [Azure docs: Azure Cosmos DB server-side programming: Stored procedures, database triggers, and UDFs](https://docs.microsoft.com/en-us/azure/cosmos-db/programming)