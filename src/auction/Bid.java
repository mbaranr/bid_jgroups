package auction;

import java.io.Serializable;

/*
 * Represents a bid in an auction.
 * Can be placed by both a buyer or seller.
 */
public class Bid implements Serializable {
    private final float price;
    private final String bidder;
    private final boolean isBuyer;

    public Bid(float price, String bidder, boolean isBuyer) {
        this.price = price;
        this.bidder = bidder;
        this.isBuyer = isBuyer;
    }

    public float getPrice() {
        return price;
    }

    public boolean isBuyer() {
        return isBuyer;
    }

    public String getBidder() {
        return bidder;
    }
}


