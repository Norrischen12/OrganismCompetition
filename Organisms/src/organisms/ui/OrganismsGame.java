package organisms.ui;

import organisms.Constants;
import organisms.Move;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static organisms.Constants.Action.STAY_PUT;

// for graphing


public final class OrganismsGame extends IFCModel implements Constants {

    Vector roundList;  // all the rounds and what players looked like on them
    GraphDrawer graph;
    boolean shouldGraph;

    private static final boolean fightingAllowed = false;
    private static int[][] food;

    private Class[] _classlist;
    private Class[] _playerlist;
    private int _currRound;
    private int _maxrounds;
    private int _numplayers;
    private int _state;
    private transient IFCUI _ui;
    private IFCConfiguration _config;
    private static Random _random;
    private JTextField _input;
    private ControlPanel _control;
    private ViewPanel _view;
    private boolean _registered;
    private static final int _CMOVING = 0;
    private static final int _CWAITING = 1;
    private static final int _CFINISHED = 2;
    private static final int[] _CSTATES = {_CMOVING, _CWAITING, _CFINISHED};
    private static final String _CNAME = "CIS 559 - Spring 2023 - Organisms 1" +
        ".0";
    private static final String _CPROPERTIES_FILE = "gamemodel.properties";
    private static final String _TOURNAMENT_FILE = "tournament.csv";
    private static final int _CMIN_ROUNDS = 1;
    private static final int _CMAX_ROUNDS = 500000;
    private static final int _CMIN_PLAYERS = 1;
    private static final int _CMAX_PLAYERS = 100;
    private static final int _CMIN_X = 1;
    private static final int _CMAX_X = 1000;
    private static final int _CMIN_Y = 1;
    private static final int _CMAX_Y = 1000;
    private static final int _CMIN_v = 1;
    private static final int _CMAX_v = 1000;
    private static final int _CMIN_u = 1;
    private static final int _CMAX_u = 1000;
    private static final int _CMIN_M = 1;
    private static final int _CMAX_M = 5000;
    private static final int _CMIN_K = 1;
    private static final int _CMAX_K = 1000;
    private static final double _CMIN_p = 0.0;
    private static final double _CMAX_p = 1.0;
    private static final double _CMIN_q = 0.0;
    private static final double _CMAX_q = 1.0;

    private HashMap<Class, FightResults> fightResults;

    class FightResults {
        int attackWin, attackLoss, defendWin, defendLoss;
    }

    class PlayerEntry {
        Class _playerclass;
        String _name;
        Color _color;
        int _population;
        int _totalenergy;
        boolean isExtinct = false;

        PlayerEntry(Class __class, String __name, Color __color) {
            _population = 1;
            _playerclass = __class;
            _name = __name;
            _color = __color;
            _totalenergy = 0;
        }

        double score() {
            return (_population <= 0) ? (0.0) : (_totalenergy / _population);
        }

        String name() {
            return _name;
        }

        Color color() {
            return _color;
        }

        Class playerclass() {
            return _playerclass;
        }

        int population() {
            return _population;
        }

        int totalenergy() {
            return _totalenergy;
        }

        void AddPop(int increment) {
            _population += increment;
        }

        void AddEnergy(int add) {
            _totalenergy += add;
        }
    }

    class Cell {
        int foodvalue;
        int playertype;
        public PlayerWrapper pw;
        boolean movedone;
    }

    private int population;    // Total number of amoebae on the grid

    private int init_energy;    // Initial Energy of an Organism
    private int X;    // Grid Width
    private int Y;    // Grid Height
    private int M;    // Maximum energy per organism
    private int K;    // Maximum Food per cell
    private int s;    // Enery consumed in staying put
    private int v;    // Energy consumed in moving / reproducing
    private int u;    // Energy per unit food
    private double p;    // Food Generation probability
    private double q;    // Food Doubling probability
    private Cell[][] cells;    // Grid
    private PlayerEntry[] OrigPlayers;    // Player Classes

    // variables for tournament stat tracking
    // length is equal to the numberof players
    private int[] extinctionTimes; // will start with all -1
    private long[] energyWhileAlive; // sums; will be turned into averages
    private long[] populationWhileAlive; // sums; will be turned into averages
    private int[] endCounts;
    private int[] endEnergies;

    //********************************************
    //*
    //* Constructors
    //*
    //********************************************
    public OrganismsGame() throws Exception {
        create(createDefaultConfiguration());
    }

    public OrganismsGame(IFCConfiguration __config) {
        roundList = new Vector();
        shouldGraph = false;
        create(__config);
    }

    public OrganismsGame(IFCTournament __tournament) {
        // this isn't used anywhere as far as I know
        run(__tournament);
    }

    public OrganismsGame(String[] csvConfig, Class[] classes) {
        roundList = new Vector();
        shouldGraph = false;
        IFCConfiguration config = getConfigFromCSVRow(csvConfig, classes);
        create(config);
        TournamentStopListener tsl = new TournamentStopListener(this,
            csvConfig);
        tsl.start();
    }

    // tournament runner to hit the CPU as hard as possible
    public static String runTournament(String[] csvConfig, Class[] classes) {
        System.out.println("Starting config " + csvConfig[12]);
        IFCConfiguration config = getConfigFromCSVRow(csvConfig, classes);
        int trials = Integer.parseInt(csvConfig[11]);
        List<OrganismsGame> games = new ArrayList<>(trials);

        for (int i = 0; i < trials; i++) {
            games.add(new OrganismsGame(config));
        }

        games.parallelStream().forEach(OrganismsGame::runGame);

        double[] averageActiveEnergy = longAverages(
            games, g -> g.energyWhileAlive);
        double[] averageActivePopulation = longAverages(games,
            g -> g.populationWhileAlive);
        double[] averageEndEnergy = intAverages(games, g -> g.endEnergies);
        double[] averageEndPopulation = intAverages(games, g -> g.endCounts);

        int[] extinctionCounts = new int[config.numPlayers()];
        int[] cohabitationCounts = new int[config.numPlayers()];
        games.forEach(game -> {
            List<Integer> indicesOfLivingPlayers = new ArrayList<>();
            for (int i = 0; i < game.numPlayers(); i++) {
                if (game.OrigPlayers[i]._population == 0) {
                    extinctionCounts[i]++;
                } else {
                    indicesOfLivingPlayers.add(i);
                }
            }
            if (indicesOfLivingPlayers.size() > 1) {
                indicesOfLivingPlayers.forEach(idx -> {
                    cohabitationCounts[idx]++;
                });
            }
        });

        int[] extinctionTimeSums = intSums(games, g -> g.extinctionTimes);
        double[] averageExtinctionTimes = new double[config.numPlayers()];
        for (int i = 0; i < config.numPlayers(); i++) {
            averageExtinctionTimes[i] = extinctionTimeSums[i] * 1.0 /
                extinctionCounts[i];
        }

        StringBuilder sb = new StringBuilder();
        sb.append("================================================\n");
        sb.append(csvConfig[12]).append('\n');
        sb.append("Trials: ").append(trials).append('\n');

        int maxNameLength = 6;
        for (Class c : classes) {
            int currLength = c.getName().length();
            if (currLength > maxNameLength) {
                maxNameLength = currLength;
            }
        }

        sb.append(padRight("Player", maxNameLength));

        String[] columns = {"Avg count (end)", "Avg energy (end)",
                "# extinctions", "# successful cohabitations", "Avg extinction time",
                "Avg energy while alive", "Avg population while alive"};

        for (String column : columns) {
            sb.append(" | ");
            sb.append(column);
        }
        sb.append('\n');

        String[] formats = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            int width = columns[i].length();
            formats[i] = "%" + width + ".2f";
        }

        for (int i = 0; i < config.numPlayers(); i++) {
            sb.append(padRight(classes[i].getName(), maxNameLength));
            sb.append(" | ");
            sb.append(String.format(formats[0], averageEndPopulation[i]));
            sb.append(" | ");
            sb.append(String.format(formats[1], averageEndEnergy[i]));
            sb.append(" | ");
            sb.append(String.format("%" + columns[2].length() + "d",
                extinctionCounts[i]));
            sb.append(" | ");
            sb.append(String.format("%" + columns[3].length() + "d",
                    cohabitationCounts[i]));
            sb.append(" | ");
            sb.append(String.format(formats[4], averageExtinctionTimes[i]));
            sb.append(" | ");
            sb.append(String.format(formats[5], averageActiveEnergy[i]));
            sb.append(" | ");
            sb.append(String.format(formats[6], averageActivePopulation[i]));
            sb.append("\n");
        }

