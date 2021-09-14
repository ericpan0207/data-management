import java.io.FileInputStream;
import java.sql.*;
import java.util.*;

/**
 * Runs queries against a back-end database
 */
public class Query
{
  private String configFilename;
  private Properties configProps = new Properties();

  private String jSQLDriver;
  private String jSQLUrl;
  private String jSQLUser;
  private String jSQLPassword;

  private HashMap<Integer, List<Flight>> itineraries = new HashMap<>();

  // DB Connection
  private Connection conn;

  // Logged In User
  private String username; // customer username is unique

  // Canned queries

  private static final String CHECK_FLIGHT_CAPACITY = "SELECT capacity FROM Flights WHERE fid = ?";
  private PreparedStatement checkFlightCapacityStatement;

  // transactions
  private static final String BEGIN_TRANSACTION_SQL = "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE; BEGIN TRANSACTION;";
  private PreparedStatement beginTransactionStatement;

  private static final String COMMIT_SQL = "COMMIT TRANSACTION";
  private PreparedStatement commitTransactionStatement;

  private static final String ROLLBACK_SQL = "ROLLBACK TRANSACTION";
  private PreparedStatement rollbackTransactionStatement;

  private static final String SEARCH_TRANSACTION_SQL1 = "SELECT TOP(?) f1.fid, f1.day_of_month, f1.carrier_id, f1.flight_num, f1.origin_city, f1.dest_city, f1.actual_time, f1.capacity, f1.price             "
                                                      + "  FROM Flights f1            "
                                                      + " WHERE f1.origin_city = ?    "
                                                      + "   AND f1.dest_city = ?      "
                                                      + "   AND f1.day_of_month = ?   "
                                                      + "   AND f1.canceled = 0       "
                                                      + " ORDER BY f1.actual_time ASC, f1.fid ASC;        ";
  private PreparedStatement searchTransactionStatement1;
  private static final String SEARCH_TRANSACTION_SQL2 = "SELECT TOP(?) f1.fid, f1.day_of_month, f1.carrier_id, f1.flight_num, f1.origin_city, f1.dest_city, f1.actual_time, f1.capacity, f1.price,  f2.fid, f2.day_of_month, f2.carrier_id, f2.flight_num, f2.origin_city, f2.dest_city, f2.actual_time, f2.capacity, f2.price                        "
                                                      + "  FROM Flights f1, Flights f2            "
                                                      + " WHERE f1.origin_city = ?                "
                                                      + "   AND f1.dest_city = f2.origin_city     "
                                                      + "   AND f2.dest_city = ?                  "
                                                      + "   AND f1.day_of_month = ?               "
                                                      + "   AND f1.day_of_month = f2.day_of_month "
                                                      + "   AND f1.month_id = f2.month_id         "
                                                      + "   AND f1.canceled = 0 AND f2.canceled = 0"
                                                      + " ORDER BY (f1.actual_time + f2.actual_time) ASC, f1.fid ASC;                    ";
  private PreparedStatement searchTransactionStatement2;

  // Login
  private static final String LOGIN_SQL = "SELECT *               "
                                        + "  FROM Users u         "
                                        + " WHERE u.username = ?  "
                                        + "   AND u.password = ?  ";
  private PreparedStatement loginStatement;

  private static final String CHECK_USER_SQL = "SELECT * FROM Users u WHERE u.username = ?";
  private PreparedStatement checkUserStatement;

  private static final String CREATE_USER_SQL = "INSERT INTO Users VALUES (?, ?, ?)";
  private PreparedStatement createUserStatement;

  private static final String CHECK_BOOKING_SQL = "SELECT *                                 "
                                                + "  FROM Flights f, Users u, Reservations r"
                                                + " WHERE f.fid = r.r_fid1                  "
                                                + "  AND u.username = r.user_id             "
                                                + " AND u.username = ?                      "
                                                + " AND f.day_of_month = ?                  ";
  private PreparedStatement checkBookingStatement;

