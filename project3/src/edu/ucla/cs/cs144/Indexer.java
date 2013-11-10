package edu.ucla.cs.cs144;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

public class Indexer {
    
    /** Creates a new instance of Indexer */
    ResultSet itemRS;
    ResultSet categoryRS;
    
    public Indexer() {
        
        itemRS = null;
        categoryRS = null;
    }

    
    public void rebuildIndexes() {

        Connection conn = null;

        // create a connection to the database to retrieve Items from MySQL
	try {
	    conn = DbManager.getConnection(true);
	} catch (SQLException ex) {
	    System.out.println(ex);
	}


	/*
	 * Add your code here to retrieve Items using the connection
	 * and add corresponding entries to your Lucene inverted indexes.
         *
         * You will have to use JDBC API to retrieve MySQL data from Java.
         * Read our tutorial on JDBC if you do not know how to use JDBC.
         *
         * You will also have to use Lucene IndexWriter and Document
         * classes to create an index and populate it with Items data.
         * Read our tutorial on Lucene as well if you don't know how.
         *
         * As part of this development, you may want to add 
         * new methods and create additional Java classes. 
         * If you create new classes, make sure that
         * the classes become part of "edu.ucla.cs.cs144" package
         * and place your class source files at src/edu/ucla/cs/cs144/.
	 * 
	 */

        try
        {
            Connection c = DbManager.getConnection(true);
            Statement s = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                            ResultSet.CONCUR_UPDATABLE);
            
            ResultSet itemRS = s.executeQuery("SELECT * FROM Item");
            
            String directory = System.getenv("LUCENE_INDEX") + "/basic";
            IndexWriter indexWriter = new IndexWriter(directory, new StandardAnalyzer(), true);
            
            while(itemRS.next())
            {
                //
                Document doc = new Document();
                                int id = itemRS.getInt("ItemID");
                String sid = "" + id;
                String categories = "";
                
                Statement sa = c.createStatement();

                ResultSet categoryRS = sa.executeQuery("SELECT * FROM ItemCategory where ItemID=" + id);
                
                while(categoryRS.next())
                {
                    categories += (categoryRS.getString("Category") + " ")        ;
                }
                
                String fullSearchable = itemRS.getString("Name") + " " + itemRS.getString("Description") + " "
                + categories;
                
                doc.add(new Field("ItemID", sid, Field.Store.YES, Field.Index.NO));
                doc.add(new Field("ItemName", itemRS.getString("Name"), Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("Description", itemRS.getString("Description"), Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("Category", categories, Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("content", fullSearchable, Field.Store.YES, Field.Index.TOKENIZED));
                
                indexWriter.addDocument(doc);
                categoryRS.close();
                 

            }
            indexWriter.close();
            itemRS.close();
            
        }
        
        catch (Exception e)
        {
            System.err.println("Error in Ajan's code rebuildIndexes");
            System.err.println(e.getMessage());
        }


        // close the database connection
	try {
	    conn.close();
	} catch (SQLException ex) {
	    System.out.println(ex);
	}
    }

    public static void main(String args[]) {
        Indexer idx = new Indexer();
        idx.rebuildIndexes();
    }
}