        return sb.toString();
    }

    private static int[] intSums(List<OrganismsGame> completedGames,
                                  Function<OrganismsGame, int[]> mapper) {
        Optional<int[]> acc = completedGames.parallelStream().map(mapper)
            .reduce((dubs, dubs2) -> {
                    for (int i = 0; i < dubs.length; i++) {
                        dubs[i] += dubs2[i];
                    }
                    return dubs;
                }
            );
        if (acc.isEmpty()) {
            throw new RuntimeException("Error while accumulating game stats " +
                "into averages");
        } else {
            return acc.get();
        }
    }

    private static double[] intAverages(List<OrganismsGame> completedGames,
                                        Function<OrganismsGame, int[]> mapper) {
        int[] totals = intSums(completedGames, mapper);
        double[] averages = new double[totals.length];
        for (int i = 0; i < totals.length; i++) {
            averages[i] = totals[i] * 1.0 / completedGames.size();
        }
        return averages;
    }

    private static long[] longSums(List<OrganismsGame> completedGames,
                                   Function<OrganismsGame, long[]> mapper) {
        Optional<long[]> acc = completedGames.parallelStream().map(mapper)
            .reduce((dubs, dubs2) -> {
                    for (int i = 0; i < dubs.length; i++) {
                        dubs[i] += dubs2[i];
                    }
                    return dubs;
                }
            );
        if (acc.isEmpty()) {
            throw new RuntimeException("Error while accumulating game stats " +
                "into averages");
        } else {
            return acc.get();
        }
    }

    private static double[] longAverages(
            List<OrganismsGame> completedGames,
            Function<OrganismsGame, long[]> mapper) {
        long[] totals = longSums(completedGames, mapper);
        double[] averages = new double[totals.length];
        for (int i = 0; i < totals.length; i++) {
            averages[i] = totals[i] * 1.0 / completedGames.size();
        }
        return averages;
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    //********************************************
    //*
    //* Constructor Delegates
    //*
    //********************************************
    public void run(IFCTournament __tournament) {

        IFCGameRecord[] games;

        _random = ThreadLocalRandom.current();

        _maxrounds = _config.numRounds();
        init_energy = _config.getInitEnergy();
        X = _config.getGridX();
        Y = _config.getGridY();
        M = _config.M();
        K = _config.K();
        v = _config.getV();
        u = _config.getU();
        p = _config.getP();
        q = _config.getQ();
        s = _config.getS();

        // TODO: tournament stat trackers should be initialized and used
        //   But this method is never used in any meaningful way so that's
        //   a problem for someone else

        cells = new Cell[X][];
        for (int i = 0; i < X; i++) {
            cells[i] = new Cell[Y];
            for (int j = 0; j < Y; j++)
                cells[i][j] = new Cell();
        }


        games = __tournament.games();
        if (games == null) {
            throw new RuntimeException("Error:  Null game record list");
        }

        for (int game = 0; game < games.length; game++) {
            population = 0;
            _playerlist = games[game].players();
            _numplayers = _playerlist.length;
            if (X * Y < _numplayers)
                throw new RuntimeException("More players than Space on Grid");
            for (int i = 0; i < X; i++) {
                for (int j = 0; j < Y; j++) {
                    cells[i][j].foodvalue = 0;
                    cells[i][j].pw = null;
                }
            }
            OrigPlayers = new PlayerEntry[_numplayers];
            for (int i = 0; i < _numplayers; i++) {
                int x, y;
                while (true) {
                    x = _random.nextInt(X);
                    y = _random.nextInt(Y);
                    if (cells[x][y].pw == null)
                        break;
                }
                population++;
                cells[x][y].pw = new PlayerWrapper(_playerlist[i]);
                cells[x][y].pw.register(this, -1);
                cells[x][y].foodvalue = 0;
                cells[x][y].playertype = i;
                OrigPlayers[i] = new PlayerEntry(_playerlist[i],
                    cells[x][y].pw.name(), cells[x][y].pw.color());
                ChangeEnergy(x, y, init_energy);
            }

            _currRound = 0;
            _state = _CMOVING;

            while (step()) {
            }
            games[game].setScores(finalScores());
        }

    } // end -- run

    double[] finalScores() {
        double[] RET = new double[OrigPlayers.length];
        for (int i = 0; i < OrigPlayers.length; i++)
            RET[i] = OrigPlayers[i].score();
        return RET;
    }

    void create(IFCConfiguration __config) {
        System.out.println("==============================");

        _random = ThreadLocalRandom.current();
        _config = __config;

        fightResults = new HashMap<Class, FightResults>();


        population = 0;
        _maxrounds = _config.numRounds();
        init_energy = _config.getInitEnergy();
        X = _config.getGridX();
        Y = _config.getGridY();
        M = _config.M();
        K = _config.K();
        v = _config.getV();
        u = _config.getU();
        p = _config.getP();
        q = _config.getQ();
        s = _config.getS();

        _classlist = _config.getClassList();
        _playerlist = _config.playerList();
        _numplayers = _playerlist.length;

        // Leaving these at 0 shouldn't cause any problems, since the only
        // "inaccurate" case is when players start with 0 energy, but it's still
        // accurate in that case
        this.extinctionTimes = new int[this._numplayers];
        this.energyWhileAlive = new long[this._numplayers];
        this.populationWhileAlive = new long[this._numplayers];
        this.endCounts = new int[this._numplayers];
        this.endEnergies = new int[this._numplayers];

        if (X * Y < _numplayers)
            throw new RuntimeException("More players than Space on Grid");
        cells = new Cell[X][];
        for (int i = 0; i < X; i++) {
            cells[i] = new Cell[Y];
            for (int j = 0; j < Y; j++)
                cells[i][j] = new Cell();
        }
        food = new int[X][Y];

        OrigPlayers = new PlayerEntry[_numplayers];
        for (int i = 0; i < _numplayers; i++) {
            int x, y;
            while (true) {
                x = _random.nextInt(X);
                y = _random.nextInt(Y);
                if (cells[x][y].pw == null)
                    break;
            }
            population++;
            cells[x][y].pw = new PlayerWrapper(_playerlist[i]);
            cells[x][y].pw.register(this, -1);
            cells[x][y].foodvalue = 0;

            cells[x][y].playertype = i;

            OrigPlayers[i] = new PlayerEntry(_playerlist[i],
                cells[x][y].pw.name(), cells[x][y].pw.color());
            ChangeEnergy(x, y, init_energy);
            fightResults.put(OrigPlayers[i]._playerclass, new FightResults());
        }

        _currRound = 0;
        _state = _CMOVING;
        //_history = new ArrayList();
    }

    void initializePanels() throws Exception {
        _control = new ControlPanel();
        _view = new ViewPanel();
    }


    //********************************************
    //*
    //* Initial Configuration
    //*
    //********************************************
    public IFCConfiguration createDefaultConfiguration() throws Exception {
        IFCConfiguration RET = new Configuration();
        String[] toks;
        Class[] classes;
        Class[] players;
        int _MAX;
        Properties properties;
        Random random = ThreadLocalRandom.current();
        ParseValue pv;

        RET.setNumRoundsBounds(_CMIN_ROUNDS, _CMAX_ROUNDS);
        RET.setNumPlayersBounds(_CMIN_PLAYERS, _CMAX_PLAYERS);
        RET.setXBounds(_CMIN_X, _CMAX_X);
        RET.setYBounds(_CMIN_Y, _CMAX_Y);
        RET.setVBounds(_CMIN_v, _CMAX_v);
        RET.setUBounds(_CMIN_u, _CMAX_u);
        RET.setMBounds(_CMIN_M, _CMAX_M);
        RET.setKBounds(_CMIN_K, _CMAX_K);
        RET.setPBounds(_CMIN_p, _CMAX_p);
        RET.setQBounds(_CMIN_q, _CMAX_q);

        properties = Util.gatherProperties(_CPROPERTIES_FILE);
        RET.setLogFile(properties.getProperty("LOG_FILE").trim());

        pv = ParseValue.parseIntegerValue(
            properties.getProperty("INIT_ENERGY").trim(), _CMIN_M, _CMAX_M);
        if (!pv.isValid()) {
            throw new Exception(
                "Properties parameter out of range, Init Energy");
        }
        RET.setInitEnergy(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(
            properties.getProperty("MAX_ROUNDS").trim(), _CMIN_ROUNDS,
            _CMAX_ROUNDS);
        if (!pv.isValid()) {
            throw new Exception(
                "Properties parameter out of range, Number of Rounds");
        }
        RET.setNumRounds(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(properties.getProperty("X").trim(),
            _CMIN_X, _CMAX_X);
        if (!pv.isValid()) {
            throw new Exception("Properties parameter out of range, X");
        }
        RET.setGridX(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(properties.getProperty("Y").trim(),
            _CMIN_Y, _CMAX_Y);
        if (!pv.isValid()) {
            throw new Exception("Properties parameter out of range, Y");
        }
        RET.setGridY(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(properties.getProperty("s").trim(), 1,
            _CMAX_M);
        if (!pv.isValid()) {
            throw new Exception("Properties parameter out of range, s");
        }
        RET.setS(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(properties.getProperty("v").trim(),
            _CMIN_v, _CMAX_v);
        if (!pv.isValid()) {
            throw new Exception("Properties parameter out of range, v");
        }
        RET.setV(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(properties.getProperty("u").trim(),
            _CMIN_u, _CMAX_u);
        if (!pv.isValid()) {
            throw new Exception("Properties parameter out of range, u");
        }
        RET.setU(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(properties.getProperty("M").trim(),
            _CMIN_M, _CMAX_M);
        if (!pv.isValid()) {
            throw new Exception("Properties parameter out of range, M");
        }
        RET.setM(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(properties.getProperty("K").trim(),
            _CMIN_K, _CMAX_K);
        if (!pv.isValid()) {
            throw new Exception("Properties parameter out of range, K");
        }
        RET.setK(pv.value().intValue());

        pv = ParseValue.parseDoubleValue(properties.getProperty("p").trim(),
            _CMIN_p, _CMAX_p);
        if (!pv.isValid()) {
            throw new Exception("Properties parameter out of range, p");
        }
        RET.setP(pv.value().doubleValue());

        pv = ParseValue.parseDoubleValue(properties.getProperty("q").trim(),
            _CMIN_q, _CMAX_q);
        if (!pv.isValid()) {
            throw new Exception("Properties parameter out of range, q");
        }
        RET.setQ(pv.value().doubleValue());

        toks = Util.split(",\t\n ",
            properties.getProperty("CLASS_LIST").trim());
        _MAX = toks.length;
        classes = new Class[_MAX];
        for (int i = 0; i < _MAX; i++) {
            classes[i] = Class.forName(toks[i]);
        }
        RET.setClassList(classes);

        toks = Util.split(",\t\n ",
            properties.getProperty("PLAYER_LIST").trim());
        _MAX = toks.length;
        if (_MAX < _CMIN_PLAYERS || _MAX > _CMAX_PLAYERS) {
            throw new Exception(
                "Properties parameter out of range, Number of Players (Player" +
                    " List)");
        }
        players = new Class[_MAX];
        for (int i = 0; i < _MAX; i++) {
            players[i] = Class.forName(toks[i]);
        }
        RET.setPlayerList(players);

        return RET;
    }

    public static IFCConfiguration getConfigFromCSVRow(String[] configuration,
                                                Class[] classes) {
        IFCConfiguration RET = new Configuration();
        String[] toks;
        int _MAX;
        Properties properties;
        ParseValue pv;

        RET.setNumRoundsBounds(_CMIN_ROUNDS, _CMAX_ROUNDS);
        RET.setNumPlayersBounds(_CMIN_PLAYERS, _CMAX_PLAYERS);
        RET.setXBounds(_CMIN_X, _CMAX_X);
        RET.setYBounds(_CMIN_Y, _CMAX_Y);
        RET.setVBounds(_CMIN_v, _CMAX_v);
        RET.setUBounds(_CMIN_u, _CMAX_u);
        RET.setMBounds(_CMIN_M, _CMAX_M);
        RET.setKBounds(_CMIN_K, _CMAX_K);
        RET.setPBounds(_CMIN_p, _CMAX_p);
        RET.setQBounds(_CMIN_q, _CMAX_q);

        properties = Util.gatherProperties(_CPROPERTIES_FILE);
        RET.setLogFile(properties.getProperty("LOG_FILE").trim());

        pv = ParseValue.parseIntegerValue(configuration[0].trim(), _CMIN_M,
            _CMAX_M);
        if (!pv.isValid()) {
            throw new RuntimeException(
                "Properties parameter out of range, Init Energy");
        }
        RET.setInitEnergy(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(configuration[1].trim(), _CMIN_ROUNDS,
            _CMAX_ROUNDS);
        if (!pv.isValid()) {
            throw new RuntimeException(
                "Properties parameter out of range, Number of Rounds");
        }
        RET.setNumRounds(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(configuration[2].trim(), _CMIN_X,
            _CMAX_X);
        if (!pv.isValid()) {
            throw new RuntimeException("Properties parameter out of range, X");
        }
        RET.setGridX(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(configuration[3].trim(), _CMIN_Y,
            _CMAX_Y);
        if (!pv.isValid()) {
            throw new RuntimeException("Properties parameter out of range, Y");
        }
        RET.setGridY(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(configuration[4].trim(), 1, _CMAX_M);
        if (!pv.isValid()) {
            throw new RuntimeException("Properties parameter out of range, s");
        }
        RET.setS(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(configuration[5].trim(), _CMIN_v,
            _CMAX_v);
        if (!pv.isValid()) {
            throw new RuntimeException("Properties parameter out of range, v");
        }
        RET.setV(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(configuration[6].trim(), _CMIN_u,
            _CMAX_u);
        if (!pv.isValid()) {
            throw new RuntimeException("Properties parameter out of range, u");
        }
        RET.setU(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(configuration[7].trim(), _CMIN_M,
            _CMAX_M);
        if (!pv.isValid()) {
            throw new RuntimeException("Properties parameter out of range, M");
        }
        RET.setM(pv.value().intValue());

        pv = ParseValue.parseIntegerValue(configuration[8].trim(), _CMIN_K,
            _CMAX_K);
        if (!pv.isValid()) {
            throw new RuntimeException("Properties parameter out of range, K");
        }
        RET.setK(pv.value().intValue());

        pv = ParseValue.parseDoubleValue(configuration[9].trim(), _CMIN_p,
            _CMAX_p);
        if (!pv.isValid()) {
            throw new RuntimeException("Properties parameter out of range, p");
        }
        RET.setP(pv.value().doubleValue());

        pv = ParseValue.parseDoubleValue(configuration[10].trim(), _CMIN_q,
            _CMAX_q);
        if (!pv.isValid()) {
            throw new RuntimeException("Properties parameter out of range, q");
        }
        RET.setQ(pv.value().doubleValue());

        RET.setClassList(classes);

        toks = Util.split(",\t\n ",
            properties.getProperty("PLAYER_LIST").trim());
        _MAX = toks.length;
        if (_MAX < _CMIN_PLAYERS || _MAX > _CMAX_PLAYERS) {
            throw new RuntimeException(
                "Properties parameter out of range, Number of Players (Player" +
                    " List)");
        }
        RET.setPlayerList(classes);

        return RET;
    }


    private int numPlayers() {
        return _numplayers;
    }

    private int currRound() {
        return _currRound;
    }

    private int maxRounds() {
        return _maxrounds;
    }

    //********************************************
    //*
    //* Exposed Methods
    //*
    //********************************************

    public int s() {
        return s;
    }

    public int v() {
        return v;
    }

    public int u() {
        return u;
    }

    public int M() {
        return M;
    }

    public int K() {
        return K;
    }

    public void print(String __str) throws Exception {
        if (_registered) {
            _ui.print(__str);
        }
    }

    public void println(String __str) throws Exception {
        if (_registered) {
            _ui.println(__str);
        }
    }

    public void println() throws Exception {
        if (_registered) {
            _ui.println();
        }
    }

    //********************************************
    //*
    //* IFCModel
    //*
    //********************************************
    public void register(IFCUI __ui) throws Exception {
        roundList = new Vector();
        _ui = __ui;
        _ui.register(this);
        _registered = true;

        //        create(createDefaultConfiguration());

        println("[Player Configuration]: ");
        for (int i = 0; i < _numplayers; i++) {
            println("\t[Player" + i + "]: " + OrigPlayers[i].name());
        }
        refreshGame();
    }

    public String name() throws Exception {
        return _CNAME;
    }

    public JPanel exportControlPanel() throws Exception {
        return _control;
    }

    public JPanel exportViewPanel() throws Exception {
        return _view;
    }

    public JComponent[] exportTools() throws Exception {
        return _control.exportTools();
    }

    public JMenu exportMenu() throws Exception {
        return null;
    }

    protected IFCConfiguration exportConfiguration() throws Exception {
        return _config;
    }


    private void refreshGame() throws Exception {
        if (_registered) {
            _ui.refresh();
        }
    }
    //********************************************
    //*
    //* Private Methods
    //*
    //********************************************


    private void resetGame() throws Exception {
        if (_registered) {
            _ui.reset();
        }
    }


    boolean WithProb(double p) {
        return ThreadLocalRandom.current().nextDouble() < p;
    }

    int min(int a, int b) {
        return (a < b) ? a : b;
    }

    int max(int a, int b) {
        return (a > b) ? a : b;
    }

    void preProcessGrid() {
        for (int x = 0; x < X; x++) {
            for (int y = 0; y < Y; y++) {
                cells[x][y].movedone = false;
                // Generate Food
                if (cells[x][y].pw == null) {
                    if (cells[x][y].foodvalue == 0) { // Generate with prob p
                        cells[x][y].foodvalue = WithProb(p) ? 1 : 0;
                    } else { // Double with prob q
                        int newfood = 0;
                        for (int i = 0; i < cells[x][y].foodvalue; i++)
                            newfood += WithProb(q) ? 1 : 0;
                        cells[x][y].foodvalue = min(K,
                            cells[x][y].foodvalue + newfood);
                    }
                } else { // Feed the Organisms
                    if (cells[x][y].foodvalue > 0) {
                        int currE = cells[x][y].pw.energy();
                        if (currE + u <= M) {
                            ChangeEnergy(x, y, currE + u);
                            cells[x][y].foodvalue--;
                        }
                    }
                }
            }
        }
    }

    // TODO: deprecate
    boolean[] GetFoodState(int x, int y) {
        boolean[] foodpresent = new boolean[5];
        for (int i = 0; i < foodpresent.length; i++) {
            int x1 = (X + x + _CXTrans[i]) % X;
            int y1 = (Y + y + _CYTrans[i]) % Y;
            foodpresent[i] = cells[x1][y1].foodvalue != 0;
        }
        return foodpresent;
    }

    // TODO: deprecate
    int[] GetEnemyState(int x, int y) {
        int[] enemypresent = new int[5];
        for (int i = 0; i < enemypresent.length; i++) {
            int x1 = (X + x + _CXTrans[i]) % X;
            int y1 = (Y + y + _CYTrans[i]) % Y;
            enemypresent[i] = (cells[x1][y1].pw == null) ? (-1) :
                (cells[x1][y1].pw.getExternalState());
        }
        return enemypresent;
    }

    void ChangeEnergy(int x, int y, int finalE) {
        int orig = cells[x][y].pw.energy();
        OrigPlayers[cells[x][y].playertype].AddEnergy(finalE - orig);
        cells[x][y].pw.setEnergy(finalE);
    }

    void KillAmoeba(int x, int y) {
        int origenergy = cells[x][y].pw.energy();
        ChangeEnergy(x, y, 0);
        OrigPlayers[cells[x][y].playertype].AddPop(-1);
        population--;
        cells[x][y].pw = null;
        //println("\tKilling Amoeba at cell " + x + ", " + y);
    }

    void ShiftAmoeba(int x, int y, int x1, int y1) {
        cells[x1][y1].playertype = cells[x][y].playertype;
        cells[x1][y1].pw = cells[x][y].pw;
        cells[x][y].pw = null;
    }

    void NewAmoeba(int x, int y, int ptype, int init_energy, int key) {
        cells[x][y].playertype = ptype;
        cells[x][y].pw = new PlayerWrapper(OrigPlayers[ptype].playerclass());
        cells[x][y].pw.register(this, key);
        ChangeEnergy(x, y, init_energy);
        OrigPlayers[ptype].AddPop(1);
        population++;
    }

    void processMove(int x, int y, Move move) {
        int currE;
        int x1, y1;
        if (move == null)
            move = Move.movement(STAY_PUT);
        switch (move.getAction()) {
            case STAY_PUT:
                currE = cells[x][y].pw.energy();
                if (currE - s <= 0)
                    KillAmoeba(x, y);
                else {
                    ChangeEnergy(x, y, currE - s);
                    cells[x][y].movedone = true;
                }
                break;
            case WEST:
            case EAST:
            case NORTH:
            case SOUTH:
                x1 = (X + x + _CXTrans[move.getAction().intValue()]) % X;
                y1 = (Y + y + _CYTrans[move.getAction().intValue()]) % Y;
                if (cells[x1][y1].pw != null) // Cell is not Empty
                {

                    if (fightingAllowed) {
                        /*
                         * If a player attempts to move onto a cell occupied
                         * by another player... it's on!
                         */
                        System.out.println("FIGHT!");

                        // check that they're not from the same species?
                        PlayerWrapper attacker = cells[x][y].pw;
                        PlayerWrapper defender = cells[x1][y1].pw;

                        Class attackerClass = attacker.playerClass();
                        Class defenderClass = defender.playerClass();
                        //System.out.println("attacker=" + attackerClass + ";
                        // defender=" + defenderClass);
                        if (attackerClass.equals(defenderClass)) {
                            //System.out.println("Cannibalism!");
                            move = Move.movement(STAY_PUT);
                            processMove(x, y, move);
                            break;
                        }

                        // get the energy level for each player
                        int attackerEnergy = attacker.energy();
                        int defenderEnergy = defender.energy();
                        //System.out.println("attacker=" + attackerEnergy +
                        // "; defender=" + defenderEnergy);

                        // determine winner and loser
                        int totalEnergy = attackerEnergy + defenderEnergy;

                        int random = (int) (Math.random() * totalEnergy);
                        //System.out.println("random=" + random);

                        if (random < attackerEnergy) {
                            // attacker wins
                            //System.out.println("attacker=" + attackerClass
                            // + " defeated defender=" + defenderClass);
                            //System.out.println("attacker wins");
                            FightResults fr = fightResults.get(attackerClass);
                            fr.attackWin++;
                            fr = fightResults.get(defenderClass);
                            fr.defendLoss++;
                            KillAmoeba(x1, y1);
                            cells[x1][y1].pw = null;
                            processMove(x, y,
                                move); // now when we do the move, the spot
                            // will be empty
                        } else {
                            // defender wins
                            //System.out.println("defender=" + defenderClass
                            // + " defeated attacker=" + attackerClass);
                            //System.out.println("defender wins");
                            FightResults fr = fightResults.get(defenderClass);
                            fr.defendWin++;
                            fr = fightResults.get(attackerClass);
                            fr.attackLoss++;
                            KillAmoeba(x, y);
                        }
                    } else {
                        move = Move.movement(STAY_PUT);
                        processMove(x, y, move);
                    }
                } else {
                    currE = cells[x][y].pw.energy();
                    if (currE - v <= 0)
                        KillAmoeba(x, y);
                    else {
                        ChangeEnergy(x, y, currE - v);
                        ShiftAmoeba(x, y, x1, y1);
                        cells[x1][y1].movedone = true;
                    }
                }
                break;
            case REPRODUCE:
                currE = cells[x][y].pw.energy();
                if (currE - v <= 1)
                    KillAmoeba(x, y);
                else {
                    switch (move.getChildPosition()) {
                        case WEST:
                        case EAST:
                        case NORTH:
                        case SOUTH:
                            x1 = (X + x + _CXTrans[move.getChildPosition()
                                .intValue()]) % X;
                            y1 = (Y + y + _CYTrans[move.getChildPosition()
                                .intValue()]) % Y;
                            if (cells[x1][y1].pw != null) {
                                move.setAction(STAY_PUT);
                                processMove(x, y, move);
                            } else {
                                currE -= v;
                                ChangeEnergy(x, y, currE / 2);
                                NewAmoeba(x1, y1, cells[x][y].playertype,
                                    currE - (currE / 2), move.getChildKey());
                                cells[x][y].movedone = true;
                                cells[x1][y1].movedone = true;
                            }
                            break;
                        default:
                            move.setAction(STAY_PUT);
                            processMove(x, y, move);
                            break;
                    }
                }
                break;
            default:
                move.setAction(STAY_PUT);
                processMove(x, y, move);
                break;
        }
    }

    //********************************************
    //*
    //* State Transition
    //*
    //********************************************

    private boolean step() {

        Move _move;

        // ------------------------------------------------------
        // keep track of board state this round
        //	System.out.println("step called; round="+_currRound);
        {
            int totalFoodAvailable = 0;

            // cound up food
            for (int i = 0; i < _config.getGridX(); i++) {
                for (int j = 0; j < _config.getGridY(); j++) {
                    totalFoodAvailable += cells[i][j].foodvalue;
                }
            }

            Round round = new Round(OrigPlayers.length,
                _currRound,
                totalFoodAvailable * u);

            for (int i = 0; i < OrigPlayers.length; i++) {
                round.addPlayerData(i,
                    OrigPlayers[i]._totalenergy,
                    OrigPlayers[i]._population);
            }
            roundList.addElement(round); // add to our round list
        }

        //	System.out.println("round history:");
        // 	for (int i = 0; i < roundList.size(); i++)
        // 	{
        // 	    Round round = (Round)roundList.elementAt(i);
        // 	    System.out.println("round: "+ round.number+
        // 		", food energy available: "+round.foodEnergyAvailable);

        // 	    for (int j = 0; j < OrigPlayers.length; j++)
        // 	    {
        // 		System.out.println("player: "+j+", name="+
        // 				   OrigPlayers[j]._name+
        // 				   ", population="+
        // 				   round.players[j].count+
        // 				   ", energy="+
        // 				   round.players[j].energy);
        // 	    }
        // 	}
	/*
	// update the int array with all the food
	for (int i = 0; i < _config.GridX(); i++) {
		for (int j = 0; j < _config.GridY(); j++) {
			food[i][j] = cells[i][j].foodvalue;
			//System.out.print(food[i][j]+"-");
		}
		//System.out.println();
	}
	*/


        if (shouldGraph && graph != null)
            graph.repaint();

        // generate food, feed organisms, resolve moves
        switch (_state) {
            case _CWAITING: {
                System.out.println("Please Make A Move, ");
                return false;
            }

            case _CMOVING: {
                if (population <= 0) {
                    _state = _CFINISHED;
                    break;
                }
                preProcessGrid();
                //				    println("------ Round " + _currRound + "
                //				    ------");
                for (int y = 0; y < Y; y++) {
                    for (int x = 0; x < X; x++) {
                        if (cells[x][y].pw == null)
                            continue;
                        if (cells[x][y].movedone)
                            continue;
                        if (!cells[x][y].pw.interactive()) {
                            Cell cell = cells[x][y];
                            Cell north = getNorth(x, y);
                            Cell south = getSouth(x, y);
                            Cell east = getEast(x, y);
                            Cell west = getWest(x, y);
                            _move = cell.pw.move(
                                cell.foodvalue,
                                north.foodvalue > 0,
                                east.foodvalue > 0,
                                south.foodvalue > 0,
                                west.foodvalue > 0,
                                getVisibleState(north),
                                getVisibleState(east),
                                getVisibleState(south),
                                getVisibleState(west));
                            cells[x][y].pw.selfUpdateExternalState();
                            processMove(x, y, _move);
                        } else { // Interactive Player
                        }
						    /*
						    SB = new StringBuffer();
						    SB.append("Player " + cells[x][y].playertype + "
						    at[" + (x+1) + ", " + (y+1));
						    SB.append("]: ");
						    if(_move != null)
							    SB.append(_move.toString());
						    println(new String(SB));
						    */
                    }
                }


                for (int i = 0; i < OrigPlayers.length; i++) {
                    if (OrigPlayers[i]._population == 0 && OrigPlayers[i].isExtinct == false) {
                        OrigPlayers[i].isExtinct = true;
                    }
                }

                _currRound++;

                if (_currRound >= maxRounds())
                    _state = _CFINISHED;
                break;
            }

        }


        for (int i = 0; i < this._numplayers; i++) {
            if (this.extinctionTimes[i] == 0) {
                // did this player go extinct this round?
                PlayerEntry player = this.OrigPlayers[i];
                if (player._population == 0) {
                    // player went extinct this round
                    this.extinctionTimes[i] = this._currRound;
                    this.energyWhileAlive[i] /= this._currRound;
                    this.populationWhileAlive[i] /= this._currRound;
                } else {
                    // player is still alive
                    this.energyWhileAlive[i] += player._totalenergy;
                    this.populationWhileAlive[i] += player._population;

                    if (this._state == _CFINISHED) {

                    }
                }
            }
            // else player is already extinct
        }

        /*
        // print out the fight results
        System.out.println("---------------------------------------");
        for (PlayerEntry pe : OrigPlayers) {
        	Class c = pe._playerclass;
        	FightResults fr = fightResults.get(c);
        	System.out.println(c.getName() + ": Attacking: " + fr.attackWin +
        	"-" + fr.attackLoss + "; Defending: " + fr.defendWin + "-" + fr
        	.defendLoss);
        }
        */

        return _state != _CFINISHED;
    }

    /**
     * Assumes the game is already initialized.
     * Runs the game until it stops, then notes records end-of-game statistics
     */
    public void runGame() {
        while (step()) {}
        for (int i = 0; i < this._numplayers; i++) {
            PlayerEntry player = this.OrigPlayers[i];
            if (player._population != 0) {
                // player is alive at end.
                // need to divide, otherwise averages are sums
                this.energyWhileAlive[i] /= this._currRound;
                this.populationWhileAlive[i] /= this._currRound;
            }
            this.endCounts[i] = player._population;
            this.endEnergies[i] = player._totalenergy;
        }
    }

    Cell getNorth(int x, int y) {
        return this.cells[x][(this.Y + y - 1) % this.Y];
    }

    Cell getSouth(int x, int y) {
        return this.cells[x][(y + 1) % this.Y];
    }

    Cell getWest(int x, int y) {
        return this.cells[(this.X + x - 1) % this.X][y];
    }

    Cell getEast(int x, int y) {
        return this.cells[(x + 1) % this.X][y];
    }

    int getVisibleState(int x, int y) {
        Cell cell = this.cells[x][y];
        return getVisibleState(cell);
    }

    int getVisibleState(Cell cell) {
        return cell.pw == null ? -1 : cell.pw.getExternalState();
    }

    void printBoard() {
        System.out.println(
            "######################### " + currRound() + " " +
                "################################");
        for (int j = 0; j < Y; j++) {
            for (int i = 0; i < X; i++) {
                System.out.print(cells[i][j].pw + ", ");
            }
            System.out.println();
        }
    }

    /**
     * @return
     */
    private Color[] getPlayerColors() {
        Color[] playerColors = new Color[OrigPlayers.length];
        for (int i = 0; i < playerColors.length; i++) {
            playerColors[i] = OrigPlayers[i]._color;
        }
        return playerColors;
    }

    //********************************************
    //*
    //* View Panel
    //*
    //********************************************
    private final class ViewPanel extends JPanel implements Serializable {
        final int _CWIDTH = 600;
        final int _CHEIGHT = 600;
        final int _MARGIN = 30;
        double _ratio;
        final Font _CVIEW_FONT = new Font("Courier", Font.BOLD, 35);
        final Font _CAMOEBA_FONT = new Font("Courier", Font.BOLD,
            (max(X, Y) >= 50) ? (max(X, Y) >= 60) ? 8 : 9 : 10);
        final int _CHOFFSET = _CAMOEBA_FONT.getSize() / 3;
        final int _CVOFFSET = _CAMOEBA_FONT.getSize();
        final Color _CBLACK = new Color(0.0f, 0.0f, 0.0f);
        final Color _CBACKGROUND_COLOR = new Color(1.0f, 1.0f, 1.0f);
        int _COUTLINE_THICKNESS = 2;
        boolean should_draw_lines = true;

        //********************************************
        //*
        //* Constructor
        //*
        //********************************************
        public ViewPanel() throws Exception {
            super();
            setLayout(new BorderLayout());
            setBackground(_CBACKGROUND_COLOR);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createLoweredBevelBorder()));
            setPreferredSize(new Dimension(_CWIDTH, _CHEIGHT));
            setMinimumSize(new Dimension(_CWIDTH, _CHEIGHT));
            setFont(_CVIEW_FONT);
            should_draw_lines =
                _config.getGridX() < 80 || _config.getGridY() < 80;
            if (!should_draw_lines) {
                _COUTLINE_THICKNESS = 0;
            }
        }


        //********************************************
        //*
        //* paint() Override
        //*
        //********************************************
        public void paintComponent(Graphics __g) {
            try {
                super.paintComponent(__g);

                int width = getWidth() - _MARGIN;
                int height = getHeight() - _MARGIN;
                _ratio = (width < height ? (double) width / (double) max(X,
                    Y) : (double) height / (double) max(X, Y));
                __g.setFont(_CAMOEBA_FONT);
                if (should_draw_lines) {
                    __g.drawLine(_MARGIN, _MARGIN, _MARGIN,
                        _MARGIN + (int) (Y * _ratio));
                    __g.drawLine(_MARGIN, _MARGIN, _MARGIN + (int) (X * _ratio),
                        _MARGIN);
                }
                //				printBoard();
                for (int x = 0; x < X; x++) {
                    if (should_draw_lines) {
                        __g.drawLine(_MARGIN + (int) ((x + 1) * _ratio),
                            _MARGIN, _MARGIN + (int) ((x + 1) * _ratio),
                            _MARGIN + (int) (Y * _ratio));
                    }
                    __g.drawString(Integer.toString(x + 1),
                        _MARGIN + (int) (x * _ratio) + _CHOFFSET, _CVOFFSET);
                    for (int y = 0; y < Y; y++) {
                        if (x == 0) {
                            if (should_draw_lines) {
                                __g.drawLine(_MARGIN,
                                    _MARGIN + (int) ((y + 1) * _ratio),
                                    _MARGIN + (int) (X * _ratio),
                                    _MARGIN + (int) ((y + 1) * _ratio));
                            }
                            __g.drawString(Integer.toString(y + 1), _CHOFFSET,
                                _MARGIN + (int) (y * _ratio) + _CVOFFSET);
                        }
                        if (cells[x][y].pw == null) {
                            if (cells[x][y].foodvalue > 0)
                                __g.drawString(
                                    Integer.toString(cells[x][y].foodvalue),
                                    _MARGIN + (int) (x * _ratio) + 2 * _CHOFFSET,
                                    _MARGIN + (int) (y * _ratio) + 2 * _CVOFFSET);
                            continue;
                        } else {
                            //if(should_draw_lines) {
                            __g.setColor(_CBLACK);
                            __g.fillRect(_MARGIN + (int) (x * _ratio),
                                _MARGIN + (int) (y * _ratio), (int) _ratio,
                                (int) _ratio);
                            //}
                            try {
                                //__g.setColor(OrigPlayers[cells[x][y]
                                // .playertype].color());
                                if (cells[x][y].pw != null)
                                    //					{
                                    //						Color c =
                                    //						cells[x][y].pw
                                    //						.color();
                                    //						float alpha =
                                    //						(float)cells[x][y]
                                    //						.pw._energy * 255
                                    //						/ M;
                                    //						Color
                                    //						relativeColor =
                                    //						new Color(c.getRed
                                    //						(), c.getGreen(),
                                    //						c.getBlue(), (int)
                                    //						alpha);
                                    //						__g.setColor
                                    //						(relativeColor);
                                    //					}
                                    __g.setColor(cells[x][y].pw.color());
                                __g.fillRect(
                                    _MARGIN + (int) (x * _ratio) + _COUTLINE_THICKNESS,
                                    _MARGIN + (int) (y * _ratio) + _COUTLINE_THICKNESS,
                                    (int) _ratio - _COUTLINE_THICKNESS * 2,
                                    (int) _ratio - _COUTLINE_THICKNESS * 2);

                                __g.setColor(_CBLACK);
                                if (cells[x][y].pw != null)
                                    __g.drawString("s" + cells[x][y].pw.getExternalState(),
                                        _MARGIN + (int) (x * _ratio) + _CHOFFSET,
                                        _MARGIN + (int) (y * _ratio) + _CVOFFSET);
                                if (cells[x][y].pw != null)
                                    __g.drawString("e" + cells[x][y].pw.energy(),
                                        _MARGIN + (int) (x * _ratio) + _CHOFFSET,
                                        _MARGIN + (int) (y * _ratio) + 2 * _CVOFFSET);
                            } catch (Exception EXC) {
                                //System.err.println("Exception in 4");
                                //System.err.println(x + ", " + y + ",pw = "
                                // + cells[x][y].pw + ", round = " +
                                // currRound());
                                //							printBoard();
                            }
                        }
                    }
                }

            } catch (Exception EXC) {
                EXC.printStackTrace();
            }
        } // End - paintComponent
    } // End - ViewPanel Class

    //********************************************
    //*
    //* Control Panel
    //*
    //********************************************
    private final class ControlPanel extends JPanel
        implements ActionListener, ItemListener, Serializable {
        JTabbedPane _tab;
        JPanel _conf;
        JPanel _info;
        final int _CWIDTH = 300;
        final int _CHEIGHT = 350;
        final int _CPANEL_WIDTH = _CWIDTH;
        final int _CPANEL_HEIGHT = 21;
        final int _CPLAYER_NAME_LENGTH = 20;
        final ImageIcon _CSTEP_ICON = new ImageIcon("Images/marble_step.gif");
        final ImageIcon _C100STEPS_ICON = new ImageIcon(
            "Images/marble_step.gif");
        final ImageIcon _CSTOP_ICON = new ImageIcon("Images/marble_stop.gif");
        final ImageIcon _CGRAPH_ICON = new ImageIcon("Images/marble_graph.gif");
        final ImageIcon _CPLAY_ICON = new ImageIcon("Images/marble_play.gif");
        final ImageIcon _CRESET_ICON = new ImageIcon("Images/marble_reset.gif");
        final Color _CDISABLED_FIELD_COLOR = new Color(1.0f, 1.0f, 1.0f);
        final Font _CCONTROL_FONT = new Font("Courier", Font.BOLD, 12);
        final Font _CCOMBO_FONT = new Font("Courier", Font.BOLD, 10);

        JTextField _currRoundfield;
        JTextField[] _scores;
        JTextField _numplayersfield;
        JComboBox[] _classes;
        JPanel _infobox;
        JPanel _confbox;
        JButton _play;
        JSlider delay;
        JButton _step;
        JButton _100steps;
        JButton _reset;
        JButton _playTrials;
        JButton _stop;
        JButton _graph;

        JTextField _maxroundsfield;
        JTextField _initenergyfield;
        JTextField _Xfield;
        JTextField _Yfield;
        JTextField _pfield;
        JTextField _qfield;
        JTextField _sfield;
        JTextField _vfield;
        JTextField _ufield;
        JTextField _Mfield;
        JTextField _Kfield;

        NumberFormat _nf;

        //********************************************
        //*
        //* Constructor
        //*
        //********************************************
        public ControlPanel() throws Exception {
            super();

            SlotPanel slot;
            JPanel box;
            JLabel label;
            int _MAX;
            StringBuffer SB;
            String name;

            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createLoweredBevelBorder()));
            setPreferredSize(new Dimension(_CWIDTH, _CHEIGHT));
            setMinimumSize(new Dimension(_CWIDTH, _CHEIGHT));
            setFont(_CCONTROL_FONT);

            _info = new JPanel();
            _info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createLoweredBevelBorder()));
            _info.setLayout(new BorderLayout());
            _info.setPreferredSize(new Dimension(_CWIDTH, _CHEIGHT));
            _info.setMinimumSize(new Dimension(_CWIDTH, _CHEIGHT));
            _info.setFont(_CCONTROL_FONT);

            //_reset = new JButton(_CRESET_ICON);
            _reset = new JButton("Reset");
            _reset.addActionListener(this);

            _playTrials = new JButton("Play Trials");
            _playTrials.addActionListener(this);

            //_step = new JButton(_CSTEP_ICON);
            _step = new JButton("Step");
            _step.addActionListener(this);
            _100steps = new JButton("100 steps");//(_C100STEPS_ICON);
            _100steps.addActionListener(this);
            //_play = new JButton(_CPLAY_ICON);
            _play = new JButton("Play");
            _play.addActionListener(this);
            delay = new JSlider(0, 500);
            delay.setValue(0);

            //_stop = new JButton(_CSTOP_ICON);
            _stop = new JButton("Stop");
            _stop.addActionListener(this);

            //_graph = new JButton(_CGRAPH_ICON);
            _graph = new JButton("Graph");
            _graph.addActionListener(this);

            box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _input = new JTextField();
            _input.setFont(_CCONTROL_FONT);
            _input.addActionListener(this);
            label = new JLabel("Input:              ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _input);
            box.add(slot);

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _currRoundfield = new JTextField();
            _currRoundfield.setEditable(false);
            _currRoundfield.setFont(_CCONTROL_FONT);
            label = new JLabel("Round No.:          ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _currRoundfield);
            box.add(slot);

            _MAX = numPlayers();
            _scores = new JTextField[_MAX];
            for (int i = 0; i < _MAX; i++) {
                _scores[i] = new JTextField();
                _scores[i].setEditable(false);
                _scores[i].setFont(_CCONTROL_FONT);
                slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
                label = new JLabel(Util.adjustString(OrigPlayers[i].name(),
                    _CPLAYER_NAME_LENGTH));
                label.setForeground(OrigPlayers[i].color());
                label.setFont(_CCONTROL_FONT);
                slot.add(label, _scores[i]);
                box.add(slot);
            }

            _info.add(box, BorderLayout.CENTER);
            _infobox = box;

            _conf = new JPanel();
            _conf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createLoweredBevelBorder()));
            _conf.setLayout(new BorderLayout());
            _conf.setPreferredSize(new Dimension(_CWIDTH, _CHEIGHT));
            _conf.setMinimumSize(new Dimension(_CWIDTH, _CHEIGHT));
            _conf.setFont(_CCONTROL_FONT);

            box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _maxroundsfield = new JTextField();
            _maxroundsfield.setFont(_CCONTROL_FONT);
            _maxroundsfield.setText(Integer.toString(_config.numRounds()));
            _maxroundsfield.addActionListener(this);
            label = new JLabel("maxRounds:      ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _maxroundsfield);
            box.add(slot);

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _initenergyfield = new JTextField();
            _initenergyfield.setFont(_CCONTROL_FONT);
            _initenergyfield.setText(Integer.toString(_config.getInitEnergy()));
            _initenergyfield.addActionListener(this);
            label = new JLabel("Initial Energy: ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _initenergyfield);
            box.add(slot);

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _Xfield = new JTextField();
            _Xfield.setFont(_CCONTROL_FONT);
            _Xfield.setText(Integer.toString(_config.getGridX()));
            _Xfield.addActionListener(this);
            label = new JLabel("Grid X:         ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _Xfield);
            box.add(slot);

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _Yfield = new JTextField();
            _Yfield.setFont(_CCONTROL_FONT);
            _Yfield.setText(Integer.toString(_config.getGridY()));
            _Yfield.addActionListener(this);
            label = new JLabel("Grid Y:         ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _Yfield);
            box.add(slot);

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _pfield = new JTextField();
            _pfield.setFont(_CCONTROL_FONT);
            _pfield.setText(Double.toString(_config.getP()));
            _pfield.addActionListener(this);
            label = new JLabel("p:              ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _pfield);
            box.add(slot);

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _qfield = new JTextField();
            _qfield.setFont(_CCONTROL_FONT);
            _qfield.setText(Double.toString(_config.getQ()));
            _qfield.addActionListener(this);
            label = new JLabel("q:              ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _qfield);
            box.add(slot);

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _sfield = new JTextField();
            _sfield.setFont(_CCONTROL_FONT);
            _sfield.setText(Integer.toString(_config.getS()));
            _sfield.addActionListener(this);
            label = new JLabel("s:              ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _sfield);
            box.add(slot);

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _vfield = new JTextField();
            _vfield.setFont(_CCONTROL_FONT);
            _vfield.setText(Integer.toString(_config.getV()));
            _vfield.addActionListener(this);
            label = new JLabel("v:              ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _vfield);
            box.add(slot);

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _ufield = new JTextField();
            _ufield.setFont(_CCONTROL_FONT);
            _ufield.setText(Integer.toString(_config.getU()));
            _ufield.addActionListener(this);
            label = new JLabel("u:              ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _ufield);
            box.add(slot);

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _Mfield = new JTextField();
            _Mfield.setFont(_CCONTROL_FONT);
            _Mfield.setText(Integer.toString(_config.M()));
            _Mfield.addActionListener(this);
            label = new JLabel("M:              ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _Mfield);
            box.add(slot);

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _Kfield = new JTextField();
            _Kfield.setFont(_CCONTROL_FONT);
            _Kfield.setText(Integer.toString(_config.K()));
            _Kfield.addActionListener(this);
            label = new JLabel("K:              ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _Kfield);
            box.add(slot);

            slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
            _numplayersfield = new JTextField();
            _numplayersfield.setFont(_CCONTROL_FONT);
            _numplayersfield.setText(Integer.toString(_MAX));
            _numplayersfield.addActionListener(this);
            label = new JLabel("NumPlayers:     ");
            label.setFont(_CCONTROL_FONT);
            slot.add(label, _numplayersfield);
            box.add(slot);

            _classes = new JComboBox[_MAX];
            for (int i = 0; i < _MAX; i++) {
                _classes[i] = new JComboBox(_classlist);
                _classes[i].setSelectedItem(OrigPlayers[i].playerclass());
                _classes[i].addItemListener(this);
                _classes[i].setFont(_CCOMBO_FONT);
                slot = new SlotPanel(_CPANEL_WIDTH, _CPANEL_HEIGHT);
                label = new JLabel("[" + i + "]:  ");
                label.setFont(_CCONTROL_FONT);
                slot.add(label, _classes[i]);
                box.add(slot);
            }

            _conf.add(box, BorderLayout.CENTER);
            _confbox = box;

            _tab = new JTabbedPane();
            _tab.add("Information", _info);
            _tab.add("Configuration", _conf);
            add(_tab, BorderLayout.CENTER);

            _nf = NumberFormat.getInstance();
            _nf.setMinimumFractionDigits(2);
            _nf.setMaximumFractionDigits(2);

        }

        //********************************************
        //*
        //* ActionListener Interface
        //*
        //********************************************
        public void actionPerformed(ActionEvent __event) {
            Object source = __event.getSource();
            ParseValue pv = null;
            JComboBox[] tmp;
            Class[] tmpcls;
            char[] moves;
            int prev;
            int curr;
            int _MAX;
            SlotPanel slot;
            JLabel label;
            double[] scores;

            try {
                if (source == _100steps) {

                    for (int i = 0; i < 100; i++) step();
                    this.refreshControlPanel();
                    OrganismsGame.this.refreshGame();
                    return;
                }

                if (source == _step) {
                    step();
                    this.refreshControlPanel();
                    OrganismsGame.this.refreshGame();
                    return;
                }
                if (source == _play) {
                    new StopListener(this).start();
                    return;
                }
                if (source == _playTrials) {
                    String strTrials = JOptionPane.showInputDialog(
                        "Enter the number of trials");
                    int trials;
                    try {
                        trials = Integer.parseInt(strTrials);
                    } catch (NumberFormatException ex) {
                        return;
                    }
                    new StopListener2(this, trials).start();
                    return;
                }
                if (source == _reset) {
                    OrganismsGame.this.resetGame();
                    return;
                }
                if (source == _stop) {
                    _state = _CFINISHED;
                }

                if (source == _graph) {
                    shouldGraph = true;
                    graph = new GraphDrawer(roundList, getPlayerColors());
                }


                if (source == _input) {
                    if (_state == _CFINISHED) {
                        return;
                    }
                    //_moves[_playerindex] = parseMove(_input.getText());
                    return;
                }
                if (source == _maxroundsfield) {
                    pv = ParseValue.parseIntegerValue(_maxroundsfield.getText(),
                        _CMIN_ROUNDS, _CMAX_ROUNDS);
                    if (pv.isValid()) {
                        _config.setNumRounds(pv.value().intValue());
                        _ui.configure(_config);
                    } else {
                        println("Invalid Input");
                    }
                }
                if (source == _initenergyfield) {
                    pv = ParseValue.parseIntegerValue(
                        _initenergyfield.getText(), _CMIN_M, _CMAX_M);
                    if (pv.isValid()) {
                        _config.setInitEnergy(
                            pv.value().intValue());
                        _ui.configure(_config);
                    } else {
                        println("Invalid Input");
                    }
                }
                if (source == _Xfield) {
                    pv = ParseValue.parseIntegerValue(_Xfield.getText(),
                        _CMIN_X, _CMAX_X);
                    if (pv.isValid()) {
                        _config.setGridX(pv.value().intValue());
                        _ui.configure(_config);
                    } else {
                        println("Invalid Input");
                    }
                }
                if (source == _Yfield) {
                    pv = ParseValue.parseIntegerValue(_Yfield.getText(),
                        _CMIN_Y, _CMAX_Y);
                    if (pv.isValid()) {
                        _config.setGridY((Integer) pv.value());
                        _ui.configure(_config);
                    } else {
                        println("Invalid Input");
                    }
                }
                if (source == _pfield) {
                    pv = ParseValue.parseDoubleValue(_pfield.getText(), _CMIN_p,
                        _CMAX_p);
                    if (pv.isValid()) {
                        _config.setP(pv.value().doubleValue());
                        _ui.configure(_config);
                    } else {
                        println("Invalid Input");
                    }
                }
                if (source == _qfield) {
                    pv = ParseValue.parseDoubleValue(_qfield.getText(), _CMIN_q,
                        _CMAX_q);
                    if (pv.isValid()) {
                        _config.setQ(pv.value().doubleValue());
                        _ui.configure(_config);
                    } else {
                        println("Invalid Input");
                    }
                }
                if (source == _sfield) {
                    pv = ParseValue.parseIntegerValue(_sfield.getText(), 1,
                        _CMAX_M);
                    if (pv.isValid()) {
                        _config.setS(pv.value().intValue());
                        _ui.configure(_config);
                    } else {
                        println("Invalid Input");
                    }
                }
                if (source == _vfield) {
                    pv = ParseValue.parseIntegerValue(_vfield.getText(),
                        _CMIN_v, _CMAX_v);
                    if (pv.isValid()) {
                        _config.setV(pv.value().intValue());
                        _ui.configure(_config);
                    } else {
                        println("Invalid Input");
                    }
                }
                if (source == _ufield) {
                    pv = ParseValue.parseIntegerValue(_ufield.getText(),
                        _CMIN_u, _CMAX_u);
                    if (pv.isValid()) {
                        _config.setU(pv.value().intValue());
                        _ui.configure(_config);
                    } else {
                        println("Invalid Input");
                    }
                }
                if (source == _Mfield) {
                    pv = ParseValue.parseIntegerValue(_Mfield.getText(),
                        _CMIN_M, _CMAX_M);
                    if (pv.isValid()) {
                        _config.setM(pv.value().intValue());
                        _ui.configure(_config);
                    } else {
                        println("Invalid Input");
                    }
                }
                if (source == _Kfield) {
                    pv = ParseValue.parseIntegerValue(_Kfield.getText(),
                        _CMIN_K, _CMAX_K);
                    if (pv.isValid()) {
                        _config.setK(pv.value().intValue());
                        _ui.configure(_config);
                    } else {
                        println("Invalid Input");
                    }
                }
                if (source == _numplayersfield) {
                    pv = ParseValue.parseIntegerValue(
                        _numplayersfield.getText(), _CMIN_PLAYERS,
                        _CMAX_PLAYERS);
                    if (pv.isValid()) {
                        prev = _config.numPlayers();
                        curr = pv.value().intValue();
                        if (prev == curr) {
                            return;
                        }
                        if (curr > prev) {
                            tmp = _classes;
                            _classes = new JComboBox[curr];
                            System.arraycopy(tmp, 0, _classes, 0, prev);
                            tmpcls = new Class[curr];
                            System.arraycopy(_config.playerList(), 0, tmpcls, 0,
                                prev);
                            for (int i = prev; i < curr; i++) {
                                _classes[i] = new JComboBox(_classlist);
                                _classes[i].addItemListener(this);
                                _classes[i].setFont(_CCOMBO_FONT);
                                slot = new SlotPanel(_CPANEL_WIDTH,
                                    _CPANEL_HEIGHT);
                                label = new JLabel("[" + i + "]:  ");
                                label.setFont(_CCONTROL_FONT);
                                slot.add(label, _classes[i]);
                                _confbox.add(slot);
                                tmpcls[i] =
                                    (Class) _classes[i].getSelectedItem();
                            }
                            _config.setPlayerList(tmpcls);
                        }
                        if (curr < prev) {
                            tmp = new JComboBox[curr];
                            System.arraycopy(_classes, 0, tmp, 0, curr);
                            tmpcls = new Class[curr];
                            System.arraycopy(_config.playerList(), 0, tmpcls, 0,
                                curr);
                            for (int i = curr; i < prev; i++) {
                                _confbox.remove(
                                    _confbox.getComponents().length - 1);
                            }
                            _classes = tmp;
                            _config.setPlayerList(tmpcls);
                        }
                        _ui.configure(_config);
                        repaint();
                        OrganismsGame.this.refreshGame();
                    } else {
                        println("Invalid Input");
                    }
                }
            } catch (Exception EXC) {
                System.out.println(EXC.getMessage());
                EXC.printStackTrace();
            }
        }

        //********************************************
        //*
        //* ItemListener Interface
        //*
        //********************************************
        public void itemStateChanged(ItemEvent __event) {
            Object source = __event.getSource();
            int _MAX = _classes.length;

            try {
                for (int i = 0; i < _MAX; i++) {
                    if (source == _classes[i]) {
                        _config.setPlayer(i,
                            (Class) _classes[i].getSelectedItem());
                        _ui.configure(_config);
                    }
                }
            } catch (Exception EXC) {
                System.out.println(EXC.getMessage());
                EXC.printStackTrace();
            }
        }

        //********************************************
        //*
        //* Score Updater
        //*
        //********************************************
        public void refreshControlPanel() throws Exception {
            int _MAX = numPlayers();

            for (int i = 0; i < _MAX; i++) {
                int pop = OrigPlayers[i].population();
                int totalE = OrigPlayers[i].totalenergy();
                _scores[i].setText(
                    pop + ", totalE " + totalE);
                //_scores[i].setText(_players[i].result());
            }
            _currRoundfield.setText(Integer.toString(currRound()));

            this.repaint();
        }


        //********************************************
        //*
        //* Action Tool Exporter
        //*
        //********************************************
        public JComponent[] exportTools() {
            /* amg2006 changed to 4 */
            // djv changed to 5
            JComponent[] ret = new JComponent[8];
            ret[0] = _reset;
            ret[1] = _step;
            ret[2] = _100steps;
            ret[3] = _play;
            ret[4] = _stop;
            ret[5] = _graph;
            ret[6] = _playTrials;
            ret[7] = delay;
            return ret;
        }
    }

    class StopListener extends Thread implements ChangeListener {
        private final ControlPanel controlPanel;
        private final JSlider slider;
        private int delay;

        public StopListener(ControlPanel cp) {
            this.controlPanel = cp;
            this.slider = controlPanel.delay;
        }

        public void run() {
            try {
                delay = slider.getValue();
                slider.addChangeListener(this);
                while (GUI._amoeba.step()) {
                    Thread.sleep(delay);
                    GUI._amoeba.refreshGame();
                    controlPanel.refreshControlPanel();
                }
                slider.removeChangeListener(this);
                GUI._amoeba.refreshGame();
                controlPanel.refreshControlPanel();
            } catch (Exception e) {
                System.out.println("unexpected exception caught in run");
                e.printStackTrace();
            }
        }

        @Override
        public void stateChanged(ChangeEvent arg0) {
            if (arg0.getSource().equals(slider)) {
                delay = ((JSlider) arg0.getSource()).getValue();
            }

        }
    }

    class StopListener2 extends Thread {
        private int trials = 10;
        private int gameIndex = 1;
        private final ControlPanel controlPanel;
        private final ArrayList<Round> lastRounds;

        public StopListener2(ControlPanel cp) {
            this(cp, 10);
        }

        public StopListener2(ControlPanel cp, int trials) {
            controlPanel = cp;
            lastRounds = new ArrayList<Round>();
            this.trials = trials;
        }

        private void printInfo(int gameIndex, int trials) {
            int _MAX = numPlayers();

            PlayerRoundData[] data = getLastRound().players;
            // output the results
            for (int i = 0; i < _MAX; i++) {
                int pop = data[i].count;
                int totalE = data[i].energy;
                String message = String.format(
                    "GAME (%d/%d) Player: %d Population: %d Energy: %d",
                    gameIndex, trials, i, pop, totalE);
                try {
                    getGame().println(message);
                } catch (Exception e) {
                }
            }
        }

        private void trackLastRound() {
            lastRounds.add(getLastRound());
        }

        private Round getLastRound() {
            return (Round) getGame().roundList.lastElement();
        }

        private OrganismsGame getGame() {
            return GUI._amoeba;
        }

        private void showSummary() {
            int _MAX = numPlayers();

            PlayerRoundData[] cumulativeData = new PlayerRoundData[_MAX];
            for (Round round : lastRounds) {
                PlayerRoundData[] data = round.players;
                // output the results
                for (int i = 0; i < _MAX; i++) {
                    int pop = data[i].count;
                    int totalE = data[i].energy;
                    boolean extinct = pop == 0 && totalE == 0;

                    if (cumulativeData[i] == null) {
                        cumulativeData[i] = new PlayerRoundData(
                            data[i].playerId, 0, 0);
                    }

                    cumulativeData[i].count += data[i].count;
                    cumulativeData[i].energy += data[i].energy;
                    if (extinct) cumulativeData[i].extinctionCount++;
                }
            }

            //calculate mean
            for (int i = 0, size = lastRounds.size(); i < _MAX; i++) {
                cumulativeData[i].count = cumulativeData[i].count / size;
                cumulativeData[i].energy = cumulativeData[i].energy / size;
            }

            println(String.format("[SUMMARY of %d trials]", trials));
            for (PlayerRoundData data : cumulativeData) {
                println(String.format(
                    "Player: %d Mean Count: %d Mean Energy: %d Extinction: %d",
                    data.playerId, data.count, data.energy,
                    data.extinctionCount));
            }
        }

        protected void println(String message) {
            try {
                getGame().println(message);
            } catch (Exception ex) {
            }
        }

        public void run() {
            try {
                while (getGame().step()) {
                    getGame().refreshGame();
                    controlPanel.refreshControlPanel();
                }
                getGame().refreshGame();
                controlPanel.refreshControlPanel();

                // graph = new GraphDrawer(getGame().roundList, getPlayerColors());

                printInfo(gameIndex, trials);
                trackLastRound();
                gameIndex++;

                if (gameIndex > trials) {
                    showSummary();
                    return;
                }

                Thread.sleep(100);

                GUI._amoeba.resetGame();
                controlPanel.refreshControlPanel();
                run();

            } catch (Exception e) {
                System.out.println("unexpected exception caught in run");
                e.printStackTrace();
            }

        }

    }

    class TournamentStopListener extends Thread {
        private int gameIndex = 1;
        private final int trials;
        private final ArrayList<Round> lastRounds;
        private final OrganismsGame og;
        private final String configName;
        private final int[][] averageExtinction;
        private final String[] csvConfig;
        private final long[][] averageEnergyWhileAlive;

        public TournamentStopListener(OrganismsGame og, String[] csvConfig) {
            this.og = og;
            this.lastRounds = new ArrayList<Round>();
            this.csvConfig = csvConfig;
            this.trials = Integer.parseInt(csvConfig[11]);
            this.configName = csvConfig[12];
            this.averageExtinction = new int[OrigPlayers.length][2];
            this.averageEnergyWhileAlive = new long[OrigPlayers.length][3];
        }

        private void trackLastRound() {
            lastRounds.add(getLastRound());
        }

        private Round getLastRound() {
            return (Round) og.roundList.lastElement();
        }

        private String showSummary() {
            int _MAX = numPlayers();
            StringBuffer result = new StringBuffer();

            result.append("================================================\n");
            result.append(configName + "\n");

            PlayerRoundData[] cumulativeData = new PlayerRoundData[_MAX];
            for (Round round : lastRounds) {
                PlayerRoundData[] data = round.players;
                // output the results
                for (int i = 0; i < _MAX; i++) {
                    int pop = data[i].count;
                    int totalE = data[i].energy;
                    boolean extinct = pop == 0 && totalE == 0;

                    if (cumulativeData[i] == null) {
                        cumulativeData[i] = new PlayerRoundData(
                            data[i].playerId, 0, 0);
                    }

                    cumulativeData[i].count += data[i].count;
                    cumulativeData[i].energy += data[i].energy;
                    if (extinct) cumulativeData[i].extinctionCount++;
                }
            }

            //calculate mean
            for (int i = 0, size = lastRounds.size(); i < _MAX; i++) {
                cumulativeData[i].count = cumulativeData[i].count / size;
                cumulativeData[i].energy = cumulativeData[i].energy / size;
            }
            for (Class klass : og._classlist) {
                result.append(klass.toString() + "\n");
            }
            result.append(String.format("[SUMMARY of %d trials]\n", trials));
            for (PlayerRoundData data : cumulativeData) {
                int extinctionTime = this.averageExtinction[data.playerId][1] == 0 ? 0 : this.averageExtinction[data.playerId][0] / this.averageExtinction[data.playerId][1];
                int averageEnergyAlive = (int) (this.averageEnergyWhileAlive[data.playerId][2] / trials);
                result.append(String.format(
                    "Player: %d\tMean Count: %d\tMean Energy: %d\t# Extinctions: %d\tAverage Time of Extinction: %d\tAverage Energy While Alive: %d\n",
                    data.playerId, data.count, data.energy,
                    data.extinctionCount, extinctionTime, averageEnergyAlive));
            }
            return result.toString();
        }

        public void run() {
            try {
                boolean[] tournamentExtinct = new boolean[OrigPlayers.length];
                while (og.step()) {
                    for (int i = 0; i < OrigPlayers.length; i++) {
                        if (OrigPlayers[i]._population == 0 && !tournamentExtinct[i]) {
                            tournamentExtinct[i] = true;
                            this.averageExtinction[i][0] += _currRound;
                            this.averageExtinction[i][1] += 1;
                            this.averageEnergyWhileAlive[i][0] = _currRound;
                        } else {
                            this.averageEnergyWhileAlive[i][1] += OrigPlayers[i]._totalenergy;
                        }

                    }
                }

                for (int i = 0; i < OrigPlayers.length; i++) {
                    if (this.averageEnergyWhileAlive[i][0] == 0) {
                        this.averageEnergyWhileAlive[i][2] += this.averageEnergyWhileAlive[i][1] / og._maxrounds;
                    } else {
                        this.averageEnergyWhileAlive[i][2] += this.averageEnergyWhileAlive[i][1] / this.averageEnergyWhileAlive[i][0];
                    }
                    this.averageEnergyWhileAlive[i][0] = 0;
                    this.averageEnergyWhileAlive[i][1] = 0;

                }

                trackLastRound();
                gameIndex++;

                if (gameIndex > trials) {
                    TournamentResultLogger.print(showSummary());
                    return;
                }

                Thread.sleep(100);
                IFCConfiguration config = getConfigFromCSVRow(csvConfig,
                    og._classlist);
                og.create(config);
                run();

            } catch (Exception e) {
                System.out.println("unexpected exception caught in run");
                e.printStackTrace();
            }

        }
    }

}
