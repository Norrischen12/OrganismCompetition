//***********************************************************
//*
//* File:           Util.java
//* Author:         Abhinav Kamra
//* Contact:        kamra-at-cs.columbia.edu
//* Update:         9.14.2003
//*
//* Description:    Static utility methods object
//*
//***********************************************************

package organisms.ui;

import java.util.*;
import java.io.*;

public final class Util {

    public static String adjustString(String __str, int __amount) throws Exception {
        StringBuffer SB;
        int length = __str.length();
        int _MAX;

        if (length > __amount) {
            return __str.substring(0, __amount);
        }

        if (length < __amount) {
            SB = new StringBuffer(__str);
            _MAX = __amount - length;
            for (int i=0; i < _MAX; i++) {
                SB.append(" ");
            }
            return new String(SB);
        }

        return __str;
    }

    public static int[] maxIndex(double[] __array) throws Exception {
        ArrayList best_list = new ArrayList();
        double best_val=Double.NEGATIVE_INFINITY;
        double val;
        int[] RET;
        int _MAX = __array.length;

        for (int i=0; i < _MAX; i++) {
            val = __array[i];
            if (val > best_val) {
                best_val = val;
            }
        }
        for (int i=0; i < _MAX; i++) {
            if (__array[i] == best_val) {
                best_list.add(i);
            }
        }

        _MAX = best_list.size();
        RET = new int[_MAX];
        for (int i=0; i < _MAX; i++) {
            RET[i] = ((Integer) best_list.get(i)).intValue();
        }
        return RET;
    }
    
    public static double[] ranks(Comparable[] __array) throws Exception {
        int _MAX;
        double[] RET;
        int rank=1;
        int j;

        Arrays.sort(__array);
        _MAX = __array.length;
        RET = new double[_MAX];
        for (int i=0; i < _MAX; ) {
            for (j=i+1; j < _MAX; j++) {
                if (__array[i].compareTo(__array[j]) == 0) {
                    continue;
                } else {
                    break;
                }
            }
            for (int k=i; k < j;  k++) {
		/* Kamra - Change */
		// Giving Average Rank when players tie
		RET[k] = rank + ((double)(j-i-1))/2.0;
//                RET[k] = rank;
            }
            rank += j-i;
            i = j;
        }
        return RET;
    }

    public static Properties gatherProperties(String __filename) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(__filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Properties RET = new Properties();
        try {
            RET.load(fis);
            fis.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        return RET;
    }
    
    public static String[][] readCSV(String filename) throws Exception {
    	BufferedReader br = new BufferedReader(new FileReader(filename));
    	// Skipping first line
    	String line = br.readLine();
    	ArrayList<String[]> list = new ArrayList<String[]>();
    	
    	while ((line = br.readLine()) != null) {
    		String[] tokens = line.split(",");
    		list.add(tokens);
    	}
		br.close();
    	return (String[][]) list.toArray(new String[0][0]);
    }

    public static String[] split(String __delim, String __str) {
        ArrayList list = new ArrayList();
        StringTokenizer st = new StringTokenizer(__str, __delim);

        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        return (String[]) list.toArray(new String[0]);
    }

    public static class ClassSorter implements Comparator {

        public int compare(Object __obj1, Object __obj2) {
            try {
                return ((Class) __obj1).toString().compareTo(((Class) __obj2).toString());
            } catch (Exception EXC) {
                System.out.println(EXC.getMessage());
                EXC.printStackTrace();
                return 0;
            }
         }
    }
}
