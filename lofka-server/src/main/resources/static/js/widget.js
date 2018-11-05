/*
 * Create by YuanYifan in 2017-6-30
 * Update by YuanYifan in 2018-05-24
 */

// import * as echarts from "./echarts.min";

const spin_option = {
    lines: 17, // The number of lines to draw
    length: 10, // The length of each line
    width: 4, // The line thickness
    radius: 10, // The radius of the inner circle
    corners: 1, // Corner roundness (0..1)
    rotate: 0, // The rotation offset
    color: '#000', // #rgb or #rrggbb
    speed: 2.2, // Rounds per second
    trail: 100, // Afterglow percentage
    shadow: false, // Whether to render a shadow
    hwaccel: false, // Whether to use hardware acceleration
    className: 'spinner', // The CSS class to assign to the spinner
    zIndex: 2e9, // The z-index (defaults to 2000000000)
};

// 使用什么主题
const using_theme = 'default';

// “加载中”动画
function loading_anime() {
    return "/static/img/load/load_1.gif"
}

// 获取加载动画的DOM对象
function get_loading_gif() {
    return $("<img>").attr({
        src: "/static/img/load/load_1.gif",
        width: "100%"
    })
}

// 获取失败的GIF
function get_failed_gif() {
    return $("<img>").attr({
        src: "/static/img/fail.gif",
        width: "100%"
    })
}

// 设置默认选项
function set_default_option(option_object, key, default_value) {
    try {
        if (option_object[key] == undefined) {
            option_object[key] = default_value;
        }
    } catch (err) {
        console.warn("Error while setting default value to :" + JSON.stringify(option_object))
    }
}

function default_describe_fun(x, y) {
    var minIndex = y.findIndex(function (x) {
        return x > 0
    });
    y.reverse();
    var maxIndex = y.length - 1 - y.findIndex(function (x) {
        return x > 0
    });
    y.reverse();
    return "Min value:" + x[minIndex] + "Max index:" + x[maxIndex];
}

/**
 * 输入参数，获取一个DIV
 * 1维分布图
 */
