package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import rental.CarType;
import rental.Reservation;
import rental.ReservationConstraints;
import session.CarRentalSessionRemote;
import session.ManagerSessionRemote;

public class Main extends AbstractTestManagement<CarRentalSessionRemote, ManagerSessionRemote> {

    @EJB
    static CarRentalSessionRemote m_carRentalSession;
    @EJB
    static ManagerSessionRemote m_managerSession;
    
    static InitialContext m_context;
    
    public Main(String scriptFile) {
        super(scriptFile);
        
        loadRental("Hertz", "hertz.csv");
        loadRental("Dockx", "dockx.csv");
    }

    public static void main(String[] args) throws Exception {
        
        m_context = new InitialContext();
        // TODO: use updated manager interface to load cars into companies
        Main m = new Main("trips");
        //m.run();
    }
    
    @Override
    protected CarRentalSessionRemote getNewReservationSession(String name) throws Exception {
        CarRentalSessionRemote out = (CarRentalSessionRemote) new InitialContext().lookup(CarRentalSessionRemote.class.getName());
        out.setRenterName(name);
        return out;
    }

    @Override
    protected ManagerSessionRemote getNewManagerSession(String name, String carRentalName) throws Exception {
        ManagerSessionRemote out = (ManagerSessionRemote) new InitialContext().lookup(ManagerSessionRemote.class.getName());
        return out;
    }
    
    @Override
    protected void checkForAvailableCarTypes(CarRentalSessionRemote session, Date start, Date end) throws Exception {
        System.out.println("Available car types between "+start+" and "+end+":");
        for(CarType ct : session.getAvailableCarTypes(start, end))
            System.out.println("\t"+ct.toString());
        System.out.println();
    }

    @Override
    protected void addQuoteToSession(CarRentalSessionRemote session, String name, Date start, Date end, String carType, String carRentalName) throws Exception {
        session.createQuote(carRentalName, new ReservationConstraints(start, end, carType));
    }

    @Override
    protected List<Reservation> confirmQuotes(CarRentalSessionRemote session, String name) throws Exception {
        return session.confirmQuotes();
    }
    
    @Override
    protected int getNumberOfReservationsBy(ManagerSessionRemote ms, String renterName) throws Exception {
        return ms.getNumberOfReservationsBy(renterName);
    }

    @Override
    protected int getNumberOfReservationsForCarType(ManagerSessionRemote ms, String name, String carType) throws Exception {
        return ms.getNumberOfReservations(name, carType);
    }

    @Override
    protected String getCheapestCarType(CarRentalSessionRemote session, Date start, Date end) throws Exception {
        System.err.println("To be implemented.");
        return null;
    }

    @Override
    protected String getMostPopularCarRentalCompany(ManagerSessionRemote ms) throws Exception {
        System.err.println("To be implemented.");
        return null;
    }
    
    /*
    METHODS TO READ IN DATA
    */
       
    
    public static void loadData(String datafile, String companyName) throws NumberFormatException, IOException {
        ManagerSessionRemote session;
        try {
            session = (ManagerSessionRemote) m_context.lookup(ManagerSessionRemote.class.getName());
        } catch (NamingException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        //open file from jar
        BufferedReader in = new BufferedReader(new InputStreamReader(Main.class.getClassLoader().getResourceAsStream(datafile)));
        //while next line exists
        while (in.ready()) {
            //read line
            String line = in.readLine();
            //if comment: skip
            if (line.startsWith("#")) {
                continue;
            }
            //tokenize on ,
            StringTokenizer csvReader = new StringTokenizer(line, ",");
            //create new car type from first 5 fields
            CarType type = new CarType(csvReader.nextToken(),
                    Integer.parseInt(csvReader.nextToken()),
                    Float.parseFloat(csvReader.nextToken()),
                    Double.parseDouble(csvReader.nextToken()),
                    Boolean.parseBoolean(csvReader.nextToken()));
            //session.addCarType(type.getName(), type.getNbOfSeats(), type.getTrunkSpace(), type.getRentalPricePerDay(), true, companyName);
            //create N new cars with given type, where N is the 5th field
            for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
                session.addCar(type, companyName);
            }
        }
    }
    
    public static void loadRental(String name, String datafile) {
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "loading {0} from file {1}", new Object[]{name, datafile});
        try {
            ManagerSessionRemote session = (ManagerSessionRemote) m_context.lookup(ManagerSessionRemote.class.getName());
            session.addCompany(name);
            loadData(datafile, name);
        } catch (NumberFormatException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "bad file", ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}