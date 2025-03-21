package auction;

import java.util.Comparator;
import java.util.LinkedList;

public class DoubleAuction extends Auction {
    
    private LinkedList<Bid> buyerBids;
    private LinkedList<Bid> sellerBids;

    public DoubleAuction(int auctionId, String auctioneer, AuctionItem item) {
        super(auctionId, auctioneer, item);
        buyerBids = new LinkedList<Bid>();
        sellerBids = new LinkedList<Bid>();
    }

    @Override
    public String closeAuction(String winner) throws Exception {
        isClosed = true;
        // sorting bids
        buyerBids.sort(Comparator.comparing(Bid::getPrice).reversed());
        sellerBids.sort(Comparator.comparing(Bid::getPrice));

        StringBuilder sb = new StringBuilder();
        sb.append("=====================================\n");
        sb.append("Auction Closed: Pairing Results\n");
        sb.append("=====================================\n");

        LinkedList<Bid> unmatchedBuyers = new LinkedList<>(buyerBids);
        LinkedList<Bid> unmatchedSellers = new LinkedList<>(sellerBids);
        LinkedList<String> successfulPairs = new LinkedList<>();

        // match sellers with the best possible buyer
        for (Bid seller : sellerBids) {
            Bid bestMatch = null;
            for (Bid buyer : unmatchedBuyers) {
                if (buyer.getPrice() >= seller.getPrice()) {
                    bestMatch = buyer;
                    break; // stop searching once we find a match
                }
            }
            // a successful match was found
            if (bestMatch != null) {
                float profit = bestMatch.getPrice() - seller.getPrice();
                successfulPairs.add(String.format(
                    "Seller: %s (Selling Price: %f)\n\tBuyer: %s (Bidding Price: %f)\n\tProfit: %f",
                    seller.getBidder(), seller.getPrice(), bestMatch.getBidder(), bestMatch.getPrice(), profit
                ));
                unmatchedBuyers.remove(bestMatch);
                unmatchedSellers.remove(seller);
            }
        }
        // successful pairs
        if (successfulPairs.isEmpty()) {
            sb.append("No successful transactions: No buyers willing to meet seller prices.\n");
        } else {
            int count = 1;
            for (String pair : successfulPairs) {
                sb.append(String.format("Pair %d:\n\t%s\n", count++, pair));
                sb.append("-------------------------------------\n");
            }
        }
        // unmatched buyers
        if (!unmatchedBuyers.isEmpty()) {
            sb.append("Unmatched Buyers:\n");
            for (Bid buyer : unmatchedBuyers) {
                sb.append(String.format("\t%s (Bid: %f)\n", buyer.getBidder(), buyer.getPrice()));
            }
            sb.append("-------------------------------------\n");
        }
        // unmatched sellers
        if (!unmatchedSellers.isEmpty()) {
            sb.append("Unmatched Sellers:\n");
            for (Bid seller : unmatchedSellers) {
                sb.append(String.format("\t%s (Asking Price: %f)\n", seller.getBidder(), seller.getPrice()));
            }
            sb.append("-------------------------------------\n");
        }
        sb.append("Auction has been closed successfully.");
        return sb.toString();
    }

    @Override
    public void placeBid(Bid bid) throws Exception {
        if (isClosed) { throw new Exception("This auction is closed!"); }
        if (bid.isBuyer()) { buyerBids.add(bid); }
        else { sellerBids.add(bid); } 
    }

    @Override
    public String getData() throws Exception {
        StringBuilder sb = new StringBuilder(); 
        sb.append("=====================================\n");
        sb.append("Auction ID: ").append(auctionId).append("\n");
        sb.append("Auction Type: Double").append("\n");
        sb.append("Opened by: ").append(auctioneer).append("\n");
        sb.append("Item: ").append(item.getItemTitle()).append("\n");
        sb.append("Description: ").append(item.getItemDescription()).append("\n");
        sb.append("-------------------------------------\n");
        // seller Bids
        if (sellerBids.size() > 0) {
            sb.append("Seller Bids:\n");
            for (Bid bid : sellerBids) {
                sb.append("-------------------------------------\n");
                sb.append("\tSelling Price: ").append(bid.getPrice()).append("\n");
                sb.append("\tSeller: ").append(bid.getBidder()).append("\n");
            }
            sb.append("-------------------------------------\n");
        } else { sb.append("No sellers registered.\n-------------------------------------\n"); }
        // buyer Bids
        if (buyerBids.size() > 0) {
            sb.append("Buyer Bids:\n");
            for (Bid bid : buyerBids) {
                sb.append("-------------------------------------\n");
                sb.append("\tBidding Price: ").append(bid.getPrice()).append("\n");
                sb.append("\tBidder: ").append(bid.getBidder()).append("\n");
            }
            sb.append("-------------------------------------\n");
        } else { sb.append("No buyers registered.\n-------------------------------------\n"); }
        sb.append("Auction Status: ").append(isClosed ? "Closed" : "Open").append("\n");
        sb.append("=====================================");
        return sb.toString();
    }
}