function widget_profile_1d(parent_container,
                           profile_id,
                           profile_name,
                           terminal_id_list, from_time, to_time, //Parameters
                           profile_x_axis_define,
                           options) {


    const profile_size = profile_x_axis_define.length;

    let color_list = [
        '#c23531', '#2f4554', '#61a0a8', '#d48265',
        '#91c7ae', '#749f83', '#ca8622', '#bda29a',
        '#6e7074', '#546570', '#c4ccd3'
    ];
    color_list = color_list.sort(function () {
        return (0.5 - Math.random());
    });
    set_default_option(options, "unit", "%");
    set_default_option(options, "div_width", "4");
    set_default_option(options, "color", color_list);
    set_default_option(options, "describe_fun", default_describe_fun);

    var comment_parag = $("<p></p>");

    var widget_block = $("<div></div>")
        .attr({
            "class": "col-md-" + options["div_width"],
            "id": profile_id
        })
        .html(
            $("<div></div>").append( // Append charts
                $("<div></div>").attr({
                    "id": profile_id + "_chart",
                    "style": "height:400px;width:100%"
                }))
        ).append( // Append describe
            $("<div></div>").attr({
                "width": "100%",
                "id": profile_id + "_comment",
                "border": "1"
            }).html(comment_parag)
        );
    parent_container.append(widget_block);
    const spinner = new Spinner(spin_option);
    spinner.spin(document.getElementById(profile_id + "_chart"));
    $.get(
        "/sum/all/",
        {
            start_date: from_time,
            end_date: to_time,
            vehicles: terminal_id_list.join(","),
            data_type: profile_id,
        },
        function (data) {
            spinner.stop();
            const chart_block = $(`#${profile_id}_chart`);
            // 以前是Text，现在是application/json，自动解析了
            const json_object_return = data;
            if (json_object_return["status"] !== "success") {
                chart_block.empty();
                chart_block.append(get_failed_gif());
                console.error(data);
            }
            const y = json_object_return["data"].standarized(100).map(function (val) {
                return leave_point(val, 2)
            });

            $("#" + profile_id + "_chart").empty();
            const velocity_chart = echarts.init(
                document.getElementById(profile_id + "_chart"), using_theme
            );

            const y_accumulate = [];
            let accumulate_sum = 0;
            y.forEach(function (v) {
                accumulate_sum += v;
                y_accumulate.push(leave_point(accumulate_sum, 2));
            });

            velocity_chart.setOption(
                {
                    color: options["color"],
                    title: {
                        text: profile_name + "分布"
                    },
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {
                            type: 'cross',
                            label: {
                                backgroundColor: '#6a7985',
                                formatter: function (params) {
                                    if (params.seriesData.length > 0) {
                                        return profile_name + " " + params.value + " " + options["unit"];
                                    } else {
                                        return "占比：" + Math.round(params.value * 100) / 100 + "%"
                                    }
                                }
                            }
                        }
                    },
                    legend: {
                        data: ['分布图', '累积分布图']
                    },
                    toolbox: {
                        feature: {
                            saveAsImage: {}
                        }
                    },
                    grid: {
                        left: '3%',
                        right: '4%',
                        bottom: '3%',
                        containLabel: true
                    },
                    xAxis: [
                        {
                            type: 'category',
                            boundaryGap: false,
                            data: profile_x_axis_define
                        }
                    ],
                    yAxis: [
                        {
                            type: 'value'
                        },
                        {
                            type: 'value'
                        }
                    ],
                    series: [
                        {
                            name: profile_name + '占比',
                            type: 'bar',
                            label: {
                                normal: {
                                    show: false,
                                    position: 'top',
                                    formatter: '{c}%'
                                }
                            },
                            areaStyle: {normal: {}},
                            data: y
                        },
                        {
                            name: profile_name + '堆积',
                            type: 'line',
                            showSymbol: false,
                            hoverAnimation: false,
                            yAxisIndex: 1,
                            label: {
                                normal: {
                                    show: false,
                                    position: 'top',
                                    formatter: '{c}%'
                                }
                            },
                            data: y_accumulate
                        }
                    ]
                }
            );
            const comment_raw = options["describe_fun"](profile_x_axis_define, y);
            comment_parag.html(comment_raw.replace(new RegExp("\n", "gm"), "<br>"));
        });
}

function get_common_profile_2d_viewer(profile_id, profile_name, x_options, y_options) {
    return function (parameters_dict) {
        var parent_container = parameters_dict.parent_container;
        var terminal_id_list = parameters_dict.vehicle_id_list;
        var from_time = parameters_dict.from_time;
        var to_time = parameters_dict.to_time;
        x_options["margin"] = (x_options["max_value"] - x_options["min_value"]) / x_options["size"];
        y_options["margin"] = (y_options["max_value"] - y_options["min_value"]) / y_options["size"];
        var profile_x_axis_define = range(x_options.size).map(function (i) {
            const start_value = ((i + 0) * x_options.margin + x_options.min_value);
            const end_value = ((i + 1) * x_options.margin + x_options.min_value);
            return `${start_value} ${x_options.unit}\n~\n${end_value} ${x_options.unit}`;
            //return (i * x_options.margin + x_options.min_value) + x_options.unit + "~" + ((i + 1) * x_options.margin + x_options.min_value) + x_options.unit
        });
        var profile_y_axis_define = range(y_options.size).map(function (i) {
            const start_value = ((i + 0) * y_options.margin + y_options.min_value);
            const end_value = ((i + 1) * y_options.margin + y_options.min_value);
            return `${start_value} ${y_options.unit}\n~\n${end_value} ${y_options.unit}`;
            //return (i * y_options.margin + y_options.min_value) + y_options.unit + "~" + ((i + 1) * y_options.margin + y_options.min_value) + y_options.unit
        });
        widget_profile_2d(parent_container, profile_id,
            profile_name,
            terminal_id_list, from_time, to_time, //Parameters
            profile_x_axis_define,
            profile_y_axis_define,
            {
                unit: "%",
                div_width: parameters_dict.div_size * 1.5,
                xname: x_options.name,
                yname: y_options.name
            });
    }
}


