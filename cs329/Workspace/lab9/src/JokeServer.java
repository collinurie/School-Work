
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A knock knock joke / riddle server.
 */
public class JokeServer {

	private static final int PORT_ = 9999;
	private static List<KnockKnockJoke> knockknocks;
	private static List<Riddle> riddles;
	private static ServerSocket listener;
	private static boolean shutdown_ = false;
	private static ArrayList<Thread> threads;

	/**
	 * Set up knock knock jokes.
	 */
	private static List<KnockKnockJoke> setupKnockKnocks() {
		List<KnockKnockJoke> knockknocks = new ArrayList<KnockKnockJoke>();

		knockknocks.add(new KnockKnockJoke("orange", "Orange you glad you asked?"));
		knockknocks.add(new KnockKnockJoke("cash", "No thanks, but I would like a peanut instead."));
		knockknocks.add(new KnockKnockJoke("rhino", "Rhino every knock knock joke there is."));

		return knockknocks;
	}

	/**
	 * Set up riddles.
	 */
	private static List<Riddle> setupRiddles() {
		List<Riddle> riddles = new ArrayList<Riddle>();

		riddles.add(new Riddle("What has roots as nobody sees, " + "is taller than trees, " + "up, up it goes, "
				+ "and yet never grows?", "a mountain"));
		riddles.add(new Riddle("Thirty white horses on a red hill, " + "first they champ, " + "then they stamp, "
				+ "then they stand still.", "teeth"));
		riddles.add(new Riddle(
				"Voiceless it cries, " + "wingless flutters, " + "toothless bites, " + "mouthless mutters.", "wind"));
		riddles.add(new Riddle("An eye in a blue face saw an eye in a green face.  "
				+ "\"That eye is like to this eye\" said the first eye, " + "\"But in low place, not in high place.\"",
				"the sun"));
		riddles.add(new Riddle("It cannot be seen, cannot be felt, " + "cannot be heard, cannot be smelt.  "
				+ "It lies behind stars and under hills, and empty holes it fills.  "
				+ "It comes first and follows after, ends life, kills laughter.", "dark"));
		riddles.add(new Riddle("A box without hinges, key, or lid, " + "yet golden treasure inside is hid.", "an egg"));
		riddles.add(new Riddle("Alive without breath, as cold as death; "
				+ "never thirsty, ever drinking, all in mail never clinking.", "fish"));
		riddles.add(new Riddle(
				"This thing all things devours: " + "Birds, beasts, trees, flowers; " + "gnaws iron, bites steel; "
						+ "grinds hard stones to meal; " + "slays king, ruins town, " + "and beats high mountain down.",
				"time"));

		return riddles;
	}

	/**
	 * Main server.
	 */
	public static void main(String[] args) {

		// set up jokes and riddles
		knockknocks = setupKnockKnocks();
		riddles = setupRiddles();

		threads = new ArrayList<Thread>();
		
		try {

			// get connection
			listener = new ServerSocket(PORT_);
			System.out.println("Listening for connection on port: " + PORT_);
			Socket connection = null;
			while (!shutdown_) {
				try {
				connection = listener.accept();
				}catch (IOException e) {
					break;
				}
				// System.out.println("connection made with: "+ connection.getLocalAddress());
				// ClientServicing client = new ClientServicing(connection,
				// knockknocks,riddles);
				// client.run();
				ClientServicing client = new ClientServicing(connection, knockknocks, riddles);
				Thread t = new Thread(client);
			
				t.start();
				
				threads.add(t);
				
			}
			
			for(int i = 0; i < threads.size(); i++) {
				try {
					threads.get(i).join(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Exiting program.");
		System.exit(0);

	}

	public static void shutdown() throws IOException{
		shutdown_ = true;
		//try {
			listener.close();
		//} catch (IOException e) {
			//e.printStackTrace();
		//}
	}

}
