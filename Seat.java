
public class Seat {
    //Row number of the seat (1-indexed)
    private int row;
    //Seat letter (A, B, C, etc.) within the row
    private char seat;
    //Whether this seat is currently reserved
    private boolean reserved;
    //Type of ticket for reserved seat (A=Adult, C=Child, S=Senior)
    //Unreserved seats use '.' as default
    private char ticketType;
    
    //Constructor to create a new seat with location and initial reservation status
    public Seat(int row, char seat, boolean reserved) {
        this.row = row;
        this.seat = seat;
        this.reserved = reserved;
        //Default ticket type when seat is created
        this.ticketType = '.';
    }
    
    //Returns the row number of this seat
    public int getRow() {
        return row;
    }
    
    //Returns the seat letter (column identifier)
    public char getSeat() {
        return seat;
    }
    
    //Checks if this seat is currently reserved
    public boolean isReserved() {
        return reserved;
    }
    
    //Marks this seat as reserved
    public void reserve() {
        this.reserved = true;
    }
    
    //Unreserves this seat, making it available again
    //Also resets the ticket type to default (unreserved)
    public void unreserve() {
        this.reserved = false;
        this.ticketType = '.';
    }
    
    //Returns the ticket type for this seat
    //Returns '.' for unreserved seats
    public char getTicketType() {
        return ticketType;
    }
    
    //Sets the ticket type for this seat
    //Should only be called after reserving the seat
    public void setTicketType(char ticketType) {
        this.ticketType = ticketType;
    }
}