
import java.io.*;
import java.util.*;

public class Auditorium {
    //Reference to the first node (top-left corner) of the auditorium
    private Node first;
    //Total number of rows in the auditorium
    private int numRows;
    //Total number of seats per row
    private int seatsPerRow;
    //Unique identifier for this auditorium
    private int auditoriumNumber;
    
    //Constructor loads auditorium layout from file
    public Auditorium(String filename, int auditoriumNumber) throws FileNotFoundException {
        this.auditoriumNumber = auditoriumNumber;
        loadFromFile(filename);
    }
    
    //Reads the auditorium layout from a file and creates the 2D linked list structure
    private void loadFromFile(String filename) throws FileNotFoundException {
        Scanner fileScanner = new Scanner(new File(filename));
        List<String> lines = new ArrayList<>();
        
        //Read all lines from the file to get the full layout
        while (fileScanner.hasNextLine()) {
            lines.add(fileScanner.nextLine());
        }
        
        fileScanner.close();
        
        //Set dimensions based on file content
        numRows = lines.size();
        if (numRows > 0) {
            seatsPerRow = lines.get(0).length();
        }
        
        //Build the 2D linked list structure
        first = null;
        Node previousRow = null;
        
        //Process each row from the file
        for (int i = 0; i < numRows; i++) {
            String rowData = lines.get(i);
            Node rowHead = null;
            Node previousSeat = null;
            
            //Process each seat in the row
            for (int j = 0; j < seatsPerRow; j++) {
                char seatType = rowData.charAt(j);
                //Create seat with 1-indexed row and letter-indexed column
                Seat seat = new Seat(i + 1, (char)('A' + j), seatType != '.');
                Node newNode = new Node(seat);
                
                //Set ticket type if seat is already reserved
                if (seatType != '.') {
                    seat.setTicketType(seatType);
                }
                
                //Link seat to previous seat horizontally
                if (rowHead == null) {
                    rowHead = newNode;
                }
                
                if (previousSeat != null) {
                    previousSeat.setRight(newNode);
                }
                
                previousSeat = newNode;
            }
            
            //Set the first node if this is the first row
            if (first == null) {
                first = rowHead;
            }
            
            //Link row to previous row vertically
            if (previousRow != null) {
                Node current = previousRow;
                Node currentNewRow = rowHead;
                
                while (current != null && currentNewRow != null) {
                    current.setDown(currentNewRow);
                    current = current.getRight();
                    currentNewRow = currentNewRow.getRight();
                }
            }
            
            previousRow = rowHead;
        }
    }
    
    //Displays the current state of the auditorium
    //Shows column letters at top and row numbers on left
    public void display() {
        //Print column headers (seat letters)
        System.out.print(" ");
        for (int i = 0; i < seatsPerRow; i++) {
            System.out.print((char)('A' + i));
        }
        System.out.println();
        
        //Print each row with row number
        Node currentRow = first;
        int rowNumber = 1;
        
        while (currentRow != null) {
            System.out.print(rowNumber + " ");
            
            //Print each seat in the row (# for reserved, . for available)
            Node currentSeat = currentRow;
            while (currentSeat != null) {
                System.out.print(currentSeat.getSeat().isReserved() ? '#' : '.');
                currentSeat = currentSeat.getRight();
            }
            
            System.out.println();
            currentRow = currentRow.getDown();
            rowNumber++;
        }
    }
    
    //Returns the total number of rows in the auditorium
    public int getNumRows() {
        return numRows;
    }
    
    //Returns the number of seats per row
    public int getSeatsPerRow() {
        return seatsPerRow;
    }
    
    //Returns the unique identifier for this auditorium
    public int getAuditoriumNumber() {
        return auditoriumNumber;
    }
    
    //Checks if a range of consecutive seats is available for reservation
    public boolean checkAvailability(int row, int startSeat, int numberOfSeats) {
        //Validate input parameters
        if (row < 1 || row > numRows || startSeat < 0 || startSeat + numberOfSeats > seatsPerRow) {
            return false;
        }
        
        //Navigate to the specified row
        Node rowNode = first;
        for (int i = 1; i < row; i++) {
            rowNode = rowNode.getDown();
        }
        
        //Navigate to the starting seat in the row
        Node seatNode = rowNode;
        for (int i = 0; i < startSeat; i++) {
            seatNode = seatNode.getRight();
        }
        
        //Check if all consecutive seats are available
        for (int i = 0; i < numberOfSeats; i++) {
            if (seatNode == null || seatNode.getSeat().isReserved()) {
                return false;
            }
            seatNode = seatNode.getRight();
        }
        
        return true;
    }
    
    //Reserves a specific seat with a ticket type
    public void reserveSeat(int row, int seat, char ticketType) {
        //Validate input parameters
        if (row < 1 || row > numRows || seat < 0 || seat >= seatsPerRow) {
            return;
        }
        
        //Navigate to the specified row
        Node rowNode = first;
        for (int i = 1; i < row; i++) {
            rowNode = rowNode.getDown();
        }
        
        //Navigate to the specified seat
        Node seatNode = rowNode;
        for (int i = 0; i < seat; i++) {
            seatNode = seatNode.getRight();
        }
        
        //Reserve the seat and set its ticket type
        seatNode.getSeat().reserve();
        seatNode.getSeat().setTicketType(ticketType);
    }
    