  private static final String CREATE_RESERVATION_SQL = "INSERT INTO Reservations (user_id, r_fid1, r_fid2) VALUES (?, ?, ?)";
  private PreparedStatement createReservationStatement;

  private static final String CHECK_AVAILABLE_FLIGHT_SQL = "SELECT COUNT(*) AS count FROM Reservations r WHERE r.r_fid1 = ? OR r.r_fid2 = ?";
  private PreparedStatement checkAvailableFlightStatement;

  private static final String GET_RESERVATION_SQL = "SELECT * FROM Reservations r where r.user_id = ? ";
  private PreparedStatement getReservationStatement;


  private static final String GET_FLIGHT_SQL = "SELECT f1.fid, f1.day_of_month, f1.carrier_id, f1.flight_num, f1.origin_city, f1.dest_city, f1.actual_time, f1.capacity, f1.price             " +
                                               "  FROM Flights f1 WHERE f1.fid = ?";
  private PreparedStatement getFlightStatement;

  private static final String PAY_RESERVATION_SQL = "SELECT * FROM Users u, Reservations r WHERE u.username = r.user_id and r.paid = 0 and r.reserve_id = ? and u.username = ?";
  private PreparedStatement payReservationStatement;

  private static final String GET_FLIGHT_PRICE_SQL = "SELECT f.price From Flights f where f.fid = ?";
  private PreparedStatement getFlightPriceStatement;

  private static final String UPDATE_BALANCE_SQL = "UPDATE Users SET balance = ? WHERE username = ?";
  private PreparedStatement updateBalanceStatement;

  private static final String UPDATE_PAID_SQL = "update Reservations SET paid = 1 WHERE reserve_id = ?";
  private PreparedStatement updatePaidStatement;

  private static final String CHECK_PAID_SQL = "SELECT * FROM Users u, Reservations r WHERE u.username = r.user_id and r.reserve_id = ? and u.username = ?";
  private PreparedStatement checkPaidStatement;

  private static final String REMOVE_RESERVATION_SQL = "DELETE FROM Reservations WHERE reserve_id = ?";
  private PreparedStatement removeReservationStatement;

  private static final String CLEAR_USERS_SQL = "DELETE FROM Users";
  private PreparedStatement clearUsersStatement;

  private static final String CLEAR_RESERVATIONS_SQL = "DELETE FROM Reservations";
  private PreparedStatement clearReservationsStatement;

  private static final String RESET_RESERVATION_SQL = "DBCC CHECKIDENT (Reservations, RESEED, 0)";
  private PreparedStatement resetReservationStatement;

  class Flight
  {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    public Flight(int fid, int dayOfMonth, String carrierId, String flightNum,
                  String originCity, String destCity, int time, int capacity, int price) {
      this.fid = fid;
      this.dayOfMonth = dayOfMonth;
      this.carrierId = carrierId;
      this.flightNum = flightNum;
      this.originCity = originCity;
      this.destCity = destCity;
      this.time = time;
      this.capacity = capacity;
      this.price = price;
    }

    @Override
    public String toString()
    {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId +
              " Number: " + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time +
              " Capacity: " + capacity + " Price: " + price;
    }
  }

  public Query(String configFilename)
  {
    this.configFilename = configFilename;
  }

  /* Connection code to SQL Azure.  */
  public void openConnection() throws Exception
  {
    configProps.load(new FileInputStream(configFilename));

    jSQLDriver = configProps.getProperty("flightservice.jdbc_driver");
    jSQLUrl = configProps.getProperty("flightservice.url");
    jSQLUser = configProps.getProperty("flightservice.sqlazure_username");
    jSQLPassword = configProps.getProperty("flightservice.sqlazure_password");

    /* load jdbc drivers */
    Class.forName(jSQLDriver).newInstance();

    /* open connections to the flights database */
    conn = DriverManager.getConnection(jSQLUrl, // database
            jSQLUser, // user
            jSQLPassword); // password

    conn.setAutoCommit(true); //by default automatically commit after each statement

    conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    /* You will also want to appropriately set the transaction's isolation level through:
       conn.setTransactionIsolation(...)
       See Connection class' JavaDoc for details.
    */
  }

