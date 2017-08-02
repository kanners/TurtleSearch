public class ProductListing {
    public String name;
    public double price;
    public double rating;
    public String retailer;
    public String url;

    public ProductListing() {
        name = "";
        price = 0.0;
        rating = 0.0;
        retailer = "";
        url = "";
    }

    public ProductListing(String na, String ret, double pr, double rat, String u) {
        name = na;
        retailer = ret;
        price = pr;
        rating = rat;
        url = u;
    }

    public String toString() {
        return String.format("Retailer:%s Price:%f Rating:%f \nProduct:\"%s\"\n%s\n", retailer, price, rating, name, url);
    }
}
