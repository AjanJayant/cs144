package edu.ucla.cs.cs144;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.util.Date;
import java.util.Iterator;
import java.text.SimpleDateFormat;

import edu.ucla.cs.cs144.DbManager;
import edu.ucla.cs.cs144.SearchConstraint;
import edu.ucla.cs.cs144.SearchResult;

import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;
import javax.xml.transform.*;
import java.io.StringWriter;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class AuctionSearch implements IAuctionSearch {

	/* 
         * You will probably have to use JDBC to access MySQL data
         * Lucene IndexSearcher class to lookup Lucene index.
         * Read the corresponding tutorial to learn about how to use these.
         *
         * Your code will need to reference the directory which contains your
	 * Lucene index files.  Make sure to read the environment variable 
         * $LUCENE_INDEX with System.getenv() to build the appropriate path.
	 *
	 * You may create helper functions or classes to simplify writing these
	 * methods. Make sure that your helper functions are not public,
         * so that they are not exposed to outside of this class.
         *
         * Any new classes that you create should be part of
         * edu.ucla.cs.cs144 package and their source files should be
         * placed at src/edu/ucla/cs/cs144.
         *
         */
	
	public SearchResult[] basicSearch(String query, int numResultsToSkip, 
			int numResultsToReturn) {
		// TODO: Your code here!
		try
		{
			SearchResult[] searchResult;

			String directory = System.getenv("LUCENE_INDEX") + "/basic";
			boolean skipped = false;
			boolean returnedAll = false;
			
			IndexSearcher searcher = new IndexSearcher(directory);
			QueryParser qParser = new QueryParser("content", new StandardAnalyzer());
			Query q = qParser.parse(query);
			Hits hits = searcher.search(q);
			
			if(numResultsToSkip == 0)
				skipped = true;	
			if(numResultsToReturn == 0)
				numResultsToReturn = hits.length();
				
			searchResult = new SearchResult[numResultsToReturn]; // Change later
			
			for(int i = 0; i < hits.length() ; i++) 
			{
				if(returnedAll)
					break;
				else if(i == numResultsToReturn - 1)
					returnedAll = true;
				if(skipped) 
				{
					Document doc = hits.doc(i);
					String itemId = doc.get("ItemID");
					String itemName = doc.get("ItemName");
					SearchResult s = new SearchResult();
					s.setItemId(itemId);
					s.setName(itemName);
					searchResult[i] = s;
				}
				else if(i > numResultsToSkip)
					skipped = true;
				
        	}
        
			return searchResult;
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			return new SearchResult[0];
		}

	}

	public SearchResult[] advancedSearch(SearchConstraint[] constraints, 
			int numResultsToSkip, int numResultsToReturn) {
		// TODO: Your code here!
		boolean doSQL = true;
		String directory = System.getenv("LUCENE_INDEX") + "/basic";		
		String query = getQuery(constraints);
		//System.out.println(query);
		if(query.equals(""))
			doSQL = false;
		try
		{
			ArrayList<SearchResult> sqlResults = new ArrayList<SearchResult>();

			if(doSQL)
			{
				Connection c = DbManager.getConnection(true);
				Statement s = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
												ResultSet.CONCUR_UPDATABLE);
	        
				ResultSet queryResults = s.executeQuery(query);
	
				while(queryResults.next())
				{
					int id = queryResults.getInt("ItemID");
					String itemID = id + "";
					String itemName = queryResults.getString("Name");
					SearchResult sR = new SearchResult();
					sR.setItemId(itemID);
					sR.setName(itemName);
					sqlResults.add(sR);
				}
			}	
			ArrayList<SearchResult> luceneResults = new ArrayList<SearchResult>();
			
			boolean doLucene = false;
			for(SearchConstraint sC : constraints)
			{
				if(sC.getFieldName().equals(FieldName.ItemName) 
						|| sC.getFieldName().equals(FieldName.Category) 
						|| sC.getFieldName().equals(FieldName.Description))
				{
					doLucene = true;
					break;
				}
			}

			//System.out.println(doLucene);
			if(doLucene)
			{
				SearchConstraint startConstraint = new SearchConstraint(); 				
				for(SearchConstraint stCn : constraints)
				{
					if(stCn.getFieldName().equals(FieldName.ItemName) || stCn.getFieldName().equals(FieldName.Category) 
						   	 || stCn.getFieldName().equals(FieldName.Description))
					{
						startConstraint = stCn;
						break;
					}
				}
				//System.out.println(startConstraint.getFieldName());

				String field = startConstraint.getFieldName();
				String value = startConstraint.getValue();
				
				IndexSearcher searcher = new IndexSearcher(directory);
				QueryParser qParser = new QueryParser("content", new StandardAnalyzer());
				Query q = qParser.parse(value);
				Hits hits = searcher.search(q);
				for(int i = 0; i < hits.length() ; i++) 
				{ 

					Document doc = hits.doc(i);

					boolean shouldAdd = true;
					
					for(int j = 0; j < constraints.length; j++)
					{
						if(constraints[j].getFieldName().equals(FieldName.ItemName) || constraints[j].getFieldName().equals(FieldName.Category) 
								   	 || constraints[j].getFieldName().equals(FieldName.Description))
						{
							//System.out.println("Ajan");
							String conVal = constraints[j].getValue();
							String correspondingVal = doc.get(constraints[j].getFieldName());
							
							//System.out.println(conVal);
							//System.out.println(correspondingVal);
							
							boolean flag = false;
							for(int k = 0; k < correspondingVal.length(); k++)
							{
								String str = new String();
								if(k + conVal.length()  >= correspondingVal.length())
									str = correspondingVal.substring(k);
								else
									str = correspondingVal.substring(k, k + conVal.length());
								if(str.equalsIgnoreCase(conVal))
								{
									if(k == 0 || ( correspondingVal.charAt(k - 1) == ' '))	
									{
										if(k + conVal.length() < correspondingVal.length())
										{
											
											if(correspondingVal.charAt(k + conVal.length()) == ' ' ||
											   correspondingVal.charAt(k + conVal.length()) == '&' || 
											   correspondingVal.charAt(k + conVal.length()) == '-' ||
											   correspondingVal.charAt(k + conVal.length()) == '~' ||
											   correspondingVal.charAt(k + conVal.length()) == ',' ||
											   correspondingVal.charAt(k + conVal.length()) == '`')
											{

												flag = true;
												break;
											}	
										}
										else
										{
											flag = true;
											break;

										}
									}
								}
							}
							if(!flag)
							{
								shouldAdd = false;
								break;
							}
						}
					}
					
					if(shouldAdd)
					{
						String itemId = doc.get("ItemID");
						String itemName = doc.get("ItemName");
	
						SearchResult sRL = new SearchResult();
					
						sRL.setItemId(itemId);
						sRL.setName(itemName);
						luceneResults.add(sRL);
					}
				}
			}
			
			SearchResult[] searchResult = new SearchResult[0];
			int count = 0;		
			
			if(doSQL && !doLucene)
			{
				searchResult = new SearchResult[sqlResults.size()];
				for(int i = 0; i < searchResult.length; i++)
				{
					searchResult[i] = sqlResults.get(i);
				}
			}
			else if(!doSQL && doLucene)
			{
				searchResult = new SearchResult[luceneResults.size()];
				for(int i = 0; i < searchResult.length; i++)
				{
					searchResult[i] = luceneResults.get(i);
				}

			}
			else
			{
				ArrayList<SearchResult> searchList = new ArrayList<SearchResult>();
				for(int i = 0; i < sqlResults.size(); i++)
				{
					for(int j = 0; j < luceneResults.size(); j++)
					{
						if(sqlResults.get(i).getItemId().equals(luceneResults.get(j).getItemId()))
							searchList.add(luceneResults.get(j));
					}
				}
				searchResult = new SearchResult[searchList.size()];
				for(int i = 0; i < searchList.size(); i++)
				{
					searchResult[i] = searchList.get(i);
				}
			}
			return searchResult;
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			return new SearchResult[0];
		}
	}

	public String getXMLDataForItemId(String itemId) 
	{

        String xmlstore = "";
        Connection conn = null;
        // Create a connection to the database
        try 
        {
            // Connection to db manager
            conn = DbManager.getConnection(true);
            Statement statement = conn.createStatement();
            // Geting the items
            ResultSet result = statement.executeQuery("SELECT * FROM Item "
                                                    + "WHERE Item.ItemID = " + itemId);
                
            result.first();
            // Somethings in it
            if (result.getRow() != 0) 
            {                             
                  
                DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
                DocumentBuilder b          = fac.newDocumentBuilder();
                org.w3c.dom.Document doc   = b.newDocument();
                // root element
                Element root = doc.createElement("Item");
                root.setAttribute("ItemID", itemId);
                doc.appendChild(root);
                Element element_name = doc.createElement("Name");
                element_name.appendChild(doc.createTextNode(result.getString("Name"))); // Need to replace special
                root.appendChild(element_name);
                	
                // Build Category Elements
                // Get the Categories
                Statement catstatement = conn.createStatement();
                ResultSet catresult = catstatement.executeQuery("SELECT Category "
                                                              + "FROM ItemCategory "
                                                              + "WHERE ItemCategory.ItemID = " + itemId + " ");
                Element category_element;
                while (catresult.next()) {
                    category_element = doc.createElement("Category");
                    category_element.appendChild(doc.createTextNode(catresult.getString("Category"))); //replace spical
                    root.appendChild(category_element);
                }
                catresult.close();
                catstatement.close();
                
                // Build Item price elements
                Element currently_element = doc.createElement("Currently");
                currently_element.appendChild(doc.createTextNode("$" + result.getString("Currently")));
                root.appendChild(currently_element);
    
                if (!result.getString("BuyPrice").equalsIgnoreCase("0.00") ) 
                {
                	System.out.println(result.getString("BuyPrice"));
                    Element buyprice_element = doc.createElement("Buy_Price");
                    buyprice_element.appendChild(doc.createTextNode("$" + result.getString("BuyPrice")));
                    root.appendChild(buyprice_element);
                }
                Element start_element = doc.createElement("First_Bid");
                start_element.appendChild(doc.createTextNode("$" + result.getString("FirstBid")));
                root.appendChild(start_element);

                Statement bidNumStmt = conn.createStatement();
                ResultSet bidNumResult = bidNumStmt.executeQuery("SELECT * "
                                                              + "FROM Bid "
                                                              + "WHERE ItemID = " + itemId + " ");
                
                Element numberbids_elements = doc.createElement("Number_of_Bids");
                bidNumResult.last();
                int temp = bidNumResult.getRow();
                numberbids_elements.appendChild(doc.createTextNode(""  + temp));              
                root.appendChild(numberbids_elements);
                
                // Build Bid Elements
                Statement bidstatement = conn.createStatement();
                ResultSet bidresult = bidstatement.executeQuery("SELECT * " 
                                                              + "FROM Bid, User "  
                                                              + "WHERE Bid.ItemId = " + itemId + " "
                                                              + "AND Bid.UserID = User.UserID");
                Element bids_element = doc.createElement("Bids");
                while (bidresult.next()) {
                    try {
                    	//System.out.println("1");
                    	//System.out.println(bidresult.getString("UserID"));
                        Element bid_element = doc.createElement("Bid");
                        Element bidder_element = doc.createElement("Bidder");
                        bidder_element.setAttribute("UserID", bidresult.getString("UserID"));
                        bidder_element.setAttribute("Rating", bidresult.getString("Rating"));
                        // Add Location and Country elements if they aren't NULL
                        if (!bidresult.getString("Location").equals("")) {
                            Element location_element = doc.createElement("Location");
                            location_element.appendChild(doc.createTextNode(bidresult.getString("Location")));
                            bidder_element.appendChild(location_element);
                        }
                        if (!bidresult.getString("Country").equals("")) {
                            Element country_element = doc.createElement("Country");
                            country_element.appendChild(doc.createTextNode(bidresult.getString("Country")));
                            bidder_element.appendChild(country_element);
                        }
                        bid_element.appendChild(bidder_element);
                        root.appendChild(bid_element);
                    }
                    catch(Exception e)
                    {
                    	System.out.println(e.getMessage());
                    }
                }
                
                Statement locStmt = conn.createStatement();
                ResultSet locResult = locStmt.executeQuery("SELECT * "
                                                          + "FROM User,Item "
                                                          + "WHERE User.UserID = Item.UserID " 
                                                          +  "AND Item.ItemID = " + itemId);
                
                locResult.next();
                Element locationElement = doc.createElement("Location");
                locationElement.appendChild(doc.createTextNode(locResult.getString("Location")));
                root.appendChild(locationElement);

                
                Element countryElement = doc.createElement("Country");
                countryElement.appendChild(doc.createTextNode(locResult.getString("Country")));
                root.appendChild(countryElement);
                
                Element startedElement = doc.createElement("Started");
                String str = result.getString("Started");
                //System.out.println(str);
                String s = timestamp(str);
                startedElement.appendChild(doc.createTextNode(s));
                root.appendChild(startedElement);
                
                Element endsElement = doc.createElement("Ends");
                String str2 = result.getString("Ends");
                //System.out.println(str);
                String s2 = timestamp(str2);

                endsElement.appendChild(doc.createTextNode(s2));
                root.appendChild(endsElement);
                
                Element sellerElement = doc.createElement("Seller");
                sellerElement.setAttribute("UserID", locResult.getString("UserID"));
                sellerElement.setAttribute("Rating", locResult.getString("Rating"));
                root.appendChild(sellerElement);
                
                Element descElement = doc.createElement("Description");
                descElement.appendChild(doc.createTextNode(result.getString("Description")));
                root.appendChild(descElement);
                
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                xmlstore = writer.getBuffer().toString();
            }
            return xmlstore;

         }
        catch(Exception e)
        {
			System.out.println(e.getMessage());
			return "";
        }	
   }
	
	public String echo(String message) {
        return message;
	}

	private String getQuery(SearchConstraint[] constraints)
	{
		ArrayList<String> whereClauses = new ArrayList<String>();
		boolean bidJoin = false;
		
		for(SearchConstraint c: constraints)
		{
			if(c.getFieldName().equals(FieldName.SellerId))
			{
				whereClauses.add("UserID = '" + c.getValue() + "'");
			}
			else if(c.getFieldName().equals(FieldName.BuyPrice))
			{
				whereClauses.add("BuyPrice = '" + c.getValue() + "'");
			}
			else if(c.getFieldName().equals(FieldName.EndTime))
			{
				whereClauses.add("Ends = \"" + convertDate(c.getValue()) + "\"");
			}
			else if(c.getFieldName().equals(FieldName.BidderId))
			{
				bidJoin = true;
				whereClauses.add("Bid.UserID = '" + c.getValue() + "'");
			}
		}
		
		String q = new String();
		if(whereClauses.size() != 0)
		{
			q += "SELECT ItemID, Name FROM Item ";
			if(bidJoin)
			{
				q += "INNER JOIN Bid ON Item.ItemID = Bid.ItemID ";
			}
			q += "WHERE " + whereClauses.get(0) + " ";
			for(int  i = 1; i < whereClauses.size(); i++)
			{
				q += "AND " + whereClauses.get(i) + " ";
			}
		}
		return q;
	}
	
    private static String convertDate(String date)
    {
            SimpleDateFormat format_in = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
            
            SimpleDateFormat format_out = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
           
            StringBuffer buffer = new StringBuffer();
               
            try
            {
                    Date parsedDate = format_in.parse(date);
            
                    return "" + format_out.format(parsedDate);
            }
            catch(Exception pe) {
                System.err.println("Parse error");
                return "Parse error";
            }
    }
    
    private static String timestamp(String date)
    {
            SimpleDateFormat format_in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            SimpleDateFormat format_out = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
           
            StringBuffer buffer = new StringBuffer();
               
            try
            {
                    Date parsedDate = format_in.parse(date);
            
                    return "" + format_out.format(parsedDate);
            }
            catch(Exception pe) {
                System.err.println("Parse error");
                return "Parse error";
            }
    }

}