  public void closeConnection() throws Exception
  {
    conn.close();
  }

  /**
   * Clear the data in any custom tables created. Do not drop any tables and do not
   * clear the flights table. You should clear any tables you use to store reservations
   * and reset the next reservation ID to be 1.
   */
  public void clearTables ()
  {
    try {
      clearReservationsStatement.execute();
      resetReservationStatement.execute();
      clearUsersStatement.execute();
      this.username = null;
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * prepare all the SQL statements in this method.
   * "preparing" a statement is almost like compiling it.
   * Note that the parameters (with ?) are still not filled in
   */
  public void prepareStatements() throws Exception
  {
    beginTransactionStatement = conn.prepareStatement(BEGIN_TRANSACTION_SQL);
    commitTransactionStatement = conn.prepareStatement(COMMIT_SQL);
    rollbackTransactionStatement = conn.prepareStatement(ROLLBACK_SQL);

    checkFlightCapacityStatement = conn.prepareStatement(CHECK_FLIGHT_CAPACITY);

    /* add here more prepare statements for all the other queries you need */
    /* . . . . . . */
    searchTransactionStatement1 = conn.prepareStatement(SEARCH_TRANSACTION_SQL1);
    searchTransactionStatement2 = conn.prepareStatement(SEARCH_TRANSACTION_SQL2);
    loginStatement = conn.prepareStatement(LOGIN_SQL);
    checkUserStatement = conn.prepareStatement(CHECK_USER_SQL);
    createUserStatement = conn.prepareStatement(CREATE_USER_SQL);
    checkBookingStatement = conn.prepareStatement(CHECK_BOOKING_SQL);
    createReservationStatement = conn.prepareStatement(CREATE_RESERVATION_SQL, Statement.RETURN_GENERATED_KEYS);
    checkAvailableFlightStatement = conn.prepareStatement(CHECK_AVAILABLE_FLIGHT_SQL);
    getReservationStatement = conn.prepareStatement(GET_RESERVATION_SQL);
    getFlightStatement = conn.prepareStatement(GET_FLIGHT_SQL);
    payReservationStatement = conn.prepareStatement(PAY_RESERVATION_SQL);
    getFlightPriceStatement = conn.prepareStatement(GET_FLIGHT_PRICE_SQL);
    updateBalanceStatement = conn.prepareStatement(UPDATE_BALANCE_SQL);
    updatePaidStatement = conn.prepareStatement(UPDATE_PAID_SQL);
    checkPaidStatement = conn.prepareStatement(CHECK_PAID_SQL);
    removeReservationStatement = conn.prepareStatement(REMOVE_RESERVATION_SQL);
    clearUsersStatement = conn.prepareStatement(CLEAR_USERS_SQL);
    clearReservationsStatement = conn.prepareStatement(CLEAR_RESERVATIONS_SQL);
    resetReservationStatement = conn.prepareStatement(RESET_RESERVATION_SQL);
  }

  /**
   * Takes a user's username and password and attempts to log the user in.
   *
   * @param username
   * @param password
   *
   * @return If someone has already logged in, then return "User already logged in\n"
   * For all other errors, return "Login failed\n".
   *
   * Otherwise, return "Logged in as [username]\n".
   */
  public String transaction_login(String username, String password)
  {
    try {
      if (this.username != null) {
        return "User already logged in\n";
      }
      loginStatement.clearParameters();
      loginStatement.setString(1, username);
      loginStatement.setString(2, password);
      ResultSet result = loginStatement.executeQuery();
      if (!result.next()) {
        result.close();
        return "Login failed\n";
      }

    } catch(SQLException e){
      e.printStackTrace();
      return "Login failed\n";
    }
    this.username = username;
    this.itineraries.clear();
    return "Logged in as " + username + "\n";
  }

  /**
   * Implement the create user function.
   *
   * @param username new user's username. User names are unique the system.
   * @param password new user's password.
   * @param initAmount initial amount to deposit into the user's account, should be >= 0 (failure otherwise).
   *
   * @return either "Created user {@code username}\n" or "Failed to create user\n" if failed.
   */
  public String transaction_createCustomer (String username, String password, int initAmount)
  {

    try {
      //beginTransaction();
      conn.setAutoCommit(false);
      checkUserStatement.clearParameters();
      checkUserStatement.setString(1, username);
      ResultSet result = checkUserStatement.executeQuery();

      if (initAmount < 0 || result.next()) {
        conn.rollback();
        //rollbackTransaction();
        return "Failed to create user\n";
      }

      result.close();
      createUserStatement.clearParameters();
      createUserStatement.setString(1, username);
      createUserStatement.setString(2, password);
      createUserStatement.setInt(3, initAmount);
      createUserStatement.execute();
      //commitTransaction();
      conn.commit();
      return "Created user " + username + "\n";
    } catch(SQLException e) {
      try {
        conn.rollback();
        //rollbackTransaction();
        return transaction_createCustomer(username, password, initAmount);
      } catch (SQLException e2) {
        return "Failed to create user\n";
      }
    }
    //return "Failed to create user\n";
  }

  /**
   * Implement the search function.
   *
   * Searches for flights from the given origin city to the given destination
   * city, on the given day of the month. If {@code directFlight} is true, it only
   * searches for direct flights, otherwise is searches for direct flights
   * and flights with two "hops." Only searches for up to the number of
   * itineraries given by {@code numberOfItineraries}.
   *
   * The results are sorted based on total flight time.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight if true, then only search for direct flights, otherwise include indirect flights as well
   * @param dayOfMonth
   * @param numberOfItineraries number of itineraries to return
   *
   * @return If no itineraries were found, return "No flights match your selection\n".
   * If an error occurs, then return "Failed to search\n".
   *
   * Otherwise, the sorted itineraries printed in the following format:
   *
   * Itinerary [itinerary number]: [number of flights] flight(s), [total flight time] minutes\n
   * [first flight in itinerary]\n
   * ...
   * [last flight in itinerary]\n
   *
   * Each flight should be printed using the same format as in the {@code Flight} class. Itinerary numbers
   * in each search should always start from 0 and increase by 1.
   *
   * @see Flight#toString()
   */
  public String transaction_search(String originCity, String destinationCity, boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries)
  {
    StringBuilder sb = new StringBuilder();
    List<Flight> flightList;
    Flight f;
    int remainingItin = numberOfItineraries;
    try{
      searchTransactionStatement1.clearParameters();
      searchTransactionStatement1.setInt(1, numberOfItineraries);
      searchTransactionStatement1.setString(2, originCity);
      searchTransactionStatement1.setString(3, destinationCity);
      searchTransactionStatement1.setInt(4, dayOfMonth);
      ResultSet result = searchTransactionStatement1.executeQuery();

      int itinerary_key = 0;
      while(result.next()) {
        flightList = new ArrayList<>();
        f = new Flight(result.getInt(1), result.getInt(2), result.getString(3), result.getString(4), result.getString(5), result.getString(6), result.getInt(7), result.getInt(8), result.getInt(9));
        flightList.add(f);
        itineraries.put(itinerary_key, flightList);
        itinerary_key++;
        remainingItin--;
      }

      if (!directFlight && remainingItin > 0) {
        searchTransactionStatement2.clearParameters();
        searchTransactionStatement2.setInt(1, numberOfItineraries);
        searchTransactionStatement2.setString(2, originCity);
        searchTransactionStatement2.setString(3, destinationCity);
        searchTransactionStatement2.setInt(4, dayOfMonth);
        result = searchTransactionStatement2.executeQuery();
        while(result.next()) {
          flightList = new ArrayList<>();
          for (int i = 0; i <= 1; i++) {
            int index = i * 9;
            int fid = result.getInt(index + 1);
            int day_of_month = result.getInt(index + 2);
            String carrier_id = result.getString(index + 3);
            String flight_num = result.getString(index + 4);
            String origin_city = result.getString(index + 5);
            String dest_city = result.getString(index + 6);
            int time = result.getInt(index + 7);
            int capacity = result.getInt(index + 8);
            int price = result.getInt(index + 9);
            f = new Flight(fid, day_of_month, carrier_id, flight_num, origin_city, dest_city, time, capacity, price);
            flightList.add(f);
          }
          itineraries.put(itinerary_key, flightList);
        }
        itinerary_key++;
      }
      result.close();

      if (itineraries.isEmpty()) {
        return "No flights match your selection\n";
      }
    } catch(SQLException e) {
      e.printStackTrace();
      return "Failed to search\n";
    }

    for (int i = 0; i < numberOfItineraries && i < itineraries.size(); i++) {
      flightList = itineraries.get(i);
      if (flightList.size() == 1) {
        f = flightList.get(0);
        sb.append("Itinerary " + i + ": " + flightList.size() + " flight(s), " + f.time + " minutes\n" + f.toString() + "\n");
      } else {
        Flight f1 = flightList.get(0);
        Flight f2 = flightList.get(1);
        sb.append("Itinerary " + i + ": " + flightList.size() + " flight(s), " + (f1.time  + f2.time + " minutes\n" + f1.toString() + "\n" + f2.toString() + "\n"));
      }
    }
    return sb.toString();
    //return transaction_search_unsafe(originCity, destinationCity, directFlight, dayOfMonth, numberOfItineraries);
  }

  /**
   * Same as {@code transaction_search} except that it only performs single hop search and
   * do it in an unsafe manner.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight
   * @param dayOfMonth
   * @param numberOfItineraries
   *
   * @return The search results. Note that this implementation *does not conform* to the format required by
   * {@code transaction_search}.
   */
  private String transaction_search_unsafe(String originCity, String destinationCity, boolean directFlight,
                                          int dayOfMonth, int numberOfItineraries)
  {
    StringBuffer sb = new StringBuffer();

    try
    {
      // one hop itineraries
      String unsafeSearchSQL =
              "SELECT TOP (" + numberOfItineraries + ") day_of_month,carrier_id,flight_num,origin_city,dest_city,actual_time,capacity,price "
                      + "FROM Flights "
                      + "WHERE origin_city = \'" + originCity + "\' AND dest_city = \'" + destinationCity + "\' AND day_of_month =  " + dayOfMonth + " "
                      + "ORDER BY actual_time ASC";

      Statement searchStatement = conn.createStatement();
      ResultSet oneHopResults = searchStatement.executeQuery(unsafeSearchSQL);

      while (oneHopResults.next())
      {
        int result_dayOfMonth = oneHopResults.getInt("day_of_month");
        String result_carrierId = oneHopResults.getString("carrier_id");
        String result_flightNum = oneHopResults.getString("flight_num");
        String result_originCity = oneHopResults.getString("origin_city");
        String result_destCity = oneHopResults.getString("dest_city");
        int result_time = oneHopResults.getInt("actual_time");
        int result_capacity = oneHopResults.getInt("capacity");
        int result_price = oneHopResults.getInt("price");

        sb.append("Day: " + result_dayOfMonth + " Carrier: " + result_carrierId + " Number: " + result_flightNum + " Origin: " + result_originCity + " Destination: " + result_destCity + " Duration: " + result_time + " Capacity: " + result_capacity + " Price: " + result_price + "\n");
      }
      oneHopResults.close();
    } catch (SQLException e) { e.printStackTrace(); }

    return sb.toString();
  }

  /**
   * Implements the book itinerary function.
   *
   * @param itineraryId ID of the itinerary to book. This must be one that is returned by search in the current session.
   *
   * @return If the user is not logged in, then return "Cannot book reservations, not logged in\n".
   * If try to book an itinerary with invalid ID, then return "No such itinerary {@code itineraryId}\n".
   * If the user already has a reservation on the same day as the one that they are trying to book now, then return
   * "You cannot book two flights in the same day\n".
   * For all other errors, return "Booking failed\n".
   *
   * And if booking succeeded, return "Booked flight(s), reservation ID: [reservationId]\n" where
   * reservationId is a unique number in the reservation system that starts from 1 and increments by 1 each time a
   * successful reservation is made by any user in the system.
   */
  public String transaction_book(int itineraryId)
  {
    try {
      //beginTransaction();
      conn.setAutoCommit(false);
      if (this.username == null) {
        conn.rollback();
        //rollbackTransaction();
        return "Cannot book reservations, not logged in\n";
      }

      if (!itineraries.containsKey(itineraryId))  {
        conn.rollback();
        //rollbackTransaction();
        return "No such itinerary " + itineraryId + "\n";
      } else {
        List<Flight> flightList = itineraries.get(itineraryId);
        Flight f1 = flightList.get(0);

        checkBookingStatement.clearParameters();
        checkBookingStatement.setString(1, this.username);
        checkBookingStatement.setInt(2, f1.dayOfMonth);
        ResultSet result = checkBookingStatement.executeQuery();

        if (result.next()) {
          conn.rollback();
          //rollbackTransaction();
          return "You cannot book two flights in the same day\n";
        }
        createReservationStatement.clearParameters();
        createReservationStatement.setString(1, this.username);
        createReservationStatement.setInt(2, f1.fid);
        if (flightList.size() > 1) {
          Flight f2 = flightList.get(1);
          createReservationStatement.setInt(3, f2.fid);
        } else {
          createReservationStatement.setNull(3, Types.INTEGER);
        }

        // Check for open spots on flight
        checkAvailableFlightStatement.clearParameters();
        checkAvailableFlightStatement.setInt(1, f1.fid);
        checkAvailableFlightStatement.setInt(2, f1.fid);
        result = checkAvailableFlightStatement.executeQuery();
        result.next();
        int spotsTaken = result.getInt("count");

        if (checkFlightCapacity(f1.fid) - spotsTaken < 1) {
          conn.rollback();
          //rollbackTransaction();
          return "Booking failed\n";
        }

        // Check the second flight
        if (flightList.size() == 2) {
          Flight f2 = flightList.get(1);
          checkAvailableFlightStatement.clearParameters();
          checkAvailableFlightStatement.setInt(1, f2.fid);
          checkAvailableFlightStatement.setInt(2, f2.fid);
          result = checkAvailableFlightStatement.executeQuery();
          result.next();
          spotsTaken = result.getInt("count");
          if (checkFlightCapacity(f2.fid) - spotsTaken < 1) {
            conn.rollback();
            //rollbackTransaction();
            return "Booking failed\n";
          }
        }
        createReservationStatement.execute();
        result = createReservationStatement.getGeneratedKeys();
        result.next();
        int reserveID = result.getInt(1);
        result.close();
        conn.commit();
        //commitTransaction();
        return "Booked flight(s), reservation ID: " + reserveID + "\n";

      }
    } catch (SQLException e) {
      try {
       e.printStackTrace();
       conn.rollback();
       //rollbackTransaction();
       return transaction_book(itineraryId);
      } catch (SQLException e2) {
        e.printStackTrace();
        return "Booking failed\n";
      }
    }

    //return "Boooking failed\n";
  }

  /**
   * Implements the reservations function.
   *
   * @return If no user has logged in, then return "Cannot view reservations, not logged in\n"
   * If the user has no reservations, then return "No reservations found\n"
   * For all other errors, return "Failed to retrieve reservations\n"
   *
   * Otherwise return the reservations in the following format:
   *
   * Reservation [reservation ID] paid: [true or false]:\n"
   * [flight 1 under the reservation]
   * [flight 2 under the reservation]
   * Reservation [reservation ID] paid: [true or false]:\n"
   * [flight 1 under the reservation]
   * [flight 2 under the reservation]
   * ...
   *
   * Each flight should be printed using the same format as in the {@code Flight} class.
   *
   * @see Flight#toString()
   */
  public String transaction_reservations()
  {
    StringBuilder sb = new StringBuilder();
    try {
      if (this.username == null) {
        return "Cannot view reservations, not logged in\n";
      }
      getReservationStatement.clearParameters();
      getReservationStatement.setString(1, this.username);
      ResultSet result = getReservationStatement.executeQuery();
      while (result.next()) {
        int reserveID = result.getInt("reserve_id");
        boolean paid = result.getInt("paid") == 1;
        sb.append("Reservation " + reserveID + " paid: " + paid + ":\n");

        Flight f1, f2;
        int fid1 = result.getInt("r_fid1");
        int fid2 = result.getInt("r_fid2");
        getFlightStatement.clearParameters();
        getFlightStatement.setInt(1, fid1);
        result = getFlightStatement.executeQuery();
        result.next();
        f1 = new Flight(result.getInt(1), result.getInt(2), result.getString(3), result.getString(4), result.getString(5), result.getString(6), result.getInt(7), result.getInt(8), result.getInt(9));
        sb.append(f1.toString()).append("\n");

        if (fid2 != 0) {
          getFlightStatement.clearParameters();
          getFlightStatement.setInt(1, fid2);
          result = getFlightStatement.executeQuery();
          result.next();
          f2 = new Flight(result.getInt(1), result.getInt(2), result.getString(3), result.getString(4), result.getString(5), result.getString(6), result.getInt(7), result.getInt(8), result.getInt(9));
          sb.append(f2.toString() + "\n");
        }
      }
      result.close();
    } catch (SQLException e) {
      e.printStackTrace();
      return "Failed to retrieve reservations\n";
    }

    if(sb.toString().equals("")){
        return "No reservations found\n";
    }
    return sb.toString();

    //return "Failed to retrieve reservations\n";
  }

  /**
   * Implements the cancel operation.
   *
   * @param reservationId the reservation ID to cancel
   *
   * @return If no user has logged in, then return "Cannot cancel reservations, not logged in\n"
   * For all other errors, return "Failed to cancel reservation [reservationId]"
   *
   * If successful, return "Canceled reservation [reservationId]"
   *
   * Even though a reservation has been canceled, its ID should not be reused by the system.
   */
  public String transaction_cancel(int reservationId)
  {

    // only implement this if you are interested in earning extra credit for the HW!
    try {
      conn.setAutoCommit(false);
      //beginTransaction();
      if (this.username == null) {
        conn.rollback();
        //rollbackTransaction();
        return "Cannot cancel reservation, not logged in\n";
      }
      checkPaidStatement.clearParameters();
      checkPaidStatement.setInt(1, reservationId);
      checkPaidStatement.setString(2, this.username);
      ResultSet result = checkPaidStatement.executeQuery();
      if (!result.next()) {
        conn.rollback();
       //rollbackTransaction();
        return "Failed to cancel reservation " + reservationId + "\n";
      }
      boolean paid = result.getInt("paid") == 1;
      int balance = result.getInt("balance");
      int fid1 = result.getInt("r_fid1");
      int fid2 = result.getInt("r_fid2");

      getFlightPriceStatement.clearParameters();
      getFlightPriceStatement.setInt(1, fid1);
      result = getFlightPriceStatement.executeQuery();
      result.next();
      int flight1price = result.getInt(1);
      int flight2price = 0;
      if (fid2 != 0) {
        getFlightPriceStatement.clearParameters();
        getFlightPriceStatement.setInt(1, fid2);
        result = getFlightPriceStatement.executeQuery();
        result.next();
        flight2price = result.getInt(1);
      }
      int itineraryPrice = flight1price + flight2price;

      if (paid) {
        balance += itineraryPrice;
        updateBalanceStatement.clearParameters();
        updateBalanceStatement.setInt(1, balance);
        updateBalanceStatement.setString(2, this.username);
        updateBalanceStatement.execute();
      }
      removeReservationStatement.clearParameters();
      removeReservationStatement.setInt(1, reservationId);
      removeReservationStatement.execute();
      conn.commit();
      //commitTransaction();
      return "Canceled reservation " + reservationId + "\n";
    } catch (SQLException e) {
      try {
        e.printStackTrace();
        conn.rollback();
        //rollbackTransaction();
        return transaction_cancel(reservationId);
      } catch (SQLException e2) {
        e.printStackTrace();
        return "Failed to cancel reservation " + reservationId + "\n";
      }
    }

    //return "Failed to cancel reservation " + reservationId;
  }

  /**
   * Implements the pay function.
   *
   * @param reservationId the reservation to pay for.
   *
   * @return If no user has logged in, then return "Cannot pay, not logged in\n"
   * If the reservation is not found / not under the logged in user's name, then return
   * "Cannot find unpaid reservation [reservationId] under user: [username]\n"
   * If the user does not have enough money in their account, then return
   * "User has only [balance] in account but itinerary costs [cost]\n"
   * For all other errors, return "Failed to pay for reservation [reservationId]\n"
   *
   * If successful, return "Paid reservation: [reservationId] remaining balance: [balance]\n"
   * where [balance] is the remaining balance in the user's account.
   */
  public String transaction_pay (int reservationId)
  {

    try {
      conn.setAutoCommit(false);
      //beginTransaction();
      if (this.username == null) {
        conn.rollback();
        //rollbackTransaction();
        return "Cannot pay, not logged in\n";
      }
      payReservationStatement.clearParameters();
      payReservationStatement.setInt(1, reservationId);
      payReservationStatement.setString(2, this.username);
      ResultSet result = payReservationStatement.executeQuery();
      if (!result.next()) {
        conn.rollback();
        //rollbackTransaction();
        return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
      }
      int fid1 = result.getInt("r_fid1");
      int fid2 = result.getInt("r_fid2");
      int balance = result.getInt("balance");

      getFlightPriceStatement.clearParameters();
      getFlightPriceStatement.setInt(1, fid1);
      result = getFlightPriceStatement.executeQuery();
      result.next();
      int flight1price = result.getInt(1);
      int flight2price = 0;
      if (fid2 != 0) {
        getFlightPriceStatement.clearParameters();
        getFlightPriceStatement.setInt(1, fid2);
        result = getFlightPriceStatement.executeQuery();
        result.next();
        flight2price = result.getInt(1);
      }
      int itineraryPrice = flight1price + flight2price;
      if (itineraryPrice > balance) {
        rollbackTransaction();
        return "User has only " + balance + " in account but itinerary costs " + itineraryPrice + "\n";
      }
      balance -= itineraryPrice;
      updateBalanceStatement.clearParameters();
      updateBalanceStatement.setInt(1, balance);
      updateBalanceStatement.setString(2, this.username);
      updateBalanceStatement.execute();

      updatePaidStatement.clearParameters();
      updatePaidStatement.setInt(1, reservationId);
      updatePaidStatement.execute();
      conn.commit();
      //commitTransaction();
      return "Paid reservation: " + reservationId + " remaining balance: " + balance + "\n";
    } catch (SQLException e) {
      try {
        e.printStackTrace();
        conn.rollback();
        //rollbackTransaction();
        return transaction_pay(reservationId);
      } catch (SQLException e2) {
        e.printStackTrace();
        return "Failed to pay for reservation " + reservationId + "\n";
      }
    }

    //return "Failed to pay for reservation " + reservationId + "\n";
  }

  /* some utility functions below */

  public void beginTransaction() throws SQLException
  {
    conn.setAutoCommit(false);
    beginTransactionStatement.executeUpdate();
  }

  public void commitTransaction() throws SQLException
  {
    commitTransactionStatement.executeUpdate();
    conn.setAutoCommit(true);
  }

  public void rollbackTransaction() throws SQLException
  {
    rollbackTransactionStatement.executeUpdate();
    conn.setAutoCommit(true);
  }

  /**
   * Shows an example of using PreparedStatements after setting arguments. You don't need to
   * use this method if you don't want to.
   */
  private int checkFlightCapacity(int fid) throws SQLException
  {
    checkFlightCapacityStatement.clearParameters();
    checkFlightCapacityStatement.setInt(1, fid);
    ResultSet results = checkFlightCapacityStatement.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }
}
