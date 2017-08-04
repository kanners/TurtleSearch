package midatlandroid.final_project;

/**
 * Created by kennedyd3 on 8/3/2017.
 */
public class ProductListing {
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
