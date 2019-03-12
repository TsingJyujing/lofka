package com.github.tsingjyujing.lofka.algorithm.cluster.common;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DivisibleGeneratorTest {

    @Test
    public void analysisCommonStrings() {
        printResult(
                DivisibleGenerator.analysisCommonStrings(
                        1, testData()
                )
        );
    }

    @Test
    public void analysisPrefixStrings() {
        printResult(
                DivisibleGenerator.analysisPrefixStrings(
                        1, testData()
                )
        );
    }

    private static void printResult(List<IDivisible<String>> result) {
        for (IDivisible iDivisible : result) {
            System.out.print(iDivisible);
        }
        System.out.print("\n");
    }

    private static List<TypedSequence<String>> testData() {
        String[] rawData = new String[]{
                "can't connect to mysql server ,errmsg:Unknown database 'cvnavidb' MySQLConnection [id=12461700, lastTime=1551971922856, user=cvnavidb, schema=cvnavidb, old shema=cvnavidb, borrowed=false, fromSlaveDB=true, threadId=298807, charset=utf8, txIsolation=3, autocommit=true, attachment=null, respHandler=null, host=10.10.10.126, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false]",
                "can't connect to mysql server ,errmsg:Unknown database 'cvnavidb' MySQLConnection [id=12461702, lastTime=1551971922856, user=cvnavidb, schema=cvnavidb, old shema=cvnavidb, borrowed=false, fromSlaveDB=true, threadId=298809, charset=utf8, txIsolation=3, autocommit=true, attachment=null, respHandler=null, host=10.10.10.126, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false]",
                "can't connect to mysql server ,errmsg:Unknown database 'cvnavidb' MySQLConnection [id=12461698, lastTime=1551971922856, user=cvnavidb, schema=cvnavidb, old shema=cvnavidb, borrowed=false, fromSlaveDB=true, threadId=298805, charset=utf8, txIsolation=3, autocommit=true, attachment=null, respHandler=null, host=10.10.10.126, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false]",
                "can't connect to mysql server ,errmsg:Unknown database 'cvnavidb' MySQLConnection [id=12461700, lastTime=1551971922856, user=cvnavidb, schema=cvnavidb, old shema=cvnavidb, borrowed=false, fromSlaveDB=true, threadId=298807, charset=utf8, txIsolation=3, autocommit=true, attachment=null, respHandler=null, host=10.10.10.126, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false]",
                "can't connect to mysql server ,errmsg:Unknown database 'cvnavidb' MySQLConnection [id=12461702, lastTime=1551971922856, user=cvnavidb, schema=cvnavidb, old shema=cvnavidb, borrowed=false, fromSlaveDB=true, threadId=298809, charset=utf8, txIsolation=3, autocommit=true, attachment=null, respHandler=null, host=10.10.10.126, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false]",
                "can't connect to mysql server ,errmsg:Unknown database 'cvnavidb' MySQLConnection [id=12461698, lastTime=1551971922856, user=cvnavidb, schema=cvnavidb, old shema=cvnavidb, borrowed=false, fromSlaveDB=true, threadId=298805, charset=utf8, txIsolation=3, autocommit=true, attachment=null, respHandler=null, host=10.10.10.126, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false]",
        };

        Pattern pattern = Pattern.compile("[^0-9a-zA-Z]+|[0-9a-zA-Z]+");
        List<TypedSequence<String>> seqs = Lists.newArrayList();
        for (String data : rawData) {
            Matcher matcher = pattern.matcher(data);
            List<String> strs = Lists.newArrayList();
            while (matcher.find()) {
                strs.add(matcher.group());
            }
            seqs.add(new TypedSequence<String>(strs));
        }
        return seqs;
    }
}
