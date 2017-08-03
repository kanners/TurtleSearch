package midatlandroid.final_project;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ListingResults {

    // Class for the items found in each product
    public static class ProductListing {
        public String name;
        public double price;
        public String retailer;
        public String url;

        public ProductListing() {
            name = "";
            price = 0.0;
            retailer = "";
            url = "";
        }

        public String toString() {
            return String.format("Retailer:%s Price:%f\nProduct:\"%s\"\n%s\n", retailer, price, name, url);
        }
    }

    
    protected ArrayList<ProductListing> products;

    public ListingResults() {
      products = new ArrayList(0);
    }

    public ArrayList<ProductListing> getResults(String inString) {
        /*
        Ignore, only for console testing

        // Obtain user input for query
        System.out.print("Enter your product search: ");
        Scanner input = new Scanner(System.in);
        String inString = input.nextLine().toString().trim();
        */

        String[] portions = inString.split(" ");
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
            Gets 10 or less items from ebay
         */
        products.addAll(getEbayProducts(query));

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

    private static String getProductIdElement(String item) {
        String[] elemSplit = item.split("<ProductID type=\"Reference\">");
        // If the tag is missing, skip it
        if (elemSplit.length == 1) {
            return "XML_TAG_NOT_FOUND";
        } else {
            return elemSplit[1].split("</ProductID>")[0];
        }
    }

    private static String getCurrentPriceElement(String item) {
        String[] elemSplit = item.split("<ConvertedCurrentPrice currencyID=\"USD\">");
        // If the tag is missing, skip it
        if (elemSplit.length == 1) {
            return "XML_TAG_NOT_FOUND";
        } else {
            return elemSplit[1].split("</ConvertedCurrentPrice>")[0];
        }
    }

    private static ArrayList<ProductListing> getWalmartProducts(String query) {
        ArrayList<ProductListing> amazon = new ArrayList(0);
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
                String item = items[itemIndex];

                prod.name = getElement(item,"name");
                prod.retailer = "Walmart";
                prod.price = Double.parseDouble(getElement(item, "salePrice"));
                prod.url = getElement(item, "productUrl");

                amazon.add(prod);
            }

        } catch (MalformedURLException e) {
            //System.out.print("walmart: Invalid search query");
        } catch (IOException e) {
            //System.out.print("walmart: Failed to open url");
        }
        return amazon;
    }

    private static ArrayList<ProductListing> getEbayProducts(String query) {
        ArrayList<ProductListing> ebay = new ArrayList<ProductListing>();
        String result;
        int itemCount = 10;
        String ebURL = "http://open.api.ebay.com/shopping?callname=FindProducts&responseencoding=XML" +
                "&appid=darrienk-TurtleSe-PRD-38e2d25a0-c44dbc5e&siteid=0&version=967&QueryKeywords=" + query +
                "&AvailableItemsOnly=true&MaxEntries=" + itemCount;
        try {
            URL url = new URL(ebURL);
            HttpURLConnection xml = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(xml.getInputStream());
            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String read;

            while ((read = br.readLine()) != null) {
                sb.append(read);
            }
            br.close();

            // xml for ebay's products
            result = sb.toString();

            // parsing xml
            String[] items = result.split("<Product>"); // do not include first for the parse
            if (itemCount > items.length) {
                itemCount = items.length - 1;
            }
            for (int itemIndex = 1; itemIndex <= itemCount; itemIndex++) {
                ProductListing prod = new ProductListing();
                String item = items[itemIndex];

                String productId = getProductIdElement(item), currentPrice;
                prod.name = getElement(item,"Title");
                prod.retailer = "Ebay";

                // Product Id to Item Id
                String itemIdXml = ebayProductIdToItemId(productId);

                String itemId = getElement(itemIdXml, "itemId");

                if (itemId.equals("XML_TAG_NOT_FOUND")) {
                    continue;
                }

                String itemContentsXml = ebayGetItemById(itemId);

                currentPrice = getCurrentPriceElement(itemContentsXml);

                if (currentPrice.equals("XML_TAG_NOT_FOUND")) {
                    prod.price = -1.0;
                } else {
                    prod.price = Double.parseDouble(currentPrice);
                }

                prod.url = "https://www.ebay.com/itm/" + itemId;

                ebay.add(prod);
            }

        } catch (MalformedURLException e) {
            System.out.print("walmart: Invalid search query");
        } catch (IOException e) {
            System.out.print("walmart: Failed to open url");
        }

        return ebay;
    }

    private static String ebayProductIdToItemId(String pid) {
        String ret = "";
        String pidToItemId = "http://svcs.ebay.com/services/search/FindingService/v1?" +
                "OPERATION-NAME=findItemsByProduct&" +
                "SERVICE-VERSION=1.0.0&" +
                "SECURITY-APPNAME=darrienk-TurtleSe-PRD-38e2d25a0-c44dbc5e&" +
                "RESPONSE-DATA-FORMAT=XML&" +
                "REST-PAYLOAD&" +
                "paginationInput.entriesPerPage=2&" +
                "productId.@type=ReferenceID&" +
                "productId=" + pid;

        try {
            URL url = new URL(pidToItemId);
            HttpURLConnection xml = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(xml.getInputStream());
            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String read;

            while ((read = br.readLine()) != null) {
                sb.append(read);
            }
            br.close();

            ret = sb.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static String ebayGetItemById(String itemId) {
        String ret = "";
        String pidToItemId = "http://open.api.ebay.com/shopping?" +
                "callname=GetSingleItem&" +
                "responseencoding=XML&" +
                "appid=darrienk-TurtleSe-PRD-38e2d25a0-c44dbc5e&" +
                "siteid=0&" +
                "version=967&" +
                "IncludeSelector=Description,ItemSpecifics,ShippingCosts&" +
                "ItemID=" + itemId;

        try {
            URL url = new URL(pidToItemId);
            HttpURLConnection xml = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(xml.getInputStream());
            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String read;

            while ((read = br.readLine()) != null) {
                sb.append(read);
            }
            br.close();

            ret = sb.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }
}