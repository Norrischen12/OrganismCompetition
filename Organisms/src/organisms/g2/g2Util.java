package organisms.g2;

public class g2Util {
    public static int[] convertToBinary(int num) {
        int[] binaryArray = new int[32];
        for (int i = 31; i >= 0; i--) {
            binaryArray[i] = (num >> (31 - i)) & 1;
        }
        return binaryArray;
    }

    /**
     *
     * @param number; external #
     * @return createsHash based on arbitrary hash value
     */
    public static int setDNA(int number) {
        int hash = 16; // Change hash value to 16
        int mask = 0b1111000000000000; // Change the mask to 0b1111000000000000
        int clearedNumber = number & ~mask;
        return clearedNumber | (hash << 12); // Shift 'hash' by 12 bits
    }

    /**
     *
     * @param number; external #
     * @param brain; brain desired
     * @return number
     */
    public static int setRole(int number, int brain) {
        int mask = 0b0000111100000000; // Change the mask to 0b0000111100000000
        int clearedNumber = number & ~mask;
        return clearedNumber | (brain << 8); // Shift 'brain' by 8 bits
    }

    /**
     *
     * @param number; external #
     * @return number
     */
    public static int setMovement(int number) {
        int mask = 0b0000000011111111; // Change the mask to 0b0000000011111111
        int clearedNumber = number & ~mask;
        int movement = (int) (System.currentTimeMillis() + getMovement(number)) / (60000);
        if (movement >= 10 && movement <= 20) {
            number = setRole(number, 1);
        } else if (movement >= 20 && movement <= 30) {
            number = setRole(number, 2);
        }
        return clearedNumber | (number); // No need to shift 'movement'
    }

    public static int getRole(int number) {
        return (number >> 8) & 0xFF; // Adjust the mask to 0xFF
    }

    public static int getDNA(int number) {
        return (number >> 12) & 0xF; // Adjust the mask to 0xF
    }

    public static int getMovement(int number) {
        return number & 0xFF; // Adjust the mask to 0xFF
    }
}