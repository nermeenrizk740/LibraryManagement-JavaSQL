import java.io.*;
import java.sql.*;
import java.util.*;
import org.apache.ibatis.jdbc.ScriptRunner;
import oracle.jdbc.driver.*;

public class Student{
    static Connection con;
    static Statement stmt;
    static Scanner newScanner = new Scanner(System.in);

    public static void main(String argv[])
    {

    do{
	 connectToDatabase();
    }while(con == null);

     System.out.println();
     loadThe_BookCopies_Contents();
     promptTheUserForTheMainMenu();

    try{

        if(con!=null && !con.isClosed()){
            con.close();
            System.out.println("The connection to Database is now closed");
        }
    }catch( Exception e) {e.printStackTrace();}
     newScanner.close();   
    }

    public static void connectToDatabase()
    {
	String driverPrefixURL="jdbc:oracle:thin:@";
	String jdbc_url="artemis.vsnet.gmu.edu:1521/vse18c.vsnet.gmu.edu";
	
        // IMPORTANT: DO NOT PUT YOUR LOGIN INFORMATION HERE. INSTEAD, PROMPT USER FOR HIS/HER LOGIN/PASSWD
        //String username="xxxxxx";
        //String password="xxxxxx";

        System.out.println("Please enter your Oracle username (same username as your GMU)");
        String username= newScanner.nextLine();
        System.out.println("Please enter your oracle password (Not your GMU's password)");
        String password= newScanner.nextLine();
	
        try{
	    //Register Oracle driver
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (Exception e) {
            System.out.println("Failed to load JDBC/ODBC driver.");
            return;
        }

       try{
            System.out.println(driverPrefixURL+jdbc_url);
            con=DriverManager.getConnection(driverPrefixURL+jdbc_url, username, password);
            DatabaseMetaData dbmd=con.getMetaData();
            stmt=con.createStatement();

            System.out.println("Connected.");

            if(dbmd==null){
                System.out.println("No database meta data");
            }
            else {
                System.out.println("Database Product Name: "+dbmd.getDatabaseProductName());
                System.out.println("Database Product Version: "+dbmd.getDatabaseProductVersion());
                System.out.println("Database Driver Name: "+dbmd.getDriverName());
                System.out.println("Database Driver Version: "+dbmd.getDriverVersion());
            }
        }catch( Exception e) {e.printStackTrace();}

    }// End of connectToDatabase()


    //This method is to load and execute the BookCopies.sql file
    public static void loadThe_BookCopies_Contents(){

        //The outer loop to keep looping if the path was not found or empty
        while(true){
            String fullFilePath ="";
            //if the user clicked enter (empty path), keep asking till they enter the vaild path
            while(fullFilePath.isEmpty()){
                System.out.println("Please enter the full path of the BookCopies.sql script file");
                System.out.println();
                fullFilePath = newScanner.nextLine();
                System.out.println();

                if(fullFilePath.isEmpty()){
                    System.out.println("Path cannot be empty");
                }
            }
        
            //once the user enter a valid path, reader it using ScriptRunner and Reader then run it
            try{
                ScriptRunner sr = new ScriptRunner(con);
                Reader reader = new BufferedReader(new FileReader(fullFilePath)); 
                sr.runScript(reader);
                break;      
            }catch(FileNotFoundException e){
                System.out.println("The file was not found " + e.getMessage());
            }catch( Exception e) {e.printStackTrace();}
        }
    }



    //This method is to promot the user for their option of the service they need
    public static void promptTheUserForTheMainMenu(){
        int userChoiceFromMenu = 0;
        String user_input_choice ="";

        do{
            //display the main menu
            System.out.println("*******Main Menu*********");
            System.out.println("1. Search Books");
            System.out.println("2. Show the Number of Available Copies");
            System.out.println("3. Add a New Copy");
            System.out.println("4. Update Book Copy Status (if valid)");
            System.out.println("5. Exit");
            System.out.println();
            System.out.println("Please enter your choice from 1-5");
            System.out.println();

            //get the user input
            user_input_choice = newScanner .nextLine().trim();
            System.out.println();

            try{

                //call the method based on the user choice
                userChoiceFromMenu = Integer.parseInt(user_input_choice);
                if(userChoiceFromMenu<1 || userChoiceFromMenu>5){
                    System.out.println("Invalid choice, Number needs to be between 1 - 5, please try again \n");
                    continue;
                }

                if(userChoiceFromMenu==1){
                    searchBooks_By_ISBN_Title_Category();
                }
                if(userChoiceFromMenu==2){
                    calculatingNumberOfAvailableCopies();
                }
                if(userChoiceFromMenu==3){
                    add_anew_copy_to_an_existing_book();
                }
                if(userChoiceFromMenu==4){
                    Update_Book_Copy_Status();
                }
                if(userChoiceFromMenu==5){
                    System.out.println("Thank you for using our library database! GoodBye");
                }
                

            }catch(NumberFormatException e){
                System.out.println("Invalid choice, Needs a number from 1 to 5, please try again\n");
            }

        }while(userChoiceFromMenu!=5);
    }


    //This method is to return any book from the database based on ISBN or a key word entered by the user
    
    public static void searchBooks_By_ISBN_Title_Category(){

        //prompot the user for a key word to search for the book
        //keep prompting as long as it is empty
        String search_KeyWord = "";
        do{
            System.out.println("Enter a search keyword (ISBN, Title, or Category)\n");
            search_KeyWord = newScanner.nextLine().trim();
            System.out.println();
            if(search_KeyWord.isEmpty()){
                System.out.println("Empty keywords are not acceptable, please try again");
            }
        }while(search_KeyWord.isEmpty());



        try{

            //using the keyword entered by the user serach for the book/books that match
            String sqlQuery = " select * from Books where ISBN = ? OR LOWER(category) LIKE ? OR LOWER(title) LIKE ? ";
            
            PreparedStatement pstmt = con.prepareStatement(sqlQuery);
            pstmt.clearParameters();
            pstmt.setString(1,search_KeyWord);
            pstmt.setString(2, "%"+search_KeyWord.toLowerCase()+"%");
            pstmt.setString(3, "%"+search_KeyWord.toLowerCase()+"%");

            ResultSet rset = pstmt.executeQuery();

            boolean checkIfBookExists = false;

            //print the book/books found
            int index = 0;
            while(rset.next()){
                checkIfBookExists = true;
                index++;
                String ISBN = rset.getString(1);
                String title = rset.getString(2);
                String edition = rset.getString(3);
                String category = rset.getString(4);
                double price = rset.getDouble(5);
                System.out.print(index +". ");
                System.out.printf("ISBN: %s\nTitle:  %s\nEdition: %s\nCategory: %s\nprice: $%.2f\n\n", ISBN, title,edition,category,price);
            }

            //if no books were found, print a message to the user
            if(!checkIfBookExists){
                System.out.println("There is no book found with (" + search_KeyWord + ") search keyword");
                System.out.println();
            }

            rset.close();
            pstmt.close();

        }catch(SQLException e){
            System.out.println("There was an error occured while we are searching for the book " + e.getMessage());
        }
    }


    //this method is to calculate how many copy of a particular book is a vailable
    public static void calculatingNumberOfAvailableCopies(){

        //prompt the user for a keyword (ISBN or title) to search for the book and return the total of the available copies of that book
        String search_KeyWord = "";
        String BookISBNThatNeedToAddedCopyToIt="";
        int bookSelection =0;
        StringBuilder displayBooks = new StringBuilder();

        do{
            System.out.println("Enter an ISBN or Title to display the number of available copies \n");
            search_KeyWord = newScanner.nextLine().trim();
            System.out.println();
            if(search_KeyWord.isEmpty()){
                System.out.println("Empty keywords are not acceptable, please try again\n");
            }
        }while(search_KeyWord.isEmpty());


        try{

            //extracting the book/books that match what the user entered
            //used LOWER to handle any case sensitive
            String sqlQuery = "select ISBN, Title from Books where ISBN = ? OR LOWER(title) LIKE ?";
            PreparedStatement pstmt = con.prepareStatement(sqlQuery);
            pstmt.clearParameters();
            pstmt.setString(1,search_KeyWord);
            pstmt.setString(2, "%"+search_KeyWord.toLowerCase()+"%");

            ResultSet rset = pstmt.executeQuery();
            List<String> foundBooksBasedOnSearchingKeyword = new ArrayList<>();

            //if no book/books were found, return to the main menu
            if(!rset.next()){
                System.out.println("No Book was found using (" + search_KeyWord + ") search key word\n");
                return;
            }
            
            //save all the books were found in a list so if we got more than a book, user should select which one they want to find the total available copies
            do{
                String ISBN = rset.getString(1);
                String title = rset.getString(2);
                foundBooksBasedOnSearchingKeyword.add(ISBN);
                displayBooks.append(foundBooksBasedOnSearchingKeyword.size()).append(". ").append(title).append(" (").append(ISBN).append(")\n");
            }while(rset.next());
            System.out.println();

            //If the book was found, we have two possibilities
            //if the result is only one book, we just use its ISBN and get the total copies of it
            //if more than one book, I print the name and ISBN and ask the user to enter a number attached to the book (index of the book or order number)
            //then use this number to get the ISBN of the book
            if(foundBooksBasedOnSearchingKeyword.size()==1){
                BookISBNThatNeedToAddedCopyToIt= foundBooksBasedOnSearchingKeyword.get(0);
            }
            else{
                System.out.println("There are multiple books found using the keyword entered, please enter the book number from the list(Not the ISBN , but the order number next to the book name)\n"+ displayBooks.toString());   
                do{
                    try{
                        bookSelection = Integer.parseInt(newScanner.nextLine());
                        if(bookSelection<1 || bookSelection> foundBooksBasedOnSearchingKeyword.size()){
                            System.out.println("This is invalid selection, please enter the number of the book in the list\n");
                        }
                        else{
                            BookISBNThatNeedToAddedCopyToIt = foundBooksBasedOnSearchingKeyword.get(bookSelection-1);
                        }
                   }catch(NumberFormatException e){
                    System.out.println("This is invalid selection, please enter the number of the book in the list");
                   }
                }while(bookSelection<1 || bookSelection> foundBooksBasedOnSearchingKeyword.size());
            }

            //getting the count of the available copies for the found book
            sqlQuery = "Select count(*) from Book_Copies where ISBN = ? AND (LOWER(status) = 'available')";
            pstmt = con.prepareStatement(sqlQuery);
            pstmt.clearParameters();
            pstmt.setString(1,BookISBNThatNeedToAddedCopyToIt);
            rset = pstmt.executeQuery();
                            

            //return the count
            if(rset.next()){
                int totalCopiesCount = rset.getInt(1);
                System.out.println("Number of Available Copies: " + totalCopiesCount + "\n");
                System.out.println();
            }

            rset.close();
            pstmt.close();

        }catch(SQLException e){
            System.out.println("There was an error occured while we are searching for the book " + e.getMessage());
        }

    }


    //This method is to add a new copy to an existing book

    public static void add_anew_copy_to_an_existing_book(){

        //prompt the user for the keyword to find the book to add the copy to it
        String search_KeyWord = "";
        String BookISBNThatNeedToAddedCopyToIt="";
        int bookSelection =0 ;
        int NextCopyNumberOfTheBook = 1;
        StringBuilder displayBooks = new StringBuilder();

        do{
            System.out.println("Enter an ISBN or Title of the book that you would like to add a new copy to \n");
            search_KeyWord = newScanner.nextLine().trim();
            System.out.println();
            if(search_KeyWord.isEmpty()){
                System.out.println("Empty keywords are not acceptable, please try again\n");
            }
        }while(search_KeyWord.isEmpty());

        //Getting the status of the new book copy from the user
        String new_Copy_status ="";
        do{ 
        System.out.println("Please enter the new book'copy Status (Available, Checked out,or Damaged) \n");
        new_Copy_status = newScanner.nextLine().trim();
        System.out.println();
        }while(!new_Copy_status.equalsIgnoreCase("Available") && !new_Copy_status.equalsIgnoreCase("Checked out") &&!new_Copy_status.equalsIgnoreCase("Damaged"));



        try{

            //extracting the book/books that match what the user entered
            String sqlQuery = "select ISBN, Title from Books where ISBN = ? OR LOWER(title) LIKE ?";
            PreparedStatement pstmt = con.prepareStatement(sqlQuery);
            pstmt.clearParameters();
            pstmt.setString(1,search_KeyWord);
            pstmt.setString(2, "%"+search_KeyWord.toLowerCase()+"%");

            ResultSet rset = pstmt.executeQuery();
            List<String> foundBooksBasedOnSearchingKeyword = new ArrayList<>();

            //if no book/books were found, return to the main menu
            if(!rset.next()){
                System.out.println("No Book was found using (" + search_KeyWord + ") search key word to add the new copy\n");
                return;
            }
            
            //save all the books were found in a list so if we got more than a book, user should select which one they should choose
            do{
                String ISBN = rset.getString(1);
                String title = rset.getString(2);
                foundBooksBasedOnSearchingKeyword.add(ISBN);
                displayBooks.append(foundBooksBasedOnSearchingKeyword.size()).append(". ").append(title).append(" (").append(ISBN).append(")\n");
            }while(rset.next());
            System.out.println();

            //If the book was found, we have two possibilities
            //if the result is only one book, we just use its ISBN and add the copy to it
            //if more than one book, I print the name and ISBN and ask the user to enter a number attached to the book
            //then use this number to get the ISBN of the book
            if(foundBooksBasedOnSearchingKeyword.size()==1){
                BookISBNThatNeedToAddedCopyToIt= foundBooksBasedOnSearchingKeyword.get(0);
            }
            else{
                System.out.println("There are multiple books found using the keyword entered, please enter the book number from the list(Not the ISBN , but the book number in the list)\n"+ displayBooks.toString());   
                do{
                    try{
                        bookSelection = Integer.parseInt(newScanner.nextLine());
                        if(bookSelection<1 || bookSelection> foundBooksBasedOnSearchingKeyword.size()){
                            System.out.println("This is invalid selection, please enter a book number from the list above \n");
                        }
                        else{
                            BookISBNThatNeedToAddedCopyToIt = foundBooksBasedOnSearchingKeyword.get(bookSelection-1);
                        }
                   }catch(NumberFormatException e){
                    System.out.println("This is invalid selection, please enter a book number from the list above");
                   }
                }while(bookSelection<1 || bookSelection> foundBooksBasedOnSearchingKeyword.size());
            }
            
            // get the next copy of the book by finding the max copy number we have to add 1 to it and get the next copy number to add to the new copy
            sqlQuery = "select MAX(Copy#) from Book_Copies where ISBN = ?";
            pstmt = con.prepareStatement(sqlQuery);
            pstmt.clearParameters();
            pstmt.setString(1,BookISBNThatNeedToAddedCopyToIt);
            rset = pstmt.executeQuery();
            if(rset.next()){
                NextCopyNumberOfTheBook = rset.getInt(1)+1;
            }
            

            //entering the new copy
            sqlQuery = "Insert into Book_Copies (ISBN, Copy#,Status) VALUES (?, ? , ?)";
            pstmt = con.prepareStatement(sqlQuery);
            pstmt.clearParameters();
            pstmt.setString(1,BookISBNThatNeedToAddedCopyToIt);
            pstmt.setInt(2, NextCopyNumberOfTheBook);
            pstmt.setString(3, new_Copy_status);
            pstmt.executeUpdate();

            System.out.println("Copy # " + NextCopyNumberOfTheBook + " was added to the book with ISBN: " + BookISBNThatNeedToAddedCopyToIt + "\n");
            System.out.println();
            rset.close();
            pstmt.close();

        }catch(SQLException e){
            System.out.println("There was an error occured while we are searching for the book " + e.getMessage());
        }
    }


    //This method is to update the status of a book copy

    public static void Update_Book_Copy_Status(){
        String search_KeyWord = "";
        int copyNumber = -1;
        String new_Copy_status ="";

        //prompot the user for the book ISBN and keep prompting as long as it is empty
        do{
            System.out.println("Enter the ISBN of the book that you would like to update one of its copy status \n");
            search_KeyWord = newScanner.nextLine().trim();
            System.out.println();
            if(search_KeyWord.isEmpty()){
                System.out.println("Empty keywords are not acceptable, please try again\n");
            }
        }while(search_KeyWord.isEmpty());


        //prompt the user for copy #
        do{
            System.out.println("Enter Copy # of the book \n ");
            String copyNum = newScanner.nextLine().trim();
            System.out.println();
            if(copyNum.isEmpty()){
                System.out.println("Copy # cannot be empty \n");
                continue;
            }
            try{
                copyNumber = Integer.parseInt(copyNum);
                if(copyNumber<=0){
                    System.out.println("Copy number needs to be positive number \n");
                    copyNumber = -1;
                }

            }catch(NumberFormatException e){
                System.out.println("Please enter a valid copy number \n");
            }
        }while(copyNumber==-1);

        //Getting the new status that user wants to update the book copy to
        do{ 
        System.out.println("Please enter the new copy' Status (Available, Checked out,or Damaged) \n");
        new_Copy_status = newScanner.nextLine().trim();
        System.out.println();
        }while(!new_Copy_status.equalsIgnoreCase("Available") && !new_Copy_status.equalsIgnoreCase("Checked out") &&!new_Copy_status.equalsIgnoreCase("Damaged"));


        //find the book copy that match the user input
        try{
            String sqlQuery = " select status from Book_copies where ISBN = ? AND copy# = ? ";
            
            PreparedStatement pstmt = con.prepareStatement(sqlQuery);
            pstmt.clearParameters();
            pstmt.setString(1,search_KeyWord);
            pstmt.setInt(2, copyNumber);
            ResultSet rset = pstmt.executeQuery();

            //if no books were found return to the main menu
            if(!rset.next()){
                System.out.println("No book copy were found with the ISBN and copy number provided\n");
                return;
            }

            String current_book_status = rset.getString("Status");

            if(current_book_status.equalsIgnoreCase("Damaged")){
                System.out.println("Damaged status book'copy cannot be changed! \n");
                return;
            }

            //update the book copy to the new status as long as its current status is not damaged
            sqlQuery = "UPDATE Book_Copies SET Status = ? where ISBN = ? AND Copy# =  ? ";
            pstmt = con.prepareStatement(sqlQuery);
            pstmt.clearParameters();
            pstmt.setString(1,new_Copy_status);
            pstmt.setString(2,search_KeyWord);
            pstmt.setInt(3, copyNumber);
           
            int update = pstmt.executeUpdate();
            if(update >0){
                System.out.println("Book'copy Status updated successfully from " +current_book_status+ " to " +  new_Copy_status);
                System.out.println();
            }
            else{
                System.out.println("Book'copy Status update did not go through");
            }

            pstmt.close();
            rset.close();

        }catch(SQLException e){
            System.out.println("There was an error occured while we are searching for the book " + e.getMessage());
        }
    }
}// End of class