function get_common_profile_1d_viewer(profile_id, profile_name, unit, min_value, max_value, size) {
    const margin = (max_value - min_value) / size;
    return function (parameters_dict) {
        var parent_container = parameters_dict.parent_container;
        var terminal_id_list = parameters_dict.vehicle_id_list;
        var from_time = parameters_dict.from_time;
        var to_time = parameters_dict.to_time;
        var div_size = parameters_dict.div_size;
        widget_profile_1d(// Append 1d distribute to axis
            parent_container,
            profile_id,
            profile_name,
            terminal_id_list, from_time, to_time, //Parameters
            range(size).map(function (i) {
                return leave_point(i * margin + min_value, 2) + unit + "~" + leave_point((i + 1) * margin + min_value, 2) + unit
            }),
            {
                unit: unit,
                div_width: div_size,
                describe_fun: function (x, y) {
                    var mean_value = 0;
                    var sumy = 0;
                    range(size).forEach(function (i) {
                        mean_value += (i * margin + min_value + min_value / 2.0) * y[i];
                        sumy += y[i]
                    });
                    mean_value /= sumy;
                    var average_info = "平均值: " + leave_point(mean_value, 1) + " " + unit + "\n";
                    var max_value_info = "最大值所在范围: ";
                    var isbreak = false;
                    for (var i = size - 1; i >= 0; i--) {
                        if (y[i] > 0) {
                            max_value_info += x[i] + "\n";
                            isbreak = true;
                            break;
                        }
                    }

                    if (!isbreak) {
                        max_value_info += "无法计算！\n";
                    }

                    var min_value_info = "最小值所在范围：";
                    var minIndex = y.findIndex(function (x) {
                        return x > 0
                    });
                    if (minIndex < 0) {
                        min_value_info += "无法计算！\n";
                    } else {
                        min_value_info += x[minIndex] + "\n";
                    }


                    return "<h4>" + profile_name + "统计信息</h4>" + average_info + max_value_info + min_value_info;
                }
            }
        );
    }
}

