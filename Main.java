
import java.io.*;
import java.util.*;

//Main class for the Theater Reservation System.
//Manages user authentication, auditorium seating, and ticket reservations.
public class Main {
    //Store user credentials
    private static Hashmap userMap = new Hashmap();
    //Three auditoriums for the theater
    private static Auditorium[] auditoriums = new Auditorium[3];
    //Input scanner for user input
    private static Scanner scanner = new Scanner(System.in);
    //Currently logged in user
    private static String currentUser = null;

    //Entry point of the application.
    //Initializes data and starts the login process.
    public static void main(String[] args) {
        //Load user credentials from file
        loadUserData();
        //Load auditorium seating arrangements
        loadAuditoriums();
        //Start login process
        loginPrompt();
    }

    //Loads user credentials from the userdb.dat file.
    //Each line contains username and password separated by spaces.
    private static void loadUserData() {
        try (Scanner fileScanner = new Scanner(new File("userdb.dat"))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(" ");
                if (parts.length >= 2) {
                    String username = parts[0];
                    String password = parts[1];
                    userMap.put(username, new Customer(username, password));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("User database file not found.");
            System.exit(1);
        }
    }

    //Loads auditorium seating arrangements from files A1.txt, A2.txt, and A3.txt.
    private static void loadAuditoriums() {
        for (int i = 1; i <= 3; i++) {
            try {
                auditoriums[i - 1] = new Auditorium("A" + i + ".txt", i);
            } catch (FileNotFoundException e) {
                System.out.println("Auditorium file A" + i + ".txt not found.");
                System.exit(1);
            }
        }
    }

    //Handles user login process.
    //Provides 3 attempts for password entry and routes to appropriate menu.
    private static void loginPrompt() {
        boolean loggedIn = false;
        
        while (!loggedIn && scanner.hasNextLine()) {
            System.out.print("Username: ");
            if (!scanner.hasNextLine()) return;
            String username = scanner.nextLine();
            
            if (userMap.containsKey(username)) {
                boolean validPassword = false;
                int attempts = 0;
                
                while (!validPassword && attempts < 3 && scanner.hasNextLine()) {
                    System.out.print("Password: ");
                    if (!scanner.hasNextLine()) return;
                    String password = scanner.nextLine();
                    
                    if (userMap.get(username).getPassword().equals(password)) {
                        currentUser = username;
                        validPassword = true;
                        loggedIn = true;
                        
                        //Route to appropriate menu based on user type
                        if (username.equals("admin")) {
                            adminMenu();
                        } else {
                            customerMenu();
                        }
                    } else {
                        System.out.println("Invalid password");
                        attempts++;
                    }
                }
            } else {
                System.out.println("Username not found");
            }
        }
    }

    //Displays and handles the customer menu options.
    private static void customerMenu() {
        boolean logout = false;
        
        while (!logout && scanner.hasNextLine()) {
            System.out.println("\n1. Reserve Seats");
            System.out.println("2. View Orders");
            System.out.println("3. Update Order");
            System.out.println("4. Display Receipt");
            System.out.println("5. Log Out");
            
            if (!scanner.hasNextLine()) return;
            int choice = getValidIntInput(1, 5);
            
            switch (choice) {
                case 1:
                    reserveSeats();
                    break;
                case 2:
                    viewOrders();
                    break;
                case 3:
                    updateOrder();
                    break;
                case 4:
                    displayReceipt();
                    break;
                case 5:
                    logout = true;
                    currentUser = null;
                    loginPrompt();
                    break;
            }
        }
    }

    //Displays and handles the admin menu options.
    private static void adminMenu() {
        boolean logout = false;
        
        while (!logout && scanner.hasNextLine()) {
            System.out.println("\n1. Print Report");
            System.out.println("2. Logout");
            System.out.println("3. Exit");
            
            if (!scanner.hasNextLine()) return;
            int choice = getValidIntInput(1, 3);
            
            switch (choice) {
                case 1:
                    printReport();
                    break;
                case 2:
                    logout = true;
                    currentUser = null;
                    loginPrompt();
                    break;
                case 3:
                    saveAuditoriums();
                    System.exit(0);
                    break;
            }
        }
    }

    //Validates integer input within a specified range.
    //Minimum valid value: min
    //Maximum valid value: max
    //Returns valid integer input
    private static int getValidIntInput(int min, int max) {
        int input = 0;
        boolean validInput = false;
        
        while (!validInput && scanner.hasNextLine()) {
            try {
                input = Integer.parseInt(scanner.nextLine());
                
                if (input >= min && input <= max) {
                    validInput = true;
                } else {
                    System.out.println("Invalid input");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            }
        }
        
        return input;
    }

    //Handles the seat reservation process.
    //Allows users to select auditorium, row, seat, and ticket quantities.
    private static void reserveSeats() {
        System.out.println("\n1. Auditorium 1");
        System.out.println("2. Auditorium 2");
        System.out.println("3. Auditorium 3");
        
        if (!scanner.hasNextLine()) return;
        int auditoriumChoice = getValidIntInput(1, 3);
        Auditorium selectedAuditorium = auditoriums[auditoriumChoice - 1];
        
        //Display current seating arrangement
        selectedAuditorium.display();
        
        //Get seat selection and ticket quantities
        if (!scanner.hasNextLine()) return;
        int rowNumber = getValidRowInput(selectedAuditorium);
        if (!scanner.hasNextLine()) return;
        char startingSeat = getValidSeatInput(selectedAuditorium);
        if (!scanner.hasNextLine()) return;
        int adultTickets = getValidTicketInput("adult");
        if (!scanner.hasNextLine()) return;
        int childTickets = getValidTicketInput("child");
        if (!scanner.hasNextLine()) return;
        int seniorTickets = getValidTicketInput("senior");
        
        int totalTickets = adultTickets + childTickets + seniorTickets;
        
        if (totalTickets == 0) {
            System.out.println("No tickets selected. Returning to main menu.");
            return;
        }
        
        int seatIndex = startingSeat - 'A';
        
        //Check if requested seats are available
        if (!selectedAuditorium.checkAvailability(rowNumber, seatIndex, totalTickets)) {
            //Find alternative available seats
            int[] bestSeats = selectedAuditorium.findBestAvailable(totalTickets);
            
            if (bestSeats != null) {
                int bestRow = bestSeats[0];
                int bestSeat = bestSeats[1];
                char bestSeatLetter = (char)('A' + bestSeat);
                char lastSeatLetter = (char)('A' + bestSeat + totalTickets - 1);
                
                System.out.println("The selected seats are not available.");
                System.out.println("Best available seats: " + bestRow + bestSeatLetter + "-" + bestRow + lastSeatLetter);
                System.out.print("Would you like these seats? (Y/N): ");
                
                if (scanner.hasNextLine()) {
                    String response = scanner.nextLine().toUpperCase();
                    
                    while (!response.equals("Y") && !response.equals("N") && scanner.hasNextLine()) {
                        System.out.println("Invalid input");
                        if (!scanner.hasNextLine()) return;
                        response = scanner.nextLine().toUpperCase();
                    }
                    
                    if (response.equals("Y")) {
                        reserveSeatsInAuditorium(selectedAuditorium, bestRow, bestSeat, 
                            adultTickets, childTickets, seniorTickets);
                        userMap.get(currentUser).addOrder(auditoriumChoice, bestRow, bestSeat, 
                            totalTickets, adultTickets, childTickets, seniorTickets);
                    } else {
                        System.out.println("Reservation canceled. Returning to main menu.");
                    }
                }
            } else {
                System.out.println("no seats available");
            }
        } else {
            //Reserve requested seats
            reserveSeatsInAuditorium(selectedAuditorium, rowNumber, seatIndex, 
                adultTickets, childTickets, seniorTickets);
            userMap.get(currentUser).addOrder(auditoriumChoice, rowNumber, seatIndex, 
                totalTickets, adultTickets, childTickets, seniorTickets);
        }
    }

    //Validates row number input.
    //auditorium: The auditorium to check against
    //Returns valid row number
    private static int getValidRowInput(Auditorium auditorium) {
        int rows = auditorium.getNumRows();
        int row = 0;
        boolean validInput = false;
        
        while (!validInput && scanner.hasNextLine()) {
            try {
                System.out.print("Enter Row: ");
                if (!scanner.hasNextLine()) return row;
                row = Integer.parseInt(scanner.nextLine());
                
                if (row >= 1 && row <= rows) {
                    validInput = true;
                } else {
                    System.out.println("Invalid input");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            }
        }
        
        return row;
    }

    //Validates seat letter input.
    //auditorium: The auditorium to check against
    //Returns valid seat letter
    private static char getValidSeatInput(Auditorium auditorium) {
        int seatsPerRow = auditorium.getSeatsPerRow();
        char seat = ' ';
        boolean validInput = false;
        
        while (!validInput && scanner.hasNextLine()) {
            System.out.print("Enter Seat: ");
            if (!scanner.hasNextLine()) return seat;
            String input = scanner.nextLine().toUpperCase();
            
            if (input.length() == 1) {
                seat = input.charAt(0);
                int seatIndex = seat - 'A';
                
                if (seatIndex >= 0 && seatIndex < seatsPerRow) {
                    validInput = true;
                } else {
                    System.out.println("Invalid input");
                }
            } else {
                System.out.println("Invalid input");
            }
        }
        
        return seat;
    }

    //Validates ticket quantity input.
    //ticketType: Type of ticket (adult, child, senior)
    //Returns valid number of tickets
    private static int getValidTicketInput(String ticketType) {
        int tickets = -1;
        boolean validInput = false;
        
        while (!validInput && scanner.hasNextLine()) {
            try {
                System.out.print("Enter number of " + ticketType + " tickets: ");
                if (!scanner.hasNextLine()) return tickets;
                tickets = Integer.parseInt(scanner.nextLine());
                
                if (tickets >= 0) {
                    validInput = true;
                } else {
                    System.out.println("Invalid input");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            }
        }
        
        return tickets;
    }

    //Reserves seats in the auditorium by type.
    //auditorium: The auditorium to reserve seats in
    //row: Row number
    //startSeat: Starting seat index
    //adultTickets: Number of adult tickets
    //childTickets: Number of child tickets
    //seniorTickets: Number of senior tickets
    private static void reserveSeatsInAuditorium(Auditorium auditorium, int row, int startSeat, 
        int adultTickets, int childTickets, int seniorTickets) {
        int currentSeat = startSeat;
        
        //Reserve adult seats
        for (int i = 0; i < adultTickets; i++) {
            auditorium.reserveSeat(row, currentSeat, 'A');
            currentSeat++;
        }
        
        //Reserve child seats
        for (int i = 0; i < childTickets; i++) {
            auditorium.reserveSeat(row, currentSeat, 'C');
            currentSeat++;
        }
        
        //Reserve senior seats
        for (int i = 0; i < seniorTickets; i++) {
            auditorium.reserveSeat(row, currentSeat, 'S');
            currentSeat++;
        }
    }

    //Displays all orders for the current customer.
    private static void viewOrders() {
        Customer customer = userMap.get(currentUser);
        List<Customer.Order> orders = customer.getOrders();
        
        if (orders.isEmpty()) {
            System.out.println("No orders");
            return;
        }
        
        for (Customer.Order order : orders) {
            displayOrder(order);
        }
    }

    //Displays details of a single order.
    //order: The order to display
    private static void displayOrder(Customer.Order order) {
        int auditoriumNumber = order.getAuditoriumNumber();
        
        List<String> seatList = generateUpdatedSeatList(order);
        
        System.out.println("Auditorium " + auditoriumNumber + ", " + String.join(",", seatList));
        System.out.println(order.getAdultTickets() + " adult, " + 
                         order.getChildTickets() + " child, " + 
                         order.getSeniorTickets() + " senior");
    }

    //Generates a list of currently reserved seats for an order.
    //Checks if seats are still reserved to handle partial cancellations.
    //order: The order to check
    //Returns list of seat identifiers
    private static List<String> generateUpdatedSeatList(Customer.Order order) {
        List<String> seatList = new ArrayList<>();
        int auditoriumNumber = order.getAuditoriumNumber();
        Auditorium auditorium = auditoriums[auditoriumNumber - 1];
        
        //Check original seat range
        int row = order.getRow();
        int startSeat = order.getStartSeat();
        int originalTicketCount = order.getOriginalTicketCount();
        
        for (int i = 0; i < originalTicketCount; i++) {
            int seatIndex = startSeat + i;
            char seatLetter = (char)('A' + seatIndex);
            
            //Add seat if still reserved
            if (auditorium.getSeatType(row, seatIndex) != '.') {
                seatList.add(row + "" + seatLetter);
            }
        }
        
        //Check additional seats (from updates)
        for (int[] additionalSeat : order.getAdditionalSeats()) {
            int additionalRow = additionalSeat[0];
            int additionalStartSeat = additionalSeat[1];
            int additionalTotal = additionalSeat[2];
            
            for (int i = 0; i < additionalTotal; i++) {
                int seatIndex = additionalStartSeat + i;
                char seatLetter = (char)('A' + seatIndex);
                
                //Add seat if still reserved
                if (auditorium.getSeatType(additionalRow, seatIndex) != '.') {
                    seatList.add(additionalRow + "" + seatLetter);
                }
            }
        }
        
        //Sort seats by row and column
        Collections.sort(seatList, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                int row1 = Integer.parseInt(s1.substring(0, s1.length() - 1));
                int row2 = Integer.parseInt(s2.substring(0, s2.length() - 1));
                if (row1 != row2) return row1 - row2;
                char seat1 = s1.charAt(s1.length() - 1);
                char seat2 = s2.charAt(s2.length() - 1);
                return seat1 - seat2;
            }
        });
        
        return seatList;
    }

    //Allows customers to modify their existing orders.
    //Options include adding tickets, deleting tickets, or canceling the order.
    private static void updateOrder() {
        Customer customer = userMap.get(currentUser);
        List<Customer.Order> orders = customer.getOrders();
        
        if (orders.isEmpty()) {
            System.out.println("No orders");
            return;
        }
        
        //Display all orders for selection
        for (int i = 0; i < orders.size(); i++) {
            System.out.println((i + 1) + ". Auditorium " + 
                             orders.get(i).getAuditoriumNumber() + ", " + 
                             String.join(",", generateUpdatedSeatList(orders.get(i))));
        }
        
        if (!scanner.hasNextLine()) return;
        int orderChoice = getValidIntInput(1, orders.size());
        Customer.Order selectedOrder = orders.get(orderChoice - 1);

        boolean done = false;
        while (!done && scanner.hasNextLine()) {
            System.out.println("\n1. Add tickets to order");
            System.out.println("2. Delete tickets from order");
            System.out.println("3. Cancel Order");
            
            if (!scanner.hasNextLine()) return;
            int updateChoice = getValidIntInput(1, 3);
            
            switch (updateChoice) {
                case 1:
                    addTicketsToOrder(selectedOrder);
                    done = true;
                    break;
                case 2:
                    deleteTicketsFromOrder(selectedOrder);
                    done = true;
                    break;
                case 3:
                    cancelOrder(selectedOrder);
                    done = true;
                    break;
            }
        }
    }

    //Adds additional tickets to an existing order.
    //order: The order to add tickets to
    private static void addTicketsToOrder(Customer.Order order) {
        int auditoriumNumber = order.getAuditoriumNumber();
        Auditorium auditorium = auditoriums[auditoriumNumber - 1];
        
        auditorium.display();
        
        //Get seat selection for additional tickets
        if (!scanner.hasNextLine()) return;
        int rowNumber = getValidRowInput(auditorium);
        if (!scanner.hasNextLine()) return;
        char startingSeat = getValidSeatInput(auditorium);
        if (!scanner.hasNextLine()) return;
        int adultTickets = getValidTicketInput("adult");
        if (!scanner.hasNextLine()) return;
        int childTickets = getValidTicketInput("child");
        if (!scanner.hasNextLine()) return;
        int seniorTickets = getValidTicketInput("senior");
        
        int totalTickets = adultTickets + childTickets + seniorTickets;
        
        if (totalTickets == 0) {
            System.out.println("No tickets selected. Returning to update menu.");
            return;
        }
        
        int seatIndex = startingSeat - 'A';
        
        //Check availability and reserve seats
        if (!auditorium.checkAvailability(rowNumber, seatIndex, totalTickets)) {
            System.out.println("The seats are not available.");
            return;
        }
        
        reserveSeatsInAuditorium(auditorium, rowNumber, seatIndex, 
            adultTickets, childTickets, seniorTickets);
        order.addTickets(rowNumber, seatIndex, totalTickets, 
                       adultTickets, childTickets, seniorTickets);
    }

    //Deletes specific tickets from an order.
    //order: The order to remove tickets from
    private static void deleteTicketsFromOrder(Customer.Order order) {
        int auditoriumNumber = order.getAuditoriumNumber();
        Auditorium auditorium = auditoriums[auditoriumNumber - 1];

        List<String> seatList = generateUpdatedSeatList(order);

        int rowToRemove = 0;
        char seatToRemove = ' ';
        boolean validInput = false;

        //Get seat to remove
        while (!validInput && scanner.hasNextLine()) {            
            try {
                System.out.print("Enter Row: ");
                if (!scanner.hasNextLine()) return;
                rowToRemove = Integer.parseInt(scanner.nextLine());

                System.out.print("Enter Seat: ");
                if (!scanner.hasNextLine()) return;
                String input = scanner.nextLine().toUpperCase();
                if (input.length() != 1) {
                    System.out.println("Invalid input");
                    continue;
                }

                seatToRemove = input.charAt(0);
                validInput = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            }
        }

        int seatIndexToRemove = seatToRemove - 'A';
        boolean validSeat = seatList.contains(rowToRemove + "" + seatToRemove);

        if (!validSeat) {
            System.out.println("Invalid selection");
            return;
        }

        //Unreserve seat and update order
        char ticketType = auditorium.getSeatType(rowToRemove, seatIndexToRemove);
        auditorium.unreserveSeat(rowToRemove, seatIndexToRemove);

        switch (ticketType) {
            case 'A': order.removeAdultTicket(); break;
            case 'C': order.removeChildTicket(); break;
            case 'S': order.removeSeniorTicket(); break;
        }

        //Remove order if no tickets remain
        if (order.getTotalTickets() == 0) {
            userMap.get(currentUser).removeOrder(order);
        }
    }

    //Cancels an entire order and unreserves all its seats.
    //order: The order to cancel
    private static void cancelOrder(Customer.Order order) {
        int auditoriumNumber = order.getAuditoriumNumber();
        Auditorium auditorium = auditoriums[auditoriumNumber - 1];

        int row = order.getRow();
        int startSeat = order.getStartSeat();
        
        //Unreserve original seats
        int originalCount = order.getOriginalTicketCount();
        for (int i = 0; i < originalCount; i++) {
            auditorium.unreserveSeat(row, startSeat + i);
        }

        //Unreserve additional seats
        for (int[] additionalSeat : order.getAdditionalSeats()) {
            int additionalRow = additionalSeat[0];
            int additionalStartSeat = additionalSeat[1];
            int additionalTotal = additionalSeat[2];

            for (int i = 0; i < additionalTotal; i++) {
                auditorium.unreserveSeat(additionalRow, additionalStartSeat + i);
            }
        }

        userMap.get(currentUser).removeOrder(order);
    }

    //Displays a receipt with all the customer's orders and total cost.
    //Ticket prices: Adult $10.00, Child $5.00, Senior $7.50
    private static void displayReceipt() {
        Customer customer = userMap.get(currentUser);
        List<Customer.Order> orders = customer.getOrders();
        
        if (orders.isEmpty()) {
            System.out.println("No orders");
            System.out.printf("Customer Total: $0.00\n");
            return;
        }
        
        double customerTotal = 0.0;
        
        for (Customer.Order order : orders) {
            int auditoriumNumber = order.getAuditoriumNumber();
            List<String> seatList = generateUpdatedSeatList(order);
            
            //Calculate order total
            double orderTotal = (order.getAdultTickets() * 10.0) +
                               (order.getChildTickets() * 5.0) +
                               (order.getSeniorTickets() * 7.5);
            
            customerTotal += orderTotal;
            
            //Display order details
            System.out.println("Auditorium " + auditoriumNumber + ", " + String.join(",", seatList));
            System.out.println(order.getAdultTickets() + " adult, " +
                             order.getChildTickets() + " child, " +
                             order.getSeniorTickets() + " senior");
            System.out.printf("Order Total: $%.2f\n", orderTotal);
            System.out.println();
        }
        
        System.out.printf("Customer Total: $%.2f\n", customerTotal);
    }

    //Generates a comprehensive report for all auditoriums.
    //Shows open seats, reserved seats, ticket type counts, and total sales.
    private static void printReport() {
        int totalOpen = 0;
        int totalReserved = 0;
        int totalAdult = 0;
        int totalChild = 0;
        int totalSenior = 0;
        double totalSales = 0.0;

        //Generate report for each auditorium
        for (int i = 0; i < auditoriums.length; i++) {
            Auditorium a = auditoriums[i];
            int open = a.getOpenSeatCount();
            int reserved = a.getReservedSeatCount();
            int adult = a.getTicketTypeCount('A');
            int child = a.getTicketTypeCount('C');
            int senior = a.getTicketTypeCount('S');
            double sales = adult * 10.0 + child * 5.0 + senior * 7.5;

            //Update totals
            totalOpen += open;
            totalReserved += reserved;
            totalAdult += adult;
            totalChild += child;
            totalSenior += senior;
            totalSales += sales;

            //Print auditorium details
            System.out.print("Auditorium " + (i + 1));
            System.out.print("\t" + open);
            System.out.print("\t" + reserved);
            System.out.print("\t" + adult);
            System.out.print("\t" + child);
            System.out.print("\t" + senior);
            System.out.print("\t");
            System.out.printf("$%.2f\n", sales);
        }

        //Print totals
        System.out.print("Total");
        System.out.print("\t" + totalOpen);
        System.out.print("\t" + totalReserved);
        System.out.print("\t" + totalAdult);
        System.out.print("\t" + totalChild);
        System.out.print("\t" + totalSenior);
        System.out.print("\t");
        System.out.printf("$%.2f\n", totalSales);
    }

    //Saves all auditorium states to files.
    //Creates files A1Final.txt, A2Final.txt, and A3Final.txt.
    private static void saveAuditoriums() {
        for (int i = 0; i < auditoriums.length; i++) {
            auditoriums[i].saveToFile("A" + (i + 1) + "Final.txt");
        }
    }
}