    //Unreserves a specific seat, making it available again
    public void unreserveSeat(int row, int seat) {
        //Validate input parameters
        if (row < 1 || row > numRows || seat < 0 || seat >= seatsPerRow) {
            return;
        }
        
        //Navigate to the specified row
        Node rowNode = first;
        for (int i = 1; i < row; i++) {
            rowNode = rowNode.getDown();
        }
        
        //Navigate to the specified seat
        Node seatNode = rowNode;
        for (int i = 0; i < seat; i++) {
            seatNode = seatNode.getRight();
        }
        
        //Unreserve the seat
        seatNode.getSeat().unreserve();
    }
    
    //Gets the ticket type of a reserved seat
    //Returns '.' if seat is not reserved
    public char getSeatType(int row, int seat) {
        //Return default if invalid parameters
        if (row < 1 || row > numRows || seat < 0 || seat >= seatsPerRow) {
            return '.';
        }
        
        //Navigate to the specified row
        Node rowNode = first;
        for (int i = 1; i < row; i++) {
            rowNode = rowNode.getDown();
        }
        
        //Navigate to the specified seat
        Node seatNode = rowNode;
        for (int i = 0; i < seat; i++) {
            seatNode = seatNode.getRight();
        }
        
        //Return the ticket type
        return seatNode.getSeat().getTicketType();
    }
    
    //Finds the best available consecutive seats closest to the center
    public int[] findBestAvailable(int numberOfSeats) {
        //Validate number of seats requested
        if (numberOfSeats <= 0 || numberOfSeats > seatsPerRow) {
            return null;
        }
        
        //Calculate center coordinates of the auditorium
        double centerRow = (numRows + 1) / 2.0;
        double centerSeat = (seatsPerRow + 1) / 2.0;
        
        //Track the best seats found so far
        double bestDistance = Double.MAX_VALUE;
        int[] bestSeats = null;
        
        //Search through each row
        Node rowNode = first;
        for (int r = 1; r <= numRows; r++) {
            //Check each possible starting position in the row
            for (int s = 0; s <= seatsPerRow - numberOfSeats; s++) {
                boolean available = true;
                
                //Check if enough consecutive seats are available
                Node seatNode = rowNode;
                //Navigate to starting seat position
                for (int i = 0; i < s; i++) {
                    if (seatNode == null) {
                        available = false;
                        break;
                    }
                    seatNode = seatNode.getRight();
                }
                
                //Verify consecutive seats are available
                Node checkNode = seatNode;
                for (int i = 0; i < numberOfSeats; i++) {
                    if (checkNode == null || checkNode.getSeat().isReserved()) {
                        available = false;
                        break;
                    }
                    checkNode = checkNode.getRight();
                }
                
                if (available) {
                    //Calculate center position of this seat selection
                    double selectionCenterRow = r;
                    double selectionCenterSeat = s + (numberOfSeats - 1) / 2.0 + 1;
                    
                    //Calculate distance from auditorium center
                    double distance = Math.sqrt(
                        Math.pow(selectionCenterRow - centerRow, 2) +
                        Math.pow(selectionCenterSeat - centerSeat, 2)
                    );
                    
                    //Update best seats if closer to center or
                    //if equal distance but better positioned
                    if (bestSeats == null || 
                        distance < bestDistance || 
                        (distance == bestDistance && r < bestSeats[0]) ||
                        (distance == bestDistance && r == bestSeats[0] && s < bestSeats[1])) {
                        bestDistance = distance;
                        bestSeats = new int[]{r, s};
                    }
                }
            }
            
            rowNode = rowNode.getDown();
        }
        
        return bestSeats;
    }
    
    //Counts the number of available (unreserved) seats
    public int getOpenSeatCount() {
        int count = 0;
        Node rowNode = first;
        
        while (rowNode != null) {
            Node seatNode = rowNode;
            while (seatNode != null) {
                if (!seatNode.getSeat().isReserved()) {
                    count++;
                }
                seatNode = seatNode.getRight();
            }
            rowNode = rowNode.getDown();
        }
        
        return count;
    }
    
    //Counts the number of reserved seats
    public int getReservedSeatCount() {
        int count = 0;
        Node rowNode = first;
        
        while (rowNode != null) {
            Node seatNode = rowNode;
            while (seatNode != null) {
                if (seatNode.getSeat().isReserved()) {
                    count++;
                }
                seatNode = seatNode.getRight();
            }
            rowNode = rowNode.getDown();
        }
        
        return count;
    }
    
    //Counts seats with a specific ticket type (A=Adult, C=Child, S=Senior)
    public int getTicketTypeCount(char ticketType) {
        int count = 0;
        Node rowNode = first;
        
        while (rowNode != null) {
            Node seatNode = rowNode;
            while (seatNode != null) {
                if (seatNode.getSeat().isReserved() && 
                    seatNode.getSeat().getTicketType() == ticketType) {
                    count++;
                }
                seatNode = seatNode.getRight();
            }
            rowNode = rowNode.getDown();
        }
        
        return count;
    }
    
    //Saves the current auditorium state to a file
    //Reserved seats are saved with their ticket type, unreserved seats as '.'
    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new File(filename))) {
            Node rowNode = first;
            
            while (rowNode != null) {
                Node seatNode = rowNode;
                StringBuilder rowText = new StringBuilder();
                
                //Build string representation of the row
                while (seatNode != null) {
                    if (seatNode.getSeat().isReserved()) {
                        rowText.append(seatNode.getSeat().getTicketType());
                    } else {
                        rowText.append('.');
                    }
                    seatNode = seatNode.getRight();
                }
                
                //Write row to file
                writer.println(rowText.toString());
                rowNode = rowNode.getDown();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error saving auditorium to file: " + e.getMessage());
        }
    }
    
}