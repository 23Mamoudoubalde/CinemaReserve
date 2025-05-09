//MXB220041 - Mamoudou Balde
import java.util.*;
public class Customer {
    //Customer's username for login
    private String username;
    //Customer's password for authentication
    private String password;
    //List of all orders made by this customer
    private List<Order> orders;
    
    //Constructor to create a new customer with username and password
    //Initializes the orders list as empty
    public Customer(String username, String password) {
        this.username = username;
        this.password = password;
        this.orders = new ArrayList<>();
    }
    
    //Returns the customer's username for identification
    public String getUsername() {
        return username;
    }
    
    //Returns the customer's password for authentication
    public String getPassword() {
        return password;
    }
    
    //Returns the list of all orders made by this customer
    public List<Order> getOrders() {
        return orders;
    }
    
    //Creates a new order and adds it to the customer's order list
    //This method is called when a customer successfully reserves seats
    public void addOrder(int auditoriumNumber, int row, int startSeat, int totalTickets, 
                       int adultTickets, int childTickets, int seniorTickets) {
        Order newOrder = new Order(auditoriumNumber, row, startSeat, 
                                adultTickets, childTickets, seniorTickets);
        orders.add(newOrder);
    }
    
    //Removes a specific order from the customer's order list
    //This is used when an order is cancelled completely
    public void removeOrder(Order order) {
        orders.remove(order);
    }
    
    //Order class represents a single ticket reservation for an auditorium
    //It tracks seat locations, ticket quantities, and allows for modifications
    public static class Order {
        //Auditorium number where the seats are reserved (1-3)
        private int auditoriumNumber;
        //Row number where seats are located
        private int row;
        //Starting seat index in the row (0-based)
        private int startSeat;
        //Number of adult tickets in this order
        private int adultTickets;
        //Number of child tickets in this order
        private int childTickets;
        //Number of senior tickets in this order
        private int seniorTickets;
        //List of additional seat groups added through order updates
        private List<int[]> additionalSeats;
        //Original total ticket count when order was first created
        private int originalTicketCount;
        
        //Constructor creates a new order with initial seat reservation and ticket counts
        public Order(int auditoriumNumber, int row, int startSeat, 
                   int adultTickets, int childTickets, int seniorTickets) {
            this.auditoriumNumber = auditoriumNumber;
            this.row = row;
            this.startSeat = startSeat;
            this.adultTickets = adultTickets;
            this.childTickets = childTickets;
            this.seniorTickets = seniorTickets;
            this.additionalSeats = new ArrayList<>();
            //Store the original ticket count for tracking changes
            this.originalTicketCount = adultTickets + childTickets + seniorTickets;
        }
        
        //Returns the original number of tickets when the order was first created
        public int getOriginalTicketCount() {
            return originalTicketCount;
        }
        
        //Returns the auditorium number (1-3)
        public int getAuditoriumNumber() {
            return auditoriumNumber;
        }
        
        //Returns the row number where seats are located
        public int getRow() {
            return row;
        }
        
        //Returns the starting seat index (0-based)
        public int getStartSeat() {
            return startSeat;
        }
        
        //Returns current count of adult tickets in this order
        public int getAdultTickets() {
            return adultTickets;
        }
        
        //Returns current count of child tickets in this order
        public int getChildTickets() {
            return childTickets;
        }
        
        //Returns current count of senior tickets in this order
        public int getSeniorTickets() {
            return seniorTickets;
        }
        
        //Calculates and returns the current total number of tickets
        //This may differ from originalTicketCount due to updates
        public int getTotalTickets() {
            return adultTickets + childTickets + seniorTickets;
        }
        
        //Returns the list of additional seat groups added through order updates
        public List<int[]> getAdditionalSeats() {
            return additionalSeats;
        }
        
        //Adds additional tickets to the existing order
        //Updates ticket counts and stores additional seat information
        public void addTickets(int row, int startSeat, int totalTickets, 
                             int adult, int child, int senior) {
            additionalSeats.add(new int[]{row, startSeat, totalTickets});
            adultTickets += adult;
            childTickets += child;
            seniorTickets += senior;
        }
        
        //Removes one adult ticket from the order if any exist
        public void removeAdultTicket() {
            if (adultTickets > 0) {
                adultTickets--;
            }
        }
        
        //Removes one child ticket from the order if any exist
        public void removeChildTicket() {
            if (childTickets > 0) {
                childTickets--;
            }
        }
        
        //Removes one senior ticket from the order if any exist
        public void removeSeniorTicket() {
            if (seniorTickets > 0) {
                seniorTickets--;
            }
        }
    }
}