function widget_profile_2d(// Append 1d distribute to axis
    parent_container,
    profile_id,
    profile_name,
    terminal_id_list, from_time, to_time, //Parameters
    profile_x_axis_define,
    profile_y_axis_define,
    options) {

    var profile_size_x = profile_x_axis_define.length;
    var profile_size_y = profile_y_axis_define.length;
    set_default_option(options, "unit", "%");
    set_default_option(options, "div_width", "6");
    set_default_option(options, "xname", "X");
    set_default_option(options, "yname", "Y");
    var widget_block = $("<div></div>")
        .attr({
            "class": "col-md-" + options["div_width"],
            "id": profile_id
        })
        .html($("<div></div>")
            .append( // Append charts
                $("<div></div>").attr({
                    "id": profile_id + "_chart",
                    "style": "height:500px;width:100%"
                })
            )
        );

    parent_container.append(widget_block);
    var spinner = new Spinner(spin_option);
    spinner.spin(document.getElementById(profile_id + "_chart"));

    $.get(
        "/sum/all/",
        {
            start_date: from_time,
            end_date: to_time,
            vehicles: terminal_id_list.join(","),
            data_type: profile_id,
        },
        function (data) {
            spinner.stop();
            //Get a 2d-map
            const chart_block = $(`#${profile_id}_chart`);
            const json_object_return = data;
            if (json_object_return["status"] !== "success") {
                chart_block.empty();
                chart_block.append(get_failed_gif());
                console.error(data)
            }

            var y = json_object_return["data"];

            var sum_count = y.sum2();

            y = y.map2(
                function (val) {
                    return leave_point(val * 100 / sum_count, 2);
                }
            );

            var heat_data = [];
            var max_value = 0;

            for (var i = 0; i < y.length; i++) {
                var sub_array = y[i];
                for (var j = 0; j < sub_array.length; j++) {
                    var current_value = sub_array[j];
                    heat_data.push([i, j, current_value || "-"]);
                    if (current_value > max_value) {
                        max_value = current_value;
                    }
                }
            }


            var min_y_index = Math.floor((y.map(
                function (subarr) {
                    return subarr.findIndex(
                        function (x) {
                            return x > 0
                        }
                    );
                }
            ).map(
                function (x) {
                    if (x < 0) {
                        return 32768
                    } else {
                        return x
                    }
                }
            ).min()) / profile_size_y * 100);

            var max_y_index = Math.ceil((y.map(
                function (subarr) {
                    return subarr.reverse_findIndex(
                        function (x) {
                            return x > 0
                        }
                    );
                }
            ).max() + 1) / profile_size_y * 100);

            var max_x_index = Math.ceil((y.map(
                function (subarr) {
                    return subarr.sum()
                }
            ).reverse_findIndex(
                function (x) {
                    return x > 0
                }
            ) + 1) / profile_size_x * 100);

            var min_x_index = Math.floor((y.map(
                function (subarr) {
                    return subarr.sum()
                }
            ).findIndex(
                function (x) {
                    return x > 0
                }
            ) - 1) / profile_size_x * 100);
            chart_block.empty();
            var heatmap_chart = echarts.init(
                document.getElementById(profile_id + "_chart"), using_theme
            );

            heatmap_chart.setOption({
                    title: {
                        text: profile_name + "热力图"
                    },
                    tooltip: {
                        trigger: "item"
                    },
                    animation: false,
                    grid: {
                        height: '72%',
                        y: '8%'
                    },
                    xAxis: {
                        type: 'category',
                        data: profile_x_axis_define,
                        axisLabel: {
                            rotate: 45
                        },
                        splitArea: {
                            show: true
                        }
                    },
                    yAxis: {
                        type: 'category',
                        data: profile_y_axis_define,
                        axisLabel: {
                            rotate: 45
                        },
                        splitArea: {
                            show: true
                        }
                    },
                    dataZoom: [
                        {
                            type: 'slider',
                            show: true,
                            xAxisIndex: [0],
                            start: min_x_index,
                            end: max_x_index,
                            bottom: "0%"
                        },
                        {
                            type: 'slider',
                            show: true,
                            yAxisIndex: [0],
                            left: '93%',
                            start: min_y_index,
                            end: max_y_index
                        }
                    ],
                    visualMap: {
                        min: 0,
                        max: max_value,
                        calculable: true,
                        left: "0%",
                        top: "25%",
                        padding: 0
                    },
                    series: [
                        {
                            name: profile_name + "热力图",
                            type: 'heatmap',
                            data: heat_data,
                            label: {
                                normal: {
                                    show: true
                                }
                            },
                            itemStyle: {
                                emphasis: {
                                    shadowBlur: 10,
                                    shadowColor: 'rgba(0, 0, 0, 0.55)'
                                }
                            },
                            tooltip: {
                                position: 'top',
                                formatter: function (val) {
                                    const posinfo = val.data;
                                    return "%s:%s  %s:%s 热力值:%s(%%)".sprintf(
                                        options.xname,
                                        profile_x_axis_define[posinfo[0]],
                                        options.yname,
                                        profile_y_axis_define[posinfo[1]],
                                        leave_point(posinfo[2], 2) + ""
                                    );
                                }
                            }
                        }
                    ]
                }
            );
        });
    return widget_block;
}

