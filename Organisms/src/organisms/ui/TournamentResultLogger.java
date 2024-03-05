package organisms.ui;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class TournamentResultLogger {
   private static PrintWriter pw;
   
   public static void setUp(String filename) {
	   try {
		   pw = new PrintWriter(filename);
	   } catch (FileNotFoundException e) {
			e.printStackTrace();
	   }
   }
   
   public static synchronized void print(String message) {
	   pw.append(message);
	   pw.flush();
   }
}