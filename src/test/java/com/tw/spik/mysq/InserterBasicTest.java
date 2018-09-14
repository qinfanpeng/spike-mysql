package com.tw.spik.mysq;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Queue;

public class InserterBasicTest {

    private InserterBasic inserter;

    @Before
    public void setUp() throws Exception {
        inserter = new InserterBasic();
    }

    private Queue<String> parseDataFile() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();

        File file1 = new File(classLoader.getResource("star2002-1.csv").getFile());
        File file2 = new File(classLoader.getResource("star2002-2.csv").getFile());
        File file3 = new File(classLoader.getResource("star2002-3.csv").getFile());

        Queue<String> lines = inserter.parseData(file1);
        lines = inserter.parseData(file2);
        lines = inserter.parseData(file3);

        return lines;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test_parallelism_info() throws Exception {
//        System.out.println(System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism"));
//        System.out.println(Runtime.getRuntime().availableProcessors());

//        IntStream.iterate(0, x -> x + 10000)
//            .limit(100)
//            .parallel()
//            .forEach(System.out::println);

    }

    @Test
    @Ignore
    public void test_insert_into_InnoDB_table() throws Exception {
        final Queue<String> lines = parseDataFile();
        inserter.recordInsertSpeed();
        inserter.insert(lines, InserterBasic.InnoDB);
    }

    @Test
    @Ignore
    public void test_insert_into_MyISAM_table() throws Exception {
        final Queue<String> lines = parseDataFile();
        inserter.recordInsertSpeed();
        inserter.insert(lines, InserterBasic.MyISAM);
    }

    @Test
    public void test_insert_concurrently_into_InnoDB_table() throws Exception {
        final Queue<String> lines = parseDataFile();
        inserter.recordInsertSpeed();
        inserter.concurrentlyInsert(lines, InserterBasic.InnoDB);
    }

    @Test
    public void test_insert_concurrently_into_MyISAM_table() throws Exception {
        final Queue<String> lines = parseDataFile();
        inserter.recordInsertSpeed();
        inserter.concurrentlyInsert(lines, InserterBasic.MyISAM);
    }

    @Test
    public void test_query_100w_data_from_InnoDB_table() throws Exception {
        inserter.recordQuerySpeed();
        inserter.query_100w_data(InserterBasic.InnoDB, false);
    }

    @Test
    public void test_query_100w_data_concurrently_from_InnoDB_table() throws Exception {
        inserter.recordQuerySpeed();
        inserter.query_100w_data(InserterBasic.InnoDB, true);
    }

    @Test
    public void test_query_100w_data_from_MyISAM_table() throws Exception {
        inserter.recordQuerySpeed();
        inserter.query_100w_data(InserterBasic.MyISAM, false);
    }

    @Test
    public void test_query_100w_data_concurrently_from_MyISAM_table() throws Exception {
        inserter.recordQuerySpeed();
        inserter.query_100w_data(InserterBasic.MyISAM, true);
    }
}