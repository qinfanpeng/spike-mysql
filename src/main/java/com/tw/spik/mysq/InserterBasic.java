package com.tw.spik.mysq;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.IntStream;

public class InserterBasic {

    private Queue<String> insertQueue = new ConcurrentLinkedDeque<>();
    private Queue<String> queryQueue = new ConcurrentLinkedDeque<>();
    private int prevInsertSize = 0;
    private int prevQuerySize = 0;
    private String columns = "antiNucleus, eventFile, eventNumber, eventTime, histFile, multiplicity, NaboveLb, NbelowLb, NLb, primaryTracks, prodTime, Pt, runNumber, vertexX, vertexY, vertexZ";

    public static String MyISAM = "MyISAM";
    public static String InnoDB = "InnoDB";

    public Queue<String> parseData(File file) throws IOException {
        System.out.println("Start parsing " + file.getName());
//        CSVReader csvReader = new CSVReaderBuilder(new FileReader(file)).withSkipLines(1).build();
//        for (String line : csvReader) {
//            insertQueue.offer(line);
//        }

//        FileInputStream inputStream = new FileInputStream(file.getPath());
//        final Scanner scanner = new Scanner(inputStream, "UTF-8");
//        scanner.nextLine(); // skip header
//
//        while (scanner.hasNextLine()) {
//            insertQueue.offer(scanner.nextLine());
//        }
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        bufferedReader.lines()
            .skip(1)   // Skip header
            .parallel()
            .forEach(insertQueue::add);

        prevInsertSize = insertQueue.size();
        return insertQueue;
    }

    public void recordInsertSpeed() {
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                int thisSize = insertQueue.size();
                System.out.println((prevInsertSize - thisSize)/10 +"条/秒");
                prevInsertSize = thisSize;
            }
        }, 0, 1000*10);
    }

    public void recordQuerySpeed() {
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                int thisSize = queryQueue.size();
                System.out.println((prevQuerySize - thisSize)/10 +"条/秒");
                prevQuerySize = thisSize;
            }
        }, 0, 1000 * 10);
    }

    public void insert(Queue<String> lines, String engine) throws ClassNotFoundException, SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();
        String tableName = engine.equals(InnoDB) ? "star2002_innodb" : "star2002_myisam";

        int i = 0;
        StringBuilder valuesBuilder = new StringBuilder();

        while(!lines.isEmpty()){
            valuesBuilder.append("(").append(lines.poll()).append(")");
            if (i % 20000 == 0) {
                statement.execute(String.format("INSERT delayed INTO %s(%s) VALUES %s;", tableName, columns, valuesBuilder.toString()));
                connection.commit();
                valuesBuilder = new StringBuilder();
            } else {
                valuesBuilder.append(", ");
            }
            i++;
        }

        statement.close();
        connection.close();
    }

    public void query_100w_data(String engine, boolean parallel) throws SQLException, ClassNotFoundException {
        Connection connection = this.getConnection();
        final Statement statement = connection.createStatement();
        ;
        String tableName = engine.equals(InnoDB) ? "star2002_innodb" : "star2002_myisam";

        IntStream offsetsStream = IntStream.iterate(0, x -> x + 10000)
            .limit(100);
        if (parallel) {
            offsetsStream = offsetsStream.parallel();
        }
        offsetsStream
            .forEach(offset -> {
                try {
                    Statement realStatement = statement;
                    if (parallel) {
                         realStatement = this.getConnection().createStatement();
                    }
                    final String querySql = String.format("select * from %s where id > %d order by id limit 10000", tableName, offset);
                    final ResultSet resultSet = realStatement.executeQuery(querySql);
                    while (resultSet.next())
                    {
                        final String lineValue = String.format("%d, %s", resultSet.getInt("id"), resultSet.getDouble("vertexY"));
                        this.queryQueue.offer(lineValue);
                    }
                    if (parallel) {
                        realStatement.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        System.out.println("Queried records: " + this.queryQueue.size());
        if (!parallel) {
            statement.close();
        }
        connection.close();
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/spike_mysql", "root", null);
    }

    public void concurrentlyInsert(Queue<String> lines, String engine) throws SQLException, ClassNotFoundException {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        if (engine.equals(MyISAM)) {
            statement.execute("ALTER TABLE star2002_myisam DISABLE KEYS;");
        } else {
            statement.execute("SET FOREIGN_KEY_CHECKS=0;");
            statement.execute("SET UNIQUE_CHECKS=0;");
            statement.execute("SET global innodb_buffer_pool_size=1342177280;");
            statement.execute("SET global innodb_autoextend_increment=128;");
            statement.execute("SET global bulk_insert_buffer_size=104857600;");
            statement.execute("ALTER TABLE star2002_innodb DROP INDEX index_eventNumber;");
        }

        IntStream.rangeClosed(1, 5)
            .parallel()
            .forEach(x -> {
                try {
                    this.insert(lines, engine);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        if (engine.equals(MyISAM)) {
            statement.execute("ALTER TABLE star2002_myisam ENABLE KEYS;");
        } else {
            statement.execute("SET FOREIGN_KEY_CHECKS=1;");
            statement.execute("SET UNIQUE_CHECKS=1;");
            statement.execute("SET global innodb_buffer_pool_size=134217728;");
            statement.execute("SET global innodb_autoextend_increment=64;");
            statement.execute("SET global bulk_insert_buffer_size=8388608;");
            statement.execute("ALTER TABLE star2002_innodb ADD INDEX index_eventNumber(eventNumber);");
        }
    }

}