function widgt_geo_heatmap() {
    return function (parameters_dict) {
        var parent_container = parameters_dict.parent_container;
        var terminal_id_list = parameters_dict.vehicle_id_list;
        var from_time = parameters_dict.from_time;
        var to_time = parameters_dict.to_time;
        var div_size = parameters_dict.div_size;

        var widget_block = $("<div></div>")
            .attr({
                "class": "col-md-" + 6,
            })
            .html($("<div></div>")
                .append( // Append charts
                    $("<div></div>").attr({
                        "id": "geo_grid_heatmap_chart",
                        "style": "height:500px;width:100%"
                    })
                )
            );
        parent_container.append(widget_block);
        var spinner = new Spinner(spin_option);
        spinner.spin(document.getElementById("geo_grid_heatmap_chart"));
        $.get('/api/gps/heat',
            {
                from_time: from_time,
                to_time: to_time,
                terminals: JSON.stringify(terminal_id_list),
                compress: parameters_dict.gps_compress,
                resample: parameters_dict.gps_resample
            },
            function (data) {
                spinner.stop();
                var chart_block = $("geo_grid_heatmap_chart");
                var data = JSON.parse(data);
                if (data["status"] == "error") {
                    chart_block.empty();
                    chart_block.append(get_failed_gif());
                } else {
                    if (!isSupportCanvas()) {
                        alert('热力图目前只支持有canvas支持的浏览器,您所使用的浏览器不能使用热力图功能~');
                        chart_block.empty();
                        chart_block.append(get_failed_gif());
                        return;
                    }
                }
                var max_count = 100;
                var heatmapData = data.map(
                    function (point_info) {
                        if (point_info[2] > max_count) {
                            max_count = point_info[2];
                        }
                        return {
                            lng: point_info[0],
                            lat: point_info[1],
                            count: point_info[2]
                        }
                    }
                );

                chart_block.empty();

                var map = new AMap.Map("geo_grid_heatmap_chart", {
                    resizeEnable: true,
                    center: [105, 35],
                    zoom: 4
                });

                map.plugin(["AMap.ToolBar"], function () {
                    map.addControl(new AMap.ToolBar());
                });

                if (location.href.indexOf('&guide=1') !== -1) {
                    map.setStatus({scrollWheel: false})
                }

                var heatmap;
                map.plugin(["AMap.Heatmap"], function () {
                    //初始化heatmap对象
                    heatmap = new AMap.Heatmap(map, {
                        radius: 5, //给定半径
                        opacity: [0, 0.8]
                    });
                    //设置数据集：该数据为北京部分“公园”数据
                    heatmap.setDataSet({
                        data: heatmapData,
                        max: max_count
                    });
                });

            });
    }
}

function widget_geo_trackmap() {
    return function (parameters_dict) {
        var parent_container = parameters_dict.parent_container;
        var terminal_id_list = parameters_dict.vehicle_id_list;
        var from_time = parameters_dict.from_time;
        var to_time = parameters_dict.to_time;
        var div_size = parameters_dict.div_size;
        var widget_block = $("<div></div>")
            .attr({
                "class": "col-md-" + 6,
            })
            .html($("<div></div>")
                .append( // Append charts
                    $("<div></div>").attr({
                        "id": "geo_track_heatmap_chart",
                        "style": "height:500px;width:100%"
                    }).append($("<img>").attr(
                        {
                            src: loading_anime(),
                            width: "100%"
                        })
                    )
                )
            );
        parent_container.append(widget_block);
        $.get('/api/gps/sparse',
            {
                from_time: from_time,
                to_time: to_time,
                terminals: JSON.stringify(terminal_id_list)
            },
            function (data) {
                var chart_block = $("geo_track_heatmap_chart");
                var json_object_return = JSON.parse(data);
                if (json_object_return["status"] == "error") {
                    chart_block.empty();
                    chart_block.append(get_failed_gif());
                }
                var lines = json_object_return.map(function (track) {
                    return {
                        coords: track
                    };
                });
                chart_block.empty();
                var geo_heatmap_chart = echarts.init(
                    document.getElementById("geo_track_heatmap_chart")
                );
                geo_heatmap_chart.setOption({
                    title: {
                        text: "轨迹/热力图"
                    },
                    bmap: {
                        center: [105, 35],
                        zoom: 5,
                        roam: true
                    },
                    series: [{
                        type: 'lines',
                        coordinateSystem: 'bmap',
                        data: lines,
                        polyline: true,
                        lineStyle: {
                            normal: {
                                color: '#f600f6',
                                opacity: 0.5,
                                width: 4
                            }
                        }
                    }]
                });
            });
    }
}

function get_common_image_viewe(image_type, div_width) {
    return function (parameters_dict) {
        const parent_container = parameters_dict.parent_container;
        const widget_block = $("<div></div>")
            .attr({
                "class": "col-md-" + div_width,
                "id": "imgs-" + image_type
            })
            .html(
                $("<img>").attr({
                    src: `/img/${image_type}/?start_date=${
                        parameters_dict["from_time"]
                        }&end_date=${
                        parameters_dict["to_time"]
                        }&vehicles=${
                        parameters_dict["vehicle_id_list"].join(",")
                        }`,
                    width: "100%"
                })
            );
        parent_container.append(
            widget_block
        )
    };
}

