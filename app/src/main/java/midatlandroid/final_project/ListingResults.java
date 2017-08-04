package midatlandroid.final_project;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ListingResults {

    public class GetData extends AsyncTask<String, Void, String> {
        public GetData() {

        }
        @Override
        public String doInBackground(String... params) {
            String result = "";
            HttpURLConnection xml = null;

            try {
                URL url = new URL(params[0]);
                xml = (HttpURLConnection) url.openConnection();

                int responseCode = xml.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = new BufferedInputStream(xml.getInputStream());
                    StringBuffer sb = new StringBuffer();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String read;

                    while ((read = br.readLine()) != null) {
                        sb.append(read);
                    }
                    br.close();
                    result = sb.toString();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                xml.disconnect();
            }

            return result;
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
    private String getElement(String item, String tag) {
        String[] elemSplit = item.split("<" + tag + ">");
        // If the tag is missing, skip it
        if (elemSplit.length == 1) {
            return "XML_TAG_NOT_FOUND";
        } else {
            return elemSplit[1].split("</" + tag + ">")[0];
        }
    }

    private String getProductIdElement(String item) {
        String[] elemSplit = item.split("<ProductID type=\"Reference\">");
        // If the tag is missing, skip it
        if (elemSplit.length == 1) {
            return "XML_TAG_NOT_FOUND";
        } else {
            return elemSplit[1].split("</ProductID>")[0];
        }
    }

    private String getCurrentPriceElement(String item) {
        String[] elemSplit = item.split("<ConvertedCurrentPrice currencyID=\"USD\">");
        // If the tag is missing, skip it
        if (elemSplit.length == 1) {
            return "XML_TAG_NOT_FOUND";
        } else {
            return elemSplit[1].split("</ConvertedCurrentPrice>")[0];
        }
    }

    private ArrayList<ProductListing> getWalmartProducts(String query) {
        ArrayList<ProductListing> amazon = new ArrayList(0);
        String wmURL = "http://api.walmartlabs.com/v1/search?query=" + query + "&format=xml&apiKey=5smuvzrv9u6dz82y6hd6jgve";
        GetData gd = new GetData();
        // xml for walmart's products
        String result = gd.doInBackground(wmURL);

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

        return amazon;
    }

    private ArrayList<ProductListing> getEbayProducts(String query) {
        ArrayList<ProductListing> ebay = new ArrayList<ProductListing>();
        int itemCount = 10;
        String ebURL = "http://open.api.ebay.com/shopping?callname=FindProducts&responseencoding=XML" +
                "&appid=darrienk-TurtleSe-PRD-38e2d25a0-c44dbc5e&siteid=0&version=967&QueryKeywords=" + query +
                "&AvailableItemsOnly=true&MaxEntries=" + itemCount;

        GetData gd = new GetData();
        String result = gd.doInBackground(ebURL);

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


        return ebay;
    }

    private String ebayProductIdToItemId(String pid) {
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

        GetData gd = new GetData();

        return gd.doInBackground(pidToItemId);
    }

    private String ebayGetItemById(String itemId) {
        String ret = "";
        String pidToItemId = "http://open.api.ebay.com/shopping?" +
                "callname=GetSingleItem&" +
                "responseencoding=XML&" +
                "appid=darrienk-TurtleSe-PRD-38e2d25a0-c44dbc5e&" +
                "siteid=0&" +
                "version=967&" +
                "IncludeSelector=Description,ItemSpecifics,ShippingCosts&" +
                "ItemID=" + itemId;

        GetData gd = new GetData();

        return gd.doInBackground(pidToItemId);
    }
}