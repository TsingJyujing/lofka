package com.github.tsingjyujing.lofka.algorithm.cluster.common;

import org.junit.Test;

import java.util.List;

public class DivisibleGeneratorTest {

    @Test
    public void analysisCommonStrings() {
        List<IDivisible<String>> res = new DivisibleGenerator<String>().analysisCommonStrings(
                1, testData()
        );
        printResult(
                res
        );
    }

    @Test
    public void analysisPrefixStrings() {
        printResult(
                new DivisibleGenerator<String>().analysisPrefixStrings(
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
        return DivisibleGenerator.generateSequenceByComma(
                "can't connect to mysql server ,errmsg:Unknown database 'cvnavidb' MySQLConnection [id=12461700, lastTime=1551971922856, user=cvnavidb, schema=cvnavidb, old shema=cvnavidb, borrowed=false, fromSlaveDB=true, threadId=298807, charset=utf8, txIsolation=3, autocommit=true, attachment=null, respHandler=null, host=10.10.10.126, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false]",
                "can't connect to mysql server ,errmsg:Unknown database 'cvnavidb' MySQLConnection [id=12461702, lastTime=1551971922856, user=cvnavidb, schema=cvnavidb, old shema=cvnavidb, borrowed=false, fromSlaveDB=true, threadId=298809, charset=utf8, txIsolation=3, autocommit=true, attachment=null, respHandler=null, host=10.10.10.126, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false]",
                "can't connect to mysql server ,errmsg:Unknown database 'cvnavidb' MySQLConnection [id=12461698, lastTime=1551971922856, user=cvnavidb, schema=cvnavidb, old shema=cvnavidb, borrowed=false, fromSlaveDB=true, threadId=298805, charset=utf8, txIsolation=3, autocommit=true, attachment=null, respHandler=null, host=10.10.10.126, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false]",
                "can't connect to mysql server ,errmsg:Unknown database 'cvnavidb' MySQLConnection [id=12461700, lastTime=1551971922856, user=cvnavidb, schema=cvnavidb, old shema=cvnavidb, borrowed=false, fromSlaveDB=true, threadId=298807, charset=utf8, txIsolation=3, autocommit=true, attachment=null, respHandler=null, host=10.10.10.126, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false]",
                "can't connect to mysql server ,errmsg:Unknown database 'cvnavidb' MySQLConnection [id=12461702, lastTime=1551971922856, user=cvnavidb, schema=cvnavidb, old shema=cvnavidb, borrowed=false, fromSlaveDB=true, threadId=298809, charset=utf8, txIsolation=3, autocommit=true, attachment=null, respHandler=null, host=10.10.10.126, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false]",
                "can't connect to mysql server ,errmsg:Unknown database 'cvnavidb' MySQLConnection [id=12461698, lastTime=1551971922856, user=cvnavidb, schema=cvnavidb, old shema=cvnavidb, borrowed=false, fromSlaveDB=true, threadId=298805, charset=utf8, txIsolation=3, autocommit=true, attachment=null, respHandler=null, host=10.10.10.126, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false]"
                );
    }
}
