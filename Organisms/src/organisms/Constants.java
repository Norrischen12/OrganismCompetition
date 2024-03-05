package organisms;

public interface Constants {
    int MIN_EXTERNAL_STATE = 0;
    int MAX_EXTERNAL_STATE = 255;

    enum Action {
        STAY_PUT(0),
        WEST (1),
        EAST (2),
        NORTH (3),
        SOUTH (4),
        REPRODUCE (5);

        private final int intValue;
        private static final Action[] allActions = values();
        private static final int numActions = allActions.length;

        Action(int intValue) {
            this.intValue = intValue;
        }

        public static int getNumActions() {
            return numActions;
        }

        public int intValue() {
            return this.intValue;
        }

        public static Action fromInt(int intValue) {
            return allActions[intValue];
        }
    }
    int[] _CXTrans     = { 0, -1, 1, 0, 0};
    int[] _CYTrans     = { 0, 0, 0, -1, 1};
}
