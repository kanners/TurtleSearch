import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ListingResults {
    ArrayList products;

    public ListingResults() {
      products = new ArrayList(0);
    }

    public static ArrayList<ProductListing> getResults(String inString) {
        /*
        Ignore, only for console testing

        // Obtain user input for query
        System.out.print("Enter your product search: ");
        Scanner input = new Scanner(System.in);
        String inString = input.nextLine().toString().trim();
        String[] portions = inString.split(" ");
        */


        // Convert input to url format
        String query = "";
        for (int index = 0; index < portions.length; index++) {
            query += portions[index];
            if (index < portions.length - 1) {
                query += "+";
            }
        }

        /*
            Walmart only has 10 items per query
         */
        products.addAll(getWalmartProducts(query));

        /*
            Some other api
         */
        //products.addAll(getAmazonProducts(query));



        /*

        for (int index = 0; index < products.size(); index++) {
            //System.out.println(products.get(index).toString());

            // TODO Add all information to a database

        }
        */
        return products;
    }

    /*
        Parse xml content from a line of xml based off of tag
     */
    private static String getElement(String item, String tag) {
        String[] elemSplit = item.split("<" + tag + ">");
        // If the tag is missing, skip it
        if (elemSplit.length == 1) {
            return "XML_TAG_NOT_FOUND";
        } else {
            return elemSplit[1].split("</" + tag + ">")[0];
        }
    }

    private static ArrayList<ProductListing> getWalmartProducts(String query) {
        ArrayList<ProductListing> products = new ArrayList(0);
        String result;
        String wmURL = "http://api.walmartlabs.com/v1/search?query=" + query + "&format=xml&apiKey=5smuvzrv9u6dz82y6hd6jgve";
        try {
            URL url = new URL(wmURL);
            HttpURLConnection xml = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(xml.getInputStream());
            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String read;

            while ((read = br.readLine()) != null) {
                sb.append(read);
            }
            br.close();

            // xml for walmart's products
            result = sb.toString();

            int itemCount = Integer.parseInt(getElement(result, "numItems"));

            // parsing xml
            String[] items = result.split("<item>"); // do not include first for the parse
            for (int itemIndex = 1; itemIndex <= itemCount; itemIndex++) {
                ProductListing prod = new ProductListing();
                String item = items[itemIndex], rating;

                prod.name = getElement(item,"name");
                prod.retailer = "Walmart";
                prod.price = Double.parseDouble(getElement(item, "salePrice"));
                if ((rating = getElement(item,"customerRating")).equals("XML_TAG_NOT_FOUND")) {
                    prod.rating = 0.0;
                } else {
                    prod.rating = Double.parseDouble(rating);
                }
                prod.url = getElement(item, "productUrl");

                products.add(prod);
            }

        } catch (MalformedURLException e) {
            System.out.print("walmart: Invalid search query");
        } catch (IOException e) {
            System.out.print("walmart: Failed to open url");
        }
        return products;
    }

    private static ArrayList<ProductListing> getAmazonProducts(String query) {

        return null;
    }
}
