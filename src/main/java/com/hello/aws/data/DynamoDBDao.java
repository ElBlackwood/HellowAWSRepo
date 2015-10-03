package com.hello.aws.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

public class DynamoDBDao {
	
	private static final Logger LOG = LoggerFactory.getLogger(DynamoDBDao.class);
	
	private static final String TABLE_NAME = "DynamicTable";
	
	private DynamoDB dynamoDB;
	
	public DynamoDBDao() {
		
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(new ProfileCredentialsProvider());
		client.setRegion(Region.getRegion(Regions.EU_WEST_1)); 
		dynamoDB = new DynamoDB(client);
	}

	public void insertDummyDataToTable() {
		
		if (tableExists()) {
			
			Table table = dynamoDB.getTable(TABLE_NAME);
			
			// Build a list of related items
			List<Number> relatedItems = new ArrayList<Number>();
			relatedItems.add(341);
			relatedItems.add(472);
			relatedItems.add(649);
			
			//Build a map of product pictures 
			Map<String, String> pictures = new HashMap<String, String>();
			pictures.put("FrontView", "http://example.com/products/206_front.jpg");
			pictures.put("RearView", "http://example.com/products/206_rear.jpg");
			pictures.put("SideView", "http://example.com/products/206_left_side.jpg");
			
			//Build a map of product reviews
			Map<String, List<String>> reviews = new HashMap<String, List<String>>();
			
			List<String> fiveStarReviews = new ArrayList<String>();
			fiveStarReviews.add("Excellent! Can't recommend it highly enough!  Buy it!");
			fiveStarReviews.add("Do yourself a favor and buy this");
			reviews.put("FiveStar", fiveStarReviews);
			
			List<String> oneStarReviews = new ArrayList<String>();
			oneStarReviews.add("Terrible product!  Do not buy this.");
			reviews.put("OneStar", oneStarReviews);
			
			// Build the item
			Item item = new Item()
					.withPrimaryKey("Id", 206)
					.withString("Title", "20-Bicycle 206")
					.withString("Description", "206 description")
					.withString("BicycleType", "Hybrid")
					.withString("Brand", "Brand-Company C")
					.withNumber("Price", 500)
					.withString("Gender", "B")
					.withStringSet("Color",  new HashSet<String>(Arrays.asList("Red", "Black")))
					.withString("ProductCategory", "Bike")
					.withBoolean("InStock", true)
					.withNull("QuantityOnHand")
					.withList("RelatedItems", relatedItems)
					.withMap("Pictures", pictures)
					.withMap("Reviews", reviews);
			
			// Write the item to the table 
			PutItemOutcome outcome = table.putItem(item);
			
			LOG.debug("Item added to table. {}", outcome.toString());
			
		} else {
			createTable();
		}
		
	}
	
	private boolean tableExists() {
		TableCollection<ListTablesResult> tables = dynamoDB.listTables();
		
		for (Table table: tables) {
			String tableName = table.getTableName();
			
			LOG.debug("Found a table called [{}]", tableName);
			
			if (TABLE_NAME.equalsIgnoreCase(tableName)) {
				LOG.info("Found {}!", TABLE_NAME);
				return true;
			}
			
		}
		LOG.debug("Coudn't find {}", TABLE_NAME);
		return false;
	}

	private void createTable() {
		
        ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition()
            .withAttributeName("Id")
            .withAttributeType("N"));

        ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
        keySchema.add(new KeySchemaElement()
            .withAttributeName("Id")
            .withKeyType(KeyType.HASH));

        CreateTableRequest request = new CreateTableRequest()
            .withTableName(TABLE_NAME)
            .withKeySchema(keySchema)
            .withAttributeDefinitions(attributeDefinitions)
            .withProvisionedThroughput(new ProvisionedThroughput()
                .withReadCapacityUnits(5L)
                .withWriteCapacityUnits(6L));

        System.out.println("Issuing CreateTable request for " + TABLE_NAME);
        Table table = dynamoDB.createTable(request);

        System.out.println("Waiting for " + TABLE_NAME
            + " to be created...this may take a while...");
        try {
			table.waitForActive();
			getTableInformation();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
    private void getTableInformation() {

        System.out.println("Describing " + TABLE_NAME);

        TableDescription tableDescription = dynamoDB.getTable(TABLE_NAME).describe();
        System.out.format("Name: %s:\n" + "Status: %s \n"
                + "Provisioned Throughput (read capacity units/sec): %d \n"
                + "Provisioned Throughput (write capacity units/sec): %d \n",
        tableDescription.getTableName(), 
        tableDescription.getTableStatus(), 
        tableDescription.getProvisionedThroughput().getReadCapacityUnits(),
        tableDescription.getProvisionedThroughput().getWriteCapacityUnits());
    }
}