/**
 * 分析工具集合
 */
const analysis_widgets = {
    /*
     GPS: {
     name: "GPS轨迹（压缩）查看",
     describe: "查看压缩过后的GPS轨迹",
     widget: widget_geo_trackmap()
     },
    GPSHeat: {
        name: "GPS热力查看",
        describe: "查看GPS热力",
        widget: widgt_geo_heatmap()
    },
    */

    wind_vehicle: {
        name: "车辆受风情况统计",
        describe: "统计车辆受风情况，检测横风危机～",
        widget: get_common_image_viewe(
            "wind_vehicle", 4
        )
    },

    work_hours: {
        name: "车辆工作时间情况统计（静态）",
        describe: "统计车辆常在何时运行",
        widget: get_common_image_viewe(
            "work_hours", 4
        )
    },

    work_hours_echart: { // CVNAVIed
        name: "车辆工作时间情况统计",
        describe: "统计车辆常在何时运行",
        widget: get_common_profile_1d_viewer("work_hours", "工作时间", "时", 0, 24, 24)
    },

    velocity: { // CVNAVIed
        name: "车速分布",
        describe: "查看不同车速的占比",
        widget: get_common_profile_1d_viewer("pd_pspeed", "车速", "km/h", -4, 121, 25)
    },

    torque: { // CVNAVIed
        name: "扭矩分布",
        describe: "查看不同扭矩的占比",
        widget: get_common_profile_1d_viewer("pd_torque", "扭矩", "%", -10, 110, 24)
    },
    rev: { // CVNAVIed
        name: "转速分布",
        describe: "查看不同转速的占比",
        widget: get_common_profile_1d_viewer("pd_rev", "转速", "rpm", 500, 2500, 40)
    },

    coolant_temperature: { // CVNAVIed
        name: "冷却剂水温分布",
        describe: "查看冷却剂水温的分布情况，有助于了解发动机工况和状态。",
        widget: get_common_profile_1d_viewer("pd_coolant_temperature", "冷却剂水温", "℃", -40, 100, 28)
    },
    acce: { // CVNAVIed
        name: "加速度分布",
        describe: "查看加速度分布情况，了解司机驾驶行为。",
        widget: get_common_profile_1d_viewer("pd_accelerate", "加速度", "m/s²", -3, 3, 30)
    },
    imm_fuel: { // CVNAVIed
        name: "瞬时油耗分布",
        describe: "查看瞬时油耗分布",
        widget: get_common_profile_1d_viewer("pd_instfuel", "瞬时油耗", "-", 0, 10, 20)
    },
    fuel_rate: { // CVNAVIed
        name: "油耗率分布",
        describe: "查看油耗率分布",
        widget: get_common_profile_1d_viewer("pd_fuelrate", "油耗率", "-", 0, 80, 20)
    },

    engine_rev_torque_map: {
        name: "转速扭矩联合分布",
        describe: "查看发动机运行的工况",
        widget: get_common_profile_2d_viewer("pd_rev_torque", "转速-扭矩联合",
            {
                name: "转速",
                min_value: 500,
                max_value: 2500,
                unit: "rpm",
                size: 20
            },
            {
                name: "扭矩",
                min_value: -10,
                max_value: 110,
                unit: "%",
                size: 12
            }
        )
    },

    power_require_map: {
        name: "车速-转矩联合分布",
        describe: "车速-转矩联合分布可以看出车辆对动力的需求",
        widget: get_common_profile_2d_viewer("pd_speed_torque", "车速-转矩联合",
            {
                name: "车速",
                min_value: -4,
                max_value: 121,
                unit: "km/h",
                size: 25
            },
            {
                name: "扭矩",
                min_value: -10,
                max_value: 110,
                unit: "%",
                size: 12
            },
        )
    },

    engine_map: {
        name: "发动机MAP图（静态）",
        describe: "发动机MAP图热力",
        widget: get_common_image_viewe(
            "engine_map", 6
        )
    },


};