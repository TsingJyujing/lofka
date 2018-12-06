/**
 * 美化JSON String
 * @param str
 * @returns {string}
 */
function prettyJsonString(str) {
    return JSON.stringify(
        JSON.parse(
            str
        ), null, 2
    );
}

$.getJSON(
    "http://httpbin.org/get",
    {
        "x1": "v1",
        "x3": 3
    },
    console.logJSON
);

$.post(
    "http://httpbin.org/post",
    {
        "x1": "v1",
        "x3": 3
    },
    function (data) {
        console.logJSON(JSON.parse(data));
    }
);

var conn = new SQLConnection({
    "uri": "jdbc:mysql://10.10.10.227:28066/cvnavidb",
    "user": "cvnavidb",
    "password": "XJAKNEBCYlHefj7YHdi0",
    "driver": "com.mysql.jdbc.Driver"
});
try {
    console.logJSON(
        conn.query(
            "SELECT F_VEHICLE_ID as id,F_GPS_TIME as gt,F_LONGITUDE AS lng, F_LATITUDE as lat FROM T_LOCATION_20181128 LIMIT 0,2"
        )
    );
    throw "Test Error";
} catch (err) {
    console.err("Error while query data from sql " + JSON.stringify(err, null, 2))
} finally {
    conn.close();
}


var redis = new RedisCluster([1, 2, 3, 4, 5].map(function (i) {
    return "10.10.10.124:700" + i
}));
console.logJSON(redis.hgetall("5008853915694069_GPS"));
console.log(redis.hget("5008853915694069_GPS", "AreaSN"));
redis.close();


var mongo = new MongoClient({
    "servers": "127.0.0.1"
});

console.logJSON(
    mongo.get_collection(
        "gis", "shanghai_poi"
    ).find({
        "typecode": "010101"
    }).sort({
        "_id": -1
    }).limit(2).toArray()
);

mongo.get_collection(
    "gis", "test"
).insert([
    {"x": 1},
    {"x": 2}
]);
mongo.close();